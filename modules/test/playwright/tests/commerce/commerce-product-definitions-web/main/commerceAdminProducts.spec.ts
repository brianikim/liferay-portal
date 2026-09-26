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
import {CommerceAdminProductDetailsPage} from '../../../../pages/commerce/commerce-product-definitions-web/commerceAdminProductDetailsPage';
import {CommerceAdminProductPage} from '../../../../pages/commerce/commerce-product-definitions-web/commerceAdminProductPage';
import {clickAndExpectToBeVisible} from '../../../../utils/clickAndExpectToBeVisible';
import {getRandomInt} from '../../../../utils/getRandomInt';
import getRandomString from '../../../../utils/getRandomString';
import {userData} from '../../../../utils/performLogin';
import {waitForAlert} from '../../../../utils/waitForAlert';
import {apiStorefrontSetUp} from '../../utils/commerce';

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

	const workflowStatus = (label: string) =>
		page.locator('.workflow-status').getByText(label, {exact: true});

	try {
		await commerceAdminProductPage.gotoProduct(productName);

		await expect(workflowStatus(status)).toBeVisible();

		if (status === 'Draft') {
			await expect(
				commerceAdminProductDetailsPage.saveAsDraftLink
			).toBeVisible();
		}
		else {
			await expect(
				commerceAdminProductDetailsPage.saveAsDraftLink
			).toHaveCount(0);
		}

		await commerceAdminProductDetailsPage.headerActionsButton.click();
		await commerceAdminProductDetailsPage
			.headerActionsMenuItem('Duplicate')
			.click();

		const duplicateFrame = page.frameLocator('iframe[title="Duplicate"]');

		await duplicateFrame.getByPlaceholder('Type Here').fill(catalog.name);
		await duplicateFrame
			.getByRole('menuitem', {exact: true, name: catalog.name})
			.click();
		await duplicateFrame
			.getByRole('button', {exact: true, name: 'Submit'})
			.click();

		await expect(commerceAdminProductDetailsPage.nameInput).toHaveValue(
			`Copy of ${productName}`
		);
		await expect(workflowStatus('Draft')).toBeVisible();

		await commerceAdminProductPage.gotoProduct(productName);

		if (status === 'Draft') {
			await expect(
				commerceAdminProductDetailsPage.saveAsDraftLink
			).toBeVisible();

			await commerceAdminProductDetailsPage.headerActionsButton.click();

			await expect(
				commerceAdminProductDetailsPage.headerActionsMenuItem(
					'Convert to Draft'
				)
			).toHaveCount(0);

			return;
		}

		await expect(
			commerceAdminProductDetailsPage.saveAsDraftLink
		).toHaveCount(0);

		page.once('dialog', (dialog) => dialog.dismiss());

		await commerceAdminProductDetailsPage.headerActionsButton.click();
		await commerceAdminProductDetailsPage
			.headerActionsMenuItem('Convert to Draft')
			.click();

		await expect(workflowStatus(status)).toBeVisible();

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
		await expect(workflowStatus('Draft')).toBeVisible();
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
			'@LPD-106244-Grouped-16',
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

		await commerceAdminProductDetailsSkusPage.skuAddButton.click();

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuInput.fill(
			'BLACKSKU'
		);

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuPurchasableToggle.check();

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuPublishButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.skuAddModalSuccessMessage
		).toBeVisible();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink('BLACKSKU')
			.click();

		await commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuFieldName.fill(
			'REDSKU'
		);

		const externalReferenceCodeInput =
			commerceAdminProductDetailsSkusPage.sidePanelFrame.locator(
				'input[id$="_externalReferenceCode"]'
			);
		const externalReferenceCode = getRandomString();

		await externalReferenceCodeInput.fill(externalReferenceCode);

		await commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuPublishButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Success:Your request completed successfully.'
			)
		).toBeVisible();
		await expect(externalReferenceCodeInput).toHaveValue(
			externalReferenceCode
		);

		await page.reload();

		await expect(
			commerceAdminProductDetailsSkusPage.skusTableRowLink('REDSKU')
		).toBeVisible();

		await commerceAdminProductPage
			.productRowActionsButton('REDSKU')
			.click();

		page.once('dialog', (dialog) => dialog.accept());

		await commerceAdminProductPage.deleteMenuItem.click();

		await waitForAlert(page);

		await expect(
			commerceAdminProductDetailsSkusPage.skusTableRowLink('REDSKU')
		).toHaveCount(0);
	}
);

