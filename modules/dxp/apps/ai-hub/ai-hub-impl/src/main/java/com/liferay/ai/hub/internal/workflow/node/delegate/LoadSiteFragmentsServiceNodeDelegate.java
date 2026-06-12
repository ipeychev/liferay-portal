/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.node.delegate;

import com.liferay.ai.hub.workflow.node.ServiceNodeDelegate;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.service.FragmentCollectionLocalService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Aggregates the site's fragment entries into the catalog JSON the page
 * builder nodes consume.
 *
 * @author Mario Gomes
 * @author Iliyan Peychev
 */
@Component(
	property = "java.delegate=com.liferay.ai.hub.internal.workflow.node.delegate.LoadSiteFragmentsServiceNodeDelegate#execute",
	service = ServiceNodeDelegate.class
)
public class LoadSiteFragmentsServiceNodeDelegate
	implements ServiceNodeDelegate {

	@Override
	public String execute(
			Map<String, String> inputVariables,
			Map<String, Serializable> workflowContext)
		throws Exception {

		String siteExternalReferenceCode = inputVariables.get(
			"siteExternalReferenceCode");

		if (Validator.isNull(siteExternalReferenceCode)) {
			throw new IllegalArgumentException(
				"The \"siteExternalReferenceCode\" input variable is required");
		}

		Group group = _groupLocalService.getGroupByExternalReferenceCode(
			siteExternalReferenceCode, CompanyThreadLocal.getCompanyId());

		JSONArray catalogJSONArray = _jsonFactory.createJSONArray();

		for (FragmentCollection fragmentCollection :
				_fragmentCollectionLocalService.getFragmentCollections(
					group.getGroupId())) {

			for (FragmentEntry fragmentEntry :
					_fragmentEntryLocalService.getFragmentEntries(
						fragmentCollection.getFragmentCollectionId())) {

				catalogJSONArray.put(
					JSONUtil.put(
						"configuration", fragmentEntry.getConfiguration()
					).put(
						"css", fragmentEntry.getCss()
					).put(
						"externalReferenceCode",
						fragmentEntry.getExternalReferenceCode()
					).put(
						"html", fragmentEntry.getHtml()
					).put(
						"js", fragmentEntry.getJs()
					).put(
						"key", fragmentEntry.getFragmentEntryKey()
					).put(
						"name", fragmentEntry.getName()
					));
			}
		}

		return catalogJSONArray.toString();
	}

	@Reference
	private FragmentCollectionLocalService _fragmentCollectionLocalService;

	@Reference
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private JSONFactory _jsonFactory;

}