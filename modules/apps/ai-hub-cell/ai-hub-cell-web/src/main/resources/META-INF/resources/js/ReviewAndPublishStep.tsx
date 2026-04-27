/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import ClayButton from '@clayui/button';
import ClayEmptyState from '@clayui/empty-state';
import ClayIcon from '@clayui/icon';
import ClayLabel from '@clayui/label';
import ClayLayout from '@clayui/layout';
import {fetch as liferayFetch} from 'frontend-js-web';
import React, {useEffect, useRef, useState} from 'react';

import StepActions from './components/StepActions';
import SummaryCard from './components/SummaryCard';

const SPRITEMAP = `${Liferay.ThemeDisplay.getPathThemeImages()}/lexicon/icons.svg`;

const RUNS_URL = '/o/content-site-generator/runs';

const SITES_URL = '/o/headless-admin-site/v1.0/sites';

const POLL_INTERVAL_MS = 2_000;

const POLL_TIMEOUT_MS = 10 * 60 * 1_000;

const PAGE_CLASS_NAMES = [
	'com.liferay.headless.admin.site.dto.v1_0.SitePage',
	'com.liferay.headless.delivery.dto.v1_0.SitePage',
];

const LANGUAGE_FROM_FILENAME = /-([a-z]{2})(?:[-_][A-Z]{2})?\.json$/i;

