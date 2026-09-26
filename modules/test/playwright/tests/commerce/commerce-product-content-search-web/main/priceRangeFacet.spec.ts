/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../../fixtures/featureFlagsTest';
import {isolatedSiteTest} from '../../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {DataApiHelpers} from '../../../../helpers/ApiHelpers';
import getRandomString from '../../../../utils/getRandomString';
import getPageDefinition from '../../../layout-content-page-editor-web/main/utils/getPageDefinition';
import getWidgetDefinition from '../../../layout-content-page-editor-web/main/utils/getWidgetDefinition';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	dataApiHelpersTest,
	featureFlagsTest({
		'LPS-178052': {enabled: true},
	}),
	isolatedSiteTest,
	loginTest()
);

async function setUpPriceRangeFacetPage(
	apiHelpers: DataApiHelpers,
	page: Page,
	site: Site,
	priceRangeFacetWidgetConfig?: Record<string, string>
) {
	await apiHelpers.headlessCommerceAdminChannel.postChannel({
		siteGroupId: site.id,
	});

	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
		name: getRandomString(),
	});

	const keyword = getRandomString().replace(/-/g, '');

	const productNames: Record<number, string> = {};

	for (const price of [12, 30, 55, 75, 150, 250, 600]) {
		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: `${keyword} ${getRandomString()}`},
				skus: [
					{
						cost: 0,
						price,
						published: true,
						purchasable: true,
						sku: getRandomString(),
					},
				],
			});

		productNames[price] = product.name['en_US'];
	}

	const layout = await apiHelpers.headlessDelivery.createSitePage({
		pageDefinition: getPageDefinition([
			getWidgetDefinition({
				id: getRandomString(),
				widgetConfig: {
					keywordsParameterName: 'q',
					searchScope: 'everything',
				},
				widgetName:
					'com_liferay_portal_search_web_search_bar_portlet_SearchBarPortlet',
			}),
			getWidgetDefinition({
				id: getRandomString(),
				widgetConfig: priceRangeFacetWidgetConfig,
				widgetName:
					'com_liferay_commerce_product_content_search_web_internal_portlet_CPPriceRangeFacetsPortlet',
			}),
			getWidgetDefinition({
				id: getRandomString(),
				widgetName:
					'com_liferay_commerce_product_content_search_web_internal_portlet_CPSearchResultsPortlet',
			}),
		]),
		siteId: site.id,
		title: getRandomString(),
	});

	const url = `/web/${site.name}/${layout.friendlyUrlPath}?q=${keyword}`;

	await expect(async () => {
		await page.goto(url);

		for (const productName of Object.values(productNames)) {
			await expect(page.getByText(productName)).toBeVisible({
				timeout: 5000,
			});
		}
	}).toPass({timeout: 60000});

	return {productNames, url};
}

test(
	'Price Range Facet should not throw console error if no price ranges exist',
	{tag: ['@LPD-83575']},
	async ({apiHelpers, page, site}) => {
		await apiHelpers.headlessCommerceAdminChannel.postChannel({
			siteGroupId: site.id,
		});

		const layout = await apiHelpers.headlessDelivery.createSitePage({
			pageDefinition: getPageDefinition([
				getWidgetDefinition({
					id: getRandomString(),
					widgetName:
						'com_liferay_commerce_product_content_search_web_internal_portlet_CPPriceRangeFacetsPortlet',
				}),
				getWidgetDefinition({
					id: getRandomString(),
					widgetName:
						'com_liferay_commerce_product_content_search_web_internal_portlet_CPSearchResultsPortlet',
				}),
			]),
			siteId: site.id,
			title: getRandomString(),
		});

		const errors = [];

		page.on('pageerror', (exception) => {
			if (exception.message.includes('Cannot read properties of null')) {
				errors.push(exception.message);
			}
		});

		await page.goto(`/web/${site.name}/${layout.friendlyUrlPath}`);

		await page.waitForLoadState('networkidle');

		expect(errors.length).toEqual(0);
	}
);

test(
	'Price Range Facet filters the search results by the selected range and by the minimum and maximum inputs',
	{tag: ['@COMMERCE-6170', '@LPD-106244-Grouped-28']},
	async ({apiHelpers, page, site, specificationFacetsPage}) => {
		const {productNames, url} = await setUpPriceRangeFacetPage(
			apiHelpers,
			page,
			site
		);

		await specificationFacetsPage.priceRangeFacetPortlet
			.getByRole('checkbox', {name: '$ 500.00 - $ 999.99'})
			.click();

		await expect(page.getByText(productNames[600])).toBeVisible();
		await expect(page.getByText(productNames[12])).toBeHidden();

		await page.goto(url);

		await specificationFacetsPage.priceRangeFacetPortlet
			.getByLabel('Minimum')
			.fill('10.00');
		await specificationFacetsPage.priceRangeFacetPortlet
			.getByLabel('Maximum')
			.fill('15.00');
		await specificationFacetsPage.priceRangeFacetPortlet
			.getByRole('button', {name: 'Go'})
			.click();

		await expect(page.getByText(productNames[12])).toBeVisible();
		await expect(page.getByText(productNames[30])).toBeHidden();
	}
);

