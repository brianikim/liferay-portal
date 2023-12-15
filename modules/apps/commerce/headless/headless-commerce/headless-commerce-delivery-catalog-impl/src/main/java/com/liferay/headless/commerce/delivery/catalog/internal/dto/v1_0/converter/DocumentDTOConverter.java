/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.converter;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryMetadata;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.model.DLFileVersion;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFileEntryMetadataLocalService;
import com.liferay.document.library.util.DLFileEntryTypeUtil;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.storage.DDMStorageEngineManager;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.ContentField;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.Document;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.DocumentType;
import com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.util.ContentFieldUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(
	property = "dto.class.name=com.liferay.document.library.kernel.model.DLFileEntry",
	service = DTOConverter.class
)
public class DocumentDTOConverter
	implements DTOConverter<DLFileEntry, Document> {

	@Override
	public String getContentType() {
		return Document.class.getSimpleName();
	}

	@Override
	public Document toDTO(DTOConverterContext dtoConverterContext)
		throws Exception {

		FileEntry fileEntry = _dlAppService.getFileEntry(
			(Long)dtoConverterContext.getId());

		FileVersion fileVersion = fileEntry.getFileVersion();

		return new Document() {
			{
				description = fileEntry.getDescription();
				documentType = _toDocumentType(
					dtoConverterContext, fileVersion);
				encodingFormat = fileEntry.getMimeType();
				externalReferenceCode = fileEntry.getExternalReferenceCode();
				fileExtension = fileEntry.getExtension();
				fileName = fileEntry.getFileName();
				id = fileEntry.getFileEntryId();
				title = fileEntry.getTitle();
			}
		};
	}

	private List<DDMFormValues> _getDDMFormValues(
			DLFileEntryType dlFileEntryType, DLFileVersion dlFileVersion)
		throws Exception {

		List<DDMFormValues> ddmFormValues = new ArrayList<>();

		for (DDMStructure ddmStructure :
				DLFileEntryTypeUtil.getDDMStructures(dlFileEntryType)) {

			DLFileEntryMetadata dlFileEntryMetadata =
				_dlFileEntryMetadataLocalService.fetchFileEntryMetadata(
					ddmStructure.getStructureId(),
					dlFileVersion.getFileVersionId());

			if (dlFileEntryMetadata == null) {
				continue;
			}

			ddmFormValues.add(
				_ddmStorageEngineManager.getDDMFormValues(
					dlFileEntryMetadata.getDDMStorageId()));
		}

		return ddmFormValues;
	}

	private DocumentType _toDocumentType(
			DTOConverterContext dtoConverterContext, FileVersion fileVersion)
		throws Exception {

		if (!(fileVersion.getModel() instanceof DLFileVersion)) {
			return null;
		}

		DLFileVersion dlFileVersion = (DLFileVersion)fileVersion.getModel();

		DLFileEntryType dlFileEntryType = dlFileVersion.getDLFileEntryType();

		List<DDMFormValues> ddmFormValues = _getDDMFormValues(
			dlFileEntryType, dlFileVersion);

		return new DocumentType() {
			{
				description = dlFileEntryType.getDescription(
					dtoConverterContext.getLocale());
				name = dlFileEntryType.getName(dtoConverterContext.getLocale());

				setAvailableLanguages(
					() -> {
						Set<Locale> locales = new HashSet<>();

						for (DDMFormValues ddmFormValue : ddmFormValues) {
							locales.addAll(ddmFormValue.getAvailableLocales());
						}

						return LocaleUtil.toW3cLanguageIds(
							locales.toArray(new Locale[0]));
					});
				setContentFields(
					() -> {
						List<DDMFormFieldValue> ddmFormFieldValues =
							new ArrayList<>();

						for (DDMFormValues ddmFormValue : ddmFormValues) {
							ddmFormFieldValues.addAll(
								ddmFormValue.getDDMFormFieldValues());
						}

						return TransformUtil.transformToArray(
							ddmFormFieldValues,
							ddmFormFieldValue ->
								ContentFieldUtil.toContentField(
									ddmFormFieldValue, dtoConverterContext),
							ContentField.class);
					});
			}
		};
	}

	@Reference
	private DDMStorageEngineManager _ddmStorageEngineManager;

	@Reference
	private DLAppService _dlAppService;

	@Reference
	private DLFileEntryMetadataLocalService _dlFileEntryMetadataLocalService;

}