const LANGUAGE_FROM_I18N = /"[a-zA-Z]+_i18n"\s*:\s*\{\s*"([a-z]{2})/;

const PHASE_KEYS = [
	'analyzing-reference-documents',
	'extracting-key-topics-and-features',
	'generating-contents',
	'generating-content-pages',
	'localizing-to-target-languages',
];

interface Artifact {
	className?: string;
	fileName?: string;
	id: number;
	json?: string;
	loadOrder?: number;
}

interface Run {
	committedAt?: string;
	failureReason?: string;
	resultingSiteERC?: string;
	runStatus?: {key?: string};
}

interface IProps {
	cancelURL?: string;
	onBack?: () => void;
	runId?: number;
}

const sleep = (ms: number) =>
	new Promise<void>((resolve) => setTimeout(resolve, ms));

const getArtifactLanguage = (artifact: Artifact): string | null => {
	const fromFilename = artifact.fileName?.match(LANGUAGE_FROM_FILENAME);

	if (fromFilename) {
		return fromFilename[1].toLowerCase();
	}

	const fromJson = artifact.json?.match(LANGUAGE_FROM_I18N);

	return fromJson ? fromJson[1].toLowerCase() : null;
};

const getItemCount = (artifact: Artifact): number => {
	if (!artifact.json) {
		return 1;
	}

	try {
		const parsed = JSON.parse(artifact.json);

		if (Array.isArray(parsed?.items)) {
			return parsed.items.length;
		}
	}
	catch (exception) {
		// Fall through to default.
	}

	return 1;
};

export default function ReviewAndPublishStep({
	cancelURL,
	onBack,
	runId,
}: IProps) {
	const [artifacts, setArtifacts] = useState<Artifact[]>([]);
	const [error, setError] = useState<string | null>(null);
	const [failed, setFailed] = useState(false);
	const [loading, setLoading] = useState(true);
	const [mode, setMode] = useState<'generating' | 'review'>('generating');
	const [phaseIndex, setPhaseIndex] = useState(0);
	const [run, setRun] = useState<Run | null>(null);

	const pollingRef = useRef(false);

	useEffect(() => {
		if (!runId) {
			setError(Liferay.Language.get('missing-run-id'));
			setLoading(false);

			return;
		}

		let cancelled = false;

		(async () => {
			setLoading(true);
			setError(null);

			try {
				const [runResponse, artifactsResponse] = await Promise.all([
					liferayFetch(`${RUNS_URL}/${runId}`),
					liferayFetch(
						`${RUNS_URL}/${runId}/artifacts?pageSize=100` +
							`&sort=loadOrder:asc`
					),
				]);

				if (!runResponse.ok || !artifactsResponse.ok) {
					throw new Error(
						`Failed to load run (${runResponse.status}/${artifactsResponse.status})`
					);
				}

				const runJson: Run = await runResponse.json();
				const artifactsJson = await artifactsResponse.json();

				if (cancelled) {
					return;
				}

				setRun(runJson);
				setArtifacts(artifactsJson.items ?? []);

				const status = runJson.runStatus?.key;

				if (status === 'committed') {
					setPhaseIndex(PHASE_KEYS.length);
					setMode('review');
				}
				else if (status === 'failed') {
					setFailed(true);
					setError(
						runJson.failureReason ||
							Liferay.Language.get(
								'failed-to-generate-please-try-again'
							)
					);
				}
			}
			catch (exception) {
				if (!cancelled) {
					setError(
						exception instanceof Error
							? exception.message
							: String(exception)
					);
				}
			}
			finally {
				if (!cancelled) {
					setLoading(false);
				}
			}
		})();

		return () => {
			cancelled = true;
		};
	}, [runId]);

	useEffect(() => {
		if (!runId || loading || mode !== 'generating' || failed) {
			return;
		}

		if (pollingRef.current) {
			return;
		}

		pollingRef.current = true;

		let cancelled = false;

		(async () => {
			const deadline = Date.now() + POLL_TIMEOUT_MS;

			while (!cancelled && Date.now() < deadline) {
				await sleep(POLL_INTERVAL_MS);

				if (cancelled) {
					return;
				}

				setPhaseIndex((current) =>
					Math.min(current + 1, PHASE_KEYS.length - 1)
				);

				try {
					const pollResponse = await liferayFetch(
						`${RUNS_URL}/${runId}`
					);

					if (!pollResponse.ok) {
						throw new Error(
							`Failed to poll run (${pollResponse.status})`
						);
					}

					const pollRun: Run = await pollResponse.json();
					const pollStatus = pollRun.runStatus?.key;

					if (cancelled) {
						return;
					}

					setRun(pollRun);

					if (pollStatus === 'committed') {
						setPhaseIndex(PHASE_KEYS.length);

						return;
					}

					if (pollStatus === 'failed') {
						setFailed(true);
						setError(
							pollRun.failureReason ||
								Liferay.Language.get(
									'failed-to-generate-please-try-again'
								)
						);

						return;
					}
				}
				catch (exception) {
					if (cancelled) {
						return;
					}

					setError(
						exception instanceof Error
							? exception.message
							: String(exception)
					);

					return;
				}
			}

			if (!cancelled) {
				setError(
					Liferay.Language.get(
						'generation-timed-out-please-try-again'
					)
				);
			}
		})();

		return () => {
			cancelled = true;
			pollingRef.current = false;
		};
	}, [failed, loading, mode, runId]);

	const status = run?.runStatus?.key ?? 'draft';

	const generationComplete = phaseIndex >= PHASE_KEYS.length;

	const languages = new Set<string>();

	for (const artifact of artifacts) {
		const language = getArtifactLanguage(artifact);

		if (language) {
			languages.add(language);
		}
	}

	const totalItems = artifacts.reduce(
		(sum, artifact) => sum + getItemCount(artifact),
		0
	);

	const pageCount = artifacts.filter((artifact) =>
		PAGE_CLASS_NAMES.includes(artifact.className ?? '')
	).length;

	const handleBack = () => {
		if (onBack) {
			onBack();
		}
	};

	const handleCancel = () => {
		if (cancelURL) {
			Liferay.Util.navigate(cancelURL);
		}
	};

	const handleContinue = async () => {
		if (mode === 'generating') {
			if (!generationComplete || failed) {
				return;
			}

			setMode('review');

			return;
		}

		const externalReferenceCode = run?.resultingSiteERC;

		if (!externalReferenceCode) {
			if (cancelURL) {
				Liferay.Util.navigate(cancelURL);
			}

			return;
		}

		try {
			const siteResponse = await liferayFetch(
				`${SITES_URL}/${encodeURIComponent(externalReferenceCode)}`
			);

			if (siteResponse.ok) {
				const site = await siteResponse.json();

				if (site?.friendlyUrlPath) {
					Liferay.Util.navigate(`/web${site.friendlyUrlPath}`);

					return;
				}
			}
		}
		catch (exception) {
			// Fall through to cancelURL.
		}

		if (cancelURL) {
			Liferay.Util.navigate(cancelURL);
		}
	};

	if (loading) {
		return (
			<div className="content-site-generator-review">
				<ClayEmptyState
					description={Liferay.Language.get(
						'loading-the-generated-content'
					)}
					small
					title=""
				/>
			</div>
		);
	}

	const summary = [
		{
			icon: 'document',
			title: Liferay.Language.get('content-entries'),
			value: totalItems,
		},
		{
			icon: 'page',
			title: Liferay.Language.get('content-pages'),
			value: pageCount,
		},
		{
			icon: 'automatic-translate',
			title: Liferay.Language.get('languages'),
			value: languages.size,
		},
	];

	return (
		<div className="content-site-generator-review">
			<div className="content-site-generator-review__header">
				<h3>
					{mode === 'review'
						? Liferay.Language.get('review-and-publish')
						: Liferay.Language.get('generate')}
				</h3>

				{mode === 'review' && (
					<p className="text-secondary">
						{Liferay.Language.get(
							'your-site-is-ready-open-it-when-you-are-done-here'
						)}
					</p>
				)}
			</div>

			{error && (
				<ClayAlert
					className="mb-3"
					displayType="danger"
					onClose={() => setError(null)}
				>
					{error}
				</ClayAlert>
			)}

			<ClayLayout.Row className="content-site-generator-review__summary">
				{summary.map((item, index) => (
					<ClayLayout.Col key={index} md={4}>
						<SummaryCard
							icon={item.icon}
							title={item.title}
							value={item.value}
						/>
					</ClayLayout.Col>
				))}
			</ClayLayout.Row>

			{mode === 'generating' && (
				<ul className="content-site-generator-review__phases">
					{PHASE_KEYS.map((key, index) => {
						const isComplete = index < phaseIndex;
						const isActive = index === phaseIndex && !failed;
						const isFailed = index === phaseIndex && failed;
						const percentage = isComplete
							? 100
							: isActive
								? 10
								: 0;

						const stateClassName = isComplete
							? 'content-site-generator-review__phase--complete'
							: isFailed
								? 'content-site-generator-review__phase--failed'
								: isActive
									? 'content-site-generator-review__phase--active'
									: 'content-site-generator-review__phase--pending';

						return (
							<li
								className={`content-site-generator-review__phase ${stateClassName}`}
								key={key}
							>
								<div className="content-site-generator-review__phase-header">
									<span className="content-site-generator-review__phase-icon">
										{isComplete && (
											<ClayIcon
												spritemap={SPRITEMAP}
												symbol="check-circle-full"
											/>
										)}

										{isActive && (
											<span
												aria-hidden="true"
												className="content-site-generator-review__phase-dot"
											/>
										)}

										{isFailed && (
											<ClayIcon
												spritemap={SPRITEMAP}
												symbol="exclamation-circle"
											/>
										)}

										{!isComplete &&
											!isActive &&
											!isFailed && (
												<span
													aria-hidden="true"
													className="content-site-generator-review__phase-dot content-site-generator-review__phase-dot--pending"
												/>
											)}
									</span>

									<span className="content-site-generator-review__phase-label">
										{Liferay.Language.get(key)}
									</span>

									{(isComplete || isActive) && (
										<ClayLabel
											displayType={
												isComplete ? 'success' : 'info'
											}
										>
											{`${percentage}%`}
										</ClayLabel>
									)}
								</div>

								{(isActive || isComplete) && (
									<div className="content-site-generator-review__phase-bar">
										<div
											className="content-site-generator-review__phase-bar-fill"
											style={{
												width: `${percentage}%`,
											}}
										/>

										{isComplete && (
											<ClayIcon
												className="content-site-generator-review__phase-bar-check"
												spritemap={SPRITEMAP}
												symbol="check-circle-full"
											/>
										)}

										{isActive && (
											<span className="content-site-generator-review__phase-bar-percentage">
												{`${percentage}%`}
											</span>
										)}
									</div>
								)}
							</li>
						);
					})}
				</ul>
			)}

			{mode === 'review' && !run?.resultingSiteERC && (
				<ClayAlert className="mb-3" displayType="warning">
					{Liferay.Language.get('no-resulting-site-was-recorded')}
				</ClayAlert>
			)}

			<StepActions
				backDisabled={false}
				backLabel={Liferay.Language.get('back-to-refine')}
				continueDisabled={
					mode === 'generating' && (!generationComplete || failed)
				}
				continueLabel={
					mode === 'review'
						? Liferay.Language.get('view-site')
						: Liferay.Language.get('continue')
				}
				onBack={handleBack}
				onCancel={handleCancel}
				onContinue={handleContinue}
			/>
		</div>
	);
}
