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
	'Editing the base price list entry of a SKU updates its storefront price',
	{tag: ['@COMMERCE-6022', '@COMMERCE-12218']},
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

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductSkus();

		await commerceAdminProductDetailsSkusPage
			.skusTableRowLink(product.skus[0].sku)
			.click();
		await commerceAdminProductDetailsSkusPage.goToSkuTab('Price');
		await commerceAdminProductDetailsSkusPage
			.sidePanelSkuPriceTableRowLink(`${catalog.name} Base Price List`)
			.click();

		await commerceAdminProductDetailsSkusPage.sidePanelNestedPriceListPrice.fill(
			'25'
		);
		await commerceAdminProductDetailsSkusPage.sidePanelNestedSaveButton.click();

		await waitForAlert(
			commerceAdminProductDetailsSkusPage.sidePanelNestedFrame
		);

		await page.goto(`/web${site.friendlyUrlPath}/catalog`);

		await expect(
			page.locator('.card').filter({hasText: product.name['en_US']})
		).toContainText('25.00');
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
