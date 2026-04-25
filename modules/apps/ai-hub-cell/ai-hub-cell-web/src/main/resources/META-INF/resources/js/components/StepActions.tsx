/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayIcon from '@clayui/icon';
import React from 'react';

interface IProps {
	backLabel: string;
	cancelLabel?: string;
	continueLabel?: string;
	onBack: () => void;
	onCancel: () => void;
	onContinue: () => void;
}

export default function StepActions({
	backLabel,
	cancelLabel = Liferay.Language.get('cancel'),
	continueLabel = Liferay.Language.get('continue'),
	onBack,
	onCancel,
	onContinue,
}: IProps) {
	return (
		<div className="content-site-generator__step-actions">
			<ClayButton displayType="link" onClick={onBack}>
				<ClayIcon className="mr-1" symbol="angle-left" />

				{backLabel}
			</ClayButton>

			<div className="content-site-generator__step-actions-end">
				<ClayButton displayType="secondary" onClick={onCancel}>
					{cancelLabel}
				</ClayButton>

				<ClayButton displayType="primary" onClick={onContinue}>
					{continueLabel}
				</ClayButton>
			</div>
		</div>
	);
}
