/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.site.generator.internal.object.action.executor;

import com.liferay.batch.engine.BatchEngineImportTaskExecutor;
import com.liferay.batch.engine.BatchEngineTaskExecuteStatus;
import com.liferay.batch.engine.BatchEngineTaskItemDelegate;
import com.liferay.batch.engine.BatchEngineTaskItemDelegateRegistry;
import com.liferay.batch.engine.constants.BatchEngineImportTaskConstants;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.batch.engine.service.BatchEngineImportTaskLocalService;
import com.liferay.object.action.executor.BaseObjectActionExecutor;
import com.liferay.object.action.executor.ObjectActionExecutor;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.executor.PortalExecutorManager;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Standalone Object Action that commits a Content Generator Run by submitting
 * each of its artifacts to the Headless Batch Engine.
 *
 * Invoked by the auto-generated endpoint:
 *   PUT /o/content-site-generator/runs/by-external-reference-code/{erc}/object-actions/commit
 *   PUT /o/content-site-generator/runs/{id}/object-actions/commit
 *
 * @author Gabriel Albuquerque
 */
@Component(
	property = "object.action.executor.key=" + CommitContentSiteGeneratorRunObjectActionExecutor.KEY,
	service = ObjectActionExecutor.class
)
public class CommitContentSiteGeneratorRunObjectActionExecutor
	extends BaseObjectActionExecutor {

	public static final String EXECUTOR_NAME = "CSG-CommitMonitor";

	public static final String KEY = "commit-content-site-generator-run";

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	protected void doExecute(
			long companyId, long objectActionId,
			UnicodeProperties parametersUnicodeProperties,
			JSONObject payloadJSONObject, long userId)
		throws Exception {

		// Standalone Object Actions execute as a transaction commit callback,
		// so by this point the originating request transaction is already
		// closed. Defer all DB work to a background thread where each local
		// service call manages its own transaction.

		long runId = payloadJSONObject.getLong("classPK");

		ExecutorService executorService =
			_portalExecutorManager.getPortalExecutor(EXECUTOR_NAME);

		executorService.submit(() -> _runCommit(companyId, userId, runId));
	}

	private void _runCommit(long companyId, long userId, long runId) {
		try {
			ObjectEntry runObjectEntry =
				_objectEntryLocalService.getObjectEntry(runId);

			String runERC = runObjectEntry.getExternalReferenceCode();

			Map<String, Serializable> runValues =
				_objectEntryLocalService.getValues(runId);

			// 1. Guard: only commit from READY or FAILED states.

			String currentStatus = GetterUtil.getString(
				runValues.get("runStatus"));

			if (!_committableStates.contains(currentStatus)) {
				throw new PortalException(
					StringBundler.concat(
						"Run ", runERC, " is not in a committable state: ",
						currentStatus));
			}

			// 2. Resolve the Artifact ObjectDefinition.

			ObjectDefinition artifactObjectDefinition =
				_objectDefinitionLocalService.
					fetchObjectDefinitionByExternalReferenceCode(
						_ARTIFACT_OBJECT_DEFINITION_EXTERNAL_REFERENCE_CODE,
						companyId);

			if (artifactObjectDefinition == null) {
				throw new PortalException(
					"ContentGeneratorArtifact ObjectDefinition is not " +
						"registered");
			}

			// 3. Load all artifacts whose run FK matches this run, ordered by
			// loadOrder. The FK column on Artifact is
			// r_artifacts_l_contentGeneratorRunId.

			List<ObjectEntry> allArtifacts =
				_objectEntryLocalService.getObjectEntries(
					0, artifactObjectDefinition.getObjectDefinitionId(), -1,
					-1);

			Map<Long, Map<String, Serializable>> artifactValues =
				new HashMap<>();

			List<ObjectEntry> artifacts = new ArrayList<>();

			for (ObjectEntry artifact : allArtifacts) {
				Map<String, Serializable> values =
					_objectEntryLocalService.getValues(
						artifact.getObjectEntryId());

				if (GetterUtil.getLong(values.get(_ARTIFACT_RUN_FK_FIELD)) ==
						runId) {

					artifactValues.put(artifact.getObjectEntryId(), values);
					artifacts.add(artifact);
				}
			}

			artifacts.sort(
				(a, b) -> Integer.compare(
					GetterUtil.getInteger(
						artifactValues.get(
							a.getObjectEntryId()
						).get(
							"loadOrder"
						)),
					GetterUtil.getInteger(
						artifactValues.get(
							b.getObjectEntryId()
						).get(
							"loadOrder"
						))));

			if (artifacts.isEmpty()) {
				throw new PortalException(
					"Run " + runERC + " has no artifacts to commit");
			}

			// 4. Flip the Run to GENERATING. Picklist values are written as
			// their raw key string.

			Map<String, Serializable> updates = new HashMap<>(runValues);

			updates.put("committedAt", null);
			updates.put("runStatus", _RUN_STATUS_GENERATING);

			_objectEntryLocalService.updateObjectEntry(
				userId, runId, 0L, updates, new ServiceContext());

			// 5. Submit each artifact to the Batch Engine. Each artifact's
			// `json` field stores a {configuration, items} envelope: the
			// configuration carries className / parameters / delegate name,
			// and only the items array is shipped to the Batch Engine.

			List<Long> taskIds = new ArrayList<>(artifacts.size());

			for (ObjectEntry artifact : artifacts) {
				Map<String, Serializable> values = artifactValues.get(
					artifact.getObjectEntryId());

				String fileName = GetterUtil.getString(values.get("fileName"));
				String json = GetterUtil.getString(values.get("json"));

				if (json.isEmpty()) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							"Skipping artifact " + fileName +
								" due to missing envelope");
					}

					continue;
				}

				BatchEngineImportTask batchEngineImportTask = _submitEnvelope(
					companyId, userId, fileName, json);

				if (batchEngineImportTask == null) {
					continue;
				}

				taskIds.add(batchEngineImportTask.getBatchEngineImportTaskId());
			}

			// 6. Monitor the batch engine and finalize when all tasks are done.

			_monitorAndFinalize(userId, runId, taskIds);
		}
		catch (Exception exception) {
			_log.error("Commit failed for run " + runId, exception);

			try {
				_finalizeRun(userId, runId, true);
			}
			catch (Exception finalizeException) {
				_log.error(
					"Unable to mark run failed: " + runId, finalizeException);
			}
		}
	}

	private void _finalizeRun(long userId, long runId, boolean failed) {
		try {
			Map<String, Serializable> updates = new HashMap<>(
				_objectEntryLocalService.getValues(runId));

			if (failed) {
				updates.put("runStatus", _RUN_STATUS_FAILED);
			}
			else {
				updates.put("committedAt", new Date());
				updates.put("runStatus", _RUN_STATUS_COMMITTED);
			}

			_objectEntryLocalService.updateObjectEntry(
				userId, runId, 0L, updates, new ServiceContext());
		}
		catch (Exception exception) {
			_log.error(
				StringBundler.concat(
					"Unable to finalize run ", runId, " (failed=", failed, ")"),
				exception);
		}
	}

	private BatchEngineImportTask _submitEnvelope(
			long companyId, long userId, String fileName, String envelopeJSON)
		throws Exception {

		JSONObject envelope = JSONFactoryUtil.createJSONObject(envelopeJSON);

		JSONObject configuration = envelope.getJSONObject("configuration");

		if (configuration == null) {
			_log.warn(
				"Skipping artifact " + fileName +
					" because envelope has no configuration");

			return null;
		}

		String className = configuration.getString("className");

		if (Validator.isNull(className)) {
			_log.warn(
				"Skipping artifact " + fileName +
					" because configuration has no className");

			return null;
		}

		String taskItemDelegateName = GetterUtil.getString(
			configuration.getString("taskItemDelegateName"), "DEFAULT");

		Map<String, Serializable> parameters = _toSerializableMap(
			configuration.getJSONObject("parameters"));

		Map<String, String> fieldNameMappingMap = _toStringMap(
			configuration.getJSONObject("fieldNameMappingMap"));

		JSONArray items = envelope.getJSONArray("items");

		if ((items == null) || (items.length() == 0)) {
			_log.warn("Skipping artifact " + fileName + " because items is empty");

			return null;
		}

		BatchEngineTaskItemDelegate<?> batchEngineTaskItemDelegate =
			_batchEngineTaskItemDelegateRegistry.getBatchEngineTaskItemDelegate(
				companyId, className, taskItemDelegateName);

		if (batchEngineTaskItemDelegate == null) {
			_log.warn(
				StringBundler.concat(
					"Skipping artifact ", fileName, " because no delegate ",
					"registered for ", className, " / ", taskItemDelegateName));

			return null;
		}

		BatchEngineImportTask batchEngineImportTask =
			_batchEngineImportTaskLocalService.addBatchEngineImportTask(
				null, companyId, userId, 100, null, className,
				_zipJSON(fileName, items.toString()), "JSON",
				BatchEngineTaskExecuteStatus.INITIAL.name(), fieldNameMappingMap,
				BatchEngineImportTaskConstants.IMPORT_STRATEGY_ON_ERROR_FAIL,
				"CREATE", parameters, taskItemDelegateName);

		_batchEngineImportTaskExecutor.execute(
			batchEngineImportTask, batchEngineTaskItemDelegate, true);

		return batchEngineImportTask;
	}

	private Map<String, Serializable> _toSerializableMap(
		JSONObject jsonObject) {

		if (jsonObject == null) {
			return null;
		}

		Map<String, Serializable> map = new HashMap<>();

		for (String key : jsonObject.keySet()) {
			Object value = jsonObject.get(key);

			if (value instanceof Serializable) {
				map.put(key, (Serializable)value);
			}
			else if (value != null) {
				map.put(key, value.toString());
			}
		}

		return map;
	}

	private Map<String, String> _toStringMap(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}

		Map<String, String> map = new HashMap<>();

		for (String key : jsonObject.keySet()) {
			map.put(key, jsonObject.getString(key));
		}

		return map;
	}

	private byte[] _zipJSON(String fileName, String json) throws Exception {
		ByteArrayOutputStream byteArrayOutputStream =
			new ByteArrayOutputStream();

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(
				byteArrayOutputStream)) {

			zipOutputStream.putNextEntry(new ZipEntry(fileName));
			zipOutputStream.write(json.getBytes(StandardCharsets.UTF_8));
			zipOutputStream.closeEntry();
		}

		return byteArrayOutputStream.toByteArray();
	}

	private void _monitorAndFinalize(
		long userId, long runId, List<Long> taskIds) {

		long deadline = System.currentTimeMillis() + _TIMEOUT_MS;

		try {
			while (System.currentTimeMillis() < deadline) {
				Thread.sleep(_POLL_INTERVAL_MS);

				boolean anyFailed = false;
				boolean allTerminal = true;

				for (long taskId : taskIds) {
					BatchEngineImportTask task =
						_batchEngineImportTaskLocalService.
							fetchBatchEngineImportTask(taskId);

					if (task == null) {
						allTerminal = false;

						continue;
					}

					String status = task.getExecuteStatus();

					if (Objects.equals(
							BatchEngineTaskExecuteStatus.FAILED.name(),
							status)) {

						anyFailed = true;
					}
					else if (!Objects.equals(
								BatchEngineTaskExecuteStatus.COMPLETED.name(),
								status)) {

						allTerminal = false;
					}
				}

				if (allTerminal) {
					_finalizeRun(userId, runId, anyFailed);

					return;
				}
			}

			// Timed out — mark FAILED so the run can be retried.

			_log.error(
				StringBundler.concat(
					"Commit monitor for run ", runId, " timed out after ",
					_TIMEOUT_MS, "ms"));

			_finalizeRun(userId, runId, true);
		}
		catch (InterruptedException interruptedException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Commit monitor interrupted for run " + runId,
					interruptedException);
			}

			Thread.currentThread(
			).interrupt();
		}
		catch (Exception exception) {
			_log.error("Commit monitor failed for run " + runId, exception);
		}
	}

	private static final String
		_ARTIFACT_OBJECT_DEFINITION_EXTERNAL_REFERENCE_CODE = "L_CSG_ARTIFACT";

	private static final String _ARTIFACT_RUN_FK_FIELD =
		"r_artifacts_l_contentGeneratorRunId";

	private static final long _POLL_INTERVAL_MS = 2_000L;

	private static final String _RUN_STATUS_COMMITTED = "committed";

	private static final String _RUN_STATUS_FAILED = "failed";

	private static final String _RUN_STATUS_GENERATING = "generating";

	private static final long _TIMEOUT_MS = 10L * 60L * 1_000L;

	private static final Log _log = LogFactoryUtil.getLog(
		CommitContentSiteGeneratorRunObjectActionExecutor.class);

	private static final List<String> _committableStates = List.of(
		"ready", "failed");

	@Reference
	private BatchEngineImportTaskExecutor _batchEngineImportTaskExecutor;

	@Reference
	private BatchEngineImportTaskLocalService
		_batchEngineImportTaskLocalService;

	@Reference
	private BatchEngineTaskItemDelegateRegistry
		_batchEngineTaskItemDelegateRegistry;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private PortalExecutorManager _portalExecutorManager;

}