/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.site.generator.internal.workflow.node.delegate;

import com.liferay.ai.hub.rest.resource.v1_0.util.SseUtil;
import com.liferay.ai.hub.workflow.node.ServiceNodeDelegate;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.io.Serializable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iliyan Peychev
 */
@Component(
	property = "java.delegate=com.liferay.content.site.generator.internal.workflow.node.delegate.MarkCSGGenerationReadyServiceNodeDelegate#execute",
	service = ServiceNodeDelegate.class
)
public class MarkCSGGenerationReadyServiceNodeDelegate
	implements ServiceNodeDelegate {

	@Override
	public String execute(
			Map<String, String> inputVariables,
			Map<String, Serializable> workflowContext)
		throws Exception {

		String generationExternalReferenceCode = inputVariables.get(
			"generationExternalReferenceCode");

		if (Validator.isNull(generationExternalReferenceCode)) {
			throw new IllegalArgumentException(
				"The \"generationExternalReferenceCode\" input variable is " +
					"required");
		}

		ServiceContext serviceContext = (ServiceContext)workflowContext.get(
			WorkflowConstants.CONTEXT_SERVICE_CONTEXT);

		_markReady(
			serviceContext.getCompanyId(), generationExternalReferenceCode,
			serviceContext.getUserId());

		String sseEventSinkKey = GetterUtil.getString(
			workflowContext.get("sseEventSinkKey"));

		if (Validator.isNotNull(sseEventSinkKey)) {
			SseUtil.send("ready", "Generation Updated", null, sseEventSinkKey);
		}

		return "The generation was marked ready.";
	}

	private ObjectEntry _getCSGGenerationObjectEntry(
			long companyId, String externalReferenceCode)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_CSG_GENERATION", companyId);

		return _objectEntryLocalService.getObjectEntry(
			externalReferenceCode, 0L,
			objectDefinition.getObjectDefinitionId());
	}

	private String _getTargetLanguages(
			long companyId, long userId, long generationObjectEntryId)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_CSG_GENERATION_ITEM", companyId);

		List<Map<String, Serializable>> valuesList =
			_objectEntryLocalService.getValuesList(
				0, companyId, userId, objectDefinition.getObjectDefinitionId(),
				_filterFactory.create(
					StringBundler.concat(
						"r_items_l_csgGenerationId eq '",
						generationObjectEntryId, "'"),
					objectDefinition),
				null, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
				new Sort[] {new Sort("loadOrder", false)});

		Set<String> languages = new LinkedHashSet<>();

		for (Map<String, Serializable> values : valuesList) {
			for (String language :
					StringUtil.split(
						GetterUtil.getString(values.get("languages")), ',')) {

				String trimmedLanguage = StringUtil.trim(language);

				if (Validator.isNotNull(trimmedLanguage)) {
					languages.add(trimmedLanguage);
				}
			}
		}

		return StringUtil.merge(languages, ",");
	}

	private void _markReady(
			long companyId, String externalReferenceCode, long userId)
		throws Exception {

		ObjectEntry objectEntry = _getCSGGenerationObjectEntry(
			companyId, externalReferenceCode);

		String targetLanguages = "";

		try {
			targetLanguages = _getTargetLanguages(
				companyId, userId, objectEntry.getObjectEntryId());
		}
		catch (Exception exception) {
			_log.error(
				"Unable to aggregate target languages for generation " +
					externalReferenceCode,
				exception);
		}

		_objectEntryLocalService.partialUpdateObjectEntry(
			userId, objectEntry.getObjectEntryId(),
			objectEntry.getObjectEntryFolderId(),
			HashMapBuilder.<String, Serializable>put(
				"generationStatus", "ready"
			).put(
				"targetLanguages", targetLanguages
			).build(),
			new ServiceContext());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		MarkCSGGenerationReadyServiceNodeDelegate.class);

	@Reference(
		target = "(filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private FilterFactory<Predicate> _filterFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

}