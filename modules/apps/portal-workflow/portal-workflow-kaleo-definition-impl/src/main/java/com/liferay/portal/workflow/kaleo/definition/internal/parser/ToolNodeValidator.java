/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.definition.internal.parser;

import com.liferay.portal.workflow.kaleo.definition.Definition;
import com.liferay.portal.workflow.kaleo.definition.NodeType;
import com.liferay.portal.workflow.kaleo.definition.ToolNode;
import com.liferay.portal.workflow.kaleo.definition.exception.KaleoDefinitionValidationException;
import com.liferay.portal.workflow.kaleo.definition.parser.NodeValidator;

import org.osgi.service.component.annotations.Component;

/**
 * @author Mahmoud Tayem
 */
@Component(service = NodeValidator.class)
public class ToolNodeValidator extends BaseNodeValidator<ToolNode> {

	@Override
	public NodeType getNodeType() {
		return NodeType.TOOL;
	}

	@Override
	protected void doValidate(Definition definition, ToolNode toolNode)
		throws KaleoDefinitionValidationException {

		if (toolNode.getOutgoingTransitionsCount() > 1) {
			throw new KaleoDefinitionValidationException.
				MustNotSetMultipleOutgoingTransitions(
					toolNode.getDefaultLabel());
		}
	}

}
