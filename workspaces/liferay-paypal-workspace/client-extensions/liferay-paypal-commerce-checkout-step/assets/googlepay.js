/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

let google = null;
let paypal = null;

async function addGooglePayButton() {
	const paymentsClient = await getGooglePaymentsClient();
	const button = paymentsClient.createButton({
		onClick: onGooglePaymentButtonClicked,
	});
	document.getElementById('paypal-button-container').append(button);
}

async function getGooglePayConfig() {
	const googlepayConfig = await paypal.Googlepay().config();

	return googlepayConfig;
}

async function getGooglePaymentDataRequest() {
	const {
		allowedPaymentMethods,
		apiVersion,
		apiVersionMinor,
		countryCode,
		merchantInfo,
	} = await getGooglePayConfig();
	const baseRequest = {
		apiVersion,
		apiVersionMinor,
	};
	const paymentDataRequest = {...baseRequest};

	paymentDataRequest.allowedPaymentMethods = allowedPaymentMethods;
	paymentDataRequest.transactionInfo =
		await getGoogleTransactionInfo(countryCode);
	paymentDataRequest.merchantInfo = merchantInfo;

	paymentDataRequest.callbackIntents = ['PAYMENT_AUTHORIZATION'];

	return paymentDataRequest;
}

async function getGooglePaymentsClient() {
	const payPalOAuth = Liferay.OAuth2Client.FromUserAgentApplication(
		'liferay-paypal-commerce-payment-integration-oaua'
	);

	const clientId = document.getElementById('payment-client-id').value;

	const orderData = await payPalOAuth
		.fetch('/set-up-payment/get-google-environment/' + clientId)
		.then((response) => {
			return response.json();
		});

	const paymentsClient = new google.payments.api.PaymentsClient({
		environment: orderData.mode,
		paymentDataCallbacks: {
			onPaymentAuthorized,
		},
	});

	return paymentsClient;
}

async function getGoogleTransactionInfo(countryCode) {
	const payPalOAuth = Liferay.OAuth2Client.FromUserAgentApplication(
		'liferay-paypal-commerce-payment-integration-oaua'
	);

	const orderId = document.getElementById('payment-order-id').value;

	const orderData = await payPalOAuth
		.fetch(
			'/set-up-payment/get-google-order/' + orderId + '/' + countryCode
		)
		.then((response) => {
			return response.json();
		});

	return orderData;
}

async function onGooglePaymentButtonClicked() {
	const paymentDataRequest = await getGooglePaymentDataRequest();
	const paymentsClient = await getGooglePaymentsClient();
	paymentsClient.loadPaymentData(paymentDataRequest);
}

export async function onGooglePayLoaded() {
	google = window.google;
	paypal = window.paypal;

	const paymentsClient = await getGooglePaymentsClient();
	const {allowedPaymentMethods, apiVersion, apiVersionMinor} =
		await getGooglePayConfig();
	paymentsClient
		.isReadyToPay({allowedPaymentMethods, apiVersion, apiVersionMinor})
		.then((response) => {
			if (response.result) {
				addGooglePayButton();
			}
		})
		.catch((error) => {
			console.error(error);
		});
}

function onPaymentAuthorized(paymentData) {
	return processPayment(paymentData);
}

async function processPayment(paymentData) {
	try {
		const payPalOAuth = Liferay.OAuth2Client.FromUserAgentApplication(
			'liferay-paypal-commerce-payment-integration-oaua'
		);

		const orderId = document.getElementById('payment-order-id').value;

		const setUpPaymentResource = await payPalOAuth.fetch('/render', {
			body: JSON.stringify({
				fundingSource: 'google_pay',
				orderId,
				redirect: false,
			}),
			method: 'POST',
		});

		const setUpPaymentURLJson = await setUpPaymentResource.json();
		const resource = await fetch(setUpPaymentURLJson.url);

		if (!resource.ok) {
			return {transactionState: 'FAIL'};
		}

		const orderData = await payPalOAuth.fetch(
			'/set-up-payment/get-paypal-order/' + orderId
		);

		const orderDataJson = await orderData.json();

		const id = orderDataJson.id;

		const {status} = await paypal.Googlepay().confirmOrder({
			orderId: id,
			paymentMethodData: paymentData.paymentMethodData,
		});

		if (status === 'PAYER_ACTION_REQUIRED') {
			await paypal.Googlepay().initiatePayerAction({orderId: id});
		}

		const capturePaymentResource = await payPalOAuth.fetch('/render', {
			body: JSON.stringify({
				orderId,
				transactionCode: id,
			}),
			method: 'POST',
		});

		const capturePaymentResourceJSON =
			await capturePaymentResource.json();
		const response = await fetch(capturePaymentResourceJSON.url);

		if (response.ok) {
			window.location.href = response.url;

			return {transactionState: 'SUCCESS'};
		}

		return {transactionState: 'FAIL'};
	}
	catch (error) {
		return {
			error: {
				message: error.message,
			},
			transactionState: 'ERROR',
		};
	}
}
