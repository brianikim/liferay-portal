/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.web.internal.display.context;

import com.liferay.digital.signature.request.DSRequestDetail;
import com.liferay.digital.signature.request.DSRequestManager;
import com.liferay.digital.signature.request.DSRequestRecipientDetail;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.http.HttpServletRequest;

import java.text.Format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author Brian Kim
 */
public class DLSignatureDetailDisplayContext {

	public DLSignatureDetailDisplayContext(
		long companyId, DSRequestManager dsRequestManager, long fileEntryId,
		HttpServletRequest httpServletRequest, Locale locale,
		TimeZone timeZone) {

		_companyId = companyId;
		_dsRequestManager = dsRequestManager;
		_fileEntryId = fileEntryId;
		_httpServletRequest = httpServletRequest;
		_locale = locale;
		_timeZone = timeZone;
	}

	public DSRequestDetail getDSRequestDetail() {
		if (_dsRequestDetail != null) {
			return _dsRequestDetail;
		}

		if (_dsRequestManager == null) {
			return null;
		}

		_dsRequestDetail = _dsRequestManager.getRequestDetail(
			_companyId, _fileEntryId);

		return _dsRequestDetail;
	}

	public List<SignatureActivity> getSignatureActivities() {
		DSRequestDetail dsRequestDetail = getDSRequestDetail();

		if (dsRequestDetail == null) {
			return Collections.emptyList();
		}

		List<SignatureActivity> signatureActivities = new ArrayList<>();

		signatureActivities.add(
			new SignatureActivity(
				"primary",
				_formatMeta(
					dsRequestDetail.getRequesterName(),
					dsRequestDetail.getCreateDate()),
				false,
				LanguageUtil.get(
					_httpServletRequest, "signature-envelope-created")));

		for (DSRequestRecipientDetail dsRequestRecipientDetail :
				dsRequestDetail.getRecipientDetails()) {

			signatureActivities.add(
				_toSignatureActivity(dsRequestRecipientDetail));
		}

		Date completionDate = dsRequestDetail.getCompletionDate();

		signatureActivities.add(
			new SignatureActivity(
				(completionDate == null) ? "secondary" : "success",
				(completionDate == null) ?
					LanguageUtil.get(_httpServletRequest, "pending") :
						_format(completionDate),
				completionDate == null,
				LanguageUtil.get(_httpServletRequest, "completed")));

		return signatureActivities;
	}

	public String getStatusDisplayType(String status) {
		if (Objects.equals(status, "completed") ||
			Objects.equals(status, "signed")) {

			return "success";
		}

		if (Objects.equals(status, "declined") ||
			Objects.equals(status, "voided")) {

			return "danger";
		}

		if (Objects.equals(status, "expired")) {
			return "warning";
		}

		if (Objects.equals(status, "draft")) {
			return "secondary";
		}

		return "info";
	}

	public String getStatusLabel(Date date) {
		if (date == null) {
			return LanguageUtil.get(_httpServletRequest, "pending");
		}

		return _format(date);
	}

	public class SignatureActivity {

		public SignatureActivity(
			String displayType, String meta, boolean pending, String title) {

			_displayType = displayType;
			_meta = meta;
			_pending = pending;
			_title = title;
		}

		public String getDisplayType() {
			return _displayType;
		}

		public String getMeta() {
			return _meta;
		}

		public String getTitle() {
			return _title;
		}

		public boolean isPending() {
			return _pending;
		}

		private final String _displayType;
		private final String _meta;
		private final boolean _pending;
		private final String _title;

	}

	private String _format(Date date) {
		if (date == null) {
			return null;
		}

		Format format = FastDateFormatFactoryUtil.getDateTime(
			_locale, _timeZone);

		return format.format(date);
	}

	private String _formatMeta(String name, Date date) {
		String formattedDate = _format(date);

		if (Validator.isNull(name)) {
			return formattedDate;
		}

		if (formattedDate == null) {
			return name;
		}

		return StringBundler.concat(name, " · ", formattedDate);
	}

	private SignatureActivity _toSignatureActivity(
		DSRequestRecipientDetail dsRequestRecipientDetail) {

		String status = dsRequestRecipientDetail.getRequestRecipientStatus();

		String name = dsRequestRecipientDetail.getName();

		if (Objects.equals(status, "signed") ||
			Objects.equals(status, "completed")) {

			return new SignatureActivity(
				"success", _format(dsRequestRecipientDetail.getStatusDate()),
				false,
				LanguageUtil.format(_httpServletRequest, "signed-by-x", name));
		}

		if (Objects.equals(status, "declined")) {
			return new SignatureActivity(
				"danger", _format(dsRequestRecipientDetail.getStatusDate()),
				false,
				LanguageUtil.format(
					_httpServletRequest, "declined-by-x", name));
		}

		return new SignatureActivity(
			"secondary", LanguageUtil.get(_httpServletRequest, "pending"), true,
			LanguageUtil.format(
				_httpServletRequest, "awaiting-signature-from-x", name));
	}

	private final long _companyId;
	private DSRequestDetail _dsRequestDetail;
	private final DSRequestManager _dsRequestManager;
	private final long _fileEntryId;
	private final HttpServletRequest _httpServletRequest;
	private final Locale _locale;
	private final TimeZone _timeZone;

}