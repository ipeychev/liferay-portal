/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.kaleo.runtime.node;

import com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util.ToolsUtil;
import com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util.VariablesUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowNodeManager;
import com.liferay.portal.workflow.kaleo.definition.NodeType;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.model.KaleoNodeSetting;
import com.liferay.portal.workflow.kaleo.model.KaleoTransition;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.runtime.graph.PathElement;
import com.liferay.portal.workflow.kaleo.runtime.node.BaseNodeExecutor;
import com.liferay.portal.workflow.kaleo.runtime.node.NodeExecutor;
import com.liferay.portal.workflow.kaleo.service.KaleoNodeSettingLocalService;

import dev.langchain4j.agent.tool.Tool;

import java.io.Serializable;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Mahmoud Tayem
 */
@Component(service = NodeExecutor.class)
public class ToolNodeExecutor extends BaseNodeExecutor {

	@Override
	public NodeType getNodeType() {
		return NodeType.TOOL;
	}

	@Override
	protected boolean doEnter(
		KaleoNode currentKaleoNode, ExecutionContext executionContext) {

		return true;
	}

	@Override
	protected void doExecute(
			KaleoNode currentKaleoNode, ExecutionContext executionContext,
			List<PathElement> remainingPathElements)
		throws PortalException {

		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();

		Map<String, String> kaleoNodeSettingValues = new HashMap<>();

		List<KaleoNodeSetting> kaleoNodeSettings =
			_kaleoNodeSettingLocalService.getKaleoNodeSettings(
				currentKaleoNode.getKaleoNodeId());

		for (KaleoNodeSetting kaleoNodeSetting : kaleoNodeSettings) {
			kaleoNodeSettingValues.put(
				kaleoNodeSetting.getName(), kaleoNodeSetting.getValue());
		}

		String toolName = kaleoNodeSettingValues.get("toolName");

		Map<String, Serializable> workflowContext =
			executionContext.getWorkflowContext();

		Object[] tools = ToolsUtil.getTools(
			kaleoInstanceToken.getCompanyId(), currentKaleoNode,
			workflowContext, _workflowNodeManager);

		try {
			String result = _invokeTool(
				tools, toolName, kaleoNodeSettingValues, workflowContext);

			JSONArray outputVariables = VariablesUtil.getVariablesJSONArray(
				"outputVariables", kaleoNodeSettingValues);

			if ((outputVariables != null) && (outputVariables.length() > 0)) {
				JSONObject outputVar = outputVariables.getJSONObject(0);

				workflowContext.put(outputVar.getString("name"), result);
			}

			if (_log.isInfoEnabled()) {
				_log.info(
					"ToolNodeExecutor executed tool '" + toolName +
						"' on node '" + currentKaleoNode.getName() + "'");
			}
		}
		catch (Exception exception) {
			_log.error(
				"ToolNodeExecutor failed to execute tool '" + toolName +
					"' on node '" + currentKaleoNode.getName() + "'",
				exception);

			throw new PortalException(exception);
		}

		String transitionName = executionContext.getTransitionName();

		KaleoTransition kaleoTransition = null;

		if (Validator.isNull(transitionName)) {
			kaleoTransition = currentKaleoNode.getDefaultKaleoTransition();
		}
		else {
			kaleoTransition = currentKaleoNode.getKaleoTransition(
				transitionName);
		}

		ExecutionContext newExecutionContext = new ExecutionContext(
			executionContext.getKaleoInstanceToken(),
			executionContext.getWorkflowContext(),
			executionContext.getServiceContext());

		remainingPathElements.add(
			new PathElement(
				currentKaleoNode, kaleoTransition.getTargetKaleoNode(),
				newExecutionContext));
	}

	@Override
	protected void doExit(
		KaleoNode currentKaleoNode, ExecutionContext executionContext,
		List<PathElement> remainingPathElements) {
	}

	private String _invokeTool(
			Object[] tools, String toolName,
			Map<String, String> kaleoNodeSettingValues,
			Map<String, Serializable> workflowContext)
		throws Exception {

		JSONArray inputVariables = VariablesUtil.getVariablesJSONArray(
			"inputVariables", kaleoNodeSettingValues);

		List<String> args = new ArrayList<>();

		if (inputVariables != null) {
			for (int i = 0; i < inputVariables.length(); i++) {
				JSONObject inputVar = inputVariables.getJSONObject(i);

				String name = inputVar.getString("name");

				args.add(
					GetterUtil.getString(workflowContext.get(name)));
			}
		}

		for (Object tool : tools) {
			for (Method method : tool.getClass().getMethods()) {
				Tool toolAnnotation = method.getAnnotation(Tool.class);

				if (toolAnnotation == null) {
					continue;
				}

				if (!method.getName().equals(toolName)) {
					continue;
				}

				Object[] methodArgs = args.toArray(new String[0]);

				Object result = method.invoke(tool, methodArgs);

				if (result == null) {
					return "";
				}

				return result.toString();
			}
		}

		throw new IllegalArgumentException(
			"Tool method '" + toolName + "' not found");
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ToolNodeExecutor.class);

	@Reference
	private KaleoNodeSettingLocalService _kaleoNodeSettingLocalService;

	@Reference
	private WorkflowNodeManager _workflowNodeManager;

}
