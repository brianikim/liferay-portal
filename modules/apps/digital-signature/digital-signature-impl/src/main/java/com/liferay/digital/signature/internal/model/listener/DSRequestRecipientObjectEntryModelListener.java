/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.model.listener;

import com.liferay.digital.signature.provider.DSRequestRecipientRetriever;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Reindexes the Document Library file entry behind a signature request whenever
 * one of its recipients changes, so the "Action required" and "Signed" filters
 * stay accurate for each recipient.
 *
 * @author Brian Kim
 */
@Component(service = ModelListener.class)
public class DSRequestRecipientObjectEntryModelListener
	extends BaseModelListener<ObjectEntry> {

	@Override
	public void onAfterCreate(ObjectEntry objectEntry)
		throws ModelListenerException {

		_reindexFileEntry(objectEntry);
	}

	@Override
	public void onAfterRemove(ObjectEntry objectEntry)
		throws ModelListenerException {

		_reindexFileEntry(objectEntry);
	}

	@Override
	public void onAfterUpdate(
			ObjectEntry originalObjectEntry, ObjectEntry objectEntry)
		throws ModelListenerException {

		_reindexFileEntry(objectEntry);
	}

	private void _reindexFileEntry(ObjectEntry objectEntry)
		throws ModelListenerException {

		if (!FeatureFlagManagerUtil.isEnabled(
				objectEntry.getCompanyId(), "LPD-69290")) {

			return;
		}

		try {
			ObjectDefinition objectDefinition =
				objectEntry.getObjectDefinition();

			if (!Objects.equals(
					objectDefinition.getExternalReferenceCode(),
					"L_DS_REQUEST_RECIPIENT")) {

				return;
			}

			long fileEntryId = _dsRequestRecipientRetriever.getFileEntryId(
				objectEntry.getCompanyId(), objectEntry.getObjectEntryId());

			if (fileEntryId <= 0) {
				return;
			}

			Indexer<?> indexer = IndexerRegistryUtil.nullSafeGetIndexer(
				"com.liferay.document.library.kernel.model.DLFileEntry");

			indexer.reindex(
				"com.liferay.document.library.kernel.model.DLFileEntry",
				fileEntryId);
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Reference
	private DSRequestRecipientRetriever _dsRequestRecipientRetriever;

}