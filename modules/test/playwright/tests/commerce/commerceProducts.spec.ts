/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// @ts-ignore

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../fixtures/commercePagesTest';
import {loginTest} from '../../fixtures/loginTest';

export const test = mergeTests(apiHelpersTest, commercePagesTest, loginTest);

test('LPD-5780 modal title and product name appear properly in product menu', async ({
	apiHelpers,
	commerceProductAdminPage,
	page,
}) => {

	// CREATE

	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
		name: 'Product Catalog',
	});

	const product1 = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: {
			en_US: '"Product1"',
		},
	});

	const product2 = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: {
			en_US: 'Product2',
		},
	});

	// double-check data set

	const returnedProduct =
		await apiHelpers.headlessCommerceAdminCatalog.getProduct(
			product1.productId
		);

	expect(returnedProduct.name).toBe('"Product1"');

	// now check in the actual GUI

	await commerceProductAdminPage.goto();

	await commerceProductAdminPage.goToSpecificProductMenu(product1.name.en_US);

	await commerceProductAdminPage.addSpareProductRelation();

	await expect(
		page.getByRole('heading', {name: 'Add New Product to "Product1"'})
	).toBeVisible();

	await commerceProductAdminPage.modalCloseButton.click();

	await commerceProductAdminPage.goto();

	await commerceProductAdminPage.goToSpecificProductMenu(product2.name.en_US);

	await commerceProductAdminPage.addSpareProductRelation();

	// there should theoretically only be two checkboxes
	// the one matching our current product would be disabled so we pick the non-disabled one

	await page
		.frameLocator('#modalIframe')
		.getByRole('checkbox', {disabled: false})
		.check();

	await commerceProductAdminPage.modalAddButton.click();

	await expect(
		page.getByRole('link', {name: product1.name.en_US})
	).toBeVisible();

	// CLEAN UP

	// this first part almost certainly could be handled using an apihelper?

	await commerceProductAdminPage.itemOptionMenu.click();

	await commerceProductAdminPage.deleteMenuItem.click();

	await apiHelpers.headlessCommerceAdminCatalog.deleteProduct(
		product1.productId
	);
	await apiHelpers.headlessCommerceAdminCatalog.deleteProduct(
		product2.productId
	);

	await apiHelpers.headlessCommerceAdminCatalog.deleteCatalog(catalog.id);
});
