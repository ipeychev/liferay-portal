/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import ClayEmptyState from '@clayui/empty-state';
import ClayIcon from '@clayui/icon';
import ClayLabel from '@clayui/label';
import ClayLayout from '@clayui/layout';
import ClayList from '@clayui/list';
import ClayPanel from '@clayui/panel';
import {fetch as liferayFetch, sub} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

import ContentSampleItem from './components/ContentSampleItem';
import MultiStepProgress from './components/MultiStepProgress';
import StepActions from './components/StepActions';
import SummaryCard from './components/SummaryCard';
import {ContentSample} from './types/ContentSample';
import {DetectedConfigItem} from './types/DetectedConfigItem';
import {GeneratedItem} from './types/GeneratedItem';
import {SummaryItem} from './types/SummaryItem';
import {Template} from './types/Template';

interface IProps {
	backURL?: string;
	cancelURL?: string;
	continueURL?: string;
	onBack?: () => void;
	onCancel?: () => void;
	onContinue?: () => void;
	runId?: number;
}

interface Artifact {
	className?: string;
	fileName?: string;
	id: number;
	json?: string;
	loadOrder?: number;
}

interface Attachment {
	id: number;
	title?: string;
}

interface Run {
	id: number;
	name?: string;
	prompt?: string;
	runStatus?: {key?: string};
	targetLanguages?: string;
}

const RUNS_URL = '/o/content-site-generator/runs';

const PAGE_CLASS_NAMES = [
	'com.liferay.headless.admin.site.dto.v1_0.SitePage',
	'com.liferay.headless.delivery.dto.v1_0.SitePage',
];

const TYPE_DEFINITIONS: Array<{
	className: string;
	icon: string;
	label: string;
}> = [
	{
		className: 'com.liferay.headless.admin.site.dto.v1_0.SitePage',
		icon: 'home',
		label: Liferay.Language.get('site-page'),
	},
	{
		className: 'com.liferay.headless.delivery.dto.v1_0.SitePage',
		icon: 'home',
		label: Liferay.Language.get('site-page'),
	},
	{
		className: 'com.liferay.headless.delivery.dto.v1_0.StructuredContent',
		icon: 'document-text',
		label: Liferay.Language.get('structured-content'),
	},
	{
		className: 'com.liferay.headless.admin.fragment.dto.v1_0.FragmentSet',
		icon: 'code',
		label: Liferay.Language.get('fragment'),
	},
	{
		className: 'com.liferay.headless.delivery.dto.v1_0.FragmentEntry',
		icon: 'code',
		label: Liferay.Language.get('fragment'),
	},
	{
		className: 'com.liferay.headless.delivery.dto.v1_0.Document',
		icon: 'document',
		label: Liferay.Language.get('document'),
	},
];

const LANGUAGE_LABELS: Record<string, string> = {
	de: 'German',
	en: 'English',
	es: 'Spanish',
	fr: 'French',
	it: 'Italian',
	ja: 'Japanese',
	pt: 'Portuguese',
};

