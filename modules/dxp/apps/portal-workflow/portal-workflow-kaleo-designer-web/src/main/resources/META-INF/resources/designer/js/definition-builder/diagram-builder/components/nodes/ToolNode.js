/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import PropTypes from 'prop-types';
import React from 'react';

import {defaultLanguageId} from '../../../constants';
import BaseNode from './BaseNode';

export default function ToolNode({
	data: {
		description,
		inputVariables,
		label,
		newNode,
		outputVariables,
		toolName,
	} = {},
	descriptionSidebar,
	id,
	...otherProps
}) {
	if (!label || !label[defaultLanguageId]) {
		label = {
			[defaultLanguageId]: Liferay.Language.get('tool-node'),
		};
	}

	return (
		<BaseNode
			description={description}
			descriptionSidebar={descriptionSidebar}
			icon="code"
			id={id}
			inputVariables={inputVariables}
			label={label}
			newNode={newNode}
			nodeTypeClassName="tool-node"
			outputVariables={outputVariables}
			toolName={toolName}
			type="tool"
			{...otherProps}
		/>
	);
}

ToolNode.propTypes = {
	data: PropTypes.object,
	descriptionSidebar: PropTypes.string,
	id: PropTypes.string,
};
