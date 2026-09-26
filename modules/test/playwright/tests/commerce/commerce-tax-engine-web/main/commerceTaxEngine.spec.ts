/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {taxCategoriesPageTest} from '../../../../fixtures/taxCategoriesPageTest';
import {liferayConfig} from '../../../../liferay.config';
import {CommerceAdminChannelDetailsPage} from '../../../../pages/commerce/commerce-channel-web/commerceAdminChannelDetailsPage';
import {getRandomInt} from '../../../../utils/getRandomInt';
import {waitForAlert} from '../../../../utils/waitForAlert';

export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	taxCategoriesPageTest,
	loginTest()
);

async function openRemoteConfiguration(
	commerceAdminChannelDetailsPage: CommerceAdminChannelDetailsPage
) {
	await (
		await commerceAdminChannelDetailsPage.generalCommerceAdminChannelTableLink(
			'Remote'
		)
	).click();
	await (
		await commerceAdminChannelDetailsPage.sidePanelFrameNavLink(
			'Configuration',
			'Tax Calculations'
		)
	).click();
}

async function verifyFixedTaxRate(
	commerceAdminChannelDetailsPage,
	name: string,
	taxAmount: string
) {
	const tableName = 'Tax Calculations';

	await (
		await commerceAdminChannelDetailsPage.generalCommerceAdminChannelTableLink(
			'Fixed Tax Rate'
		)
	).click();
	await (
		await commerceAdminChannelDetailsPage.taxRatesTab(tableName)
	).click();

	await expect(
		(
			await commerceAdminChannelDetailsPage.sidePanelFrame(tableName)
		).getByRole('cell', {exact: true, name})
	).toBeVisible();
	await expect(
		(
			await commerceAdminChannelDetailsPage.getRowByTextFromSidePanelTable(
				tableName,
				name
			)
		).locator('.cell-rate')
	).toHaveText(taxAmount);
}

async function verifyByAddressTaxRate(
	commerceAdminChannelDetailsPage,
	country: string,
	name: string,
	region: string,
	taxAmount: string,
	zip: string
) {
	const tableName = 'Tax Calculations';

	await (
		await commerceAdminChannelDetailsPage.generalCommerceAdminChannelTableLink(
			'By Address'
		)
	).click();
	await (
		await commerceAdminChannelDetailsPage.taxRateSettingsTab(tableName)
	).click();

	const sidePanelFrame =
		await commerceAdminChannelDetailsPage.sidePanelFrame(tableName);

	await expect(
		sidePanelFrame.getByRole('cell', {exact: true, name: country})
	).toBeVisible();

	await expect(
		sidePanelFrame.getByRole('cell', {exact: true, name})
	).toBeVisible();

	await expect(
		sidePanelFrame.getByRole('cell', {exact: true, name: region})
	).toBeVisible();
	await expect(
		(
			await commerceAdminChannelDetailsPage.getRowByTextFromSidePanelTable(
				tableName,
				name
			)
		).locator('.cell-rate')
	).toHaveText(taxAmount);
	await expect(sidePanelFrame.getByText(zip)).toBeVisible();
}

