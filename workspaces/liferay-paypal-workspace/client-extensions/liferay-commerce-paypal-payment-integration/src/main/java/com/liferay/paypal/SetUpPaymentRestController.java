/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.paypal;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Brian I. Kim
 */
@RequestMapping("/set-up-payment")
@RestController
public class SetUpPaymentRestController extends BaseRestController {

	@GetMapping("get/{orderId}")
	public ResponseEntity<String> get(
		@AuthenticationPrincipal Jwt jwt,
		@PathVariable("orderId") long orderId) {

		return new ResponseEntity<>(
			new JSONObject(
			).put(
				"id", _id
			).put(
				"entryId", _getCommercePaymentEntryId(jwt, orderId)
			).toString(),
			HttpStatus.OK);
	}

	private String _getCommercePaymentEntryId(		@AuthenticationPrincipal Jwt jwt,
													  @PathVariable("orderId") long orderId) {

		JSONObject paymentsJSONObject = new JSONObject(
			Objects.requireNonNull(
				WebClient.create(
				).get(
				).uri(
					StringBundler.concat(
						lxcDXPServerProtocol, "://", lxcDXPMainDomain,
						"/o/headless-commerce-admin-payment/v1.0/payments/?filter=classPK eq ",
						orderId)
				).accept(
					MediaType.APPLICATION_JSON
				).header(
					HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue()
				).retrieve(
				).bodyToMono(
					String.class
				).block()));

		JSONArray itemsjsonArray = paymentsJSONObject.getJSONArray("items");

		for (int i = 0; i < itemsjsonArray.length(); i++) {
			JSONObject itemjsonObject = itemsjsonArray.getJSONObject(i);

			if (Objects.equals(itemjsonObject.getString("transactionCode"),
				_id)) {

				return String.valueOf(itemjsonObject.getLong("id"));
			}
		}

		return null;
	}


	@PostMapping
	public ResponseEntity<String> post(
		@AuthenticationPrincipal Jwt jwt, @RequestBody String json) {

		String errorMessages = null;
		String paymentStatus = "4";
		String transactionCode = null;

		try {
			JSONObject jsonObject = new JSONObject(json);

			JSONObject typeSettingsJSONObject = jsonObject.getJSONObject(
				"typeSettings");

			JSONObject payPalRequestJSONObject = new JSONObject();

			JSONObject commercePaymentEntryJSONObject =
				jsonObject.getJSONObject("commercePaymentEntry");

			payPalRequestJSONObject.put(
				"intent", "CAPTURE"
			).put(
				"payment_source",
				_getPaymentSource(commercePaymentEntryJSONObject)
			).put(
				"purchase_units", _getPurchaseUnit(commercePaymentEntryJSONObject, typeSettingsJSONObject.getString("merchantId"), jwt)
			);

			String createOrderRequest = WebClient.create(
				getEnvironmentURL(typeSettingsJSONObject.getString("mode"))
			).post(
			).uri(
				"/v2/checkout/orders"
			).accept(
				MediaType.APPLICATION_JSON
			).contentType(
				MediaType.APPLICATION_JSON
			).header(
				HttpHeaders.AUTHORIZATION,
				"Bearer " +
					getAuthorization(
						typeSettingsJSONObject.getString("mode"),
						typeSettingsJSONObject.getString("clientId"),
						typeSettingsJSONObject.getString("clientSecret"))
			).header(
				"PayPal-Partner-Attribution-Id", "Liferay_SP_PPCP_API"
			).header(
				"Prefer", "return=representation"
			).bodyValue(
				payPalRequestJSONObject.toString()
			).retrieve(
			).bodyToMono(
				String.class
			).block();

			JSONObject createOrderRequestJSONObject = new JSONObject(
				createOrderRequest);

			transactionCode = createOrderRequestJSONObject.getString("id");
		}
		catch (Exception exception) {
			errorMessages = ExceptionUtils.getStackTrace(exception);

			_log.error(errorMessages);
		}

		_id = transactionCode;

		return new ResponseEntity<>(
			new JSONObject(
			).put(
				"errorMessages", errorMessages
			).put(
				"paymentStatus", 18
			).put(
				"redirectURL", "LPD-20381"
			).put(
				"transactionCode", transactionCode
			).toString(),
			HttpStatus.OK);
	}

