/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.request;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Brian Kim
 */
public class DSRequestDetail {

	public DSRequestDetail(
		Date completionDate, Date createDate, String emailSubject,
		String providerRequestId,
		List<DSRequestRecipientDetail> recipientDetails,
		String requesterEmailAddress, String requesterName,
		long requesterUserId, String requestStatus) {

		_completionDate = completionDate;
		_createDate = createDate;
		_emailSubject = emailSubject;
		_providerRequestId = providerRequestId;
		_recipientDetails = recipientDetails;
		_requesterEmailAddress = requesterEmailAddress;
		_requesterName = requesterName;
		_requesterUserId = requesterUserId;
		_requestStatus = requestStatus;
	}

	public Date getCompletionDate() {
		if (_completionDate == null) {
			return null;
		}

		return new Date(_completionDate.getTime());
	}

	public Date getCreateDate() {
		if (_createDate == null) {
			return null;
		}

		return new Date(_createDate.getTime());
	}

	public String getEmailSubject() {
		return _emailSubject;
	}

	public String getProviderRequestId() {
		return _providerRequestId;
	}

	public List<DSRequestRecipientDetail> getRecipientDetails() {
		if (_recipientDetails == null) {
			return Collections.emptyList();
		}

		return _recipientDetails;
	}

	public String getRequesterEmailAddress() {
		return _requesterEmailAddress;
	}

	public String getRequesterName() {
		return _requesterName;
	}

	public long getRequesterUserId() {
		return _requesterUserId;
	}

	public String getRequestStatus() {
		return _requestStatus;
	}

	private final Date _completionDate;
	private final Date _createDate;
	private final String _emailSubject;
	private final String _providerRequestId;
	private final List<DSRequestRecipientDetail> _recipientDetails;
	private final String _requesterEmailAddress;
	private final String _requesterName;
	private final long _requesterUserId;
	private final String _requestStatus;

}