test(
	'Editing the base price and tier prices of a SKU updates its storefront price',
	{
		tag: [
			'@COMMERCE-6022',
			'@COMMERCE-12218',
			'@LPD-106244-Grouped-16',
			'@LPD-106244-Grouped-24',
		],
	},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
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

		const goToSkuPrices = async () => {
			await commerceAdminProductPage.gotoProduct(product.name['en_US']);

			await commerceAdminProductDetailsPage.goToProductSkus();

			await commerceAdminProductDetailsSkusPage
				.skusTableRowLink(product.skus[0].sku)
				.click();
			await commerceAdminProductDetailsSkusPage.goToSkuTab('Price');
		};

		const expectStorefrontPrice = async (price: string) => {
			await page.goto(`/web${site.friendlyUrlPath}/catalog`);

			await expect(
				page.locator('.card').filter({hasText: product.name['en_US']})
			).toContainText(price);
		};

		const nestedFrame =
			commerceAdminProductDetailsSkusPage.sidePanelNestedFrame;
		const tierPriceModalFrame = page.frameLocator('.modal-body iframe');

		const clickTierPriceAction = async (price: string, action: string) => {
			await goToSkuPrices();

			await commerceAdminProductDetailsSkusPage
				.sidePanelSkuPriceTableRowLink(basePriceListName)
				.click();

			await nestedFrame
				.getByRole('row')
				.filter({hasText: price})
				.getByRole('button', {name: /Actions$/})
				.click();

			await nestedFrame
				.getByRole('menuitem', {exact: true, name: action})
				.click();
		};

		const submitTierPrice = async (price: string) => {
			await tierPriceModalFrame.getByLabel('Quantity Required').fill('1');
			await tierPriceModalFrame
				.getByLabel('Tier Price Required')
				.fill(price);
			await tierPriceModalFrame
				.getByRole('button', {exact: true, name: 'Submit'})
				.click();
		};

		await goToSkuPrices();

		await commerceAdminProductDetailsSkusPage
			.sidePanelSkuPriceTableRowLink(basePriceListName)
			.click();

		await commerceAdminProductDetailsSkusPage.sidePanelNestedPriceListPrice.fill(
			'25'
		);
		await commerceAdminProductDetailsSkusPage.sidePanelNestedSaveButton.click();

		await waitForAlert(nestedFrame);

		await expectStorefrontPrice('25.00');

		await goToSkuPrices();

		await commerceAdminProductDetailsSkusPage
			.sidePanelSkuPriceTableRowLink(basePriceListName)
			.click();

		await nestedFrame
			.locator('[data-testid="fdsCreationActionButton"]')
			.click();

		await submitTierPrice('10');

		await expectStorefrontPrice('10.00');

		await clickTierPriceAction('10.00', 'Edit');

		await submitTierPrice('20');

		await expectStorefrontPrice('20.00');

		await clickTierPriceAction('20.00', 'Delete');

		await expectStorefrontPrice('25.00');

		await goToSkuPrices();

		const basePriceListRow =
			commerceAdminProductDetailsSkusPage.skuPriceFrame
				.getByRole('row')
				.filter({hasText: basePriceListName});

		await basePriceListRow.getByRole('button', {name: /Actions$/}).click();

		await commerceAdminProductDetailsSkusPage.skuPriceFrame
			.getByRole('menuitem', {exact: true, name: 'Delete'})
			.click();

		await expect(basePriceListRow).toHaveCount(0);

		await expectStorefrontPrice('$ 0.00');
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
	'Add a SKU with subscriptions',
	{tag: ['@COMMERCE-6024', '@COMMERCE-6085', '@LPD-106244-Grouped-16']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
		page,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
				name: 'Master',
			});

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: 'Simple T-Shirt'},
				productType: 'simple',
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await page
			.getByRole('link', {exact: true, name: 'Subscription'})
			.click();

		for (const subscriptionEnabledName of [
			'deliverySubscriptionEnabled',
			'subscriptionEnabled',
		]) {
			await page
				.locator(`label[for$="_${subscriptionEnabledName}"]`)
				.click();

			await expect(
				page.locator(`input[id$="_${subscriptionEnabledName}"]`)
			).toBeChecked();
		}

		await commerceAdminProductDetailsPage.publish();

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage.skuAddButton.click();

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuInput.fill(
			'BLACKSKU'
		);

		await commerceAdminProductDetailsSkusPage.skuAddModalSkuPublishButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.skuAddModalSuccessMessage
		).toBeVisible();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink('BLACKSKU')
			.click();

		await commerceAdminProductDetailsSkusPage.goToSkuTab('Subscriptions');

		const overrideToggle =
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByLabel(
				'Override Subscription Settings'
			);

		await overrideToggle.check();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Payment Subscription'
			)
		).toBeVisible();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Delivery Subscription'
			)
		).toBeVisible();

		await commerceAdminProductDetailsSkusPage.sidePanelSaveButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Success:Your request completed successfully.'
			)
		).toBeVisible();

		await overrideToggle.uncheck();

		await commerceAdminProductDetailsSkusPage.sidePanelSaveButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Success:Your request completed successfully.'
			)
		).toBeVisible();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Payment Subscription'
			)
		).not.toBeVisible();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelFrame.getByText(
				'Delivery Subscription'
			)
		).not.toBeVisible();
	}
);

