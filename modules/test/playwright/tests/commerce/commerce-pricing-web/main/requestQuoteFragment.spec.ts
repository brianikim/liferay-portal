/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
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
	{tag: ['@COMMERCE-11324', '@LPD-106244']},
	async ({
		apiHelpers,
		displayPageTemplatesPage,
		page,
		pageEditorPage,
		productDetailsPage,
		site,
	}) => {

		// Create the channel, catalog and products

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

		// Set one product's SKU to Price on Application and give the other a list price

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

		// Create a default product display page template with the Request Quote and Price fragments

		const displayPageTemplateName = getRandomString();

		const {classNameId} =
			await apiHelpers.jsonWebServicesClassName.fetchClassName(
				'com.liferay.commerce.product.model.CPDefinition'
			);

		const {layoutPageTemplateEntryId} =
			await apiHelpers.jsonWebServicesLayoutPageTemplateEntry.addDisplayPageLayoutPageTemplateEntry(
				{
					classNameId,
					groupId: String(site.id),
					name: displayPageTemplateName,
				}
			);

		await apiHelpers.jsonWebServicesLayoutPageTemplateEntry.markAsDefaultDisplayPageLayoutPageTemplateEntry(
			{layoutPageTemplateEntryId}
		);

		await displayPageTemplatesPage.goto(site.friendlyUrlPath);

		await displayPageTemplatesPage.editTemplate(displayPageTemplateName);

		await pageEditorPage.addFragment('Product', 'Request Quote');
		await pageEditorPage.addFragment('Product', 'Price');

		await pageEditorPage.waitForChangesSaved();

		await displayPageTemplatesPage.publishTemplate();

		// View the storefront as a buyer

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		// Verify the Price on Application product shows the Request A Quote button and the Price on Application label

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

		// Verify the list price product shows its price without the Request A Quote button

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
