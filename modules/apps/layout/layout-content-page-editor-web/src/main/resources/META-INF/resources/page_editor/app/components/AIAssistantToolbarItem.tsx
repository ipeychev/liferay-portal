/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	AIAssistantChat,
	ChatContext,
} from '@liferay/ai-hub-cell-js-components-web';
import React from 'react';

import {config} from '../config/index';
import {useDispatch} from '../contexts/StoreContext';
import updateLayoutData from '../thunks/updateLayoutData';

export default function AIAssistantToolbarItem() {
	const {layoutExternalReferenceCode, siteExternalReferenceCode} = config;

	const dispatch = useDispatch();

	function getContext(): ChatContext {
		return {
			context: {
				siteExternalReferenceCode,
				sitePageExternalReferenceCode: layoutExternalReferenceCode,
			},
			instructionDefinitionScope: 'pageEditor',
		};
	}

	function handleExternalEvent(type: string) {
		if (type === 'pageUpdater') {
			dispatch(updateLayoutData());
		}
	}

	return (
		<AIAssistantChat
			compact
			externalEventTypes={['pageUpdater']}
			getContext={getContext}
			onExternalEvent={handleExternalEvent}
		/>
	);
}
