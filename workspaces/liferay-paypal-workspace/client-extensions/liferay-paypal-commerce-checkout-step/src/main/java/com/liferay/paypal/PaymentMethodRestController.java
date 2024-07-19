/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.paypal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Brian I. Kim
 */
@RequestMapping("/payment-method")
@RestController
public class PaymentMethodRestController extends BaseRestController {

	@GetMapping
	public String get() {
		return "liferay-paypal-commerce-payment-integration";
	}

}