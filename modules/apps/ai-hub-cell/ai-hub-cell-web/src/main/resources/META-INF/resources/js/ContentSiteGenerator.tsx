/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import React, {useRef, useState} from 'react';

import MultiStepProgress from './MultiStepProgress';

const SPRITEMAP = `${Liferay.ThemeDisplay.getPathThemeImages()}/lexicon/icons.svg`;

interface Example {
	icon: string;
	label: string;
}

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

export default function ContentSiteGenerator() {
	const [prompt, setPrompt] = useState('');
	const [attachedFiles, setAttachedFiles] = useState<File[]>([]);
	const fileInputRef = useRef<HTMLInputElement>(null);

	const hasText = !!prompt.trim().length;

	const handleAttachFiles = () => {
		fileInputRef.current?.click();
	};

	const handleFilesSelected = (
		event: React.ChangeEvent<HTMLInputElement>
	) => {
		const files = event.target.files;

		if (files) {
			setAttachedFiles(Array.from(files));
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
							onChange={(event) => setPrompt(event.target.value)}
							placeholder={Liferay.Language.get(
								'prompt-example-placeholder'
							)}
							rows={5}
							value={prompt}
						/>

						<div className="content-site-generator__actions">
							<ClayButton
								displayType="secondary"
								onClick={handleAttachFiles}
							>
								{Liferay.Language.get('attach-files')}

								<ClayIcon
									className="ml-2"
									spritemap={SPRITEMAP}
									symbol="paperclip"
								/>

								{!!attachedFiles.length && (
									<span className="ml-2 text-secondary">
										{`(${attachedFiles.length})`}
									</span>
								)}
							</ClayButton>

							<input
								className="d-none"
								multiple
								onChange={handleFilesSelected}
								ref={fileInputRef}
								type="file"
							/>

							<ClayButton
								disabled={!hasText}
								displayType={hasText ? 'primary' : 'secondary'}
							>
								{Liferay.Language.get('analyze-and-configure')}

								<ClayIcon
									className="ml-2"
									spritemap={SPRITEMAP}
									symbol="magic"
								/>
							</ClayButton>
						</div>

						<div className="content-site-generator__examples">
							<p className="font-weight-semi-bold">
								{Liferay.Language.get('try-an-example')}
							</p>

							<ul className="list-group">
								{EXAMPLES.map((example, index) => (
									<li
										className="content-site-generator__example list-group-item"
										key={index}
										onClick={() => setPrompt(example.label)}
										onKeyDown={(event) => {
											if (
												event.key === 'Enter' ||
												event.key === ' '
											) {
												event.preventDefault();
												setPrompt(example.label);
											}
										}}
										role="button"
										tabIndex={0}
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
