/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.kaleo.runtime.node.util;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Feliphe Marinho
 */
public class ToolsConfigUtil {

	public static List<String> getValues(
		JSONFactory jsonFactory, Map<String, String> kaleoNodeSettingValues,
		String propertyName) {

		List<String> values = new ArrayList<>();

		try {
			JSONArray jsonArray = jsonFactory.createJSONArray(
				kaleoNodeSettingValues.get("tools"));

			for (JSONObject jsonObject : (Iterable<JSONObject>)jsonArray) {
				String value = jsonObject.getString(propertyName);

				if (Validator.isNotNull(value)) {
					values.add(value);
				}
			}
		}
		catch (JSONException jsonException) {
			if (_log.isDebugEnabled()) {
				_log.debug(jsonException);
			}
		}

		return values;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ToolsConfigUtil.class);

}