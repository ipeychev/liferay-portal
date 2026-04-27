/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLayout from '@clayui/layout';
import React from 'react';

import ReviewAndPublishStep from './ReviewAndPublishStep';
import MultiStepProgress from './components/MultiStepProgress';

interface IProps {
	backURL?: string;
	cancelURL?: string;
	runId?: number;
}

export default function ReviewStep({backURL, cancelURL, runId}: IProps) {
	const handleBack = () => {
		if (backURL) {
			Liferay.Util.navigate(
				runId
					? `${backURL}${
							backURL.includes('?') ? '&' : '?'
						}runId=${runId}`
					: backURL
			);
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

						<ReviewAndPublishStep
							cancelURL={cancelURL}
							onBack={handleBack}
							runId={runId}
						/>
					</ClayLayout.Col>
				</ClayLayout.Row>
			</ClayLayout.ContainerFluid>
		</div>
	);
}
