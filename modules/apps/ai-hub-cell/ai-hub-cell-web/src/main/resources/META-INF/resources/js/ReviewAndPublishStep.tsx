/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {ClayCheckbox, ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLabel from '@clayui/label';
import {ClayPaginationWithBasicItems} from '@clayui/pagination';
import ClayPaginationBar from '@clayui/pagination-bar';
import ClayTable from '@clayui/table';
import React, {useState} from 'react';

import {Entry} from './types/Entry';

const SPRITEMAP = `${Liferay.ThemeDisplay.getPathThemeImages()}/lexicon/icons.svg`;

interface Stat {
	icon: string;
	label: string;
	value: React.ReactNode;
}

const STATS: Stat[] = [
	{
		icon: 'document',
		label: Liferay.Language.get('total-items'),
		value: '155',
	},
	{
		icon: 'globe',
		label: Liferay.Language.get('languages'),
		value: '2',
	},
	{
		icon: 'document',
		label: Liferay.Language.get('status'),
		value: (
			<ClayLabel displayType="secondary">
				{Liferay.Language.get('draft').toUpperCase()}
			</ClayLabel>
		),
	},
];

const LANGUAGES = 'Spanish, Italian, French';

const ENTRIES: Entry[] = [
	{
		icon: 'folder',
		items: 60,
		language: LANGUAGES,
		title: 'Products',
		url: '/products/blog-article-1----spanish',
	},
	{
		icon: 'document',
		items: 1,
		language: LANGUAGES,
		title: 'Product',
		url: '/products/blog-article-1----german',
	},
	{
		icon: 'document',
		items: 1,
		language: LANGUAGES,
		title: 'Blogs',
		url: '/products/blog-article-1----english-(us)',
	},
	{
		icon: 'folder',
		items: 20,
		language: LANGUAGES,
		title: 'Blog Articles',
		url: '/products/blog-article-1----spanish',
	},
	{
		icon: 'document',
		items: 1,
		language: LANGUAGES,
		title: 'Blog Article',
		url: '/products/blog-article-1----german',
	},
	{
		icon: 'document',
		items: 1,
		language: LANGUAGES,
		title: 'Contact',
		url: '/products/blog-article-1----english-(us)',
	},
	{
		icon: 'folder',
		items: 30,
		language: LANGUAGES,
		title: 'Contact form',
		url: '/products/blog-article-1----german',
	},
];

const TOTAL_ENTRIES = 400;

export default function ReviewAndPublishStep() {
	const [page, setPage] = useState(2);
	const [pageSize, setPageSize] = useState(10);
	const [search, setSearch] = useState('');
	const [selected, setSelected] = useState<Set<number>>(new Set());

	const allSelected =
		!!ENTRIES.length && selected.size === ENTRIES.length;

	const toggleSelectAll = () => {
		if (allSelected) {
			setSelected(new Set());
		}
		else {
			setSelected(new Set(ENTRIES.map((_, index) => index)));
		}
	};

	const toggleSelected = (index: number) => {
		const next = new Set(selected);

		if (next.has(index)) {
			next.delete(index);
		}
		else {
			next.add(index);
		}

		setSelected(next);
	};

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

			<div className="content-site-generator__stats">
				{STATS.map((stat, index) => (
					<div className="content-site-generator__stat" key={index}>
						<div className="content-site-generator__stat-label">
							<ClayIcon
								className="mr-2 text-secondary"
								spritemap={SPRITEMAP}
								symbol={stat.icon}
							/>

							{stat.label}
						</div>

						<div className="content-site-generator__stat-value">
							{stat.value}
						</div>
					</div>
				))}
			</div>

			<div className="content-site-generator__toolbar">
				<ClayCheckbox
					aria-label={Liferay.Language.get('select-all')}
					checked={allSelected}
					onChange={toggleSelectAll}
				/>

				<ClayButton displayType="unstyled">
					<ClayIcon
						className="mr-2"
						spritemap={SPRITEMAP}
						symbol="filter"
					/>

					{Liferay.Language.get('filter')}

					<ClayIcon
						className="ml-1"
						spritemap={SPRITEMAP}
						symbol="caret-bottom"
					/>
				</ClayButton>

				<ClayButton displayType="unstyled">
					<ClayIcon
						className="mr-2"
						spritemap={SPRITEMAP}
						symbol="order-arrow"
					/>

					{Liferay.Language.get('order')}

					<ClayIcon
						className="ml-1"
						spritemap={SPRITEMAP}
						symbol="caret-bottom"
					/>
				</ClayButton>

				<ClayInput.Group className="content-site-generator__search">
					<ClayInput.GroupItem>
						<ClayInput
							aria-label={Liferay.Language.get('search')}
							onChange={(event) => setSearch(event.target.value)}
							placeholder={Liferay.Language.get('search')}
							type="text"
							value={search}
						/>

						<ClayInput.GroupInsetItem after tag="span">
							<ClayButton
								displayType="unstyled"
								title={Liferay.Language.get('search')}
							>
								<ClayIcon
									spritemap={SPRITEMAP}
									symbol="search"
								/>
							</ClayButton>
						</ClayInput.GroupInsetItem>
					</ClayInput.GroupItem>
				</ClayInput.Group>
			</div>

			<ClayTable className="content-site-generator__table">
				<ClayTable.Head>
					<ClayTable.Row>
						<ClayTable.Cell headingCell>
							<ClayCheckbox
								aria-label={Liferay.Language.get('select-all')}
								checked={allSelected}
								onChange={toggleSelectAll}
							/>
						</ClayTable.Cell>

						<ClayTable.Cell headingCell>
							{Liferay.Language.get('title')}
						</ClayTable.Cell>

						<ClayTable.Cell headingCell>
							{Liferay.Language.get('language')}
						</ClayTable.Cell>

						<ClayTable.Cell headingCell>
							{Liferay.Language.get('items')}
						</ClayTable.Cell>

						<ClayTable.Cell headingCell>
							{Liferay.Language.get('url')}
						</ClayTable.Cell>

						<ClayTable.Cell headingCell>
							<ClayButton
								aria-label={Liferay.Language.get(
									'column-options'
								)}
								className="component-action"
								displayType="unstyled"
							>
								<ClayIcon
									spritemap={SPRITEMAP}
									symbol="caret-bottom"
								/>
							</ClayButton>
						</ClayTable.Cell>
					</ClayTable.Row>
				</ClayTable.Head>

				<ClayTable.Body>
					{ENTRIES.map((entry, index) => (
						<ClayTable.Row key={index}>
							<ClayTable.Cell>
								<ClayCheckbox
									aria-label={Liferay.Language.get('select')}
									checked={selected.has(index)}
									onChange={() => toggleSelected(index)}
								/>
							</ClayTable.Cell>

							<ClayTable.Cell>
								<ClayIcon
									className="mr-2 text-secondary"
									spritemap={SPRITEMAP}
									symbol={entry.icon}
								/>

								<a href="#">{entry.title}</a>
							</ClayTable.Cell>

							<ClayTable.Cell>{entry.language}</ClayTable.Cell>

							<ClayTable.Cell>{entry.items}</ClayTable.Cell>

							<ClayTable.Cell>
								<span className="content-site-generator__url">
									{entry.url}
								</span>
							</ClayTable.Cell>

							<ClayTable.Cell>
								<ClayButton
									aria-label={Liferay.Language.get('actions')}
									className="component-action"
									displayType="unstyled"
								>
									<ClayIcon
										spritemap={SPRITEMAP}
										symbol="ellipsis-v"
									/>
								</ClayButton>
							</ClayTable.Cell>
						</ClayTable.Row>
					))}
				</ClayTable.Body>
			</ClayTable>

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
						Liferay.Language.get('showing-x-to-x-of-x-entries'),
						String((page - 1) * pageSize + 1),
						String(
							Math.min(page * pageSize, TOTAL_ENTRIES)
						),
						String(TOTAL_ENTRIES)
					)}
				</ClayPaginationBar.Results>

				<ClayPaginationWithBasicItems
					activePage={page}
					ellipsisBuffer={1}
					ellipsisProps={{
						'aria-label': Liferay.Language.get('more'),
						'title': Liferay.Language.get('more'),
					}}
					onPageChange={setPage}
					totalPages={Math.ceil(TOTAL_ENTRIES / pageSize)}
				/>
			</ClayPaginationBar>
		</div>
	);
}
