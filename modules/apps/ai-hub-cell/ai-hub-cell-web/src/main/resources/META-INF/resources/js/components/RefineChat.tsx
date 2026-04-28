/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayIcon from '@clayui/icon';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import React, {useEffect, useRef, useState} from 'react';

const SPRITEMAP = `${Liferay.ThemeDisplay.getPathThemeImages()}/lexicon/icons.svg`;

export interface ChatMessage {
	sender: 'assistant' | 'user';
	text: string;
}

interface IProps {
	disabled?: boolean;
	isGenerating?: boolean;
	messages: ChatMessage[];
	onSendMessage: (text: string) => void;
}

const QUICK_ACTIONS = [
	{
		icon: 'stars',
		label: Liferay.Language.get('assist-me-on-creating-pages'),
	},
	{
		icon: 'stars',
		label: Liferay.Language.get('generate-content'),
	},
	{
		icon: 'stars',
		label: Liferay.Language.get('translate-content'),
	},
];

export default function RefineChat({
	disabled = false,
	isGenerating = false,
	messages,
	onSendMessage,
}: IProps) {
	const [draft, setDraft] = useState('');
	const messagesEndRef = useRef<HTMLDivElement | null>(null);

	useEffect(() => {
		messagesEndRef.current?.scrollIntoView({behavior: 'smooth'});
	}, [messages, isGenerating]);

	const sendDraft = () => {
		const text = draft.trim();

		if (!text || disabled) {
			return;
		}

		onSendMessage(text);
		setDraft('');
	};

	const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
		event.preventDefault();
		sendDraft();
	};

	const handleKeyDown = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
		if (event.key === 'Enter' && !event.shiftKey) {
			event.preventDefault();
			sendDraft();
		}
	};

	const handleQuickAction = (label: string) => {
		if (disabled) {
			return;
		}

		onSendMessage(label);
	};

	return (
		<div className="content-site-generator-refine-chat d-flex flex-column">
			<div className="content-site-generator-refine-chat__messages flex-grow-1 overflow-auto">
				{messages.map((item, index) =>
					item.sender === 'user' ? (
						<div
							className="content-site-generator-refine-chat__user-message"
							key={index}
						>
							<span className="content-site-generator-refine-chat__user-message-text">
								{item.text}
							</span>

							<span className="content-site-generator-refine-chat__user-message-avatar">
								<ClayIcon
									spritemap={SPRITEMAP}
									symbol="user"
								/>
							</span>
						</div>
					) : (
						<div
							className="content-site-generator-refine-chat__assistant-message"
							key={index}
						>
							{item.text}
						</div>
					)
				)}

				{isGenerating && (
					<div className="content-site-generator-refine-chat__generating">
						<ClayLoadingIndicator className="mb-0 mt-0" />

						<span className="content-site-generator-refine-chat__generating-label">
							{Liferay.Language.get('structuring-the-content')}
						</span>
					</div>
				)}

				<div ref={messagesEndRef} />
			</div>

			<div className="content-site-generator-refine-chat__quick-actions">
				<p className="content-site-generator-refine-chat__quick-actions-label">
					{Liferay.Language.get('quick-actions')}
				</p>

				{QUICK_ACTIONS.map((action) => (
					<button
						className="btn btn-secondary btn-sm content-site-generator-refine-chat__quick-action"
						disabled={disabled}
						key={action.label}
						onClick={() => handleQuickAction(action.label)}
						type="button"
					>
						<ClayIcon
							className="mr-2"
							spritemap={SPRITEMAP}
							symbol={action.icon}
						/>

						{action.label}
					</button>
				))}
			</div>

			<form
				className="content-site-generator-refine-chat__input-form"
				onSubmit={handleSubmit}
			>
				<textarea
					aria-label={Liferay.Language.get('ask-me-anything')}
					className="content-site-generator-refine-chat__input form-control"
					disabled={disabled}
					onChange={(event) => setDraft(event.target.value)}
					onKeyDown={handleKeyDown}
					placeholder={Liferay.Language.get('ask-me-anything')}
					rows={2}
					value={draft}
				/>

				<div className="content-site-generator-refine-chat__input-actions">
					<button
						aria-label={Liferay.Language.get('attach-files')}
						className="btn btn-unstyled content-site-generator-refine-chat__attach"
						disabled={disabled}
						type="button"
					>
						<ClayIcon
							spritemap={SPRITEMAP}
							symbol="upload"
						/>
					</button>

					<ClayButton
						aria-label={Liferay.Language.get('send')}
						className="content-site-generator-refine-chat__send"
						disabled={disabled || !draft.trim()}
						displayType="primary"
						type="submit"
					>
						<ClayIcon spritemap={SPRITEMAP} symbol="order-arrow-up" />
					</ClayButton>
				</div>
			</form>
		</div>
	);
}
