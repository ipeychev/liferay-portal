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
import java.util.Comparator;
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
 * Standalone Object Action that commits a Content Generator Run by merging the
 * JSON of its artifacts into a single array and submitting it to the Headless
 * Batch Engine through the "CMSBlog" task item delegate.
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

	private boolean _awaitTerminal(long taskId) throws InterruptedException {
		long deadline = System.currentTimeMillis() + _TIMEOUT_MS;

		while (System.currentTimeMillis() < deadline) {
			Thread.sleep(_POLL_INTERVAL_MS);

			BatchEngineImportTask batchEngineImportTask =
				_batchEngineImportTaskLocalService.fetchBatchEngineImportTask(
					taskId);

			if (batchEngineImportTask == null) {
				continue;
			}

			String status = batchEngineImportTask.getExecuteStatus();

			if (Objects.equals(
					BatchEngineTaskExecuteStatus.FAILED.name(), status)) {

				return false;
			}

			if (Objects.equals(
					BatchEngineTaskExecuteStatus.COMPLETED.name(), status)) {

				return true;
			}
		}

		_log.error(
			StringBundler.concat(
				"Batch task ", taskId, " timed out after ", _TIMEOUT_MS, "ms"));

		return false;
	}

	private void _finalizeRun(
		long userId, long runId, boolean failed, String failureReason) {

		try {
			Map<String, Serializable> updates = new HashMap<>(
				_objectEntryLocalService.getValues(runId));

			if (failed) {
				updates.put("runStatus", _RUN_STATUS_FAILED);

				if (failureReason != null) {
					updates.put("failureReason", failureReason);
				}
			}
			else {
				updates.put("committedAt", new Date());
				updates.put("failureReason", "");
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

	private void _runCommit(long companyId, long userId, long runId) {
		try {
			ObjectEntry runObjectEntry =
				_objectEntryLocalService.getObjectEntry(runId);

			String runERC = runObjectEntry.getExternalReferenceCode();

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
				Comparator.comparingInt(a -> GetterUtil.getInteger(
					artifactValues.get(
						a.getObjectEntryId()
					).get(
						"loadOrder"
					))));

			if (artifacts.isEmpty()) {
				throw new PortalException(
					"Run " + runERC + " has no artifacts to commit");
			}

			JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

			for (ObjectEntry artifact : artifacts) {
				String json = GetterUtil.getString(
					artifactValues.get(
						artifact.getObjectEntryId()
					).get(
						"json"
					));

				if (Validator.isBlank(json)) {
					continue;
				}

				JSONArray artifactItems = JSONFactoryUtil.createJSONArray(json);

				for (int i = 0; i < artifactItems.length(); i++) {
					jsonArray.put(artifactItems.getJSONObject(i));
				}
			}

			if (jsonArray.length() == 0) {
				throw new PortalException(
					"Run " + runERC + " has no items to commit");
			}

			Map<String, Serializable> runValues =
				_objectEntryLocalService.getValues(runId);

			Map<String, Serializable> updates = new HashMap<>(runValues);

			updates.put("committedAt", null);
			updates.put("runStatus", _RUN_STATUS_GENERATING);

			_objectEntryLocalService.updateObjectEntry(
				userId, runId, 0L, updates, new ServiceContext());

			BatchEngineTaskItemDelegate<?> batchEngineTaskItemDelegate =
				_batchEngineTaskItemDelegateRegistry.
					getBatchEngineTaskItemDelegate(
						companyId, _OBJECT_ENTRY_CLASS_NAME, _DELEGATE_NAME);

			if (batchEngineTaskItemDelegate == null) {
				throw new PortalException(
					StringBundler.concat(
						"No batch engine task item delegate registered for ",
						_OBJECT_ENTRY_CLASS_NAME, " / ", _DELEGATE_NAME));
			}

			Map<String, Serializable> parameters = new HashMap<>();

			parameters.put("createStrategy", "UPSERT");
			parameters.put("siteExternalReferenceCode", "Space1");
			parameters.put("updateStrategy", "UPDATE");

			BatchEngineImportTask batchEngineImportTask =
				_batchEngineImportTaskLocalService.addBatchEngineImportTask(
					null, companyId, userId, 100, null,
					_OBJECT_ENTRY_CLASS_NAME,
					_zipJSON(_FILE_NAME, jsonArray.toString()), "JSON",
					BatchEngineTaskExecuteStatus.INITIAL.name(), null,
					BatchEngineImportTaskConstants.IMPORT_STRATEGY_ON_ERROR_FAIL,
					"CREATE", parameters, _DELEGATE_NAME);

			_batchEngineImportTaskExecutor.execute(
				batchEngineImportTask, batchEngineTaskItemDelegate, true);

			boolean succeeded = _awaitTerminal(
				batchEngineImportTask.getBatchEngineImportTaskId());

			_finalizeRun(
				userId, runId, !succeeded,
				succeeded ? null :
					"Batch import failed; see batch engine task errors");
		}
		catch (Exception exception) {
			_log.error("Commit failed for run " + runId, exception);

			try {
				_finalizeRun(
					userId, runId, true,
					"Commit failed: " + exception.getMessage());
			}
			catch (Exception finalizeException) {
				_log.error(
					"Unable to mark run failed: " + runId, finalizeException);
			}
		}
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

	private static final String
		_ARTIFACT_OBJECT_DEFINITION_EXTERNAL_REFERENCE_CODE = "L_CSG_ARTIFACT";

	private static final String _ARTIFACT_RUN_FK_FIELD =
		"r_artifacts_l_contentGeneratorRunId";

	private static final String _DELEGATE_NAME = "CMSBlog";

	private static final String _FILE_NAME = "blogs.json";

	private static final String _OBJECT_ENTRY_CLASS_NAME =
		"com.liferay.object.rest.dto.v1_0.ObjectEntry";

	private static final long _POLL_INTERVAL_MS = 2_000L;

	private static final String _RUN_STATUS_COMMITTED = "committed";

	private static final String _RUN_STATUS_FAILED = "failed";

	private static final String _RUN_STATUS_GENERATING = "generating";

	private static final long _TIMEOUT_MS = 10L * 60L * 1_000L;

	private static final Log _log = LogFactoryUtil.getLog(
		CommitContentSiteGeneratorRunObjectActionExecutor.class);

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
