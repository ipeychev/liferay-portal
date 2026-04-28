/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayForm from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import React, {useEffect, useRef} from 'react';

import AIAssistantMessageBalloon from './AIAssistantMessageBalloon';
import UserMessageBalloon from './UserMessageBalloon';

export interface Message {
	sender: string;
	text: string;
}

interface AIAssistantChatBodyProps {
	embedded: boolean;
	isGenerating: boolean;
	message: string;
	messages: Message[];
	onCloseClick?: () => void | null;
	onSendMessage: (text: string) => void;
	setMessage: (message: string) => void;
}

const AIAssistantChatBody: React.FC<AIAssistantChatBodyProps> = ({
	embedded,
	isGenerating,
	message,
	messages,
	onCloseClick = null,
	onSendMessage,
	setMessage,
}) => {
	const messagesEndRef = useRef<HTMLDivElement | null>(null);
	const textAreaRef = useRef<HTMLTextAreaElement | null>(null);

	useEffect(() => {
		messagesEndRef.current?.scrollIntoView({behavior: 'smooth'});
	}, [messages, isGenerating]);

	function adjustTextAreaHeight(element: HTMLTextAreaElement) {
		const textArea = element ?? textAreaRef.current;

		if (!textArea) {
			return;
		}

		const style = window.getComputedStyle(textArea);
		const lineHeight =
			parseFloat(style.lineHeight) || parseFloat(style.fontSize) * 1.2;
		const maxHeight = lineHeight * 4;

		textArea.style.height = 'auto';
		const newHeight = Math.min(textArea.scrollHeight, maxHeight);
		textArea.style.height = `${newHeight}px`;
		textArea.style.overflowY =
			textArea.scrollHeight > maxHeight ? 'auto' : 'hidden';
	}

	function handleTextAreaKeyDown(
		event: React.KeyboardEvent<HTMLTextAreaElement>
	) {
		if (event.key !== 'Enter') {
			event.stopPropagation();

			return;
		}

		if (event.shiftKey) {
			setTimeout(
				() => adjustTextAreaHeight(event.target as HTMLTextAreaElement),
				0
			);

			return;
		}

		event.preventDefault();

		const form = (event.target as HTMLElement).closest(
			'form'
		) as HTMLFormElement | null;

		if (form?.requestSubmit) {
			form.requestSubmit();
		}
		else {
			form?.dispatchEvent(
				new Event('submit', {
					bubbles: true,
					cancelable: true,
				})
			);
		}
	}

	function onSubmit(event: React.FormEvent<HTMLFormElement>) {
		event.preventDefault();

		if (!message.trim()) {
			return;
		}

		onSendMessage(message);
	}

	return (
		<div className="ai-assistant-chat__dropdown-container d-flex flex-column">
			{!embedded && (
				<div className="flex-shrink-0 p-3">
					<ClayLayout.ContentRow className="align-items-center border-bottom justify-content-between mb-3 pb-2">
						<ClayLayout.ContentCol className="ai-assistant-chat__dropdown-title font-weight-semi-bold">
							{Liferay.Language.get('ai-assistant')}
						</ClayLayout.ContentCol>

						<ClayLayout.ContentCol>
							<ClayButton
								aria-label={Liferay.Language.get('close')}
								borderless
								displayType="unstyled"
								onClick={onCloseClick}
							>
								<ClayIcon
									className="ai-assistant-chat__dropdown-close-button"
									spritemap={Liferay.Icons.spritemap}
									symbol="times"
								/>
							</ClayButton>
						</ClayLayout.ContentCol>
					</ClayLayout.ContentRow>
				</div>
			)}

			<div className="ai-assistant-chat__messages-container flex-grow-1 overflow-auto px-3">
				{!messages.length && (
					<AIAssistantMessageBalloon
						error={false}
						message="Hi! I can help you generate content, titles, tags, or
						translate your work. What would you like to do?"
					/>
				)}

				{messages.map((item, index) =>
					item.sender === 'user' ? (
						<UserMessageBalloon key={index} message={item.text} />
					) : (
						<AIAssistantMessageBalloon
							error={false}
							key={index}
							message={item.text}
						/>
					)
				)}

				{isGenerating && (
					<div className="ai-assistant-chat-balloon d-flex flex-row mb-2 rounded">
						<div className="align-items-center d-flex ml-2">
							<ClayLoadingIndicator />
						</div>

						<span className="ai-assistant-chat__generating-loading-text font-weight-semi-bold m-2 tex">
							{Liferay.Language.get('generating')}
						</span>
					</div>
				)}

				<div ref={messagesEndRef} />
			</div>

			<ClayForm className="flex-shrink-0 p-3" onSubmit={onSubmit}>
				<div className="align-items-end border-top d-flex flex-row pt-4">
					<textarea
						className="ai-assistant-chat__input form-control mr-2"
						disabled={isGenerating}
						id="assistant-user-input"
						onChange={(event) => {
							setMessage(event.target.value);
							adjustTextAreaHeight(event.target);
						}}
						onKeyDown={handleTextAreaKeyDown}
						placeholder="Ask me anything..."
						ref={textAreaRef}
						rows={1}
						value={message}
					/>

					<ClayButton
						disabled={!message.trim()}
						displayType="primary"
						type="submit"
					>
						<ClayIcon
							height={12}
							spritemap={Liferay.Icons.spritemap}
							symbol={isGenerating ? 'square' : 'order-arrow-up'}
							width={12}
						/>
					</ClayButton>
				</div>
			</ClayForm>
		</div>
	);
};

export default AIAssistantChatBody;
