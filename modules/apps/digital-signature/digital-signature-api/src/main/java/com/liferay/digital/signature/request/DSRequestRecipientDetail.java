/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.request;

import java.util.Date;

/**
 * @author Brian Kim
 */
public class DSRequestRecipientDetail {

	public DSRequestRecipientDetail(
		String emailAddress, String name, long recipientUserId,
		String requestRecipientStatus, Date statusDate) {

		_emailAddress = emailAddress;
		_name = name;
		_recipientUserId = recipientUserId;
		_requestRecipientStatus = requestRecipientStatus;
		_statusDate = statusDate;
	}

	public String getEmailAddress() {
		return _emailAddress;
	}

	public String getName() {
		return _name;
	}

	public long getRecipientUserId() {
		return _recipientUserId;
	}

	public String getRequestRecipientStatus() {
		return _requestRecipientStatus;
	}

	public Date getStatusDate() {
		if (_statusDate == null) {
			return null;
		}

		return new Date(_statusDate.getTime());
	}

	private final String _emailAddress;
	private final String _name;
	private final long _recipientUserId;
	private final String _requestRecipientStatus;
	private final Date _statusDate;

}