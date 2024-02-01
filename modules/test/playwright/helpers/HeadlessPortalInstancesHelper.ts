/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ApiHelpers} from './ApiHelpers';

export class HeadlessPortalInstancesHelper {
	readonly apiHelpers: ApiHelpers;
	readonly basePath: string;

	constructor(apiHelpers: ApiHelpers) {
		this.apiHelpers = apiHelpers;
		this.basePath = 'headless-portal-instances/v1.0/';
	}

	async postPortalInstance(
        domain: string = "liferaytest.com",
        portalInstanceId: string = "liferaytest.com",
        virtualHost: string = "localhosttest"
    ) {
		const postPortalInstance = await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/portal-instances`,
			{
				domain: domain,
				portalInstanceId: portalInstanceId,
				virtualHost: virtualHost	
    		}
		);

		return postPortalInstance;
	}

	async deletePortalInstance(
        portalInstanceId: string,
    ) {
		return await this.apiHelpers.delete(
			`${this.apiHelpers.baseUrl}${this.basePath}/portal-instances/${portalInstanceId}`,
		);
	}
}