	private JSONObject _getExperienceContext(
		JSONObject commercePaymentEntryJSONObject) {

		JSONObject experienceContextJSONObject = new JSONObject();

		experienceContextJSONObject.put(
			"cancel_url", 			commercePaymentEntryJSONObject.getString("cancelURL")
		).put(
			"return_url",
			commercePaymentEntryJSONObject.getString("callbackURL")
		).put(
			"shipping_preference", "SET_PROVIDED_ADDRESS"
		).put(
			"user_action", "PAY_NOW"
		);

		return experienceContextJSONObject;
	}

	private JSONObject _getPaymentSource(
		JSONObject commercePaymentEntryJSONObject) {

		JSONObject paymentSourceJSONObject = new JSONObject();

		paymentSourceJSONObject.put(
			"paypal", _getPayPalPaymentSource(commercePaymentEntryJSONObject));

		_log.fatal("paymentSource");
		_log.fatal(paymentSourceJSONObject.toString());

		return paymentSourceJSONObject;
	}

	private JSONObject _getPayPalPaymentSource(
		JSONObject commercePaymentEntryJSONObject) {

		JSONObject payPalPaymentSourceJSONObject = new JSONObject();

		payPalPaymentSourceJSONObject.put(
			"experience_context",
			_getExperienceContext(commercePaymentEntryJSONObject));

		return payPalPaymentSourceJSONObject;
	}

	private JSONArray _getPurchaseUnit(JSONObject commercePaymentEntryJSONObject, String merchantId, Jwt jwt) {


		JSONArray purchaseUnitArray = new JSONArray();

		JSONObject purchaseUnitJSONObject = new JSONObject();

		if (Objects.equals(
			commercePaymentEntryJSONObject.getString("className"),
			"com.liferay.commerce.model.CommerceOrder")) {

			JSONObject orderJSONObject = new JSONObject(
				Objects.requireNonNull(
					WebClient.create(
					).get(
					).uri(
						StringBundler.concat(
							lxcDXPServerProtocol, "://", lxcDXPMainDomain,
							"/o/headless-commerce-admin-order/v1.0/orders/",
							commercePaymentEntryJSONObject.getLong("classPK"),
							"?nestedFields=orderItems,shippingAddress")
					).accept(
						MediaType.APPLICATION_JSON
					).header(
						HttpHeaders.AUTHORIZATION,
						"Bearer " + jwt.getTokenValue()
					).retrieve(
					).bodyToMono(
						String.class
					).block()));

			purchaseUnitJSONObject.put("shipping", _getShippingJSONObject(orderJSONObject.getJSONObject("shippingAddress")));
			purchaseUnitJSONObject.put("amount", _getAmountJSONObject(orderJSONObject, commercePaymentEntryJSONObject.getString("currencyCode")));
			purchaseUnitJSONObject.put("items",
				_getItemsJSONArray(orderJSONObject,
					commercePaymentEntryJSONObject.getString("currencyCode"), commercePaymentEntryJSONObject.getString("languageId")));
		}

		purchaseUnitJSONObject.put("description", "Payment: " + commercePaymentEntryJSONObject.getString("commercePaymentEntryId"));
		purchaseUnitJSONObject.put("payee", new JSONObject().put("merchant_id", merchantId));
		purchaseUnitJSONObject.put("reference_id", commercePaymentEntryJSONObject.getString("commercePaymentEntryId"));

		purchaseUnitArray.put(purchaseUnitJSONObject);

		return purchaseUnitArray;
	}

