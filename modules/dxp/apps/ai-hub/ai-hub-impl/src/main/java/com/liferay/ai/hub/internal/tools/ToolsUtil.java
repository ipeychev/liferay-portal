/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.tools;

import com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util.ToolsConfigUtil;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowNodeManager;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Feliphe Marinho
 */
public class ToolsUtil {

	public static Object[] getTools(
		long companyId, KaleoNode currentKaleoNode, JSONFactory jsonFactory,
		Map<String, String> kaleoNodeSettingValues,
		Map<String, Serializable> workflowContext,
		WorkflowNodeManager workflowNodeManager) {

		if (_sitePageToolsNodeNames.contains(currentKaleoNode.getName())) {
			return new Object[] {
				new SitePageTools(
					GetterUtil.getString(workflowContext.get("accessToken")),
					companyId,
					GetterUtil.getString(workflowContext.get("userToken")))
			};
		}

		if (_catalogBuilderNodeNames.contains(currentKaleoNode.getName())) {
			return new Object[] {new CatalogBuilderTools(companyId)};
		}

		if (_fragmentLoaderNodeNames.contains(currentKaleoNode.getName())) {
			return new Object[] {
				new SiteFragmentTools(
					GetterUtil.getString(workflowContext.get("accessToken")),
					companyId,
					GetterUtil.getString(workflowContext.get("userToken")),
					workflowContext)
			};
		}

		if (_pageSpecToIRNodeNames.contains(currentKaleoNode.getName())) {
			return new Object[] {new PageSpecToIRTools()};
		}

		if (_irToPageSpecNodeNames.contains(currentKaleoNode.getName())) {
			return new Object[] {
				new IRToPageSpecTools(
					GetterUtil.getString(
						workflowContext.get("fullFragmentsCatalog")))
			};
		}

		List<Object> tools = new ArrayList<>();

		for (String name :
				ToolsConfigUtil.getValues(
					jsonFactory, kaleoNodeSettingValues, "name")) {

			if (Objects.equals(name, "cmsBlog")) {
				tools.add(new CMSBlogTools());
			}
			else if (Objects.equals(name, "workflowNode")) {
				tools.add(new WorkflowNodeTools(workflowNodeManager));
			}
		}

		return tools.toArray();
	}

	private static final Set<String> _catalogBuilderNodeNames = Set.of(
		"catalogBuilder");

	private static final Set<String> _fragmentLoaderNodeNames = Set.of(
		"fragmentLoader");

	private static final Set<String> _irToPageSpecNodeNames = Set.of(
		"irToPageSpec");

	private static final Set<String> _pageSpecToIRNodeNames = Set.of(
		"pageSpecToIR");

	private static final Set<String> _sitePageToolsNodeNames = Set.of(
		"pageFetcher", "pageUpdater");

}