test(
	'Currency changes based on price lists',
	{
		tag: [
			'@COMMERCE-11084',
			'@COMMERCE-12218',
			'@LPD-52938',
			'@LPD-106244-Grouped-16',
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
			});

		const productSkus = await apiHelpers.headlessCommerceAdminCatalog
			.getProduct(product.productId)
			.then((product) => {
				return product.skus;
			});

		const currencies =
			await apiHelpers.headlessCommerceAdminCatalog.getCurrenciesPage('');

		const currencyEUR = currencies.items.find(
			(item) => item.name['en_US'] === 'Euro'
		);
		const currencyUSD = currencies.items.find(
			(item) => item.name['en_US'] === 'US Dollar'
		);

		const priceListEUR =
			await apiHelpers.headlessCommerceAdminPricing.postPriceList({
				catalogId: catalog.id,
				currencyCode: currencyEUR.code,
				name: 'EUR-pl ' + getRandomString(),
				type: 'price-list',
			});
		const priceListUSD =
			await apiHelpers.headlessCommerceAdminPricing.postPriceList({
				catalogId: catalog.id,
				currencyCode: currencyUSD.code,
				name: 'USD-pl ' + getRandomString(),
				type: 'price-list',
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink(`${productSkus[0].sku}`)
			.click();
		await commerceAdminProductDetailsSkusPage.goToSkuTab('Price');
		await commerceAdminProductDetailsSkusPage.skuPriceAddButton.click();
		await commerceAdminProductDetailsSkusPage.skuPriceListSelect.selectOption(
			priceListEUR.name
		);

		await expect(
			commerceAdminProductDetailsSkusPage.skuPriceAddModal.getByText(
				'EUR',
				{exact: true}
			)
		).toBeVisible();

		await commerceAdminProductDetailsSkusPage.skuPriceListSelect.selectOption(
			priceListUSD.name
		);

		await expect(
			commerceAdminProductDetailsSkusPage.skuPriceAddModal.getByText(
				'USD',
				{exact: true}
			)
		).toBeVisible();

		const unitPriceInputs =
			commerceAdminProductDetailsSkusPage.skuPriceAddModal.getByLabel(
				'Unit Price'
			);

		await unitPriceInputs.first().fill('10');

		await commerceAdminProductDetailsSkusPage.skuPriceAddModal
			.getByRole('button', {name: 'Add Entry'})
			.click();

		await commerceAdminProductDetailsSkusPage.skuPriceListSelect
			.nth(1)
			.selectOption(priceListEUR.name);

		await unitPriceInputs.nth(1).fill('20');

		await page
			.locator('.modal-footer')
			.getByRole('button', {exact: true, name: 'Add'})
			.click();

		for (const {price, priceList} of [
			{price: '10.00', priceList: priceListUSD},
			{price: '20.00', priceList: priceListEUR},
		]) {
			await expect(
				commerceAdminProductDetailsSkusPage.skuPriceFrame
					.getByRole('row')
					.filter({hasText: priceList.name})
			).toContainText(price);
		}

		await commerceAdminProductDetailsSkusPage
			.sidePanelSkuPriceTableRowLink(priceListUSD.name)
			.click();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelNestedFrame
				.locator('.input-group-text', {hasText: 'USD'})
				.first()
		).toBeVisible();

		await commerceAdminPriceListsPage.goto();

		await commerceAdminPriceListsPage
			.priceListLink(priceListUSD.name)
			.click();

		await commerceAdminPriceListDetailsPage.currencySelect.selectOption({
			label: 'EUR',
		});
		await commerceAdminPriceListDetailsPage.publishButton.click();

		await waitForAlert(page);

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink(`${productSkus[0].sku}`)
			.click();
		await commerceAdminProductDetailsSkusPage.goToSkuTab('Price');
		await commerceAdminProductDetailsSkusPage
			.sidePanelSkuPriceTableRowLink(priceListUSD.name)
			.click();

		await expect(
			commerceAdminProductDetailsSkusPage.sidePanelNestedFrame
				.locator('.input-group-text', {hasText: 'EUR'})
				.first()
		).toBeVisible();
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
	'Edit a product name and its translation',
	{tag: ['@COMMERCE-5808', '@LPD-106244-Grouped-16']},
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

		const productName = getRandomString();

		await commerceAdminProductDetailsPage.nameInput.fill(productName);

		await commerceAdminProductDetailsPage.publish();

		await expect(commerceAdminProductDetailsPage.nameInput).toHaveValue(
			productName
		);

		const spanishLocaleMenuItem = page.getByRole('menuitem', {
			name: 'es-ES',
		});

		await clickAndExpectToBeVisible({
			autoClick: true,
			target: spanishLocaleMenuItem,
			trigger: commerceAdminProductDetailsPage.nameInputLocaleSelector,
		});

		const translatedProductName = getRandomString();

		await commerceAdminProductDetailsPage.nameInput.fill(
			translatedProductName
		);

		await commerceAdminProductDetailsPage.publish();

		await clickAndExpectToBeVisible({
			autoClick: true,
			target: spanishLocaleMenuItem,
			trigger: commerceAdminProductDetailsPage.nameInputLocaleSelector,
		});

		await expect(commerceAdminProductDetailsPage.nameInput).toHaveValue(
			translatedProductName
		);
	}
);

