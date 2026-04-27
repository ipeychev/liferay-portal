/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {LayoutData} from '../../../types/layout_data/LayoutData';
import {State} from '../../../types/State';
import {FragmentEntryLinkMap} from '../../actions/addFragmentEntryLinks';
import updateLayoutDataAction from '../../actions/updateLayoutData';

type Props = {
	action: ReturnType<typeof updateLayoutDataAction> & {
		previousFragmentEntryLinks: FragmentEntryLinkMap;
		previousLayoutData: LayoutData;
	};
};

function undoAction({action}: Props) {
	return (
		dispatch: (action: ReturnType<typeof updateLayoutDataAction>) => void
	) =>
		dispatch(
			updateLayoutDataAction({
				fragmentEntryLinks: action.previousFragmentEntryLinks,
				layoutData: action.previousLayoutData,
			})
		);
}

function getDerivedStateForUndo({state}: {state: State}) {
	return {
		previousFragmentEntryLinks: state.fragmentEntryLinks,
		previousLayoutData: state.layoutData,
	};
}

export {getDerivedStateForUndo, undoAction};
