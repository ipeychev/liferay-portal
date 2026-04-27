/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import ClayButton from '@clayui/button';
import ClayEmptyState from '@clayui/empty-state';
import ClayIcon from '@clayui/icon';
import ClayLabel from '@clayui/label';
import {ClayPaginationWithBasicItems} from '@clayui/pagination';
import ClayPaginationBar from '@clayui/pagination-bar';
import ClayTable from '@clayui/table';
import {fetch as liferayFetch} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

const SPRITEMAP = `${Liferay.ThemeDisplay.getPathThemeImages()}/lexicon/icons.svg`;

const RUNS_URL = '/o/content-site-generator/runs';

const SITES_URL = '/o/headless-admin-site/v1.0/sites';

const POLL_INTERVAL_MS = 2_000;

const POLL_TIMEOUT_MS = 10 * 60 * 1_000;

const TYPE_DEFINITIONS: Array<{
	className: string;
	icon: string;
	labelKey: string;
}> = [
	{
		className: 'com.liferay.headless.admin.site.dto.v1_0.Site',
		icon: 'sites',
		labelKey: 'site',
	},
	{
		className: 'com.liferay.headless.delivery.dto.v1_0.SitePage',
		icon: 'page',
		labelKey: 'site-page',
	},
	{
		className: 'com.liferay.headless.delivery.dto.v1_0.Layout',
		icon: 'page',
		labelKey: 'site-page',
	},
	{
		className: 'com.liferay.headless.delivery.dto.v1_0.StructuredContent',
		icon: 'document',
		labelKey: 'structured-content',
	},
	{
		className: 'com.liferay.headless.admin.fragment.dto.v1_0.FragmentSet',
		icon: 'code',
		labelKey: 'fragment',
	},
];

const LANGUAGE_FROM_FILENAME = /-([a-z]{2})(?:[-_][A-Z]{2})?\.json$/i;

