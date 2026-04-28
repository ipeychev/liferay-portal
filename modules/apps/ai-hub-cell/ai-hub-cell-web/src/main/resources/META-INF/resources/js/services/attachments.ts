/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {fetch as liferayFetch} from 'frontend-js-web';

import {Attachment} from '../types/Attachment';
import {RUNS_URL} from './runs';

export async function getAttachments(runId: number): Promise<Attachment[]> {
	const response = await liferayFetch(
		`${RUNS_URL}/${runId}/attachments?pageSize=100`
	);

	if (!response.ok) {
		return [];
	}

	const json = await response.json();

	return json.items ?? [];
}
