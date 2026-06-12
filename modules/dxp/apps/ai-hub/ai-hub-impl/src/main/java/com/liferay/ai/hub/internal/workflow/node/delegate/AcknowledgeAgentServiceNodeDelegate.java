/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.node.delegate;

import com.liferay.ai.hub.internal.agent.util.AgentUtil;
import com.liferay.ai.hub.workflow.node.ServiceNodeDelegate;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * Completes the agent invocation early with an acknowledgment so the chat
 * answers immediately while the workflow keeps running. Long-running agents
 * place this node after their first step instead of holding the supervisor
 * thread until the workflow ends.
 *
 * @author Iliyan Peychev
 */
@Component(
	property = "java.delegate=com.liferay.ai.hub.internal.workflow.node.delegate.AcknowledgeAgentServiceNodeDelegate#execute",
	service = ServiceNodeDelegate.class
)
public class AcknowledgeAgentServiceNodeDelegate
	implements ServiceNodeDelegate {

	@Override
	public String execute(
		Map<String, String> inputVariables,
		Map<String, Serializable> workflowContext) {

		String acknowledgment = inputVariables.get("acknowledgment");

		if (Validator.isNull(acknowledgment)) {
			acknowledgment =
				"I am generating the content now. The preview will refresh " +
					"as soon as it is ready.";
		}

		Message message = new Message();

		message.put(
			"workflowContext",
			HashMapBuilder.<String, Serializable>put(
				"output", acknowledgment
			).build());
		message.put(
			"workflowInstanceId",
			GetterUtil.getLong(workflowContext.get("workflowInstanceId")));

		AgentUtil.complete(message);

		return acknowledgment;
	}

}