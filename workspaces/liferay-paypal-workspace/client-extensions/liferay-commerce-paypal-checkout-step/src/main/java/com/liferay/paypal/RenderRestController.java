package com.liferay.paypal;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RequestMapping("/render")
@RestController
public class RenderRestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(
		@AuthenticationPrincipal Jwt jwt,
		@RequestBody String json) {

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
				orderId,"/payment-url", "?callbackURL=", callbackURL)
		).get(
		).accept(
			MediaType.TEXT_PLAIN
		).header(
			HttpHeaders.AUTHORIZATION,
			"Bearer " + jwt.getTokenValue()
		).retrieve(
		).bodyToMono(
			String.class
		).block();

		StringBuilder sb = new StringBuilder();

		sb.append(cartPaymentURL);

		if (jsonObject.has("entryId")) {
			sb.append("&entryId=");
			sb.append(jsonObject.getString("entryId"));
		}

		return new ResponseEntity<>(
			new JSONObject(
			).put(
				"url", sb.toString()
			).toString(),
			HttpStatus.OK);
	}

	private static final Log _log = LogFactory.getLog(
		RenderRestController.class);


	@Value("${com.liferay.lxc.dxp.mainDomain}")
	protected String lxcDXPMainDomain;

	@Value("${com.liferay.lxc.dxp.server.protocol}")
	protected String lxcDXPServerProtocol;
}
