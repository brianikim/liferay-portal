package com.liferay.paypal;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringBundler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.liferay.client.extension.util.spring.boot.LiferayOAuth2AccessTokenManager;
import org.springframework.web.reactive.function.client.WebClient;

@RequestMapping("/render")
@RestController
public class RenderRestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(
		@AuthenticationPrincipal Jwt jwt, @RequestBody String json) {

		log(jwt, _log);

		JSONObject jsonObject = new JSONObject(json);

		String callbackURL = "";

		if (jsonObject.has("callbackURL")) {
			callbackURL = jsonObject.getString("callbackURL");
		}

		long orderId = jsonObject.getLong("orderId");

		String cartPaymentURL = WebClient.create(
			StringBundler.concat(
				lxcDXPServerProtocol, "://", lxcDXPMainDomain,
				"/o/headless-commerce-delivery-cart/v1.0/carts/",
				String.valueOf(orderId),
				"/payment-url", "?callbackURL=", callbackURL)
		).get(
		).accept(
			MediaType.TEXT_PLAIN
		).header(
			HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue()
		).retrieve(
		).bodyToMono(
			String.class
		).block();

		StringBuilder sb = new StringBuilder();

		sb.append(cartPaymentURL);

		if (jsonObject.has("transactionId")) {
			sb.append("&entryId=");
			sb.append(_getPaymentId(orderId, jsonObject.getString("transactionId")));
		}

		if (jsonObject.has("cancel")) {
			sb.append("&cancel=true&redirect=false");
		}

		if (jsonObject.has("fundingSource")) {
			_storeFundingSource(jwt, orderId, jsonObject.getString("fundingSource"));
		}

		return new ResponseEntity<>(
			new JSONObject(
			).put(
				"url", sb.toString()
			).toString(),
			HttpStatus.OK);
	}

	private String _getPaymentId(
		long orderId, String transactionId) {

		JSONObject paymentsJSONObject = get(			"Bearer " + jwt.getTokenValue(),
			StringBundler.concat(
				"/o/headless-commerce-admin-payment/v1.0/payments/?filter=",
				"classPK eq ", String.valueOf(orderId)));

		JSONArray itemsJSONArray = paymentsJSONObject.getJSONArray("items");

		for (int i = 0; i < itemsJSONArray.length(); i++) {
			JSONObject itemJSONObject = itemsJSONArray.getJSONObject(i);

			String payload = itemJSONObject.getString("payload");

			if (StringUtils.contains(payload, transactionId)) {

				return String.valueOf(itemJSONObject.getInt("id"));
			}
		}

		return null;
	}

	private void _storeFundingSource(
		@AuthenticationPrincipal Jwt jwt, long orderId, String fundingSource) {

		post(
			"Bearer " + jwt.getTokenValue(),
			new JSONObject(
			).put(
				"externalReferenceCode", orderId
			).put(
				"fundingSource",
				fundingSource
			).put(
				"orderId",
				orderId
			).toString(),
			"/o/c/n2a1paypalwebhooks");
	}

	private static final Log _log = LogFactory.getLog(
		RenderRestController.class);

}