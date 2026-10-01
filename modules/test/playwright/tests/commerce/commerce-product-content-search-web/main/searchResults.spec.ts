/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../../fixtures/featureFlagsTest';
import {isolatedSiteTest} from '../../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {searchPageTest} from '../../../../fixtures/searchPageTest';
import {DataApiHelpers} from '../../../../helpers/ApiHelpers';
import {SpecificationFacetsPage} from '../../../../pages/commerce/commerce-product-content-search-web/specificationFacetsPage';
import {getRandomInt} from '../../../../utils/getRandomInt';
import getRandomString from '../../../../utils/getRandomString';
import getPageDefinition from '../../../layout-content-page-editor-web/main/utils/getPageDefinition';
import getWidgetDefinition from '../../../layout-content-page-editor-web/main/utils/getWidgetDefinition';
import {commerceSearchPageSetUp} from '../../utils/commerce';

export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	featureFlagsTest({
		'LPS-178052': {enabled: true},
	}),
	isolatedSiteTest,
	loginTest(),
	searchPageTest
);

async function setUpSearchResultsPage(
	apiHelpers: DataApiHelpers,
	page: Page,
	site: Site,
	specificationFacetsPage: SpecificationFacetsPage,
	searchResultsWidgetConfig: Record<string, string>
) {
	const {catalogId, keyword, url} = await commerceSearchPageSetUp(
		apiHelpers,
		site,
		[
			getWidgetDefinition({
				id: getRandomString(),
				widgetConfig: searchResultsWidgetConfig,
				widgetName:
					'com_liferay_commerce_product_content_search_web_internal_portlet_CPSearchResultsPortlet',
			}),
		]
	);

	const productNames = [];

	for (let i = 0; i < 5; i++) {
		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId,
				name: {en_US: `${keyword} ${getRandomInt()}`},
			});

		productNames.push(product.name.en_US);
	}

	await expect(async () => {
		await page.goto(url);

		await expect(
			specificationFacetsPage.searchResult(keyword).first()
		).toBeVisible({timeout: 5000});
	}).toPass({timeout: 60000});

	return productNames;
}

test(
	'Search should always properly strip keyword of leading asterisk when searching for a product',
	{tag: ['@LPD-88196']},
	async ({apiHelpers, page, searchPage, site}) => {
		await apiHelpers.headlessCommerceAdminChannel.postChannel({
			siteGroupId: site.id,
		});

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product1 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		const product2 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		const layout = await apiHelpers.headlessDelivery.createSitePage({
			pageDefinition: getPageDefinition([
				getWidgetDefinition({
					id: getRandomString(),
					widgetConfig: {
						allowEmptySearches: 'true',
						keywordsParameterName: 'q',
						searchScope: 'everything',
					},
					widgetName:
						'com_liferay_portal_search_web_search_bar_portlet_SearchBarPortlet',
				}),
				getWidgetDefinition({
					id: getRandomString(),
					widgetConfig: {
						allowEmptySearches: 'true',
					},
					widgetName:
						'com_liferay_portal_search_web_search_options_portlet_SearchOptionsPortlet',
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

		await page.goto(`/web/${site.name}/${layout.friendlyUrlPath}`);

		await expect(page.getByText(product1.name.en_US)).toBeVisible();

		await expect(page.getByText(product2.name.en_US)).toBeVisible();

		await page.goto(
			`/web/${site.name}/${layout.friendlyUrlPath}?q=*(cpProductId:${product1.productId})`
		);

		await expect(page.getByText(product1.name.en_US)).toBeVisible();

		await expect(page.getByText(product2.name.en_US)).not.toBeVisible();

		await searchPage.searchBarInputInMainContent.fill(
			`*(cpProductId:${product2.productId})`
		);
		await searchPage.searchBarInputInMainContent.press('Enter');

		await expect(page.getByText(product1.name.en_US)).not.toBeVisible();

		await expect(page.getByText(product2.name.en_US)).toBeVisible();
	}
);

test(
	'Commerce search results should still display when a search bar on the same page targets a different destination',
	{tag: ['@LPD-97403']},
	async ({apiHelpers, page, searchPage, site}) => {
		await apiHelpers.headlessCommerceAdminChannel.postChannel({
			siteGroupId: site.id,
		});

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product1 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		const product2 =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		const searchLayout = await apiHelpers.headlessDelivery.createSitePage({
			pageDefinition: getPageDefinition([
				getWidgetDefinition({
					id: getRandomString(),
					widgetName:
						'com_liferay_commerce_product_content_search_web_internal_portlet_CPSearchResultsPortlet',
				}),
			]),
			siteId: site.id,
			title: getRandomString(),
		});

		const layout = await apiHelpers.headlessDelivery.createSitePage({
			pageDefinition: getPageDefinition([
				getWidgetDefinition({
					id: getRandomString(),
					widgetConfig: {
						allowEmptySearches: 'true',
						destination: searchLayout.friendlyUrlPath,
						keywordsParameterName: 'q',
						searchScope: 'everything',
					},
					widgetName:
						'com_liferay_portal_search_web_search_bar_portlet_SearchBarPortlet',
				}),
				getWidgetDefinition({
					id: getRandomString(),
					widgetConfig: {
						allowEmptySearches: 'true',
					},
					widgetName:
						'com_liferay_portal_search_web_search_options_portlet_SearchOptionsPortlet',
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

		await page.goto(`/web/${site.name}/${layout.friendlyUrlPath}`);

		await expect(searchPage.searchBarInputInMainContent).toBeVisible();

		await expect(page.getByText(product1.name.en_US)).toBeVisible();

		await expect(page.getByText(product2.name.en_US)).toBeVisible();

		await searchPage.searchBarInputInMainContent.fill(product2.name.en_US);
		await searchPage.searchBarInputInMainContent.press('Enter');

		await page.waitForURL(`**${searchLayout.friendlyUrlPath}**`);

		await expect(page.getByText(product2.name.en_US)).toBeVisible();

		await expect(page.getByText(product1.name.en_US)).not.toBeVisible();
	}
);

test(
	'Search results should hide the pagination bar when pagination is disabled',
	{tag: ['@COMMERCE-6664', '@LPD-106244-Grouped-9']},
	async ({apiHelpers, page, site, specificationFacetsPage}) => {
		const productNames = await setUpSearchResultsPage(
			apiHelpers,
			page,
			site,
			specificationFacetsPage,
			{paginate: 'false'}
		);

		for (const productName of productNames) {
			await expect(
				specificationFacetsPage.searchResult(productName)
			).toBeVisible();
		}

		await expect(
			specificationFacetsPage.searchResultsPaginationBar
		).toHaveCount(0);
	}
);

test(
	'Search results should display the configured number of items per page',
	{tag: ['@COMMERCE-6146', '@LPD-106244-Grouped-9']},
	async ({apiHelpers, page, site, specificationFacetsPage}) => {
		await setUpSearchResultsPage(
			apiHelpers,
			page,
			site,
			specificationFacetsPage,
			{paginationDelta: '4'}
		);

		await expect(
			specificationFacetsPage.searchResultsPaginationBar
		).toBeVisible();
		await expect(
			specificationFacetsPage.searchResultsItemsPerPageButton(4)
		).toBeVisible();
		await expect(
			specificationFacetsPage.searchResultsPaginationResults
		).toContainText('1 to 4 of 5');
	}
);
