/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.workflow.node.delegate;

import com.liferay.ai.hub.workflow.node.ServiceNodeDelegate;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.io.Serializable;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Strips Markdown fences from an LLM response and repairs common JSON
 * malformations (truncated strings, trailing commas, missing commas, and
 * unbalanced braces or brackets).
 *
 * @author Mario Gomes
 * @author Iliyan Peychev
 */
@Component(
	property = "java.delegate=com.liferay.ai.hub.internal.workflow.node.delegate.JSONRepairServiceNodeDelegate#execute",
	service = ServiceNodeDelegate.class
)
public class JSONRepairServiceNodeDelegate implements ServiceNodeDelegate {

	@Override
	public String execute(
		Map<String, String> inputVariables,
		Map<String, Serializable> workflowContext) {

		if (inputVariables.isEmpty()) {
			return "";
		}

		Set<Map.Entry<String, String>> entrySet = inputVariables.entrySet();

		Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();

		Map.Entry<String, String> entry = iterator.next();

		return _repairJSON(_stripMarkdownFences(entry.getValue()));
	}

	private String _repairJSON(String json) {
		try {
			_jsonFactory.createJSONObject(json);

			return json;
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}

		StringBuilder fixedSB = new StringBuilder();

		boolean inString = false;
		char previousChar = 0;

		for (int i = 0; i < json.length(); i++) {
			char c = json.charAt(i);

			if (inString) {
				if ((c == '"') && (previousChar != '\\')) {
					inString = false;
				}
				else if ((c == '\n') || (c == '\r')) {
					fixedSB.append('"');

					inString = false;
				}
			}
			else if (c == '"') {
				inString = true;
			}

			fixedSB.append(c);

			previousChar = c;
		}

		if (inString) {
			fixedSB.append('"');
		}

		String repaired = fixedSB.toString();

		repaired = _trailingCommaPattern.matcher(
			repaired
		).replaceAll(
			"$1"
		);

		repaired = _missingCommaPattern.matcher(
			repaired
		).replaceAll(
			"$1,$2"
		);

		int braces = 0;
		int brackets = 0;

		inString = false;
		previousChar = 0;

		for (int i = 0; i < repaired.length(); i++) {
			char c = repaired.charAt(i);

			if (inString) {
				if ((c == '"') && (previousChar != '\\')) {
					inString = false;
				}
			}
			else if (c == '"') {
				inString = true;
			}
			else if (c == '{') {
				braces++;
			}
			else if (c == '}') {
				braces--;
			}
			else if (c == '[') {
				brackets++;
			}
			else if (c == ']') {
				brackets--;
			}

			previousChar = c;
		}

		StringBuilder repairedSB = new StringBuilder(repaired);

		while (brackets > 0) {
			repairedSB.append(']');

			brackets--;
		}

		while (braces > 0) {
			repairedSB.append('}');

			braces--;
		}

		repaired = repairedSB.toString();

		try {
			_jsonFactory.createJSONObject(repaired);

			if (_log.isWarnEnabled()) {
				_log.warn("Repaired malformed JSON");
			}

			return repaired;
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to repair malformed JSON", exception);
			}

			return json;
		}
	}

	private String _stripMarkdownFences(String text) {
		if (text == null) {
			return "";
		}

		text = text.trim();

		if (text.startsWith("```")) {
			int index = text.indexOf('\n');

			if (index != -1) {
				text = text.substring(index + 1);
			}
		}

		if (text.endsWith("```")) {
			text = text.substring(0, text.length() - 3);
		}

		return text.trim();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		JSONRepairServiceNodeDelegate.class);

	private static final Pattern _missingCommaPattern = Pattern.compile(
		"(\"[^\"]*\")\\s*\\n\\s*(\")");
	private static final Pattern _trailingCommaPattern = Pattern.compile(
		",\\s*([}\\]])");

	@Reference
	private JSONFactory _jsonFactory;

}