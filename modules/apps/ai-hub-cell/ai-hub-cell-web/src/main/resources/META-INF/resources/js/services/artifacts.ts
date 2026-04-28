/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {fetch as liferayFetch} from 'frontend-js-web';

import {Artifact} from '../types/Artifact';
import {RUNS_URL} from './runs';

interface GetArtifactsOptions {
	pageSize?: number;
	sort?: string;
}

export async function getArtifacts(
	runId: number,
	{pageSize = 100, sort}: GetArtifactsOptions = {}
): Promise<Artifact[]> {
	const params = new URLSearchParams({pageSize: String(pageSize)});

	if (sort) {
		params.set('sort', sort);
	}

	const response = await liferayFetch(
		`${RUNS_URL}/${runId}/artifacts?${params.toString()}`
	);

	if (!response.ok) {
		return [];
	}

	const json = await response.json();

	return json.items ?? [];
}
