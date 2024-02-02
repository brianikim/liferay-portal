/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

//@ts-ignore
import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../fixtures/commercePagesTest';
import {loginTest} from '../../fixtures/loginTest';

export const test = mergeTests(apiHelpersTest, commercePagesTest, loginTest);

test('LPD-5780 modal title and product name appear properly in product menu', async ({
    apiHelpers,
    commerceProductsPage
}) => {

    const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog(
		'Product Catalog'
	);

    const product1 = await apiHelpers.headlessCommerceAdminCatalog.postProduct(
		catalog.id,
		'\"Product1\"'
	);

    const product2 = await apiHelpers.headlessCommerceAdminCatalog.postProduct(
        catalog.id,
        'Product2'
    );

	//double-check data set
	const returnedProduct = await apiHelpers.headlessCommerceAdminCatalog.getProduct(
		product1.productId);

	expect(returnedProduct.name).toBe('\"Product1\"');

	//now check in the actual GUI
    await commerceProductsPage.goto();

	await commerceProductsPage.goToSpecificProductMenu(product1.name);

	await commerceProductsPage.addSpareProductRelation();

	await expect(
		commerceProductsPage.getByRole(
			'heading', {name: 'Add New Product to \"Product1\"'})
	).toBeVisible();

	await commerceProductsPage.modalCloseButton.click();

	await commerceProductsPage.goto();

	await commerceProductsPage.goToSpecificProductMenu(product2.name);

	await commerceProductsPage.addSpareProductRelation();

	await commerceProductsPage
		.frameLocator('#modalIframe')
		.getByRole('checkbox', {value: product1.productId})
	.check();

	await commerceProductsPage.modalAddButton.click();

	await expect(
		commerceProductsPage.getByRole('link', {name: product1.name}))
	.toBeVisible();

	//this almost certainly could be handled using an apihelper
	await commerceProductsPage.itemOptionMenu.click();

	await commerceProductsPage.deleteMenuItem.click();

    await apiHelpers.headlessCommerceAdminCatalog.deleteProduct(
		product1.productId
	);
	await apiHelpers.headlessCommerceAdminCatalog.deleteProduct(
		product2.productId
	);

	await apiHelpers.headlessCommerceAdminCatalog.deleteCatalog(catalog.id);
});