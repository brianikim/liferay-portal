/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {isolatedSiteTest} from '../../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {userPersonalBarPagesTest} from '../../../../fixtures/userPersonalBarPagesTest';
import {DataApiHelpers} from '../../../../helpers/ApiHelpers';
import {getTableRowCells} from '../../../../pages/commerce/commerce-order-content-web/orderImportPage';
import {CommerceAdminProductDetailsPage} from '../../../../pages/commerce/commerce-product-definitions-web/commerceAdminProductDetailsPage';
import {CommerceAdminProductPage} from '../../../../pages/commerce/commerce-product-definitions-web/commerceAdminProductPage';
import {clickAndExpectToBeVisible} from '../../../../utils/clickAndExpectToBeVisible';
import {getRandomInt} from '../../../../utils/getRandomInt';
import getRandomString from '../../../../utils/getRandomString';
import {userData} from '../../../../utils/performLogin';
import {waitForAlert} from '../../../../utils/waitForAlert';
import {apiStorefrontSetUp} from '../../utils/commerce';
import {fillLocalizedInput} from '../../utils/fillLocalizedInput';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	dataApiHelpersTest,
	isolatedSiteTest,
	loginTest(),
	userPersonalBarPagesTest
);

async function checkProductStatusAfterDuplicationAndConversionToDraft({
	apiHelpers,
	commerceAdminProductDetailsPage,
	commerceAdminProductPage,
	page,
	productStatus,
	status,
}: {
	apiHelpers: DataApiHelpers;
	commerceAdminProductDetailsPage: CommerceAdminProductDetailsPage;
	commerceAdminProductPage: CommerceAdminProductPage;
	page: Page;
	productStatus: number;
	status: string;
}) {
	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		productStatus,
	});

	const productName = product.name['en_US'];

	try {
		await commerceAdminProductPage.gotoProduct(productName);

		await expect(
			commerceAdminProductDetailsPage.saveAsDraftLink
		).toHaveCount(0);
		await expect(
			commerceAdminProductDetailsPage.workflowStatus(status)
		).toBeVisible();

		await commerceAdminProductDetailsPage.duplicate(catalog.name);

		await expect(commerceAdminProductDetailsPage.nameInput).toHaveValue(
			`Copy of ${productName}`
		);
		await expect(
			commerceAdminProductDetailsPage.workflowStatus('Draft')
		).toBeVisible();

		await commerceAdminProductPage.gotoProduct(productName);

		const updatedProductName = getRandomString();

		await commerceAdminProductDetailsPage.nameInput.fill(
			updatedProductName
		);

		page.once('dialog', (dialog) => dialog.dismiss());

		await commerceAdminProductDetailsPage.headerActionsButton.click();
		await commerceAdminProductDetailsPage
			.headerActionsMenuItem('Convert to Draft')
			.click();

		await expect(
			commerceAdminProductDetailsPage.workflowStatus(status)
		).toBeVisible();

		page.once('dialog', async (dialog) => {
			expect(dialog.message()).toBe(
				'Converting the product status to draft will remove the product from the product catalog. Do you wish to proceed?'
			);

			await dialog.accept();
		});

		await commerceAdminProductDetailsPage.headerActionsButton.click();
		await commerceAdminProductDetailsPage
			.headerActionsMenuItem('Convert to Draft')
			.click();

		await expect(
			commerceAdminProductDetailsPage.saveAsDraftLink
		).toBeVisible();
		await expect(
			commerceAdminProductDetailsPage.workflowStatus('Draft')
		).toBeVisible();

		await commerceAdminProductPage.gotoProduct(updatedProductName);

		await expect(commerceAdminProductDetailsPage.nameInput).toHaveValue(
			updatedProductName
		);
		await expect(
			commerceAdminProductDetailsPage.workflowStatus('Draft')
		).toBeVisible();
	}
	finally {
		const copyProduct = (
			await apiHelpers.headlessCommerceAdminCatalog.getProducts(
				new URLSearchParams({
					filter: `name eq 'Copy of ${productName}'`,
				})
			)
		).items[0];

		if (copyProduct) {
			apiHelpers.data.push({id: copyProduct.productId, type: 'product'});
		}
	}
}

test(
	'Add, edit, and delete a SKU',
	{
		tag: [
			'@COMMERCE-5807',
			'@COMMERCE-6021',
			'@COMMERCE-6022',
			'@COMMERCE-6023',
			'@COMMERCE-9891',
			'@LPD-106244-Grouped-4',
		],
	},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		page,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		const sku1 = 'SKU1' + getRandomString();

		await commerceAdminProductDetailsSkusPage.skuAddButton.click();
		await commerceAdminProductDetailsSkusPage.skuAddModalSkuInput.fill(
			sku1
		);
		await commerceAdminProductDetailsSkusPage.skuAddModalSkuPurchasableToggle.check();
		await commerceAdminProductDetailsSkusPage.skuAddModalSkuPublishButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.skuAddModalSuccessMessage
		).toBeVisible();

		const externalReferenceCode = getRandomString();
		const sku2 = 'SKU2' + getRandomString();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink(sku1)
			.click();
		await commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuFieldName.fill(
			sku2
		);
		await commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuExternalReferenceCodeInput.fill(
			externalReferenceCode
		);
		await commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuPublishButton.click();

		await waitForAlert(commerceAdminProductDetailsSkusPage.sidePanelFrame);

		await page.reload();

		await expect(
			commerceAdminProductDetailsSkusPage.skusTableRowLink(sku1)
		).toHaveCount(0);

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink(sku2)
			.click();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuFieldName
		).toHaveValue(sku2);
		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuExternalReferenceCodeInput
		).toHaveValue(externalReferenceCode);

		await page.reload();

		page.once('dialog', (dialog) => dialog.accept());

		await commerceAdminProductPage.productRowActionsButton(sku2).click();
		await commerceAdminProductPage.deleteMenuItem.click();

		await waitForAlert(page);

		await expect(
			commerceAdminProductDetailsSkusPage.skusTableRowLink(sku2)
		).toHaveCount(0);
	}
);