	private JSONArray _getItemsJSONArray(JSONObject orderJSONObject, String currencyCode, String languageId) {
		JSONArray itemsJSONArray = new JSONArray();

		JSONArray orderItemsJSONArray = orderJSONObject.getJSONArray("orderItems");

		for (int i = 0; i < orderItemsJSONArray.length(); i++) {
			JSONObject orderItemJSONObject = orderItemsJSONArray.getJSONObject(
				i);

			BigDecimal finalPrice = BigDecimal.valueOf(
				orderItemJSONObject.getDouble("finalPrice"));
			long quantity = orderItemJSONObject.getLong("quantity");

			JSONObject tempItemJSONObject = new JSONObject();

			tempItemJSONObject.put("unit_amount", new JSONObject().put("currency_code", currencyCode).put("value", 			finalPrice.divide(
				BigDecimal.valueOf(quantity)
			).longValue()));

			String name = orderItemJSONObject.getJSONObject(
				"name"
			).getString(
				languageId
			);

			tempItemJSONObject.put("sku", orderItemJSONObject.getString("sku"));
			tempItemJSONObject.put("name", name);
			tempItemJSONObject.put("quantity", 				BigDecimal.valueOf(quantity).stripTrailingZeros());

			itemsJSONArray.put(tempItemJSONObject);
		}

		return itemsJSONArray;
	}

	private JSONObject _getAmountJSONObject(JSONObject orderJSONObject, String currencyCode) {
		JSONObject amountJSONObject = new JSONObject();

		amountJSONObject.put("currency_code", currencyCode);
		amountJSONObject.put(
			"value", 					BigDecimal.valueOf(
				orderJSONObject.getDouble("totalAmount")
			).longValue());
		amountJSONObject.put("breakdown", _getBreakdownJSONObject(orderJSONObject, currencyCode));


		return amountJSONObject;
	}

	private JSONObject _getBreakdownJSONObject(JSONObject orderJSONObject, String currencyCode) {
		JSONObject breakdownJSONObject = new JSONObject();

		breakdownJSONObject.put("item_total", new JSONObject().put("currency_code", currencyCode).put(			"value", 					BigDecimal.valueOf(
			orderJSONObject.getDouble("subtotalAmount")
		).longValue()));

		breakdownJSONObject.put("shipping", new JSONObject().put("currency_code", currencyCode).put(			"value", 					BigDecimal.valueOf(
			orderJSONObject.getDouble("shippingAmountValue")
		).longValue()));

		breakdownJSONObject.put("tax_total", new JSONObject().put("currency_code", currencyCode).put(			"value", 					BigDecimal.valueOf(
			orderJSONObject.getDouble("taxAmount")
		).longValue()));

		return breakdownJSONObject;
	}

	private JSONObject _getShippingJSONObject(JSONObject shippingAddressJSONObject) {
		JSONObject shippingJSONObject = new JSONObject();


		JSONObject nameJSONObject = new JSONObject();
		nameJSONObject.put("full_name", shippingAddressJSONObject.getString("name"));

		shippingJSONObject.put("name", nameJSONObject);

		JSONObject addressJSONObject = new JSONObject();
		addressJSONObject.put("address_line_1", shippingAddressJSONObject.getString("street1"));
		addressJSONObject.put("address_line_2", shippingAddressJSONObject.getString("street2"));
		addressJSONObject.put("admin_area_2", shippingAddressJSONObject.getString("city"));
		addressJSONObject.put("admin_area_1", shippingAddressJSONObject.getString("regionISOCode"));
		addressJSONObject.put("postal_code", shippingAddressJSONObject.getString("zip"));
		addressJSONObject.put("country_code", shippingAddressJSONObject.getString("countryISOCode"));

		shippingJSONObject.put("address", addressJSONObject);

		return shippingJSONObject;
	}

	private String _getRedirectURL(JSONArray jsonArray) {
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);

			String rel = jsonObject.getString("rel");

			if (rel.equals("payer-action")) {
				return jsonObject.getString("href");
			}
		}

		return StringPool.BLANK;
	}

	private static final Log _log = LogFactory.getLog(
		SetUpPaymentRestController.class);

}