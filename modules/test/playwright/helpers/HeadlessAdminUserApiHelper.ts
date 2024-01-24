/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import { getRandomInt } from '../utils/util';
import {ApiHelpers} from './ApiHelpers';

export class HeadlessAdminUserApiHelper {
	readonly apiHelpers: ApiHelpers;
	readonly basePath: string;

	constructor(apiHelpers: ApiHelpers) {
		this.apiHelpers = apiHelpers;
		this.basePath = 'headless-admin-user/v1.0/';
	}

	async getSiteByFriendlyUrlPath(friendlyUrlPath: string) {
		return this.apiHelpers.get(
			`${this.apiHelpers.baseUrl}${this.basePath}/sites/by-friendly-url-path/${friendlyUrlPath}`
		);
	}

	async getAccountRolesPage(
		accountId: string,
		baseUrl?: string
	){
		if (typeof baseUrl !== 'undefined') {
			return await this.apiHelpers.get(
				`${baseUrl}${this.basePath}/accounts/${accountId}/account-roles`
			);
		}

		return await this.apiHelpers.get(
			`${this.apiHelpers.baseUrl}${this.basePath}/accounts/${accountId}/account-roles`
		);
	}

	async postAccount(
		name: string = "name" + getRandomInt(),
		type: string = "business",
		accountUserAccounts: DataObject[] = [],
		baseUrl?: string
    ) {
		if (typeof baseUrl !== 'undefined') {
			return await this.apiHelpers.post(
				`${baseUrl}${this.basePath}/accounts`,
				{
					name: name,
					type: type,
					accountUserAccounts: accountUserAccounts,
				}
			);
		}

		return await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/accounts`,
			{
				name: name,
				type: type,
				accountUserAccounts: accountUserAccounts,
			}
		);
	}

	async postAccountByExternalReferenceCodeAccountRoleUserAccountByEmailAddress(
		externalReferenceCode: string,
		accountRoleId: string,
		emailAddress: string,
		baseUrl?: string
    ) {
		if (typeof baseUrl !== 'undefined') {
			return await this.apiHelpers.post(
				`${baseUrl}${this.basePath}/accounts/by-external-reference-code/${externalReferenceCode}/account-roles/${accountRoleId}/user-accounts/by-email-address/${emailAddress}`,
				{}
			);
		}
		return await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/accounts/by-external-reference-code/${externalReferenceCode}/account-roles/${accountRoleId}/user-accounts/by-email-address/${emailAddress}`,
			{}
		);
	}

	async postUserAccount(
		alternateName: string = "alternateName" + getRandomInt(),
		emailAddress: string = "emailAddress" + getRandomInt() + "@liferay.com",
		familyName: string = "familyName" + getRandomInt(),
		givenName: string = "givenName" + getRandomInt(),
		password: string = "test",
		baseUrl?: string
    ) {
		if (typeof baseUrl !== 'undefined') {
			return await this.apiHelpers.post(
				`${baseUrl}${this.basePath}/user-accounts`,
				{
					alternateName: alternateName,
					emailAddress: emailAddress,
					familyName: familyName,
					givenName: givenName,
					password: password,
				}
			);
		}

		return await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/user-accounts`,
			{
				alternateName: alternateName,
				emailAddress: emailAddress,
				familyName: familyName,
				givenName: givenName,
				password: password,
			}
		);
	}
}