test(
	'LPD-31663 Activate Fixed Tax Engine and Add Tax Rate',
	{tag: ['@COMMERCE-6124', '@LPD-106244-Grouped-7']},
	async ({
		apiHelpers,
		commerceAdminChannelDetailsPage,
		commerceAdminChannelsPage,
		newTaxCategoryPage,
		page,
		taxCategoriesPage,
	}) => {
		const taxCategoriesExternalReferenceCode =
			'Test Reference ' + getRandomInt();

		const taxCategories = [
			{
				description: 'Test Description ' + getRandomInt(),
				name: 'Test ' + getRandomInt(),
				taxCategoriesExternalReferenceCode,
			},
			{
				description: 'Test Description ' + getRandomInt(),
				name: 'Test ' + getRandomInt(),
				taxCategoriesExternalReferenceCode,
			},
		];

		try {
			await test.step('Create two tax categories and verify unique external reference code', async () => {
				await taxCategoriesPage.goto();

				for (const taxCategory of taxCategories) {
					await taxCategoriesPage.newButton.click();
					await newTaxCategoryPage.externalReferenceCodeInput.fill(
						taxCategory.taxCategoriesExternalReferenceCode
					);
					await newTaxCategoryPage.nameInput.fill(taxCategory.name);
					await newTaxCategoryPage.descriptionInput.fill(
						taxCategory.description
					);
					await newTaxCategoryPage.saveButton.click();

					if (taxCategory.name === taxCategories[0].name) {
						await waitForAlert(page);

						await expect(taxCategoriesPage.newButton).toBeVisible();
					}
					else {
						await waitForAlert(
							page,
							'Error:Please enter a unique external reference code.',
							{autoClose: false, type: 'danger'}
						);

						await newTaxCategoryPage.externalReferenceCodeInput.fill(
							'Test Reference' + getRandomInt()
						);
						await newTaxCategoryPage.saveButton.click();

						await waitForAlert(page);
					}
				}
			});

			await test.step('Create a commerce channel via API and navigate to channel details page', async () => {
				const site =
					await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath(
						'guest'
					);

				const channel =
					await apiHelpers.headlessCommerceAdminChannel.postChannel({
						siteGroupId: site.id,
					});

				await commerceAdminChannelsPage.goto();

				await (
					await commerceAdminChannelsPage.channelsTableRowLink(
						channel.name
					)
				).click();
			});

			await test.step('Add Fixed Tax Rate and assert values', async () => {
				await commerceAdminChannelDetailsPage.addFixedTaxRate(
					'7.5',
					taxCategories[0].name
				);
				await verifyFixedTaxRate(
					commerceAdminChannelDetailsPage,
					taxCategories[0].name,
					'$ 7.50'
				);
				await commerceAdminChannelDetailsPage.editFixedTaxRate(
					'10.0',
					taxCategories[0].name
				);
				await verifyFixedTaxRate(
					commerceAdminChannelDetailsPage,
					taxCategories[0].name,
					'$ 10.00'
				);
			});

			await test.step('Delete the Fixed Tax Rate rate and assert it is removed', async () => {
				await commerceAdminChannelDetailsPage.deleteTaxRate(
					'Fixed Tax Rate',
					taxCategories[0].name
				);

				await expect(
					(
						await commerceAdminChannelDetailsPage.sidePanelFrame(
							'Tax Calculations'
						)
					).getByRole('cell', {
						exact: true,
						name: taxCategories[0].name,
					})
				).toBeHidden();
			});
		}
		finally {
			await page.reload();

			page.on('dialog', (dialog) => {
				dialog.accept();
			});

			await taxCategoriesPage.goto();

			for (const taxCategory of taxCategories) {
				await expect(async () => {
					await (
						await taxCategoriesPage.taxCategoriesTableRowActions(
							taxCategory.name
						)
					).click();

					await expect(
						taxCategoriesPage.deleteMenuItem
					).toBeVisible();
				}).toPass();
				await taxCategoriesPage.deleteMenuItem.click();

				await waitForAlert(page);
			}
		}
	}
);

test(
	'LPD-31663 Activate By Address Tax Engine and Add Tax Rate',
	{tag: ['@COMMERCE-6121', '@LPD-106244-Grouped-7']},
	async ({
		apiHelpers,
		commerceAdminChannelDetailsPage,
		commerceAdminChannelsPage,
		newTaxCategoryPage,
		page,
		taxCategoriesPage,
	}) => {
		const taxCategories = [
			{
				description: 'Test Description ' + getRandomInt(),
				name: 'Test ' + getRandomInt(),
				taxCategoriesExternalReferenceCode:
					'Test Reference ' + getRandomInt(),
			},
			{
				description: 'Test Description ' + getRandomInt(),
				name: 'Test ' + getRandomInt(),
				taxCategoriesExternalReferenceCode:
					'Test Reference ' + getRandomInt(),
			},
		];

		try {
			await test.step('Create two tax categories', async () => {
				await taxCategoriesPage.goto();

				for (const taxCategory of taxCategories) {
					await taxCategoriesPage.newButton.click();
					await newTaxCategoryPage.externalReferenceCodeInput.fill(
						taxCategory.taxCategoriesExternalReferenceCode
					);
					await newTaxCategoryPage.nameInput.fill(taxCategory.name);
					await newTaxCategoryPage.descriptionInput.fill(
						taxCategory.description
					);
					await newTaxCategoryPage.saveButton.click();

					await waitForAlert(page);

					await expect(taxCategoriesPage.newButton).toBeVisible();
				}
			});

			await test.step('Create a commerce channel via API and navigate to channel details page', async () => {
				const site =
					await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath(
						'guest'
					);

				const channel =
					await apiHelpers.headlessCommerceAdminChannel.postChannel({
						siteGroupId: site.id,
					});

				await commerceAdminChannelsPage.goto();

				await (
					await commerceAdminChannelsPage.channelsTableRowLink(
						channel.name
					)
				).click();
			});

			await test.step('Add By Address Tax Rate and assert values', async () => {
				await commerceAdminChannelDetailsPage.addByAddressTaxRate(
					'7.5',
					'Italy',
					taxCategories[0].name,
					'Roma',
					'12345'
				);
				await verifyByAddressTaxRate(
					commerceAdminChannelDetailsPage,
					'Italy',
					taxCategories[0].name,
					'Roma',
					'$ 7.50',
					'12345'
				);
				await commerceAdminChannelDetailsPage.editByAddressTaxRate(
					'10.0',
					taxCategories[0].name
				);
				await verifyByAddressTaxRate(
					commerceAdminChannelDetailsPage,
					'Italy',
					taxCategories[0].name,
					'Roma',
					'$ 10.00',
					'12345'
				);
			});

			await test.step('Delete the By Address rate and assert it is removed', async () => {
				await commerceAdminChannelDetailsPage.deleteTaxRate(
					'By Address',
					taxCategories[0].name
				);

				await expect(
					(
						await commerceAdminChannelDetailsPage.sidePanelFrame(
							'Tax Calculations'
						)
					).getByRole('cell', {
						exact: true,
						name: taxCategories[0].name,
					})
				).toBeHidden();
			});
		}
		finally {
			await page.reload();

			page.on('dialog', (dialog) => {
				dialog.accept();
			});

			await taxCategoriesPage.goto();

			for (const taxCategory of taxCategories) {
				await expect(async () => {
					await (
						await taxCategoriesPage.taxCategoriesTableRowActions(
							taxCategory.name
						)
					).click();

					await expect(
						taxCategoriesPage.deleteMenuItem
					).toBeVisible();
				}).toPass();
				await taxCategoriesPage.deleteMenuItem.click();

				await waitForAlert(page);
			}
		}
	}
);

