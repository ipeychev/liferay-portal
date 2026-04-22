/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util;

import com.liferay.ai.hub.internal.assistant.tool.SitePageTools;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;

import java.io.Serializable;

import java.util.Map;
import java.util.Objects;

/**
 * @author Feliphe Marinho
 */
public class ToolsUtil {

	public static Object[] getTools(
		long companyId, KaleoNode currentKaleoNode,
		Map<String, Serializable> workflowContext) {

		if (Objects.equals(currentKaleoNode.getName(), "pageBuilder")) {
			return new Object[] {
				new SitePageTools(
					GetterUtil.getString(workflowContext.get("accessToken")),
					companyId,
					GetterUtil.getString(workflowContext.get("userToken")))
			};
		}

		return new Object[0];
	}

}