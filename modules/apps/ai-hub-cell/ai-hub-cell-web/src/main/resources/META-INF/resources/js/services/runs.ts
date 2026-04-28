/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {fetch as liferayFetch} from 'frontend-js-web';

import {Run} from '../types/Run';

export const RUNS_URL = '/o/content-site-generator/runs';

interface CreateRunInput {
	name: string;
	prompt: string;
	runStatus?: string;
}

export async function createRun(input: CreateRunInput): Promise<Run> {
	const response = await liferayFetch(RUNS_URL, {
		body: JSON.stringify(input),
		headers: {'Content-Type': 'application/json'},
		method: 'POST',
	});

	if (!response.ok) {
		throw new Error(`Failed to create run (${response.status})`);
	}

	return response.json();
}

export async function getRun(runId: number): Promise<Run> {
	const response = await liferayFetch(`${RUNS_URL}/${runId}`);

	if (!response.ok) {
		throw new Error(`Failed to load run ${runId} (${response.status})`);
	}

	return response.json();
}

export async function deleteRun(runId: number): Promise<void> {
	await liferayFetch(`${RUNS_URL}/${runId}`, {method: 'DELETE'});
}

export async function analyzeRun(runId: number): Promise<void> {
	const response = await liferayFetch(
		`${RUNS_URL}/${runId}/object-actions/analyze`,
		{
			headers: {'Content-Type': 'application/json'},
			method: 'PUT',
		}
	);

	if (!response.ok) {
		throw new Error(`Failed to start analysis (${response.status})`);
	}
}

export async function commitRun(runId: number): Promise<void> {
	const response = await liferayFetch(
		`${RUNS_URL}/${runId}/object-actions/commit`,
		{
			headers: {'Content-Type': 'application/json'},
			method: 'PUT',
		}
	);

	if (!response.ok) {
		throw new Error(`Failed to start generation (${response.status})`);
	}
}