test(
	'Tax engines can be reactivated and the Remote engine can be configured',
	{tag: ['@COMMERCE-6118', '@COMMERCE-6122', '@LPD-106244-Grouped-7']},
	async ({
		apiHelpers,
		commerceAdminChannelDetailsPage,
		commerceAdminChannelsPage,
		page,
	}) => {
		const site =
			await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath(
				'guest'
			);

		const channel =
			await apiHelpers.headlessCommerceAdminChannel.postChannel({
				siteGroupId: site.id,
			});

		await commerceAdminChannelsPage.goto();

		await (
			await commerceAdminChannelsPage.channelsTableRowLink(channel.name)
		).click();

		const taxEngines = [
			'Avalara',
			'By Address',
			'Fixed Tax Rate',
			'Remote',
		];

		for (const taxEngine of taxEngines) {
			await commerceAdminChannelDetailsPage.deactivateChannelConfiguration(
				taxEngine,
				'Tax Calculations'
			);
			await commerceAdminChannelDetailsPage.activateChannelConfiguration(
				taxEngine,
				'Tax Calculations'
			);
		}

		await page.reload();

		for (const taxEngine of taxEngines) {
			await expect(
				page
					.getByRole('row')
					.filter({
						has: page.getByRole('link', {
							exact: true,
							name: taxEngine,
						}),
					})
					.locator('.label-success')
			).toBeVisible();
		}

		const remoteConfiguration = [
			{
				label: 'Tax Value Endpoint URL',
				value: 'http://localhost:8080/web/test',
			},
			{label: 'Tax Value Endpoint Authorization Token', value: 'Test'},
		];

		await openRemoteConfiguration(commerceAdminChannelDetailsPage);

		for (const {label, value} of remoteConfiguration) {
			await (
				await commerceAdminChannelDetailsPage.sidePanelFrameInput(
					label,
					'Tax Calculations'
				)
			).fill(value);
		}

		await (
			await commerceAdminChannelDetailsPage.frameSaveButton(
				false,
				'Tax Calculations'
			)
		).click();

		await waitForAlert(
			await commerceAdminChannelDetailsPage.sidePanelFrame(
				'Tax Calculations'
			)
		);

		await page.reload();

		await openRemoteConfiguration(commerceAdminChannelDetailsPage);

		for (const {label, value} of remoteConfiguration) {
			await expect(
				await commerceAdminChannelDetailsPage.sidePanelFrameInput(
					label,
					'Tax Calculations'
				)
			).toHaveValue(value);
		}
	}
);

