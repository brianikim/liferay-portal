/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.web.internal.exception;

import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Brian I. Kim
 */
public class DSCommerceOrderAttachmentException extends PortalException {

	public static class InvalidRecipient
		extends DSCommerceOrderAttachmentException {

		public InvalidRecipient(long userId) {
			super("User " + userId + " cannot sign this commerce order");
		}

	}

	public static class MustHaveRecipient
		extends DSCommerceOrderAttachmentException {

		public MustHaveRecipient() {
			super(
				"A digital signature request must have at least one recipient");
		}

	}

	public static class NoDSRequest extends DSCommerceOrderAttachmentException {

		public NoDSRequest(long commerceOrderAttachmentId) {
			super(
				"Commerce order attachment " + commerceOrderAttachmentId +
					" has no digital signature request");
		}

	}

	public static class NotEnabled extends DSCommerceOrderAttachmentException {

		public NotEnabled(long commerceOrderId) {
			super(
				"Digital signature is not enabled for commerce order " +
					commerceOrderId);
		}

	}

	public static class TooManyCountersigners
		extends DSCommerceOrderAttachmentException {

		public TooManyCountersigners() {
			super("Only one internal countersigner is allowed");
		}

	}

	public static class UnsupportedFileExtension
		extends DSCommerceOrderAttachmentException {

		public UnsupportedFileExtension(long commerceOrderAttachmentId) {
			super(
				"The file extension of commerce order attachment " +
					commerceOrderAttachmentId +
						" is not supported by digital signature");
		}

	}

	private DSCommerceOrderAttachmentException(String message) {
		super(message);
	}

}