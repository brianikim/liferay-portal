/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.catalog.client.serdes.v1_0;

import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.ContentFieldValue;
import com.liferay.headless.commerce.delivery.catalog.client.json.BaseJSONParser;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Generated;

/**
 * @author Andrea Sbarra
 * @generated
 */
@Generated("")
public class ContentFieldValueSerDes {

	public static ContentFieldValue toDTO(String json) {
		ContentFieldValueJSONParser contentFieldValueJSONParser =
			new ContentFieldValueJSONParser();

		return contentFieldValueJSONParser.parseToDTO(json);
	}

	public static ContentFieldValue[] toDTOs(String json) {
		ContentFieldValueJSONParser contentFieldValueJSONParser =
			new ContentFieldValueJSONParser();

		return contentFieldValueJSONParser.parseToDTOs(json);
	}

	public static String toJSON(ContentFieldValue contentFieldValue) {
		if (contentFieldValue == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (contentFieldValue.getData() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"data\": ");

			sb.append("\"");

			sb.append(_escape(contentFieldValue.getData()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ContentFieldValueJSONParser contentFieldValueJSONParser =
			new ContentFieldValueJSONParser();

		return contentFieldValueJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		ContentFieldValue contentFieldValue) {

		if (contentFieldValue == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (contentFieldValue.getData() == null) {
			map.put("data", null);
		}
		else {
			map.put("data", String.valueOf(contentFieldValue.getData()));
		}

		return map;
	}

	public static class ContentFieldValueJSONParser
		extends BaseJSONParser<ContentFieldValue> {

		@Override
		protected ContentFieldValue createDTO() {
			return new ContentFieldValue();
		}

		@Override
		protected ContentFieldValue[] createDTOArray(int size) {
			return new ContentFieldValue[size];
		}

		@Override
		protected void setField(
			ContentFieldValue contentFieldValue, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "data")) {
				if (jsonParserFieldValue != null) {
					contentFieldValue.setData((String)jsonParserFieldValue);
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

			Class<?> valueClass = value.getClass();

			if (value instanceof Map) {
				sb.append(_toJSON((Map)value));
			}
			else if (valueClass.isArray()) {
				Object[] values = (Object[])value;

				sb.append("[");

				for (int i = 0; i < values.length; i++) {
					sb.append("\"");
					sb.append(_escape(values[i]));
					sb.append("\"");

					if ((i + 1) < values.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(entry.getValue()));
				sb.append("\"");
			}
			else {
				sb.append(String.valueOf(entry.getValue()));
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

}