test("COMMERCE-7000 No XSS is present when the user updates a tax categories entry's details URL", async ({
	apiHelpers,
	newTaxCategoryPage,
	page,
	taxCategoriesPage,
}) => {
	try {
		await test.step('Create a tax category', async () => {
			await taxCategoriesPage.goto();

			await taxCategoriesPage.newButton.click();
			await newTaxCategoryPage.externalReferenceCodeInput.fill(
				'Test Reference 1'
			);
			await newTaxCategoryPage.nameInput.fill('Test 1');
			await newTaxCategoryPage.descriptionInput.fill(
				'Test Description 1'
			);
			await newTaxCategoryPage.saveButton.click();

			await waitForAlert(page);

			await expect(taxCategoriesPage.newButton).toBeVisible();
		});

		await test.step("Modify the 'redirect' URL parameter and assert that no alert is visible", async () => {
			const taxCategory = (
				await apiHelpers.headlessCommerceAdminChannel.getTaxCategories()
			).items[0];

			const newUrl = `${liferayConfig.environment.baseUrl}/group/guest/~/control_panel/manage?p_p_id=com_liferay_commerce_product_tax_category_web_internal_portlet_CPTaxCategoryPortlet&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&_com_liferay_commerce_product_tax_category_web_internal_portlet_CPTaxCategoryPortlet_mvcRenderCommandName=%2Fcp_tax_category%2Fedit_cp_tax_category&_com_liferay_commerce_product_tax_category_web_internal_portlet_CPTaxCategoryPortlet_redirect=%22%3E%3Cscript%3Ealert(111)%3C/script%3E&_com_liferay_commerce_product_tax_category_web_internal_portlet_CPTaxCategoryPortlet_cpTaxCategoryId=${taxCategory.id}`;

			let alertTriggered = false;

			page.on('dialog', async (dialog) => {
				if (dialog.type() === 'alert') {
					alertTriggered = true;
					await dialog.dismiss();
				}
			});

			await page.goto(newUrl);

			expect(alertTriggered).toBe(false);
		});
	}
	finally {
		page.on('dialog', (dialog) => {
			dialog.accept();
		});

		await taxCategoriesPage.goto();

		await expect(async () => {
			await (
				await taxCategoriesPage.taxCategoriesTableRowActions('Test 1')
			).click();

			await expect(taxCategoriesPage.deleteMenuItem).toBeVisible({
				timeout: 500,
			});
		}).toPass();
		await taxCategoriesPage.deleteMenuItem.click();

		await waitForAlert(page);
	}
});

test('COMMERCE-6263 Configure tax properties at channel level', async ({
	apiHelpers,
	commerceAdminChannelDetailsPage,
	commerceAdminChannelsPage,
	newTaxCategoryPage,
	page,
	taxCategoriesPage,
}) => {
	let channel;

	const taxCategories = [
		{
			description: 'Test Description ' + getRandomInt(),
			name: 'Test ' + getRandomInt(),
			taxCategoriesExternalReferenceCode:
				'Test Reference ' + getRandomInt(),
		},
		{
			description: 'Test Description ' + getRandomInt(),
			name: 'Test ' + getRandomInt(),
			taxCategoriesExternalReferenceCode:
				'Test Reference ' + getRandomInt(),
		},
	];

	try {
		await test.step('Create two tax categories', async () => {
			await taxCategoriesPage.goto();

			for (const taxCategory of taxCategories) {
				await taxCategoriesPage.newButton.click();
				await newTaxCategoryPage.externalReferenceCodeInput.fill(
					taxCategory.taxCategoriesExternalReferenceCode
				);
				await newTaxCategoryPage.nameInput.fill(taxCategory.name);
				await newTaxCategoryPage.descriptionInput.fill(
					taxCategory.description
				);
				await newTaxCategoryPage.saveButton.click();

				await waitForAlert(page);

				await expect(taxCategoriesPage.newButton).toBeVisible();
			}
		});

		await test.step('Create a commerce channel via API and navigate to channel details page', async () => {
			const site =
				await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath(
					'guest'
				);

			channel = await apiHelpers.headlessCommerceAdminChannel.postChannel(
				{
					siteGroupId: site.id,
				}
			);

			await commerceAdminChannelsPage.goto();

			await (
				await commerceAdminChannelsPage.channelsTableRowLink(
					channel.name
				)
			).click();
		});

		await test.step('Add tax property and verify value', async () => {
			await commerceAdminChannelDetailsPage.taxCategoryInput.fill(
				taxCategories[0].name
			);

			await commerceAdminChannelDetailsPage
				.searchedEntry(taxCategories[0].name)
				.click();

			await commerceAdminChannelDetailsPage.saveButton.click();

			await waitForAlert(page);

			await commerceAdminChannelsPage.goto();

			await (
				await commerceAdminChannelsPage.channelsTableRowLink(
					channel.name
				)
			).click();

			await expect(
				commerceAdminChannelDetailsPage.taxCategoryInput
			).toHaveValue(taxCategories[0].name);
		});
	}
	finally {
		page.on('dialog', (dialog) => {
			dialog.accept();
		});

		await taxCategoriesPage.goto();

		for (const taxCategory of taxCategories) {
			await expect(async () => {
				await (
					await taxCategoriesPage.taxCategoriesTableRowActions(
						taxCategory.name
					)
				).click();

				await expect(taxCategoriesPage.deleteMenuItem).toBeVisible({
					timeout: 500,
				});
			}).toPass();
			await taxCategoriesPage.deleteMenuItem.click();

			await waitForAlert(page);
		}
	}
});
