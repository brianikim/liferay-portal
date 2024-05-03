/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.paypal;

import com.liferay.petra.string.StringBundler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.json.JSONObject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * @author Brian I. Kim
 */
@RequestMapping("/render")
@RestController
public class RenderRestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(
		@AuthenticationPrincipal Jwt jwt, @RequestBody String json) {

		JSONObject jsonObject = new JSONObject(json);

		_clientId = String.valueOf(jsonObject.get("clientId"));
		_merchantId = String.valueOf(jsonObject.get("merchantId"));

		String mode = String.valueOf(jsonObject.get("mode"));

		if (mode.equals("live")) {
			_apiBaseURL = "https://api-m.paypal.com/v2/";
		}
		else {
			_apiBaseURL = "https://api-m.sandbox.paypal.com/v2/";
		}

		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	@GetMapping
	public ResponseEntity<String> get(
		@AuthenticationPrincipal Jwt jwt) {

		JSONObject jsonObject = new JSONObject();

		jsonObject.put("clientId", _clientId);
		jsonObject.put("merchantId", _merchantId);
		jsonObject.put("clientSecret", _clientSeret);
		jsonObject.put("apiBaseURL", _apiBaseURL);

		return new ResponseEntity<>(jsonObject.toString(), HttpStatus.OK);
	}

	private String _clientId;
	private String _clientSeret;
	private String _merchantId;

	private String _apiBaseURL;
}