/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayMultiStepNavWithBasicItems} from '@clayui/multi-step-nav';
import React from 'react';

import {MultiStepProgressStep} from '../types/MultiStepProgressStep';

interface IProps {
	activeStep: number;
	steps: MultiStepProgressStep[];
}

export default function MultiStepProgress({activeStep, steps}: IProps) {
	return (
		<ClayMultiStepNavWithBasicItems
			active={activeStep}
			className="multi-step-title-center"
			spritemap={`${Liferay.ThemeDisplay.getPathThemeImages()}/lexicon/icons.svg`}
			steps={steps}
		/>
	);
}
