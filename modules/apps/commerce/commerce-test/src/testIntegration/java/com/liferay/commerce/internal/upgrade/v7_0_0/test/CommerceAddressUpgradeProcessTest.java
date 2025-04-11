/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.internal.upgrade.v7_0_0.test;

import com.liferay.account.constants.AccountListTypeConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.constants.CommerceAddressConstants;
import com.liferay.commerce.model.CommerceAddress;
import com.liferay.commerce.service.CommerceAddressLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.EntityCacheUtil;
import com.liferay.portal.kernel.model.Address;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.ListType;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.AddressLocalService;
import com.liferay.portal.kernel.service.CountryLocalService;
import com.liferay.portal.kernel.service.ListTypeLocalService;
import com.liferay.portal.kernel.service.RegionLocalService;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.portal.upgrade.test.util.UpgradeTestUtil;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Brian I. Kim
 */
@RunWith(Arquillian.class)
public class CommerceAddressUpgradeProcessTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testUpdateCommerceAddress() throws Exception {
		_company = CompanyTestUtil.addCompany();

		CommerceAddress commerceAddress = _addCommerceAddress(
			_company.getCompanyId(), _company.getGroupId());

		_runUpgrade();

		EntityCacheUtil.clearCache();

		Address address =
			_addressLocalService.fetchAddressByExternalReferenceCode(
				String.valueOf(commerceAddress.getCommerceAddressId()),
				_company.getCompanyId());

		Assert.assertNotNull(address);

		Assert.assertEquals(
			commerceAddress.getClassNameId(), address.getClassNameId());
		Assert.assertEquals(commerceAddress.getClassPK(), address.getClassPK());
		Assert.assertEquals(
			commerceAddress.getCompanyId(), address.getCompanyId());
		Assert.assertEquals(commerceAddress.getName(), address.getName());
		Assert.assertEquals(commerceAddress.getUserId(), address.getUserId());

		ListType listType = _listTypeLocalService.getListType(
			_company.getCompanyId(),
			AccountListTypeConstants.
				ACCOUNT_ENTRY_ADDRESS_TYPE_BILLING_AND_SHIPPING,
			AccountListTypeConstants.ACCOUNT_ENTRY_ADDRESS);

		Assert.assertEquals(listType.getListTypeId(), address.getListTypeId());
	}

	private CommerceAddress _addCommerceAddress(long companyId, long groupId)
		throws Exception {

		Country country = _countryLocalService.fetchCountryByA2(
			companyId, "US");

		Region region = _regionLocalService.getRegion(
			country.getCountryId(), "CA");

		User user = UserTestUtil.getAdminUser(_company.getCompanyId());

		AccountEntry accountEntry =
			CommerceAccountTestUtil.addBusinessAccountEntry(
				user.getUserId(), "Test Business Account", null, null,
				new long[] {user.getUserId()}, null,
				ServiceContextTestUtil.getServiceContext(groupId));

		return _commerceAddressLocalService.addCommerceAddress(
			StringPool.BLANK, AccountEntry.class.getName(),
			accountEntry.getAccountEntryId(), country.getCountryId(),
			region.getRegionId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			StringPool.BLANK,
			CommerceAddressConstants.ADDRESS_TYPE_BILLING_AND_SHIPPING,
			RandomTestUtil.randomString(),
			ServiceContextTestUtil.getServiceContext(groupId));
	}

	private void _runUpgrade() throws Exception {
		UpgradeProcess upgradeProcess = UpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);

		upgradeProcess.upgrade();
	}

	private static final String _CLASS_NAME =
		"com.liferay.commerce.internal.upgrade.v7_0_0." +
			"CommerceAddressUpgradeProcess";

	@Inject
	private static AddressLocalService _addressLocalService;

	@Inject
	private static CommerceAddressLocalService _commerceAddressLocalService;

	@Inject
	private static CountryLocalService _countryLocalService;

	@Inject
	private static ListTypeLocalService _listTypeLocalService;

	@Inject
	private static RegionLocalService _regionLocalService;

	@Inject(
		filter = "(&(component.name=com.liferay.commerce.internal.upgrade.registry.CommerceServiceUpgradeStepRegistrator))"
	)
	private static UpgradeStepRegistrator _upgradeStepRegistrator;

	@DeleteAfterTestRun
	private Company _company;

}