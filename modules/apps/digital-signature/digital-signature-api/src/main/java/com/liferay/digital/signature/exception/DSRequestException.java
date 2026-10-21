/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.exception;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Brian I. Kim
 */
public class DSRequestException extends PortalException {

	public static class DuplicateRecipient extends DSRequestException {

		public DuplicateRecipient(long userId) {
			super(
				"User " + userId +
					" is already a digital signature request recipient");
		}

	}

	public static class ExpirationMustNotBeNegative extends DSRequestException {

		public ExpirationMustNotBeNegative() {
			super("Expire after and expire warn must not be negative");
		}

	}

	public static class ExpireWarnMustBeLessThanExpireAfter
		extends DSRequestException {

		public ExpireWarnMustBeLessThanExpireAfter() {
			super("Expire warn must be less than expire after");
		}

	}

	public static class FileEntryHasDSRequest extends DSRequestException {

		public FileEntryHasDSRequest(long fileEntryId, String status) {
			super(
				StringBundler.concat(
					"File entry ", fileEntryId,
					" already has a digital signature request with status \"",
					status, "\""));
		}

	}

	public static class InvalidRecipient extends DSRequestException {

		public InvalidRecipient(long userId) {
			super(
				"User " + userId +
					" cannot be a digital signature request recipient");
		}

	}

	public static class MustHaveFileEntry extends DSRequestException {

		public MustHaveFileEntry() {
			super(
				"A digital signature request must have at least one file " +
					"entry");
		}

	}

	public static class NotActive extends DSRequestException {

		public NotActive(long dsRequestId) {
			super(
				"Digital signature request " + dsRequestId +
					" is no longer active");
		}

	}

	public static class NotEnabled extends DSRequestException {

		public NotEnabled(long groupId) {
			super("Digital signature is not enabled for site " + groupId);
		}

	}

	public static class StagingNotSupported extends DSRequestException {

		public StagingNotSupported(long groupId) {
			super(
				"Digital signature requests are not supported in staging " +
					"group " + groupId);
		}

	}

	public static class UnknownRecipient extends DSRequestException {

		public UnknownRecipient(String emailAddress) {
			super("No user exists with email address " + emailAddress);
		}

	}

	private DSRequestException(String message) {
		super(message);
	}

}