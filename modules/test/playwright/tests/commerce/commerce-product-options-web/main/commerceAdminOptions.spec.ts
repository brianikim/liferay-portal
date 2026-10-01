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
	'Product options can be added, edited and deleted from product admins',
	{
		tag: [
			'@COMMERCE-6017',
			'@COMMERCE-6019',
			'@COMMERCE-6269',
			'@LPD-45740',
			'@LPD-106244-Grouped-3',
		],
	},
	async ({
		apiHelpers,
		commerceAdminOptionsPage,
		commerceAdminProductDetailsPage,
		commerceAdminProductDetailsProductOptionsPage,
		commerceAdminProductPage,
		globalMenuPage,
		page,
	}) => {
		const optionName = getRandomString();

		const option = await apiHelpers.headlessCommerceAdminCatalog.postOption(
			'select',
			'color-' + getRandomString().toLowerCase(),
			optionName,
			1,
			true
		);

		const catalog =
			await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

		const product =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: getRandomString()},
				productOptions: [
					{
						facetable: true,
						fieldType: 'select',
						key: option.key,
						name: {en_US: optionName},
						optionId: option.id,
						priority: 1,
						productOptionValues: [
							{
								key: 'black',
								name: {en_US: 'Black'},
								priority: 1,
							},
						],
						required: true,
						skuContributor: true,
					},
				],
			});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']);

		await commerceAdminProductDetailsPage.goToProductOptions();

		await test.step('Edit and delete an option value', async () => {
			await commerceAdminProductDetailsProductOptionsPage.openOption(
				optionName
			);

			await commerceAdminProductDetailsProductOptionsPage
				.optionValueLink('Black')
				.click();

			await commerceAdminProductDetailsProductOptionsPage.optionValueNameInput.fill(
				'Red'
			);
			await commerceAdminProductDetailsProductOptionsPage.optionValueKeyInput.fill(
				'red'
			);
			await commerceAdminProductDetailsProductOptionsPage.optionValueSaveButton.click();

			await waitForAlert(
				commerceAdminProductDetailsProductOptionsPage.optionValueSidePanelFrame
			);

			await commerceAdminProductDetailsProductOptionsPage.closeOptionValue();
			await commerceAdminProductDetailsProductOptionsPage.closeOption();

			await page.reload();

			await commerceAdminProductDetailsProductOptionsPage.openOption(
				optionName
			);

			await expect(
				commerceAdminProductDetailsProductOptionsPage.optionValueLink(
					'Red'
				)
			).toBeVisible();

			await commerceAdminProductDetailsProductOptionsPage.clickOptionValueAction(
				'Delete',
				'Red'
			);

			await waitForAlert(
				commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame
			);

			await page.reload();

			await commerceAdminProductDetailsProductOptionsPage.openOption(
				optionName
			);

			await expect(
				commerceAdminProductDetailsProductOptionsPage.optionValueRow(
					'Red'
				)
			).toHaveCount(0);

			await commerceAdminProductDetailsProductOptionsPage.closeOption();
		});

		await test.step('Delete a product option', async () => {
			await commerceAdminProductDetailsProductOptionsPage
				.optionActionsButton(optionName)
				.click();
			await commerceAdminProductDetailsProductOptionsPage.deleteMenuItem.click();

			await waitForAlert(page);

			await page.reload();

			await expect(
				commerceAdminProductDetailsProductOptionsPage.optionLink(
					optionName
				)
			).toHaveCount(0);
		});

		const fieldTypeOptionNames: string[] = [];

		try {
			await test.step('Add a product option of every field type', async () => {
				for (const fieldType of [
					'Text',
					'Single Selection',
					'Multiple Selection',
					'Date',
					'Numeric',
					'Boolean',
				]) {
					const fieldTypeOptionName = getRandomString();

					fieldTypeOptionNames.push(fieldTypeOptionName);

					await commerceAdminProductDetailsProductOptionsPage.addOptionsSearch.fill(
						fieldTypeOptionName
					);
					await commerceAdminProductDetailsProductOptionsPage.createNewOptionsButton.click();

					await waitForAlert(page, 'Success:Option Created');

					await page.reload();

					const optionRow = (
						await commerceAdminProductDetailsProductOptionsPage.tableRow(
							0,
							fieldTypeOptionName,
							true
						)
					).row;

					await expect(optionRow).toBeVisible();
					await expect(optionRow).toContainText('Select from List');

					await commerceAdminProductDetailsProductOptionsPage.openOption(
						fieldTypeOptionName
					);

					await commerceAdminProductDetailsProductOptionsPage.optionFieldTypeSelect.selectOption(
						{label: fieldType}
					);
					await commerceAdminProductDetailsProductOptionsPage.optionSaveButton.click();

					await waitForAlert(
						commerceAdminProductDetailsProductOptionsPage.optionSidePanelFrame
					);

					await commerceAdminProductDetailsProductOptionsPage.closeOption();

					await page.reload();

					await expect(
						(
							await commerceAdminProductDetailsProductOptionsPage.tableRow(
								0,
								fieldTypeOptionName,
								true
							)
						).row
					).toContainText(fieldType);
				}
			});

			await test.step('Find the added options in the options portlet', async () => {
				await globalMenuPage.goToCommerce('Options');

				for (const fieldTypeOptionName of fieldTypeOptionNames) {
					await commerceAdminOptionsPage.search(fieldTypeOptionName);

					await expect(
						commerceAdminOptionsPage.optionLink(fieldTypeOptionName)
					).toBeVisible();
				}
			});
		}
		finally {
			for (const fieldTypeOptionName of fieldTypeOptionNames) {
				const fieldTypeOption = (
					await apiHelpers.headlessCommerceAdminCatalog.getOptions(
						new URLSearchParams({search: fieldTypeOptionName})
					)
				).items.find(
					(item: {name: {en_US: string}}) =>
						item.name['en_US'] === fieldTypeOptionName
				);

				if (fieldTypeOption) {
					apiHelpers.data.push({
						id: fieldTypeOption.id,
						type: 'option',
					});
				}
			}
		}
	}
);

