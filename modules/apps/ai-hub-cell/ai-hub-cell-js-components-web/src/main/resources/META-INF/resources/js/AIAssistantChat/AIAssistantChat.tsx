/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayDropDown from '@clayui/drop-down';
import ClayIcon from '@clayui/icon';
import {EventSource} from 'eventsource';
import React, {useEffect, useRef, useState} from 'react';

import {
	ChatContext,
	createEventSource,
	postChatByExternalReferenceCodeMessage,
} from './api';
import AIAssistantChatBody, {Message} from './components/AIAssistantChatBody';

import './chat.scss';

interface AIAssistantChatProps {
	compact?: boolean;
	embedded?: boolean;
	externalEventTypes?: string[];
	getContext: () => ChatContext;
	initialAssistantReply?: string;
	initialMessage?: string;
	onExternalEvent?: (type: string, data: string) => void;
}

const AIAssistantChat: React.FC<AIAssistantChatProps> = ({
	compact = false,
	embedded = false,
	externalEventTypes,
	getContext,
	initialAssistantReply,
	initialMessage,
	onExternalEvent,
}) => {
	const [active, setActive] = useState<boolean>(false);
	const [isGenerating, setIsGenerating] = useState<boolean>(false);
	const [messages, setMessages] = useState<Message[]>([]);
	const [message, setMessage] = useState<string>('');
	const eventSourceRef = useRef<EventSource | null>(null);
	const eventSourceReference = useRef<string | null>(null);
	const initialMessageAppliedRef = useRef<boolean>(false);
	const triggerRef = useRef<HTMLButtonElement | null>(null);

	useEffect(() => {
		if (initialMessageAppliedRef.current) {
			return;
		}

		const trimmedMessage = initialMessage?.trim();

		if (!trimmedMessage) {
			return;
		}

		initialMessageAppliedRef.current = true;

		const seeded: Message[] = [{sender: 'user', text: trimmedMessage}];

		const trimmedReply = initialAssistantReply?.trim();

		if (trimmedReply) {
			seeded.push({sender: 'assistant', text: trimmedReply});
		}

		setMessages(seeded);
	}, [initialAssistantReply, initialMessage]);

	function onSendMessage(text: string) {
		setMessages((previousMessages) => [
			...previousMessages,
			{sender: 'user', text},
		]);

		setMessage('');

		setIsGenerating(true);

		if (eventSourceReference.current) {
			postChatByExternalReferenceCodeMessage({
				chatContext: getContext(),
				eventSourceReference: eventSourceReference.current,
				message: text,
			});
		}
	}

	function openAIAssistantChatConnection() {
		createEventSource().then((eventSource) => {
			if (!eventSource) {
				return;
			}

			eventSourceRef.current = eventSource;

			eventSourceRef.current.addEventListener(
				'Chat Message Sent',
				(event) => {
					const dataJSON = JSON.parse(event.data);

					setMessages((previousMessages) => [
						...previousMessages,
						{
							sender: 'assistant',
							text: dataJSON['data'],
						},
					]);

					setIsGenerating(false);
				}
			);

			eventSourceRef.current.addEventListener('Subscribe', (event) => {
				eventSourceReference.current = event.data;
			});

			externalEventTypes?.forEach((type) => {
				eventSourceRef.current?.addEventListener(type, (event) => {
					onExternalEvent?.(type, event.data);
				});
			});
		});
	}

	function closeAIAssistantChatConnection() {
		eventSourceRef.current?.close();

		eventSourceRef.current = null;
	}

	useEffect(() => {
		openAIAssistantChatConnection();

		return () => {
			closeAIAssistantChatConnection();
		};
	}, []);

	if (embedded) {
		return (
			<AIAssistantChatBody
				embedded={embedded}
				isGenerating={isGenerating}
				message={message}
				messages={messages}
				onSendMessage={onSendMessage}
				setMessage={setMessage}
			/>
		);
	}

	return (
		<ClayDropDown
			active={active}
			alignmentPosition={4}
			className="d-flex p-0"
			hasRightSymbols={false}
			menuElementAttrs={{
				style: {
					height: 552,
					maxHeight: 'none',
					maxWidth: 'none',
					overflow: 'hidden',
					width: 448,
				},
			}}
			onActiveChange={setActive}
			trigger={
				<ClayButton
					aria-label={Liferay.Language.get('ai-assistant')}
					borderless
					className="text-primary"
					displayType="secondary"
					monospaced={compact}
					ref={triggerRef}
					size={compact ? 'sm' : undefined}
					title={
						compact
							? Liferay.Language.get('ai-assistant')
							: undefined
					}
				>
					<ClayIcon
						className={compact ? '' : 'mr-2'}
						height={16}
						spritemap={Liferay.Icons.spritemap}
						symbol="stars"
						width={16}
					/>

					{!compact && Liferay.Language.get('ai-assistant')}
				</ClayButton>
			}
		>
			<AIAssistantChatBody
				embedded={embedded}
				isGenerating={isGenerating}
				message={message}
				messages={messages}
				onCloseClick={() => setActive(false)}
				onSendMessage={onSendMessage}
				setMessage={setMessage}
			/>
		</ClayDropDown>
	);
};

export default AIAssistantChat;
