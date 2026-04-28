/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayEmptyState from '@clayui/empty-state';
import ClayIcon from '@clayui/icon';
import ClayLabel from '@clayui/label';
import ClayLayout from '@clayui/layout';
import ClayList from '@clayui/list';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import ClayPanel from '@clayui/panel';
import {sub} from 'frontend-js-web';
import React from 'react';

import {ContentSample} from '../types/ContentSample';
import {DetectedConfigItem} from '../types/DetectedConfigItem';
import {GeneratedItem} from '../types/GeneratedItem';
import {SummaryItem} from '../types/SummaryItem';
import {Template} from '../types/Template';
import ContentSampleItem from './ContentSampleItem';
import SummaryCard from './SummaryCard';

interface ContentPreviewFormProps {
	contentSamples: ContentSample[];
	detectedConfig: DetectedConfigItem[];
	generatedItems: GeneratedItem[];
	loading: boolean;
	summary: SummaryItem[];
	templates: Template[];
}

const ContentPreviewForm: React.FC<ContentPreviewFormProps> = ({
	contentSamples,
	detectedConfig,
	generatedItems,
	loading,
	summary,
	templates,
}) => {
	if (loading) {
		return (
			<div className="content-site-generator-refine__preview-loading text-center py-5">
				<ClayLoadingIndicator displayType="primary" size="md" />

				<p className="mt-3 text-secondary">
					{Liferay.Language.get('regenerating-preview')}
				</p>
			</div>
		);
	}

	return (
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
						{Liferay.Language.get('detected-configuration')}
					</h4>

					{detectedConfig.length ? (
						<ClayList className="border-0 content-site-generator-refine__config-list">
							{detectedConfig.map((item, index) => (
								<ClayList.Item className="px-0" flex key={index}>
									<ClayList.ItemField className="p-0" expand>
										<ClayList.ItemText>{item.label}</ClayList.ItemText>
									</ClayList.ItemField>

									<ClayList.ItemField>
										<ClayList.ItemText>{item.value}</ClayList.ItemText>
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
							title={Liferay.Language.get('no-configuration-detected')}
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
												Liferay.Language.get('x-entries'),
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
						title={Liferay.Language.get('no-content-samples-available')}
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

										{item.description ? ` ${item.description}` : ''}
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
		</>
	);
};

export default ContentPreviewForm;
