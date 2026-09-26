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
	{tag: ['@COMMERCE-6301', '@COMMERCE-9800', '@LPD-106244-Grouped-18']},
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

		await expect(
			page
				.frameLocator('iframe')
				.getByText(
					'http://test-virtual-product-sku-details-section.com'
				)
		).toBeVisible();
	}
);
