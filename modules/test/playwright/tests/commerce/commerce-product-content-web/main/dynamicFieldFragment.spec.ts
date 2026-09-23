/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {displayPageTemplatesPagesTest} from '../../../../fixtures/displayPageTemplatesPagesTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {pageEditorPagesTest} from '../../../../fixtures/pageEditorPagesTest';
import {
	performLoginViaApi,
	performLogout,
} from '../../../../utils/performLogin';
import {createAccountWithBuyerUser, miniumSetUp} from '../../utils/commerce';

export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	displayPageTemplatesPagesTest,
	loginTest(),
	pageEditorPagesTest
);

test(
	'Dynamic Field fragments show the mapped SKU fields in the configured elements and follow the selected option',
	{tag: ['@COMMERCE-11074', '@COMMERCE-11075']},
	async ({
		apiHelpers,
		displayPageTemplatesPage,
		page,
		pageEditorPage,
		productDetailsPage,
	}) => {
		test.setTimeout(180000);

		const {site} = await miniumSetUp(apiHelpers);

		for (const [index, skuName] of [
			'MIN55861',
			'MIN93022A',
			'MIN93022C',
		].entries()) {
			const sku =
				await apiHelpers.headlessCommerceAdminCatalog.getSkuByName(
					skuName
				);

			await apiHelpers.headlessCommerceAdminCatalog.patchSku(sku.id, {
				gtin: `GTIN000${index + 1}`,
				manufacturerPartNumber: `MPN000${index + 1}`,
				sku: sku.sku,
			});
		}

		const dynamicFields = [
			{
				field: 'availability.stockQuantity',
				label: 'Inventory',
				labelElementType: 'div',
				valueElementType: 'h1',
			},
			{
				field: 'sku',
				label: 'SKU',
				labelElementType: 'h2',
				valueElementType: 'h3',
			},
			{
				field: 'gtin',
				label: 'GTIN',
				labelElementType: 'h4',
				valueElementType: 'h5',
			},
			{
				field: 'manufacturerPartNumber',
				label: 'Manufacturer Part Number',
				labelElementType: 'h6',
				valueElementType: 'p',
			},
		];

		await displayPageTemplatesPage.addDefaultTemplate(
			'com.liferay.commerce.product.model.CPDefinition',
			site
		);

		for (let index = 0; index < dynamicFields.length; index++) {
			await pageEditorPage.addFragment('Product', 'Dynamic Field');
		}

		await pageEditorPage.addFragment('Product', 'Option Selector');

		for (const [index, dynamicField] of dynamicFields.entries()) {
			const fragmentId = await pageEditorPage.getFragmentId(
				'Dynamic Field',
				index
			);

			for (const [fieldLabel, value] of [
				['Field', dynamicField.field],
				['Label', dynamicField.label],
				['Label Element Type', dynamicField.labelElementType],
				['Value Element Type', dynamicField.valueElementType],
			]) {
				await pageEditorPage.changeFragmentConfiguration({
					fieldLabel,
					fragmentId,
					tab: 'General',
					value,
				});
			}
		}

		await pageEditorPage.waitForChangesSaved();

		await displayPageTemplatesPage.publishTemplate();

		const {buyerUser} = await createAccountWithBuyerUser(
			apiHelpers,
			site.id
		);

		await performLogout(page);
		await performLoginViaApi({page, screenName: buyerUser.alternateName});

		const dynamicFieldLocator = (label: string) =>
			page
				.locator('[class$="dynamic-field"]')
				.filter({hasText: `${label}:`});

		const assertDynamicFieldValues = async (values: string[]) => {
			for (const [index, {label}] of dynamicFields.entries()) {
				await expect(
					dynamicFieldLocator(label).locator('.node-value')
				).toHaveText(values[index]);
			}
		};

		await page.goto(`/web${site.friendlyUrlPath}/p/u-joint`);

		await assertDynamicFieldValues([
			'120',
			'MIN55861',
			'GTIN0001',
			'MPN0001',
		]);

		for (const {
			label,
			labelElementType,
			valueElementType,
		} of dynamicFields) {
			await expect(
				dynamicFieldLocator(label).locator(
					`${labelElementType}.node-label`
				)
			).toBeVisible();
			await expect(
				dynamicFieldLocator(label).locator(
					`${valueElementType}.node-value`
				)
			).toBeVisible();
		}

		await page.goto(`/web${site.friendlyUrlPath}/p/hoses`);

		await assertDynamicFieldValues([
			'120',
			'MIN93022A',
			'GTIN0002',
			'MPN0002',
		]);

		await productDetailsPage.selectOption('48', 'Package Quantity');

		await assertDynamicFieldValues([
			'240',
			'MIN93022C',
			'GTIN0003',
			'MPN0003',
		]);
	}
);
