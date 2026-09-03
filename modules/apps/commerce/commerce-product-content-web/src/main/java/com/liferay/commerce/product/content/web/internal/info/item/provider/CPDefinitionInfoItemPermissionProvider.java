/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.content.web.internal.info.item.provider;

import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.permission.CommerceProductViewPermission;
import com.liferay.commerce.util.CommerceContextThreadLocal;
import com.liferay.commerce.util.CommerceUtil;
import com.liferay.info.exception.InfoItemPermissionException;
import com.liferay.info.item.ClassPKInfoItemIdentifier;
import com.liferay.info.item.InfoItemIdentifier;
import com.liferay.info.item.InfoItemReference;
import com.liferay.info.item.provider.InfoItemPermissionProvider;
import com.liferay.layout.page.template.model.LayoutPageTemplateEntry;
import com.liferay.layout.page.template.service.LayoutPageTemplateEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alec Sloan
 * @author Alessio Antonio Rendina
 */
@Component(service = InfoItemPermissionProvider.class)
public class CPDefinitionInfoItemPermissionProvider
	implements InfoItemPermissionProvider<CPDefinition> {

	@Override
	public boolean hasPermission(
			PermissionChecker permissionChecker, CPDefinition cpDefinition,
			String actionId)
		throws InfoItemPermissionException {

		CommerceContext commerceContext = CommerceContextThreadLocal.get();

		if (commerceContext == null) {
			return false;
		}

		try {
			if (_commerceProductViewPermission.contains(
					permissionChecker,
					CommerceUtil.getCommerceAccountId(commerceContext),
					commerceContext.getCommerceChannelGroupId(),
					cpDefinition.getCPDefinitionId()) &&
				_hasLayoutPageTemplateEntryPermission(
					permissionChecker, actionId)) {

				return true;
			}

			return false;
		}
		catch (PortalException portalException) {
			throw new InfoItemPermissionException(
				cpDefinition.getCPDefinitionId(), portalException);
		}
	}

	@Override
	public boolean hasPermission(
			PermissionChecker permissionChecker,
			InfoItemReference infoItemReference, String actionId)
		throws InfoItemPermissionException {

		CommerceContext commerceContext = CommerceContextThreadLocal.get();

		if (commerceContext == null) {
			return false;
		}

		InfoItemIdentifier infoItemIdentifier =
			infoItemReference.getInfoItemIdentifier();

		if (!(infoItemIdentifier instanceof ClassPKInfoItemIdentifier)) {
			return false;
		}

		ClassPKInfoItemIdentifier classPKInfoItemIdentifier =
			(ClassPKInfoItemIdentifier)infoItemIdentifier;

		try {
			if (_commerceProductViewPermission.contains(
					permissionChecker,
					CommerceUtil.getCommerceAccountId(commerceContext),
					commerceContext.getCommerceChannelGroupId(),
					classPKInfoItemIdentifier.getClassPK()) &&
				_hasLayoutPageTemplateEntryPermission(
					permissionChecker, actionId)) {

				return true;
			}

			return false;
		}
		catch (PortalException portalException) {
			throw new InfoItemPermissionException(
				classPKInfoItemIdentifier.getClassPK(), portalException);
		}
	}

	private boolean _hasLayoutPageTemplateEntryPermission(
			PermissionChecker permissionChecker, String actionId)
		throws PortalException {

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		if (serviceContext == null) {
			return true;
		}

		ThemeDisplay themeDisplay = serviceContext.getThemeDisplay();

		if (themeDisplay == null) {
			return true;
		}

		Layout layout = themeDisplay.getLayout();

		if (layout == null) {
			return true;
		}

		LayoutPageTemplateEntry layoutPageTemplateEntry =
			_layoutPageTemplateEntryLocalService.
				fetchLayoutPageTemplateEntryByPlid(layout.getPlid());

		if (layoutPageTemplateEntry == null) {
			return true;
		}

		return _layoutPageTemplateEntryModelResourcePermission.contains(
			permissionChecker, layoutPageTemplateEntry, actionId);
	}

	@Reference
	private CommerceProductViewPermission _commerceProductViewPermission;

	@Reference
	private LayoutPageTemplateEntryLocalService
		_layoutPageTemplateEntryLocalService;

	@Reference(
		target = "(model.class.name=com.liferay.layout.page.template.model.LayoutPageTemplateEntry)"
	)
	private ModelResourcePermission<LayoutPageTemplateEntry>
		_layoutPageTemplateEntryModelResourcePermission;

}