for (const {productType, tags} of [
	{productType: 'Grouped', tags: ['@COMMERCE-6302']},
	{productType: 'Simple', tags: ['@COMMERCE-9179', '@COMMERCE-9180']},
	{productType: 'Virtual', tags: ['@COMMERCE-6301']},
]) {
	test(
		`Publish a ${productType} product`,
		{tag: [...tags, '@LPD-106244-Grouped-16']},
		async ({
			apiHelpers,
			commerceAdminProductDetailsPage,
			commerceAdminProductPage,
			page,
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

				await expect(page.getByText(productName)).toBeVisible();
				await expect(
					page.locator('.workflow-status-draft')
				).toBeVisible();
				await expect(
					page
						.locator('select[name$="_commerceCatalogGroupId"]')
						.locator('option:checked')
				).toHaveText(catalog.name);
				await expect(
					commerceAdminProductDetailsPage.nameInput
				).toHaveValue(productName);
				await expect(
					commerceAdminProductDetailsPage.publishLink
				).toBeVisible();
				await expect(
					commerceAdminProductDetailsPage.saveAsDraftLink
				).toBeVisible();

				await commerceAdminProductDetailsPage.publish();

				await expect(
					page.locator('.workflow-status-approved')
				).toBeVisible();

				await commerceAdminProductDetailsPage.backLink.click();

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

for (const {productStatus, status} of [
	{productStatus: 0, status: 'Approved'},
	{productStatus: 2, status: 'Draft'},
	{productStatus: 3, status: 'Expired'},
	{productStatus: 4, status: 'Denied'},
	{productStatus: 5, status: 'Inactive'},
	{productStatus: 6, status: 'Incomplete'},
	{productStatus: 7, status: 'Scheduled'},
	{productStatus: 8, status: 'In Recycle Bin'},
	{productStatus: 9, status: 'Any'},
]) {
	test(
		`A product with ${status} status can be duplicated and converted to draft`,
		{tag: ['@COMMERCE-9251', '@LPD-106244-Grouped-23']},
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
				productStatus,
				status,
			});
		}
	);
}

test(
	'A product with Pending status can be duplicated and converted to draft',
	{tag: ['@COMMERCE-9251', '@LPD-106244-Grouped-23']},
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
	'Converting an approved product to draft keeps its edited name',
	{tag: ['@COMMERCE-9183', '@LPD-106244-Grouped-23']},
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

		await expect(
			commerceAdminProductDetailsPage.saveAsDraftLink
		).toHaveCount(0);

		const productName = getRandomString();

		await commerceAdminProductDetailsPage.nameInput.fill(productName);

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
		await expect(page.locator('.workflow-status-draft')).toBeVisible();
		await expect(commerceAdminProductDetailsPage.nameInput).toHaveValue(
			productName
		);
	}
);

