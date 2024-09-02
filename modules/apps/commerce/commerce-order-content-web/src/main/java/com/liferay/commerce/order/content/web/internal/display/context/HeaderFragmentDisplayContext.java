/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.order.content.web.internal.display.context;

import com.liferay.account.model.AccountEntry;
import com.liferay.commerce.configuration.CommerceOrderFieldsConfiguration;
import com.liferay.commerce.constants.CommerceConstants;
import com.liferay.commerce.constants.CommerceOrderActionKeys;
import com.liferay.commerce.constants.CommerceOrderConstants;
import com.liferay.commerce.constants.CommerceOrderPaymentConstants;
import com.liferay.commerce.constants.CommercePaymentMethodConstants;
import com.liferay.commerce.constants.CommercePortletKeys;
import com.liferay.commerce.constants.CommerceWebKeys;
import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.frontend.model.HeaderActionModel;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.model.CommerceOrderItemModel;
import com.liferay.commerce.order.CommerceOrderHttpHelper;
import com.liferay.commerce.order.engine.CommerceOrderEngine;
import com.liferay.commerce.order.status.CommerceOrderStatus;
import com.liferay.commerce.payment.constants.CommercePaymentIntegrationConstants;
import com.liferay.commerce.payment.integration.CommercePaymentIntegration;
import com.liferay.commerce.payment.integration.CommercePaymentIntegrationRegistry;
import com.liferay.commerce.payment.method.CommercePaymentMethod;
import com.liferay.commerce.payment.method.CommercePaymentMethodRegistry;
import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRel;
import com.liferay.commerce.payment.service.CommercePaymentMethodGroupRelLocalService;
import com.liferay.commerce.product.display.context.helper.CPRequestHelper;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Brian I. Kim
 */
public class HeaderFragmentDisplayContext {

	public HeaderFragmentDisplayContext(
			CommerceChannelLocalService commerceChannelLocalService,
			CommerceOrderEngine commerceOrderEngine,
			CommerceOrderHttpHelper commerceOrderHttpHelper,
			CommerceOrderService commerceOrderService,
			CommercePaymentIntegrationRegistry
				commercePaymentIntegrationRegistry,
			CommercePaymentMethodGroupRelLocalService
				commercePaymentMethodGroupRelLocalService,
			CommercePaymentMethodRegistry commercePaymentMethodRegistry,
			ConfigurationProvider configurationProvider,
			HttpServletRequest httpServletRequest, Portal portal,
			PortletResourcePermission portletResourcePermission)
		throws PortalException {

		_commerceChannelLocalService = commerceChannelLocalService;
		_commerceOrderEngine = commerceOrderEngine;
		_commerceOrderHttpHelper = commerceOrderHttpHelper;
		_commerceOrderService = commerceOrderService;
		_commercePaymentIntegrationRegistry =
			commercePaymentIntegrationRegistry;
		_commercePaymentMethodGroupRelLocalService =
			commercePaymentMethodGroupRelLocalService;
		_commercePaymentMethodRegistry = commercePaymentMethodRegistry;
		_configurationProvider = configurationProvider;
		_httpServletRequest = httpServletRequest;
		_portal = portal;
		_portletResourcePermission = portletResourcePermission;

		_cpRequestHelper = new CPRequestHelper(httpServletRequest);

		_themeDisplay = _cpRequestHelper.getThemeDisplay();

		_commerceContext = (CommerceContext)httpServletRequest.getAttribute(
			CommerceWebKeys.COMMERCE_CONTEXT);

		_accountEntry = _commerceContext.getAccountEntry();
	}

	public CommerceChannel fetchCommerceChannel() {
		return _commerceChannelLocalService.fetchCommerceChannelBySiteGroupId(
			_cpRequestHelper.getScopeGroupId());
	}

	public AccountEntry getAccountEntry() {
		return _accountEntry;
	}

	public String getCheckoutOrderURL() throws Exception {
		CommerceOrder commerceOrder = getCommerceOrder();

		if (ListUtil.exists(
				commerceOrder.getCommerceOrderItems(),
				CommerceOrderItemModel::isPriceOnApplication)) {

			return PortletURLBuilder.create(
				_commerceOrderHttpHelper.getCommerceCartPortletURL(
					_httpServletRequest, commerceOrder)
			).toString();
		}

		return PortletURLBuilder.create(
			_commerceOrderHttpHelper.getCommerceCheckoutPortletURL(
				_httpServletRequest)
		).setParameter(
			"commerceOrderId", commerceOrder.getCommerceOrderId()
		).buildString();
	}