const LANGUAGE_FROM_FILENAME = /-([a-z]{2})(?:[-_][A-Z]{2})?\.json$/i;
const LANGUAGE_FROM_I18N = /"[a-zA-Z]+_i18n"\s*:\s*\{\s*"([a-z]{2})/;

const getTypeDefinition = (className: string | undefined) =>
	TYPE_DEFINITIONS.find((definition) => definition.className === className);

const getTypeLabel = (className: string | undefined) =>
	getTypeDefinition(className)?.label ??
	(className ? className.split('.').pop() ?? className : '');

const getTypeIcon = (className: string | undefined) =>
	getTypeDefinition(className)?.icon ?? 'document';

const getLanguageLabel = (code: string) =>
	LANGUAGE_LABELS[code.toLowerCase()] ?? code.toUpperCase();

const getArtifactLanguage = (artifact: Artifact): string | null => {
	const fromFilename = artifact.fileName?.match(LANGUAGE_FROM_FILENAME);

	if (fromFilename) {
		return fromFilename[1].toLowerCase();
	}

	const fromJson = artifact.json?.match(LANGUAGE_FROM_I18N);

	return fromJson ? fromJson[1].toLowerCase() : null;
};

const buildSummary = (
	artifacts: Artifact[],
	languages: string[],
	templateCount: number
): SummaryItem[] => {
	const pages = artifacts.filter(
		(artifact) => PAGE_CLASS_NAMES.includes(artifact.className ?? '')
	).length;

	return [
		{
			icon: 'document',
			title: Liferay.Language.get('total-pages'),
			value: pages,
		},
		{
			icon: 'automatic-translate',
			title: Liferay.Language.get('languages'),
			value: languages.length,
		},
		{
			icon: 'stars',
			title: Liferay.Language.get('templates'),
			value: templateCount,
		},
		{
			icon: 'document',
			title: Liferay.Language.get('total-entries'),
			value: artifacts.length,
		},
	];
};

const buildTemplates = (
	artifacts: Artifact[],
	languageCount: number
): Template[] => {
	const grouped = new Map<string, Artifact[]>();

	for (const artifact of artifacts) {
		const key = artifact.className ?? '';
		const list = grouped.get(key) ?? [];

		list.push(artifact);
		grouped.set(key, list);
	}

	return Array.from(grouped.entries()).map(([className, list]) => ({
		entries: list.length,
		icon: getTypeIcon(className),
		labels: [
			{
				text: sub(
					Liferay.Language.get('x-languages'),
					String(languageCount || 1)
				),
				type: 'success' as const,
			},
			{
				text: sub(Liferay.Language.get('x-entries'), String(list.length)),
				type: 'info' as const,
			},
		],
		name: getTypeLabel(className),
	}));
};

const buildContentSamples = (artifacts: Artifact[]): ContentSample[] =>
	artifacts.slice(0, 4).map((artifact) => {
		const language = getArtifactLanguage(artifact);

		return {
			fields: [
				{label: Liferay.Language.get('file-name'), value: artifact.fileName ?? ''},
				{label: Liferay.Language.get('class-name'), value: getTypeLabel(artifact.className)},
				...(language
					? [
							{
								label: Liferay.Language.get('language'),
								value: getLanguageLabel(language),
							},
						]
					: []),
			],
			tags: [],
			title: `${getTypeLabel(artifact.className)}${
				language ? ` - ${getLanguageLabel(language)}` : ''
			}`,
		};
	});

const buildGeneratedItems = (
	artifacts: Artifact[],
	languages: string[]
): GeneratedItem[] => {
	const items: GeneratedItem[] = [
		{
			description: sub(
				Liferay.Language.get(
					'across-x-content-types-and-x-languages'
				),
				String(
					new Set(
						artifacts.map((artifact) => artifact.className ?? '')
					).size
				),
				String(languages.length || 1)
			),
			title: sub(
				Liferay.Language.get('x-complete-content-entries'),
				String(artifacts.length)
			),
		},
	];

	if (languages.length) {
		items.push({
			description: languages.map(getLanguageLabel).join(', '),
			title: Liferay.Language.get('multi-language-support'),
		});
	}

	return items;
};

export default function RefineStep({
	backURL,
	cancelURL,
	continueURL,
	onBack,
	onCancel,
	onContinue,
	runId,
}: IProps) {
	const [artifacts, setArtifacts] = useState<Artifact[]>([]);
	const [attachments, setAttachments] = useState<Attachment[]>([]);
	const [error, setError] = useState<string | null>(null);
	const [generating, setGenerating] = useState(false);
	const [loading, setLoading] = useState(!!runId);
	const [run, setRun] = useState<Run | null>(null);
	const [showTip, setShowTip] = useState(true);

	useEffect(() => {
		if (!runId) {
			return;
		}

		let cancelled = false;

		(async () => {
			try {
				const [runResponse, artifactsResponse, attachmentsResponse] =
					await Promise.all([
						liferayFetch(`${RUNS_URL}/${runId}`),
						liferayFetch(`${RUNS_URL}/${runId}/artifacts?pageSize=100`),
						liferayFetch(
							`${RUNS_URL}/${runId}/attachments?pageSize=100`
						),
					]);

				if (!runResponse.ok) {
					throw new Error(
						`Failed to load run ${runId} (${runResponse.status})`
					);
				}

				const runJson = await runResponse.json();

				if (cancelled) {
					return;
				}

				const statusKey =
					runJson?.runStatus?.key?.toString().toLowerCase() ?? '';

				if (
					continueURL &&
					(statusKey === 'committed' || statusKey === 'generating')
				) {
					const separator = continueURL.includes('?') ? '&' : '?';

					cancelled = true;

					Liferay.Util.navigate(
						`${continueURL}${separator}runId=${runId}`
					);

					return;
				}

				const artifactsJson = artifactsResponse.ok
					? await artifactsResponse.json()
					: {items: []};
				const attachmentsJson = attachmentsResponse.ok
					? await attachmentsResponse.json()
					: {items: []};

				setRun(runJson);
				setArtifacts(artifactsJson.items ?? []);
				setAttachments(attachmentsJson.items ?? []);
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
	}, [continueURL, runId]);

	const handleBack = () => {
		if (onBack) {
			onBack();

			return;
		}

		if (backURL) {
			Liferay.Util.navigate(backURL);
		}
	};

	const handleCancel = async () => {
		if (onCancel) {
			onCancel();

			return;
		}

		if (runId) {
			try {
				await liferayFetch(`${RUNS_URL}/${runId}`, {method: 'DELETE'});
			}
			catch (exception) {
				// Best effort: still navigate away.
			}
		}

		if (cancelURL) {
			Liferay.Util.navigate(cancelURL);
		}
	};

	const handleContinue = async () => {
		if (generating) {
			return;
		}

		if (onContinue) {
			onContinue();

			return;
		}

		if (!runId) {
			if (continueURL) {
				Liferay.Util.navigate(continueURL);
			}

			return;
		}

		setGenerating(true);
		setError(null);

		try {
			const commitResponse = await liferayFetch(
				`${RUNS_URL}/${runId}/object-actions/commit`,
				{
					headers: {'Content-Type': 'application/json'},
					method: 'PUT',
				}
			);

			if (!commitResponse.ok) {
				throw new Error(
					`Failed to start generation (${commitResponse.status})`
				);
			}

			if (continueURL) {
				const separator = continueURL.includes('?') ? '&' : '?';

				Liferay.Util.navigate(
					`${continueURL}${separator}runId=${runId}`
				);
			}
		}
		catch (exception) {
			setError(
				exception instanceof Error
					? exception.message
					: String(exception)
			);
			setGenerating(false);
		}
	};

	const languages = Array.from(
		new Set(
			artifacts
				.map((artifact) => getArtifactLanguage(artifact))
				.filter((language): language is string => !!language)
		)
	);
	const templates = buildTemplates(artifacts, languages.length);
	const summary = buildSummary(artifacts, languages, templates.length);
	const contentSamples = buildContentSamples(artifacts);
	const generatedItems = buildGeneratedItems(artifacts, languages);
	const detectedConfig: DetectedConfigItem[] = [
		{
			label: Liferay.Language.get('languages'),
			value: languages.length
				? languages.map(getLanguageLabel).join(', ')
				: Liferay.Language.get('none-detected'),
		},
		{
			label: Liferay.Language.get('reference-documents'),
			value: sub(Liferay.Language.get('x-files'), attachments.length),
		},
	];
	const promptText = run?.prompt ?? '';
	const attachmentNames = attachments.map(
		(attachment, index) =>
			attachment.title ?? `${Liferay.Language.get('attachment')} ${index + 1}`
	);

	return (
		<div className="content-site-generator">
			<ClayLayout.ContainerFluid view>
				<ClayLayout.Row justify="center">
					<ClayLayout.Col md={10} xl={8}>
						<div className="content-site-generator__progress">
							<MultiStepProgress
								activeStep={1}
								steps={[
									{title: Liferay.Language.get('ideate')},
									{title: Liferay.Language.get('refine')},
									{title: Liferay.Language.get('review')},
								]}
							/>
						</div>

						<div className="content-site-generator-refine">
							<div className="content-site-generator-refine__header">
								<h3>
									{Liferay.Language.get(
										'preview-content-to-be-generated'
									)}
								</h3>

								<p className="text-secondary">
									{Liferay.Language.get(
										'review-the-configuration-and-content-samples-before-generating'
									)}
								</p>
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

							{loading ? (
								<ClayEmptyState
									description={Liferay.Language.get(
										'fetching-the-generated-content'
									)}
									small
									title={Liferay.Language.get('loading')}
								/>
							) : (
								<>
									{summary.length ? (
										<ClayLayout.Row className="content-site-generator-refine__summary">
											{summary.map((item, index) => (
												<ClayLayout.Col key={index} md={3}>
													<SummaryCard
														icon={item?.icon}
														title={item?.title}
														value={item?.value}
													/>
												</ClayLayout.Col>
											))}
										</ClayLayout.Row>
									) : (
										<ClayEmptyState
											description={Liferay.Language.get(
												'configuration-summary-will-appear-here'
											)}
											small
											title={Liferay.Language.get('no-summary-available')}
										/>
									)}

									<ClayPanel
										className="content-site-generator-refine__section"
										displayType="secondary"
									>
										<ClayPanel.Body>
											<h4 className="content-site-generator-refine__section-title">
												{Liferay.Language.get('your-prompt')}
											</h4>

											{promptText && (
												<>
													<p className="content-site-generator-refine__prompt">
														{`"${promptText}"`}
													</p>

													<div className="dropdown-divider" />

													<p className="content-site-generator-refine__attachments-label text-secondary">
														{attachmentNames.length
															? sub(
																	Liferay.Language.get(
																		'attached-files-x'
																	),
																	attachmentNames.length
																)
															: Liferay.Language.get('attached-files')}
													</p>

													{attachmentNames.length ? (
														<div className="content-site-generator-refine__attachments">
															{attachmentNames.map(
																(file: string, index: number) => (
																	<ClayLabel
																		displayType="secondary"
																		key={index}
																	>
																		{file}
																	</ClayLabel>
																)
															)}
														</div>
													) : (
														<p className="font-italic text-secondary">
															{Liferay.Language.get('no-files-attached')}
														</p>
													)}
												</>
											)}
										</ClayPanel.Body>
									</ClayPanel>

									<ClayPanel
										className="content-site-generator-refine__section"
										displayType="secondary"
									>
										<ClayPanel.Body>
											<h4 className="content-site-generator-refine__section-title">
												{Liferay.Language.get('detected-configuration')}
											</h4>

											{detectedConfig.length ? (
												<ClayList className="border-0 content-site-generator-refine__config-list">
													{detectedConfig.map((item, index) => (
														<ClayList.Item className="px-0" flex key={index}>
															<ClayList.ItemField className="p-0" expand>
																<ClayList.ItemText>
																	{item.label}
																</ClayList.ItemText>
															</ClayList.ItemField>

															<ClayList.ItemField>
																<ClayList.ItemText>
																	{item.value}
																</ClayList.ItemText>
															</ClayList.ItemField>
														</ClayList.Item>
													))}
												</ClayList>
											) : (
												<ClayEmptyState
													description={Liferay.Language.get(
														'detected-settings-will-appear-here'
													)}
													small
													title={Liferay.Language.get(
														'no-configuration-detected'
													)}
												/>
											)}
										</ClayPanel.Body>
									</ClayPanel>

									<section className="content-site-generator-refine__section">
										<h3 className="content-site-generator-refine__section-heading">
											{Liferay.Language.get('content-by-template-type')}
										</h3>

										{templates.length ? (
											templates.map((template, index) => (
												<ClayPanel
													className="content-site-generator-refine__template"
													displayType="secondary"
													key={index}
												>
													<ClayPanel.Body>
														<ClayLayout.ContentRow>
															<ClayLayout.Col size={1}>
																<ClayIcon symbol={template.icon} />
															</ClayLayout.Col>

															<ClayLayout.ContentCol expand>
																<h5 className="content-site-generator-refine__template-name">
																	{template.name}
																</h5>

																<span className="content-site-generator-refine__template-entries text-secondary">
																	{sub(
																		Liferay.Language.get(
																			'x-entries'
																		),
																		template.entries
																	)}
																</span>

																<div className="content-site-generator-refine__template-labels">
																	{template.labels.map((label, i) => (
																		<ClayLabel
																			displayType={label.type}
																			key={i}
																		>
																			{label.text}
																		</ClayLabel>
																	))}
																</div>
															</ClayLayout.ContentCol>
														</ClayLayout.ContentRow>
													</ClayPanel.Body>
												</ClayPanel>
											))
										) : (
											<ClayEmptyState
												description={Liferay.Language.get(
													'template-breakdown-will-appear-here'
												)}
												small
												title={Liferay.Language.get('no-templates-detected')}
											/>
										)}
									</section>

									<section className="content-site-generator-refine__section">
										<h3 className="content-site-generator-refine__section-heading">
											{Liferay.Language.get('content-samples')}
										</h3>

										<p className="text-secondary">
											{Liferay.Language.get(
												'preview-of-how-your-generated-content-will-be-structured'
											)}
										</p>

										{contentSamples.length ? (
											contentSamples.map((sample, index) => (
												<ContentSampleItem
													defaultExpanded={index === 0}
													key={index}
													sample={sample}
												/>
											))
										) : (
											<ClayEmptyState
												description={Liferay.Language.get(
													'content-previews-will-appear-here'
												)}
												small
												title={Liferay.Language.get(
													'no-content-samples-available'
												)}
											/>
										)}
									</section>

									<section>
										<h3 className="mb-3">
											{Liferay.Language.get('what-will-be-generated?')}
										</h3>

										{generatedItems.length ? (
											<ClayList className="content-site-generator-refine__generated-list">
												{generatedItems.map((item, index) => (
													<ClayList.Item flex key={index}>
														<ClayList.ItemField>
															<ClayIcon symbol="check" />
														</ClayList.ItemField>

														<ClayList.ItemField expand>
															<ClayList.ItemText>
																<strong>{item.title}</strong>

																{item.description
																	? ` ${item.description}`
																	: ''}
															</ClayList.ItemText>
														</ClayList.ItemField>
													</ClayList.Item>
												))}
											</ClayList>
										) : (
											<ClayEmptyState
												description={Liferay.Language.get(
													'generation-breakdown-will-appear-here'
												)}
												small
												title={Liferay.Language.get('nothing-to-generate-yet')}
											/>
										)}
									</section>

									{showTip && (
										<ClayAlert
											className="content-site-generator-refine__tip"
											displayType="info"
											onClose={() => setShowTip(false)}
											title={Liferay.Language.get('tip')}
										>
											{Liferay.Language.get(
												'use-the-chat-on-the-left-to-refine-your-requirements-before-generating-you-can-ask-to-add-more-pages-change-layouts-or-adjust-any-configuration'
											)}
										</ClayAlert>
									)}
								</>
							)}

							<StepActions
								backDisabled={generating}
								backLabel={Liferay.Language.get('back-to-prompt')}
								cancelDisabled={generating}
								continueDisabled={loading || !runId}
								continueLabel={Liferay.Language.get('generate')}
								continueLoading={generating}
								onBack={handleBack}
								onCancel={handleCancel}
								onContinue={handleContinue}
							/>
						</div>
					</ClayLayout.Col>
				</ClayLayout.Row>
			</ClayLayout.ContainerFluid>
		</div>
	);
}
