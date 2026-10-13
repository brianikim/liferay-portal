/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.model;

import com.liferay.digital.signature.constants.DSRequestConstants;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Brian I. Kim
 * @author Danny Situ
 */
public class DSRequest {

	public DSRequest(
		long companyId, Date createDate, long dsRequestId,
		List<DSRequestRecipient> dsRequestRecipients, List<Long> fileEntryIds,
		String requesterEmailAddress, String requesterName,
		long requesterUserId, Map<String, Serializable> values) {

		_companyId = companyId;
		_createDate = createDate;
		_dsRequestId = dsRequestId;
		_dsRequestRecipients = dsRequestRecipients;
		_fileEntryIds = fileEntryIds;
		_requesterEmailAddress = requesterEmailAddress;
		_requesterName = requesterName;
		_requesterUserId = requesterUserId;

		_emailBody = MapUtil.getString(values, "emailBody");
		_emailSubject = MapUtil.getString(values, "emailSubject");
		_expirationDate = _toDate(values.get("requestExpirationDate"));
		_providerKey = MapUtil.getString(values, "providerKey");
		_providerRequestId = MapUtil.getString(values, "providerRequestId");
		_siteGroupId = MapUtil.getLong(values, "siteGroupId");
		_status = MapUtil.getString(values, "requestStatus");
		_statusDate = _toDate(values.get("requestStatusDate"));
	}

	public long getCompanyId() {
		return _companyId;
	}

	public Date getCreateDate() {
		if (_createDate == null) {
			return null;
		}

		return new Date(_createDate.getTime());
	}

	public long getDSRequestId() {
		return _dsRequestId;
	}

	public List<DSRequestRecipient> getDSRequestRecipients() {
		if (_dsRequestRecipients == null) {
			return Collections.emptyList();
		}

		return _dsRequestRecipients;
	}

	public String getEmailBody() {
		return _emailBody;
	}

	public String getEmailSubject() {
		return _emailSubject;
	}

	public Date getExpirationDate() {
		if (_expirationDate == null) {
			return null;
		}

		return new Date(_expirationDate.getTime());
	}

	public List<Long> getFileEntryIds() {
		if (_fileEntryIds == null) {
			return Collections.emptyList();
		}

		return _fileEntryIds;
	}

	public String getProviderKey() {
		return _providerKey;
	}

	public String getProviderRequestId() {
		return _providerRequestId;
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

	public long getSiteGroupId() {
		return _siteGroupId;
	}

	public String getStatus() {
		if ((_expirationDate != null) &&
			!ArrayUtil.contains(
				DSRequestConstants.STATUSES_REQUEST_TERMINAL, _status) &&
			_expirationDate.before(new Date())) {

			return DSRequestConstants.STATUS_EXPIRED;
		}

		return _status;
	}

	public Date getStatusDate() {
		if (_statusDate == null) {
			return null;
		}

		return new Date(_statusDate.getTime());
	}

	public boolean isRequestable() {
		if (isTerminal() &&
			!Objects.equals(getStatus(), DSRequestConstants.STATUS_COMPLETED)) {

			return true;
		}

		return false;
	}

	public boolean isSignatureRequired(String emailAddress) {
		if (Validator.isNull(emailAddress) || isTerminal()) {
			return false;
		}

		for (DSRequestRecipient dsRequestRecipient : getDSRequestRecipients()) {
			if (StringUtil.equalsIgnoreCase(
					emailAddress, dsRequestRecipient.getEmailAddress()) &&
				Objects.equals(
					DSRequestConstants.STATUS_SENT,
					dsRequestRecipient.getStatus())) {

				return true;
			}
		}

		return false;
	}

	public boolean isTerminal() {
		return ArrayUtil.contains(
			DSRequestConstants.STATUSES_REQUEST_TERMINAL, getStatus());
	}

	private Date _toDate(Serializable value) {
		if (value instanceof Date) {
			return (Date)value;
		}

		return null;
	}

	private final long _companyId;
	private final Date _createDate;
	private final long _dsRequestId;
	private final List<DSRequestRecipient> _dsRequestRecipients;
	private final String _emailBody;
	private final String _emailSubject;
	private final Date _expirationDate;
	private final List<Long> _fileEntryIds;
	private final String _providerKey;
	private final String _providerRequestId;
	private final String _requesterEmailAddress;
	private final String _requesterName;
	private final long _requesterUserId;
	private final long _siteGroupId;
	private final String _status;
	private final Date _statusDate;

}