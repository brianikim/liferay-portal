/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.provider;

import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.provider.DSRequestRecipientRetriever;
import com.liferay.digital.signature.provider.DSSignatureRequiredProvider;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Kim
 */
@Component(service = DSSignatureRequiredProvider.class)
public class DSSignatureRequiredProviderImpl
	implements DSSignatureRequiredProvider {

	@Override
	public int getSignatureRequiredCount(long companyId, long userId) {
		if (!_isEnabled(companyId)) {
			return 0;
		}

		return _dsRequestRecipientRetriever.getPendingDocumentCount(
			companyId, userId, _PENDING_STATUSES);
	}

	@Override
	public Set<Long> getSignatureRequiredFileEntryIds(
		long companyId, long userId, Collection<Long> fileEntryIds) {

		if ((fileEntryIds == null) || fileEntryIds.isEmpty() ||
			!_isEnabled(companyId)) {

			return Collections.emptySet();
		}

		Map<Long, Set<Long>> userIdsByFileEntryId =
			_dsRequestRecipientRetriever.getUserIdsByFileEntryId(
				companyId, fileEntryIds, _PENDING_STATUSES);

		Set<Long> signatureRequiredFileEntryIds = new HashSet<>();

		for (Map.Entry<Long, Set<Long>> entry :
				userIdsByFileEntryId.entrySet()) {

			Set<Long> userIds = entry.getValue();

			if (userIds.contains(userId)) {
				signatureRequiredFileEntryIds.add(entry.getKey());
			}
		}

		return signatureRequiredFileEntryIds;
	}

	private boolean _isEnabled(long companyId) {
		if (!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-69290")) {
			return false;
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				companyId, 0);

		if ((digitalSignatureConfiguration != null) &&
			digitalSignatureConfiguration.enabled()) {

			return true;
		}

		return false;
	}

	private static final String[] _PENDING_STATUSES = {"sent", "viewed"};

	@Reference
	private DSRequestRecipientRetriever _dsRequestRecipientRetriever;

}