test(
	'Editing the base price and tier prices of a SKU updates its storefront price',
	{tag: ['@COMMERCE-6022', '@COMMERCE-12218', '@LPD-106244-Grouped-4']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		commerceThemeClassicCatalogPage,
		page,
	}) => {
		const {catalog, product, site} = await apiStorefrontSetUp(apiHelpers, [
			{
				title: 'Catalog',
				widgetName:
					'com_liferay_commerce_product_content_search_web_internal_portlet_CPSearchResultsPortlet',
			},
		]);

		const basePriceListName = `${catalog.name} Base Price List`;
		const productCard = commerceThemeClassicCatalogPage.productCard(
			product.name['en_US']
		);

		await test.step('Edit the base price list entry', async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				product.skus[0].sku,
				'Price'
			);
			await commerceAdminProductDetailsSkusPage
				.sidePanelSkuPriceTableRowLink(basePriceListName)
				.click();
			await commerceAdminProductDetailsSkusPage.sidePanelNestedPriceListPrice.fill(
				'25'
			);
			await commerceAdminProductDetailsSkusPage.sidePanelNestedSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelNestedFrame
			);

			await page.goto(`/web${site.friendlyUrlPath}/catalog`);

			await expect(productCard).toContainText('25.00');
		});

		await test.step('Add a tier price', async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				product.skus[0].sku,
				'Price'
			);
			await commerceAdminProductDetailsSkusPage
				.sidePanelSkuPriceTableRowLink(basePriceListName)
				.click();
			await commerceAdminProductDetailsSkusPage.sidePanelTierPriceAddButton.click();
			await commerceAdminProductDetailsSkusPage.submitTierPrice('10');

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelTierPriceRow(
					'10.00'
				)
			).toBeVisible();

			await page.goto(`/web${site.friendlyUrlPath}/catalog`);

			await expect(productCard).toContainText('10.00');
		});

		await test.step('Edit the tier price', async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				product.skus[0].sku,
				'Price'
			);
			await commerceAdminProductDetailsSkusPage
				.sidePanelSkuPriceTableRowLink(basePriceListName)
				.click();
			await commerceAdminProductDetailsSkusPage.clickTierPriceAction(
				'Edit',
				'10.00'
			);
			await commerceAdminProductDetailsSkusPage.submitTierPrice('20');

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelTierPriceRow(
					'20.00'
				)
			).toBeVisible();

			await page.goto(`/web${site.friendlyUrlPath}/catalog`);

			await expect(productCard).toContainText('20.00');
		});

		await test.step('Delete the tier price', async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				product.skus[0].sku,
				'Price'
			);
			await commerceAdminProductDetailsSkusPage
				.sidePanelSkuPriceTableRowLink(basePriceListName)
				.click();
			await commerceAdminProductDetailsSkusPage.clickTierPriceAction(
				'Delete',
				'20.00'
			);

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelTierPriceRow(
					'20.00'
				)
			).toHaveCount(0);

			await page.goto(`/web${site.friendlyUrlPath}/catalog`);

			await expect(productCard).toContainText('25.00');
		});

		await test.step('Delete the base price list entry', async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				product.skus[0].sku,
				'Price'
			);
			await commerceAdminProductDetailsSkusPage.clickSkuPriceAction(
				'Delete',
				basePriceListName
			);

			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceTableRow(
					basePriceListName
				)
			).toHaveCount(0);

			await page.goto(`/web${site.friendlyUrlPath}/catalog`);

			await expect(productCard).toContainText('$ 0.00');
		});
	}
);

test(
	'The Price tab of a SKU marks its price list and promotion entries as priced on application',
	{
		tag: [
			'@COMMERCE-11548',
			'@COMMERCE-12210',
			'@COMMERCE-12211',
			'@LPD-106244-Grouped-5',
		],
	},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		commerceThemeMiniumCatalogPage,
		page,
	}) => {
		const {catalog, product, site} = await apiStorefrontSetUp(apiHelpers, [
			{
				title: 'Catalog',
				widgetName:
					'com_liferay_commerce_product_content_search_web_internal_portlet_CPSearchResultsPortlet',
			},
		]);

		const basePriceListName = `${catalog.name} Base Price List`;
		const productName = product.name['en_US'];
		const sku = product.skus[0];

		const priceOnApplicationLabel =
			commerceThemeMiniumCatalogPage.productCardFragmentPriceOnApplicationLabel(
				commerceThemeMiniumCatalogPage.productCard(productName)
			);
		const productCardPrice =
			commerceThemeMiniumCatalogPage.productCardPrice(
				productName,
				'$ 25.00'
			);

		await test.step('Set the base price list entry price', async () => {
			await commerceAdminProductPage.gotoProduct(productName);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				sku.sku,
				'Price'
			);

			await commerceAdminProductDetailsSkusPage
				.sidePanelSkuPriceTableRowLink(basePriceListName)
				.click();

			await commerceAdminProductDetailsSkusPage.sidePanelNestedPriceListPrice.fill(
				'25'
			);
			await commerceAdminProductDetailsSkusPage.sidePanelNestedSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelNestedFrame
			);

			await page.goto(`/web${site.friendlyUrlPath}/catalog`);

			await expect(productCardPrice).toBeVisible();
		});

		await test.step('Enable Price on Application', async () => {
			await commerceAdminProductPage.gotoProduct(productName);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				sku.sku,
				'Price'
			);

			await commerceAdminProductDetailsSkusPage
				.sidePanelSkuPriceTableRowLink(basePriceListName)
				.click();

			await commerceAdminProductDetailsSkusPage.sidePanelNestedPriceOnApplicationTooltip.hover();

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedPriceOnApplicationTooltip
			).toHaveAttribute(
				'data-restore-title',
				'Do not set a base price for this product and instead require that buyers request a quote.'
			);

			await commerceAdminProductDetailsSkusPage.sidePanelNestedPriceOnApplicationToggle.check();
			await commerceAdminProductDetailsSkusPage.sidePanelNestedSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelNestedFrame
			);

			await page.goto(`/web${site.friendlyUrlPath}/catalog`);

			await expect(priceOnApplicationLabel).toBeVisible();
		});

		await test.step('Disable Price on Application', async () => {
			await commerceAdminProductPage.gotoProduct(productName);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				sku.sku,
				'Price'
			);

			await commerceAdminProductDetailsSkusPage
				.sidePanelSkuPriceTableRowLink(basePriceListName)
				.click();

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedPriceOnApplicationToggle
			).toBeChecked();

			await commerceAdminProductDetailsSkusPage.sidePanelNestedPriceOnApplicationToggle.uncheck();
			await commerceAdminProductDetailsSkusPage.sidePanelNestedSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelNestedFrame
			);

			await page.goto(`/web${site.friendlyUrlPath}/catalog`);

			await expect(productCardPrice).toBeVisible();
			await expect(priceOnApplicationLabel).toBeHidden();
		});

		const priceEntries: {name: string; priceEntryId: number}[] = [];

		for (const [type, price] of [
			['price-list', 20],
			['promotion', 0],
		] as const) {
			const priceList =
				await apiHelpers.headlessCommerceAdminPricing.postPriceList({
					catalogId: catalog.id,
					currencyCode: 'USD',
					name: getRandomString(),
					type,
				});

			const priceEntry =
				await apiHelpers.headlessCommerceAdminPricing.postPriceEntry({
					price,
					priceListId: priceList.id,
					skuId: sku.id,
				});

			priceEntries.push({
				name: priceList.name,
				priceEntryId: priceEntry.priceEntryId,
			});
		}

		const basePriceListNames = [
			basePriceListName,
			`${catalog.name} Base Promotion`,
		];
		const priceListNames = priceEntries.map(({name}) => name);

		const expectPriceOnApplication = async (
			names: string[],
			priceOnApplication: 'No' | 'Yes'
		) => {
			for (const name of names) {
				await expect(async () => {
					expect(
						await getTableRowCells(
							commerceAdminProductDetailsSkusPage.sidePanelSkuPriceTable,
							name
						)
					).toMatchObject({
						'PRICE ON APPLICATION': priceOnApplication,
					});
				}).toPass({timeout: 30000});
			}
		};

		await test.step('Products admin lists no POA entries', async () => {
			await commerceAdminProductPage.gotoProduct(productName);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				sku.sku,
				'Price'
			);

			await expectPriceOnApplication(
				[...basePriceListNames, ...priceListNames],
				'No'
			);
		});

		await test.step('SKUs admin lists no POA entries', async () => {
			await commerceAdminProductPage.goto();

			await commerceAdminProductPage.productSkusLink.click();

			await commerceAdminProductPage.searchByValue(sku.sku);

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				sku.sku,
				'Price'
			);

			await expectPriceOnApplication(
				[...basePriceListNames, ...priceListNames],
				'No'
			);
		});

		for (const {priceEntryId} of priceEntries) {
			await apiHelpers.headlessCommerceAdminPricing.patchPriceEntry(
				priceEntryId,
				{priceOnApplication: true}
			);
		}

		await test.step('Products admin lists the POA entries', async () => {
			await commerceAdminProductPage.gotoProduct(productName);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				sku.sku,
				'Price'
			);

			await expectPriceOnApplication(basePriceListNames, 'No');
			await expectPriceOnApplication(priceListNames, 'Yes');
		});

		await test.step('SKUs admin lists the POA entries', async () => {
			await commerceAdminProductPage.goto();

			await commerceAdminProductPage.productSkusLink.click();

			await commerceAdminProductPage.searchByValue(sku.sku);

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				sku.sku,
				'Price'
			);

			await expectPriceOnApplication(basePriceListNames, 'No');
			await expectPriceOnApplication(priceListNames, 'Yes');
		});
	}
);

