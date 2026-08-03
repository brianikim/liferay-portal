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
		Date deliveredDate, String emailAddress, String name,
		long recipientUserId, String requestRecipientStatus, Date sentDate,
		Date signedDate, int signingOrder) {

		_deliveredDate = deliveredDate;
		_emailAddress = emailAddress;
		_name = name;
		_recipientUserId = recipientUserId;
		_requestRecipientStatus = requestRecipientStatus;
		_sentDate = sentDate;
		_signedDate = signedDate;
		_signingOrder = signingOrder;
	}

	public Date getDeliveredDate() {
		if (_deliveredDate == null) {
			return null;
		}

		return new Date(_deliveredDate.getTime());
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

	public Date getSentDate() {
		if (_sentDate == null) {
			return null;
		}

		return new Date(_sentDate.getTime());
	}

	public Date getSignedDate() {
		if (_signedDate == null) {
			return null;
		}

		return new Date(_signedDate.getTime());
	}

	public int getSigningOrder() {
		return _signingOrder;
	}

	private final Date _deliveredDate;
	private final String _emailAddress;
	private final String _name;
	private final long _recipientUserId;
	private final String _requestRecipientStatus;
	private final Date _sentDate;
	private final Date _signedDate;
	private final int _signingOrder;

}