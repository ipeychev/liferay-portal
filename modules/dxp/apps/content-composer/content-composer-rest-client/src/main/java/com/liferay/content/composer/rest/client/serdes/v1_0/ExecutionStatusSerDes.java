/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.composer.rest.client.serdes.v1_0;

import com.liferay.content.composer.rest.client.dto.v1_0.ExecutionStatus;
import com.liferay.content.composer.rest.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Feliphe Marinho
 * @generated
 */
@Generated("")
public class ExecutionStatusSerDes {

	public static ExecutionStatus toDTO(String json) {
		ExecutionStatusJSONParser executionStatusJSONParser =
			new ExecutionStatusJSONParser();

		return executionStatusJSONParser.parseToDTO(json);
	}

	public static ExecutionStatus[] toDTOs(String json) {
		ExecutionStatusJSONParser executionStatusJSONParser =
			new ExecutionStatusJSONParser();

		return executionStatusJSONParser.parseToDTOs(json);
	}

	public static String toJSON(ExecutionStatus executionStatus) {
		if (executionStatus == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (executionStatus.getExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"externalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(executionStatus.getExternalReferenceCode()));

			sb.append("\"");
		}

		if (executionStatus.getStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"status\": ");

			sb.append("\"");

			sb.append(_escape(executionStatus.getStatus()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ExecutionStatusJSONParser executionStatusJSONParser =
			new ExecutionStatusJSONParser();

		return executionStatusJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(ExecutionStatus executionStatus) {
		if (executionStatus == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (executionStatus.getExternalReferenceCode() == null) {
			map.put("externalReferenceCode", null);
		}
		else {
			map.put(
				"externalReferenceCode",
				String.valueOf(executionStatus.getExternalReferenceCode()));
		}

		if (executionStatus.getStatus() == null) {
			map.put("status", null);
		}
		else {
			map.put("status", String.valueOf(executionStatus.getStatus()));
		}

		return map;
	}

	public static class ExecutionStatusJSONParser
		extends BaseJSONParser<ExecutionStatus> {

		@Override
		protected ExecutionStatus createDTO() {
			return new ExecutionStatus();
		}

		@Override
		protected ExecutionStatus[] createDTOArray(int size) {
			return new ExecutionStatus[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "externalReferenceCode")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "status")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			ExecutionStatus executionStatus, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "externalReferenceCode")) {
				if (jsonParserFieldValue != null) {
					executionStatus.setExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "status")) {
				if (jsonParserFieldValue != null) {
					executionStatus.setStatus((String)jsonParserFieldValue);
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}
// LIFERAY-REST-BUILDER-HASH:328509500