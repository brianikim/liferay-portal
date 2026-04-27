/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.pricing.web.internal.util;

import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.util.CommercePriceFormatter;
import com.liferay.commerce.pricing.configuration.CommercePricingConfiguration;
import com.liferay.commerce.pricing.constants.CommercePricingConstants;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.settings.SystemSettingsLocator;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * @author Riccardo Alberti
 */
public class CommercePricingUtil {

	public static String getPricingEngineVersion(
			ConfigurationProvider configurationProvider)
		throws ConfigurationException {

		CommercePricingConfiguration commercePricingConfiguration =
			configurationProvider.getConfiguration(
				CommercePricingConfiguration.class,
				new SystemSettingsLocator(
					CommercePricingConstants.SERVICE_NAME));

		return commercePricingConfiguration.commercePricingCalculationKey();
	}

	public static String getFormattedValue(CommerceCurrency commerceCurrency, CommercePriceFormatter commercePriceFormatter, boolean includeSymbol, Locale locale, BigDecimal value)
		throws PortalException {

		String formattedValue = commercePriceFormatter.format(
			commerceCurrency, value, locale);

		if (includeSymbol) {
			String symbol = commerceCurrency.getSymbol();

			if (Validator.isNotNull(symbol) &&
				formattedValue.contains(symbol)) {

				formattedValue = StringUtil.replace(
					formattedValue, symbol, StringPool.BLANK);

				formattedValue = StringUtil.trim(formattedValue);
			}
		}

		return formattedValue;
	}
}