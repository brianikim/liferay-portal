/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';
import {readFileSync} from 'fs';
import path from 'path';

import {apiHelpersTest} from '../../../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {globalMenuPagesTest} from '../../../../fixtures/globalMenuPagesTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {clickAndExpectToBeVisible} from '../../../../utils/clickAndExpectToBeVisible';
import createTempFile from '../../../../utils/createTempFile';
import getRandomString from '../../../../utils/getRandomString';
import {waitForAlert} from '../../../../utils/waitForAlert';

export const test = mergeTests(
	apiHelpersTest,
	globalMenuPagesTest,
	commercePagesTest,
	dataApiHelpersTest,
	loginTest()
);

test(
	'LPD-21637 Virtual item details section visible for product and sku',
	{
		tag: [
			'@COMMERCE-6301',
			'@COMMERCE-9800',
			'@COMMERCE-11343',
			'@LPD-106244-Grouped-18',
			'@LPD-106244-Grouped-32',
		],
	},
	async ({apiHelpers, commerceAdminProductPage, globalMenuPage, page}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const virtualProduct =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				productType: 'virtual',
				skus: [
					{
						cost: 0,
						price: 0,
						published: true,
						purchasable: true,
						sku: 'VirtualSku',
					},
				],
			});

		await globalMenuPage.goToCommerce('Products');

		await commerceAdminProductPage.managementToolbarSearchInput.fill(
			virtualProduct.name.en_US
		);
		await commerceAdminProductPage.managementToolbarSearchInput.press(
			'Enter'
		);
		await commerceAdminProductPage
			.productsTableRowLink(virtualProduct.name.en_US)
			.click();
		await commerceAdminProductPage.productVirtualLink.click();
		await commerceAdminProductPage.addVirtualProductFileEntryButton.click();

		const productVirtualFileEntryURL =
			'http://test-virtual-product-details-section.com';

		await commerceAdminProductPage.productVirtualFileEntryURLInput.fill(
			productVirtualFileEntryURL
		);

		await commerceAdminProductPage.productVirtualFileEntrySaveButton.click();
		await commerceAdminProductPage.productVirtualFileEntryCancelButton.click();

		await expect(page.getByText(productVirtualFileEntryURL)).toBeVisible();

		const enableSampleToggle = page.getByLabel('Enable Sample');
		const sampleSectionLink = page.locator('fieldset#sample > a');

		await clickAndExpectToBeVisible({
			target: enableSampleToggle,
			trigger: sampleSectionLink,
		});

		await enableSampleToggle.check();

		await page.locator('[id$="_selectSampleFile"]').click();

		const itemSelectorFrame = page.frameLocator(
			'iframe[title="Select File"]'
		);
		const sampleFileName = `${getRandomString()}.txt`;

		await itemSelectorFrame
			.locator('input[type="file"]')
			.setInputFiles(
				createTempFile(
					sampleFileName,
					readFileSync(
						path.join(__dirname, '/dependencies/attachment.txt')
					)
				)
			);
		await itemSelectorFrame
			.getByRole('button', {exact: true, name: 'Add'})
			.click();

		const sampleFileEntryName = page.locator(
			'[id$="_sampleFileEntryNameInput"]'
		);

		await expect(sampleFileEntryName).toContainText(sampleFileName);

		await page.getByRole('button', {exact: true, name: 'Save'}).click();

		await waitForAlert(page);

		const sampleFileEntryRemoveButton = page
			.locator('[id$="_sampleFileEntryRemove"]')
			.getByRole('button');

		await clickAndExpectToBeVisible({
			target: sampleFileEntryRemoveButton,
			trigger: sampleSectionLink,
		});

		await sampleFileEntryRemoveButton.click();

		await expect(sampleFileEntryName).not.toContainText(sampleFileName);

		const sampleFileURLInput = page.getByLabel('Sample File URL');

		await sampleFileURLInput.fill('virtualproduct');

		await expect(sampleFileURLInput).toHaveValue('virtualproduct');

		await commerceAdminProductPage.productSkusLink.click();
		await commerceAdminProductPage
			.productSkuTableRowLink('VirtualSku')
			.click();

		await expect(
			commerceAdminProductPage.virtualSettingsOverrideLink
		).toBeVisible();
		await expect(
			page
				.frameLocator('iframe')
				.getByText('Shipping Override', {exact: true})
		).toHaveCount(0);

		await commerceAdminProductPage.virtualSettingsOverrideLink.click();
		await commerceAdminProductPage.productSkuVirtualOverrideToggle.check();
		await commerceAdminProductPage.addVirtualSkuFileEntryButton.click();
		await commerceAdminProductPage.productSkuVirtualFileEntryURLInput.fill(
			'http://test-virtual-product-sku-details-section.com'
		);
		await commerceAdminProductPage.productSkuVirtualFileEntrySaveButton.click();

		await waitForAlert(
			page.frameLocator('iframe').frameLocator('iframe >> nth=1')
		);

		await commerceAdminProductPage.productSkuVirtualFileEntryCancelButton.click();

		const skuFrame = page.frameLocator('iframe');

		const skuFileEntryURL = skuFrame.getByText(
			'http://test-virtual-product-sku-details-section.com'
		);

		await expect(skuFileEntryURL).toBeVisible();

		const activationStatusSelect = skuFrame.getByLabel('Activation Status');
		const baseInformationLink = skuFrame.getByRole('button', {
			exact: true,
			name: 'Base Information',
		});

		await clickAndExpectToBeVisible({
			target: activationStatusSelect,
			trigger: baseInformationLink,
		});

		await activationStatusSelect.selectOption({label: 'Pending'});

		const durationInput = skuFrame.getByLabel('Duration');
		const maxUsagesInput = skuFrame.getByLabel('Max Number of Downloads');

		await durationInput.fill('3');
		await maxUsagesInput.fill('3');

		const skuEnableSampleToggle = skuFrame.getByLabel('Enable Sample');

		await clickAndExpectToBeVisible({
			target: skuEnableSampleToggle,
			trigger: skuFrame.getByRole('button', {
				exact: true,
				name: 'Sample',
			}),
		});

		await skuEnableSampleToggle.check();

		await skuFrame
			.getByLabel('Sample File URL')
			.fill('http://test-virtual-product-sku-sample.com');

		const skuSaveButton = skuFrame.getByRole('button', {
			exact: true,
			name: 'Save',
		});

		await skuSaveButton.click();

		await waitForAlert(skuFrame);

		await commerceAdminProductPage.productSkuVirtualOverrideToggle.uncheck();

		await skuSaveButton.click();

		await waitForAlert(skuFrame);

		await commerceAdminProductPage.productSkuVirtualOverrideToggle.check();

		await clickAndExpectToBeVisible({
			target: activationStatusSelect,
			trigger: baseInformationLink,
		});

		await expect(
			activationStatusSelect.locator('option:checked')
		).toHaveText('Completed');
		await expect(durationInput).toHaveValue('0');
		await expect(maxUsagesInput).toHaveValue('0');
		await expect(skuEnableSampleToggle).not.toBeChecked();
		await expect(skuFileEntryURL).toHaveCount(0);
	}
);

