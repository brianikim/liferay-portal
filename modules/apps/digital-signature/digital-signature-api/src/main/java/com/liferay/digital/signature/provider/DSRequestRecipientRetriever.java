/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.provider;

import com.liferay.portal.kernel.exception.PortalException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Traverses the DSRequest to DSRequestRecipient one-to-many relationship to
 * resolve, per document, which users hold a recipient in a given set of
 * statuses. Shared by the recipient provider (badge and count) and the search
 * index contributor.
 *
 * @author Brian Kim
 */
@ProviderType
public interface DSRequestRecipientRetriever {

	public long getFileEntryId(long companyId, long recipientObjectEntryId)
		throws PortalException;

	public int getPendingDocumentCount(
		long companyId, long userId, String... statusKeys);

	public Map<Long, Set<Long>> getUserIdsByFileEntryId(
		long companyId, Collection<Long> fileEntryIds, String... statusKeys);

}