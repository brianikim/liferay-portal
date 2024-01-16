/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpers.fixture';
import {commercePagesTest} from '../../fixtures/commercePages.fixture';

export const test = mergeTests(apiHelpersTest, commercePagesTest);

const getCatalog = (catalogName: string) => {
	const postCatalog = {
		accountId: 0,
		currencyCode: 'USD',
		defaultLanguageId: 'en_US',
		name: catalogName,
	};

	return postCatalog;
};

const getChannel = (channelName: string, siteGroupId: string) => {
	const postChannel = {
		currencyCode: 'USD',
		name: channelName,
		siteGroupId,
		type: 'site',
	};

	return postChannel;
};

const getOptionCategory = (optionCategoryName: string, priority: number) => {
	const postOptionCategory = {
		key: optionCategoryName,
		priority,
		title: {
			en_US: optionCategoryName,
		},
	};

	return postOptionCategory;
};

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
	_apiHelpers,
	_layoutsPage,
	_specificationFacetsPage,
}) => {
	const pageLabel = 'Specification Facet Page';

	await _layoutsPage.createWidgetPage(pageLabel);

	await expect(
		_layoutsPage.page.getByRole('heading', {
			name: pageLabel,
		})
	).toBeVisible({timeout: 60 * 1000});

	await _specificationFacetsPage.addSearchOptionsWidget();
	await _specificationFacetsPage.addSpecificationFacetWidget();

	const site = await _apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath(
		'guest'
	);

	const channel = await _apiHelpers.headlessCommerceAdminChannel.postChannel(
		getChannel('Specification Facet Channel', site.id)
	);

	const optionCategory1 = await _apiHelpers.headlessCommerceAdminCatalog.postOptionCategory(
		getOptionCategory('Warranty', 0)
	);
	const optionCategory2 = await _apiHelpers.headlessCommerceAdminCatalog.postOptionCategory(
		getOptionCategory('Material', 1)
	);

	const specification1 = await _apiHelpers.headlessCommerceAdminCatalog.postSpecification(
		getSpecification(
			optionCategory1.id,
			optionCategory1.key,
			optionCategory1.priority
		)
	);
	const specification2 = await _apiHelpers.headlessCommerceAdminCatalog.postSpecification(
		getSpecification(
			optionCategory2.id,
			optionCategory2.key,
			optionCategory2.priority
		)
	);

	const catalog = await _apiHelpers.headlessCommerceAdminCatalog.postCatalog(
		getCatalog('Specification Facet Catalog')
	);

	const product1 = await _apiHelpers.headlessCommerceAdminCatalog.postProduct(
		getProduct(catalog.id, 'Product1', specification1.key)
	);
	const product2 = await _apiHelpers.headlessCommerceAdminCatalog.postProduct(
		getProduct(catalog.id, 'Product2', specification2.key)
	);

	await _specificationFacetsPage.configureSearchOptions();

	await _specificationFacetsPage.goToPage();

	await expect(
		_layoutsPage.page.getByRole('heading', {
			name: pageLabel,
		})
	).toBeVisible();

	await expect(
		_layoutsPage.page.getByTestId(optionCategory1.key + '1')
	).toBeVisible({timeout: 60 * 1000})

	await expect(
		_layoutsPage.page.getByTestId(optionCategory2.key + '2')
	).toBeVisible({timeout: 60 * 1000});

	await _apiHelpers.headlessCommerceAdminCatalog.deleteOptionCategory(
		_apiHelpers,
		_apiHelpers.headlessCommerceAdminCatalog.basePath,
		optionCategory1.id
	);
	await _apiHelpers.headlessCommerceAdminCatalog.deleteOptionCategory(
		_apiHelpers,
		_apiHelpers.headlessCommerceAdminCatalog.basePath,
		optionCategory2.id
	);

	await _apiHelpers.headlessCommerceAdminCatalog.deleteSpecification(
		_apiHelpers,
		_apiHelpers.headlessCommerceAdminCatalog.basePath,
		specification1.id
	);
	await _apiHelpers.headlessCommerceAdminCatalog.deleteSpecification(
		_apiHelpers,
		_apiHelpers.headlessCommerceAdminCatalog.basePath,
		specification2.id
	);

	await _apiHelpers.headlessCommerceAdminCatalog.deleteProduct(
		_apiHelpers,
		_apiHelpers.headlessCommerceAdminCatalog.basePath,
		product1.productId
	);
	await _apiHelpers.headlessCommerceAdminCatalog.deleteProduct(
		_apiHelpers,
		_apiHelpers.headlessCommerceAdminCatalog.basePath,
		product2.productId
	);

	await _apiHelpers.headlessCommerceAdminChannel.deleteChannel(
		_apiHelpers,
		_apiHelpers.headlessCommerceAdminChannel.basePath,
		channel.id
	);
	await _apiHelpers.headlessCommerceAdminCatalog.deleteCatalog(
		_apiHelpers,
		_apiHelpers.headlessCommerceAdminCatalog.basePath,
		catalog.id
	);

	await _layoutsPage.deleteSpecificationPage();

	await expect(
		_layoutsPage.page.getByRole('heading', {
			name: pageLabel,
		})
	).not.toBeVisible({timeout: 60 * 1000});
});