test(
	'Configure the file entry, maximum downloads and sample of a virtual product',
	{tag: ['@COMMERCE-6751', '@LPD-106244-Grouped-23']},
	async ({apiHelpers, commerceAdminProductPage, page}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const virtualProduct =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				productType: 'virtual',
			});

		await commerceAdminProductPage.gotoProduct(virtualProduct.name.en_US);

		await commerceAdminProductPage.productVirtualLink.click();
		await commerceAdminProductPage.addVirtualProductFileEntryButton.click();

		const fileEntrySidePanelFrame = page.frameLocator('iframe >> nth=1');

		await fileEntrySidePanelFrame
			.getByRole('button', {exact: true, name: 'Select'})
			.click();

		const fileEntryItemSelectorFrame = fileEntrySidePanelFrame.frameLocator(
			'iframe[title="Select File"]'
		);
		const fileEntryName = `${getRandomString()}.txt`;
		const fileContent = readFileSync(
			path.join(__dirname, '/dependencies/attachment.txt')
		);

		await fileEntryItemSelectorFrame
			.locator('input[type="file"]')
			.setInputFiles(createTempFile(fileEntryName, fileContent));
		await fileEntryItemSelectorFrame
			.getByRole('button', {exact: true, name: 'Add'})
			.click();

		await commerceAdminProductPage.productVirtualFileEntrySaveButton.click();
		await commerceAdminProductPage.productVirtualFileEntryCancelButton.click();

		await expect(page.getByText(fileEntryName)).toBeVisible();

		const maxNumberOfDownloadsInput = page.getByLabel(
			'Max Number of Downloads'
		);

		await clickAndExpectToBeVisible({
			target: maxNumberOfDownloadsInput,
			trigger: page.locator('fieldset#baseInformation > a'),
		});

		await maxNumberOfDownloadsInput.fill('10');

		const enableSampleToggle = page.getByLabel('Enable Sample');

		await clickAndExpectToBeVisible({
			target: enableSampleToggle,
			trigger: page.locator('fieldset#sample > a'),
		});

		await enableSampleToggle.check();

		await page.locator('[id$="_selectSampleFile"]').click();

		const sampleItemSelectorFrame = page.frameLocator(
			'iframe[title="Select File"]'
		);
		const sampleFileName = `${getRandomString()}.txt`;

		await sampleItemSelectorFrame
			.locator('input[type="file"]')
			.setInputFiles(createTempFile(sampleFileName, fileContent));
		await sampleItemSelectorFrame
			.getByRole('button', {exact: true, name: 'Add'})
			.click();

		await page.getByRole('button', {exact: true, name: 'Save'}).click();

		await waitForAlert(page);

		await page.reload();

		await expect(page.getByText(fileEntryName)).toBeVisible();
		await expect(
			page.locator('[id$="_sampleFileEntryNameInput"]')
		).toContainText(sampleFileName);
		await expect(maxNumberOfDownloadsInput).toHaveValue('10');
	}
);
