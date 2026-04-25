/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.site.generator.internal.object.action.executor;

import com.liferay.batch.engine.BatchEngineTaskExecuteStatus;
import com.liferay.batch.engine.constants.BatchEngineImportTaskConstants;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.batch.engine.service.BatchEngineImportTaskLocalService;
import com.liferay.object.action.executor.BaseObjectActionExecutor;
import com.liferay.object.action.executor.ObjectActionExecutor;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.persistence.ObjectEntryPersistence;
import com.liferay.petra.executor.PortalExecutorManager;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.UnicodeProperties;

import java.io.Serializable;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

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

		// Standalone action payloads ship the entry id under "classPK". Load
		// the entry directly to read field values reliably.

		long runId = payloadJSONObject.getLong("classPK");

		ObjectEntry runObjectEntry = _objectEntryLocalService.getObjectEntry(
			runId);

		String runERC = runObjectEntry.getExternalReferenceCode();

		Map<String, Serializable> runValues = runObjectEntry.getValues();

		// 1. Guard: only commit from READY or FAILED states.

		String currentStatus = GetterUtil.getString(runValues.get("runStatus"));

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
				"ContentGeneratorArtifact ObjectDefinition is not registered");
		}

		// 3. Load all artifacts whose run FK matches this run, ordered by
		// loadOrder. Company-scoped Object entries use groupId 0; the FK
		// column on Artifact is r_artifacts_l_contentGeneratorRunId.

		List<ObjectEntry> artifacts =
			_objectEntryPersistence.findByObjectDefinitionId(
				artifactObjectDefinition.getObjectDefinitionId(), 0, -1);

		artifacts.removeIf(
			artifact ->
				GetterUtil.getLong(
					artifact.getValues(
					).get(
						_ARTIFACT_RUN_FK_FIELD
					)) != runId);

		artifacts.sort(
			(a, b) -> Integer.compare(
				GetterUtil.getInteger(
					a.getValues(
					).get(
						"loadOrder"
					)),
				GetterUtil.getInteger(
					b.getValues(
					).get(
						"loadOrder"
					))));

		if (artifacts.isEmpty()) {
			throw new PortalException(
				"Run " + runERC + " has no artifacts to commit");
		}

		// 4. Flip the Run to GENERATING. Picklist values are written as their
		// raw key string.

		Map<String, Serializable> updates = new HashMap<>();

		updates.put("committedAt", null);
		updates.put("runStatus", _RUN_STATUS_GENERATING);

		_objectEntryLocalService.updateObjectEntry(
			userId, runId, 0L, updates, new ServiceContext());

		// 5. Submit each artifact to the Batch Engine.

		List<Long> taskIds = new ArrayList<>(artifacts.size());

		for (ObjectEntry artifact : artifacts) {
			Map<String, Serializable> values = artifact.getValues();

			String fileName = GetterUtil.getString(values.get("fileName"));
			String className = GetterUtil.getString(values.get("className"));
			String json = GetterUtil.getString(values.get("json"));

			if (className.isEmpty() || json.isEmpty()) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Skipping artifact " + fileName +
							" due to missing className or json");
				}

				continue;
			}

			BatchEngineImportTask batchEngineImportTask =
				_batchEngineImportTaskLocalService.addBatchEngineImportTask(
					null, companyId, userId, 100, null, className,
					json.getBytes(StandardCharsets.UTF_8), "JSON",
					BatchEngineTaskExecuteStatus.INITIAL.name(), null,
					BatchEngineImportTaskConstants.
						IMPORT_STRATEGY_ON_ERROR_FAIL,
					"CREATE",
					HashMapBuilder.<String, Serializable>put(
						"createStrategy", "UPSERT"
					).put(
						"updateStrategy", "UPDATE"
					).build(),
					"DEFAULT");

			taskIds.add(batchEngineImportTask.getBatchEngineImportTaskId());
		}

		// 6. Spawn an async monitor that polls the batch engine and finalizes
		// the Run state when all tasks are done.

		ExecutorService executorService =
			_portalExecutorManager.getPortalExecutor(EXECUTOR_NAME);

		List<Long> finalTaskIds = taskIds;

		executorService.submit(
			() -> _monitorAndFinalize(userId, runId, finalTaskIds));
	}

	private void _finalizeRun(long userId, long runId, boolean failed) {
		try {
			Map<String, Serializable> updates = new HashMap<>();

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
	private BatchEngineImportTaskLocalService
		_batchEngineImportTaskLocalService;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectEntryPersistence _objectEntryPersistence;

	@Reference
	private PortalExecutorManager _portalExecutorManager;

}