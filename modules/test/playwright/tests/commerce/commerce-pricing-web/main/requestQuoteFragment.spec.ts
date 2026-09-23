/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {displayPageTemplatesPagesTest} from '../../../../fixtures/displayPageTemplatesPagesTest';
import {isolatedSiteTest} from '../../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {pageEditorPagesTest} from '../../../../fixtures/pageEditorPagesTest';
import getRandomString from '../../../../utils/getRandomString';
import {
	performLoginViaApi,
	performLogout,
} from '../../../../utils/performLogin';
import {createAccountWithBuyerUser} from '../../utils/commerce';

export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	displayPageTemplatesPagesTest,
	isolatedSiteTest,
	pageEditorPagesTest,
	loginTest()
);

test(
	'Buyer sees the Request A Quote button and Price on Application label from the Request Quote and Price fragments on a default product display page template',
	{tag: ['@COMMERCE-11324']},
	async ({
		apiHelpers,
		displayPageTemplatesPage,
		page,
		pageEditorPage,
		productDetailsPage,
		site,
	}) => {
		await apiHelpers.headlessCommerceAdminChannel.postChannel({
			siteGroupId: site.id,
		});

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
				name: getRandomString(),
			});

		const priceOnApplicationProduct =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: 'Product' + getRandomString()},
			});

		const listPriceProduct =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: 'Product' + getRandomString()},
			});

		const basePriceListId = (
			await apiHelpers.headlessCommerceAdminPricing.getBasePriceListId(
				catalog.id
			)
		).items[0].id;

		await apiHelpers.headlessCommerceAdminPricing.postPriceEntry({
			price: 0,
			priceListId: basePriceListId,
			priceOnApplication: true,
			skuId: priceOnApplicationProduct.skus[0].id,
		});

		await apiHelpers.headlessCommerceAdminPricing.postPriceEntry({
			price: 15,
			priceListId: basePriceListId,
			skuId: listPriceProduct.skus[0].id,
		});

		await displayPageTemplatesPage.createDefaultTemplateWithFragments({
			className: 'com.liferay.commerce.product.model.CPDefinition',
			fragments: [
				{collectionName: 'Product', fragmentName: 'Request Quote'},
				{collectionName: 'Product', fragmentName: 'Price'},
			],
			pageEditorPage,
			site,
		});

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		await page.goto(
			`/web${site.friendlyUrlPath}/p/${priceOnApplicationProduct.name['en_US']}`
		);

		await expect(productDetailsPage.requestAQuoteButton).toBeVisible();

		await expect(
			productDetailsPage.priceContainer.getByText(
				'Price on Application',
				{
					exact: true,
				}
			)
		).toBeVisible();

		await page.goto(
			`/web${site.friendlyUrlPath}/p/${listPriceProduct.name['en_US']}`
		);

		await expect(
			await productDetailsPage.priceField(
				'$ 15.00',
				productDetailsPage.priceContainer
			)
		).toBeVisible();

		await expect(productDetailsPage.requestAQuoteButton).toBeHidden();
	}
);
