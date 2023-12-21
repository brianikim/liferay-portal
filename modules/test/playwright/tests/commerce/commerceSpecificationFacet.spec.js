/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const {expect, mergeTests} = require('@playwright/test');

import {test as ApiHelpersTest} from '../../fixtures/apiHelpers.fixture';
import {test as commercePagesTest} from '../../fixtures/commercePages.fixture';
import {test as dataHelperTest} from '../../fixtures/dataHelper.fixture';

export const test = mergeTests(
	ApiHelpersTest,
	commercePagesTest,
	dataHelperTest
);

const getCatalog = (catalogName) => {
	const postCatalog = {
		accountId: 0,
		currencyCode: 'USD',
		defaultLanguageId: 'en_US',
		name: catalogName,
	};

	return postCatalog;
};

const getChannel = (channelName, siteGroupId) => {
	const postChannel = {
		currencyCode: 'USD',
		name: channelName,
		siteGroupId,
		type: 'site',
	};

	return postChannel;
};

const getOptionCategory = (optionCategoryName, priority) => {
	const postOptionCategory = {
		key: optionCategoryName,
		priority,
		title: {
			en_US: optionCategoryName,
		},
	};

	return postOptionCategory;
};

const getProduct = (catalogId, productName, specificationKey) => {
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

const getSpecification = (optionCategoryId, optionCategoryName, priority) => {
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

test.afterEach(async ({_dataHelper}) => {
	await _dataHelper.clearData();
});

test('can sort specifications by specification group priority', async ({
	_apiHelpers,
	_layoutsPage,
	_specificationFacetsPage,
}) => {
	await _layoutsPage.createWidgetPage('Specification Facet Page');

	await expect(
		_layoutsPage.page.getByRole('heading', {
			name: 'Specification Facet Page',
		})
	).toBeVisible();

	await _layoutsPage.addWidgetsToPage('Specification Facet Page', [
		'Search Options',
		'Specification Facet',
	]);

	const site = await _apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath(
		'guest'
	);

	await _apiHelpers.headlessCommerceAdminChannel.postChannel(
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
			optionCategory1.key + 'Specification',
			optionCategory1.priority
		)
	);
	const specification2 = await _apiHelpers.headlessCommerceAdminCatalog.postSpecification(
		getSpecification(
			optionCategory2.id,
			optionCategory2.key + 'Specification',
			optionCategory2.priority
		)
	);

	const catalog = await _apiHelpers.headlessCommerceAdminCatalog.postCatalog(
		getCatalog('Specification Facet Catalog')
	);

	const product1 = await _apiHelpers.headlessCommerceAdminCatalog.postProduct(
		getProduct(catalog.id, 'Product 1', specification1.key)
	);
	const product2 = await _apiHelpers.headlessCommerceAdminCatalog.postProduct(
		getProduct(catalog.id, 'Product 2', specification2.key)
	);

	await _specificationFacetsPage.configureSearchOptions();

	await expect(
		_layoutsPage.page.getByRole('heading', {
			name: 'Specification Facet Page',
		})
	).toBeVisible();

	await _specificationFacetsPage.configureSpecificationFacet();

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
});
