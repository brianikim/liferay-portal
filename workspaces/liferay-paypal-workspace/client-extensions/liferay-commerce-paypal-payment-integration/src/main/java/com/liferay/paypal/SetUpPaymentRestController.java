/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.paypal;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;

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
 * @author Crescenzo Rega
 */
@RequestMapping("/set-up-payment")
@RestController
public class SetUpPaymentRestController extends BaseRestController {

	@GetMapping("get/{orderId}")
	public ResponseEntity<String> get(
		@AuthenticationPrincipal Jwt jwt,
		@PathVariable("orderId") long orderId) {

		JSONObject paymentJSONObject = new JSONObject(
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

		return new ResponseEntity<>(
			new JSONObject(
			).put(
				"id", _id
			).put(
				"entryId", paymentJSONObject.getLong("id")
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
				_getPaymentSource(commercePaymentEntryJSONObject)
			).put(
				"purchase_units", _getPurchaseUnit(jsonObject)
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

			_id = createOrderRequestJSONObject.getString("id");
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
			"cancel_url", "https://www.google.com/"
		).put(
			"return_url",
			commercePaymentEntryJSONObject.getString("callbackURL")
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

	private JSONArray _getPurchaseUnit(JSONObject jsonObject) {
		JSONArray jsonArray = new JSONArray();

		JSONObject amountObject = new JSONObject();

		amountObject.put("currency_code", "USD");
		amountObject.put("value", "100.00");

		JSONObject test = new JSONObject();

		test.put("amount", amountObject);
		test.put("reference_id", "d9f80740-38f0-11e8-b467-0ed5f89f718b");

		jsonArray.put(test);

		_log.fatal("purchase");
		_log.fatal(jsonArray.toString());

		return jsonArray;
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