/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export default async function CommerceCheckoutStep() {
	const commerceCheckoutStepContainer = document.getElementById(
		'_com_liferay_commerce_checkout_web_internal_portlet_CommerceCheckoutPortlet_commerceCheckoutStepContainer'
	);

	console.log("hello");

	const oauth = Liferay.OAuth2Client.FromUserAgentApplication("liferay-commerce-paypal-payment-integration-oauth-application-user-agent")

	const response = await oauth.fetch("/render/get", {method: "get"});

	const data = await response.json();
	console.log(data);

	let apiBaseUrl = data.apiBaseUrl;
	let clientId = data.clientId;
	let clientAuth = btoa(`${clientId}:EMiMaIJKB121Z_bYFDOxLPe0kkIQMkhUuKf_v9uOUFQ2tqyauoxdwwwb8ZNAQlYM_ns7sBGIP-GDgJKI`)

	const paypalButtonContainerDivElement = document.createElement("div");
	paypalButtonContainerDivElement.setAttribute('id', "paypal-button-container");

	const resultMessageElement = document.createElement("p");
	resultMessageElement.setAttribute('id', "result-message");

	let paypalSdkScript = document.createElement( 'script' );
	paypalSdkScript.src = `https://www.paypal.com/sdk/js?client-id=${clientId}&currency=USD`;

	paypalSdkScript.addEventListener('load', () => {
		window.paypal
			.Buttons({
				async createOrder() {
					try {
						const response = commercepaymentserv
						});

						const orderData = await response.json();

						if (orderData.id) {
							return orderData.id;
						} else {
							const errorDetail = orderData?.details?.[0];
							const errorMessage = errorDetail
								? `${errorDetail.issue} ${errorDetail.description} (${orderData.debug_id})`
								: JSON.stringify(orderData);

							throw new Error(errorMessage);
						}
					} catch (error) {
						console.error(error);
						resultMessage(`Could not initiate PayPal Checkout...<br><br>${error}`);
					}
				},

				async onApprove(data, actions) {
					try {
						const response = await fetch(apiBaseUrl + `checkout/orders/${data.orderID}/capture`, {
							method: "POST",
							headers: {
								"Content-Type": "application/json",
								'Authorization': 'Basic ' + clientAuth
							},
						});

						const orderData = await response.json();

						const errorDetail = orderData?.details?.[0];

						if (errorDetail?.issue === "INSTRUMENT_DECLINED") {
							return actions.restart();
						} else if (errorDetail) {
							throw new Error(`${errorDetail.description} (${orderData.debug_id})`);
						} else if (!orderData.purchase_units) {
							throw new Error(JSON.stringify(orderData));
						} else {
							const transaction =
								orderData?.purchase_units?.[0]?.payments?.captures?.[0] ||
								orderData?.purchase_units?.[0]?.payments?.authorizations?.[0];
							resultMessage(
								`Transaction ${transaction.status}: ${transaction.id}<br><br>See console for all available details`,
							);
							console.log(
								"Capture result",
								orderData,
								JSON.stringify(orderData, null, 2),
							);
							orderDataTest.setAttribute("data", orderData);
						}
					} catch (error) {
						console.error(error);
						resultMessage(
							`Sorry, your transaction could not be processed...<br><br>${error}`,
						);
					}
				},
			})
			.render("#paypal-button-container");
	})

	paypalButtonContainerDivElement.appendChild( paypalSdkScript );
	paypalButtonContainerDivElement.appendChild( resultMessageElement );
	commerceCheckoutStepContainer.appendChild(paypalButtonContainerDivElement);
}

function resultMessage(message) {
	const container = document.querySelector("#result-message");
	container.innerHTML = message;
}
