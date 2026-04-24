/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.content.composer.rest.client.serdes.v1_0;

import com.liferay.content.composer.rest.client.dto.v1_0.Composition;
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
public class CompositionSerDes {

	public static Composition toDTO(String json) {
		CompositionJSONParser compositionJSONParser =
			new CompositionJSONParser();

		return compositionJSONParser.parseToDTO(json);
	}

	public static Composition[] toDTOs(String json) {
		CompositionJSONParser compositionJSONParser =
			new CompositionJSONParser();

		return compositionJSONParser.parseToDTOs(json);
	}

	public static String toJSON(Composition composition) {
		if (composition == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (composition.getBrief() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"brief\": ");

			sb.append("\"");

			sb.append(_escape(composition.getBrief()));

			sb.append("\"");
		}

		if (composition.getChatExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"chatExternalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(composition.getChatExternalReferenceCode()));

			sb.append("\"");
		}

		if (composition.getExternalReferenceCode() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"externalReferenceCode\": ");

			sb.append("\"");

			sb.append(_escape(composition.getExternalReferenceCode()));

			sb.append("\"");
		}

		if (composition.getId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"id\": ");

			sb.append(composition.getId());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		CompositionJSONParser compositionJSONParser =
			new CompositionJSONParser();

		return compositionJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(Composition composition) {
		if (composition == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (composition.getBrief() == null) {
			map.put("brief", null);
		}
		else {
			map.put("brief", String.valueOf(composition.getBrief()));
		}

		if (composition.getChatExternalReferenceCode() == null) {
			map.put("chatExternalReferenceCode", null);
		}
		else {
			map.put(
				"chatExternalReferenceCode",
				String.valueOf(composition.getChatExternalReferenceCode()));
		}

		if (composition.getExternalReferenceCode() == null) {
			map.put("externalReferenceCode", null);
		}
		else {
			map.put(
				"externalReferenceCode",
				String.valueOf(composition.getExternalReferenceCode()));
		}

		if (composition.getId() == null) {
			map.put("id", null);
		}
		else {
			map.put("id", String.valueOf(composition.getId()));
		}

		return map;
	}

	public static class CompositionJSONParser
		extends BaseJSONParser<Composition> {

		@Override
		protected Composition createDTO() {
			return new Composition();
		}

		@Override
		protected Composition[] createDTOArray(int size) {
			return new Composition[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "brief")) {
				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "chatExternalReferenceCode")) {

				return false;
			}
			else if (Objects.equals(
						jsonParserFieldName, "externalReferenceCode")) {

				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "id")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			Composition composition, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "brief")) {
				if (jsonParserFieldValue != null) {
					composition.setBrief((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "chatExternalReferenceCode")) {

				if (jsonParserFieldValue != null) {
					composition.setChatExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "externalReferenceCode")) {

				if (jsonParserFieldValue != null) {
					composition.setExternalReferenceCode(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "id")) {
				if (jsonParserFieldValue != null) {
					composition.setId(
						Long.valueOf((String)jsonParserFieldValue));
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
// LIFERAY-REST-BUILDER-HASH:670569755