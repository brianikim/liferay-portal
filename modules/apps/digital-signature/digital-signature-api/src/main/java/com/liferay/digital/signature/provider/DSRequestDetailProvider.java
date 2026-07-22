/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.provider;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Provides the per-document signature status detail, so surfaces such as the
 * Document Library can render a drill-in side panel of each recipient's status
 * and the signing timeline without depending on the digital signature
 * implementation.
 *
 * @author Brian Kim
 */
@ProviderType
public interface DSRequestDetailProvider {

	/**
	 * Returns the signature request detail for the given document, or
	 * <code>null</code> when the document has no signature request or the digital
	 * signature feature is disabled.
	 *
	 * @param  companyId the company to look up the request in
	 * @param  fileEntryId the file entry the request was made for
	 * @return the signature request detail, or <code>null</code>
	 */
	public DSRequestDetail getRequestDetail(long companyId, long fileEntryId);

}