/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayIcon from '@clayui/icon';
import ClayPanel from '@clayui/panel';
import React from 'react';

import {SummaryItem} from '../types/SummaryItem';

export default function SummaryCard({icon, title, value}: SummaryItem) {
	return (
		<ClayPanel
			className="content-site-generator-refine__summary-card"
			displayType="secondary"
		>
			<ClayPanel.Body>
				<div className="content-site-generator-refine__summary-card-header text-secondary">
					<ClayIcon symbol={icon} />

					<span>{title}</span>
				</div>

				<h2 className="content-site-generator-refine__summary-card-value">
					{value}
				</h2>
			</ClayPanel.Body>
		</ClayPanel>
	);
}
