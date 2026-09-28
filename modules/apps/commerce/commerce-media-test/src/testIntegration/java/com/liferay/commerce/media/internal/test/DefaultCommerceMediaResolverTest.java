/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.media.internal.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.model.AccountGroup;
import com.liferay.account.service.AccountEntryUserRelLocalService;
import com.liferay.account.service.AccountGroupLocalService;
import com.liferay.account.service.AccountGroupRelLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.media.CommerceMediaResolver;
import com.liferay.commerce.media.constants.CommerceMediaConstants;
import com.liferay.commerce.product.constants.CPAttachmentFileEntryConstants;
import com.liferay.commerce.product.model.CPAttachmentFileEntry;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPAttachmentFileEntryLocalService;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Brian I. Kim
 */
@RunWith(Arquillian.class)
public class DefaultCommerceMediaResolverTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
		_user = UserTestUtil.addUser();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getCompanyId(), _group.getGroupId(), _user.getUserId());

		_accountEntry = CommerceAccountTestUtil.addBusinessAccountEntry(
			_user.getUserId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString() + "@liferay.com", _serviceContext);

		CommerceCurrency commerceCurrency =
			CommerceCurrencyTestUtil.addCommerceCurrency(_group.getCompanyId());

		_commerceCatalog = CommerceTestUtil.addCommerceCatalog(
			_group.getCompanyId(), _group.getGroupId(), _user.getUserId(),
			commerceCurrency.getCode());
	}

	@Test
	public void testGetURL() throws Exception {
		CPDefinition cpDefinition = CPTestUtil.addCPDefinition(
			_commerceCatalog.getGroupId());

		_cpDefinitionLocalService.updateCPDefinitionAccountGroupFilter(
			cpDefinition.getCPDefinitionId(), true);

		FileEntry fileEntry = _dlAppLocalService.addFileEntry(
			null, _user.getUserId(), _commerceCatalog.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString() + ".jpg", ContentTypes.IMAGE_JPEG,
			RandomTestUtil.randomBytes(), null, null, null, _serviceContext);

		CPAttachmentFileEntry cpAttachmentFileEntry =
			_cpAttachmentFileEntryLocalService.addCPAttachmentFileEntry(
				null, _user.getUserId(), _commerceCatalog.getGroupId(),
				_portal.getClassNameId(CPDefinition.class.getName()),
				cpDefinition.getCPDefinitionId(), fileEntry.getFileEntryId(),
				false, null, 1, 1, 2020, 1, 1, 2, 2, 2021, 2, 2, true, true,
				RandomTestUtil.randomLocaleStringMap(), null, 0D,
				CPAttachmentFileEntryConstants.TYPE_IMAGE, _serviceContext);

		AccountGroup accountGroup =
			CommerceAccountTestUtil.addAccountGroupAndAccountRel(
				_group.getCompanyId(), RandomTestUtil.randomString(),
				AccountConstants.ACCOUNT_GROUP_TYPE_STATIC,
				_accountEntry.getAccountEntryId(), _serviceContext);

		_accountGroupRelLocalService.addAccountGroupRel(
			accountGroup.getAccountGroupId(), CPDefinition.class.getName(),
			cpDefinition.getCPDefinitionId());

		String defaultURL = _commerceMediaResolver.getDefaultURL(
			cpAttachmentFileEntry.getGroupId());

		User user1 = UserTestUtil.addUser();
		User user2 = _userLocalService.getGuestUser(_group.getCompanyId());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user1)) {

			Assert.assertEquals(
				defaultURL,
				_commerceMediaResolver.getURL(
					_accountEntry.getAccountEntryId(),
					cpAttachmentFileEntry.getCPAttachmentFileEntryId()));
		}

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user2)) {

			Assert.assertEquals(
				defaultURL,
				_commerceMediaResolver.getURL(
					_accountEntry.getAccountEntryId(),
					cpAttachmentFileEntry.getCPAttachmentFileEntryId()));
			Assert.assertEquals(
				defaultURL,
				_commerceMediaResolver.getURL(
					AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
					cpAttachmentFileEntry.getCPAttachmentFileEntryId()));
		}

		_accountEntryUserRelLocalService.addAccountEntryUserRel(
			_accountEntry.getAccountEntryId(), user1.getUserId());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user1)) {

			Assert.assertEquals(
				_getURL(
					_accountEntry.getAccountEntryId(),
					cpAttachmentFileEntry.getCPAttachmentFileEntryId()),
				_commerceMediaResolver.getURL(
					_accountEntry.getAccountEntryId(),
					cpAttachmentFileEntry.getCPAttachmentFileEntryId()));
		}

		accountGroup = _accountGroupLocalService.getDefaultAccountGroup(
			_group.getCompanyId());

		_accountGroupRelLocalService.addAccountGroupRel(
			accountGroup.getAccountGroupId(), CPDefinition.class.getName(),
			cpDefinition.getCPDefinitionId());

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user2)) {

			Assert.assertEquals(
				_getURL(
					AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
					cpAttachmentFileEntry.getCPAttachmentFileEntryId()),
				_commerceMediaResolver.getURL(
					AccountConstants.ACCOUNT_ENTRY_ID_GUEST,
					cpAttachmentFileEntry.getCPAttachmentFileEntryId()));
		}
	}

	private String _getURL(long accountEntryId, long cpAttachmentFileEntryId) {
		return StringBundler.concat(
			_portal.getPathModule(), StringPool.SLASH,
			CommerceMediaConstants.SERVLET_PATH, "/accounts/", accountEntryId,
			"/images/", cpAttachmentFileEntryId, "?download=false");
	}

	private AccountEntry _accountEntry;

	@Inject
	private AccountEntryUserRelLocalService _accountEntryUserRelLocalService;

	@Inject
	private AccountGroupLocalService _accountGroupLocalService;

	@Inject
	private AccountGroupRelLocalService _accountGroupRelLocalService;

	private CommerceCatalog _commerceCatalog;

	@Inject
	private CommerceMediaResolver _commerceMediaResolver;

	@Inject
	private CPAttachmentFileEntryLocalService
		_cpAttachmentFileEntryLocalService;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Inject
	private DLAppLocalService _dlAppLocalService;

	private Group _group;

	@Inject
	private Portal _portal;

	private ServiceContext _serviceContext;
	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}