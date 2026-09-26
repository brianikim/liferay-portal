/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// @ts-ignore

import {expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {customFieldsPagesTest} from '../../../../fixtures/customFieldsPagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {globalMenuPagesTest} from '../../../../fixtures/globalMenuPagesTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {TCustomField} from '../../../../helpers/CustomFieldTypesHelper';
import getRandomString from '../../../../utils/getRandomString';
import {waitForAlert} from '../../../../utils/waitForAlert';

export const test = mergeTests(
	commercePagesTest,
	customFieldsPagesTest,
	dataApiHelpersTest,
	globalMenuPagesTest,
	loginTest()
);

test('LPD-26249 Configure options and Product options', async ({
	apiHelpers,
	commerceAdminProductDetailsPage,
	commerceAdminProductDetailsProductOptionsPage,
	commerceAdminProductPage,
	page,
}) => {
	await page.goto('/');

	const option1 = await apiHelpers.headlessCommerceAdminCatalog.postOption(
		'select',
		'color',
		'Color',
		1
	);

	const option2 = await apiHelpers.headlessCommerceAdminCatalog.postOption(
		'select',
		'size',
		'Size',
		2
	);

	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: {en_US: 'Simple T-Shirt'},
		productOptions: [
			{
				fieldType: 'select',
				key: 'color',
				name: {
					en_US: 'Color',
				},
				optionId: option1.id,
				priority: 1,
				productOptionValues: [
					{
						key: 'black',
						name: {
							en_US: 'Black',
						},
						priority: 1,
					},
					{
						key: 'white',
						name: {
							en_US: 'White',
						},
						priority: 2,
					},
				],
				skuContributor: true,
			},
			{
				fieldType: 'select',
				key: 'size',
				name: {
					en_US: 'Size',
				},
				optionId: option2.id,
				priority: 2,
				productOptionValues: [
					{
						key: 'xs',
						name: {
							en_US: 'XS',
						},
						priority: 1,
					},
					{
						key: 'xl',
						name: {
							en_US: 'XL',
						},
						priority: 2,
					},
				],
				skuContributor: true,
			},
		],
	});

	await commerceAdminProductPage.gotoProduct(product.name['en_US']);

	await commerceAdminProductDetailsPage.goToProductOptions();

	await expect(
		(
			await commerceAdminProductDetailsProductOptionsPage.tableRow(
				0,
				option1.name['en_US'],
				true
			)
		).row
	).toBeVisible();
});

test(
	'LPD-45740 Product options can be added from product admins',
	{tag: ['@COMMERCE-6017', '@COMMERCE-6269', '@LPD-106244-Grouped-16']},
	async ({
		apiHelpers,
		commerceAdminOptionsPage,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsProductOptionsPage,
		commerceAdminProductPage,
		globalMenuPage,
		page,
	}) => {
		await page.goto('/');

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: 'Simple T-Shirt'},
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductOptions();

		const optionNames = [];

		try {
			for (const fieldType of [
				'Text',
				'Single Selection',
				'Multiple Selection',
				'Date',
				'Numeric',
				'Boolean',
			]) {
				const optionName = getRandomString();

				optionNames.push(optionName);

				await commerceAdminProductDetailsProductOptionsPage.addOptionsSearch.fill(
					optionName
				);

				await commerceAdminProductDetailsProductOptionsPage.createNewOptionsButton.click();

				await waitForAlert(page, 'Success:Option Created');

				await page.reload();

				const optionRow = (
					await commerceAdminProductDetailsProductOptionsPage.tableRow(
						0,
						optionName,
						true
					)
				).row;

				await expect(optionRow).toBeVisible();
				await expect(optionRow).toContainText('Select from List');

				await commerceAdminProductDetailsProductOptionsPage.openOption(
					optionName
				);

				await commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame
					.getByLabel('Field Type')
					.selectOption({label: fieldType});
				await commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame
					.getByRole('button', {exact: true, name: 'Save'})
					.click();

				await waitForAlert(
					commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame
				);

				await commerceAdminProductDetailsProductOptionsPage.closeOption();

				await page.reload();

				await expect(
					(
						await commerceAdminProductDetailsProductOptionsPage.tableRow(
							0,
							optionName,
							true
						)
					).row
				).toContainText(fieldType);
			}

			await globalMenuPage.goToCommerce('Options');

			for (const optionName of optionNames) {
				await expect(
					commerceAdminOptionsPage.optionLink(optionName)
				).toBeVisible();
			}
		}
		finally {
			const options =
				await apiHelpers.headlessCommerceAdminCatalog.getOptions();

			for (const option of options?.items ?? []) {
				if (optionNames.includes(option.name['en_US'])) {
					apiHelpers.data.push({id: option.id, type: 'option'});
				}
			}
		}
	}
);

