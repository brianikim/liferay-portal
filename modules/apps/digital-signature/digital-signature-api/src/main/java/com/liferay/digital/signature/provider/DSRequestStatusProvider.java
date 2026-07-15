/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.provider;

import java.util.Collection;
import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Provides the canonical signature request status for documents, so surfaces such
 * as the Document Library table can render a signature status badge without
 * depending on the digital signature implementation.
 *
 * @author Brian Kim
 */
@ProviderType
public interface DSRequestStatusProvider {

	/**
	 * Returns the current user's recipient status keyed by file entry ID for the
	 * given file entries. Entries where the user is not a recipient are omitted
	 * from the map. The returned values are the canonical DSRequestRecipient
	 * statuses (draft, sent, viewed, signed, completed, declined). Returns an
	 * empty map when the digital signature feature is disabled.
	 *
	 * @param  companyId the company to look up requests in
	 * @param  userId the recipient to resolve statuses for
	 * @param  fileEntryIds the file entry IDs to resolve, typically one page of
	 *         the Document Library table
	 * @return the current user's recipient status by file entry ID
	 */
	public Map<Long, String> getRecipientStatuses(
		long companyId, long userId, Collection<Long> fileEntryIds);

	/**
	 * Returns the request status keyed by file entry ID for the given file
	 * entries. Entries with no signature request are omitted from the map. The
	 * returned values are the canonical DSRequest statuses (draft, sent, viewed,
	 * completed, declined, expired, voided). Returns an empty map when the digital
	 * signature feature is disabled.
	 *
	 * @param  companyId the company to look up requests in
	 * @param  fileEntryIds the file entry IDs to resolve, typically one page of the
	 *         Document Library table
	 * @return the request status by file entry ID
	 */
	public Map<Long, String> getRequestStatuses(
		long companyId, Collection<Long> fileEntryIds);

}