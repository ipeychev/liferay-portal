/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLayout from '@clayui/layout';
import React, {useState} from 'react';

import GenerateStep from './GenerateStep';
import ReviewAndPublishStep from './ReviewAndPublishStep';
import MultiStepProgress from './components/MultiStepProgress';
import {SubStep} from './types/SubStep';

interface IProps {
	backURL?: string;
	initialSubStep?: SubStep;
	onBack?: () => void;
}

export default function ReviewStep({
	backURL,
	initialSubStep = 'generate',
	onBack,
}: IProps) {
	const [subStep, setSubStep] = useState<SubStep>(initialSubStep);

	const handleBack = () => {
		if (onBack) {
			onBack();
		}
		else if (backURL) {
			Liferay.Util.navigate(backURL);
		}
	};

	return (
		<div className="content-site-generator">
			<ClayLayout.ContainerFluid view>
				<ClayLayout.Row justify="center">
					<ClayLayout.Col md={10} xl={8}>
						<div className="content-site-generator__progress">
							<MultiStepProgress
								activeStep={2}
								steps={[
									{title: Liferay.Language.get('ideate')},
									{title: Liferay.Language.get('refine')},
									{title: Liferay.Language.get('review')},
								]}
							/>
						</div>

						{subStep === 'generate' ? (
							<GenerateStep
								onBack={handleBack}
								onCancel={handleBack}
								onContinue={() =>
									setSubStep('review-and-publish')
								}
							/>
						) : (
							<ReviewAndPublishStep />
						)}
					</ClayLayout.Col>
				</ClayLayout.Row>
			</ClayLayout.ContainerFluid>
		</div>
	);
}
