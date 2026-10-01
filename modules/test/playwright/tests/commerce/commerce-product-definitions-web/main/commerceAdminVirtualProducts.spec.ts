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
	'Virtual settings can be configured for a product and overridden for its SKU',
	{tag: ['@COMMERCE-6301', '@LPD-21637', '@LPD-106244-Grouped-7']},
	async ({apiHelpers, commerceAdminProductPage, page}) => {
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

		await test.step('Add a file entry to the virtual product', async () => {
			await commerceAdminProductPage.gotoProduct(
				virtualProduct.name.en_US
			);

			await commerceAdminProductPage.productVirtualLink.click();
			await commerceAdminProductPage.addVirtualProductFileEntryButton.click();

			const productVirtualFileEntryURL =
				'http://test-virtual-product-details-section.com';

			await commerceAdminProductPage.productVirtualFileEntryURLInput.fill(
				productVirtualFileEntryURL
			);
			await commerceAdminProductPage.productVirtualFileEntrySaveButton.click();
			await commerceAdminProductPage.productVirtualFileEntryCancelButton.click();

			await expect(
				commerceAdminProductPage.productVirtualFileEntry(
					productVirtualFileEntryURL
				)
			).toBeVisible();
		});

		const productSkuVirtualFileEntryURL =
			'http://test-virtual-product-sku-details-section.com';

		await test.step('Override the virtual settings of its SKU', async () => {
			await commerceAdminProductPage.productSkusLink.click();
			await commerceAdminProductPage
				.productSkuTableRowLink('VirtualSku')
				.click();

			await expect(
				commerceAdminProductPage.virtualSettingsOverrideLink
			).toBeVisible();
			await expect(
				commerceAdminProductPage.productSkuShippingOverrideTitle
			).toHaveCount(0);

			await commerceAdminProductPage.virtualSettingsOverrideLink.click();
			await commerceAdminProductPage.productSkuVirtualOverrideToggle.check();
			await commerceAdminProductPage.addVirtualSkuFileEntryButton.click();
			await commerceAdminProductPage.productSkuVirtualFileEntryURLInput.fill(
				productSkuVirtualFileEntryURL
			);
			await commerceAdminProductPage.productSkuVirtualFileEntrySaveButton.click();

			await waitForAlert(
				commerceAdminProductPage.productSkuVirtualFileEntryFrame
			);

			await commerceAdminProductPage.productSkuVirtualFileEntryCancelButton.click();

			await expect(
				commerceAdminProductPage.productSkuVirtualFileEntry(
					productSkuVirtualFileEntryURL
				)
			).toBeVisible();
		});
	}
);

test(
	'Configure the file entry, maximum downloads and sample of a virtual product',
	{tag: ['@COMMERCE-6751', '@COMMERCE-9800', '@LPD-106244-Grouped-7']},
	async ({apiHelpers, commerceAdminProductPage, page}) => {
		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const virtualProduct =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				productType: 'virtual',
			});

		const fileContent = readFileSync(
			path.join(__dirname, '/dependencies/attachment.txt')
		);
		const fileEntryName = `${getRandomString()}.txt`;
		const sampleFileName = `${getRandomString()}.txt`;

		await test.step('Configure the file entry, maximum downloads and sample', async () => {
			await commerceAdminProductPage.gotoProduct(
				virtualProduct.name.en_US
			);

			await commerceAdminProductPage.productVirtualLink.click();
			await commerceAdminProductPage.addVirtualProductFileEntryButton.click();
			await commerceAdminProductPage.productVirtualFileEntrySelectButton.click();
			await commerceAdminProductPage.productVirtualFileEntryItemSelectorFileInput.setInputFiles(
				createTempFile(fileEntryName, fileContent)
			);
			await commerceAdminProductPage.productVirtualFileEntryItemSelectorAddButton.click();
			await commerceAdminProductPage.productVirtualFileEntrySaveButton.click();
			await commerceAdminProductPage.productVirtualFileEntryCancelButton.click();

			await expect(
				commerceAdminProductPage.productVirtualFileEntry(fileEntryName)
			).toBeVisible();

			await clickAndExpectToBeVisible({
				target: commerceAdminProductPage.productVirtualMaxUsagesInput,
				trigger:
					commerceAdminProductPage.productVirtualBaseInformationLink,
			});

			await commerceAdminProductPage.productVirtualMaxUsagesInput.fill(
				'10'
			);

			await clickAndExpectToBeVisible({
				target: commerceAdminProductPage.productVirtualEnableSampleToggle,
				trigger: commerceAdminProductPage.productVirtualSampleLink,
			});

			await commerceAdminProductPage.productVirtualEnableSampleToggle.check();
			await commerceAdminProductPage.productVirtualSelectSampleFileButton.click();
			await commerceAdminProductPage.productVirtualSampleItemSelectorFileInput.setInputFiles(
				createTempFile(sampleFileName, fileContent)
			);
			await commerceAdminProductPage.productVirtualSampleItemSelectorAddButton.click();
			await commerceAdminProductPage.productVirtualSaveButton.click();

			await waitForAlert(page);

			await page.reload();

			await expect(
				commerceAdminProductPage.productVirtualFileEntry(fileEntryName)
			).toBeVisible();
			await expect(
				commerceAdminProductPage.productVirtualEnableSampleToggle
			).toBeChecked();
			await expect(
				commerceAdminProductPage.productVirtualMaxUsagesInput
			).toHaveValue('10');
			await expect(
				commerceAdminProductPage.productVirtualSampleFileEntryNameInput
			).toContainText(sampleFileName);
		});

		await test.step('Replace the sample file with a sample file URL', async () => {
			await clickAndExpectToBeVisible({
				target: commerceAdminProductPage.productVirtualSampleFileEntryRemoveButton,
				trigger: commerceAdminProductPage.productVirtualSampleLink,
			});

			await commerceAdminProductPage.productVirtualSampleFileEntryRemoveButton.click();

			await expect(
				commerceAdminProductPage.productVirtualSampleFileEntryNameInput
			).not.toContainText(sampleFileName);

			await commerceAdminProductPage.productVirtualSampleFileURLInput.fill(
				'http://www.liferay.com/sample'
			);
			await commerceAdminProductPage.productVirtualSaveButton.click();

			await waitForAlert(page);

			await page.reload();

			await expect(
				commerceAdminProductPage.productVirtualSampleFileEntryNameInput
			).not.toContainText(sampleFileName);
			await expect(
				commerceAdminProductPage.productVirtualSampleFileURLInput
			).toHaveValue('http://www.liferay.com/sample');
		});
	}
);