test(
	'Saving a draft product as draft keeps its edited name',
	{tag: ['@COMMERCE-9181', '@LPD-106244-Grouped-23']},
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

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		const productName = getRandomString();

		await commerceAdminProductDetailsPage.nameInput.fill(productName);

		await commerceAdminProductDetailsPage.saveAsDraftLink.click();

		await expect(page.locator('.workflow-status-draft')).toBeVisible();
		await expect(commerceAdminProductDetailsPage.nameInput).toHaveValue(
			productName
		);
	}
);

test(
	'Publish a product with an expiration date',
	{tag: ['@COMMERCE-6303', '@LPD-106244-Grouped-23']},
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

		await page.getByLabel('Never Expire').uncheck();

		const expirationDate = new Date();

		expirationDate.setFullYear(expirationDate.getFullYear() + 1);

		await page
			.locator('input[id*="expirationDate"][placeholder="mm/dd/yyyy"]')
			.fill(
				[
					String(expirationDate.getMonth() + 1).padStart(2, '0'),
					String(expirationDate.getDate()).padStart(2, '0'),
					expirationDate.getFullYear(),
				].join('/')
			);

		await expect(
			await commerceAdminProductDetailsPage.publish()
		).toBeVisible();
	}
);

test(
	'Search, sort and paginate the products and SKUs admin lists',
	{
		tag: [
			'@COMMERCE-5801',
			'@COMMERCE-5803',
			'@COMMERCE-5805',
			'@LPD-106244-Grouped-23',
		],
	},
	async ({apiHelpers, commerceAdminProductPage, page}) => {
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

		const nextPageButton = page.locator('[data-testid="nextArrow"]');

		const searchList = async (keywords: string) => {
			await commerceAdminProductPage.managementToolbarSearchInput.fill(
				keywords
			);
			await commerceAdminProductPage.managementToolbarSearchInput.press(
				'Enter'
			);
		};

		await test.step('Search and sort the products', async () => {
			await commerceAdminProductPage.goto();

			await searchList(prefix);

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
				await commerceAdminProductPage.table
					.getByRole('columnheader', {exact: true, name: column})
					.click();

				for (const [index, productName] of expectedProductNames
					.slice(0, 3)
					.entries()) {
					await expect(
						commerceAdminProductPage.table
							.locator('tbody tr')
							.nth(index)
					).toContainText(productName);
				}
			}
		});

		await test.step('Paginate the products', async () => {
			await expect(
				page.getByText('Showing 1 to 20 of 21 entries.')
			).toBeVisible();
			await expect(
				page.getByRole('button', {name: '20 Items'})
			).toBeVisible();

			await nextPageButton.click();

			await expect(
				page.getByText('Showing 21 to 21 of 21 entries.')
			).toBeVisible();
		});

		await test.step('Search and paginate the SKUs', async () => {
			await commerceAdminProductPage.productSkusLink.click();

			await searchList(prefix);

			await expect(
				page.getByText('Showing 1 to 20 of 21 entries.')
			).toBeVisible();

			await nextPageButton.click();

			await expect(
				page.getByText('Showing 21 to 21 of 21 entries.')
			).toBeVisible();

			await searchList(`${prefix}sku7`);

			await expect(
				page.getByText(`${prefix}sku7`, {exact: true})
			).toBeVisible();
			await expect(
				page.getByText('Showing 1 to 1 of 1 entries.')
			).toBeVisible();
		});
	}
);

