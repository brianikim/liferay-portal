/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';
import getGlobalSiteId from '../../../../utils/getGlobalSiteId';
import getRandomString from '../../../../utils/getRandomString';
import getPageDefinition from '../../../layout-content-page-editor-web/main/utils/getPageDefinition';
import getWidgetDefinition from '../../../layout-content-page-editor-web/main/utils/getWidgetDefinition';
import {apiStorefrontSetUp} from '../../utils/commerce';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	dataApiHelpersTest,
	loginTest()
);

test('LPD-3381 Error message is shown when saving an existing override category display page entry without selecting a valid category', async ({
	apiHelpers,
	commerceAdminChannelDetailsCategoryDisplayPagesPage,
	commerceAdminChannelDetailsPage,
	commerceAdminChannelsPage,
}) => {
	await apiHelpers.headlessAdminSite.postSite({
		name: 'Minium',
		templateKey: 'minium-initializer',
		templateType: 'site-initializer',
	});

	const pageName = 'Placed Orders';

	const channels =
		await apiHelpers.headlessCommerceAdminChannel.getChannelsPage(
			'Minium Portal'
		);

	apiHelpers.data.push({id: channels.items[0].id, type: 'channel'});

	const catalogs =
		await apiHelpers.headlessCommerceAdminCatalog.getCatalogsPage('Minium');

	apiHelpers.data.push({id: catalogs.items[0].id, type: 'catalog'});

	const products =
		await apiHelpers.headlessCommerceAdminCatalog.getProductsPage(50, '');

	for (let i = 0; i < products.totalCount; i++) {
		if (products.items[i].catalogId === catalogs.items[0].id) {
			apiHelpers.data.push({
				id: products.items[i].productId,
				type: 'product',
			});
		}
	}
	const options = await apiHelpers.headlessCommerceAdminCatalog.getOptions();

	for (let i = 0; i < options.totalCount; i++) {
		apiHelpers.data.push({
			id: options.items[i].id,
			type: 'option',
		});
	}

	const optionCategories =
		await apiHelpers.headlessCommerceAdminCatalog.getOptionCategories();

	for (let i = 0; i < optionCategories.totalCount; i++) {
		apiHelpers.data.push({
			id: optionCategories.items[i].id,
			type: 'optionCategory',
		});
	}

	const specifications =
		await apiHelpers.headlessCommerceAdminCatalog.getSpecifications();

	for (let i = 0; i < specifications.totalCount; i++) {
		apiHelpers.data.push({
			id: specifications.items[i].id,
			type: 'specification',
		});
	}

	const warehouses =
		await apiHelpers.headlessCommerceAdminInventoryApiHelper.getWarehousesPage();

	for (let i = 0; i < warehouses.totalCount; i++) {
		apiHelpers.data.push({
			id: warehouses.items[i].id,
			type: 'warehouse',
		});
	}

	await commerceAdminChannelsPage.goto();

	await (
		await commerceAdminChannelsPage.channelsTableRowLink(
			channels.items[0].name
		)
	).click();

	await commerceAdminChannelDetailsPage.goToCategoryDisplayPages();

	await commerceAdminChannelDetailsCategoryDisplayPagesPage.addDisplayLayoutButton.click();

	await commerceAdminChannelDetailsCategoryDisplayPagesPage.clickFrameButton(
		'Choose'
	);

	await expect(
		commerceAdminChannelDetailsCategoryDisplayPagesPage.frameSearchBar
	).toBeVisible();

	await commerceAdminChannelDetailsCategoryDisplayPagesPage.selectCategoryDisplayPage(
		pageName
	);

	await expect(
		await commerceAdminChannelDetailsCategoryDisplayPagesPage.categoryDisplayPageLabel(
			pageName
		)
	).toBeVisible();

	await commerceAdminChannelDetailsCategoryDisplayPagesPage.clickFrameButton(
		'Save'
	);

	await expect(
		commerceAdminChannelDetailsCategoryDisplayPagesPage.errorMessageSelectCategory
	).toBeVisible();
});

test(
	'Clicking a category in the categories navigation widget opens the category content page when no default category display page is set',
	{tag: ['@COMMERCE-7843', '@LPD-106244-Grouped-30']},
	async ({apiHelpers, page}) => {
		const {site} = await apiStorefrontSetUp(apiHelpers);

		const vocabulary =
			await apiHelpers.headlessAdminTaxonomy.postSiteTaxonomyVocabulary({
				name: getRandomString(),
				siteId: String(await getGlobalSiteId(apiHelpers)),
			});

		const categoryName = getRandomString();

		await apiHelpers.headlessAdminTaxonomy.postTaxonomyVocabularyTaxonomyCategory(
			{
				name: categoryName,
				vocabularyId: vocabulary.id,
			}
		);

		const layoutTitle = getRandomString();

		const layout = await apiHelpers.headlessDelivery.createSitePage({
			pageDefinition: getPageDefinition([
				getWidgetDefinition({
					id: getRandomString(),
					widgetConfig: {
						assetVocabularyId: String(vocabulary.id),
					},
					widgetName:
						'com_liferay_commerce_product_asset_categories_navigation_web_internal_portlet_CPAssetCategoriesNavigationPortlet',
				}),
				getWidgetDefinition({
					id: getRandomString(),
					widgetName:
						'com_liferay_commerce_product_content_web_internal_portlet_CPCategoryContentPortlet',
				}),
			]),
			siteId: site.id,
			title: layoutTitle,
		});

		await page.goto(`/web${site.friendlyUrlPath}${layout.friendlyUrlPath}`);

		await page.getByRole('link', {exact: true, name: categoryName}).click();

		await expect(page).toHaveURL(
			new RegExp(
				`/web${site.friendlyUrlPath}/g/${categoryName.toLowerCase()}`
			)
		);
		await expect(
			page.getByTestId('headerTitle').filter({hasText: layoutTitle})
		).toBeVisible();
		await expect(
			page
				.locator(
					'[id^="portlet_com_liferay_commerce_product_content_web_internal_portlet_CPCategoryContentPortlet"]'
				)
				.getByText(categoryName)
		).toBeVisible();
	}
);
