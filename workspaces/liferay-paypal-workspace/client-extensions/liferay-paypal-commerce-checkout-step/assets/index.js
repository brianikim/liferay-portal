/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

async function getGooglePayConfig() {
		const googlePayConfig = await window.paypal.Googlepay().config();

		const allowedPaymentMethods = googlePayConfig.allowedPaymentMethods;

		const merchantInfo = googlePayConfig.merchantInfo;

	return {

		allowedPaymentMethods,

		merchantInfo,

	};
}

let paymentsClient = null;

function getGooglePaymentsClient() {

	if (paymentsClient === null) {

		paymentsClient = new google.payments.api.PaymentsClient({

			environment: "TEST",
			paymentDataCallbacks: {

				onPaymentAuthorized: onPaymentAuthorized,

			},
		});

	}

	return paymentsClient;

}

async function getGooglePaymentDataRequest() {

	const baseRequest = {

		apiVersion: 2,

		apiVersionMinor: 0,

	};

	const paymentDataRequest = Object.assign({}, baseRequest);

	const { allowedPaymentMethods, merchantInfo } = await getGooglePayConfig();

	paymentDataRequest.allowedPaymentMethods = allowedPaymentMethods;

	paymentDataRequest.transactionInfo = getGoogleTransactionInfo();

	paymentDataRequest.merchantInfo = merchantInfo;

	paymentDataRequest.callbackIntents = ["PAYMENT_AUTHORIZATION"];

	return paymentDataRequest;

}

async function onGooglePaymentButtonClicked() {

	const paymentDataRequest = await getGooglePaymentDataRequest();

	paymentDataRequest.transactionInfo = getGoogleTransactionInfo();

	const paymentsClient = getGooglePaymentsClient();

	paymentsClient.loadPaymentData(paymentDataRequest);

}

function getGoogleTransactionInfo() {

	return {

		displayItems: [

			{

				label: "Subtotal",

				type: "SUBTOTAL",

				price: "100.00",

			},

			{

				label: "Tax",

				type: "TAX",

				price: "10.00",

			},

		],

		countryCode: "US",

		currencyCode: "USD",

		totalPriceStatus: "FINAL",

		totalPrice: "110.00",

		totalPriceLabel: "Total",

	};

}

function onPaymentAuthorized(paymentData) {

	return new Promise(function (resolve, reject) {

		processPayment(paymentData)

			.then(function (data) {

				resolve({ transactionState: "SUCCESS" });

			})

			.catch(function (errDetails) {

				resolve({ transactionState: "ERROR" });

			});

	});

}

async function processPayment(paymentData) {

	return new Promise(async function (resolve, reject) {

		try {

			// Create the order on your server

			const cartPaymentResource = await checkoutStepOauth.fetch("/render", {
				body: JSON.stringify({
					orderId: Liferay.CommerceContext.order.orderId,
				}), method: "POST"
			});

			const cartPaymentURLJson = await cartPaymentResource.json();
			const cartPaymentURL = cartPaymentURLJson.url;

			let orderData;

			const resource = await fetch(cartPaymentURL);

			if (resource.ok) {
				orderData = await payPalOAuth.fetch("/set-up-payment/get/" + Liferay.CommerceContext.order.orderId);

				if (orderData) {
					const orderDataJson = await orderData.json();

					const confirmOrderResponse = await paypal.Googlepay().confirmOrder({

						orderId: orderDataJson.id,

						paymentMethodData: paymentData.paymentMethodData


					});

					/** Capture the Order on your Server  */

					if(confirmOrderResponse.status === "APPROVED"){

						const response =  await fetch(`/capture/${id}`, {

							method: 'POST',

						}).then(res => res.json());

						if(response.capture.status === "COMPLETED")

							resolve({transactionState: 'SUCCESS'});

						else

							resolve({

								transactionState: 'ERROR',

								error: {

									intent: 'PAYMENT_AUTHORIZATION',

									message: 'TRANSACTION FAILED',

								}

							})

					} else {

						resolve({

							transactionState: 'ERROR',

							error: {

								intent: 'PAYMENT_AUTHORIZATION',

								message: 'TRANSACTION FAILED',

							}

						})

					}

				}
			}
		} catch(err) {

			resolve({

				transactionState: 'ERROR',

				error: {

					intent: 'PAYMENT_AUTHORIZATION',

					message: err.message,

				}

			})

		}

	});

}


