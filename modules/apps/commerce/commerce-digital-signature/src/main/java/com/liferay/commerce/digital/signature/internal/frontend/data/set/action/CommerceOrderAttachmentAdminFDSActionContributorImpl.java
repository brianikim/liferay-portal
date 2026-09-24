/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.internal.frontend.data.set.action;

import com.liferay.commerce.constants.CommercePortletKeys;
import com.liferay.commerce.digital.signature.internal.helper.DSCommerceOrderAttachmentHelper;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderAttachment;
import com.liferay.commerce.order.CommerceOrderAttachmentAdminFDSActionContributor;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemBuilder;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemList;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.portlet.PortletURLFactory;
import com.liferay.portal.kernel.portlet.url.builder.ResourceURLBuilder;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.PortletRequest;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(service = CommerceOrderAttachmentAdminFDSActionContributor.class)
public class CommerceOrderAttachmentAdminFDSActionContributorImpl
	implements CommerceOrderAttachmentAdminFDSActionContributor {

	@Override
	public Map<String, Object> getAdditionalProps(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		if (!_dsCommerceOrderAttachmentHelper.isEnabled(commerceOrder)) {
			return Collections.emptyMap();
		}

		Map<Long, DSRequest> dsRequests =
			_dsCommerceOrderAttachmentHelper.getDSRequests(
				commerceOrder, httpServletRequest);

		return HashMapBuilder.<String, Object>put(
			"signatureRequest",
			() -> {
				if (!_hasUpdatePermission(commerceOrder, httpServletRequest)) {
					return null;
				}

				return HashMapBuilder.<String, Object>put(
					"accountUsers",
					JSONUtil.toJSONArray(
						_dsCommerceOrderAttachmentHelper.getAccountUsers(
							commerceOrder),
						user -> JSONUtil.put(
							"emailAddress", user.getEmailAddress()
						).put(
							"name", user.getFullName()
						).put(
							"userId", user.getUserId()
						))
				).put(
					"addDSRequestsURL",
					_getResourceURL(
						httpServletRequest, "/commerce_order/add_ds_requests")
				).put(
					"buyerUserId", commerceOrder.getUserId()
				).put(
					"commerceOrderId", commerceOrder.getCommerceOrderId()
				).put(
					"portletNamespace",
					_portal.getPortletNamespace(
						CommercePortletKeys.COMMERCE_ORDER)
				).put(
					"requestableIds",
					_getRequestableIds(commerceOrder, dsRequests)
				).put(
					"searchUsersURL",
					_getResourceURL(
						httpServletRequest,
						"/commerce_order/search_ds_request_users")
				).build();
			}
		).put(
			"signatureStatuses",
			_dsCommerceOrderAttachmentHelper.getSignatureStatuses(dsRequests)
		).build();
	}

	@Override
	public List<FDSActionDropdownItem> getBulkFDSActionDropdownItems(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		if (!_dsCommerceOrderAttachmentHelper.isEnabled(commerceOrder)) {
			return Collections.emptyList();
		}

		return FDSActionDropdownItemList.of(
			_getRequestSignatureFDSActionDropdownItem(httpServletRequest));
	}

	@Override
	public List<FDSActionDropdownItem> getFDSActionDropdownItems(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		if (!_dsCommerceOrderAttachmentHelper.isEnabled(commerceOrder)) {
			return Collections.emptyList();
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return FDSActionDropdownItemList.of(
			_getRequestSignatureFDSActionDropdownItem(httpServletRequest),
			FDSActionDropdownItemBuilder.putData(
				"signatureStatusURL",
				_dsCommerceOrderAttachmentHelper.getActionURL(
					"/commerce-digital-signature/signature-status",
					themeDisplay)
			).setHref(
				StringPool.POUND
			).setIcon(
				"list-ul"
			).setLabel(
				_language.get(httpServletRequest, "view-signature-status")
			).build(
				"view-signature-status"
			),
			FDSActionDropdownItemBuilder.putData(
				"resendURL",
				_getItemResourceURL(
					httpServletRequest, "/commerce_order/resend_ds_request")
			).setHref(
				StringPool.POUND
			).setIcon(
				"envelope-closed"
			).setLabel(
				_language.get(httpServletRequest, "resend")
			).setPermissionKey(
				"update"
			).build(
				"resend-signature-request"
			),
			FDSActionDropdownItemBuilder.putData(
				"voidURL",
				_getItemResourceURL(
					httpServletRequest, "/commerce_order/void_ds_request")
			).setHref(
				StringPool.POUND
			).setIcon(
				"times-circle"
			).setLabel(
				_language.get(httpServletRequest, "void")
			).setPermissionKey(
				"update"
			).build(
				"void-signature-request"
			));
	}

	private String _getItemResourceURL(
		HttpServletRequest httpServletRequest, String resourceID) {

		return ResourceURLBuilder.createResourceURL(
			_portletURLFactory.create(
				httpServletRequest, CommercePortletKeys.COMMERCE_ORDER,
				PortletRequest.RESOURCE_PHASE)
		).setParameter(
			"commerceOrderAttachmentId", "{id}"
		).setResourceID(
			resourceID
		).buildString();
	}

	private List<String> _getRequestableIds(
		CommerceOrder commerceOrder, Map<Long, DSRequest> dsRequests) {

		List<String> requestableIds = new ArrayList<>();

		for (CommerceOrderAttachment commerceOrderAttachment :
				_dsCommerceOrderAttachmentHelper.getCommerceOrderAttachments(
					commerceOrder)) {

			if (_dsCommerceOrderAttachmentHelper.isRequestable(
					dsRequests.get(
						commerceOrderAttachment.
							getCommerceOrderAttachmentId()))) {

				requestableIds.add(
					String.valueOf(
						commerceOrderAttachment.
							getCommerceOrderAttachmentId()));
			}
		}

		return requestableIds;
	}

	private FDSActionDropdownItem _getRequestSignatureFDSActionDropdownItem(
		HttpServletRequest httpServletRequest) {

		return FDSActionDropdownItemBuilder.setHref(
			StringPool.POUND
		).setIcon(
			"signature"
		).setLabel(
			_language.get(httpServletRequest, "request-signature")
		).setPermissionKey(
			"update"
		).build(
			"request-signature"
		);
	}

	private String _getResourceURL(
		HttpServletRequest httpServletRequest, String resourceID) {

		return ResourceURLBuilder.createResourceURL(
			_portletURLFactory.create(
				httpServletRequest, CommercePortletKeys.COMMERCE_ORDER,
				PortletRequest.RESOURCE_PHASE)
		).setResourceID(
			resourceID
		).buildString();
	}

	private boolean _hasUpdatePermission(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		try {
			return _commerceOrderModelResourcePermission.contains(
				themeDisplay.getPermissionChecker(), commerceOrder,
				ActionKeys.UPDATE);
		}
		catch (PortalException portalException) {
			throw new SystemException(portalException);
		}
	}

	@Reference(
		target = "(model.class.name=com.liferay.commerce.model.CommerceOrder)"
	)
	private ModelResourcePermission<CommerceOrder>
		_commerceOrderModelResourcePermission;

	@Reference
	private DSCommerceOrderAttachmentHelper _dsCommerceOrderAttachmentHelper;

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

	@Reference
	private PortletURLFactory _portletURLFactory;

}