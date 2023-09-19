/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.internal.messaging;

import com.liferay.commerce.discount.constants.CommerceDiscountConstants;
import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.service.CommerceDiscountEntryCheckLocalService;
import com.liferay.commerce.discount.service.CommerceDiscountLocalService;
import com.liferay.commerce.discount.service.CommerceDiscountUsageEntryLocalService;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.messaging.BaseMessageListener;
import com.liferay.portal.kernel.messaging.DestinationNames;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageListener;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserService;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Riccardo Alberti
 */
@Component(
	property = "destination.name=" + DestinationNames.COMMERCE_DISCOUNT_ENTRY_CHECK,
	service = MessageListener.class
)
public class CommerceDiscountEntryCheckMessageListener
	extends BaseMessageListener {

	@Override
	protected void doReceive(Message message) throws Exception {
		JSONObject jsonObject = (JSONObject)message.getPayload();

		long commerceDiscountId = jsonObject.getLong("commerceDiscountId");

		CommerceDiscount commerceDiscount =
			_commerceDiscountLocalService.getCommerceDiscount(
				commerceDiscountId);

		if (Objects.equals(
				commerceDiscount.getLimitationType(),
				CommerceDiscountConstants.LIMITATION_TYPE_UNLIMITED)) {

			return;
		}

		int limitationTimes = commerceDiscount.getLimitationTimes();

		if (Objects.equals(
				commerceDiscount.getLimitationType(),
				CommerceDiscountConstants.LIMITATION_TYPE_LIMITED)) {

			int commerceDiscountUsageEntriesCount =
				_commerceDiscountUsageEntryLocalService.
					getCommerceDiscountUsageEntriesCount(commerceDiscountId);

			if (commerceDiscountUsageEntriesCount >= limitationTimes) {
				_addCommerceDiscountEntryCheck(0, commerceDiscountId);
			}

			return;
		}

		long commerceAccountId = jsonObject.getLong("commerceAccountId");

		int limitationTimesPerAccount =
			commerceDiscount.getLimitationTimesPerAccount();

		if (Objects.equals(
				commerceDiscount.getLimitationType(),
				CommerceDiscountConstants.
					LIMITATION_TYPE_LIMITED_FOR_ACCOUNTS)) {

			int commerceDiscountUsageEntriesCount =
				_commerceDiscountUsageEntryLocalService.
					getCommerceDiscountUsageEntriesCountByAccountId(
						commerceAccountId, commerceDiscountId);

			if (commerceDiscountUsageEntriesCount >=
					limitationTimesPerAccount) {

				_addCommerceDiscountEntryCheck(
					commerceAccountId, commerceDiscountId);
			}

			return;
		}

		int commerceDiscountUsageEntriesTotalCount =
			_commerceDiscountUsageEntryLocalService.
				getCommerceDiscountUsageEntriesCount(commerceDiscountId);

		if (commerceDiscountUsageEntriesTotalCount >= limitationTimes) {
			_addCommerceDiscountEntryCheck(0, commerceDiscountId);
		}

		int commerceDiscountUsageEntriesUserCount =
			_commerceDiscountUsageEntryLocalService.
				getCommerceDiscountUsageEntriesCountByAccountId(
					commerceAccountId, commerceDiscountId);

		if (commerceDiscountUsageEntriesUserCount >=
				limitationTimesPerAccount) {

			_addCommerceDiscountEntryCheck(
				commerceAccountId, commerceDiscountId);
		}
	}

	private void _addCommerceDiscountEntryCheck(
			long commerceAccountId, long commerceDiscountId)
		throws Exception {

		User currentUser = _userService.getCurrentUser();

		_commerceDiscountEntryCheckLocalService.addCommerceDiscountEntryCheck(
			currentUser.getUserId(), commerceAccountId, commerceDiscountId);
	}

	@Reference
	private CommerceDiscountEntryCheckLocalService
		_commerceDiscountEntryCheckLocalService;

	@Reference
	private CommerceDiscountLocalService _commerceDiscountLocalService;

	@Reference
	private CommerceDiscountUsageEntryLocalService
		_commerceDiscountUsageEntryLocalService;

	@Reference
	private UserService _userService;

}