test(
	'Create, reopen and remove an option from the standalone options portlet',
	{tag: ['@COMMERCE-6280', '@LPD-106244-Grouped-3']},
	async ({apiHelpers, commerceAdminOptionsPage, globalMenuPage}) => {
		const optionKey = 'color-' + getRandomString().toLowerCase();
		const optionName = 'Color-' + getRandomString();

		const optionSearchParams = new URLSearchParams({
			filter: `key eq '${optionKey}'`,
		});

		try {
			await globalMenuPage.goToCommerce('Options');

			await commerceAdminOptionsPage.createOption(
				'Select from List',
				optionKey,
				optionName
			);

			await globalMenuPage.goToCommerce('Options');

			await commerceAdminOptionsPage.search(optionName);
			await commerceAdminOptionsPage.optionLink(optionName).click();

			await expect(commerceAdminOptionsPage.nameInput).toHaveValue(
				optionName
			);
			await expect(commerceAdminOptionsPage.fieldTypeSelect).toHaveValue(
				'select'
			);
			await expect(commerceAdminOptionsPage.keyInput).toHaveValue(
				optionKey
			);

			const option = (
				await apiHelpers.headlessCommerceAdminCatalog.getOptions(
					optionSearchParams
				)
			).items[0];

			await apiHelpers.headlessCommerceAdminCatalog.postOptionValue(
				option.id,
				'black',
				'Black',
				0
			);

			await globalMenuPage.goToCommerce('Options');

			await commerceAdminOptionsPage.search(optionName);
			await commerceAdminOptionsPage.deleteOption(optionName);

			await expect(
				commerceAdminOptionsPage.optionLink(optionName)
			).toHaveCount(0);
			expect(
				(
					await apiHelpers.headlessCommerceAdminCatalog.getOptions(
						optionSearchParams
					)
				).items
			).toHaveLength(0);
		}
		finally {
			const option = (
				await apiHelpers.headlessCommerceAdminCatalog.getOptions(
					optionSearchParams
				)
			).items[0];

			if (option) {
				apiHelpers.data.push({id: option.id, type: 'option'});
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
		const option = await apiHelpers.headlessCommerceAdminCatalog.postOption(
			'select',
			'option-' + getRandomString().toLowerCase(),
			'Option-' + getRandomString(),
			1
		);

		const optionValue =
			await apiHelpers.headlessCommerceAdminCatalog.postOptionValue(
				option.id,
				'value-' + getRandomString().toLowerCase(),
				'Value-' + getRandomString(),
				1
			);

		const customField: TCustomField = {
			fieldName: 'CF-' + getRandomString(),
			fieldType: 'inputField',
			resource: 'Product Option Value',
		};

		await addCustomFieldPage.addCustomField(customField);

		try {
			await globalMenuPage.goToCommerce('Options');

			await commerceAdminOptionsPage.search(option.name['en_US']);
			await commerceAdminOptionsPage
				.optionLink(option.name['en_US'])
				.click();

			const customFieldValue = getRandomString();

			await commerceAdminOptionsPage.setOptionValueCustomField(
				optionValue.name['en_US'],
				customField.fieldName,
				customFieldValue
			);

			const {customFields} =
				await apiHelpers.headlessCommerceAdminCatalog.getOptionValue(
					optionValue.id
				);

			expect(
				customFields.find(
					({name}: {name: string}) => name === customField.fieldName
				).customValue.data
			).toBe(customFieldValue);
		}
		finally {
			await viewAttributesPage.deleteCustomField(
				customField.fieldName,
				customField.resource
			);
		}
	}
);
