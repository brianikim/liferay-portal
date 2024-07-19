export async function onGooglePayLoaded() {
	const paymentsClient = getGooglePaymentsClient();
	const { allowedPaymentMethods, apiVersion, apiVersionMinor } = await getGooglePayConfig();
	paymentsClient.isReadyToPay({allowedPaymentMethods, apiVersion,apiVersionMinor})
		.then(function(response) {
			if (response.result) {
				addGooglePayButton();
			}
		})
		.catch(function(err) {
			// show error in developer console for debugging
			console.error(err);
		});
}

function getGooglePaymentsClient() {
	const paymentsClient = new google.payments.api.PaymentsClient({
		environment: 'TEST',
		paymentDataCallbacks: {
			onPaymentAuthorized: onPaymentAuthorized
		}
	});

	return paymentsClient;
}

async function processPayment(paymentData) {
	try {
		const payPalOAuth = Liferay.OAuth2Client.FromUserAgentApplication(
			'liferay-paypal-commerce-payment-integration-oauth-application-user-agent'
		);

		const cartPaymentResource = await payPalOAuth.fetch('/render', {
			body: JSON.stringify({
				orderId: 34699,
				fundingSource: "google_pay",
				redirect: false,
			}),
			method: 'POST',
		});

		const cartPaymentURLJson = await cartPaymentResource.json();
		const cartPaymentURL = cartPaymentURLJson.url;
		const resource = await fetch(cartPaymentURL);

		if (resource.ok) {
			const orderData = await payPalOAuth.fetch(
				'/set-up-payment/get/' +
				34699
			);

			if (orderData) {
				const orderDataJson = await orderData.json();

				const id = orderDataJson.id;

				const {status} = await paypal.Googlepay().confirmOrder({
					orderId: id,
					paymentMethodData: paymentData.paymentMethodData
				});

				console.log(status);

/*				if (status === 'PAYER_ACTION_REQUIRED') {
					console.log(
						" ===== Confirm Payment Completed Payer Action Required ===== ")
					paypal.Googlepay().initiatePayerAction({orderId: id}).then(
						async () => {

							/!**
							 *  GET Order
							 *!/
							const orderResponse = await fetch(`/api/orders/${id}`, {
								method: "GET"
							}).then(res => res.json())
						})
				}*/

				const cartPaymentResourceNew = await payPalOAuth.fetch('/render', {
					body: JSON.stringify({
						orderId: 34699,
						transactionCode: id
					}),
					method: 'POST',
				});

				const cartPaymentResourceJSON =
					await cartPaymentResourceNew.json();
				const cartPaymentURLNew = cartPaymentResourceJSON.url;
				const response = await fetch(cartPaymentURLNew);

				if (response.ok) {
					return {transactionState: 'SUCCESS'}
				}
			}
		}
	}
	catch (err) {
		return {
			transactionState: 'ERROR',
			error: {
				message: err.message
			}
		}
	}
}

function onPaymentAuthorized(paymentData) {
	return new Promise(function(resolve, reject) {
		processPayment(paymentData)
			.then(function() {
				resolve({transactionState: 'SUCCESS'});
			})
			.catch(function() {
				resolve({transactionState: 'ERROR'});
			});
	});
}

function addGooglePayButton() {
	const paymentsClient = getGooglePaymentsClient();
	const button =
		paymentsClient.createButton({
			onClick: onGooglePaymentButtonClicked
		});
	document.getElementById('paypal-button-container').append(button);
}

async function onGooglePaymentButtonClicked() {
	const paymentDataRequest = await getGooglePaymentDataRequest();
	const paymentsClient = getGooglePaymentsClient();
	paymentsClient.loadPaymentData(paymentDataRequest);
}

async function getGooglePaymentDataRequest() {
	const {allowedPaymentMethods,merchantInfo, apiVersion, apiVersionMinor , countryCode} = await getGooglePayConfig();
	const baseRequest = {
		apiVersion,
		apiVersionMinor
	}
	const paymentDataRequest = Object.assign({}, baseRequest);

	paymentDataRequest.allowedPaymentMethods = allowedPaymentMethods;
	paymentDataRequest.transactionInfo = getGoogleTransactionInfo(countryCode);
	paymentDataRequest.merchantInfo =merchantInfo;

	paymentDataRequest.callbackIntents = ["PAYMENT_AUTHORIZATION"];

	return paymentDataRequest;
}

function getGoogleTransactionInfo(countryCode) {
	return {
		displayItems: [{
			label: "Subtotal",
			type: "SUBTOTAL",
			price: "24.00",
		},
			{
				label: "Tax",
				type: "TAX",
				price: "0.00",
			}
		],
		countryCode: countryCode,
		currencyCode: "USD",
		totalPriceStatus: "FINAL",
		totalPrice: "39.00",
		totalPriceLabel: "Total"
	};
}

async function getGooglePayConfig(){
	const googlepayConfig = await paypal.Googlepay().config();

	return googlepayConfig;
}