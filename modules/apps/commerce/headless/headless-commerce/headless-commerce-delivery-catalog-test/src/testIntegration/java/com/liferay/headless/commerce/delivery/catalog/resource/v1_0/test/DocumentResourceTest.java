/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.catalog.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.constants.CPAttachmentFileEntryConstants;
import com.liferay.commerce.product.model.CPAttachmentFileEntry;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CPAttachmentFileEntryLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.Document;
import com.liferay.headless.commerce.delivery.catalog.client.serdes.v1_0.DocumentSerDes;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Brian I. Kim
 */
@DataGuard(scope = DataGuard.Scope.METHOD)
@RunWith(Arquillian.class)
public class DocumentResourceTest extends BaseDocumentResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());

		_fileEntry = _dlAppLocalService.addFileEntry(
			RandomTestUtil.randomString(), _user.getUserId(),
			testGroup.getGroupId(), DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(), null,
			null, RandomTestUtil.nextDate(), _serviceContext);

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			testGroup.getGroupId(), RandomTestUtil.randomString());
		_cpDefinition = CPTestUtil.addCPDefinition(
			testGroup.getGroupId(), "simple", true, false);
	}

	@Override
	@Test
	public void testGetAttachmentIdDocument() throws Exception {
		Document postDocument = testGetAttachmentIdDocument_addDocument();

		CPAttachmentFileEntry cpAttachmentFileEntry =
			_addCPAttachmentFileEntry();

		Document getDocument = documentResource.getAttachmentIdDocument(
			cpAttachmentFileEntry.getCPAttachmentFileEntryId());

		Assert.assertTrue(equals(postDocument, getDocument));
		assertValid(getDocument);
	}

	@Test
	public void testGraphQLGetAttachmentIdDocument() throws Exception {
		Document document = testGraphQLGetAttachmentIdDocument_addDocument();

		Assert.assertTrue(
			equals(
				document,
				DocumentSerDes.toDTO(
					JSONUtil.getValueAsString(
						invokeGraphQLQuery(
							new GraphQLField(
								"attachmentIdDocument",
								HashMapBuilder.<String, Object>put(
									"id",
									() -> {
										CPAttachmentFileEntry
											cpAttachmentFileEntry =
												_addCPAttachmentFileEntry();

										return cpAttachmentFileEntry.
											getCPAttachmentFileEntryId();
									}
								).build(),
								getGraphQLFields())),
						"JSONObject/data", "Object/attachmentIdDocument"))));
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"externalReferenceCode"};
	}

	@Override
	protected Document testGetAttachmentIdDocument_addDocument()
		throws Exception {

		return _toDocument();
	}

	@Override
	protected Document testGraphQLDocument_addDocument() throws Exception {
		return _toDocument();
	}

	private CPAttachmentFileEntry _addCPAttachmentFileEntry() throws Exception {
		Calendar displayDate = Calendar.getInstance();
		Calendar expirationDate = Calendar.getInstance();

		displayDate.setTime(RandomTestUtil.nextDate());
		expirationDate.setTime(RandomTestUtil.nextDate());

		CPAttachmentFileEntry attachmentFileEntry =
			_cpAttachmentFileEntryLocalService.addCPAttachmentFileEntry(
				RandomTestUtil.randomString(), _user.getUserId(),
				testGroup.getGroupId(),
				_classNameLocalService.getClassNameId(CPDefinition.class),
				_cpDefinition.getCPDefinitionId(), _fileEntry.getFileEntryId(),
				false, null, displayDate.get(Calendar.MONTH),
				displayDate.get(Calendar.DAY_OF_MONTH),
				displayDate.get(Calendar.YEAR), displayDate.get(Calendar.HOUR),
				displayDate.get(Calendar.MINUTE),
				expirationDate.get(Calendar.MONTH),
				expirationDate.get(Calendar.DAY_OF_MONTH),
				expirationDate.get(Calendar.YEAR),
				expirationDate.get(Calendar.HOUR),
				expirationDate.get(Calendar.MINUTE), true, true,
				RandomTestUtil.randomLocaleStringMap(), null,
				RandomTestUtil.nextDouble(),
				CPAttachmentFileEntryConstants.TYPE_OTHER, _serviceContext);

		_attachmentFileEntries.add(attachmentFileEntry);

		return attachmentFileEntry;
	}

	private Document _toDocument() throws Exception {
		return new Document() {
			{
				description = _fileEntry.getDescription();
				encodingFormat = _fileEntry.getMimeType();
				externalReferenceCode = _fileEntry.getExternalReferenceCode();
				fileExtension = _fileEntry.getExtension();
				fileName = _fileEntry.getFileName();
				id = _fileEntry.getFileEntryId();
				title = _fileEntry.getTitle();
			}
		};
	}

	@DeleteAfterTestRun
	private final List<CPAttachmentFileEntry> _attachmentFileEntries =
		new ArrayList<>();

	@Inject
	private ClassNameLocalService _classNameLocalService;

	@DeleteAfterTestRun
	private CommerceChannel _commerceChannel;

	@Inject
	private CPAttachmentFileEntryLocalService
		_cpAttachmentFileEntryLocalService;

	@DeleteAfterTestRun
	private CPDefinition _cpDefinition;

	@Inject
	private DLAppLocalService _dlAppLocalService;

	private FileEntry _fileEntry;
	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}