test(
	'Back button works as expected',
	{tag: '@LPD-43791'},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductPage,
		page,
		site,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.backLink.click();

		await expect(
			commerceAdminProductPage.productsTableRowLink(product.name['en_US'])
		).toBeVisible();

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await page.goto(
			`${page.url()}&_com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet_backURL=${site.friendlyUrlPath}`
		);

		await commerceAdminProductDetailsPage.backLink.click();

		await expect(
			commerceAdminProductPage.productsTableRowLink(product.name['en_US'])
		).toHaveCount(0);
	}
);

test(
	'Add price entries for price lists, currencies and units of measure from the SKU price modal',
	{
		tag: [
			'@COMMERCE-11084',
			'@COMMERCE-12218',
			'@COMMERCE-12289',
			'@COMMERCE-12290',
			'@LPD-52938',
			'@LPD-106244-Grouped-4',
		],
	},
	async ({
		apiHelpers,
		commerceAdminPriceListDetailsPage,
		commerceAdminPriceListsPage,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		page,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				skus: [
					{
						cost: 0,
						price: 50,
						published: true,
						purchasable: true,
						sku: getRandomString(),
					},
				],
			});

		const priceListEUR =
			await apiHelpers.headlessCommerceAdminPricing.postPriceList({
				catalogId: catalog.id,
				currencyCode: 'EUR',
				name: 'EUR-pl ' + getRandomString(),
				type: 'price-list',
			});
		const priceListUSD =
			await apiHelpers.headlessCommerceAdminPricing.postPriceList({
				catalogId: catalog.id,
				currencyCode: 'USD',
				name: 'USD-pl ' + getRandomString(),
				type: 'price-list',
			});

		const basePriceListName = `${catalog.name} Base Price List`;
		const sku = product.skus[0];

		await test.step('Add price entries for two price lists', async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				sku.sku,
				'Price'
			);
			await commerceAdminProductDetailsSkusPage.skuPriceAddButton.click();

			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceListSelect.locator(
					'option:checked'
				)
			).toHaveText(basePriceListName);
			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitOfMeasureSelect
			).toHaveCount(0);
			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput
			).toHaveValue(/^50(\.0+)?$/);

			await commerceAdminProductDetailsSkusPage.skuPriceListSelect.selectOption(
				priceListEUR.name
			);

			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalCurrency(
					'EUR'
				)
			).toBeVisible();

			await commerceAdminProductDetailsSkusPage.skuPriceListSelect.selectOption(
				priceListUSD.name
			);

			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalCurrency(
					'USD'
				)
			).toBeVisible();

			await commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput.fill(
				'10'
			);
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalAddEntryButton.click();
			await commerceAdminProductDetailsSkusPage.skuPriceListSelect
				.nth(1)
				.selectOption(priceListEUR.name);
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput
				.nth(1)
				.fill('20');
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalAddButton.click();

			await commerceAdminProductDetailsSkusPage.goToSkuTab('Price');

			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceTableRow(
					priceListUSD.name
				)
			).toContainText('$ 10.00');
			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceTableRow(
					priceListEUR.name
				)
			).toContainText('€ 20.00');
		});

		await test.step('Change the currency of a price list', async () => {
			await commerceAdminProductDetailsSkusPage
				.sidePanelSkuPriceTableRowLink(priceListUSD.name)
				.click();

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedPriceListCurrency(
					'USD'
				)
			).toBeVisible();

			await commerceAdminPriceListsPage.goto();

			await commerceAdminPriceListsPage
				.priceListLink(priceListUSD.name)
				.click();
			await commerceAdminPriceListDetailsPage.currencySelect.selectOption(
				{
					label: 'EUR',
				}
			);
			await commerceAdminPriceListDetailsPage.publishButton.click();

			await waitForAlert(page);

			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				sku.sku,
				'Price'
			);
			await commerceAdminProductDetailsSkusPage
				.sidePanelSkuPriceTableRowLink(priceListUSD.name)
				.click();

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelNestedPriceListCurrency(
					'EUR'
				)
			).toBeVisible();
		});

		await test.step('Add price entries for two units of measure', async () => {
			for (const index of [1, 2]) {
				await apiHelpers.headlessCommerceAdminCatalog.postSkuUnitOfMeasure(
					sku.id,
					{
						basePrice: index,
						key: `uom${index}`,
						name: {en_US: `uom${index}`},
						priority: index,
					}
				);
			}

			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				sku.sku,
				'Price'
			);

			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceTableRow(
					catalog.name
				)
			).toHaveCount(4);

			await commerceAdminProductDetailsSkusPage.skuPriceAddButton.click();

			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceListSelect.locator(
					'option:checked'
				)
			).toHaveText(basePriceListName);
			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitOfMeasureSelect.locator(
					'option:checked'
				)
			).toHaveText('uom1');
			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput
			).toHaveValue(/^1(\.0+)?$/);

			await commerceAdminProductDetailsSkusPage.skuPriceListSelect.selectOption(
				priceListUSD.name
			);
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitOfMeasureSelect.selectOption(
				'uom1'
			);
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput.fill(
				'30'
			);
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalAddEntryButton.click();
			await commerceAdminProductDetailsSkusPage.skuPriceListSelect
				.nth(1)
				.selectOption(priceListEUR.name);
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitOfMeasureSelect
				.nth(1)
				.selectOption('uom2');
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalUnitPriceInput
				.nth(1)
				.fill('90');
			await commerceAdminProductDetailsSkusPage.skuPriceAddModalAddButton.click();

			await commerceAdminProductDetailsSkusPage.goToSkuTab('Price');

			await expect(
				commerceAdminProductDetailsSkusPage
					.skuPriceTableRow(priceListUSD.name)
					.filter({hasText: 'uom1'})
			).toContainText('€ 30.00');
			await expect(
				commerceAdminProductDetailsSkusPage
					.skuPriceTableRow(priceListEUR.name)
					.filter({hasText: 'uom2'})
			).toContainText('€ 90.00');
		});
	}
);

