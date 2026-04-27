/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {State} from '../../types/State';
import updateLayoutDataAction from '../actions/updateLayoutData';
import updateNetwork from '../actions/updateNetwork';
import LayoutService from '../services/LayoutService';

export default function updateLayoutData() {
	return (
		dispatch: (
			action: ReturnType<
				typeof updateLayoutDataAction | typeof updateNetwork
			>
		) => void,
		getState: () => State
	) => {
		const {segmentsExperienceId} = getState();

		return LayoutService.getLayoutData({
			onNetworkStatus: dispatch,
			segmentsExperienceId,
		}).then(({fragmentEntryLinks, layoutData}) =>
			dispatch(updateLayoutDataAction({fragmentEntryLinks, layoutData}))
		);
	};
}
