/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.kaleo.runtime.node;

import com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util.VariablesUtil;
import com.liferay.ai.hub.rest.resource.v1_0.util.SseUtil;
import com.liferay.ai.hub.workflow.node.ServiceNodeDelegate;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
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

import java.io.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iliyan Peychev
 */
@Component(service = NodeExecutor.class)
public class ServiceNodeExecutor extends BaseNodeExecutor {

	@Override
	public NodeType getNodeType() {
		return NodeType.SERVICE;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, ServiceNodeDelegate.class, "java.delegate");
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
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

		Map<String, String> kaleoNodeSettingValues = new HashMap<>();

		List<KaleoNodeSetting> kaleoNodeSettings =
			_kaleoNodeSettingLocalService.getKaleoNodeSettings(
				currentKaleoNode.getKaleoNodeId());

		for (KaleoNodeSetting kaleoNodeSetting : kaleoNodeSettings) {
			kaleoNodeSettingValues.put(
				kaleoNodeSetting.getName(), kaleoNodeSetting.getValue());
		}

		String javaDelegate = kaleoNodeSettingValues.get("javaDelegate");

		ServiceNodeDelegate serviceNodeDelegate = _serviceTrackerMap.getService(
			javaDelegate);

		if (serviceNodeDelegate == null) {
			throw new PortalException(
				StringBundler.concat(
					"No service node delegate is registered for \"",
					javaDelegate, "\" on node \"", currentKaleoNode.getName(),
					"\""));
		}

		Map<String, Serializable> workflowContext =
			executionContext.getWorkflowContext();

		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();

		workflowContext.put(
			"workflowInstanceId", kaleoInstanceToken.getKaleoInstanceId());

		try {
			String result = serviceNodeDelegate.execute(
				_getInputVariables(kaleoNodeSettingValues, workflowContext),
				workflowContext);

			JSONArray outputVariablesJSONArray =
				VariablesUtil.getVariablesJSONArray(
					"outputVariables", kaleoNodeSettingValues);

			if ((outputVariablesJSONArray != null) &&
				(outputVariablesJSONArray.length() > 0)) {

				JSONObject outputJSONObject =
					outputVariablesJSONArray.getJSONObject(0);

				workflowContext.put(outputJSONObject.getString("name"), result);
			}

			String sseEventSinkKey = GetterUtil.getString(
				workflowContext.get("sseEventSinkKey"));

			if (Validator.isNotNull(sseEventSinkKey)) {
				SseUtil.send(
					currentKaleoNode.getName(), "Node Completed", null,
					sseEventSinkKey);
			}
		}
		catch (Exception exception) {
			_log.error(
				StringBundler.concat(
					"Unable to execute service node delegate \"", javaDelegate,
					"\" on node \"", currentKaleoNode.getName(), "\""),
				exception);

			throw new PortalException(exception);
		}

		KaleoTransition kaleoTransition =
			currentKaleoNode.getDefaultKaleoTransition();

		remainingPathElements.add(
			new PathElement(
				currentKaleoNode, kaleoTransition.getTargetKaleoNode(),
				new ExecutionContext(
					executionContext.getKaleoInstanceToken(), workflowContext,
					executionContext.getServiceContext())));
	}

	@Override
	protected void doExit(
		KaleoNode currentKaleoNode, ExecutionContext executionContext,
		List<PathElement> remainingPathElements) {
	}

	private Map<String, String> _getInputVariables(
		Map<String, String> kaleoNodeSettingValues,
		Map<String, Serializable> workflowContext) {

		Map<String, String> inputVariables = new HashMap<>();

		JSONArray inputVariablesJSONArray = VariablesUtil.getVariablesJSONArray(
			"inputVariables", kaleoNodeSettingValues);

		if (inputVariablesJSONArray == null) {
			return inputVariables;
		}

		for (int i = 0; i < inputVariablesJSONArray.length(); i++) {
			JSONObject inputJSONObject = inputVariablesJSONArray.getJSONObject(
				i);

			String name = inputJSONObject.getString("name");

			inputVariables.put(
				name, GetterUtil.getString(workflowContext.get(name)));
		}

		return inputVariables;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ServiceNodeExecutor.class);

	@Reference
	private KaleoNodeSettingLocalService _kaleoNodeSettingLocalService;

	private ServiceTrackerMap<String, ServiceNodeDelegate> _serviceTrackerMap;

}