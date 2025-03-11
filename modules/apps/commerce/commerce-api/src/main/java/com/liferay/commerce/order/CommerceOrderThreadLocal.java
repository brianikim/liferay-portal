/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.order;

import com.liferay.petra.lang.CentralizedThreadLocal;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Brian I. Kim
 * @author Crescenzo Rega
 */
public class CommerceOrderThreadLocal {

	public static HttpServletRequest getHttpServletRequest() {
		return _httpServletRequest.get();
	}

	public static boolean isDeleteInProcess() {
		return _deleteInProcess.get();
	}

	public static boolean isSkipValidateAccountLimit() {
		return _skipValidateAccountLimit.get();
	}

	public static void setDeleteInProcess(boolean deleteInProcess) {
		_deleteInProcess.set(deleteInProcess);
	}

	public static void setHttpServletRequest(
		HttpServletRequest httpServletRequest) {

		_httpServletRequest.set(httpServletRequest);
	}

	public static void setSkipValidateAccountLimit(
		boolean skipValidateAccountLimit) {

		_skipValidateAccountLimit.set(skipValidateAccountLimit);
	}

	private static final ThreadLocal<Boolean> _deleteInProcess =
		new CentralizedThreadLocal<>(
			CommerceOrderThreadLocal.class + "._deleteInProcess",
			() -> Boolean.FALSE);
	private static final ThreadLocal<HttpServletRequest> _httpServletRequest =
		new CentralizedThreadLocal<>(
			CommerceOrderThreadLocal.class + "._httpServletRequest",
			() -> null);
	private static final ThreadLocal<Boolean> _skipValidateAccountLimit =
		new CentralizedThreadLocal<>(
			CommerceReturnThreadLocal.class + "._skipValidateAccountLimit",
			() -> Boolean.FALSE);

}