test(
	'Product name should be created for both user language and instance language',
	{tag: '@LPD-64086'},
	async ({apiHelpers, commerceAdminProductPage, page}) => {
		const user =
			await apiHelpers.headlessAdminUser.getUserAccountByEmailAddress(
				'test@liferay.com'
			);

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		userData[user.alternateName] = {
			name: user.givenName,
			password: 'test',
			surname: user.familyName,
		};

		const role =
			await apiHelpers.headlessAdminUser.getRoleByName('Administrator');

		await apiHelpers.headlessAdminUser.assignUserToRole(
			role.externalReferenceCode,
			user.id
		);

		await commerceAdminProductPage.goto();

		await apiHelpers.headlessAdminUser.patchUserAccount(user, {
			languageId: 'es_ES',
		});

		await page.reload();

		const translateLink = page.getByRole('link', {
			name: 'Mostrar la página en español (España).',
		});

		const inEnglish = await translateLink.isVisible();

		if (inEnglish) {
			await translateLink.click();
		}

		await commerceAdminProductPage.addButton.click();
		await commerceAdminProductPage.menuItemProductType('Simple').click();

		await expect(
			commerceAdminProductPage.modalFrameLocator.getByText(
				'Crear nuevo producto'
			)
		).toBeVisible();

		const productName = getRandomString();

		await commerceAdminProductPage.modalFrameLocator
			.getByLabel('Nombre Requerido')
			.fill(productName);
		await commerceAdminProductPage.modalFrameLocator
			.getByPlaceholder('Escriba aquí')
			.fill(catalog.name);
		await commerceAdminProductPage.modalMenuItem(catalog.name).click();
		await commerceAdminProductPage.modalFrameLocator
			.getByRole('button', {
				exact: true,
				name: 'Enviar',
			})
			.click();

		await expect(page.getByText(productName)).toBeVisible();

		const product = (
			await apiHelpers.headlessCommerceAdminCatalog.getProducts(
				new URLSearchParams({
					filter: `name eq '${productName}'`,
				})
			)
		).items[0];

		const productNameUS = product.name['en_US'];
		const productNameES = product.name['es_ES'];

		expect(productNameUS).toBe(productName);
		expect(productNameES).toBe(productName);

		await apiHelpers.headlessAdminUser.patchUserAccount(user, {
			languageId: 'en_US',
		});
	}
);

test(
	'Creating product with pending status in headless triggers workflow notification',
	{tag: ['@LPD-73496']},
	async ({
		apiHelpers,
		commerceAdminProductPage,
		page,
		userPersonalBarPage,
	}) => {
		await userPersonalBarPage.goToProcessBuilderConfigurationTab();
		await userPersonalBarPage.enableSingleApproverWorkflowProduct();

		try {
			const catalog =
				await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

			const product =
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId: catalog.id,
					productStatus: 1,
				});

			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await expect(page.getByText('Assigned to:')).toBeVisible();
			await expect(userPersonalBarPage.notificationBadge).toBeVisible();
		}
		finally {
			await userPersonalBarPage.disableSingleApproverWorkflowProduct();
		}
	}
);

test(
	'Add SKU with duplicate values and confirm warning message shows',
	{tag: '@LPD-90070'},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		page,
	}) => {
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

		await commerceAdminProductPage.gotoProduct(product2.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage.skuAddButton.click();

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuInput.fill(
			product1.skus[0].sku.toUpperCase()
		);

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuPurchasableToggle.check();

		page.once('dialog', async (dialog) => {
			expect(dialog.message()).toContain('The SKU is already in use');

			await dialog.accept();
		});

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuPublishButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.skuAddModalSuccessMessage
		).toBeVisible();
	}
);

