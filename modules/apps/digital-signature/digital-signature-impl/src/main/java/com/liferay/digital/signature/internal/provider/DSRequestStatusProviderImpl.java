/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.provider;

import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.provider.DSActiveRequestResolver;
import com.liferay.digital.signature.provider.DSRequestRecipientRetriever;
import com.liferay.digital.signature.provider.DSRequestStatusProvider;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;

import java.io.Serializable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Kim
 */
@Component(service = DSRequestStatusProvider.class)
public class DSRequestStatusProviderImpl implements DSRequestStatusProvider {

	@Override
	public Map<Long, String> getRecipientStatuses(
		long companyId, long userId, Collection<Long> fileEntryIds) {

		if ((fileEntryIds == null) || fileEntryIds.isEmpty() ||
			!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-69290")) {

			return Collections.emptyMap();
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				companyId, 0);

		if ((digitalSignatureConfiguration == null) ||
			!digitalSignatureConfiguration.enabled()) {

			return Collections.emptyMap();
		}

		Map<Long, Map<Long, String>> recipientStatusesByFileEntryId =
			_dsRequestRecipientRetriever.getRecipientStatusesByFileEntryId(
				companyId, fileEntryIds);

		Map<Long, String> recipientStatuses = new HashMap<>();

		for (Map.Entry<Long, Map<Long, String>> entry :
				recipientStatusesByFileEntryId.entrySet()) {

			String recipientStatus = entry.getValue(
			).get(
				userId
			);

			if (recipientStatus != null) {
				recipientStatuses.put(entry.getKey(), recipientStatus);
			}
		}

		return recipientStatuses;
	}

	@Override
	public Map<Long, String> getRequestStatuses(
		long companyId, Collection<Long> fileEntryIds) {

		if ((fileEntryIds == null) || fileEntryIds.isEmpty() ||
			!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-69290")) {

			return Collections.emptyMap();
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				companyId, 0);

		if ((digitalSignatureConfiguration == null) ||
			!digitalSignatureConfiguration.enabled()) {

			return Collections.emptyMap();
		}

		try {
			Map<Long, String> requestStatuses = new HashMap<>();

			// A document can have more than one signature request over time;
			// report the status of the active one.

			for (Map.Entry<Long, Map<String, Serializable>> entry :
					_dsActiveRequestResolver.
						getActiveRequestValuesByFileEntryId(
							companyId, fileEntryIds
						).entrySet()) {

				requestStatuses.put(
					entry.getKey(),
					GetterUtil.getString(
						entry.getValue(
						).get(
							"requestStatus"
						)));
			}

			return requestStatuses;
		}
		catch (Exception exception) {
			_log.error(
				"Unable to load signature request statuses for company " +
					companyId,
				exception);

			return Collections.emptyMap();
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DSRequestStatusProviderImpl.class);

	@Reference
	private DSActiveRequestResolver _dsActiveRequestResolver;

	@Reference
	private DSRequestRecipientRetriever _dsRequestRecipientRetriever;

}