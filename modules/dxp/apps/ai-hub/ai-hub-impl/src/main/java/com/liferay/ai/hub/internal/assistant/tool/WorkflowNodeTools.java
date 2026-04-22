/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.assistant.tool;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.workflow.WorkflowNodeManager;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.invocation.InvocationParameters;

import java.io.Serializable;

import java.util.Map;

/**
 * @author João Victor Alves
 */
public class WorkflowNodeTools {

	public WorkflowNodeTools(WorkflowNodeManager workflowNodeManager) {
		_workflowNodeManager = workflowNodeManager;
	}

	@Tool("Complete the workflow node by proceeding to the chosen transition")
	public void completeWorkflowNode(
			InvocationParameters invocationParameters,
			@P("A brief, one-sentence justification for the chosen transition.")
				String reason,
			@P("Transition name") String transitionName)
		throws PortalException {

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		try (SafeCloseable safeCloseable =
				CompanyThreadLocal.setCompanyIdWithSafeCloseable(
					invocationParameters.get("companyId"))) {

			PermissionThreadLocal.setPermissionChecker(
				invocationParameters.get("permissionChecker"));

			ExecutionContext executionContext = invocationParameters.get(
				"executionContext");

			KaleoInstanceToken kaleoInstanceToken =
				executionContext.getKaleoInstanceToken();

			Map<String, Serializable> workflowContext =
				executionContext.getWorkflowContext();

			workflowContext.put("reason", reason);

			_workflowNodeManager.completeWorkflowNode(
				kaleoInstanceToken.getCompanyId(),
				kaleoInstanceToken.getUserId(),
				kaleoInstanceToken.getKaleoInstanceTokenId(), transitionName,
				workflowContext, false);
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
		}
	}

	private final WorkflowNodeManager _workflowNodeManager;

}