export default function CommerceCheckoutStep() {
	const commerceCheckoutStepContainer = document.getElementById(
		'_com_liferay_commerce_checkout_web_internal_portlet_CommerceCheckoutPortlet_commerceCheckoutStepContainer'
	);

	const paypalOauth = Liferay.OAuth2Client.FromUserAgentApplication(
		"liferay-paypal-commerce-payment-integration-oauth-application-user-agent");

	let clientId = document.getElementById("payment-client-id").value;

	const payPalButtonContainerDivElement = document.createElement("div");
	payPalButtonContainerDivElement.setAttribute(
		'id', "paypal-button-container");

	const resultMessageElement = document.createElement("p");
	resultMessageElement.setAttribute('id', "result-message");

	let payPalSDKScript = document.createElement('script');
	payPalSDKScript.src =
		`https://www.paypal.com/sdk/js?client-id=${clientId}&currency=${Liferay.CommerceContext.currency.currencyCode}&buyer-country=US&components=googlepay,buttons&enable-funding=venmo&disable-funding=blik,sepa`;

	let googleSDKScript = document.createElement('script');
	googleSDKScript.src = 'https://pay.google.com/gp/p/js/pay.js'
	googleSDKScript.async = true;
/*	googleSDKScript.addEventListener('load', () => {
		const paymentsClient = new window.google.payments.api.PaymentsClient({environment: 'TEST'});

		const test = getGooglePayConfig();

		const button =

			paymentsClient.createButton({
			});

		payPalButtonContainerDivElement.append(button);
	});*/


	payPalSDKScript.addEventListener('load', () => {
		if (window.google) {
			const paymentsClient = new window.google.payments.api.PaymentsClient({environment: 'TEST'});

			const test = getGooglePayConfig();

			const button1 = paymentsClient.createButton({

				onClick: onGooglePaymentButtonClicked,

			});

			payPalButtonContainerDivElement.append(button1);

		}
			var button = 		window.paypal
				.Buttons({

					async createOrder(data) {
						try {
							const cartPaymentResource = await paypalOauth.fetch("/render", {
								body: JSON.stringify({
									orderId: Liferay.CommerceContext.order.orderId,
									fundingSource: data.paymentSource
								}), method: "POST"
							});

							const cartPaymentURLJson = await cartPaymentResource.json();
							const cartPaymentURL = cartPaymentURLJson.url;
							const resource = await fetch(cartPaymentURL);

							if (resource.ok) {
								const orderData = await paypalOauth.fetch("/set-up-payment/get/" + Liferay.CommerceContext.order.orderId);

								if (orderData) {
									const orderDataJson = await orderData.json();

									return orderDataJson.id;
								}
							}
						}
						catch (error) {
							console.error(error);
							resultMessage(
								`Could not initiate PayPal Checkout...<br><br>${error}`);
						}
					},
					async onApprove(data, actions) {
						try {
							const cartPaymentResource = await paypalOauth.fetch("/render", {
								body: JSON.stringify({
									orderId: Liferay.CommerceContext.order.orderId,
									transactionId: data.orderID,
								}), method: "POST"
							});

							const cartPaymentResourceJSON = await cartPaymentResource.json();
							const cartPaymentURL = cartPaymentResourceJSON.url;
							const response = await fetch(cartPaymentURL);

							if (response.ok) {
								window.location.href = response.url;
							}
						} catch (error) {
							console.error(error);
							resultMessage(
								`Sorry, your transaction could not be processed...<br><br>${error}`,
							);
						}
					},
					async onCancel(data) {
						const cartPaymentResource = await paypalOauth.fetch("/render", {
							body: JSON.stringify({
								cancel: true,
								orderId: Liferay.CommerceContext.order.orderId,
								transactionId: data.orderID,
							}), method: "POST"
						});

						const cartPaymentResourceJSON = await cartPaymentResource.json();
						const cartPaymentURL = cartPaymentResourceJSON.url;
						const response = await fetch(cartPaymentURL);
					},
					onError: function(err) {
						console.log(err);
					}
				})
			// Check if the button is eligible
			if (button.isEligible()) {
				// Render the standalone button for that payment method
				button.render('#paypal-button-container')
			}
	})

	payPalButtonContainerDivElement.appendChild(payPalSDKScript);
	payPalButtonContainerDivElement.appendChild(googleSDKScript);
	payPalButtonContainerDivElement.appendChild(resultMessageElement);
	commerceCheckoutStepContainer.appendChild(payPalButtonContainerDivElement);
}

function resultMessage(message) {
	const container = document.querySelector("#result-message");
	container.innerHTML = message;
}
