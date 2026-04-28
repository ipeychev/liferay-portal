/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export interface Run {
	committedAt?: string;
	failureReason?: string;
	id: number;
	name?: string;
	prompt?: string;
	resultingSiteERC?: string;
	runStatus?: {key?: string};
	targetLanguages?: string;
}
