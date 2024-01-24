/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../fixtures/commercePagesTest';
import {loginTest} from '../../fixtures/loginTest';
import { getRandomInt } from '../../utils/util';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	loginTest,
);

test('headlesstest', async ({
	page,
	apiHelpers,
}) => {
	const virtualInstance = await apiHelpers.headlessPortalInstances.postPortalInstance();
	const baseUrl = "http://" + virtualInstance.virtualHost + ":8080/o/"

	await page.goto("http://" + virtualInstance.virtualHost + ":8080");
	await page.getByRole('button', { name: 'Sign In' }).click();
	await page.getByLabel('Email Address').fill('test@liferaytest.com');
	await page.getByLabel('Password').fill('test');
	await page.getByLabel('Remember Me').check();
	await page.getByLabel('Sign In- Loading').getByRole('button', { name: 'Sign In' }).click();

	const user = await apiHelpers.headlessAdminUser.postUserAccount(
		"alternateName" + getRandomInt(),
		"emailAddress" + getRandomInt() + "@liferay.com",
		"familyName" + getRandomInt(),
		"givenName" + getRandomInt(),
		"test",
		baseUrl
	);

	const userAccount = await apiHelpers.headlessAdminUser.postAccount(
		"name" + getRandomInt(),
		'business',
		[{
			id: user.id
		}],
		baseUrl
	);

	const userAccountRoles = await apiHelpers.headlessAdminUser.getAccountRolesPage(userAccount.id, baseUrl);
	const userAccountBuyerRole = userAccountRoles.items.filter(role => role.name = "Buyer");

	await apiHelpers.headlessAdminUser.postAccountByExternalReferenceCodeAccountRoleUserAccountByEmailAddress(
		userAccount.externalReferenceCode,
		userAccountBuyerRole.id,
		user.emailAddress,
		baseUrl
	)

	await apiHelpers.headlessSite.createSite(
		'Minium',
		"minium-initializer",
		"site-initializer",
		baseUrl
	);
	await page.goto('http://localhosttest:8080/web/minium/catalog');

	// const site = await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath(
	// 	'guest'
	// );

	// const channel = await apiHelpers.headlessCommerceAdminChannel.postChannel(
	// 	'Specification Facet Channel',
	// 	site.id
	// );

	// const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog(
	// 	'Specification Facet Catalog'
	// );

	// const product1 = await apiHelpers.headlessCommerceAdminCatalog.postProduct(
	// 	catalog.id,
	// 	'Product1'
	// );
	// const product2 = await apiHelpers.headlessCommerceAdminCatalog.postProduct(
	// 	catalog.id,
	// 	'Product2'
	// );

	// // create user account and order
	// const user = await apiHelpers.headlessAdminUser.postUserAccount(
	// 	'test2',
	// 	'test2@liferay.com',
	// 	'test2',
	// 	'test2',
	// 	'test'
	// );

	// const userAccount = await apiHelpers.headlessAdminUser.postAccount(
	// 	'buyer',
	// 	'business'
	// );

	// const userOrder = await apiHelpers.headlessCommerceAdminOrder.postOrder(
	// 	userAccount.accountId,
	// 	channel.channelId,
	// 	'USD',
	// 	[
	// 		{
	// 		  quantity: "1",
	// 		  skuId: "78529",
	// 		  unitOfMeasure: "",
	// 		  unitOfMeasureKey: "",
	// 		  unitPrice: "24",
	// 		  unitPriceWithTaxAmount: "24"
	// 		}
	// 	  ],
	// 	'2'
	// );

	// // create guest account and order
	// const guestAccount = await apiHelpers.headlessAdminUser.postAccount(
	// 	'guestBuyer',
	// 	'guest'
	// );

	// const guestOrder = await apiHelpers.headlessCommerceAdminOrder.postOrder(
	// 	guestAccount.accountId,
	// 	channel.channelId,
	// 	'USD',
	// 	[
	// 		{
	// 		  quantity: "1",
	// 		  skuId: "77466",
	// 		  unitOfMeasure: "",
	// 		  unitOfMeasureKey: "",
	// 		  unitPrice: "24",
	// 		  unitPriceWithTaxAmount: "24"
	// 		}
	// 	  ],
	// 	'2',
	// );

	// clean up
});
