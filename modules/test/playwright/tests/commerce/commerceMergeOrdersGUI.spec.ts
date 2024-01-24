/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../fixtures/commercePagesTest';
import {loginTest} from '../../fixtures/loginTest';
import { applicationsMenuPageTest } from '../../fixtures/applicationsMenuPageTest';
import { getRandomInt } from '../../utils/util';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	applicationsMenuPageTest,
	loginTest,
);

test('can merge guest order with user order after logging in', async ({
	page,
	apiHelpers,
	applicationsMenuPage,
}) => {
	const virtualInstance = await apiHelpers.headlessPortalInstances.postPortalInstance();

	await page.goto("http://" + virtualInstance.virtualHost + ":8080");
	await page.getByRole('button', { name: 'Sign In' }).click();
	await page.getByLabel('Email Address').fill('test@liferaytest.com');
	await page.getByLabel('Password').fill('test');
	await page.getByLabel('Remember Me').check();
	await page.getByLabel('Sign In- Loading').getByRole('button', { name: 'Sign In' }).click();

	await page.getByLabel('Open Applications MenuCtrl+Alt+A').click();
	await page.getByRole('tab', { name: 'Control Panel' }).click();
	await page.getByRole('menuitem', { name: 'Sites' }).click();
	await page.getByRole('link', { name: 'Add Site' }).click();
	await page.getByRole('button', { name: 'Select Template: Minium', exact: true }).click();
	await page.frameLocator('iframe[title="Add Site"]').getByLabel('Name').fill('Minium');
	await page.frameLocator('iframe[title="Add Site"]').getByRole('button', { name: 'Add' }).click();
	await page.waitForURL('http://localhosttest:8080/group/minium/~/control_panel/manage/-/site/settings?_com_liferay_site_admin_web_portlet_SiteSettingsPortlet_redirect=http%3A%2F%2Flocalhosttest%3A8080%2Fgroup%2Fminium%2F%7E%2Fcontrol_panel%2Fmanage%2F-%2Fsite%2Fsettings&_com_liferay_site_admin_web_portlet_SiteSettingsPortlet_historyKey=&p_p_state=normal');

	// add page permissions
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

	// toggle guest checkout in channel
	await page.getByLabel('Open Applications MenuCtrl+Alt+A').click();
	await page.getByRole('tab', { name: 'Commerce' }).click();
	await page.getByRole('menuitem', { name: 'Channels' }).click();
	await page.getByRole('link', { name: 'Minium Portal' }).click();
	await page.getByLabel('Guest Checkout').check();
	await page.getByRole('link', { name: 'Save' }).click();

	// create user and add it as member to minium
	await page.getByLabel('Open Applications MenuCtrl+Alt+A').click();
	await page.getByRole('tab', { name: 'Control Panel' }).click();
	await page.getByRole('menuitem', { name: 'Users and Organizations' }).click();
	await page.getByRole('link', { name: 'Add User' }).click();
	await page.getByLabel('Screen Name').fill('test2');
	await page.getByLabel('Email Address').fill('test2@liferaytest.com');
	await page.getByLabel('First Name').fill('test');
	await page.getByLabel('Last Name').fill('test');
	await page.getByRole('button', { name: 'Save' }).click();
	await page.getByRole('link', { name: 'Password' }).click();
	await page.getByLabel('New Password').fill('test');
	await page.getByLabel('Reenter Password').fill('test');
	await page.getByRole('button', { name: 'Save' }).click();
	await page.getByRole('link', { name: 'Memberships' }).click();
	await page.getByLabel('Select Sites').click();
	await page.frameLocator('iframe[title="Select Site"]').getByRole('link', { name: 'Minium' }).click();
	await page.getByRole('button', { name: 'Save' }).click();

	// assign user and role
	await page.getByLabel('Open Applications MenuCtrl+Alt+A').click();
	await page.getByRole('menuitem', { name: 'Accounts', exact: true }).click();
	await page.getByRole('link', { name: 'Add Account' }).click();
	await page.getByLabel('Account Name').fill('account');
	await page.getByRole('button', { name: 'Save' }).click();
	await page.getByRole('link', { name: 'Users' }).click();
	await page.getByLabel('New').click();
	await page.getByRole('menuitem', { name: 'Assign Users' }).click();
	await page.frameLocator('iframe[title="Assign Users to account"]').getByLabel('test test').check();
	await page.getByRole('button', { name: 'Assign' }).click();
	await page.getByLabel('Show Actions').click();
	await page.getByRole('menuitem', { name: 'Assign Roles' }).click();
	await page.frameLocator('iframe[title="Assign Roles"]').getByLabel('Buyer').check();
	await page.getByRole('button', { name: 'Done' }).click();

	// // sign out and sign in as new user
	// await page.getByRole('link', { name: 'Sign Out' }).click();
	// await page.getByRole('button', { name: 'Sign In' }).click();
	// await page.getByLabel('Email Address').fill('test2@liferaytest.com');
	// await page.getByLabel('Password').fill('test');
	// await page.getByLabel('Sign In- Loading').getByRole('button', { name: 'Sign In' }).click();
	// await page.goto('http://localhosttest:8080/web/minium/catalog');
	// await page.locator('#vpth_column_2d_2_1_add_to_cart').getByRole('button', { name: 'Add to Cart' }).click();
	// await page.getByRole('link', { name: 'Sign Out' }).click();

	// // go to minium as guest user, create an order, add an address and log in as user
	// await page.goto('http://localhosttest:8080/web/minium/catalog');
	// await page.locator('#wwxc_column_2d_2_1_add_to_cart').getByRole('button', { name: 'Add to Cart' }).click();
	// await page.getByRole('button', { name: '1' }).click();
	// await page.getByRole('button', { name: 'Submit' }).click();
	// await page.getByPlaceholder('Name', { exact: true }).fill('a');
	// await page.getByPlaceholder('Phone Number').fill('a');
	// await page.getByPlaceholder('Address', { exact: true }).fill('a');
	// await page.getByTitle('Country').selectOption('20137');
	// await page.getByPlaceholder('Zip').fill('a');
	// await page.getByPlaceholder('City').fill('a');
	// await page.getByLabel('Email').fill('a@a.com');
	// await page.getByRole('button', { name: 'Continue' }).click();
	// await page.getByRole('link', { name: 'Sign In' }).click();
	// await page.getByLabel('Email Address').fill('test2@liferaytest.com');
	// await page.getByLabel('Password').fill('test');
	// await page.getByLabel('Remember Me').check();
	// await page.getByRole('button', { name: 'Sign In' }).click();

	// // open minicart and click view details / go to order details view
	// await page.getByRole('button', { name: '2' }).click();
	// await page.getByRole('button', { name: 'View Details' }).click();


	// clean up
	console.log(virtualInstance);
	await apiHelpers.headlessPortalInstances.deletePortalInstance(
		virtualInstance.portalInstanceId
	);
});
