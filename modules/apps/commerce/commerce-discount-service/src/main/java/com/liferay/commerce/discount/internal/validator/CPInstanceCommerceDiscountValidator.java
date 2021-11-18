/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.discount.internal.validator;

import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.model.CommerceDiscountRel;
import com.liferay.commerce.discount.service.CommerceDiscountRelService;
import com.liferay.commerce.discount.validator.CommerceDiscountValidator;
import com.liferay.commerce.discount.validator.CommerceDiscountValidatorResult;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.portal.kernel.exception.PortalException;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(
	enabled = false, immediate = true,
	property = {
		"commerce.discount.validator.key=" + CPInstanceCommerceDiscountValidator.KEY,
		"commerce.discount.validator.priority:Integer=40",
		"commerce.discount.validator.type=" + CommerceDiscountConstants.VALIDATOR_TYPE_PRE_QUALIFICATION
	},
	service = CommerceDiscountValidator.class
)
public class CPInstanceCommerceDiscountValidator
	implements CommerceDiscountValidator {

	public static final String KEY = "sku";

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public CommerceDiscountValidatorResult validate(
			CommerceContext commerceContext, CommerceDiscount commerceDiscount)
		throws PortalException {

		CommerceOrder commerceOrder = commerceContext.getCommerceOrder();

		List<CommerceOrderItem> commerceOrderItems =
			commerceOrder.getCommerceOrderItems();

		for (CommerceOrderItem commerceOrderItem : commerceOrderItems) {
			CommerceDiscountRel commerceDiscountRel =
				_commerceDiscountRelService.fetchCommerceDiscountRel(
					CPDefinition.class.getName(),
					commerceOrderItem.getCPDefinitionId());

			if (commerceDiscountRel == null) {
				return new CommerceDiscountValidatorResult(
					commerceDiscount.getCommerceDiscountId(), false,
					"the-discount-is-not-valid");
			}
		}

		return new CommerceDiscountValidatorResult(true);
	}

	@Reference
	private CommerceDiscountRelService _commerceDiscountRelService;

}