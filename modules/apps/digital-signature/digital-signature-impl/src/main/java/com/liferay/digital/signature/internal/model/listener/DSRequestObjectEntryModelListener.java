/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.model.listener;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.util.GetterUtil;

import java.io.Serializable;

import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;

/**
 * Reindexes the Document Library file entry backing a signature request whenever
 * the request status changes, so the Document Library table's signature status
 * filter stays accurate. Only signature request object entries trigger a
 * reindex, and only while the feature is enabled.
 *
 * @author Brian Kim
 */
@Component(service = ModelListener.class)
public class DSRequestObjectEntryModelListener
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
					"L_DS_REQUEST")) {

				return;
			}

			Map<String, Serializable> values = objectEntry.getValues();

			long fileEntryId = GetterUtil.getLong(values.get("fileEntryId"));

			if (fileEntryId <= 0) {
				return;
			}

			Indexer<?> indexer = IndexerRegistryUtil.nullSafeGetIndexer(
				"com.liferay.document.library.kernel.model.DLFileEntry");

			indexer.reindex(
				"com.liferay.document.library.kernel.model.DLFileEntry",
				fileEntryId);
		}
		catch (PortalException portalException) {
			throw new ModelListenerException(portalException);
		}
	}

}