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
import {sub} from 'frontend-js-web';
import React, {useState} from 'react';

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
	attachments?: string[];
	backURL?: string;
	cancelURL?: string;
	contentSamples?: ContentSample[];
	continueURL?: string;
	detectedConfig?: DetectedConfigItem[];
	generatedItems?: GeneratedItem[];
	onBack?: () => void;
	onCancel?: () => void;
	onContinue?: () => void;
	prompt?: string;
	summary?: SummaryItem[];
	templates?: Template[];
}


const MOCK_SUMMARY: SummaryItem[] = [
	{
		icon: 'document',
		title: Liferay.Language.get('total-pages'),
		value: 60,
	},
	{
		icon: 'automatic-translate',
		title: Liferay.Language.get('languages'),
		value: 3,
	},
	{
		icon: 'stars',
		title: Liferay.Language.get('templates'),
		value: 3,
	},
	{
		icon: 'document',
		title: Liferay.Language.get('total-entries'),
		value: 180,
	},
];

const MOCK_PROMPT =
	'Generate 10 product pages with detailed specifications, also 10 blogs in spanish and english';

const MOCK_ATTACHMENTS = ['image (2).png', 'image (4).png'];

const MOCK_DETECTED_CONFIG: DetectedConfigItem[] = [
	{
		label: Liferay.Language.get('languages'),
		value: 'English (US), Spanish',
	},
	{
		label: Liferay.Language.get('reference-documents'),
		value: sub(Liferay.Language.get('x-files'), 2),
	},
];

const MOCK_TEMPLATES: Template[] = [
	{
		entries: 60,
		icon: 'shopping-cart',
		labels: [
			{text: '3 Languages', type: 'success'},
			{text: '20 Pages', type: 'info'},
		],
		name: 'Product Page',
	},
	{
		entries: 60,
		icon: 'polls',
		labels: [
			{text: '3 Languages', type: 'success'},
			{text: '20 Pages', type: 'info'},
		],
		name: 'Comparison Page',
	},
	{
		entries: 60,
		icon: 'document-text',
		labels: [
			{text: '3 Languages', type: 'success'},
			{text: '20 Pages', type: 'info'},
		],
		name: 'Blog Article',
	},
];

const MOCK_CONTENT_SAMPLES: ContentSample[] = [
	{
		fields: [
			{
				label: 'SEO Title',
				value: 'Premium Your content Products | Buy Online',
			},
			{
				label: 'Meta Description',
				value: 'Shop our selection of high-quality your content products. Fast shipping, competitive prices, and excellent customer service.',
			},
			{label: 'URL', value: '/products/your-content'},
			{label: 'H1 Heading', value: 'Your content Products'},
			{
				label: 'Excerpt',
				value: 'Explore our curated collection of your content products designed for performance and durability.',
			},
		],
		tags: [
			'Product Name',
			'SEO Title',
			'Meta Description',
			'Price',
			'SKU',
			'Description',
			'Features',
			'Specifications',
			'Images',
			'Stock Status',
		],
		title: 'Product Page - Spanish',
	},
	{fields: [], tags: [], title: 'Product Page - English'},
	{fields: [], tags: [], title: 'Blog Article - Spanish'},
	{fields: [], tags: [], title: 'Blog Article - English'},
];

const MOCK_GENERATED_ITEMS: GeneratedItem[] = [
	{
		description: '(15 pages × 2 languages)',
		title: '30 complete content entries',
	},
	{
		description:
			'including titles, descriptions, keywords, and structured data',
		title: 'SEO-optimized metadata',
	},
	{
		description: 'with content for: English (US), Spanish (SP)',
		title: 'Multi-language support',
	},
	{
		description: 'optimized for each content type',
		title: 'Layout-specific structures',
	},
	{
		description: 'and canonical URLs for each page',
		title: 'URL structures',
	},
	{
		description: 'for enhanced search engine visibility',
		title: 'Schema.org markup',
	},
];

export default function RefineStep({
	attachments = MOCK_ATTACHMENTS,
	backURL,
	cancelURL,
	contentSamples = MOCK_CONTENT_SAMPLES,
	continueURL,
	detectedConfig = MOCK_DETECTED_CONFIG,
	generatedItems = MOCK_GENERATED_ITEMS,
	onBack,
	onCancel,
	onContinue,
	prompt = MOCK_PROMPT,
	summary = MOCK_SUMMARY,
	templates = MOCK_TEMPLATES,
}: IProps) {
	const [showTip, setShowTip] = useState(true);

	const handleBack = () => {
		if (onBack) {
			onBack();
		}
		else if (backURL) {
			Liferay.Util.navigate(backURL);
		}
	};

	const handleCancel = () => {
		if (onCancel) {
			onCancel();
		}
		else if (cancelURL) {
			Liferay.Util.navigate(cancelURL);
		}
	};

	const handleContinue = () => {
		if (onContinue) {
			onContinue();
		}
		else if (continueURL) {
			Liferay.Util.navigate(continueURL);
		}
	};

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

					{prompt && (
						<>
							<p className="content-site-generator-refine__prompt">
								{`"${prompt}"`}
							</p>

							<div className="dropdown-divider" />

							<p className="content-site-generator-refine__attachments-label text-secondary">
								{attachments.length
									? sub(
											Liferay.Language.get(
												'attached-files-x'
											),
											attachments.length
										)
									: Liferay.Language.get('attached-files')}
							</p>

							{attachments.length ? (
								<div className="content-site-generator-refine__attachments">
									{attachments.map(
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
							displayType='secondary'
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
				<h3 className='mb-3'>
					{Liferay.Language.get('what-will-be-generated?')}
				</h3>

				{generatedItems.length ? (
					<ClayList className="content-site-generator-refine__generated-list">
								{generatedItems.map((item, index) => (
									<ClayList.Item flex key={index}>
										<ClayList.ItemField>
											<ClayIcon
												symbol="check"
											/>
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

							<StepActions
								backLabel={Liferay.Language.get(
									'back-to-prompt'
								)}
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