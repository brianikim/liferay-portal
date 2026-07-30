/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.request;

import com.liferay.digital.signature.model.DSEnvelope;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Persists a signature request and its signers as the source of truth that the
 * Document Library surfaces read from. Backed by the <code>L_DS_REQUEST</code>
 * and <code>L_DS_REQUEST_RECIPIENT</code> object definitions. All operations are
 * no-ops when the digital signature feature is disabled.
 *
 * @author Brian Kim
 */
@ProviderType
public interface DSRequestManager {

	/**
	 * Records a signature request for each document in the envelope, together
	 * with a recipient for each signer, capturing the provider envelope and
	 * recipient identifiers so later status updates can be matched back.
	 *
	 * @param companyId the company the envelope was created in
	 * @param groupId the site the envelope was created from
	 * @param userId the user who requested the signatures
	 * @param dsEnvelope the envelope returned by the provider on creation
	 */
	public void addDSRequests(
		long companyId, long groupId, long userId, DSEnvelope dsEnvelope);

	/**
	 * Refreshes the stored signature request and its recipients for a provider
	 * envelope from the provider's current state. This is how status stays
	 * current without a provider webhook: callers invoke it at the points where
	 * they already talk to the provider, such as when a signer returns from the
	 * embedded signing ceremony.
	 *
	 * @param companyId the company the envelope belongs to
	 * @param groupId the site to resolve the provider configuration from
	 * @param providerRequestId the provider envelope identifier
	 */
	public void syncDSRequest(
		long companyId, long groupId, String providerRequestId);

}