const LANGUAGE_FROM_I18N = /"[a-zA-Z]+_i18n"\s*:\s*\{\s*"([a-z]{2})/;

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

const getTypeDefinition = (className?: string) =>
	TYPE_DEFINITIONS.find((definition) => definition.className === className);

const getTypeLabel = (className?: string) => {
	const labelKey = getTypeDefinition(className)?.labelKey;

	return labelKey ? Liferay.Language.get(labelKey) : (className ?? '');
};

const getTypeIcon = (className?: string) =>
	getTypeDefinition(className)?.icon ?? 'document';

const getArtifactLanguage = (artifact: Artifact): string | null => {
	const fromFilename = artifact.fileName?.match(LANGUAGE_FROM_FILENAME);

	if (fromFilename) {
		return fromFilename[1].toLowerCase();
	}

	const fromJson = artifact.json?.match(LANGUAGE_FROM_I18N);

	if (fromJson) {
		return fromJson[1].toLowerCase();
	}

	return null;
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

const buildTitle = (artifact: Artifact) => {
	const className = artifact.className;
	const language = getArtifactLanguage(artifact);
	const typeLabel = getTypeLabel(className);

	if (artifact.fileName) {
		return artifact.fileName;
	}

	return language ? `${typeLabel} (${language.toUpperCase()})` : typeLabel;
};

export default function ReviewAndPublishStep({
	cancelURL,
	onBack,
	runId,
}: IProps) {
	const [artifacts, setArtifacts] = useState<Artifact[]>([]);
	const [error, setError] = useState<string | null>(null);
	const [loading, setLoading] = useState(true);
	const [page, setPage] = useState(1);
	const [pageSize, setPageSize] = useState(10);
	const [publishing, setPublishing] = useState(false);
	const [run, setRun] = useState<Run | null>(null);
	const [totalCount, setTotalCount] = useState(0);

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
						`${RUNS_URL}/${runId}/artifacts?page=${page}` +
							`&pageSize=${pageSize}&sort=loadOrder:asc`
					),
				]);

				if (!runResponse.ok || !artifactsResponse.ok) {
					throw new Error(
						`Failed to load run (${runResponse.status}/${artifactsResponse.status})`
					);
				}

				const runJson = await runResponse.json();
				const artifactsJson = await artifactsResponse.json();

				if (cancelled) {
					return;
				}

				setRun(runJson);
				setArtifacts(artifactsJson.items ?? []);
				setTotalCount(artifactsJson.totalCount ?? 0);
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
	}, [page, pageSize, runId]);

	const handleCancel = () => {
		if (cancelURL) {
			Liferay.Util.navigate(cancelURL);
		}
	};

	const handleBack = () => {
		if (onBack) {
			onBack();
		}
	};

	const handlePublish = async () => {
		if (!runId || publishing) {
			return;
		}

		setPublishing(true);
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
					`Failed to start commit (${commitResponse.status})`
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

				const pollRun: Run = await pollResponse.json();
				const status = pollRun.runStatus?.key;

				setRun(pollRun);

				if (status === 'committed') {
					await navigateToResultingSite(pollRun);

					return;
				}

				if (status === 'failed') {
					throw new Error(
						pollRun.failureReason ||
							Liferay.Language.get(
								'failed-to-publish-please-try-again'
							)
					);
				}
			}

			throw new Error(
				Liferay.Language.get('publishing-timed-out-please-try-again')
			);
		}
		catch (exception) {
			setError(
				exception instanceof Error
					? exception.message
					: String(exception)
			);
			setPublishing(false);
		}
	};

	const navigateToResultingSite = async (resolvedRun: Run) => {
		const externalReferenceCode = resolvedRun.resultingSiteERC;

		if (!externalReferenceCode) {
			setPublishing(false);

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

		setPublishing(false);

		if (cancelURL) {
			Liferay.Util.navigate(cancelURL);
		}
	};

	const status = run?.runStatus?.key ?? 'draft';
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

	if (loading) {
		return (
			<ClayEmptyState
				description={Liferay.Language.get(
					'loading-the-generated-content'
				)}
				spritemap={SPRITEMAP}
				title=""
			/>
		);
	}

	return (
		<div className="content-site-generator__review">
			<div className="content-site-generator__review-header">
				<h3 className="content-site-generator__section-title">
					{Liferay.Language.get('review-and-publish')}
				</h3>

				<p className="text-secondary">
					{Liferay.Language.get(
						'review-generated-pages-before-publishing-to-cms'
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

			<div className="content-site-generator__stats">
				<div className="content-site-generator__stat">
					<div className="content-site-generator__stat-label">
						<ClayIcon
							className="mr-2 text-secondary"
							spritemap={SPRITEMAP}
							symbol="document"
						/>

						{Liferay.Language.get('total-items')}
					</div>

					<div className="content-site-generator__stat-value">
						{totalItems}
					</div>
				</div>

				<div className="content-site-generator__stat">
					<div className="content-site-generator__stat-label">
						<ClayIcon
							className="mr-2 text-secondary"
							spritemap={SPRITEMAP}
							symbol="globe"
						/>

						{Liferay.Language.get('languages')}
					</div>

					<div className="content-site-generator__stat-value">
						{languages.size}
					</div>
				</div>

				<div className="content-site-generator__stat">
					<div className="content-site-generator__stat-label">
						<ClayIcon
							className="mr-2 text-secondary"
							spritemap={SPRITEMAP}
							symbol="info-circle"
						/>

						{Liferay.Language.get('status')}
					</div>

					<div className="content-site-generator__stat-value">
						<ClayLabel
							displayType={
								status === 'committed' ? 'success' : 'secondary'
							}
						>
							{Liferay.Language.get(status).toUpperCase()}
						</ClayLabel>
					</div>
				</div>
			</div>

			{!artifacts.length ? (
				<ClayEmptyState
					description={Liferay.Language.get(
						'no-generated-content-found'
					)}
					spritemap={SPRITEMAP}
					title={Liferay.Language.get('no-items-yet')}
				/>
			) : (
				<>
					<ClayTable className="content-site-generator__table">
						<ClayTable.Head>
							<ClayTable.Row>
								<ClayTable.Cell headingCell>
									{Liferay.Language.get('title')}
								</ClayTable.Cell>

								<ClayTable.Cell headingCell>
									{Liferay.Language.get('type')}
								</ClayTable.Cell>

								<ClayTable.Cell headingCell>
									{Liferay.Language.get('language')}
								</ClayTable.Cell>

								<ClayTable.Cell headingCell>
									{Liferay.Language.get('items')}
								</ClayTable.Cell>
							</ClayTable.Row>
						</ClayTable.Head>

						<ClayTable.Body>
							{artifacts.map((artifact) => {
								const language = getArtifactLanguage(artifact);

								return (
									<ClayTable.Row key={artifact.id}>
										<ClayTable.Cell>
											<ClayIcon
												className="mr-2 text-secondary"
												spritemap={SPRITEMAP}
												symbol={getTypeIcon(
													artifact.className
												)}
											/>

											{buildTitle(artifact)}
										</ClayTable.Cell>

										<ClayTable.Cell>
											{getTypeLabel(artifact.className)}
										</ClayTable.Cell>

										<ClayTable.Cell>
											{language
												? language.toUpperCase()
												: Liferay.Language.get(
														'none-detected'
													)}
										</ClayTable.Cell>

										<ClayTable.Cell>
											{getItemCount(artifact)}
										</ClayTable.Cell>
									</ClayTable.Row>
								);
							})}
						</ClayTable.Body>
					</ClayTable>

					{totalCount > pageSize && (
						<ClayPaginationBar>
							<ClayPaginationBar.DropDown
								items={[10, 20, 30, 50].map((size) => ({
									label: String(size),
									onClick: () => {
										setPageSize(size);
										setPage(1);
									},
								}))}
								trigger={
									<ClayButton displayType="unstyled">
										{Liferay.Util.sub(
											Liferay.Language.get('x-items'),
											String(pageSize)
										)}

										<ClayIcon
											className="ml-1"
											spritemap={SPRITEMAP}
											symbol="caret-bottom"
										/>
									</ClayButton>
								}
							/>

							<ClayPaginationBar.Results>
								{Liferay.Util.sub(
									Liferay.Language.get(
										'showing-x-to-x-of-x-entries'
									),
									String((page - 1) * pageSize + 1),
									String(
										Math.min(page * pageSize, totalCount)
									),
									String(totalCount)
								)}
							</ClayPaginationBar.Results>

							<ClayPaginationWithBasicItems
								activePage={page}
								ellipsisBuffer={1}
								onPageChange={setPage}
								totalPages={Math.ceil(totalCount / pageSize)}
							/>
						</ClayPaginationBar>
					)}
				</>
			)}

			<div className="content-site-generator__actions mt-4">
				<ClayButton
					disabled={publishing}
					displayType="secondary"
					onClick={handleBack}
				>
					{Liferay.Language.get('back')}
				</ClayButton>

				<ClayButton
					disabled={publishing}
					displayType="secondary"
					onClick={handleCancel}
				>
					{Liferay.Language.get('cancel')}
				</ClayButton>

				<ClayButton
					className="content-site-generator__analyze"
					disabled={
						publishing ||
						!artifacts.length ||
						status === 'committed'
					}
					displayType="primary"
					onClick={handlePublish}
				>
					{publishing
						? Liferay.Language.get('publishing')
						: Liferay.Language.get('publish')}

					{publishing && (
						<span
							aria-hidden="true"
							className="content-site-generator__analyze-spinner loading-animation loading-animation-sm"
						/>
					)}
				</ClayButton>
			</div>
		</div>
	);
}