test(
	'Product fields default to the catalog default language',
	{tag: ['@COMMERCE-9532', '@LPD-103842']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductPage,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
				defaultLanguageId: 'es_ES',
				name: getRandomString(),
			});

		const productName = getRandomString();

		await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			name: {en_US: productName},
		});

		await commerceAdminProductPage.gotoProduct(productName);

		await expect(
			commerceAdminProductDetailsPage.nameInputLocaleSelector
		).toHaveText('es-ES');
	}
);

test(
	'Create, publish, edit and delete a simple product',
	{
		tag: [
			'@COMMERCE-5808',
			'@COMMERCE-6303',
			'@COMMERCE-9179',
			'@COMMERCE-9180',
			'@LPD-106244-Grouped-2',
		],
	},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductPage,
		page,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const expirationDate = new Date();

		expirationDate.setFullYear(expirationDate.getFullYear() + 1);

		const formattedExpirationDate = [
			String(expirationDate.getMonth() + 1).padStart(2, '0'),
			String(expirationDate.getDate()).padStart(2, '0'),
			expirationDate.getFullYear(),
		].join('/');
		const productName = getRandomString();
		const shortDescription = getRandomString();
		const translatedProductName = getRandomString();
		const updatedProductName = getRandomString();

		try {
			await test.step('Create a simple product', async () => {
				await commerceAdminProductPage.goto();

				await commerceAdminProductPage.addButton.click();
				await commerceAdminProductPage
					.menuItemProductType('Simple')
					.click();
				await commerceAdminProductPage.modalFieldName.fill(productName);
				await commerceAdminProductPage.modalPlaceHolder.fill(
					catalog.name
				);
				await commerceAdminProductPage
					.modalMenuItem(catalog.name)
					.click();
				await commerceAdminProductPage.modalSubmitButton.click();

				await expect(
					commerceAdminProductDetailsPage.nameInput
				).toHaveValue(productName);
				await expect(
					commerceAdminProductDetailsPage.publishLink
				).toBeVisible();
				await expect(
					commerceAdminProductDetailsPage.saveAsDraftLink
				).toBeVisible();
				await expect(
					commerceAdminProductDetailsPage.selectedCatalogOption
				).toHaveText(catalog.name);
				await expect(
					commerceAdminProductDetailsPage.workflowStatus('Draft')
				).toBeVisible();
			});

			await test.step('Publish the product with a short description and an expiration date', async () => {
				await fillLocalizedInput(
					commerceAdminProductDetailsPage.shortDescriptionInput,
					shortDescription
				);
				await commerceAdminProductDetailsPage.neverExpireToggle.uncheck();
				await commerceAdminProductDetailsPage.expirationDateInput.fill(
					formattedExpirationDate
				);
				await commerceAdminProductDetailsPage.publish();

				await expect(
					commerceAdminProductDetailsPage.workflowStatus('Approved')
				).toBeVisible();
			});

			await test.step('Edit the product name and its translation', async () => {
				await commerceAdminProductDetailsPage.nameInput.fill(
					updatedProductName
				);
				await commerceAdminProductDetailsPage.publish();

				await expect(
					commerceAdminProductDetailsPage.nameInput
				).toHaveValue(updatedProductName);

				await clickAndExpectToBeVisible({
					autoClick: true,
					target: commerceAdminProductDetailsPage.nameInputLocaleMenuItem(
						'es-ES'
					),
					trigger:
						commerceAdminProductDetailsPage.nameInputLocaleSelector,
				});
				await commerceAdminProductDetailsPage.nameInput.fill(
					translatedProductName
				);
				await commerceAdminProductDetailsPage.publish();
			});

			await test.step('Reopen the product and verify its saved values', async () => {
				await commerceAdminProductPage.gotoProduct(updatedProductName);

				await expect(
					commerceAdminProductDetailsPage.expirationDateInput
				).toHaveValue(formattedExpirationDate);
				await expect(
					commerceAdminProductDetailsPage.nameInput
				).toHaveValue(updatedProductName);
				await expect(
					commerceAdminProductDetailsPage.neverExpireToggle
				).not.toBeChecked();
				await expect(
					commerceAdminProductDetailsPage.shortDescriptionInput
				).toHaveValue(shortDescription);
				await expect(
					commerceAdminProductDetailsPage.workflowStatus('Approved')
				).toBeVisible();

				const product = (
					await apiHelpers.headlessCommerceAdminCatalog.getProducts(
						new URLSearchParams({
							filter: `name eq '${updatedProductName}'`,
						})
					)
				).items[0];

				expect(product.name).toMatchObject({
					en_US: updatedProductName,
					es_ES: translatedProductName,
				});
			});

			await test.step('Delete the product from the products list', async () => {
				await commerceAdminProductDetailsPage.backLink.click();

				await commerceAdminProductPage.search(updatedProductName);
				await commerceAdminProductPage
					.productRowActionsButton(updatedProductName)
					.click();
				await commerceAdminProductPage.deleteMenuItem.click();

				await waitForAlert(page);

				await expect(
					commerceAdminProductPage.productsTableRow(
						updatedProductName
					)
				).toHaveCount(0);
			});
		}
		finally {
			for (const name of [productName, updatedProductName]) {
				const product = (
					await apiHelpers.headlessCommerceAdminCatalog.getProducts(
						new URLSearchParams({
							filter: `name eq '${name}'`,
						})
					)
				).items[0];

				if (product) {
					apiHelpers.data.push({
						id: product.productId,
						type: 'product',
					});
				}
			}
		}
	}
);

