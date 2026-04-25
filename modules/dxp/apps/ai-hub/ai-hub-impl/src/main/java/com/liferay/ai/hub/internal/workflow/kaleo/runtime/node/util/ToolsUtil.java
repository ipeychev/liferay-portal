/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util;

import com.liferay.ai.hub.internal.assistant.tool.IRToPageSpecTools;
import com.liferay.ai.hub.internal.assistant.tool.PageSpecToIRTools;
import com.liferay.ai.hub.internal.assistant.tool.SitePageTools;
import com.liferay.ai.hub.internal.assistant.tool.WorkflowNodeTools;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowNodeManager;
import com.liferay.portal.workflow.kaleo.definition.NodeType;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;

import java.io.Serializable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Feliphe Marinho
 */
public class ToolsUtil {

	public static Object[] getTools(
		long companyId, KaleoNode currentKaleoNode,
		Map<String, Serializable> workflowContext,
		WorkflowNodeManager workflowNodeManager) {

		if (Objects.equals(
				currentKaleoNode.getType(), NodeType.AI_DECISION.name())) {

			return new Object[] {new WorkflowNodeTools(workflowNodeManager)};
		}

		if (_sitePageToolsNodeNames.contains(currentKaleoNode.getName())) {
			return new Object[] {
				new SitePageTools(
					GetterUtil.getString(workflowContext.get("accessToken")),
					companyId,
					GetterUtil.getString(workflowContext.get("userToken")))
			};
		}

		if (_pageSpecToIRNodeNames.contains(currentKaleoNode.getName())) {
			return new Object[] {new PageSpecToIRTools()};
		}

		if (_irToPageSpecNodeNames.contains(currentKaleoNode.getName())) {
			return new Object[] {new IRToPageSpecTools()};
		}

		return new Object[0];
	}

	private static final Set<String> _irToPageSpecNodeNames = Set.of(
		"irToPageSpec");

	private static final Set<String> _pageSpecToIRNodeNames = Set.of(
		"pageSpecToIR");

	private static final Set<String> _sitePageToolsNodeNames = Set.of(
		"pageFetcher", "pageUpdater");

}