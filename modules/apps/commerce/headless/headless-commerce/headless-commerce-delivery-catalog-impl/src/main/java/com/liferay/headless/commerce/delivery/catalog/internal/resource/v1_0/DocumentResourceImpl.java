/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.catalog.internal.resource.v1_0;

import com.liferay.commerce.product.model.CPAttachmentFileEntry;
import com.liferay.commerce.product.service.CPAttachmentFileEntryLocalService;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.Attachment;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.Document;
import com.liferay.headless.commerce.delivery.catalog.resource.v1_0.DocumentResource;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.fields.NestedField;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Andrea Sbarra
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/document.properties",
	property = "nested.field.support=true", scope = ServiceScope.PROTOTYPE,
	service = DocumentResource.class
)
public class DocumentResourceImpl extends BaseDocumentResourceImpl {

	@NestedField(parentClass = Attachment.class, value = "document")
	@Override
	public Document getAttachmentIdDocument(Long id) throws Exception {
		CPAttachmentFileEntry cpAttachmentFileEntry =
			_cpAttachmentFileEntryLocalService.getCPAttachmentFileEntry(id);

		return _toDocument(cpAttachmentFileEntry.getFileEntryId());
	}

	private Document _toDocument(long fileEntryId) throws Exception {
		return _documentDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				fileEntryId, contextAcceptLanguage.getPreferredLocale()));
	}

	@Reference
	private CPAttachmentFileEntryLocalService
		_cpAttachmentFileEntryLocalService;

	@Reference(
		target = "(component.name=com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.converter.DocumentDTOConverter)"
	)
	private DTOConverter<CPAttachmentFileEntry, Document> _documentDTOConverter;

}