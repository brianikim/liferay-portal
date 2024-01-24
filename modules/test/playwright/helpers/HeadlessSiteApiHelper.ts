/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import getRandomId from '../utils/getRandomId';
import { getRandomInt } from '../utils/util';
import {ApiHelpers} from './ApiHelpers';

export class HeadlessSiteApiHelper {
	apiHelpers: ApiHelpers;
	basePath: string;

	constructor(apiHelpers: ApiHelpers) {
		this.apiHelpers = apiHelpers;
		this.basePath = 'headless-site/v1.0';
	}

	async createSite(
		name: string = "name" + getRandomInt(),
		templateKey: string = "",
		templateType: string = "",
		baseUrl?: string
	): Promise<Site> {
		if (typeof baseUrl !== 'undefined') {
			return this.apiHelpers.post(
				`${baseUrl}${this.basePath}/sites`,
				{
					name,
					templateKey,
					templateType
				}
			);
		}
		return this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/sites`,
			{
				name,
				templateKey,
				templateType
			}
		);
	}

	async deleteSite(siteId: string) {
		return this.apiHelpers.delete(
			`${this.apiHelpers.baseUrl}${this.basePath}/sites/${siteId}`
		);
	}
}