	public long getCommerceAccountId() {
		long accountEntryId = 0;

		if (_accountEntry != null) {
			accountEntryId = _accountEntry.getAccountEntryId();
		}

		return accountEntryId;
	}

	public CommerceOrder getCommerceOrder() throws PortalException {
		long commerceOrderId = getCommerceOrderId();

		if (commerceOrderId > 0) {
			return _commerceOrderService.fetchCommerceOrder(
				getCommerceOrderId());
		}

		return _commerceOrderService.fetchCommerceOrder(
			ParamUtil.getString(_httpServletRequest, "commerceOrderUuid"),
			_cpRequestHelper.getCommerceChannelGroupId());
	}

	public long getCommerceOrderId() {
		return ParamUtil.getLongValues(
			_httpServletRequest,
			"_com_liferay_commerce_order_content_web_internal_portlet_" +
				"CommerceOpenOrderContentPortlet_commerceOrderId")[0];
	}

	public List<HeaderActionModel> getHeaderActionModels() throws Exception {
		List<HeaderActionModel> headerActionModels = new ArrayList<>();

		CommerceOrder commerceOrder = getCommerceOrder();

		if (isShowRetryPayment()) {
			headerActionModels.add(
				new HeaderActionModel(
					"btn-primary", null, getRetryPaymentURL(), null,
					"retry-payment"));
		}

		CommerceOrderStatus currentCommerceOrderStatus =
			_commerceOrderEngine.getCurrentCommerceOrderStatus(commerceOrder);

		if ((currentCommerceOrderStatus == null) ||
			!currentCommerceOrderStatus.isComplete(commerceOrder) ||
			(currentCommerceOrderStatus.getKey() ==
				CommerceOrderConstants.ORDER_STATUS_CANCELLED) ||
			(currentCommerceOrderStatus.getKey() ==
				CommerceOrderConstants.ORDER_STATUS_IN_PROGRESS)) {

			return headerActionModels;
		}

		if (!commerceOrder.isOpen()) {
			headerActionModels.add(
				new HeaderActionModel(
					"btn-primary", null, getOrderDetailURL(), null, "reorder"));
		}

		List<CommerceOrderStatus> commerceOrderStatuses =
			_commerceOrderEngine.getNextCommerceOrderStatuses(commerceOrder);

		for (CommerceOrderStatus commerceOrderStatus : commerceOrderStatuses) {
			if ((commerceOrderStatus.getKey() ==
					CommerceOrderConstants.ORDER_STATUS_SHIPPED) ||
				!commerceOrderStatus.isValidForOrder(commerceOrder) ||
				!commerceOrderStatus.isTransitionCriteriaMet(commerceOrder)) {

				continue;
			}

			String buttonCssClass = null;
			String id = null;
			String label;
			String href = null;

			if (commerceOrderStatus.getKey() ==
					CommerceOrderConstants.ORDER_STATUS_IN_PROGRESS) {

				if (!hasPermission(
						CommerceOrderActionKeys.
							CHECKOUT_OPEN_COMMERCE_ORDERS)) {

					continue;
				}

				label = "checkout";
				href = getCheckoutOrderURL();

				if (!commerceOrder.isApproved()) {
					label = "submit";

					/*					href = PortletURLBuilder.create(
											getTransitionOrderPortletURL(commerceOrder)
										).setParameter(
											"transitionName", label
										).buildString();*/
				}
			}
			else if ((commerceOrderStatus.getKey() ==
						CommerceOrderConstants.ORDER_STATUS_QUOTE_REQUESTED) &&
					 isRequestQuoteEnabled()) {

				if (!isValidCommerceOrder()) {
					continue;
				}

				buttonCssClass = "btn-primary request-quote";
				id = "requestQuote";
				label = "request-a-quote";
			}
			else if (commerceOrderStatus.getKey() ==
						CommerceOrderConstants.ORDER_STATUS_PROCESSING) {

				if (!hasPermission(
						CommerceOrderActionKeys.APPROVE_OPEN_COMMERCE_ORDERS)) {

					continue;
				}

				label = "accept-order";
			}
			else if (commerceOrderStatus.getKey() ==
						CommerceOrderConstants.ORDER_STATUS_QUOTE_PROCESSED) {

				label = "process-quote";
			}
			else {
				continue;
			}

			if (Validator.isNull(buttonCssClass)) {
				buttonCssClass = "btn-primary";
			}

			if (commerceOrderStatus.getPriority() ==
					CommerceOrderConstants.ORDER_STATUS_ANY) {

				buttonCssClass = "btn-secondary";
			}

			headerActionModels.add(
				new HeaderActionModel(buttonCssClass, null, href, id, label));
		}

		return headerActionModels;
	}

