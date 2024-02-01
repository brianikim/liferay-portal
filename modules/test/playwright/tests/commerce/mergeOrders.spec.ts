/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../fixtures/commercePagesTest';
import {loginTest} from '../../fixtures/loginTest';
import { getRandomInt } from '../../utils/util';
import { applicationsMenuPageTest } from '../../fixtures/applicationsMenuPageTest';
import { ApplicationsMenuPage } from '../../pages/product-navigation-applications-menu/ApplicationsMenuPage';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	applicationsMenuPageTest,
	loginTest,
);

test('headlesstest', async ({
	page,
	apiHelpers,
	applicationsMenuPage,
}) => {
	const virtualInstance = await apiHelpers.headlessPortalInstances.postPortalInstance();
	const baseUrl = "http://" + virtualInstance.virtualHost + ":8080/o/"
	const base64encodedData = Buffer.from("test@liferaytest.com" + ':' + "test").toString('base64');
	const headers = {
			'Authorization': 'Basic ' + base64encodedData,
			'Content-Type': 'application/json'
	}

	await page.goto("http://" + virtualInstance.virtualHost + ":8080");
	await page.getByRole('button', { name: 'Sign In' }).click();
	await page.getByLabel('Email Address').fill('test@liferaytest.com');
	await page.getByLabel('Password').fill('test');
	await page.getByLabel('Remember Me').check();
	await page.getByLabel('Sign In- Loading').getByRole('button', { name: 'Sign In' }).click();

	const sitePost = await page.request.post(
		`${baseUrl}headless-site/v1.0/sites`,
		{
			data: {
				name: 'Minium',
				templateKey: "minium-initializer",
				templateType: "site-initializer",
			},
			headers: headers 	
		}
	);
	const site = JSON.parse(await sitePost.text());

	const userPost = await page.request.post(
		`${baseUrl}headless-admin-user/v1.0/user-accounts`,
		{
			data: {
				alternateName: "alternateName" + getRandomInt(),
				emailAddress: "emailAddress" + getRandomInt() + "@liferay.com",
				familyName: "familyName" + getRandomInt(),
				givenName: "givenName" + getRandomInt(),
				password: "test"
			},
			headers: headers 	
		}
	);
	const user = JSON.parse(await userPost.text());

	const rolesGet = await page.request.get(
		`${baseUrl}headless-admin-user/v1.0/roles?pageSize=30`,
		{
			headers: headers 	
		}
	);
	const roles = JSON.parse(await rolesGet.text());
	const siteMemberRole = roles.items.filter(role => role.name == "Site Member")[0];

	await page.request.post(
		`${baseUrl}headless-admin-user/v1.0/roles/${siteMemberRole.id}/association/user-account/${user.id}/site/${site.id}`,
		{
			headers: headers 	
		}
	);

	const accountPost = await page.request.post(
		`${baseUrl}headless-admin-user/v1.0/accounts`,
		{
			data: {
				name: "name" + getRandomInt(),
				type: 'business',
				accountUserAccounts: [{
					id: user.id
				}],
			},
			headers: headers 	
		}
	);
	const account = JSON.parse(await accountPost.text());

	const userAccountRolesGet = await page.request.get(
		`${baseUrl}headless-admin-user/v1.0/accounts/${account.id}/account-roles`,
		{
			headers: headers 	
		}
	);
	const userAccountRoles = JSON.parse(await userAccountRolesGet.text());
	const userAccountBuyerRole = userAccountRoles.items.filter(accountRole => accountRole.name == "Buyer")[0];

	await page.request.post(
		`${baseUrl}headless-admin-user/v1.0/accounts/by-external-reference-code/${account.externalReferenceCode}/account-roles/${userAccountBuyerRole.id}/user-accounts/by-email-address/${user.emailAddress}`,
		{
			headers: headers 	
		}
	);

	const channelsGet = await page.request.get(
		`${baseUrl}headless-commerce-admin-channel/v1.0/channels`,
		{
			headers: headers 	
		}
	);
	const channels = JSON.parse(await channelsGet.text());
	const channel = channels.items.filter(channel => channel.name == "Minium Portal")[0];

	const skuGet = await page.request.get(
		`${baseUrl}headless-commerce-admin-catalog/v1.0/skus`,
		{
			headers: headers 	
		}
	);
	const skus = JSON.parse(await skuGet.text());
	const sku = skus.items[0];

	const orderPost = await page.request.post(
		`${baseUrl}headless-commerce-admin-order/v1.0/orders`,
		{
			data: {
				accountId: account.id,
				channelId: channel.id,
				currencyCode: 'USD',
				orderItems: [
					{
					quantity: "1",
					skuId: sku.id,
					unitOfMeasure: "",
					unitOfMeasureKey: "",
					unitPrice: sku.price,
					unitPriceWithTaxAmount: sku.price
					}
				],
				orderStatus: '2',
				workflowStatusInfo: {
					"label": "approved",
					"label_i18n": "Approved"
				}
			},
			headers: headers 	
		}
	);
	const order = JSON.parse(await orderPost.text());

	await page.goto('http://localhosttest:8080/web/minium/catalog');
	await page.getByLabel('Open Product Menu').click();
	await page.getByRole('menuitem', { name: 'Site Builder' }).click();
	await page.getByRole('menuitem', { name: 'Pages' }).click();
	await page.locator('li').filter({ hasText: 'CatalogCatalogWidget Page' }).getByRole('button').nth(2).click();
	await page.getByRole('menuitem', { name: 'Permissions' }).click();
	await page.frameLocator('iframe[title="Permissions"]').locator('#guest_ACTION_VIEW').check();
	await page.frameLocator('iframe[title="Permissions"]').getByRole('button', { name: 'Save' }).click();
	await page.getByLabel('close', { exact: true }).click();
	await page.locator('li').filter({ hasText: 'Pending OrdersPending OrdersWidget Page' }).getByRole('button').nth(2).click();
	await page.getByRole('menuitem', { name: 'Permissions' }).click();
	await page.frameLocator('iframe[title="Permissions"]').locator('#guest_ACTION_VIEW').check();
	await page.frameLocator('iframe[title="Permissions"]').getByRole('button', { name: 'Save' }).click();
	await page.getByLabel('close', { exact: true }).click();
	await page.getByRole('button', { name: 'Pending Orders' }).click();
	await page.locator('li').filter({ hasText: 'CheckoutCheckoutWidget Page' }).getByRole('button').nth(2).click();
	await page.getByRole('menuitem', { name: 'Permissions' }).click();
	await page.frameLocator('iframe[title="Permissions"]').locator('#guest_ACTION_VIEW').check();
	await page.frameLocator('iframe[title="Permissions"]').getByRole('button', { name: 'Save' }).click();
	await page.getByLabel('close', { exact: true }).click();

	await page.getByTestId('applicationsMenu').click();
	await page.getByRole('tab', { name: 'Commerce' }).click();
	await page.getByRole('menuitem', { name: 'Channels' }).click();
	await page.getByRole('link', { name: 'Minium Portal' }).click();
	await page.getByLabel('Guest Checkout').check();
	await page.getByRole('link', { name: 'Save' }).click();

	await page.getByLabel('Test Test User Profile').click();
	await page.getByRole('menuitem', { name: 'Sign Out' }).click();

	// go to minium as guest user, create an order, add an address and log in as user
	await page.goto('http://localhosttest:8080/web/minium/catalog');
	await page.locator('#wwxc_column_2d_2_1_add_to_cart').getByRole('button', { name: 'Add to Cart' }).click();
	await page.getByRole('button', { name: '1' }).click();
	await page.getByRole('button', { name: 'Submit' }).click();
	await page.getByPlaceholder('Name', { exact: true }).fill('a');
	await page.getByPlaceholder('Phone Number').fill('a');
	await page.getByPlaceholder('Address', { exact: true }).fill('a');
	await page.getByTitle('Country').selectOption('20137');
	await page.getByPlaceholder('Zip').fill('a');
	await page.getByPlaceholder('City').fill('a');
	await page.getByLabel('Email').fill('a@a.com');
	await page.getByRole('button', { name: 'Continue' }).click();
	await page.getByRole('link', { name: 'Sign In' }).click();
	await page.getByLabel('Email Address').fill(`${user.emailAddress}`);
	await page.getByLabel('Password').fill('test');
	await page.getByLabel('Remember Me').check();
	await page.getByRole('button', { name: 'Sign In' }).click();

	// clean up

	//await page.request.delete(
	//	`${baseUrl}headless-commerce-admin-order/v1.0/orders/${order.id}`,
	//	{
	//		headers: headers 	
	//	}
	//);

	//await applicationsMenuPage.goto()
	//await apiHelpers.headlessPortalInstances.deletePortalInstance(virtualInstance.portalInstanceId);
});
