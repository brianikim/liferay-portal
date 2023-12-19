/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.util;

import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.ContentField;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.ContentFieldValue;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import java.util.Locale;

/**
 * @author Brian I. Kim
 */
public class ContentFieldUtil {

	public static ContentField toContentField(
		DDMFormFieldValue ddmFormFieldValue,
		DTOConverterContext dtoConverterContext) {

		DDMFormField ddmFormField = ddmFormFieldValue.getDDMFormField();

		if (ddmFormField == null) {
			return null;
		}

		LocalizedValue localizedValue = ddmFormField.getLabel();

		return new ContentField() {
			{
				contentFieldValue = _toContentFieldValue(
					dtoConverterContext.getLocale(),
					ddmFormFieldValue.getValue());
				dataType = ddmFormField.getDataType();
				label = localizedValue.getString(
					dtoConverterContext.getLocale());
			}
		};
	}

	private static ContentFieldValue _getContentFieldValue(String valueString) {
		return new ContentFieldValue() {
			{
				data = valueString;
			}
		};
	}

	private static ContentFieldValue _toContentFieldValue(
		Locale locale, Value value) {

		if (value == null) {
			return new ContentFieldValue();
		}

		String valueString = String.valueOf(value.getString(locale));

		return _getContentFieldValue(valueString);
	}

}