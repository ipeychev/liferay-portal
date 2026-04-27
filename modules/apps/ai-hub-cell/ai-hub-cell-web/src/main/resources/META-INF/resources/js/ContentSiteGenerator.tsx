/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import ClayButton from '@clayui/button';
import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import {fetch as liferayFetch} from 'frontend-js-web';
import React, {useState} from 'react';

import MultiStepProgress from './components/MultiStepProgress';
import {Example} from './types/Example';

const SPRITEMAP = `${Liferay.ThemeDisplay.getPathThemeImages()}/lexicon/icons.svg`;

const EXAMPLES: Example[] = [
	{
		icon: 'document',
		label: Liferay.Language.get(
			'build-25-blog-articles-about-low-code-development-api-first-architecture-headless-cms-and-digital-transformation-in-english-and-japanese'
		),
	},
	{
		icon: 'pencil',
		label: Liferay.Language.get(
			'create-5-blog-articles-about-industry-trends'
		),
	},
	{
		icon: 'home',
		label: Liferay.Language.get(
			'build-a-landing-page-with-hero-section-and-features'
		),
	},
	{
		icon: 'books',
		label: Liferay.Language.get(
			'create-75-glossary-pages-explaining-technical-terms-related-to-digital-experience-platforms-cms-and-web-development'
		),
	},
];

interface IProps {
	refineStepURL?: string;
}

const RUNS_URL = '/o/content-site-generator/runs';

const POLL_INTERVAL_MS = 1500;

const POLL_TIMEOUT_MS = 5 * 60 * 1000;

const buildRunName = (prompt: string) => {
	const trimmed = prompt.trim().split(/\s+/).slice(0, 6).join(' ');

	return trimmed.length > 60 ? `${trimmed.slice(0, 57)}...` : trimmed;
};

const sleep = (ms: number) =>
	new Promise<void>((resolve) => setTimeout(resolve, ms));

export default function ContentSiteGenerator({refineStepURL}: IProps) {
	const [prompt, setPrompt] = useState('');
	const [error, setError] = useState<string | null>(null);
	const [loading, setLoading] = useState(false);

	const hasText = !!prompt.trim().length;

	const handleAnalyze = async () => {
		if (!hasText || loading) {
			return;
		}

		setError(null);
		setLoading(true);

		try {
			const createResponse = await liferayFetch(RUNS_URL, {
				body: JSON.stringify({
					name: buildRunName(prompt) || 'Generator',
					prompt,
					runStatus: 'refining',
				}),
				headers: {'Content-Type': 'application/json'},
				method: 'POST',
			});

			if (!createResponse.ok) {
				throw new Error(
					`Failed to create run (${createResponse.status})`
				);
			}

			const run = await createResponse.json();
			const runId: number = run.id;

			// TODO: upload attachments via POST /o/content-site-generator/attachments
			// (multipart with FK r_attachments_l_contentGeneratorRunId).

			const analyzeResponse = await liferayFetch(
				`${RUNS_URL}/${runId}/object-actions/analyze`,
				{
					headers: {'Content-Type': 'application/json'},
					method: 'PUT',
				}
			);

			if (!analyzeResponse.ok) {
				throw new Error(
					`Failed to start analysis (${analyzeResponse.status})`
				);
			}

			const deadline = Date.now() + POLL_TIMEOUT_MS;

			while (Date.now() < deadline) {
				await sleep(POLL_INTERVAL_MS);

				const pollResponse = await liferayFetch(
					`${RUNS_URL}/${runId}`
				);

				if (!pollResponse.ok) {
					throw new Error(
						`Failed to poll run (${pollResponse.status})`
					);
				}

				const pollRun = await pollResponse.json();
				const status = pollRun?.runStatus?.key;

				if (status === 'ready') {
					if (refineStepURL) {
						const separator = refineStepURL.includes('?')
							? '&'
							: '?';

						Liferay.Util.navigate(
							`${refineStepURL}${separator}runId=${runId}`
						);
					}

					return;
				}

				if (status === 'failed') {
					throw new Error(
						Liferay.Language.get(
							'analysis-failed-please-try-again'
						)
					);
				}
			}

			throw new Error(
				Liferay.Language.get('analysis-timed-out-please-try-again')
			);
		}
		catch (exception) {
			setError(
				exception instanceof Error
					? exception.message
					: String(exception)
			);
			setLoading(false);
		}
	};

	return (
		<div className="content-site-generator">
			<ClayLayout.ContainerFluid view>
				<ClayLayout.Row justify="center">
					<ClayLayout.Col md={10} xl={8}>
						<div className="content-site-generator__progress">
							<MultiStepProgress
								activeStep={0}
								steps={[
									{title: Liferay.Language.get('ideate')},
									{title: Liferay.Language.get('refine')},
									{title: Liferay.Language.get('review')},
								]}
							/>
						</div>

						<div className="content-site-generator__title">
							<h2>
								{Liferay.Language.get(
									'what-do-you-want-to-create'
								)}
							</h2>

							<p className="text-secondary">
								{Liferay.Language.get(
									'describe-your-content-and-add-any-reference-materials-to-get-started'
								)}
							</p>
						</div>

						<textarea
							aria-label={Liferay.Language.get(
								'describe-your-content'
							)}
							className="content-site-generator__textarea form-control"
							disabled={loading}
							onChange={(event) => setPrompt(event.target.value)}
							placeholder={Liferay.Language.get(
								'prompt-example-placeholder'
							)}
							rows={5}
							value={prompt}
						/>

						{error && (
							<ClayAlert
								className="mt-3"
								displayType="danger"
								onClose={() => setError(null)}
							>
								{error}
							</ClayAlert>
						)}

						<div className="content-site-generator__actions">
							<ClayButton
								className="content-site-generator__analyze"
								disabled={!hasText || loading}
								displayType={hasText ? 'primary' : 'secondary'}
								onClick={handleAnalyze}
							>
								{loading
									? Liferay.Language.get('analyzing')
									: Liferay.Language.get(
											'analyze-and-configure'
										)}

								{loading ? (
									<span
										aria-hidden="true"
										className="content-site-generator__analyze-spinner loading-animation loading-animation-sm"
									/>
								) : (
									<ClayIcon
										className="ml-2"
										spritemap={SPRITEMAP}
										symbol="magic"
									/>
								)}
							</ClayButton>
						</div>

						<div className="content-site-generator__examples">
							<p className="font-weight-semi-bold">
								{Liferay.Language.get('try-an-example')}
							</p>

							<ul className="list-group">
								{EXAMPLES.map((example, index) => (
									<li
										aria-disabled={loading}
										className="content-site-generator__example list-group-item"
										key={index}
										onClick={() => {
											if (!loading) {
												setPrompt(example.label);
											}
										}}
										onKeyDown={(event) => {
											if (loading) {
												return;
											}

											if (
												event.key === 'Enter' ||
												event.key === ' '
											) {
												event.preventDefault();
												setPrompt(example.label);
											}
										}}
										role="button"
										tabIndex={loading ? -1 : 0}
									>
										<ClayIcon
											className="mr-2 text-secondary"
											spritemap={SPRITEMAP}
											symbol={example.icon}
										/>

										{example.label}
									</li>
								))}
							</ul>
						</div>
					</ClayLayout.Col>
				</ClayLayout.Row>
			</ClayLayout.ContainerFluid>
		</div>
	);
}
