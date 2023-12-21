/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export class HeadlessCommerceAdminPaymentApiHelper {
	constructor(apiHelpers, dataHelper) {
		this.apiHelpers = apiHelpers;
		this.basePath = 'headless-commerce-admin-payment/v1.0';
		this.dataHelper = dataHelper;
	}

	async deletePayment(
		apiHelpers = this.apiHelpers,
		paymentId
	) {
		return apiHelpers.delete(
			`${apiHelpers.baseUrl}${this.basePath}/payments/${paymentId}`
		);
	}

	async patchPayment(
		paymentId,
		payment
	) {
		return this.apiHelpers.patch(
			`${this.apiHelpers.baseUrl}${this.basePath}/payments/${paymentId}`,
			payment
		);
	}

	async postRandomPayment(
		payment
	) {
		const postPayment = await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/payments`,
			payment
		);

		this.dataHelper.addDataObject({handleDelete: this.deletePayment, id: postPayment.id});

		return postPayment;
	}
}