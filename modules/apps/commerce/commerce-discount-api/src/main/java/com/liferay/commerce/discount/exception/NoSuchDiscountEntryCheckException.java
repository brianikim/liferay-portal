/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.exception;

import com.liferay.portal.kernel.exception.NoSuchModelException;

/**
 * @author Marco Leo
 */
public class NoSuchDiscountEntryCheckException extends NoSuchModelException {

	public NoSuchDiscountEntryCheckException() {
	}

	public NoSuchDiscountEntryCheckException(String msg) {
		super(msg);
	}

	public NoSuchDiscountEntryCheckException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public NoSuchDiscountEntryCheckException(Throwable throwable) {
		super(throwable);
	}

}