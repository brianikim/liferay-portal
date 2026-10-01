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

test('LPD-45740 Product options can be added from product admins', async ({
	apiHelpers,
	commerceAdminProductDetailsPage,
	commerceAdminProductDetailsProductOptionsPage,
	commerceAdminProductPage,
	page,
}) => {
	await page.goto('/');

	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: {en_US: 'Simple T-Shirt'},
	});

	await commerceAdminProductPage.gotoProduct(product.name['en_US']);

	await commerceAdminProductDetailsPage.goToProductOptions();

	const optionName = getRandomString();

	await commerceAdminProductDetailsProductOptionsPage.addOptionsSearch.fill(
		optionName
	);

	await commerceAdminProductDetailsProductOptionsPage.createNewOptionsButton.click();

	await waitForAlert(page, 'Success:Option Created');

	await page.reload();

	await expect(
		(
			await commerceAdminProductDetailsProductOptionsPage.tableRow(
				0,
				optionName,
				true
			)
		).row
	).toBeVisible();
});

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