	public String getOrderDetailURL() throws PortalException {
		PortletURL portletURL = null;

		long groupId = _cpRequestHelper.getScopeGroupId();

		// Identify correct order details url and just use that as
		// the href rather than doing below plid
		// search for OPEN_ORDER_CONTENT portlet

		long plid = _portal.getPlidFromPortletId(
			groupId, CommercePortletKeys.COMMERCE_OPEN_ORDER_CONTENT);

		if (plid > 0) {
			portletURL = PortletProviderUtil.getPortletURL(
				_httpServletRequest, CommerceOrder.class.getName(),
				PortletProvider.Action.EDIT);

			if (getCommerceOrder() != null) {
				portletURL.setParameter(
					"mvcRenderCommandName",
					"/commerce_open_order_content/edit_commerce_order");
				portletURL.setParameter(
					"commerceOrderId", String.valueOf(getCommerceOrderId()));

				String backURL = ParamUtil.getString(
					_httpServletRequest, "backURL");

				portletURL.setParameter("backURL", backURL);
			}

			return portletURL.toString();
		}

		plid = _portal.getPlidFromPortletId(
			groupId, CommercePortletKeys.COMMERCE_CART_CONTENT);

		if (plid > 0) {
			portletURL = PortletURLBuilder.createLiferayPortletURL(
				_cpRequestHelper.getLiferayPortletResponse(), plid,
				CommercePortletKeys.COMMERCE_CART_CONTENT,
				PortletRequest.RENDER_PHASE
			).setParameter(
				"commerceOrderId", getCommerceOrderId()
			).buildPortletURL();
		}

		return portletURL.toString();
	}

	public String getRetryPaymentURL() throws PortalException {
		return PortletURLBuilder.create(
			_commerceOrderHttpHelper.getCommerceCheckoutPortletURL(
				_cpRequestHelper.getRequest())
		).setParameter(
			"checkoutStepName", "payment-process"
		).setParameter(
			"commerceOrderUuid",
			() -> {
				CommerceOrder commerceOrder = getCommerceOrder();

				return commerceOrder.getUuid();
			}
		).buildString();
	}

	public PortletURL getTransitionOrderPortletURL(
		CommerceOrder commerceOrder) {

		return PortletURLBuilder.createActionURL(
			_cpRequestHelper.getLiferayPortletResponse()
		).setActionName(
			"/commerce_open_order_content/edit_commerce_order"
		).setCMD(
			"transition"
		).setRedirect(
			_cpRequestHelper.getCurrentURL()
		).setParameter(
			"commerceOrderId", commerceOrder.getCommerceOrderId()
		).buildPortletURL();
	}

	public boolean hasPermission(String actionId) {
		return _portletResourcePermission.contains(
			_cpRequestHelper.getPermissionChecker(),
			_cpRequestHelper.getScopeGroupId(), actionId);
	}

	public boolean isRequestQuoteEnabled() throws PortalException {
		CommerceOrderFieldsConfiguration commerceOrderFieldsConfiguration =
			_getCommerceOrderFieldsConfiguration();

		if (commerceOrderFieldsConfiguration == null) {
			return false;
		}

		return commerceOrderFieldsConfiguration.requestQuoteEnabled();
	}

