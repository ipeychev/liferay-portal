/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.site.generator.internal.object.action.executor;

import com.liferay.object.action.executor.BaseObjectActionExecutor;
import com.liferay.object.action.executor.ObjectActionExecutor;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.executor.PortalExecutorManager;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.UnicodeProperties;

import java.io.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Standalone Object Action that simulates the AI Hub agent: it flips the Run
 * to "generating", seeds a few hard-coded ContentGeneratorArtifact rows, and
 * flips the Run to "ready". Replaced by the real agent integration in a
 * follow-up branch.
 *
 * Invoked by the auto-generated endpoint:
 *   PUT /o/content-site-generator/runs/by-external-reference-code/{erc}/object-actions/analyze
 *   PUT /o/content-site-generator/runs/{id}/object-actions/analyze
 *
 * @author Gabriel Albuquerque
 */
@Component(
	property = "object.action.executor.key=" + AnalyzeContentSiteGeneratorRunObjectActionExecutor.KEY,
	service = ObjectActionExecutor.class
)
public class AnalyzeContentSiteGeneratorRunObjectActionExecutor
	extends BaseObjectActionExecutor {

	public static final String EXECUTOR_NAME = "CSG-AnalyzeStub";

	public static final String KEY = "analyze-content-site-generator-run";

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

		long runId = payloadJSONObject.getLong("classPK");

		// 1. Flip the Run to GENERATING immediately so the front-end's poll
		// sees the transition.

		_updateRunStatus(userId, runId, _RUN_STATUS_GENERATING);

		// 2. Spawn the stubbed agent on a background thread.

		ExecutorService executorService =
			_portalExecutorManager.getPortalExecutor(EXECUTOR_NAME);

		executorService.submit(
			() -> _runStubbedAgent(companyId, userId, runId));
	}

	private void _addArtifact(
			long userId, long artifactObjectDefinitionId, long runId,
			String fileName, String className, String json, int loadOrder)
		throws PortalException {

		_objectEntryLocalService.addObjectEntry(
			0L, userId, artifactObjectDefinitionId, 0L, "en_US",
			HashMapBuilder.<String, Serializable>put(
				"className", className
			).put(
				"fileName", fileName
			).put(
				"json", json
			).put(
				"loadOrder", loadOrder
			).put(
				_ARTIFACT_RUN_FK_FIELD, runId
			).build(),
			new ServiceContext());
	}

	private void _runStubbedAgent(long companyId, long userId, long runId) {
		try {
			Thread.sleep(_SIMULATED_AGENT_MS);

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

			long artifactObjectDefinitionId =
				artifactObjectDefinition.getObjectDefinitionId();

			int loadOrder = 0;

			for (Map<String, String> seed : _SEED_ARTIFACTS) {
				String json = seed.get(
					"json"
				).replace(
					"${runId}", String.valueOf(runId)
				);

				_addArtifact(
					userId, artifactObjectDefinitionId, runId,
					seed.get("fileName"), seed.get("className"), json,
					loadOrder++);
			}

			_updateRunStatus(userId, runId, _RUN_STATUS_READY);
		}
		catch (InterruptedException interruptedException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Stubbed agent interrupted for run " + runId,
					interruptedException);
			}

			Thread.currentThread(
			).interrupt();
		}
		catch (Exception exception) {
			_log.error(
				StringBundler.concat(
					"Stubbed agent failed for run ", runId),
				exception);

			try {
				_updateRunStatus(userId, runId, _RUN_STATUS_FAILED);
			}
			catch (Exception updateException) {
				_log.error(
					"Unable to mark run failed: " + runId, updateException);
			}
		}
	}

	private void _updateRunStatus(long userId, long runId, String runStatus)
		throws PortalException {

		Map<String, Serializable> updates = new HashMap<>(
			_objectEntryLocalService.getValues(runId));

		updates.put("runStatus", runStatus);

		_objectEntryLocalService.updateObjectEntry(
			userId, runId, 0L, updates, new ServiceContext());
	}

	private static final String
		_ARTIFACT_OBJECT_DEFINITION_EXTERNAL_REFERENCE_CODE = "L_CSG_ARTIFACT";

	private static final String _ARTIFACT_RUN_FK_FIELD =
		"r_artifacts_l_contentGeneratorRunId";

	private static final String _RUN_STATUS_FAILED = "failed";

	private static final String _RUN_STATUS_GENERATING = "generating";

	private static final String _RUN_STATUS_READY = "ready";

	private static final List<Map<String, String>> _SEED_ARTIFACTS = List.of(
		HashMapBuilder.<String, String>put(
			"className",
			"com.liferay.headless.admin.site.dto.v1_0.Site"
		).put(
			"fileName", "site.json"
		).put(
			"json",
			_envelope(
				"com.liferay.headless.admin.site.dto.v1_0.Site",
				"{\"createStrategy\":\"UPSERT\"}",
				"[{\"externalReferenceCode\":\"csg-site-${runId}\"," +
					"\"name\":\"CSG Site ${runId}\"," +
					"\"friendlyUrlPath\":\"/csg-${runId}\"," +
					"\"membershipType\":\"open\"}]")
		).build(),
		HashMapBuilder.<String, String>put(
			"className",
			"com.liferay.headless.asset.library.dto.v1_0.AssetLibrary"
		).put(
			"fileName", "space.json"
		).put(
			"json",
			_envelope(
				"com.liferay.headless.asset.library.dto.v1_0.AssetLibrary",
				"{\"createStrategy\":\"UPSERT\"}",
				"[{\"externalReferenceCode\":\"csg-space-${runId}\"," +
					"\"name\":\"CSG Space ${runId}\"," +
					"\"type\":\"Space\"}]")
		).build(),
		HashMapBuilder.<String, String>put(
			"className",
			"com.liferay.headless.asset.library.dto.v1_0.ConnectedSite"
		).put(
			"fileName", "connected-site.json"
		).put(
			"json",
			_envelope(
				"com.liferay.headless.asset.library.dto.v1_0.ConnectedSite",
				"{\"createStrategy\":\"UPSERT\"," +
					"\"assetLibraryExternalReferenceCode\":" +
					"\"csg-space-${runId}\"}",
				"[{\"externalReferenceCode\":\"csg-site-${runId}\"," +
					"\"searchable\":true}]")
		).build(),
		HashMapBuilder.<String, String>put(
			"className",
			"com.liferay.headless.admin.site.dto.v1_0.SitePage"
		).put(
			"fileName", "site-page-home.json"
		).put(
			"json",
			_envelope(
				"com.liferay.headless.admin.site.dto.v1_0.SitePage",
				"{\"createStrategy\":\"UPSERT\"," +
					"\"privateLayout\":\"false\"," +
					"\"siteExternalReferenceCode\":" +
					"\"csg-site-${runId}\"}",
				"[{\"externalReferenceCode\":\"csg-page-home-${runId}\"," +
					"\"name_i18n\":{\"en_US\":\"Home\"}," +
					"\"friendlyUrlPath_i18n\":{\"en_US\":\"/home\"}}]")
		).build(),
		HashMapBuilder.<String, String>put(
			"className",
			"com.liferay.headless.admin.site.dto.v1_0.SitePage"
		).put(
			"fileName", "site-page-about.json"
		).put(
			"json",
			_envelope(
				"com.liferay.headless.admin.site.dto.v1_0.SitePage",
				"{\"createStrategy\":\"UPSERT\"," +
					"\"privateLayout\":\"false\"," +
					"\"siteExternalReferenceCode\":" +
					"\"csg-site-${runId}\"}",
				"[{\"externalReferenceCode\":\"csg-page-about-${runId}\"," +
					"\"name_i18n\":{\"en_US\":\"About\"}," +
					"\"friendlyUrlPath_i18n\":{\"en_US\":\"/about\"}}]")
		).build(),
		HashMapBuilder.<String, String>put(
			"className",
			"com.liferay.object.rest.dto.v1_0.ObjectEntry"
		).put(
			"fileName", "cms-blog-welcome.json"
		).put(
			"json",
			_envelope(
				"com.liferay.object.rest.dto.v1_0.ObjectEntry",
				"{\"scopeKey\":\"csg-space-${runId}\"}", "CMSBlog",
				"[{\"externalReferenceCode\":\"csg-cms-blog-welcome-" +
					"${runId}\"," +
					"\"title\":\"Welcome to your generated site\"," +
					"\"subtitle\":\"A first look at what was generated\"," +
					"\"content\":\"<p>This blog entry was created by the " +
						"Content Site Generator.</p>\"}]")
		).build(),
		HashMapBuilder.<String, String>put(
			"className",
			"com.liferay.object.rest.dto.v1_0.ObjectEntry"
		).put(
			"fileName", "cms-basic-web-content-about.json"
		).put(
			"json",
			_envelope(
				"com.liferay.object.rest.dto.v1_0.ObjectEntry",
				"{\"scopeKey\":\"csg-space-${runId}\"}",
				"CMSBasicWebContent",
				"[{\"externalReferenceCode\":\"csg-cms-basic-about-" +
					"${runId}\"," +
					"\"title\":\"About this site\"," +
					"\"content\":\"<p>This piece of basic web content " +
						"was created by the Content Site Generator.</p>\"}]")
		).build());

	private static String _envelope(
		String className, String parametersJSON, String itemsJSONArray) {

		return _envelope(
			className, parametersJSON, "DEFAULT", itemsJSONArray);
	}

	private static String _envelope(
		String className, String parametersJSON, String taskItemDelegateName,
		String itemsJSONArray) {

		String parametersFragment = "";

		if (parametersJSON != null) {
			parametersFragment = ",\"parameters\":" + parametersJSON;
		}

		return StringBundler.concat(
			"{\"configuration\":{\"className\":\"", className,
			"\",\"taskItemDelegateName\":\"", taskItemDelegateName, "\"",
			parametersFragment, "},\"items\":", itemsJSONArray, "}");
	}

	private static final long _SIMULATED_AGENT_MS = 3_000L;

	private static final Log _log = LogFactoryUtil.getLog(
		AnalyzeContentSiteGeneratorRunObjectActionExecutor.class);

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private PortalExecutorManager _portalExecutorManager;

}
