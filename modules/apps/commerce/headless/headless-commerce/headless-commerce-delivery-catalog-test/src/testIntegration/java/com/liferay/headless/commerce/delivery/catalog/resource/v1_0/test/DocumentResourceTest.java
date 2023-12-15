/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.delivery.catalog.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.Document;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DataGuard;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.test.rule.Inject;

import org.junit.Before;
import org.junit.Ignore;
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
	}

	@Ignore
	@Override
	@Test
	public void testGetAttachmentIdDocument() throws Exception {
		super.testGetAttachmentIdDocument();
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLGetAttachmentIdDocument() throws Exception {
		super.testGraphQLGetAttachmentIdDocument();
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

	@Inject
	private DLAppLocalService _dlAppLocalService;

	@DeleteAfterTestRun
	private FileEntry _fileEntry;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}