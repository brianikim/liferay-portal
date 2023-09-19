/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.service.impl;

import com.liferay.commerce.discount.model.CommerceDiscountEntryCheck;
import com.liferay.commerce.discount.model.CommerceDiscountEntryCheckTable;
import com.liferay.commerce.discount.service.base.CommerceDiscountEntryCheckLocalServiceBaseImpl;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 */
@Component(
	property = "model.class.name=com.liferay.commerce.discount.model.CommerceDiscountEntryCheck",
	service = AopService.class
)
public class CommerceDiscountEntryCheckLocalServiceImpl
	extends CommerceDiscountEntryCheckLocalServiceBaseImpl {

	@Override
	public CommerceDiscountEntryCheck addCommerceDiscountEntryCheck(
			long userId, long commerceAccountId, long commerceDiscountId)
		throws PortalException {

		User user = _userLocalService.getUser(userId);

		long commerceDiscountUsageEntryId = counterLocalService.increment();

		CommerceDiscountEntryCheck commerceDiscountEntryCheck =
			commerceDiscountEntryCheckPersistence.create(
				commerceDiscountUsageEntryId);

		commerceDiscountEntryCheck.setCompanyId(user.getCompanyId());
		commerceDiscountEntryCheck.setUserId(userId);
		commerceDiscountEntryCheck.setUserName(user.getFullName());
		commerceDiscountEntryCheck.setCommerceAccountId(commerceAccountId);
		commerceDiscountEntryCheck.setCommerceDiscountId(commerceDiscountId);

		return commerceDiscountEntryCheckPersistence.update(
			commerceDiscountEntryCheck);
	}

	@Override
	public void deleteCommerceDiscountEntryChecks(long commerceDiscountId)
		throws PortalException {

		commerceDiscountEntryCheckPersistence.removeByCommerceDiscountId(
			commerceDiscountId);
	}

	@Override
	public void deleteCommerceDiscountEntryChecks(
			long commerceAccountId, long commerceDiscountId)
		throws PortalException {

		commerceDiscountEntryCheckPersistence.removeByCAI_CDI(
			commerceAccountId, commerceDiscountId);
	}

	@Override
	public CommerceDiscountEntryCheck fetchCommerceDiscountEntryCheck(
		long commerceAccountId, long commerceDiscountId) {

		return commerceDiscountEntryCheckPersistence.fetchByCAI_CDI_First(
			commerceAccountId, commerceDiscountId, null);
	}

	@Override
	public CommerceDiscountEntryCheck getCommerceDiscountEntryCheck(
			long commerceAccountId, long commerceDiscountId)
		throws PortalException {

		List<CommerceDiscountEntryCheck> commerceDiscountEntryChecks =
			commerceDiscountEntryCheckPersistence.dslQuery(
				DSLQueryFactoryUtil.select(
					CommerceDiscountEntryCheckTable.INSTANCE
				).from(
					CommerceDiscountEntryCheckTable.INSTANCE
				).where(
					CommerceDiscountEntryCheckTable.INSTANCE.commerceDiscountId.
						eq(
							commerceDiscountId
						).and(
							Predicate.withParentheses(
								Predicate.or(
									CommerceDiscountEntryCheckTable.INSTANCE.
										commerceAccountId.eq(commerceAccountId),
									CommerceDiscountEntryCheckTable.INSTANCE.
										commerceAccountId.eq(0L)))
						)
				).limit(
					0, 1
				));

		if (commerceDiscountEntryChecks.isEmpty()) {
			return null;
		}

		return commerceDiscountEntryChecks.get(0);
	}

	@Reference
	private UserLocalService _userLocalService;

}