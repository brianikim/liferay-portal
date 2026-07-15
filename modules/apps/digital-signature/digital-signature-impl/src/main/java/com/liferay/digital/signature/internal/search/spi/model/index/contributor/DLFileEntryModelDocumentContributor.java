/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.search.spi.model.index.contributor;

import com.liferay.digital.signature.provider.DSRequestStatusProvider;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;

import java.util.Collections;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Indexes the canonical signature request status on each Document Library file
 * entry so the Document Library table can filter by status without breaking
 * pagination. The status is only contributed when the digital signature feature
 * is enabled, because {@link DSRequestStatusProvider} gates on the feature flag
 * and configuration.
 *
 * @author Brian Kim
 */
@Component(
	property = "indexer.class.name=com.liferay.document.library.kernel.model.DLFileEntry",
	service = ModelDocumentContributor.class
)
public class DLFileEntryModelDocumentContributor
	implements ModelDocumentContributor<DLFileEntry> {

	@Override
	public void contribute(Document document, DLFileEntry dlFileEntry) {
		Map<Long, String> requestStatuses =
			_dsRequestStatusProvider.getRequestStatuses(
				dlFileEntry.getCompanyId(),
				Collections.singletonList(dlFileEntry.getFileEntryId()));

		String signatureStatus = requestStatuses.get(
			dlFileEntry.getFileEntryId());

		if (Validator.isNotNull(signatureStatus)) {
			document.addKeyword("signatureStatus", signatureStatus);
		}
	}

	@Reference
	private DSRequestStatusProvider _dsRequestStatusProvider;

}