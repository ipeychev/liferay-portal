/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayIcon from '@clayui/icon';
import ClayLabel from '@clayui/label';
import React from 'react';

import StepActions from './components/StepActions';
import {Task} from './types/Task';

const SPRITEMAP = `${Liferay.ThemeDisplay.getPathThemeImages()}/lexicon/icons.svg`;

interface Stat {
	icon: string;
	label: string;
	value: string;
}

const STATS: Stat[] = [
	{
		icon: 'document',
		label: Liferay.Language.get('content-entries'),
		value: '4 / 150',
	},
	{
		icon: 'magic',
		label: Liferay.Language.get('content-pages'),
		value: '5',
	},
	{
		icon: 'globe',
		label: Liferay.Language.get('languages'),
		value: '3',
	},
];

const TASKS: Task[] = [
	{
		label: Liferay.Language.get('analyzing-reference-documents'),
		progress: 100,
		status: 'completed',
	},
	{
		label: Liferay.Language.get('extracting-key-topics-and-features'),
		progress: 100,
		status: 'completed',
	},
	{
		label: Liferay.Language.get('generating-contents'),
		progress: 10,
		status: 'in-progress',
	},
	{
		label: Liferay.Language.get('generating-content-pages'),
		progress: 0,
		status: 'pending',
	},
	{
		label: Liferay.Language.get('localizing-to-target-languages'),
		progress: 0,
		status: 'pending',
	},
	{label: Liferay.Language.get('tbd'), progress: 0, status: 'pending'},
	{
		label: Liferay.Language.get('seo-optimization'),
		progress: 0,
		status: 'pending',
	},
];

interface IProps {
	onBack: () => void;
	onCancel: () => void;
	onContinue: () => void;
}

export default function GenerateStep({onBack, onCancel, onContinue}: IProps) {
	return (
		<div className="content-site-generator__generate">
			<h3 className="content-site-generator__section-title">
				{Liferay.Language.get('generate')}
			</h3>

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

			<ul className="content-site-generator__tasks list-unstyled">
				{TASKS.map((task, index) => (
					<li
						className={`content-site-generator__task content-site-generator__task--${task.status}`}
						key={index}
					>
						<div className="content-site-generator__task-header">
							<span className="content-site-generator__task-bullet">
								{task.status === 'completed' && (
									<ClayIcon
										spritemap={SPRITEMAP}
										symbol="check-circle-full"
									/>
								)}

								{task.status === 'in-progress' && (
									<span className="content-site-generator__task-bullet--in-progress" />
								)}

								{task.status === 'pending' && (
									<span className="content-site-generator__task-bullet--pending" />
								)}
							</span>

							<span className="content-site-generator__task-label">
								{task.label}
							</span>

							{task.status !== 'pending' && (
								<ClayLabel
									displayType={
										task.status === 'completed'
											? 'success'
											: 'info'
									}
								>
									{`${task.progress}%`}
								</ClayLabel>
							)}
						</div>

						{task.status !== 'pending' && (
							<div className="content-site-generator__task-progress">
								<div className="progress">
									<div
										className={`progress-bar ${
											task.status === 'completed'
												? 'bg-success'
												: 'bg-primary'
										}`}
										style={{width: `${task.progress}%`}}
									/>
								</div>

								<span className="content-site-generator__task-progress-end">
									{task.status === 'completed' ? (
										<ClayIcon
											className="text-success"
											spritemap={SPRITEMAP}
											symbol="check-circle"
										/>
									) : (
										`${task.progress}%`
									)}
								</span>
							</div>
						)}
					</li>
				))}
			</ul>

			<StepActions
				backLabel={Liferay.Util.sub(
					Liferay.Language.get('back-to-x'),
					Liferay.Language.get('refine')
				)}
				onBack={onBack}
				onCancel={onCancel}
				onContinue={onContinue}
			/>
		</div>
	);
}
