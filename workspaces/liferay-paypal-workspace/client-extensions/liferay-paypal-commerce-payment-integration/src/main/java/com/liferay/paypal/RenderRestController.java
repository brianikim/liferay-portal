/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.paypal;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

import java.util.Objects;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Brian I. Kim
 */
@RequestMapping("/render")
@RestController
public class RenderRestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(
		@AuthenticationPrincipal Jwt jwt, @RequestBody String json) {

		log(jwt, _log);

		JSONObject jsonObject = new JSONObject(json);

		if (!jsonObject.has("orderId")) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		long orderId = jsonObject.getLong("orderId");

		StringBuilder sb = new StringBuilder(
			get(
				HashMapBuilder.put(
					HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE
				).put(
					HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue()
				).build(),
				UriComponentsBuilder.fromPath(
					"/o/headless-commerce-delivery-cart/v1.0/carts/" + orderId +
						"/payment-url"
				).build(
				).toUri()));

		if (jsonObject.has("callbackURL")) {
			_appendQueryParameter(
				sb, "callbackURL", jsonObject.getString("callbackURL"));
		}

		if (jsonObject.has("cancel")) {
			_appendQueryParameter(
				sb, "cancel", String.valueOf(jsonObject.getBoolean("cancel")));

			String transactionCode = jsonObject.getString("transactionCode");

			try {
				delete(
					"Bearer " + jwt.getTokenValue(), StringPool.BLANK,
					UriComponentsBuilder.fromPath(
						"/o/c/b9k3paypalwebhooks/by-external-reference-code/" +
							transactionCode
					).build(
					).toUri());
			}
			catch (Exception exception) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to delete the webhook record for transaction " +
							transactionCode,
						exception);
				}
			}
		}

		if (jsonObject.has("transactionCode")) {
			_appendQueryParameter(
				sb, "entryId",
				_getPaymentEntryId(
					jwt, orderId, jsonObject.getString("transactionCode")));
		}

		if (jsonObject.has("fundingSource")) {
			_appendQueryParameter(
				sb, "fundingSource", jsonObject.getString("fundingSource"));
		}

		if (jsonObject.has("redirect")) {
			_appendQueryParameter(
				sb, "redirect",
				String.valueOf(jsonObject.getBoolean("redirect")));
		}

		return new ResponseEntity<>(
			new JSONObject(
			).put(
				"url", sb.toString()
			).toString(),
			HttpStatus.OK);
	}

	private void _appendQueryParameter(
		StringBuilder sb, String name, String value) {

		if (sb.indexOf(StringPool.QUESTION) < 0) {
			sb.append(StringPool.QUESTION);
		}
		else {
			sb.append(StringPool.AMPERSAND);
		}

		sb.append(name);
		sb.append(StringPool.EQUAL);
		sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
	}

	private String _getPaymentEntryId(
		Jwt jwt, long orderId, String transactionCode) {

		JSONObject paymentsJSONObject = new JSONObject(
			get(
				"Bearer " + jwt.getTokenValue(),
				UriComponentsBuilder.fromPath(
					"/o/headless-commerce-admin-payment/v1.0/payments/"
				).queryParam(
					"filter", "relatedItemId eq " + orderId
				).build(
				).toUri()));

		JSONArray itemsJSONArray = paymentsJSONObject.getJSONArray("items");

		for (int i = 0; i < itemsJSONArray.length(); i++) {
			JSONObject itemJSONObject = itemsJSONArray.getJSONObject(i);

			if (Objects.equals(
					transactionCode,
					itemJSONObject.getString("transactionCode"))) {

				return String.valueOf(itemJSONObject.getInt("id"));
			}
		}

		return StringPool.BLANK;
	}

	private static final Log _log = LogFactory.getLog(
		RenderRestController.class);

}