for (const {productType, tags} of [
	{productType: 'Grouped', tags: ['@COMMERCE-6302', '@LPD-106244-Grouped-2']},
	{productType: 'Virtual', tags: ['@COMMERCE-6301', '@LPD-106244-Grouped-7']},
]) {
	test(
		`Publish a ${productType} product`,
		{tag: tags},
		async ({
			apiHelpers,
			commerceAdminProductDetailsPage,
			commerceAdminProductPage,
		}) => {
			const catalog =
				await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

			const productName = getRandomString();

			try {
				await commerceAdminProductPage.goto();

				await commerceAdminProductPage.addButton.click();
				await commerceAdminProductPage
					.menuItemProductType(productType)
					.click();
				await commerceAdminProductPage.modalFieldName.fill(productName);
				await commerceAdminProductPage.modalPlaceHolder.fill(
					catalog.name
				);
				await commerceAdminProductPage
					.modalMenuItem(catalog.name)
					.click();
				await commerceAdminProductPage.modalSubmitButton.click();

				await expect(
					commerceAdminProductDetailsPage.nameInput
				).toHaveValue(productName);
				await expect(
					commerceAdminProductDetailsPage.selectedCatalogOption
				).toHaveText(catalog.name);
				await expect(
					commerceAdminProductDetailsPage.workflowStatus('Draft')
				).toBeVisible();

				await commerceAdminProductDetailsPage.publish();

				await expect(
					commerceAdminProductDetailsPage.workflowStatus('Approved')
				).toBeVisible();

				await commerceAdminProductDetailsPage.backLink.click();

				await commerceAdminProductPage.search(productName);

				await expect(
					commerceAdminProductPage.productsTableRow(productName)
				).toContainText(productType);
			}
			finally {
				const product = (
					await apiHelpers.headlessCommerceAdminCatalog.getProducts(
						new URLSearchParams({
							filter: `name eq '${productName}'`,
						})
					)
				).items[0];

				if (product) {
					apiHelpers.data.push({
						id: product.productId,
						type: 'product',
					});
				}
			}
		}
	);
}

test(
	'An approved product can be duplicated and converted to draft',
	{tag: ['@COMMERCE-9183', '@COMMERCE-9251', '@LPD-106244-Grouped-2']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductPage,
		page,
	}) => {
		await checkProductStatusAfterDuplicationAndConversionToDraft({
			apiHelpers,
			commerceAdminProductDetailsPage,
			commerceAdminProductPage,
			page,
			productStatus: 0,
			status: 'Approved',
		});
	}
);

test(
	'A pending product can be duplicated and converted to draft',
	{tag: ['@COMMERCE-9251', '@LPD-106244-Grouped-2']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductPage,
		page,
		userPersonalBarPage,
	}) => {
		await userPersonalBarPage.goToProcessBuilderConfigurationTab();
		await userPersonalBarPage.enableSingleApproverWorkflowProduct();

		try {
			await checkProductStatusAfterDuplicationAndConversionToDraft({
				apiHelpers,
				commerceAdminProductDetailsPage,
				commerceAdminProductPage,
				page,
				productStatus: 1,
				status: 'Pending',
			});
		}
		finally {
			await userPersonalBarPage.disableSingleApproverWorkflowProduct();
		}
	}
);

test(
	'A draft product can be duplicated and saved as draft',
	{tag: ['@COMMERCE-9181', '@COMMERCE-9251', '@LPD-106244-Grouped-2']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductPage,
		page,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				productStatus: 2,
			});

		const productName = product.name['en_US'];

		try {
			await commerceAdminProductPage.gotoProduct(productName);

			await expect(
				commerceAdminProductDetailsPage.saveAsDraftLink
			).toBeVisible();
			await expect(
				commerceAdminProductDetailsPage.workflowStatus('Draft')
			).toBeVisible();

			await commerceAdminProductDetailsPage.duplicate(catalog.name);

			await expect(commerceAdminProductDetailsPage.nameInput).toHaveValue(
				`Copy of ${productName}`
			);
			await expect(
				commerceAdminProductDetailsPage.workflowStatus('Draft')
			).toBeVisible();

			await commerceAdminProductPage.gotoProduct(productName);

			const updatedProductName = getRandomString();

			await commerceAdminProductDetailsPage.nameInput.fill(
				updatedProductName
			);
			await commerceAdminProductDetailsPage.saveAsDraftLink.click();

			await waitForAlert(page);

			await commerceAdminProductPage.gotoProduct(updatedProductName);

			await expect(commerceAdminProductDetailsPage.nameInput).toHaveValue(
				updatedProductName
			);
			await expect(
				commerceAdminProductDetailsPage.workflowStatus('Draft')
			).toBeVisible();

			await commerceAdminProductDetailsPage.headerActionsButton.click();

			await expect(
				commerceAdminProductDetailsPage.headerActionsMenuItem(
					'Duplicate'
				)
			).toBeVisible();
			await expect(
				commerceAdminProductDetailsPage.headerActionsMenuItem(
					'Convert to Draft'
				)
			).toHaveCount(0);
		}
		finally {
			const copyProduct = (
				await apiHelpers.headlessCommerceAdminCatalog.getProducts(
					new URLSearchParams({
						filter: `name eq 'Copy of ${productName}'`,
					})
				)
			).items[0];

			if (copyProduct) {
				apiHelpers.data.push({
					id: copyProduct.productId,
					type: 'product',
				});
			}
		}
	}
);

test(
	'Search, sort and paginate the products and SKUs admin lists',
	{
		tag: [
			'@COMMERCE-5801',
			'@COMMERCE-5803',
			'@COMMERCE-5805',
			'@LPD-106244-Grouped-2',
		],
	},
	async ({apiHelpers, commerceAdminProductPage}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const prefix = 'p' + getRandomInt();

		const productNames = Array.from(
			{length: 21},
			(_, index) =>
				`${prefix} ${String.fromCharCode(65 + ((index * 8) % 21))}`
		);

		for (const [index, productName] of productNames.entries()) {
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: productName},
				skus: [
					{
						cost: 0,
						price: 0,
						published: true,
						purchasable: true,
						sku: `${prefix}sku${index}`,
					},
				],
			});
		}

		await test.step('Search and sort the products', async () => {
			await commerceAdminProductPage.goto();

			await commerceAdminProductPage.search(prefix);

			const sortedProductNames = [...productNames].sort();

			for (const {column, expectedProductNames} of [
				{column: 'Name', expectedProductNames: sortedProductNames},
				{
					column: 'Name',
					expectedProductNames: [...sortedProductNames].reverse(),
				},
				{column: 'Modified Date', expectedProductNames: productNames},
				{
					column: 'Modified Date',
					expectedProductNames: [...productNames].reverse(),
				},
			]) {
				await commerceAdminProductPage.columnHeader(column).click();

				for (const [index, productName] of expectedProductNames
					.slice(0, 3)
					.entries()) {
					await expect(
						commerceAdminProductPage.tableBodyRow(index)
					).toContainText(productName);
				}
			}
		});

		await test.step('Paginate the products', async () => {
			await expect(
				commerceAdminProductPage.paginationResults(1, 20, 21)
			).toBeVisible();
			await expect(
				commerceAdminProductPage.paginationItemsButton(20)
			).toBeVisible();

			await commerceAdminProductPage.paginationNextButton.click();

			await expect(
				commerceAdminProductPage.paginationResults(21, 21, 21)
			).toBeVisible();
		});

		await test.step('Search and paginate the SKUs', async () => {
			await commerceAdminProductPage.productSkusLink.click();

			await commerceAdminProductPage.search(prefix);

			await expect(
				commerceAdminProductPage.paginationResults(1, 20, 21)
			).toBeVisible();

			await commerceAdminProductPage.paginationNextButton.click();

			await expect(
				commerceAdminProductPage.paginationResults(21, 21, 21)
			).toBeVisible();

			await commerceAdminProductPage.search(`${prefix}sku7`);

			await expect(
				commerceAdminProductPage.productSkuTableRowLink(`${prefix}sku7`)
			).toBeVisible();
			await expect(
				commerceAdminProductPage.paginationResults(1, 1, 1)
			).toBeVisible();
		});
	}
);

