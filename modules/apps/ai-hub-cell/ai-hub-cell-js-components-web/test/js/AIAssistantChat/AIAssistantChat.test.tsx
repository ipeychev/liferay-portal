/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {act, fireEvent, render, screen} from '@testing-library/react';
import React from 'react';

import '@testing-library/jest-dom';

import AIAssistantChat from '../../../src/main/resources/META-INF/resources/js/AIAssistantChat/AIAssistantChat';
import {
	createEventSource,
	postChatByExternalReferenceCodeMessage,
} from '../../../src/main/resources/META-INF/resources/js/AIAssistantChat/api';
import {classifyCategorizationIntent} from '../../../src/main/resources/META-INF/resources/js/Categorization/services/classifyCategorizationIntent';
import {postAIIssueReport} from '../../../src/main/resources/META-INF/resources/js/ReportFeedback/api';

jest.mock(
	'../../../src/main/resources/META-INF/resources/js/AIAssistantChat/api',
	() => ({
		createEventSource: jest.fn(() => Promise.resolve(null)),
		postChatByExternalReferenceCodeMessage: jest.fn(() =>
			Promise.resolve()
		),
	})
);

jest.mock(
	'../../../src/main/resources/META-INF/resources/js/AIAssistantChat/components/CategorizationMessageBalloon',
	() => ({
		__esModule: true,
		default: () => 'categorization-balloon',
	})
);

jest.mock(
	'../../../src/main/resources/META-INF/resources/js/Categorization/services/classifyCategorizationIntent'
);

jest.mock(
	'../../../src/main/resources/META-INF/resources/js/ReportFeedback/api'
);

const mockClassify = classifyCategorizationIntent as jest.MockedFunction<
	typeof classifyCategorizationIntent
>;
const mockCreateEventSource = createEventSource as jest.MockedFunction<
	typeof createEventSource
>;
const mockPostChat =
	postChatByExternalReferenceCodeMessage as jest.MockedFunction<
		typeof postChatByExternalReferenceCodeMessage
	>;
const mockPostAIIssueReport = postAIIssueReport as jest.MockedFunction<
	typeof postAIIssueReport
>;

const defaultProps = {
	getContext: () => ({}),
	instructionDefinitionScope: 'test-scope',
};

function createFakeEventSource() {
	const listeners: Record<string, (event: {data: string}) => void> = {};

	return {
		addEventListener: jest.fn(
			(type: string, handler: (event: {data: string}) => void) => {
				listeners[type] = handler;
			}
		),
		close: jest.fn(),
		emit(type: string, data: string) {
			listeners[type]?.({data});
		},
	};
}

async function renderAndOpen() {
	await act(async () => {
		render(<AIAssistantChat {...defaultProps} />);
	});

	await act(async () => {
		screen
			.getByRole('button', {name: 'ai-assistant'})
			.dispatchEvent(new MouseEvent('click', {bubbles: true}));
	});
}

