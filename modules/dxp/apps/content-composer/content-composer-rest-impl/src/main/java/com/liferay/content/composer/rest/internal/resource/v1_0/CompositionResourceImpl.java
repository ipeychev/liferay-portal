/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.composer.rest.internal.resource.v1_0;

import com.liferay.content.composer.rest.dto.v1_0.ExecutionStatus;
import com.liferay.content.composer.rest.resource.v1_0.CompositionResource;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.object.rest.manager.v1_0.DefaultObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Feliphe Marinho
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/composition.properties",
	scope = ServiceScope.PROTOTYPE, service = CompositionResource.class
)
public class CompositionResourceImpl extends BaseCompositionResourceImpl {

	@Override
	public ExecutionStatus postCompositionByExternalReferenceCodeExecute(
			String externalReferenceCode, String siteExternalReferenceCode)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.getObjectDefinition(
				contextCompany.getCompanyId(), "ContentComposerComposition");

		DTOConverterContext dtoConverterContext = _createDTOConverterContext();

		ObjectEntry compositionObjectEntry = _objectEntryManager.getObjectEntry(
			contextCompany.getCompanyId(), dtoConverterContext,
			externalReferenceCode, objectDefinition, null);

		DefaultObjectEntryManager defaultObjectEntryManager =
			(DefaultObjectEntryManager)_objectEntryManager;

		Page<ObjectEntry> objectEntriesPage =
			defaultObjectEntryManager.getRelatedObjectEntries(
				dtoConverterContext, compositionObjectEntry.getId(),
				_objectRelationshipLocalService.getObjectRelationship(
					objectDefinition.getObjectDefinitionId(),
					"compositionToArtifacts"),
				Pagination.of(QueryUtil.ALL_POS, QueryUtil.ALL_POS));

		JSONArray jsonArray = _jsonFactory.createJSONArray();

		for (ObjectEntry artifactObjectEntry : objectEntriesPage.getItems()) {
			String json = GetterUtil.getString(
				artifactObjectEntry.getPropertyValue("json"));

			if (Validator.isBlank(json)) {
				continue;
			}

			jsonArray.put(_jsonFactory.createJSONObject(json));
		}

		String location = StringBundler.concat(
			contextHttpServletRequest.getScheme(), "://",
			contextHttpServletRequest.getServerName(), ":",
			contextHttpServletRequest.getServerPort(),
			"/o/headless-batch-engine/v1.0/import-task",
			"/com.liferay.object.rest.dto.v1_0.ObjectEntry");

		location = HttpComponentsUtil.addParameter(
			location, "createStrategy", "UPSERT");
		location = HttpComponentsUtil.addParameter(
			location, "importStrategy", "ON_ERROR_FAIL");
		location = HttpComponentsUtil.addParameter(
			location, "siteExternalReferenceCode", siteExternalReferenceCode);
		location = HttpComponentsUtil.addParameter(
			location, "taskItemDelegateName", "CMSBlog");

		Http.Options options = new Http.Options();

		options.addHeader(
			HttpHeaders.AUTHORIZATION,
			contextHttpServletRequest.getHeader(HttpHeaders.AUTHORIZATION));
		options.addHeader(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
		options.setBody(
			jsonArray.toString(), ContentTypes.APPLICATION_JSON, "UTF-8");
		options.setLocation(location);
		options.setMethod(Http.Method.POST);

		_http.URLtoString(options);

		return new ExecutionStatus() {
			{
				setStatus(() -> "ACCEPTED");
			}
		};
	}

	private DTOConverterContext _createDTOConverterContext() {
		return new DefaultDTOConverterContext(
			contextAcceptLanguage.isAcceptAllLanguages(), null,
			_dtoConverterRegistry, contextHttpServletRequest, null,
			contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
			contextUser);
	}

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private Http _http;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference(target = "(object.entry.manager.storage.type=default)")
	private ObjectEntryManager _objectEntryManager;

	@Reference
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

}