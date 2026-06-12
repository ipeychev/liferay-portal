/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.node.delegate;

import com.liferay.ai.hub.internal.workflow.node.delegate.util.IRToPageSpecConverter;
import com.liferay.ai.hub.workflow.node.ServiceNodeDelegate;
import com.liferay.portal.kernel.util.GetterUtil;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Mario Gomes
 * @author Iliyan Peychev
 */
@Component(
	property = "java.delegate=com.liferay.ai.hub.internal.workflow.node.delegate.IRToPageSpecServiceNodeDelegate#execute",
	service = ServiceNodeDelegate.class
)
public class IRToPageSpecServiceNodeDelegate implements ServiceNodeDelegate {

	@Override
	public String execute(
			Map<String, String> inputVariables,
			Map<String, Serializable> workflowContext)
		throws Exception {

		IRToPageSpecConverter irToPageSpecConverter = new IRToPageSpecConverter(
			GetterUtil.getString(
				inputVariables.get("fragmentCatalog"),
				GetterUtil.getString(
					inputVariables.get("fullFragmentsCatalog"), "[]")));

		String sitePageExternalReferenceCode = inputVariables.get(
			"sitePageExternalReferenceCode");

		return irToPageSpecConverter.convert(
			inputVariables.get("updatedPageIR"),
			GetterUtil.getString(
				inputVariables.get("draftERC"),
				sitePageExternalReferenceCode + "-draft"),
			GetterUtil.getString(
				inputVariables.get("experienceERC"),
				sitePageExternalReferenceCode + "-default"),
			GetterUtil.getString(inputVariables.get("locale"), "en-US"));
	}

}