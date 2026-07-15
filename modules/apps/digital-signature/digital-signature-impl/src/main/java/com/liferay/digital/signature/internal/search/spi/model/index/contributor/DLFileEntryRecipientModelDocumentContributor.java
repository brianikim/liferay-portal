/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.search.spi.model.index.contributor;

import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.provider.DSRequestRecipientRetriever;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Indexes, per Document Library file entry, the users who have a signature
 * recipient in a pending state (signatureRequiredUserIds) and in a signed state
 * (signatureSignedUserIds), so the Document Library table can filter by "Action
 * required" and "Signed" for the current user without breaking pagination.
 *
 * <p>
 * It also indexes every recipient's status keyed by user
 * (signatureRecipientStatuses, holding "userId:status" values) and the set of
 * recipient user IDs (signatureRecipientUserIds), so the signature status column
 * can be filtered by each viewer's own recipient status. User IDs are held as
 * field values, not field names, to keep the mapping bounded.
 * </p>
 *
 * @author Brian Kim
 */
@Component(
	property = "indexer.class.name=com.liferay.document.library.kernel.model.DLFileEntry",
	service = ModelDocumentContributor.class
)
public class DLFileEntryRecipientModelDocumentContributor
	implements ModelDocumentContributor<DLFileEntry> {

	@Override
	public void contribute(Document document, DLFileEntry dlFileEntry) {
		long companyId = dlFileEntry.getCompanyId();

		if (!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-69290")) {
			return;
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				companyId, 0);

		if ((digitalSignatureConfiguration == null) ||
			!digitalSignatureConfiguration.enabled()) {

			return;
		}

		_contribute(
			companyId, document, dlFileEntry.getFileEntryId(),
			"signatureRequiredUserIds", "sent", "viewed");
		_contribute(
			companyId, document, dlFileEntry.getFileEntryId(),
			"signatureSignedUserIds", "signed", "completed");

		_contributeRecipientStatuses(
			companyId, document, dlFileEntry.getFileEntryId());
	}

	private void _contribute(
		long companyId, Document document, long fileEntryId, String fieldName,
		String... statusKeys) {

		Map<Long, Set<Long>> userIdsByFileEntryId =
			_dsRequestRecipientRetriever.getUserIdsByFileEntryId(
				companyId, Collections.singletonList(fileEntryId), statusKeys);

		Set<Long> userIds = userIdsByFileEntryId.get(fileEntryId);

		if ((userIds == null) || userIds.isEmpty()) {
			return;
		}

		document.addKeyword(fieldName, ArrayUtil.toLongArray(userIds));
	}

	private void _contributeRecipientStatuses(
		long companyId, Document document, long fileEntryId) {

		Map<Long, Map<Long, String>> recipientStatusesByFileEntryId =
			_dsRequestRecipientRetriever.getRecipientStatusesByFileEntryId(
				companyId, Collections.singletonList(fileEntryId));

		Map<Long, String> statusesByUserId = recipientStatusesByFileEntryId.get(
			fileEntryId);

		if ((statusesByUserId == null) || statusesByUserId.isEmpty()) {
			return;
		}

		List<String> recipientStatuses = new ArrayList<>(
			statusesByUserId.size());

		for (Map.Entry<Long, String> entry : statusesByUserId.entrySet()) {
			recipientStatuses.add(entry.getKey() + ":" + entry.getValue());
		}

		document.addKeyword(
			"signatureRecipientStatuses",
			recipientStatuses.toArray(new String[0]));
		document.addKeyword(
			"signatureRecipientUserIds",
			ArrayUtil.toLongArray(statusesByUserId.keySet()));
	}

	@Reference
	private DSRequestRecipientRetriever _dsRequestRecipientRetriever;

}