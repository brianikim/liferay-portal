/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';
import getRandomString from '../../../../utils/getRandomString';
import {performUserSwitch} from '../../../../utils/performLogin';
import {
	apiStorefrontSetUp,
	configureOperationsManagerUserForSite,
} from '../../utils/commerce';

export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	loginTest()
);

test(
	'Users with the manage payment methods permission can edit the payment method of an order',
	{tag: ['@COMMERCE-9979', '@LPD-106244-Grouped-7']},
	async ({
		apiHelpers,
		commerceAdminChannelDetailsPage,
		commerceAdminChannelsPage,
		commerceAdminOrderDetailsPage,
		commerceAdminOrdersPage,
		page,
	}) => {
		const {channel, product, site} = await apiStorefrontSetUp(apiHelpers);

		const account = await apiHelpers.headlessAdminUser.postAccount({
			name: 'Commerce Account ' + getRandomString(),
			type: 'business',
		});

		const address =
			await apiHelpers.headlessCommerceAdminAccount.postAddress(
				account.id,
				{
					city: 'Test City',
					countryISOCode: 'US',
					defaultBilling: true,
					defaultShipping: true,
					name: 'Test Address',
					regionISOCode: 'CA',
					street1: 'Test Street',
					zip: '12345',
				}
			);

		await commerceAdminChannelsPage.goto();

		await (
			await commerceAdminChannelsPage.channelsTableRowLink(channel.name)
		).click();

		await commerceAdminChannelDetailsPage.activatePaymentMethod(
			'Money Order',
			'Test'
		);
		await commerceAdminChannelDetailsPage.activateChannelConfiguration(
			'PayPal',
			'Payment Methods'
		);

		const orders = [];

		for (const orderItems of [
			[],
			[{quantity: 1, skuId: String(product.skus[0].id)}],
		]) {
			orders.push(
				await apiHelpers.headlessCommerceAdminOrder.postOrder({
					accountId: account.id,
					billingAddressId: address.id,
					channelId: channel.id,
					orderItems,
					orderStatus: '1',
					paymentMethod: 'money-order',
					paymentStatus: '2',
					shippingAddressId: address.id,
				})
			);
		}

		const companyId = await page.evaluate(() => {
			return Liferay.ThemeDisplay.getCompanyId();
		});

		const operationsManagerUser =
			await configureOperationsManagerUserForSite(
				account,
				apiHelpers,
				companyId,
				site,
				[
					{
						actionIds: ['MANAGE_COMMERCE_ORDER_PAYMENT_METHODS'],
						primaryKey: companyId,
						resourceName: 'com.liferay.commerce.order',
						scope: 1,
					},
				]
			);

		await performUserSwitch(page, operationsManagerUser.alternateName);

		for (const order of orders) {
			await commerceAdminOrdersPage.goto();

			await (
				await commerceAdminOrdersPage.tableRowLink({
					colIndex: 1,
					rowValue: order.id,
				})
			).click();

			await (
				await commerceAdminOrderDetailsPage.orderDetailsTab('Payments')
			).click();

			await expect(
				commerceAdminOrderDetailsPage.paymentMethodName
			).toContainText('Money Order');

			await (
				await commerceAdminOrderDetailsPage.editEntryActionLink(
					'Payment Method',
					'Edit'
				)
			).click();

			for (const paymentMethod of ['Money Order', 'PayPal']) {
				await expect(
					commerceAdminOrderDetailsPage.paymentMethodOption(
						paymentMethod
					)
				).toBeVisible();
			}

			await expect(
				commerceAdminOrderDetailsPage.paymentMethodOption(
					'Authorize.Net'
				)
			).toHaveCount(0);

			await (
				await commerceAdminOrderDetailsPage.paymentMethodRadioButton(
					'PayPal'
				)
			).click();

			await commerceAdminOrderDetailsPage.submitPaymentMethod.click();

			await expect(
				commerceAdminOrderDetailsPage.paymentMethodName
			).toContainText('PayPal');
		}
	}
);
