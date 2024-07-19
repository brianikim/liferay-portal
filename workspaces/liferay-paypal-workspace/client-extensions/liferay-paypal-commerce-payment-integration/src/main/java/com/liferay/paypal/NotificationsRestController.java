/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.paypal;

import com.liferay.client.extension.util.spring.boot.LiferayOAuth2AccessTokenManager;
import com.liferay.petra.string.StringBundler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * @author Brian I. Kim
 */
@RequestMapping("/notifications")
@RestController
public class NotificationsRestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(
		@RequestHeader Map<String, String> headers, @RequestBody String json) {

		try {
			JSONObject payPalJSONObject = new JSONObject(json);

			if (!payPalJSONObject.isEmpty()) {
				if (!_hasAuthentication(
						headers, payPalJSONObject)) {

					return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
				}

				String errorMessages = null;
				String eventType = payPalJSONObject.getString("event_type");
				String paymentStatus = "4";

				if (StringUtils.equals(
					eventType,
					"PAYMENT.CAPTURE.COMPLETED")) {

					paymentStatus = "0";
				}
				else if (StringUtils.equals(
					eventType,
					"PAYMENT.CAPTURE.DENIED")) {

					paymentStatus = "4";
					errorMessages = payPalJSONObject.getString("summary");
				}
				else if (StringUtils.equals(
					eventType,
					"PAYMENT.CAPTURE.REFUNDED")) {

					paymentStatus = "17";
				}

				JSONObject payPalResourceJSONObject = payPalJSONObject.getJSONObject("resource");

				String paymentId = payPalResourceJSONObject.getString("id");

				if (StringUtils.isNotBlank(paymentId)) {
					_updatePayment(
						errorMessages, json, paymentId, paymentStatus);
				}
			}
		}
		catch (Exception exception) {
			_log.error(ExceptionUtils.getStackTrace(exception));

			return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
		}

		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	private boolean _hasAuthentication(
		Map<String, String> headers, JSONObject payPalJSONObject) {

		if (StringUtils.isBlank(authorization) &&
			!StringUtils.contains(authorization, "Basic")) {

			return false;
		}

		String[] authorizationParts = new String(
			Base64.getDecoder(
			).decode(
				authorization.substring(
					"Basic".length()
				).trim()
			),
			StandardCharsets.UTF_8
		).split(
			":", 2
		);

		String webhookPassword = authorizationParts[1];
		String webhookUserName = authorizationParts[0];

		if (webhookPassword.equals(
				adyenWebhookJSONObject.getString("webhookPassword")) &&
			webhookUserName.equals(
				adyenWebhookJSONObject.getString("webhookUsername"))) {

			return true;
		}

		return false;
	}

	private void _updatePayment(
		String errorMessages, String json, String paymentId,
		String paymentStatus) {

		patch(
			_liferayOAuth2AccessTokenManager.getAuthorization(
				"liferay-paypal-payment-integration-oauth-application-" +
					"headless-server"),
			new JSONObject(
			).put(
				"errorMessages", errorMessages
			).put(
				"payload",
				json
			).put(
				"paymentStatus", paymentStatus
			).toString(),
			"/o/headless-commerce-admin-payment/v1.0/payments/" + paymentId);
	}

	private static final Log _log = LogFactory.getLog(
		NotificationsRestController.class);

	@Autowired
	private LiferayOAuth2AccessTokenManager _liferayOAuth2AccessTokenManager;

}