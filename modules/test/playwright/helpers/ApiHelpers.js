/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {liferayConfig} from '../liferay.config';
import {FeatureFlagApiHelper} from './FeatureFlagApiHelper';
import {HeadlessAdminUserApiHelper} from './HeadlessAdminUserApiHelper';
import {HeadlessCommerceAdminCatalogApiHelper} from './HeadlessCommerceAdminCatalogApiHelper';
import {HeadlessCommerceAdminChannelApiHelper} from './HeadlessCommerceAdminChannelApiHelper';
import {HeadlessCommerceAdminPaymentApiHelper} from './HeadlessCommerceAdminPaymentApiHelper';
import {ObjectAdminApiHelper} from './ObjectAdminApiHelper';

export class ApiHelpers {
	constructor(dataHelper, page) {
		this.baseUrl = liferayConfig.environment.baseUrl + '/o/';
		this.featureFlag = new FeatureFlagApiHelper(page);
		this.headlessCommerceAdminPayment = new HeadlessCommerceAdminPaymentApiHelper(
			this,
			dataHelper
		);
		this.headlessCommerceAdminCatalog = new HeadlessCommerceAdminCatalogApiHelper(
			this,
			dataHelper
		);
		this.headlessCommerceAdminChannel = new HeadlessCommerceAdminChannelApiHelper(
			this,
			dataHelper
		);
		this.headlessAdminUser = new HeadlessAdminUserApiHelper(this);
		this.objectAdmin = new ObjectAdminApiHelper(this);
		this.page = page;
	}

	async delete(url) {
		return this.page.request.delete(url, {
			headers: await this.getHeader(),
		});
	}

	async get(url, data) {
		const response = await this.page.request.get(url, {
			data,
			headers: await this.getHeader(),
		});

		return response.json();
	}

	async patch(url, data) {
		const response = await this.page.request.patch(url, {
			data,
			headers: await this.getHeader(),
		});

		return response.json();
	}

	async post(url, data) {
		const response = await this.page.request.post(url, {
			data,
			headers: await this.getHeader(),
		});

		return response.json();
	}

	async getHeader() {
		const authToken = await this.page.evaluate(() => Liferay.authToken);

		return {
			'Content-Type': 'application/json',
			'x-csrf-token': authToken,
		};
	}
}
