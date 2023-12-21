/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const {expect, mergeTests} = require('@playwright/test');

import {test as ApiHelpersTest} from '../../fixtures/apiHelpers.fixture';
import {test as applicationsMenuPagesTest} from '../../fixtures/applicationsMenuPages.fixture';
import {test as commercePagesTest} from '../../fixtures/commercePages.fixture';
import {test as dataHelperTest} from '../../fixtures/dataHelper.fixture';
import {getRandomInt} from '../../utils/util';

export const test = mergeTests(
	ApiHelpersTest,
	applicationsMenuPagesTest,
	commercePagesTest,
	dataHelperTest
);

const getRandomPayment = (payment = {}) => {
	const randomPayment = {
		amount: getRandomInt(),
		channelId: getRandomInt(),
		currencyCode: 'USD',
		externalReferenceCode: 'Payment' + getRandomInt(),
		paymentIntegrationKey: 'paypal-integration',
		paymentIntegrationType: 0,
		relatedItemId: getRandomInt(),
		relatedItemName: 'com.liferay.commerce.model.CommerceOrder',
		type: 0,
	};

	return Object.assign(randomPayment, payment);
};

test.afterEach(async ({_dataHelper}) => {
	await _dataHelper.clearData();
});

test('payments page is visible', async ({
	_apiHelpers,
	_applicationsMenuPage,
	_paymentsPage,
}) => {
	await _apiHelpers.featureFlag.updateFeatureFlag('COMMERCE-12754', 'false');

	await _applicationsMenuPage.goToCommerce();

	await expect(_applicationsMenuPage.paymentsMenuItem).toHaveCount(0);

	await _apiHelpers.featureFlag.updateFeatureFlag('COMMERCE-12754', 'true');

	const payment = await _apiHelpers.headlessCommerceAdminPayment.postRandomPayment(
		getRandomPayment()
	);

	await _apiHelpers.headlessCommerceAdminPayment.patchPayment(payment.id, {
		paymentStatus: 0,
	});

	await _paymentsPage.goto();

	await expect(_paymentsPage.title).toBeVisible();
});
