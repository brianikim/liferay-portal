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
import {fillLocalizedInput} from '../../utils/fillLocalizedInput';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	dataApiHelpersTest,
	isolatedSiteTest,
	loginTest(),
	userPersonalBarPagesTest
);

async function addProductToCleanUp(
	apiHelpers: DataApiHelpers,
	productName: string
) {
	const product = (
		await apiHelpers.headlessCommerceAdminCatalog.getProducts(
			new URLSearchParams({
				filter: `name eq '${productName}'`,
			})
		)
	).items[0];

	if (product) {
		apiHelpers.data.push({id: product.productId, type: 'product'});
	}
}

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
		await addProductToCleanUp(apiHelpers, `Copy of ${productName}`);
	}
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
	'Add a SKU',
	{tag: '@COMMERCE-6021'},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
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
	{tag: '@COMMERCE-6024'},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
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
	{tag: '@LPD-52938'},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsSkusPage,
		commerceAdminProductPage,
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
				name: 'EUR-pl',
				type: 'price-list',
			});
		const priceListUSD =
			await apiHelpers.headlessCommerceAdminPricing.postPriceList({
				catalogId: catalog.id,
				currencyCode: currencyUSD.code,
				name: 'USD-pl',
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
				await addProductToCleanUp(apiHelpers, name);
			}
		}
	}
);

test(
	'Publish a Grouped product',
	{tag: ['@COMMERCE-6302', '@LPD-106244-Grouped-2']},
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
				.menuItemProductType('Grouped')
				.click();
			await commerceAdminProductPage.modalFieldName.fill(productName);
			await commerceAdminProductPage.modalPlaceHolder.fill(catalog.name);
			await commerceAdminProductPage.modalMenuItem(catalog.name).click();
			await commerceAdminProductPage.modalSubmitButton.click();

			await expect(commerceAdminProductDetailsPage.nameInput).toHaveValue(
				productName
			);
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
			).toContainText('Grouped');
		}
		finally {
			await addProductToCleanUp(apiHelpers, productName);
		}
	}
);
