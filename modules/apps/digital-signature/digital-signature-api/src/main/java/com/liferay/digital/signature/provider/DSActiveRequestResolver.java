/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.provider;

import java.io.Serializable;

import java.util.Collection;
import java.util.Map;

/**
 * Resolves the single active signature request for a document.
 *
 * <p>
 * A document can accumulate more than one signature request over its lifetime,
 * for example a request that is voided and then resent. The read side shows one
 * current request per document: the most recent non-terminal request, or, when
 * every request has reached a terminal state, the most recent terminal request.
 * Recency is ordered by the object entry primary key, which increases with
 * creation. The remaining requests are history.
 * </p>
 *
 * @author Brian Kim
 */
public interface DSActiveRequestResolver {

	public Map<Long, Long> getActiveFileEntryIdsByRequestId(
			long companyId, Collection<Long> fileEntryIds)
		throws Exception;

	public Map<Long, Map<String, Serializable>>
			getActiveRequestValuesByFileEntryId(
				long companyId, Collection<Long> fileEntryIds)
		throws Exception;

}