describe('AIAssistantChat', () => {
	beforeEach(() => {
		window.HTMLElement.prototype.scrollIntoView = jest.fn();

		mockCreateEventSource.mockReset();
		mockCreateEventSource.mockResolvedValue(null);
		mockPostAIIssueReport.mockReset();
		mockPostAIIssueReport.mockResolvedValue({id: 'report-1'});

		global.Liferay = {
			...global.Liferay,
			Util: {
				...global.Liferay?.Util,
				openToast: jest.fn(),
			},
		};
	});

	it('shows the chat input immediately on open', async () => {
		await renderAndOpen();

		expect(
			screen.getByPlaceholderText('Ask me anything...')
		).toBeInTheDocument();
	});

	it('shows the footer disclaimer', async () => {
		await renderAndOpen();

		expect(
			screen.getByText('ai-generated-responses-may-be-inaccurate')
		).toBeInTheDocument();
	});

	it('exposes the feedback row on a successful message and wires the codes', async () => {
		const fakeEventSource = createFakeEventSource();

		mockCreateEventSource.mockResolvedValue(fakeEventSource as never);

		await renderAndOpen();

		await act(async () => {
			fakeEventSource.emit(
				'Chat Message Sent',
				JSON.stringify({
					agentDefinitionExternalReferenceCodes: ['agent-x'],
					data: 'Here is your answer',
				})
			);
		});

		expect(
			screen.getByRole('button', {name: 'report-bad-result'})
		).toBeInTheDocument();

		await act(async () => {
			fireEvent.click(
				screen.getByRole('button', {name: 'good-response'})
			);
		});

		expect(mockPostAIIssueReport).toHaveBeenCalledWith({
			agentDefinitionExternalReferenceCodes: ['agent-x'],
			feedback: 'positive',
			surface: 'aiAssistant',
		});
	});

	it('hides the feedback row on an error message', async () => {
		const fakeEventSource = createFakeEventSource();

		mockCreateEventSource.mockResolvedValue(fakeEventSource as never);

		await renderAndOpen();

		await act(async () => {
			fakeEventSource.emit(
				'Agent Invocation Failed',
				JSON.stringify({data: 'Something went wrong'})
			);
		});

		expect(screen.getByText('Something went wrong')).toBeInTheDocument();
		expect(
			screen.queryByRole('button', {name: 'good-response'})
		).not.toBeInTheDocument();
		expect(
			screen.queryByRole('button', {name: 'report-bad-result'})
		).not.toBeInTheDocument();
	});

	describe('free-form categorization', () => {
		beforeEach(() => {
			mockClassify.mockReset();
			mockPostChat.mockClear();
			(Liferay.fire as jest.Mock).mockClear();
		});

		it('fires a single request event for a categorization message', async () => {
			const fakeEventSource = createFakeEventSource();

			mockCreateEventSource.mockResolvedValue(fakeEventSource as never);
			mockClassify.mockResolvedValue({
				actions: [{agent: 'tag', count: 3, targets: []}],
				passthrough: false,
			});

			await act(async () => {
				render(
					<AIAssistantChat
						{...defaultProps}
						enableFreeFormCategorization
						initialMessage="tag this article"
					/>
				);
			});

			await act(async () => {
				fakeEventSource.emit('Subscribe', 'ref-1');
			});

			expect(mockClassify).toHaveBeenCalledWith('tag this article');
			expect(Liferay.fire).toHaveBeenCalledWith(
				'cms:aiAssistant:requestCategorize',
				{actions: [{agent: 'tag', count: 3, targets: []}]}
			);
			expect(mockPostChat).not.toHaveBeenCalled();
		});

		it('posts a passthrough message to the chat', async () => {
			const fakeEventSource = createFakeEventSource();

			mockCreateEventSource.mockResolvedValue(fakeEventSource as never);
			mockClassify.mockResolvedValue({actions: [], passthrough: true});

			await act(async () => {
				render(
					<AIAssistantChat
						{...defaultProps}
						enableFreeFormCategorization
						initialMessage="what can you do?"
					/>
				);
			});

			await act(async () => {
				fakeEventSource.emit('Subscribe', 'ref-1');
			});

			expect(mockClassify).toHaveBeenCalledWith('what can you do?');
			expect(mockPostChat).toHaveBeenCalled();
			expect(Liferay.fire).not.toHaveBeenCalledWith(
				'cms:aiAssistant:requestCategorize',
				expect.anything()
			);
		});

		it('does not classify when the feature is disabled', async () => {
			const fakeEventSource = createFakeEventSource();

			mockCreateEventSource.mockResolvedValue(fakeEventSource as never);

			await act(async () => {
				render(
					<AIAssistantChat
						{...defaultProps}
						initialMessage="tag this article"
					/>
				);
			});

			await act(async () => {
				fakeEventSource.emit('Subscribe', 'ref-1');
			});

			expect(mockClassify).not.toHaveBeenCalled();
			expect(mockPostChat).toHaveBeenCalled();
		});

		it('renders only the balloon when the categorization event suppresses the user message', async () => {
			const handlers: Record<string, (payload: unknown) => void> = {};

			(Liferay.on as jest.Mock).mockImplementation(
				(name: string, callback: (payload: unknown) => void) => {
					handlers[name] = callback;
				}
			);

			await act(async () => {
				render(<AIAssistantChat {...defaultProps} />);
			});

			await act(async () => {
				handlers['cms:aiAssistant:categorize']({
					agent: 'L_GENERATE_TAGS',
					cmsGroupId: 1,
					content: 'x',
					scopeId: 1,
					suppressUserMessage: true,
					targets: ['kayaking'],
				});
			});

			expect(
				screen.getByText('categorization-balloon')
			).toBeInTheDocument();
			expect(screen.queryByText('generate-tags')).not.toBeInTheDocument();

			(Liferay.on as jest.Mock).mockReset();
		});
	});
});
