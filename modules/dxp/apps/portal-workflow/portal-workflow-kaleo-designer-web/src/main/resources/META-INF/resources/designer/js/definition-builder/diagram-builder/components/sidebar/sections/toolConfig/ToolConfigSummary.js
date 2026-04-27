/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayForm, {ClayInput} from '@clayui/form';
import PropTypes from 'prop-types';
import React, {useContext, useMemo} from 'react';

import {DiagramBuilderContext} from '../../../../DiagramBuilderContext';
import {
	formatVariablesForTextarea,
	parseVariablesInput,
} from '../../../../util/parseVariables';
import SidebarPanel from '../../SidebarPanel';

const ToolConfigSummary = () => {
	const {selectedItem, setSelectedItem} = useContext(DiagramBuilderContext);

	const inputVariablesValue = useMemo(
		() => formatVariablesForTextarea(selectedItem?.data?.inputVariables),
		[selectedItem]
	);

	const outputVariablesValue = useMemo(
		() => formatVariablesForTextarea(selectedItem?.data?.outputVariables),
		[selectedItem]
	);

	const handleVariablesChange =
		(field) =>
		({target}) => {
			if (!selectedItem) {
				return;
			}

			const text = target.value;
			const parsed = parseVariablesInput(text);

			const updatedItem = {
				...selectedItem,
				data: {
					...selectedItem.data,
					[field]: parsed,
				},
			};

			setSelectedItem(updatedItem);
		};

	return (
		<SidebarPanel panelTitle={Liferay.Language.get('tool-configuration')}>
			<ClayForm.Group>
				<label htmlFor="toolName">
					{Liferay.Language.get('tool-name')}
				</label>

				<ClayInput
					id="toolName"
					onChange={({target}) =>
						setSelectedItem({
							...selectedItem,
							data: {
								...selectedItem.data,
								toolName: target.value,
							},
						})
					}
					required={true}
					type="text"
					value={selectedItem?.data?.toolName ?? ''}
				/>

				<label className="mt-4" htmlFor="inputVariables">
					{Liferay.Language.get('input-variables')}
				</label>

				<ClayInput
					className="mt-2"
					component="textarea"
					id="inputVariables"
					onChange={handleVariablesChange('inputVariables')}
					placeholder='[{"name":"myVar", "type":"string"}]'
					type="text"
					value={inputVariablesValue}
				/>

				<label className="mt-4" htmlFor="outputVariables">
					{Liferay.Language.get('output-variables')}
				</label>

				<ClayInput
					className="mt-2"
					component="textarea"
					id="outputVariables"
					onChange={handleVariablesChange('outputVariables')}
					placeholder='[{"name":"result", "type":"string"}]'
					type="text"
					value={outputVariablesValue}
				/>
			</ClayForm.Group>
		</SidebarPanel>
	);
};

ToolConfigSummary.propTypes = {
	setContentName: PropTypes.func.isRequired,
};

export default ToolConfigSummary;
