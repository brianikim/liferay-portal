/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {onPaypalLoaded} from './paypal.js';

function loadScript(src) {
	return new Promise((resolve, reject) => {
		const script = document.createElement('script');
		script.src = src;
		script.onload = resolve;
		script.onerror = reject;
		document.head.appendChild(script);
	});
}

const script1 = loadScript('script1.js');
const script2 = loadScript('script2.js');


Promise.all([script1, script2])
	.then(() => {
		// Both scripts loaded, execute your logic here

	})
	.catch((error) => {
		console.error("Error loading script:", error);
	});

export default function CommerceCheckoutStep() {
	const commerceCheckoutStepContainer = document.getElementById(
		'_com_liferay_commerce_checkout_web_internal_portlet_CommerceCheckoutPortlet_commerceCheckoutStepContainer'
	);

	const payPalButtonContainerDivElement = document.createElement('div');
	payPalButtonContainerDivElement.setAttribute(
		'id',
		'paypal-button-container'
	);

	const resultMessageElement = document.createElement('p');
	resultMessageElement.setAttribute('id', 'result-message');

	const clientId = document.getElementById('payment-client-id').value;
	const payPalSDKScript = document.createElement('script');
	payPalSDKScript.src = `https://www.paypal.com/sdk/js?client-id=${clientId}&currency=${Liferay.CommerceContext.currency.currencyCode}&components=buttons,googlepay&enable-funding=venmo&disable-funding=blik,sepa`;
	payPalSDKScript.setAttribute(
		'data-partner-attribution-id',
		'Liferay_SP_PPCP_API'
	);
	payPalSDKScript.onload = onPaypalLoaded;

	const googleSDKScript = document.createElement('script');
	googleSDKScript.src = "https://pay.google.com/gp/p/js/pay.js"

	payPalButtonContainerDivElement.appendChild(googleSDKScript);
	payPalButtonContainerDivElement.appendChild(payPalSDKScript);
	payPalButtonContainerDivElement.appendChild(resultMessageElement);
	commerceCheckoutStepContainer.appendChild(payPalButtonContainerDivElement);
}