for (const {
	cycleLengthContainerId,
	subscriptionEnabledId,
	subscriptionLengthId,
	subscriptionName,
	subscriptionTypeId,
} of [
	{
		cycleLengthContainerId: 'deliveryCycleLengthContainer',
		subscriptionEnabledId: 'deliverySubscriptionEnabled',
		subscriptionLengthId: 'deliverySubscriptionLength',
		subscriptionName: 'Delivery Subscription',
		subscriptionTypeId: 'deliverySubscriptionType',
	},
	{
		cycleLengthContainerId: 'cycleLengthContainer',
		subscriptionEnabledId: 'subscriptionEnabled',
		subscriptionLengthId: 'subscriptionLength',
		subscriptionName: 'Payment Subscription',
		subscriptionTypeId: 'subscriptionType',
	},
]) {
	test(
		`The ${subscriptionName} length shows the subscription type in singular or plural`,
		{tag: ['@COMMERCE-9744', '@COMMERCE-9798', '@LPD-106244-Grouped-24']},
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

			await page
				.getByRole('link', {exact: true, name: 'Subscription'})
				.click();

			await page
				.locator(`label[for$="_${subscriptionEnabledId}"]`)
				.click();

			const subscriptionLengthInput = page.locator(
				`input[id$="_${subscriptionLengthId}"]`
			);
			const subscriptionLengthSuffix = page.locator(
				`[id$="_${cycleLengthContainerId}"] .input-group-text`
			);
			const subscriptionTypeSelect = page.locator(
				`select[id$="_${subscriptionTypeId}"]`
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
	{tag: ['@COMMERCE-9806', '@COMMERCE-11150', '@LPD-106244-Grouped-24']},
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

		const subscriptionLink = page.getByRole('link', {
			exact: true,
			name: 'Subscription',
		});

		await subscriptionLink.click();

		await page
			.locator('label[for$="_deliverySubscriptionEnabled"]')
			.click();

		const deliveryDayInput = page.locator(
			'input[name$="_deliverySubscriptionTypeSettings--yearly--deliveryMonthDay--"]'
		);
		const deliveryModeSelect = page.locator(
			'select[name$="_deliverySubscriptionTypeSettings--yearly--deliveryYearlyMode--"]'
		);
		const deliveryMonthSelect = page.locator(
			'select[name$="_deliverySubscriptionTypeSettings--yearly--deliveryMonth--"]'
		);
		const deliverySubscriptionLengthInput = page.locator(
			'input[id$="_deliverySubscriptionLength"]'
		);
		const deliverySubscriptionTypeSelect = page.locator(
			'select[id$="_deliverySubscriptionType"]'
		);

		await deliverySubscriptionTypeSelect.selectOption({label: 'Year'});
		await deliveryModeSelect.selectOption({label: 'Exact Day of Year'});
		await deliveryMonthSelect.selectOption({label: 'March'});
		await deliveryDayInput.fill('5');
		await deliverySubscriptionLengthInput.fill('5');

		await commerceAdminProductDetailsPage.publishLink.click();

		await waitForAlert(page);

		await page.getByRole('link', {exact: true, name: 'Details'}).click();

		await subscriptionLink.click();

		for (const {select, value} of [
			{select: deliverySubscriptionTypeSelect, value: 'Year'},
			{select: deliveryModeSelect, value: 'Exact Day of Year'},
			{select: deliveryMonthSelect, value: 'March'},
		]) {
			await expect(select.locator('option:checked')).toHaveText(value);
		}

		for (const input of [
			deliveryDayInput,
			deliverySubscriptionLengthInput,
		]) {
			await expect(input).toHaveValue('5');
		}

		await page.locator('label[for$="_subscriptionEnabled"]').click();

		await commerceAdminProductDetailsPage.publishLink.click();

		await waitForAlert(page);

		const paymentSubscriptionTypeSelect = page.locator(
			'select[id$="_subscriptionType"]'
		);

		await expect(
			paymentSubscriptionTypeSelect.locator('option:checked')
		).toHaveText('Day');

		await paymentSubscriptionTypeSelect.selectOption({label: 'Year'});

		await commerceAdminProductDetailsPage.publishLink.click();

		await waitForAlert(page);

		await expect(
			page
				.locator(
					'select[name$="_subscriptionTypeSettings--yearly--yearlyMode--"]'
				)
				.locator('option:checked')
		).toHaveText('Order Date');

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink(product.skus[0].sku)
			.click();
		await commerceAdminProductDetailsSkusPage.goToSkuTab('Subscriptions');

		const sidePanelFrame =
			commerceAdminProductDetailsSkusPage.sidePanelFrame;

		await sidePanelFrame
			.getByLabel('Override Subscription Settings')
			.check();
		await sidePanelFrame
			.locator('label[for$="_deliverySubscriptionEnabled"]')
			.click();

		await commerceAdminProductDetailsSkusPage.sidePanelSaveButton.click();

		await waitForAlert(sidePanelFrame);

		const skuDeliveryModeSelect = sidePanelFrame.locator(
			'select[name$="_deliverySubscriptionTypeSettings--yearly--deliveryYearlyMode--"]'
		);
		const skuDeliverySubscriptionTypeSelect = sidePanelFrame.locator(
			'select[id$="_deliverySubscriptionType"]'
		);

		await expect(
			skuDeliverySubscriptionTypeSelect.locator('option:checked')
		).toHaveText('Day');

		await skuDeliverySubscriptionTypeSelect.selectOption({label: 'Year'});

		await expect(
			skuDeliveryModeSelect.locator('option:checked')
		).toHaveText('Order Date');

		await commerceAdminProductDetailsSkusPage.sidePanelSaveButton.click();

		await waitForAlert(sidePanelFrame);

		for (const {select, value} of [
			{select: skuDeliverySubscriptionTypeSelect, value: 'Year'},
			{select: skuDeliveryModeSelect, value: 'Order Date'},
		]) {
			await expect(select.locator('option:checked')).toHaveText(value);
		}

		await expect(
			sidePanelFrame.locator('input[id$="_deliverySubscriptionLength"]')
		).toHaveValue('1');
	}
);

test(
	'Add price entries for different units of measure from the SKU price modal',
	{tag: ['@COMMERCE-12289', '@COMMERCE-12290', '@LPD-106244-Grouped-24']},
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

		const currencies =
			await apiHelpers.headlessCommerceAdminCatalog.getCurrenciesPage('');

		const [priceListEUR, priceListUSD] = await Promise.all(
			['Euro', 'US Dollar'].map((currencyName) =>
				apiHelpers.headlessCommerceAdminPricing.postPriceList({
					catalogId: catalog.id,
					currencyCode: currencies.items.find(
						(item) => item.name['en_US'] === currencyName
					).code,
					name: getRandomString(),
					type: 'price-list',
				})
			)
		);

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink(product.skus[0].sku)
			.click();
		await commerceAdminProductDetailsSkusPage.goToSkuTab('Price');
		await commerceAdminProductDetailsSkusPage.skuPriceAddButton.click();

		const skuPriceAddModal =
			commerceAdminProductDetailsSkusPage.skuPriceAddModal;
		const unitOfMeasureSelects =
			skuPriceAddModal.getByLabel('Unit of Measure');
		const unitPriceInputs = skuPriceAddModal.getByLabel('Unit Price');

		await expect(
			commerceAdminProductDetailsSkusPage.skuPriceListSelect.locator(
				'option:checked'
			)
		).toHaveText(`${catalog.name} Base Price List`);
		await expect(unitPriceInputs).toHaveValue(/^50(\.0+)?$/);
		await expect(unitOfMeasureSelects).toHaveCount(0);

		await page
			.locator('.modal-footer')
			.getByRole('button', {exact: true, name: 'Cancel'})
			.click();

		for (const index of [1, 2]) {
			await apiHelpers.headlessCommerceAdminCatalog.postSkuUnitOfMeasure(
				product.skus[0].id,
				{
					basePrice: index,
					key: `uom${index}`,
					name: {en_US: `uom${index}`},
					priority: index,
				}
			);
		}

		await commerceAdminProductDetailsSkusPage.goToSkuTab('Price');

		await expect(
			commerceAdminProductDetailsSkusPage.skuPriceFrame
				.getByRole('row')
				.filter({hasText: catalog.name})
		).toHaveCount(4);

		await commerceAdminProductDetailsSkusPage.skuPriceAddButton.click();

		await expect(
			commerceAdminProductDetailsSkusPage.skuPriceListSelect.locator(
				'option:checked'
			)
		).toHaveText(`${catalog.name} Base Price List`);
		await expect(unitOfMeasureSelects.locator('option:checked')).toHaveText(
			'uom1'
		);
		await expect(unitPriceInputs).toHaveValue(/^1(\.0+)?$/);

		await commerceAdminProductDetailsSkusPage.skuPriceListSelect.selectOption(
			priceListUSD.name
		);
		await unitOfMeasureSelects.selectOption('uom1');
		await unitPriceInputs.fill('30');

		await skuPriceAddModal.getByRole('button', {name: 'Add Entry'}).click();

		await commerceAdminProductDetailsSkusPage.skuPriceListSelect
			.nth(1)
			.selectOption(priceListEUR.name);
		await unitOfMeasureSelects.nth(1).selectOption('uom2');
		await unitPriceInputs.nth(1).fill('90');

		await page
			.locator('.modal-footer')
			.getByRole('button', {exact: true, name: 'Add'})
			.click();

		for (const {price, priceList, unitOfMeasure} of [
			{price: '$ 30.00', priceList: priceListUSD, unitOfMeasure: 'uom1'},
			{price: '€ 90.00', priceList: priceListEUR, unitOfMeasure: 'uom2'},
		]) {
			const priceEntryRow =
				commerceAdminProductDetailsSkusPage.skuPriceFrame
					.getByRole('row')
					.filter({hasText: priceList.name});

			await expect(priceEntryRow).toContainText(unitOfMeasure);
			await expect(priceEntryRow).toContainText(price);
		}
	}
);

test(
	'A SKU external reference code cannot be used by another SKU',
	{tag: ['@COMMERCE-9891', '@LPD-106244-Grouped-24']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
	}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const externalReferenceCode = getRandomString();
		const sidePanelFrame =
			commerceAdminProductDetailsSkusPage.sidePanelFrame;

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

			await sidePanelFrame
				.locator('input[id$="_externalReferenceCode"]')
				.fill(externalReferenceCode);

			await commerceAdminProductDetailsSkusPage.sidePanelDetailsSkuPublishButton.click();
		};

		await publishSkuExternalReferenceCode();

		await waitForAlert(sidePanelFrame);

		await publishSkuExternalReferenceCode();

		await waitForAlert(
			sidePanelFrame,
			'Error:Please enter a unique external reference code.',
			{autoClose: false, type: 'danger'}
		);

		await expect(sidePanelFrame.locator('.alert-success')).toHaveCount(0);
	}
);
