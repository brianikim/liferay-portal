/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.configuration.AttachmentsConfiguration;
import com.liferay.commerce.product.constants.CPAttachmentFileEntryConstants;
import com.liferay.commerce.product.exception.CPAttachmentFileEntryCountException;
import com.liferay.commerce.product.exception.CPAttachmentFileEntryNameException;
import com.liferay.commerce.product.exception.CPAttachmentFileEntrySizeException;
import com.liferay.commerce.product.exception.DuplicateCPAttachmentFileEntryException;
import com.liferay.commerce.product.exception.NoSuchCPAttachmentFileEntryException;
import com.liferay.commerce.product.model.CPAttachmentFileEntry;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPAttachmentFileEntryLocalService;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPOptionLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalServiceUtil;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.lazy.referencing.LazyReferencingThreadLocal;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.CalendarFactoryUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.Calendar;
import java.util.List;

import org.frutilla.FrutillaRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author João Cordeiro
 */
@RunWith(Arquillian.class)
public class CPAttachmentFileEntryLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_company = CompanyTestUtil.addCompany();

		_user = UserTestUtil.addUser(_company);
	}

	@Before
	public void setUp() throws Exception {
		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_company.getGroupId(), _user.getUserId());

		_commerceCatalog = CommerceCatalogLocalServiceUtil.addCommerceCatalog(
			null, RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			LocaleUtil.US.getDisplayLanguage(), _serviceContext);
	}

	@After
	public void tearDown() throws Exception {
		List<CPDefinition> cpDefinitions =
			_cpDefinitionLocalService.getCPDefinitions(
				_commerceCatalog.getGroupId(), WorkflowConstants.STATUS_ANY,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		for (CPDefinition cpDefinition : cpDefinitions) {
			_cpDefinitionLocalService.deleteCPDefinition(cpDefinition);
		}

		_cpOptionLocalService.deleteCPOptions(_company.getCompanyId());
	}

	@Test
	public void testAddCPAttachmentFileEntryWhenFileIsRestricted()
		throws Exception {

		frutillaRule.scenario(
			"Add product attachments while file restrictions are configured"
		).given(
			"A product"
		).and(
			"Restrictions that allow only PNG images and PDF attachments up " +
				"to 100 bytes"
		).when(
			"Images and attachments are added"
		).then(
			"Oversized or disallowed files are rejected"
		).and(
			"Allowed files are accepted"
		);

		CPDefinition cpDefinition = CPTestUtil.addCPDefinition(
			_company.getGroupId(), "simple", true, false);

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				_getConfigurationTemporarySwapper()) {

			_assertAddCPAttachmentFileEntryFails(
				CPAttachmentFileEntrySizeException.class, cpDefinition,
				_addFileEntry(
					"large.png", ContentTypes.IMAGE_PNG, new byte[200]),
				CPAttachmentFileEntryConstants.TYPE_IMAGE);
			_assertAddCPAttachmentFileEntryFails(
				CPAttachmentFileEntryNameException.class, cpDefinition,
				_addFileEntry(
					"image.pdf", ContentTypes.APPLICATION_PDF, new byte[10]),
				CPAttachmentFileEntryConstants.TYPE_IMAGE);
			_assertAddCPAttachmentFileEntryFails(
				CPAttachmentFileEntrySizeException.class, cpDefinition,
				_addFileEntry(
					"large.pdf", ContentTypes.APPLICATION_PDF, new byte[200]),
				CPAttachmentFileEntryConstants.TYPE_OTHER);
			_assertAddCPAttachmentFileEntryFails(
				CPAttachmentFileEntryNameException.class, cpDefinition,
				_addFileEntry(
					"attachment.png", ContentTypes.IMAGE_PNG, new byte[10]),
				CPAttachmentFileEntryConstants.TYPE_OTHER);

			_addCPAttachmentFileEntry(
				cpDefinition,
				_addFileEntry(
					"image.png", ContentTypes.IMAGE_PNG, new byte[10]),
				CPAttachmentFileEntryConstants.TYPE_IMAGE);
			_addCPAttachmentFileEntry(
				cpDefinition,
				_addFileEntry(
					"attachment.pdf", ContentTypes.APPLICATION_PDF,
					new byte[10]),
				CPAttachmentFileEntryConstants.TYPE_OTHER);
		}

		_assertCPAttachmentFileEntriesCount(
			1, cpDefinition, CPAttachmentFileEntryConstants.TYPE_IMAGE);
		_assertCPAttachmentFileEntriesCount(
			1, cpDefinition, CPAttachmentFileEntryConstants.TYPE_OTHER);
	}

	@Test
	public void testAddCPAttachmentFileEntryWhenMaximumCountIsReached()
		throws Exception {

		frutillaRule.scenario(
			"Add product attachments beyond the configured maximums"
		).given(
			"A product"
		).and(
			"Maximums of two images and one attachment per product"
		).when(
			"More images and attachments than allowed are added"
		).then(
			"The images and attachments beyond the maximums are rejected"
		);

		CPDefinition cpDefinition = CPTestUtil.addCPDefinition(
			_company.getGroupId(), "simple", true, false);

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				_getConfigurationTemporarySwapper()) {

			for (int i = 0; i < 2; i++) {
				_addCPAttachmentFileEntry(
					cpDefinition,
					_addFileEntry(
						"image" + i + ".png", ContentTypes.IMAGE_PNG,
						new byte[10]),
					CPAttachmentFileEntryConstants.TYPE_IMAGE);
			}

			_assertAddCPAttachmentFileEntryFails(
				CPAttachmentFileEntryCountException.class, cpDefinition,
				_addFileEntry(
					"image.png", ContentTypes.IMAGE_PNG, new byte[10]),
				CPAttachmentFileEntryConstants.TYPE_IMAGE);

			_addCPAttachmentFileEntry(
				cpDefinition,
				_addFileEntry(
					"attachment1.pdf", ContentTypes.APPLICATION_PDF,
					new byte[10]),
				CPAttachmentFileEntryConstants.TYPE_OTHER);

			_assertAddCPAttachmentFileEntryFails(
				CPAttachmentFileEntryCountException.class, cpDefinition,
				_addFileEntry(
					"attachment2.pdf", ContentTypes.APPLICATION_PDF,
					new byte[10]),
				CPAttachmentFileEntryConstants.TYPE_OTHER);
		}

		_assertCPAttachmentFileEntriesCount(
			2, cpDefinition, CPAttachmentFileEntryConstants.TYPE_IMAGE);
		_assertCPAttachmentFileEntriesCount(
			1, cpDefinition, CPAttachmentFileEntryConstants.TYPE_OTHER);
	}

	@Test
	public void testGetOrAddEmptyCPAttachmentFileEntry() throws Exception {
		frutillaRule.scenario(
			"Get or add an empty product attachment file entry"
		).given(
			"A product and an external reference code"
		).when(
			"An empty attachment file entry is requested"
		).then(
			"A NoSuchCPAttachmentFileEntryException is thrown while lazy " +
				"referencing is disabled"
		).and(
			"An empty stub with the given external reference code is " +
				"returned while lazy referencing is enabled"
		).and(
			"The same attachment file entry is resolved on subsequent requests"
		).and(
			"The empty status is cleared once the stub is updated"
		);

		CPDefinition cpDefinition = CPTestUtil.addCPDefinition(
			_company.getGroupId(), "simple", true, false);

		long classNameId = _classNameLocalService.getClassNameId(
			CPDefinition.class);
		long classPK = cpDefinition.getCPDefinitionId();

		String externalReferenceCode = RandomTestUtil.randomString();

		try {
			_cpAttachmentFileEntryLocalService.
				getOrAddEmptyCPAttachmentFileEntry(
					externalReferenceCode, _company.getCompanyId(),
					_user.getUserId(), _company.getGroupId(), classNameId,
					classPK);

			Assert.fail();
		}
		catch (NoSuchCPAttachmentFileEntryException
					noSuchCPAttachmentFileEntryException) {

			Assert.assertNotNull(noSuchCPAttachmentFileEntryException);
		}

		CPAttachmentFileEntry cpAttachmentFileEntry;

		try (SafeCloseable safeCloseable =
				LazyReferencingThreadLocal.setEnabledWithSafeCloseable(true)) {

			cpAttachmentFileEntry =
				_cpAttachmentFileEntryLocalService.
					getOrAddEmptyCPAttachmentFileEntry(
						externalReferenceCode, _company.getCompanyId(),
						_user.getUserId(), _company.getGroupId(), classNameId,
						classPK);

			Assert.assertEquals(
				WorkflowConstants.STATUS_EMPTY,
				cpAttachmentFileEntry.getStatus());
			Assert.assertEquals(
				externalReferenceCode,
				cpAttachmentFileEntry.getExternalReferenceCode());
			Assert.assertFalse(cpAttachmentFileEntry.isCDNEnabled());

			CPAttachmentFileEntry resolvedCPAttachmentFileEntry =
				_cpAttachmentFileEntryLocalService.
					getOrAddEmptyCPAttachmentFileEntry(
						externalReferenceCode, _company.getCompanyId(),
						_user.getUserId(), _company.getGroupId(), classNameId,
						classPK);

			Assert.assertEquals(
				cpAttachmentFileEntry.getCPAttachmentFileEntryId(),
				resolvedCPAttachmentFileEntry.getCPAttachmentFileEntryId());
		}

		Calendar calendar = CalendarFactoryUtil.getCalendar();

		cpAttachmentFileEntry =
			_cpAttachmentFileEntryLocalService.updateCPAttachmentFileEntry(
				_user.getUserId(),
				cpAttachmentFileEntry.getCPAttachmentFileEntryId(), 0, true,
				"http://" + RandomTestUtil.randomString(),
				calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE),
				calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE), 0, 0, 0, 0, 0, true, false,
				HashMapBuilder.put(
					LocaleUtil.getSiteDefault(), RandomTestUtil.randomString()
				).build(),
				null, 0, 0, _serviceContext);

		Assert.assertNotEquals(
			WorkflowConstants.STATUS_EMPTY, cpAttachmentFileEntry.getStatus());
	}

	@Test
	public void testUpdateCPAttachmentFileEntryWhenFileIsRestricted()
		throws Exception {

		frutillaRule.scenario(
			"Update a product image while file restrictions are configured"
		).given(
			"A product with the maximum number of images"
		).and(
			"Restrictions that allow only PNG images up to 100 bytes"
		).when(
			"The file of an image is replaced"
		).then(
			"A disallowed file is rejected"
		).and(
			"An allowed file is accepted without counting toward the maximum"
		);

		CPDefinition cpDefinition = CPTestUtil.addCPDefinition(
			_company.getGroupId(), "simple", true, false);

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				_getConfigurationTemporarySwapper()) {

			_addCPAttachmentFileEntry(
				cpDefinition,
				_addFileEntry(
					"image1.png", ContentTypes.IMAGE_PNG, new byte[10]),
				CPAttachmentFileEntryConstants.TYPE_IMAGE);

			CPAttachmentFileEntry cpAttachmentFileEntry =
				_addCPAttachmentFileEntry(
					cpDefinition,
					_addFileEntry(
						"image2.png", ContentTypes.IMAGE_PNG, new byte[10]),
					CPAttachmentFileEntryConstants.TYPE_IMAGE);

			try {
				_updateCPAttachmentFileEntry(
					cpAttachmentFileEntry,
					_addFileEntry(
						"image.pdf", ContentTypes.APPLICATION_PDF,
						new byte[10]));

				Assert.fail();
			}
			catch (CPAttachmentFileEntryNameException
						cpAttachmentFileEntryNameException) {

				Assert.assertNotNull(cpAttachmentFileEntryNameException);
			}

			_updateCPAttachmentFileEntry(
				cpAttachmentFileEntry,
				_addFileEntry(
					"image3.png", ContentTypes.IMAGE_PNG, new byte[10]));
		}

		_assertCPAttachmentFileEntriesCount(
			2, cpDefinition, CPAttachmentFileEntryConstants.TYPE_IMAGE);
	}

	@Test(expected = DuplicateCPAttachmentFileEntryException.class)
	public void testUpdateWithExistingExternalReferenceCode() throws Exception {
		frutillaRule.scenario(
			"Update a product attachment"
		).given(
			"A product with an attachment"
		).and(
			"A second product without an attachment"
		).when(
			"A second attachment with the same ERC as the first attachment " +
				"is posted to the second product"
		).then(
			"An exception is thrown"
		);

		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinition(
			_company.getGroupId(), "simple", true, false);

		CPDefinition cpDefinition2 = CPTestUtil.addCPDefinition(
			_company.getGroupId(), "simple", true, false);

		CPAttachmentFileEntry cpAttachmentFileEntry = _addCPAttachmentFileEntry(
			cpDefinition1,
			_dlAppLocalService.addFileEntry(
				RandomTestUtil.randomString(), _user.getUserId(),
				_company.getGroupId(),
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				null, null, null, RandomTestUtil.nextDate(), _serviceContext),
			CPAttachmentFileEntryConstants.TYPE_OTHER);

		Calendar displayDateCalendar = Calendar.getInstance();

		displayDateCalendar.setTime(RandomTestUtil.nextDate());

		Calendar expirationDateCalendar = Calendar.getInstance();

		expirationDateCalendar.setTime(RandomTestUtil.nextDate());

		_cpAttachmentFileEntryLocalService.addOrUpdateCPAttachmentFileEntry(
			cpAttachmentFileEntry.getExternalReferenceCode(), _user.getUserId(),
			_company.getGroupId(),
			_classNameLocalService.getClassNameId(CPDefinition.class),
			cpDefinition2.getCPDefinitionId(), 0,
			cpAttachmentFileEntry.getFileEntryId(), false, null,
			displayDateCalendar.get(Calendar.MONTH),
			displayDateCalendar.get(Calendar.DAY_OF_MONTH),
			displayDateCalendar.get(Calendar.YEAR),
			displayDateCalendar.get(Calendar.HOUR),
			displayDateCalendar.get(Calendar.MINUTE),
			expirationDateCalendar.get(Calendar.MONTH),
			expirationDateCalendar.get(Calendar.DAY_OF_MONTH),
			expirationDateCalendar.get(Calendar.YEAR),
			expirationDateCalendar.get(Calendar.HOUR),
			expirationDateCalendar.get(Calendar.MINUTE), true, true,
			RandomTestUtil.randomLocaleStringMap(), null,
			RandomTestUtil.nextDouble(),
			CPAttachmentFileEntryConstants.TYPE_OTHER, _serviceContext);
	}

	@Rule
	public final FrutillaRule frutillaRule = new FrutillaRule();

	private CPAttachmentFileEntry _addCPAttachmentFileEntry(
			CPDefinition cpDefinition, FileEntry fileEntry, int type)
		throws Exception {

		Calendar displayDateCalendar = Calendar.getInstance();

		displayDateCalendar.setTime(RandomTestUtil.nextDate());

		Calendar expirationDateCalendar = Calendar.getInstance();

		expirationDateCalendar.setTime(RandomTestUtil.nextDate());

		return _cpAttachmentFileEntryLocalService.addCPAttachmentFileEntry(
			RandomTestUtil.randomString(), _user.getUserId(),
			_company.getGroupId(),
			_classNameLocalService.getClassNameId(CPDefinition.class),
			cpDefinition.getCPDefinitionId(), fileEntry.getFileEntryId(), false,
			null, displayDateCalendar.get(Calendar.MONTH),
			displayDateCalendar.get(Calendar.DAY_OF_MONTH),
			displayDateCalendar.get(Calendar.YEAR),
			displayDateCalendar.get(Calendar.HOUR),
			displayDateCalendar.get(Calendar.MINUTE),
			expirationDateCalendar.get(Calendar.MONTH),
			expirationDateCalendar.get(Calendar.DAY_OF_MONTH),
			expirationDateCalendar.get(Calendar.YEAR),
			expirationDateCalendar.get(Calendar.HOUR),
			expirationDateCalendar.get(Calendar.MINUTE), true, true,
			RandomTestUtil.randomLocaleStringMap(), null,
			RandomTestUtil.nextDouble(), type, _serviceContext);
	}

	private FileEntry _addFileEntry(
			String sourceFileName, String mimeType, byte[] bytes)
		throws Exception {

		return _dlAppLocalService.addFileEntry(
			null, _user.getUserId(), _company.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID, sourceFileName,
			mimeType, bytes, null, null, null, _serviceContext);
	}

	private void _assertAddCPAttachmentFileEntryFails(
			Class<? extends Exception> exceptionClass,
			CPDefinition cpDefinition, FileEntry fileEntry, int type)
		throws Exception {

		try {
			_addCPAttachmentFileEntry(cpDefinition, fileEntry, type);

			Assert.fail();
		}
		catch (Exception exception) {
			Assert.assertEquals(exceptionClass, exception.getClass());
		}
	}

	private void _assertCPAttachmentFileEntriesCount(
		int expectedCount, CPDefinition cpDefinition, int type) {

		Assert.assertEquals(
			expectedCount,
			_cpAttachmentFileEntryLocalService.getCPAttachmentFileEntriesCount(
				_classNameLocalService.getClassNameId(CPDefinition.class),
				cpDefinition.getCPDefinitionId(), type,
				WorkflowConstants.STATUS_ANY));
	}

	private ConfigurationTemporarySwapper _getConfigurationTemporarySwapper()
		throws Exception {

		return new ConfigurationTemporarySwapper(
			AttachmentsConfiguration.class.getName(),
			HashMapDictionaryBuilder.<String, Object>put(
				"attachmentExtensions", new String[] {".pdf"}
			).put(
				"attachmentMaxSize", 100L
			).put(
				"imageExtensions", new String[] {".png"}
			).put(
				"imageMaxSize", 100L
			).put(
				"maximumNumberOfAttachmentsPerProduct", 1
			).put(
				"maximumNumberOfImagesPerProduct", 2
			).build());
	}

	private CPAttachmentFileEntry _updateCPAttachmentFileEntry(
			CPAttachmentFileEntry cpAttachmentFileEntry, FileEntry fileEntry)
		throws Exception {

		Calendar calendar = CalendarFactoryUtil.getCalendar();

		return _cpAttachmentFileEntryLocalService.updateCPAttachmentFileEntry(
			_user.getUserId(),
			cpAttachmentFileEntry.getCPAttachmentFileEntryId(),
			fileEntry.getFileEntryId(), false, null,
			calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE),
			calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY),
			calendar.get(Calendar.MINUTE), 0, 0, 0, 0, 0, true, true,
			cpAttachmentFileEntry.getTitleMap(), null, 0,
			cpAttachmentFileEntry.getType(), _serviceContext);
	}

	private static Company _company;
	private static User _user;

	@Inject
	private ClassNameLocalService _classNameLocalService;

	private CommerceCatalog _commerceCatalog;

	@Inject
	private CPAttachmentFileEntryLocalService
		_cpAttachmentFileEntryLocalService;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Inject
	private CPOptionLocalService _cpOptionLocalService;

	@Inject
	private DLAppLocalService _dlAppLocalService;

	private ServiceContext _serviceContext;

}