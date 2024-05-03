package com.liferay.commerce.client.extension.web.internal.display.context;

import com.liferay.commerce.constants.CommerceCheckoutWebKeys;
import com.liferay.commerce.constants.CommerceWebKeys;
import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.payment.service.CommercePaymentMethodGroupRelLocalService;
import com.liferay.petra.string.StringPool;

import javax.servlet.http.HttpServletRequest;

public class ClientExtensionCommerceCheckoutStepDisplayContext {

	public ClientExtensionCommerceCheckoutStepDisplayContext(
		HttpServletRequest httpServletRequest,
		CommercePaymentMethodGroupRelLocalService commercePaymentMethodGroupRelLocalService) {

		_cartResourceFactory = commercePaymentMethodGroupRelLocalService;

		CommerceContext commerceContext =
			(CommerceContext)httpServletRequest.getAttribute(
				CommerceWebKeys.COMMERCE_CONTEXT);

		_commerceOrder = commerceContext.getCommerceOrder();
	}

	public String getPaymentURL() throws Exception {
		CartResource.Builder cartResourceBuilder =
			_cartResourceFactory.create();

		CartResource cartResource = cartResourceBuilder.httpServletRequest(
			_commerceCheckoutRequestHelper.getRequest()
		).preferredLocale(
			_commerceCheckoutRequestHelper.getLocale()
		).user(
			_commerceCheckoutRequestHelper.getUser()
		).build();

		return cartResource.getCartPaymentURL(
			_commerceOrder.getCommerceOrderId(), StringPool.BLANK);
	}

	private final CartResource.Factory _cartResourceFactory;
	private final CommerceCheckoutRequestHelper _commerceCheckoutRequestHelper;
	private final CommerceOrder _commerceOrder;
}
