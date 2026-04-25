/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLabel from '@clayui/label';
import ClayPanel from '@clayui/panel';
import React from 'react';

import {ContentSample} from '../types/ContentSample';

interface IProps {
	defaultExpanded?: boolean;
	sample: ContentSample;
}

export default function ContentSampleItem({
	defaultExpanded,
	sample,
}: IProps) {
	return (
		<ClayPanel
			className="content-site-generator-refine__sample"
			collapsable
			defaultExpanded={defaultExpanded}
			displayTitle={sample.title}
			displayType="unstyled"
			showCollapseIcon
		>
			<ClayPanel.Body>
				{sample.fields.map((field, index) => (
					<div className="form-group" key={index}>
						<label className="control-label">{field.label}</label>

						<div className="form-control">{field.value}</div>
					</div>
				))}

				{sample.tags.length ? (
					<>
						<div className="content-site-generator-refine__sample-fields-header">
							{Liferay.Language.get('content-fields')}
						</div>

						<div className="content-site-generator-refine__sample-tags">
							{sample.tags.map((tag, index) => (
								<ClayLabel
									displayType="unstyled"
									key={index}
								>
									{tag}
								</ClayLabel>
							))}
						</div>
					</>
				) : null}
			</ClayPanel.Body>
		</ClayPanel>
	);
}