test(
	'A SKU external reference code cannot be used by another SKU',
	{tag: ['@COMMERCE-9891', '@LPD-106244-Grouped-4']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const externalReferenceCode = getRandomString();

		const publishSkuExternalReferenceCode = async () => {
			const product =
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId: catalog.id,
				});

			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(product.skus[0].sku)
				.click();
			await commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuExternalReferenceCodeInput.fill(
				externalReferenceCode
			);
			await commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuPublishButton.click();

			return product;
		};

		await publishSkuExternalReferenceCode();

		await waitForAlert(commerceAdminProductDetailsSkusPage.sidePanelFrame);

		const product = await publishSkuExternalReferenceCode();

		await waitForAlert(
			commerceAdminProductDetailsSkusPage.sidePanelFrame,
			'Error:Please enter a unique external reference code.',
			{autoClose: false, type: 'danger'}
		);

		const {skus} = await apiHelpers.headlessCommerceAdminCatalog.getProduct(
			product.productId
		);

		expect(skus[0].externalReferenceCode).not.toBe(externalReferenceCode);
	}
);

test(
	'Changing the channel currency does not change the SKU price list currency',
	{tag: ['@COMMERCE-12218', '@LPD-106244-Grouped-4']},
	async ({
		apiHelpers,
		commerceAdminChannelDetailsPage,
		commerceAdminChannelsPage,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		commerceThemeClassicCatalogPage,
		page,
	}) => {
		const {catalog, channel, product, site} = await apiStorefrontSetUp(
			apiHelpers,
			[
				{
					title: 'Catalog',
					widgetName:
						'com_liferay_commerce_product_content_search_web_internal_portlet_CPSearchResultsPortlet',
				},
			]
		);

		const basePriceListName = `${catalog.name} Base Price List`;

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage.openSkuTab(
			product.skus[0].sku,
			'Price'
		);
		await commerceAdminProductDetailsSkusPage
			.sidePanelSkuPriceTableRowLink(basePriceListName)
			.click();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelNestedPriceListCurrency(
				'USD'
			)
		).toBeVisible();

		await commerceAdminChannelsPage.goto();

		await (
			await commerceAdminChannelsPage.channelsTableRowLink(channel.name)
		).click();

		await expect(
			commerceAdminChannelDetailsPage.channelCurrencySelect
		).toHaveValue('USD');

		await commerceAdminChannelDetailsPage.changeChannelDefaultCurrency(
			'EUR'
		);

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage.openSkuTab(
			product.skus[0].sku,
			'Price'
		);
		await commerceAdminProductDetailsSkusPage
			.sidePanelSkuPriceTableRowLink(basePriceListName)
			.click();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelNestedPriceListCurrency(
				'USD'
			)
		).toBeVisible();

		await page.goto(`/web${site.friendlyUrlPath}/catalog`);

		await expect(
			commerceThemeClassicCatalogPage.productCard(product.name['en_US'])
		).toContainText('€ 0.00');
	}
);

for (const subscriptionName of [
	'Delivery Subscription',
	'Payment Subscription',
] as const) {
	test(
		`The ${subscriptionName} length shows the subscription type in singular or plural`,
		{tag: ['@COMMERCE-9744', '@COMMERCE-9798', '@LPD-106244-Grouped-4']},
		async ({
			apiHelpers,
			commerceAdminProductDetailsPage,
			commerceAdminProductPage,
			page,
		}) => {
			const catalog =
				await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

			const product =
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId: catalog.id,
				});

			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.productSubscriptionLink.click();

			await commerceAdminProductDetailsPage
				.subscriptionEnabledLabel(subscriptionName)
				.click();

			const subscriptionLengthInput =
				commerceAdminProductDetailsPage.subscriptionLengthInput(
					subscriptionName
				);
			const subscriptionLengthSuffix =
				commerceAdminProductDetailsPage.subscriptionLengthSuffix(
					subscriptionName
				);
			const subscriptionTypeSelect =
				commerceAdminProductDetailsPage.subscriptionTypeSelect(
					subscriptionName
				);

			await expect(
				subscriptionTypeSelect.locator('option:checked')
			).toHaveText('Day');
			await expect(subscriptionLengthInput).toHaveValue('1');
			await expect(subscriptionLengthSuffix).toHaveText('Day');

			for (const subscriptionType of ['Week', 'Month', 'Year']) {
				await subscriptionTypeSelect.selectOption({
					label: subscriptionType,
				});

				await commerceAdminProductDetailsPage.publishLink.click();

				await waitForAlert(page);

				await expect(subscriptionLengthSuffix).toHaveText(
					subscriptionType
				);
			}

			await subscriptionLengthInput.fill('2');

			for (const {subscriptionType, subscriptionTypeSuffix} of [
				{subscriptionType: 'Day', subscriptionTypeSuffix: 'Days'},
				{subscriptionType: 'Week', subscriptionTypeSuffix: 'Weeks'},
				{subscriptionType: 'Month', subscriptionTypeSuffix: 'Months'},
				{subscriptionType: 'Year', subscriptionTypeSuffix: 'Years'},
			]) {
				await subscriptionTypeSelect.selectOption({
					label: subscriptionType,
				});

				await commerceAdminProductDetailsPage.publishLink.click();

				await waitForAlert(page);

				await expect(subscriptionLengthSuffix).toHaveText(
					subscriptionTypeSuffix
				);
			}
		}
	);
}

