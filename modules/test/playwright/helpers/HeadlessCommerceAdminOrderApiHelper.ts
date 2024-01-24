/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import { channel } from 'diagnostics_channel';
import {getRandomInt} from '../utils/util';
import {ApiHelpers} from './ApiHelpers';

export class HeadlessCommerceAdminOrderApiHelper {
	readonly apiHelpers: ApiHelpers;
	readonly basePath: string;

	constructor(apiHelpers: ApiHelpers) {
		this.apiHelpers = apiHelpers;
		this.basePath = 'headless-commerce-admin-order/v1.0/';
	}

	async postOrder(
		accountId: number,
		channelId: string,
		currencyCode: string,
		orderItems: DataObject[],
		orderStatus: string,
	) {
		return await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/orders`,
			{
				accountId: accountId,
				channelId: channelId,
				currencyCode: currencyCode,
				orderItems: orderItems,
				orderStatus: orderStatus,
				workflowStatusInfo: {
					"code": 0,
					"label": "approved",
					"label_i18n": "Approved"
				}
			}
		);
	}

	async postOrderVirtualInstance(
		accountId: number,
		channelId: string,
		currencyCode: string,
		orderItems: DataObject[],
		orderStatus: string,
		baseUrl: string,
	) {
		return await this.apiHelpers.post(
			`${baseUrl}${this.basePath}/orders`,
			{
				accountId: accountId,
				channelId: channelId,
				currencyCode: currencyCode,
				orderItems: orderItems,
				orderStatus: orderStatus,
				workflowStatusInfo: {
					"code": 0,
					"label": "approved",
					"label_i18n": "Approved"
				}
			}
		);
	}
}
