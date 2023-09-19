/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.internal.model.listener;

import com.liferay.commerce.discount.model.CommerceDiscount;
import com.liferay.commerce.discount.model.CommerceDiscountEntryCheck;
import com.liferay.commerce.discount.model.CommerceDiscountUsageEntryTable;
import com.liferay.commerce.discount.service.CommerceDiscountEntryCheckLocalService;
import com.liferay.commerce.discount.service.CommerceDiscountUsageEntryLocalService;
import com.liferay.petra.sql.dsl.DSLFunctionFactoryUtil;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserService;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Riccardo Alberti
 */
@Component(service = ModelListener.class)
public class CommerceDiscountModelListener
	extends BaseModelListener<CommerceDiscount> {

	@Override
	public void onAfterUpdate(
		CommerceDiscount originalCommerceDiscount,
		CommerceDiscount commerceDiscount) {

		if ((originalCommerceDiscount.getLimitationTimes() ==
				commerceDiscount.getLimitationTimes()) &&
			(originalCommerceDiscount.getLimitationTimesPerAccount() ==
				commerceDiscount.getLimitationTimesPerAccount())) {

			return;
		}

		try {
			int commerceDiscountUsageEntriesTotalCount =
				_commerceDiscountUsageEntryLocalService.
					getCommerceDiscountUsageEntriesCount(
						commerceDiscount.getCommerceDiscountId());

			if ((commerceDiscount.getLimitationTimes() > 0) &&
				(commerceDiscountUsageEntriesTotalCount >=
					commerceDiscount.getLimitationTimes())) {

				_addCommerceDiscountEntryCheck(
					0, commerceDiscount.getCommerceDiscountId());
			}
			else {
				_commerceDiscountEntryCheckLocalService.
					deleteCommerceDiscountEntryChecks(
						0, commerceDiscount.getCommerceDiscountId());
			}

			List<Object[]> commerceAccountIdCounts =
				_getCommerceAccountIdCounts(
					commerceDiscount.getCommerceDiscountId());

			for (Object[] commerceAccountIdCount : commerceAccountIdCounts) {
				long commerceAccountId = (long)commerceAccountIdCount[0];
				long count = (long)commerceAccountIdCount[1];

				if ((commerceDiscount.getLimitationTimesPerAccount() > 0) &&
					(count >=
						commerceDiscount.getLimitationTimesPerAccount())) {

					_addCommerceDiscountEntryCheck(
						commerceAccountId,
						commerceDiscount.getCommerceDiscountId());
				}
				else {
					_commerceDiscountEntryCheckLocalService.
						deleteCommerceDiscountEntryChecks(
							commerceAccountId,
							commerceDiscount.getCommerceDiscountId());
				}
			}
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}
	}

	private void _addCommerceDiscountEntryCheck(
			long commerceAccountId, long commerceDiscountId)
		throws PortalException {

		CommerceDiscountEntryCheck commerceDiscountEntryCheck =
			_commerceDiscountEntryCheckLocalService.
				fetchCommerceDiscountEntryCheck(
					commerceAccountId, commerceDiscountId);

		if (commerceDiscountEntryCheck == null) {
			User currentUser = _userService.getCurrentUser();

			_commerceDiscountEntryCheckLocalService.
				addCommerceDiscountEntryCheck(
					currentUser.getUserId(), commerceAccountId,
					commerceDiscountId);
		}
	}

	private List<Object[]> _getCommerceAccountIdCounts(
		long commerceDiscountId) {

		return _commerceDiscountUsageEntryLocalService.dslQuery(
			DSLQueryFactoryUtil.select(
				CommerceDiscountUsageEntryTable.INSTANCE.commerceAccountId,
				DSLFunctionFactoryUtil.count(
					CommerceDiscountUsageEntryTable.INSTANCE.
						commerceDiscountUsageEntryId
				).as(
					"COUNT"
				)
			).from(
				CommerceDiscountUsageEntryTable.INSTANCE
			).where(
				CommerceDiscountUsageEntryTable.INSTANCE.commerceDiscountId.eq(
					commerceDiscountId)
			).groupBy(
				CommerceDiscountUsageEntryTable.INSTANCE.commerceAccountId
			));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceDiscountModelListener.class);

	@Reference
	private CommerceDiscountEntryCheckLocalService
		_commerceDiscountEntryCheckLocalService;

	@Reference
	private CommerceDiscountUsageEntryLocalService
		_commerceDiscountUsageEntryLocalService;

	@Reference
	private UserService _userService;

}