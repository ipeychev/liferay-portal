/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.rest.internal.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Iliyan Peychev
 */
public class MessageUtil {

	public static String appendContext(String text, Map<String, ?> context) {
		if (text == null) {
			text = "";
		}

		if (MapUtil.isEmpty(context)) {
			return text;
		}

		StringBundler sb = new StringBundler();

		sb.append(text);
		sb.append("\n\n# Context\n");

		Map<String, ?> sortedContext = new TreeMap<>(context);

		for (Map.Entry<String, ?> entry : sortedContext.entrySet()) {
			if (Validator.isNull(entry.getValue())) {
				continue;
			}

			sb.append(entry.getKey());
			sb.append(": ");
			sb.append(entry.getValue());
			sb.append("\n");
		}

		return sb.toString();
	}

}