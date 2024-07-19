/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.paypal;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;

import java.math.BigDecimal;

import java.util.Objects;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.liferay.portal.kernel.util.ArrayUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.apache.commons.lang3.StringUtils;
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
			new JSONObject().put(
				"entryId", _getCommercePaymentEntryId(jwt, orderId)
			).toString(),
			HttpStatus.OK);
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
				_getPaymentSource(commercePaymentEntryJSONObject, jwt)
			).put(
				"purchase_units",
				_getPurchaseUnit(
					commercePaymentEntryJSONObject,
					typeSettingsJSONObject.getString("merchantId"), jwt)
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
				"PayPal-Request-Id",
				commercePaymentEntryJSONObject.getString(
					"commercePaymentEntryId")
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
			paymentStatus = "18";

			delete(
				"Bearer " + jwt.getTokenValue(),
				"/o/c/n2a1paypalwebhooks/by-external-reference-code/" +
				commercePaymentEntryJSONObject.getString("classPK"));
		}
		catch (Exception exception) {
			errorMessages = ExceptionUtils.getStackTrace(exception);

			_log.error(errorMessages);
		}

		return new ResponseEntity<>(
			new JSONObject(
			).put(
				"errorMessages", errorMessages
			).put(
				"paymentStatus", paymentStatus
			).put(
				"transactionCode", transactionCode
			).toString(),
			HttpStatus.OK);
	}

	private JSONObject _getAmountJSONObject(
		JSONObject orderJSONObject, String currencyCode) {

		JSONObject amountJSONObject = new JSONObject();

		amountJSONObject.put(
			"breakdown",
			_getBreakdownJSONObject(orderJSONObject, currencyCode));
		amountJSONObject.put("currency_code", currencyCode);
		amountJSONObject.put(
			"value",
			BigDecimal.valueOf(
				orderJSONObject.getDouble("totalAmount")
			).longValue());

		return amountJSONObject;
	}

	private JSONObject _getBreakdownJSONObject(
		JSONObject orderJSONObject, String currencyCode) {

		JSONObject breakdownJSONObject = new JSONObject();

		breakdownJSONObject.put(
			"item_total",
			new JSONObject(
			).put(
				"currency_code", currencyCode
			).put(
				"value",
				BigDecimal.valueOf(
					orderJSONObject.getDouble("subtotalAmount")
				).longValue()
			));

		breakdownJSONObject.put(
			"shipping",
			new JSONObject(
			).put(
				"currency_code", currencyCode
			).put(
				"value",
				BigDecimal.valueOf(
					orderJSONObject.getDouble("shippingAmountValue")
				).longValue()
			));

		breakdownJSONObject.put(
			"tax_total",
			new JSONObject(
			).put(
				"currency_code", currencyCode
			).put(
				"value",
				BigDecimal.valueOf(
					orderJSONObject.getDouble("taxAmount")
				).longValue()
			));

		return breakdownJSONObject;
	}

	private String _getCommercePaymentEntryId(
		@AuthenticationPrincipal Jwt jwt,
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

			if (Objects.equals(
					itemjsonObject.getString("paymentStatus"), "18")) {

				return String.valueOf(itemjsonObject.getLong("id"));
			}
		}

		return null;
	}

	private JSONObject _getExperienceContext(
		long orderId, String cancelURL, String callbackURL) {

		JSONObject experienceContextJSONObject = new JSONObject();

		JSONObject payPalWebhookJSONObject = get("Bearer " + jwt.getTokenValue(),
				"/o/c/n2a1paypalwebhooks/by-external-reference-code/" +
				orderId);

		if (payPalWebhookJSONObject.has("fundingSource")) {
			String fundingSource = payPalWebhookJSONObject.getString("fundingSource");

			if (!Objects.equals(fundingSource, "google_pay") && !Objects.equals(fundingSource,"apple_pay")) {
				experienceContextJSONObject.put(
					"shipping_preference", "SET_PROVIDED_ADDRESS"
				);
			}

			if (ArrayUtil.contains(_funding_sources, fundingSource) || Objects.equals(_funding_sources, "paypal")) {
				experienceContextJSONObject.put(
					"cancel_url",
					cancelURL
				).put(
					"return_url",
					callbackURL
				);
			}

			if (fundingSource.equals("paypal")) {
				experienceContextJSONObject.put(
					"user_action", "PAY_NOW"
				);
			}
		}

		return experienceContextJSONObject;
	}

	private JSONArray _getItemsJSONArray(
		JSONObject orderJSONObject, String currencyCode, String languageId) {

		JSONArray itemsJSONArray = new JSONArray();

		JSONArray orderItemsJSONArray = orderJSONObject.getJSONArray(
			"orderItems");

		for (int i = 0; i < orderItemsJSONArray.length(); i++) {
			JSONObject orderItemJSONObject = orderItemsJSONArray.getJSONObject(
				i);

			BigDecimal finalPrice = BigDecimal.valueOf(
				orderItemJSONObject.getDouble("finalPrice"));
			long quantity = orderItemJSONObject.getLong("quantity");

			JSONObject tempItemJSONObject = new JSONObject();

			tempItemJSONObject.put(
				"unit_amount",
				new JSONObject(
				).put(
					"currency_code", currencyCode
				).put(
					"value",
					finalPrice.divide(
						BigDecimal.valueOf(quantity)
					).longValue()
				));

			String name = orderItemJSONObject.getJSONObject(
				"name"
			).getString(
				languageId
			);

			tempItemJSONObject.put("name", name);
			tempItemJSONObject.put(
				"quantity",
				BigDecimal.valueOf(
					quantity
				).stripTrailingZeros());
			tempItemJSONObject.put("sku", orderItemJSONObject.getString("sku"));

			itemsJSONArray.put(tempItemJSONObject);
		}

		return itemsJSONArray;
	}

	private JSONObject _getPaymentSource(
		JSONObject commercePaymentEntryJSONObject, Jwt jwt) {

		JSONObject paymentSourceJSONObject = new JSONObject();

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
					HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue()
				).retrieve(
				).bodyToMono(
					String.class
				).block()));

		paymentSourceJSONObject.put(
			_fundingSource,
			_getPayPalPaymentSource(
				commercePaymentEntryJSONObject, orderJSONObject));

		return paymentSourceJSONObject;
	}

	private JSONObject _getPayPalPaymentSource(
		JSONObject commercePaymentEntryJSONObject, JSONObject orderJSONObject) {

		JSONObject paymentSourceJSONObject = new JSONObject();

		if (ArrayUtil.contains(_funding_sources, _fundingSource)) {
			JSONObject shippingAddressJSONObject =
				orderJSONObject.getJSONObject("shippingAddress");

			paymentSourceJSONObject.put(
				"country_code",
				shippingAddressJSONObject.getString("countryISOCode")
			).put(
				"name", shippingAddressJSONObject.getString("name")
			);
		}

		paymentSourceJSONObject.put(
			"experience_context",
			_getExperienceContext(commercePaymentEntryJSONObject.getLong("classPK"), commercePaymentEntryJSONObject.getString("cancelURL"),
				commercePaymentEntryJSONObject.getString("callbackURL")));

		return paymentSourceJSONObject;
	}

	private JSONArray _getPurchaseUnit(
		JSONObject commercePaymentEntryJSONObject, String merchantId, Jwt jwt) {

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

			purchaseUnitJSONObject.put(
				"amount",
				_getAmountJSONObject(
					orderJSONObject,
					commercePaymentEntryJSONObject.getString("currencyCode")));
			purchaseUnitJSONObject.put(
				"items",
				_getItemsJSONArray(
					orderJSONObject,
					commercePaymentEntryJSONObject.getString("currencyCode"),
					commercePaymentEntryJSONObject.getString("languageId")));
			purchaseUnitJSONObject.put(
				"shipping",
				_getShippingJSONObject(
					orderJSONObject.getJSONObject("shippingAddress")));
		}

		purchaseUnitJSONObject.put(
			"description",
			"Payment: " +
				commercePaymentEntryJSONObject.getString(
					"commercePaymentEntryId"));
		purchaseUnitJSONObject.put(
			"payee",
			new JSONObject(
			).put(
				"merchant_id", merchantId
			));
		purchaseUnitJSONObject.put(
			"reference_id",
			commercePaymentEntryJSONObject.getString("commercePaymentEntryId"));

		purchaseUnitArray.put(purchaseUnitJSONObject);

		return purchaseUnitArray;
	}

	private JSONObject _getShippingJSONObject(
		JSONObject shippingAddressJSONObject) {

		JSONObject shippingJSONObject = new JSONObject();

		JSONObject nameJSONObject = new JSONObject();

		nameJSONObject.put(
			"full_name", shippingAddressJSONObject.getString("name"));

		shippingJSONObject.put("name", nameJSONObject);

		JSONObject addressJSONObject = new JSONObject();

		addressJSONObject.put(
			"address_line_1", shippingAddressJSONObject.getString("street1"));
		addressJSONObject.put(
			"address_line_2", shippingAddressJSONObject.getString("street2"));
		addressJSONObject.put(
			"admin_area_1",
			shippingAddressJSONObject.getString("regionISOCode"));
		addressJSONObject.put(
			"admin_area_2", shippingAddressJSONObject.getString("city"));
		addressJSONObject.put(
			"country_code",
			shippingAddressJSONObject.getString("countryISOCode"));
		addressJSONObject.put(
			"postal_code", shippingAddressJSONObject.getString("zip"));

		shippingJSONObject.put("address", addressJSONObject);

		return shippingJSONObject;
	}

	private static final Log _log = LogFactory.getLog(
		SetUpPaymentRestController.class);

	private static final String[] _funding_sources = {
		"bancontact", "eps", "giropay", "ideal", "mybank", "p24", "trustly"
	};
}