test(
	'Product options can be deleted from product admins',
	{tag: ['@LPD-45740']},
	async ({
		apiHelpers,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsProductOptionsPage,
		commerceAdminProductPage,
		page,
	}) => {
		await page.goto('/');

		const option = await apiHelpers.headlessCommerceAdminCatalog.postOption(
			'select',
			'color',
			'Color',
			1
		);

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: 'Simple T-Shirt'},
				productOptions: [
					{
						fieldType: 'select',
						key: 'color',
						name: {
							en_US: 'Color',
						},
						optionId: option.id,
						priority: 1,
						productOptionValues: [
							{
								key: 'black',
								name: {
									en_US: 'Black',
								},
								priority: 1,
							},
							{
								key: 'white',
								name: {
									en_US: 'White',
								},
								priority: 2,
							},
						],
						skuContributor: true,
					},
				],
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductOptions();

		await expect(
			(
				await commerceAdminProductDetailsProductOptionsPage.tableRow(
					0,
					option.name['en_US'],
					true
				)
			).row
		).toBeVisible();

		await commerceAdminProductDetailsProductOptionsPage
			.optionActionsButton(option.name['en_US'])
			.click();

		await commerceAdminProductDetailsProductOptionsPage.deleteMenuItem.click();

		await waitForAlert(page);
	}
);

test(
	'Create an option from the standalone options portlet',
	{tag: '@LPD-106244-Grouped-3'},
	async ({apiHelpers, commerceAdminOptionsPage, globalMenuPage}) => {
		const optionName = 'Color-' + getRandomString();
		const optionKey = 'color-' + getRandomString();

		try {
			await globalMenuPage.goToCommerce('Options');

			await commerceAdminOptionsPage.createOption(
				optionName,
				'Text',
				optionKey
			);

			await expect(commerceAdminOptionsPage.optionNameInput).toHaveValue(
				optionName
			);
		}
		finally {
			const options =
				await apiHelpers.headlessCommerceAdminCatalog.getOptions();

			for (const option of options?.items ?? []) {
				if (option.key === optionKey) {
					apiHelpers.data.push({id: option.id, type: 'option'});
				}
			}
		}
	}
);

test(
	'Remove an option from the standalone options portlet',
	{tag: ['@COMMERCE-6280', '@LPD-106244-Grouped-3']},
	async ({apiHelpers, commerceAdminOptionsPage, globalMenuPage}) => {
		const optionName = 'Color-' + getRandomString();
		const optionKey = 'color-' + getRandomString();

		try {
			await globalMenuPage.goToCommerce('Options');

			await commerceAdminOptionsPage.createOption(
				optionName,
				'Select from List',
				optionKey
			);

			await globalMenuPage.goToCommerce('Options');

			await commerceAdminOptionsPage.deleteOption(optionName);

			await expect(
				commerceAdminOptionsPage.optionLink(optionName)
			).toBeHidden();
		}
		finally {
			const options =
				await apiHelpers.headlessCommerceAdminCatalog.getOptions();

			for (const option of options?.items ?? []) {
				if (option.key === optionKey) {
					apiHelpers.data.push({id: option.id, type: 'option'});
				}
			}
		}
	}
);

test(
	'Custom fields can be returned from the option value API',
	{tag: ['@COMMERCE-11715', '@LPD-106244-Grouped-3']},
	async ({
		addCustomFieldPage,
		apiHelpers,
		commerceAdminOptionsPage,
		globalMenuPage,
		viewAttributesPage,
	}) => {
		const customFieldName = 'CF-' + getRandomString();
		const customFieldValue = 'Test Custom Value';
		const optionValueName = 'value1-' + getRandomString();

		const customField: TCustomField = {
			fieldName: customFieldName,
			fieldType: 'inputField',
			resource: 'Product Option Value',
		};

		const option = await apiHelpers.headlessCommerceAdminCatalog.postOption(
			'select',
			'option-' + getRandomString(),
			'Option-' + getRandomString(),
			1
		);

		const optionValue =
			await apiHelpers.headlessCommerceAdminCatalog.postOptionValue(
				option.id,
				'value1-' + getRandomString(),
				optionValueName,
				1
			);

		try {
			await addCustomFieldPage.addCustomField(customField);

			await globalMenuPage.goToCommerce('Options');

			await commerceAdminOptionsPage
				.optionLink(option.name.en_US)
				.click();

			await commerceAdminOptionsPage.setOptionValueCustomField(
				optionValueName,
				customFieldName,
				customFieldValue
			);

			const optionValues =
				await apiHelpers.headlessCommerceAdminCatalog.getOptionValues(
					option.id
				);

			const returnedOptionValue = optionValues.items.find(
				(item: {id: number}) => item.id === optionValue.id
			);

			expect(JSON.stringify(returnedOptionValue.customFields)).toContain(
				customFieldValue
			);
		}
		finally {
			await viewAttributesPage.deleteCustomField(
				customFieldName,
				'Product Option Value'
			);
		}
	}
);