	public boolean isShowRetryPayment() throws PortalException {
		CommerceOrder commerceOrder = getCommerceOrder();

		if (_hasOrderStatusInProgress(commerceOrder.getOrderStatus()) &&
			_hasPaymentStatusRetryPayment(commerceOrder.getPaymentStatus()) &&
			_isCommercePaymentMethodOnline(
				commerceOrder.getCommercePaymentMethodKey()) &&
			_isCommercePaymentMethodActive(
				commerceOrder.getCommercePaymentMethodKey(),
				commerceOrder.getGroupId())) {

			return true;
		}

		return false;
	}

	public boolean isValidCommerceOrder() throws PortalException {
		CommerceOrder commerceOrder = getCommerceOrder();

		if (commerceOrder == null) {
			return false;
		}

		List<CommerceOrderItem> commerceOrderItems =
			commerceOrder.getCommerceOrderItems();

		if (commerceOrderItems.isEmpty()) {
			return false;
		}

		return true;
	}

	private CommerceOrderFieldsConfiguration
			_getCommerceOrderFieldsConfiguration()
		throws PortalException {

		if (_commerceOrderFieldsConfiguration != null) {
			return _commerceOrderFieldsConfiguration;
		}

		CommerceChannel commerceChannel = fetchCommerceChannel();

		if (commerceChannel == null) {
			return null;
		}

		_commerceOrderFieldsConfiguration =
			_configurationProvider.getConfiguration(
				CommerceOrderFieldsConfiguration.class,
				new GroupServiceSettingsLocator(
					commerceChannel.getGroupId(),
					CommerceConstants.SERVICE_NAME_COMMERCE_ORDER_FIELDS));

		return _commerceOrderFieldsConfiguration;
	}

	private boolean _hasOrderStatusInProgress(int orderStatus) {
		if (CommerceOrderConstants.ORDER_STATUS_IN_PROGRESS == orderStatus) {
			return true;
		}

		return false;
	}

	private boolean _hasPaymentStatusRetryPayment(int paymentStatus) {
		return ArrayUtil.contains(
			CommerceOrderPaymentConstants.STATUSES_RETRY_PAYMENT,
			paymentStatus);
	}

	private boolean _isCommercePaymentMethodActive(
			String commercePaymentMethodKey, long groupId)
		throws PortalException {

		CommercePaymentMethodGroupRel commercePaymentMethodGroupRel =
			_commercePaymentMethodGroupRelLocalService.
				getCommercePaymentMethodGroupRel(
					groupId, commercePaymentMethodKey);

		return commercePaymentMethodGroupRel.isActive();
	}

	private boolean _isCommercePaymentMethodOnline(
		String commercePaymentMethodKey) {

		CommercePaymentMethod commercePaymentMethod =
			_commercePaymentMethodRegistry.getCommercePaymentMethod(
				commercePaymentMethodKey);

		if (commercePaymentMethod != null) {
			return ArrayUtil.contains(
				CommercePaymentMethodConstants.TYPES_ONLINE,
				commercePaymentMethod.getPaymentType());
		}

		CommercePaymentIntegration commercePaymentIntegration =
			_commercePaymentIntegrationRegistry.getCommercePaymentIntegration(
				commercePaymentMethodKey);

		if (commercePaymentIntegration != null) {
			return ArrayUtil.contains(
				CommercePaymentIntegrationConstants.TYPES_ONLINE,
				commercePaymentIntegration.getPaymentIntegrationType());
		}

		return false;
	}

	private final AccountEntry _accountEntry;
	private final CommerceChannelLocalService _commerceChannelLocalService;
	private final CommerceContext _commerceContext;
	private final CommerceOrderEngine _commerceOrderEngine;
	private CommerceOrderFieldsConfiguration _commerceOrderFieldsConfiguration;
	private final CommerceOrderHttpHelper _commerceOrderHttpHelper;
	private final CommerceOrderService _commerceOrderService;
	private final CommercePaymentIntegrationRegistry
		_commercePaymentIntegrationRegistry;
	private final CommercePaymentMethodGroupRelLocalService
		_commercePaymentMethodGroupRelLocalService;
	private final CommercePaymentMethodRegistry _commercePaymentMethodRegistry;
	private final ConfigurationProvider _configurationProvider;
	private final CPRequestHelper _cpRequestHelper;
	private final HttpServletRequest _httpServletRequest;
	private final Portal _portal;
	private final PortletResourcePermission _portletResourcePermission;
	private final ThemeDisplay _themeDisplay;

}