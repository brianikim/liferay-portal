/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.catalog.internal.util.v1_0;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryService;
import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Brian I. Kim
 */
public class AccountEntryUtil {

	public static long getAccountEntryId(
			AccountEntryService accountEntryService, Long accountEntryId)
		throws PortalException {

		if (accountEntryId == null) {
			return 0;
		}

		if (accountEntryId > 0) {
			AccountEntry accountEntry = accountEntryService.fetchAccountEntry(
				accountEntryId);

			if (accountEntry != null) {
				return accountEntry.getAccountEntryId();
			}
		}

		return accountEntryId;
	}

}