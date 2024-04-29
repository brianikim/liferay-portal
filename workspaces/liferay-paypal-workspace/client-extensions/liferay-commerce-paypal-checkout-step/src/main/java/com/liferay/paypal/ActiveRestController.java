/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.paypal;

import org.springframework.web.bind.annotation.RequestMapping;

import org.json.JSONObject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Brian I. Kim
 */
@RequestMapping("/active")
@RestController
public class ActiveRestController extends BaseRestController {

	@PostMapping("/payment-method")
	public ResponseEntity<String> post(
			@AuthenticationPrincipal Jwt jwt, @RequestBody String json) {

		JSONObject jsonObject = new JSONObject(json);

		String paymentMethod = String.valueOf(jsonObject.get("paymentMethod"));

		if (paymentMethod.equals("liferay-commerce-paypal-payment-integration")) {
			jsonObject.put("active", true);
		}
		else {
			jsonObject.put("active", false);
		}

		return new ResponseEntity<>(jsonObject.toString(), HttpStatus.OK);
	}
}