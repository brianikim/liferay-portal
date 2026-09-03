/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.content.web.internal.info.item.provider;

import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.permission.CommerceProductViewPermission;
import com.liferay.commerce.util.CommerceContextThreadLocal;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Brian I. Kim
 */
public class CPDefinitionInfoItemPermissionProviderTest {

	@ClassRule
	public static LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_cpDefinitionInfoItemPermissionProvider =
			new CPDefinitionInfoItemPermissionProvider();

		ReflectionTestUtil.setFieldValue(
			_cpDefinitionInfoItemPermissionProvider,
			"_commerceProductViewPermission", _commerceProductViewPermission);
		ReflectionTestUtil.setFieldValue(
			_cpDefinitionInfoItemPermissionProvider,
			"_layoutPageTemplateEntryLocalService",
			_layoutPageTemplateEntryLocalService);
		ReflectionTestUtil.setFieldValue(
			_cpDefinitionInfoItemPermissionProvider,
			"_layoutPageTemplateEntryModelResourcePermission",
			_layoutPageTemplateEntryModelResourcePermission);

		CommerceContext commerceContext = Mockito.mock(CommerceContext.class);

		CommerceContextThreadLocal.set(commerceContext);

		PermissionThreadLocal.setPermissionChecker(_permissionChecker);

		_setUpCommerceProductViewPermission();
		_setUpServiceContext();
	}

	@After
	public void tearDown() {
		CommerceContextThreadLocal.set(null);

		PermissionThreadLocal.setPermissionChecker(null);

		ServiceContextThreadLocal.popServiceContext();
	}

	@Test
	public void testHasPermissionWithLayoutPageTemplateEntry()
		throws Exception {

		_setUpLayoutPageTemplateEntryPermission(false);

		Assert.assertFalse(
			_cpDefinitionInfoItemPermissionProvider.hasPermission(
				_permissionChecker, _createCPDefinition(), ActionKeys.VIEW));

		_setUpLayoutPageTemplateEntryPermission(true);

		Assert.assertTrue(
			_cpDefinitionInfoItemPermissionProvider.hasPermission(
				_permissionChecker, _createCPDefinition(), ActionKeys.VIEW));
	}

	@Test
	public void testHasPermissionWithoutLayoutPageTemplateEntry()
		throws Exception {

		Mockito.doReturn(
			null
		).when(
			_layoutPageTemplateEntryLocalService
		).fetchLayoutPageTemplateEntryByPlid(
			Mockito.anyLong()
		);

		Assert.assertTrue(
			_cpDefinitionInfoItemPermissionProvider.hasPermission(
				_permissionChecker, _createCPDefinition(), ActionKeys.VIEW));
	}

	private CPDefinition _createCPDefinition() {
		CPDefinition cpDefinition = Mockito.mock(CPDefinition.class);

		Mockito.doReturn(
			RandomTestUtil.randomLong()
		).when(
			cpDefinition
		).getCPDefinitionId();

		return cpDefinition;
	}

	private void _setUpCommerceProductViewPermission() throws Exception {
		Mockito.doReturn(
			true
		).when(
			_commerceProductViewPermission
		).contains(
			Mockito.any(), Mockito.anyLong(), Mockito.anyLong(),
			Mockito.anyLong()
		);
	}

	private void _setUpLayoutPageTemplateEntryPermission(boolean contains)
		throws Exception {

		LayoutPageTemplateEntry layoutPageTemplateEntry = Mockito.mock(
			LayoutPageTemplateEntry.class);

		Mockito.doReturn(
			layoutPageTemplateEntry
		).when(
			_layoutPageTemplateEntryLocalService
		).fetchLayoutPageTemplateEntryByPlid(
			Mockito.anyLong()
		);

		Mockito.doReturn(
			contains
		).when(
			_layoutPageTemplateEntryModelResourcePermission
		).contains(
			Mockito.any(), Mockito.eq(layoutPageTemplateEntry),
			Mockito.anyString()
		);
	}

	private void _setUpServiceContext() {
		ServiceContext serviceContext = Mockito.mock(ServiceContext.class);

		ThemeDisplay themeDisplay = Mockito.mock(ThemeDisplay.class);

		Layout layout = Mockito.mock(Layout.class);

		Mockito.doReturn(
			RandomTestUtil.randomLong()
		).when(
			layout
		).getPlid();

		Mockito.doReturn(
			layout
		).when(
			themeDisplay
		).getLayout();

		Mockito.doReturn(
			themeDisplay
		).when(
			serviceContext
		).getThemeDisplay();

		ServiceContextThreadLocal.pushServiceContext(serviceContext);
	}

	private final CommerceProductViewPermission _commerceProductViewPermission =
		Mockito.mock(CommerceProductViewPermission.class);
	private CPDefinitionInfoItemPermissionProvider
		_cpDefinitionInfoItemPermissionProvider;
	private final LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService = Mockito.mock(
			LayoutPageTemplateEntryLocalService.class);
	private final ModelResourcePermission<LayoutPageTemplateEntry>
		_layoutPageTemplateEntryModelResourcePermission = Mockito.mock(
			ModelResourcePermission.class);
	private final PermissionChecker _permissionChecker = Mockito.mock(
		PermissionChecker.class);

}