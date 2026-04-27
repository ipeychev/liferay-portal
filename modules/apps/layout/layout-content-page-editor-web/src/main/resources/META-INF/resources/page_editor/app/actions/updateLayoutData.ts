/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {LayoutData} from '../../types/layout_data/LayoutData';
import {FragmentEntryLinkMap} from './addFragmentEntryLinks';
import {UPDATE_LAYOUT_DATA} from './types';

export default function updateLayoutData({
	fragmentEntryLinks,
	layoutData,
}: {
	fragmentEntryLinks: FragmentEntryLinkMap;
	layoutData: LayoutData;
}) {
	return {
		fragmentEntryLinks,
		layoutData,
		type: UPDATE_LAYOUT_DATA,
	} as const;
}
