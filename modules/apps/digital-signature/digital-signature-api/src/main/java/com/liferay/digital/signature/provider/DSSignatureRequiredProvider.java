/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.provider;

import java.util.Collection;
import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Identifies documents awaiting a given user's signature, so surfaces such as
 * the Document Library table can flag them for the recipient without depending
 * on the digital signature implementation.
 *
 * @author Brian Kim
 */
@ProviderType
public interface DSSignatureRequiredProvider {

	/**
	 * Returns the total number of documents awaiting the user's signature.
	 * Returns 0 when the digital signature feature is disabled.
	 *
	 * @param  companyId the company to look up requests in
	 * @param  userId the recipient to check
	 * @return the number of documents awaiting the user's signature
	 */
	public int getSignatureRequiredCount(long companyId, long userId);

	/**
	 * Returns the subset of the given file entry IDs that await the user's
	 * signature, that is, the user is a recipient whose request has been sent or
	 * viewed but not yet signed. Returns an empty set when the digital signature
	 * feature is disabled.
	 *
	 * @param  companyId the company to look up requests in
	 * @param  userId the recipient to check
	 * @param  fileEntryIds the file entry IDs to resolve, typically one page of
	 *         the Document Library table
	 * @return the file entry IDs awaiting the user's signature
	 */
	public Set<Long> getSignatureRequiredFileEntryIds(
		long companyId, long userId, Collection<Long> fileEntryIds);

}