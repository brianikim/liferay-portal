/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../fixtures/commercePagesTest';

export const test = mergeTests(apiHelpersTest, commercePagesTest);

const getProduct = (
	catalogId: number,
	productName: string,
	specificationKey: string
) => {
	const postProduct = {
		active: true,
		catalogId,
		name: {
			en_US: productName,
		},
		productSpecifications: [
			{
				specificationKey,

				value: {
					en_US: productName,
				},
			},
		],
		productStatus: 0,
		productType: 'simple',
	};

	return postProduct;
};

const getSpecification = (
	optionCategoryId: number,
	optionCategoryName: number,
	priority: number
) => {
	const postSpecification = {
		facetable: true,
		key: optionCategoryName,
		optionCategory: {
			id: optionCategoryId,
			key: optionCategoryName,
			priority,
			title: {
				en_US: optionCategoryName,
			},
		},
		title: {
			en_US: optionCategoryName,
		},
	};

	return postSpecification;
};

test('can sort specifications by specification group priority', async ({
	apiHelpers,
	commerceLayoutsPage,
	specificationFacetsPage,
}) => {
	const pageLabel = 'Specification Facet Page';

	await commerceLayoutsPage.goToPages();
	await commerceLayoutsPage.createWidgetPage(pageLabel);

	await expect(specificationFacetsPage.pageTitle).toBeVisible({
		timeout: 60 * 1000,
	});

	await specificationFacetsPage.goToPage();
	await specificationFacetsPage.addSearchOptionsWidget();
	await specificationFacetsPage.configureSearchOptions();
	await specificationFacetsPage.reloadPage();
	await specificationFacetsPage.addSpecificationFacetWidget();

	const site = await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath(
		'guest'
	);

	const channel = await apiHelpers.headlessCommerceAdminChannel.postChannel(
		'Specification Facet Channel',
		site.id
	);

	const optionCategory1 =
		await apiHelpers.headlessCommerceAdminCatalog.postOptionCategory(
			'Warranty',
			1
		);
	const optionCategory2 =
		await apiHelpers.headlessCommerceAdminCatalog.postOptionCategory(
			'Material',
			0
		);

	const specification1 =
		await apiHelpers.headlessCommerceAdminCatalog.postSpecification(
			getSpecification(
				optionCategory1.id,
				optionCategory1.key,
				optionCategory1.priority
			)
		);
	const specification2 =
		await apiHelpers.headlessCommerceAdminCatalog.postSpecification(
			getSpecification(
				optionCategory2.id,
				optionCategory2.key,
				optionCategory2.priority
			)
		);

	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog(
		'Specification Facet Catalog'
	);

	const product1 = await apiHelpers.headlessCommerceAdminCatalog.postProduct(
		getProduct(catalog.id, 'Product1', specification1.key)
	);
	const product2 = await apiHelpers.headlessCommerceAdminCatalog.postProduct(
		getProduct(catalog.id, 'Product2', specification2.key)
	);

	await specificationFacetsPage.reloadPage();

	const panelList = await specificationFacetsPage.panelList.all();

	const optionCategoryList = ['material', 'warranty'];

	for (let i = 0; i < optionCategoryList.length; i++) {
		await expect(panelList[i]).toHaveText(optionCategoryList[i]);
	}

	await apiHelpers.headlessCommerceAdminCatalog.deleteOptionCategory(
		apiHelpers,
		apiHelpers.headlessCommerceAdminCatalog.basePath,
		optionCategory1.id
	);
	await apiHelpers.headlessCommerceAdminCatalog.deleteOptionCategory(
		apiHelpers,
		apiHelpers.headlessCommerceAdminCatalog.basePath,
		optionCategory2.id
	);

	await apiHelpers.headlessCommerceAdminCatalog.deleteSpecification(
		apiHelpers,
		apiHelpers.headlessCommerceAdminCatalog.basePath,
		specification1.id
	);
	await apiHelpers.headlessCommerceAdminCatalog.deleteSpecification(
		apiHelpers,
		apiHelpers.headlessCommerceAdminCatalog.basePath,
		specification2.id
	);

	await apiHelpers.headlessCommerceAdminCatalog.deleteProduct(
		apiHelpers,
		apiHelpers.headlessCommerceAdminCatalog.basePath,
		product1.productId
	);
	await apiHelpers.headlessCommerceAdminCatalog.deleteProduct(
		apiHelpers,
		apiHelpers.headlessCommerceAdminCatalog.basePath,
		product2.productId
	);

	await apiHelpers.headlessCommerceAdminChannel.deleteChannel(
		apiHelpers,
		apiHelpers.headlessCommerceAdminChannel.basePath,
		channel.id
	);
	await apiHelpers.headlessCommerceAdminCatalog.deleteCatalog(
		apiHelpers,
		apiHelpers.headlessCommerceAdminCatalog.basePath,
		catalog.id
	);

	await commerceLayoutsPage.goToPages();
	await commerceLayoutsPage.deleteSpecificationPage();

	await expect(commerceLayoutsPage.pageLabel).not.toBeVisible({
		timeout: 60 * 1000,
	});
});