test(
	'Product and SKU subscription configurations are saved',
	{
		tag: [
			'@COMMERCE-6024',
			'@COMMERCE-6085',
			'@COMMERCE-9806',
			'@COMMERCE-11150',
			'@LPD-106244-Grouped-4',
		],
	},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		page,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await test.step('Configure the product delivery subscription', async () => {
			await commerceAdminProductDetailsPage.productSubscriptionLink.click();

			await commerceAdminProductDetailsPage
				.subscriptionEnabledLabel('Delivery Subscription')
				.click();
			await commerceAdminProductDetailsPage
				.subscriptionTypeSelect('Delivery Subscription')
				.selectOption({label: 'Year'});
			await commerceAdminProductDetailsPage
				.subscriptionYearlyModeSelect('Delivery Subscription')
				.selectOption({label: 'Exact Day of Year'});
			await commerceAdminProductDetailsPage
				.subscriptionYearlyMonthSelect('Delivery Subscription')
				.selectOption({label: 'March'});
			await commerceAdminProductDetailsPage
				.subscriptionYearlyMonthDayInput('Delivery Subscription')
				.fill('5');
			await commerceAdminProductDetailsPage
				.subscriptionLengthInput('Delivery Subscription')
				.fill('5');
			await commerceAdminProductDetailsPage.publishLink.click();

			await waitForAlert(page);

			await commerceAdminProductDetailsPage.productDetailsLink.click();
			await commerceAdminProductDetailsPage.productSubscriptionLink.click();

			await expect(
				commerceAdminProductDetailsPage.subscriptionEnabledInput(
					'Delivery Subscription'
				)
			).toBeChecked();
			await expect(
				commerceAdminProductDetailsPage
					.subscriptionTypeSelect('Delivery Subscription')
					.locator('option:checked')
			).toHaveText('Year');
			await expect(
				commerceAdminProductDetailsPage
					.subscriptionYearlyModeSelect('Delivery Subscription')
					.locator('option:checked')
			).toHaveText('Exact Day of Year');
			await expect(
				commerceAdminProductDetailsPage
					.subscriptionYearlyMonthSelect('Delivery Subscription')
					.locator('option:checked')
			).toHaveText('March');
			await expect(
				commerceAdminProductDetailsPage.subscriptionYearlyMonthDayInput(
					'Delivery Subscription'
				)
			).toHaveValue('5');
			await expect(
				commerceAdminProductDetailsPage.subscriptionLengthInput(
					'Delivery Subscription'
				)
			).toHaveValue('5');
		});

		await test.step('Configure the product payment subscription', async () => {
			await commerceAdminProductDetailsPage
				.subscriptionEnabledLabel('Payment Subscription')
				.click();
			await commerceAdminProductDetailsPage.publishLink.click();

			await waitForAlert(page);

			await expect(
				commerceAdminProductDetailsPage
					.subscriptionTypeSelect('Payment Subscription')
					.locator('option:checked')
			).toHaveText('Day');

			await commerceAdminProductDetailsPage
				.subscriptionTypeSelect('Payment Subscription')
				.selectOption({label: 'Year'});
			await commerceAdminProductDetailsPage.publishLink.click();

			await waitForAlert(page);

			await commerceAdminProductDetailsPage.productDetailsLink.click();
			await commerceAdminProductDetailsPage.productSubscriptionLink.click();

			await expect(
				commerceAdminProductDetailsPage.subscriptionEnabledInput(
					'Delivery Subscription'
				)
			).toBeChecked();
			await expect(
				commerceAdminProductDetailsPage.subscriptionEnabledInput(
					'Payment Subscription'
				)
			).toBeChecked();
			await expect(
				commerceAdminProductDetailsPage
					.subscriptionTypeSelect('Payment Subscription')
					.locator('option:checked')
			).toHaveText('Year');
			await expect(
				commerceAdminProductDetailsPage
					.subscriptionYearlyModeSelect('Payment Subscription')
					.locator('option:checked')
			).toHaveText('Order Date');
		});

		await test.step('Override the SKU subscription settings', async () => {
			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage.openSkuTab(
				product.skus[0].sku,
				'Subscriptions'
			);
			await commerceAdminProductDetailsSkusPage.sidePanelSubscriptionOverrideToggle.check();

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelSubscriptionSection(
					'Delivery Subscription'
				)
			).toBeVisible();
			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelSubscriptionSection(
					'Payment Subscription'
				)
			).toBeVisible();

			await commerceAdminProductDetailsSkusPage
				.sidePanelSubscriptionEnabledLabel('Delivery Subscription')
				.click();
			await commerceAdminProductDetailsSkusPage.sidePanelSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelFrame
			);

			await expect(
				commerceAdminProductDetailsSkusPage
					.sidePanelSubscriptionTypeSelect('Delivery Subscription')
					.locator('option:checked')
			).toHaveText('Day');

			await commerceAdminProductDetailsSkusPage
				.sidePanelSubscriptionTypeSelect('Delivery Subscription')
				.selectOption({label: 'Year'});

			await expect(
				commerceAdminProductDetailsSkusPage
					.sidePanelSubscriptionYearlyModeSelect(
						'Delivery Subscription'
					)
					.locator('option:checked')
			).toHaveText('Order Date');

			await commerceAdminProductDetailsSkusPage.sidePanelSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelFrame
			);

			await expect(
				commerceAdminProductDetailsSkusPage
					.sidePanelSubscriptionTypeSelect('Delivery Subscription')
					.locator('option:checked')
			).toHaveText('Year');
			await expect(
				commerceAdminProductDetailsSkusPage
					.sidePanelSubscriptionYearlyModeSelect(
						'Delivery Subscription'
					)
					.locator('option:checked')
			).toHaveText('Order Date');
			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelSubscriptionLengthInput(
					'Delivery Subscription'
				)
			).toHaveValue('1');
		});

		await test.step('Stop overriding the SKU subscription settings', async () => {
			await commerceAdminProductDetailsSkusPage.sidePanelSubscriptionOverrideToggle.uncheck();
			await commerceAdminProductDetailsSkusPage.sidePanelSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsSkusPage.sidePanelFrame
			);

			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelSubscriptionSection(
					'Delivery Subscription'
				)
			).toBeHidden();
			await expect(
				commerceAdminProductDetailsSkusPage.sidePanelSubscriptionSection(
					'Payment Subscription'
				)
			).toBeHidden();
		});
	}
);