test(
	'Price Range Facet applies the custom ranges of its configuration',
	{tag: ['@COMMERCE-10907', '@LPD-106244-Grouped-28']},
	async ({apiHelpers, page, site, specificationFacetsPage}) => {
		const {productNames, url} = await setUpPriceRangeFacetPage(
			apiHelpers,
			page,
			site,
			{
				rangesJSONArrayString:
					"[{'range': '[0 TO 59.99]'}, {'range': '[60 TO 99.99]'}, {'range': '[100 TO 199.99]'}, {'range': '[200 TO 499.99]'}, {'range': '[500 TO 999.99]'}, {'range': '[1000 TO *]'}]",
			}
		);

		for (const {count, priceRange} of [
			{count: '(3)', priceRange: '$ 0.00 - $ 59.99'},
			{count: '(1)', priceRange: '$ 60.00 - $ 99.99'},
			{count: '(1)', priceRange: '$ 100.00 - $ 199.99'},
			{count: '(1)', priceRange: '$ 200.00 - $ 499.99'},
			{count: '(1)', priceRange: '$ 500.00 - $ 999.99'},
		]) {
			await expect(
				specificationFacetsPage.priceRangeFacetTermCount(priceRange)
			).toHaveText(count);
		}

		await expect(
			specificationFacetsPage.priceRangeFacetTermCount('$ 1,000.00+')
		).toHaveCount(0);

		for (const {hiddenPrices, priceRange, visiblePrices} of [
			{
				hiddenPrices: [75],
				priceRange: '$ 0.00 - $ 59.99',
				visiblePrices: [12, 30, 55],
			},
			{
				hiddenPrices: [55],
				priceRange: '$ 60.00 - $ 99.99',
				visiblePrices: [75],
			},
		]) {
			await page.goto(url);

			await specificationFacetsPage.priceRangeFacetPortlet
				.getByRole('checkbox', {name: priceRange})
				.click();

			for (const price of visiblePrices) {
				await expect(page.getByText(productNames[price])).toBeVisible();
			}

			for (const price of hiddenPrices) {
				await expect(page.getByText(productNames[price])).toBeHidden();
			}
		}
	}
);

test(
	'Price Range Facet keeps the term counts when ranges are selected',
	{tag: ['@COMMERCE-11970', '@LPD-106244-Grouped-28']},
	async ({apiHelpers, page, site, specificationFacetsPage}) => {
		const {url} = await setUpPriceRangeFacetPage(apiHelpers, page, site);

		const termCounts = [
			{count: '(2)', priceRange: '$ 0.00 - $ 49.99'},
			{count: '(2)', priceRange: '$ 50.00 - $ 99.99'},
			{count: '(1)', priceRange: '$ 100.00 - $ 199.99'},
			{count: '(1)', priceRange: '$ 200.00 - $ 499.99'},
			{count: '(1)', priceRange: '$ 500.00 - $ 999.99'},
		];

		await specificationFacetsPage.priceRangeFacetPortlet
			.getByRole('checkbox', {name: '$ 50.00 - $ 99.99'})
			.click();

		await expect(
			specificationFacetsPage.priceRangeFacetPortlet.getByRole(
				'checkbox',
				{
					name: '$ 50.00 - $ 99.99',
				}
			)
		).toBeChecked();

		for (const {count, priceRange} of termCounts) {
			await expect(
				specificationFacetsPage.priceRangeFacetTermCount(priceRange)
			).toHaveText(count);
		}

		await page.goto(
			url +
				[
					'[0 TO 49.99]',
					'[50 TO 99.99]',
					'[100 TO 199.99]',
					'[200 TO 499.99]',
					'[500 TO 999.99]',
					'[1000 TO 1.7976931348623157E308]',
				]
					.map((range) => `&basePrice=${encodeURIComponent(range)}`)
					.join('')
		);

		for (const {count, priceRange} of termCounts) {
			await expect(
				specificationFacetsPage.priceRangeFacetTermCount(priceRange)
			).toHaveText(count);
		}

		await expect(
			specificationFacetsPage.priceRangeFacetTermCount('$ 1,000.00+')
		).toHaveCount(0);
	}
);
