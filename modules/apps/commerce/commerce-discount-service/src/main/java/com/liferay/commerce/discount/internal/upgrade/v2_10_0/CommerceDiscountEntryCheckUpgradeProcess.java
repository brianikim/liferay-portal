/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.internal.upgrade.v2_10_0;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Riccardo Alberti
 */
public class CommerceDiscountEntryCheckUpgradeProcess extends UpgradeProcess {

	public CommerceDiscountEntryCheckUpgradeProcess(
		UserLocalService userLocalService) {

		_userLocalService = userLocalService;
	}

	@Override
	protected void doUpgrade() throws Exception {
		String insertCommerceDiscountEntryCheckSQL = StringBundler.concat(
			"insert into CommerceDiscountEntryCheck (",
			"commerceDiscountEntryCheckId, companyId, userId, userName, ",
			"createDate, modifiedDate, commerceAccountId, commerceDiscountId) ",
			"values (?, ?, ?, ?, ?, ?, ?, ?)");

		try (PreparedStatement preparedStatement1 =
				AutoBatchPreparedStatementUtil.concurrentAutoBatch(
					connection, insertCommerceDiscountEntryCheckSQL);
			PreparedStatement preparedStatement2 = connection.prepareStatement(
				StringBundler.concat(
					"select CommerceDiscountUsageEntry.companyId, ",
					"CommerceDiscountUsageEntry.commerceDiscountId, ",
					"CommerceDiscount.limitationTimes, count(*) from ",
					"CommerceDiscountUsageEntry join CommerceDiscount on ",
					"CommerceDiscountUsageEntry.commerceDiscountId = ",
					"CommerceDiscount.commerceDiscountId group by ",
					"CommerceDiscountUsageEntry.companyId, ",
					"CommerceDiscountUsageEntry.commerceDiscountId, ",
					"CommerceDiscount.limitationTimes"));
			ResultSet resultSet1 = preparedStatement2.executeQuery();
			PreparedStatement preparedStatement3 = connection.prepareStatement(
				StringBundler.concat(
					"select CommerceDiscountUsageEntry.companyId, ",
					"CommerceDiscountUsageEntry.commerceAccountId, ",
					"CommerceDiscountUsageEntry.commerceDiscountId, ",
					"CommerceDiscount.limitationTimesPerAccount, count(*) ",
					"from CommerceDiscountUsageEntry join CommerceDiscount on ",
					"CommerceDiscountUsageEntry.commerceDiscountId = ",
					"CommerceDiscount.commerceDiscountId group by ",
					"CommerceDiscountUsageEntry.companyId, ",
					"CommerceDiscountUsageEntry.commerceAccountId, ",
					"CommerceDiscountUsageEntry.commerceDiscountId, ",
					"CommerceDiscount.limitationTimesPerAccount"));
			ResultSet resultSet2 = preparedStatement3.executeQuery()) {

			while (resultSet1.next()) {
				int limitationTimes = resultSet1.getInt(3);
				int commerceDiscountUsageEntryCount = resultSet1.getInt(4);

				if ((limitationTimes > 0) &&
					(commerceDiscountUsageEntryCount >= limitationTimes)) {

					long companyId = resultSet1.getLong(1);
					long commerceDiscountId = resultSet1.getLong(2);

					_addBatch(
						companyId, 0, commerceDiscountId, preparedStatement1);
				}
			}

			while (resultSet2.next()) {
				int limitationTimesPerAccount = resultSet2.getInt(4);
				int commerceDiscountUsageEntryCount = resultSet2.getInt(5);

				if ((limitationTimesPerAccount > 0) &&
					(commerceDiscountUsageEntryCount >=
						limitationTimesPerAccount)) {

					long companyId = resultSet2.getLong(1);
					long commerceAccountId = resultSet2.getLong(2);
					long commerceDiscountId = resultSet2.getLong(3);

					_addBatch(
						companyId, commerceAccountId, commerceDiscountId,
						preparedStatement1);
				}
			}

			preparedStatement1.executeBatch();
		}
	}

	private void _addBatch(
			long companyId, long commerceAccountId, long commerceDiscountId,
			PreparedStatement preparedStatement)
		throws Exception {

		User user = _userLocalService.getGuestUser(companyId);

		Date date = new Date(System.currentTimeMillis());

		preparedStatement.setLong(1, increment());
		preparedStatement.setLong(2, companyId);
		preparedStatement.setLong(3, user.getUserId());
		preparedStatement.setString(4, user.getFullName());
		preparedStatement.setDate(5, date);
		preparedStatement.setDate(6, date);
		preparedStatement.setLong(7, commerceAccountId);
		preparedStatement.setLong(8, commerceDiscountId);

		preparedStatement.addBatch();
	}

	private final UserLocalService _userLocalService;

}