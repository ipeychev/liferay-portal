/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLoadingIndicator from '@clayui/loading-indicator';
import {
	AIAssistantChat,
	ChatContext,
} from '@liferay/ai-hub-cell-js-components-web';
import {openToast} from 'frontend-js-components-web';
import React, {useCallback, useEffect, useState} from 'react';

import StepLayout from './components/StepLayout';
import {
	commitGeneration,
	createGeneration,
	getGeneration,
	getGenerationItems,
} from './services/generations';
import IdeateStep from './steps/IdeateStep';
import RefineStep from './steps/RefineStep';
import ReviewStep from './steps/ReviewStep';

import type {Generation} from './types/Generation';
import type {GenerationItem} from './types/GenerationItem';

const POLL_INTERVAL = 3000;

const QUICK_ACTIONS = [
	Liferay.Language.get('assist-me-on-creating-pages'),
	Liferay.Language.get('generate-content'),
	Liferay.Language.get('translate-content'),
];

const STEP_IDEATE = 0;
const STEP_REFINE = 1;
const STEP_REVIEW = 2;

interface IProps {
	apiURL: string;
	generationId?: number;
	generationsURL: string;
}

export default function ContentSiteGeneratorWizard({
	apiURL,
	generationId,
	generationsURL,
}: IProps) {
	const [activeStep, setActiveStep] = useState(STEP_IDEATE);
	const [autoSendMessage, setAutoSendMessage] = useState<string>();
	const [error, setError] = useState<string>();
	const [generation, setGeneration] = useState<Generation>();
	const [items, setItems] = useState<GenerationItem[]>([]);
	const [loading, setLoading] = useState(!!generationId);
	const [publishing, setPublishing] = useState(false);

	const getChatContext = useCallback((): ChatContext => {
		if (!generation) {
			return {};
		}

		return {
			generationExternalReferenceCode: generation.externalReferenceCode,
			generationId: generation.id,
		};
	}, [generation]);

	const handleCancel = useCallback(() => {
		Liferay.Util.navigate(generationsURL);
	}, [generationsURL]);

	const refresh = useCallback(
		async (currentGenerationId: number) => {
			const [newGeneration, newItems] = await Promise.all([
				getGeneration(apiURL, currentGenerationId),
				getGenerationItems(apiURL, currentGenerationId),
			]);

			setGeneration(newGeneration);
			setItems(newItems);

			return newGeneration;
		},
		[apiURL]
	);

	useEffect(() => {
		if (!generationId) {
			return;
		}

		refresh(generationId)
			.then((newGeneration) => {
				const statusKey = newGeneration.generationStatus.key;

				setActiveStep(
					statusKey === 'committed' || statusKey === 'ready'
						? STEP_REVIEW
						: STEP_REFINE
				);
			})
			.catch((newError: Error) => setError(newError.message))
			.finally(() => setLoading(false));
	}, [generationId, refresh]);

	useEffect(() => {
		if (
			!generation ||
			(generation.generationStatus.key !== 'generating' &&
				generation.generationStatus.key !== 'refining')
		) {
			return;
		}

		const intervalId = setInterval(
			() =>
				refresh(generation.id).catch(() => {
					clearInterval(intervalId);
				}),
			POLL_INTERVAL
		);

		return () => clearInterval(intervalId);
	}, [generation, refresh]);

	const handleAnalyze = async (prompt: string) => {
		setError(undefined);
		setLoading(true);

		try {
			const newGeneration = await createGeneration(apiURL, {
				prompt,
				title: prompt.split('\n')[0].slice(0, 75),
			});

			setGeneration(newGeneration);
			setItems([]);
			setAutoSendMessage(prompt);
			setActiveStep(STEP_REFINE);
		}
		catch (newError) {
			setError((newError as Error).message);
		}
		finally {
			setLoading(false);
		}
	};

	const handlePublish = async () => {
		if (!generation) {
			return;
		}

		setError(undefined);
		setPublishing(true);

		try {
			await commitGeneration(apiURL, generation.id);

			openToast({
				message: Liferay.Language.get(
					'the-generated-content-was-published'
				),
				type: 'success',
			});

			Liferay.Util.navigate(generationsURL);
		}
		catch (newError) {
			setError((newError as Error).message);
		}
		finally {
			setPublishing(false);
		}
	};

	if (loading && !generation) {
		return <ClayLoadingIndicator displayType="secondary" size="md" />;
	}

	return (
		<div className="content-site-generator">
			{activeStep === STEP_IDEATE && (
				<StepLayout activeStep={STEP_IDEATE}>
					<IdeateStep
						error={error}
						loading={loading}
						onAnalyze={handleAnalyze}
					/>
				</StepLayout>
			)}

			{activeStep === STEP_REFINE && generation && (
				<StepLayout
					activeStep={STEP_REFINE}
					sidebar={
						<AIAssistantChat
							embedded
							getContext={getChatContext}
							initialMessage={autoSendMessage}
							instructionDefinitionScope=""
							quickActions={QUICK_ACTIONS}
						/>
					}
				>
					<RefineStep
						generation={generation}
						items={items}
						onBack={() => setActiveStep(STEP_IDEATE)}
						onCancel={handleCancel}
						onContinue={() => setActiveStep(STEP_REVIEW)}
					/>
				</StepLayout>
			)}

			{activeStep === STEP_REVIEW && generation && (
				<StepLayout activeStep={STEP_REVIEW}>
					<ReviewStep
						error={error}
						generation={generation}
						items={items}
						onBack={() => setActiveStep(STEP_REFINE)}
						onCancel={handleCancel}
						onPublish={handlePublish}
						publishing={publishing}
					/>
				</StepLayout>
			)}
		</div>
	);
}
