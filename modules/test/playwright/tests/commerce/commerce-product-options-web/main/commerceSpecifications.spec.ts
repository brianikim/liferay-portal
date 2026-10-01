/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {globalMenuPagesTest} from '../../../../fixtures/globalMenuPagesTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {clickAndExpectToBeVisible} from '../../../../utils/clickAndExpectToBeVisible';
import getRandomString from '../../../../utils/getRandomString';
import {waitForAlert} from '../../../../utils/waitForAlert';
import {fillLocalizedInput} from '../../utils/fillLocalizedInput';

export const test = mergeTests(
	apiHelpersTest,
	globalMenuPagesTest,
	commercePagesTest,
	dataApiHelpersTest,
	loginTest()
);

function acceptNextConfirmation(page: Page) {
	const confirmation = {message: ''};

	page.once('dialog', async (dialog) => {
		confirmation.message = dialog.message();

		await dialog.accept();
	});

	return confirmation;
}

test(
	'Unable to delete specification picklist items',
	{tag: '@LPD-46948'},
	async ({apiHelpers, commerceSpecificationsPage, globalMenuPage, page}) => {
		const specification =
			await apiHelpers.headlessCommerceAdminCatalog.postSpecification();

		const picklist =
			await apiHelpers.listTypeAdmin.postRandomListTypeDefinition();

		const listTypeEntry = await apiHelpers.listTypeAdmin.postListTypeEntry({
			key: 'item1',
			listTypeDefinitionExternalReferenceCode:
				picklist.externalReferenceCode,
			name_i18n: {en_US: 'item1'},
		});

		await apiHelpers.headlessCommerceAdminCatalog.patchSpecification(
			specification.id,
			[picklist.id]
		);

		await globalMenuPage.goToCommerce('Specifications');

		await commerceSpecificationsPage
			.specificationNameLink(specification.title.en_US)
			.click();

		await expect(
			page
				.getByRole('cell', {name: picklist.externalReferenceCode})
				.nth(1)
		).toBeVisible();

		await commerceSpecificationsPage
			.specificationPicklistActionButton(picklist.externalReferenceCode)
			.click();
		await commerceSpecificationsPage
			.specificationPicklistDropdownMenu('Edit')
			.click();

		await expect(
			page.frameLocator('iframe').locator('tbody')
		).toContainText(listTypeEntry.externalReferenceCode);

		await commerceSpecificationsPage
			.specificationPicklistItemsActionButton(listTypeEntry.key)
			.click();
		await commerceSpecificationsPage
			.specificationPicklistDropdownMenuItems('Delete')
			.click();

		await expect(page.getByLabel('Delete Item')).toBeVisible();

		await commerceSpecificationsPage
			.deleteModalButtonAction('Delete')
			.click();

		await waitForAlert(
			page,
			'Success:The picklist item was deleted successfully.'
		);
	}
);

test(
	'Specification groups and labels can be created',
	{tag: ['@LPD-28891', '@LPD-106244-Grouped-2']},
	async ({apiHelpers, commerceSpecificationsPage, globalMenuPage, page}) => {
		const groupTitle = getRandomString();
		const specificationKey = getRandomString();
		const specificationTitle = getRandomString();

		try {
			await test.step('Create a specification label', async () => {
				await globalMenuPage.goToCommerce('Specifications');

				await commerceSpecificationsPage.createNewSpecificationsProduct.click();
				await fillLocalizedInput(
					commerceSpecificationsPage.specificationLabel,
					specificationTitle
				);
				await fillLocalizedInput(
					commerceSpecificationsPage.addDescriptionSpecifications,
					`${specificationTitle} Description`
				);
				await commerceSpecificationsPage.keyContent.fill(
					specificationKey
				);

				await expect(commerceSpecificationsPage.keyContent).toHaveValue(
					specificationKey
				);

				await commerceSpecificationsPage.saveButton.click();

				await waitForAlert(page);

				await commerceSpecificationsPage.goBack.click();

				await commerceSpecificationsPage.searchInput.fill(
					specificationTitle
				);
				await commerceSpecificationsPage.searchInput.press('Enter');
				await commerceSpecificationsPage
					.specificationNameLink(specificationTitle)
					.click();

				await expect(
					commerceSpecificationsPage.specificationLabel
				).toHaveValue(specificationTitle);
				await expect(
					commerceSpecificationsPage.addDescriptionSpecifications
				).toHaveValue(`${specificationTitle} Description`);
				await expect(commerceSpecificationsPage.keyContent).toHaveValue(
					specificationKey
				);
			});

			await test.step('Create a specification group', async () => {
				await globalMenuPage.goToCommerce('Specifications');

				await commerceSpecificationsPage.goToSpecificationGroup.click();
				await commerceSpecificationsPage.createNewSpecificationsProductGroup.click();
				await fillLocalizedInput(
					commerceSpecificationsPage.groupTitle,
					groupTitle
				);
				await fillLocalizedInput(
					commerceSpecificationsPage.addDescriptionSpecificationsGroup,
					`${groupTitle} Description`
				);

				await expect(commerceSpecificationsPage.keyContent).toHaveValue(
					groupTitle
				);

				await commerceSpecificationsPage.saveButton.click();

				await waitForAlert(page);

				await commerceSpecificationsPage.goBack.click();

				await commerceSpecificationsPage.searchInput.fill(groupTitle);
				await commerceSpecificationsPage.searchInput.press('Enter');
				await commerceSpecificationsPage
					.specificationNameLink(groupTitle)
					.click();

				await expect(commerceSpecificationsPage.groupTitle).toHaveValue(
					groupTitle
				);
				await expect(
					commerceSpecificationsPage.addDescriptionSpecificationsGroup
				).toHaveValue(`${groupTitle} Description`);
				await expect(commerceSpecificationsPage.keyContent).toHaveValue(
					groupTitle
				);
			});
		}
		finally {
			const specification = (
				await apiHelpers.headlessCommerceAdminCatalog.getSpecifications(
					new URLSearchParams({search: specificationTitle})
				)
			).items.find((item) => item.title.en_US === specificationTitle);

			if (specification) {
				apiHelpers.data.push({
					id: specification.id,
					type: 'specification',
				});
			}

			const optionCategory = (
				await apiHelpers.headlessCommerceAdminCatalog.getOptionCategories(
					new URLSearchParams({filter: `key eq '${groupTitle}'`})
				)
			).items.find((item) => item.title.en_US === groupTitle);

			if (optionCategory) {
				apiHelpers.data.push({
					id: optionCategory.id,
					type: 'optionCategory',
				});
			}
		}
	}
);

test(
	'Specification visibility is correctly saved',
	{tag: '@LPD-48103'},
	async ({apiHelpers, commerceSpecificationsPage, globalMenuPage, page}) => {
		const specification =
			await apiHelpers.headlessCommerceAdminCatalog.postSpecification(
				true,
				0,
				getRandomString(),
				null,
				false
			);

		await globalMenuPage.goToCommerce('Specifications');

		await commerceSpecificationsPage
			.specificationNameLink(specification.title.en_US)
			.click();
		await commerceSpecificationsPage.visibleToggle.check();
		await commerceSpecificationsPage.saveButton.click();

		await waitForAlert(page);

		await expect(commerceSpecificationsPage.visibleToggle).toBeChecked();
	}
);

test(
	'Specification groups and labels can be edited and deleted',
	{
		tag: [
			'@COMMERCE-6281',
			'@COMMERCE-6282',
			'@COMMERCE-6283',
			'@COMMERCE-6284',
			'@LPD-106079',
		],
	},
	async ({apiHelpers, commerceSpecificationsPage, globalMenuPage, page}) => {
		const optionCategory =
			await apiHelpers.headlessCommerceAdminCatalog.postOptionCategory(
				getRandomString()
			);

		const specification1 =
			await apiHelpers.headlessCommerceAdminCatalog.postSpecification(
				true,
				0,
				getRandomString(),
				optionCategory
			);
		const specification2 =
			await apiHelpers.headlessCommerceAdminCatalog.postSpecification(
				true,
				0,
				getRandomString(),
				optionCategory
			);

		const deleteConfirmationMessage =
			'Are you sure you want to delete this? It will be deleted immediately.';

		const newGroupKey = getRandomString();
		const newGroupTitle = getRandomString();
		const newSpecificationKey = getRandomString();
		const newSpecificationTitle = getRandomString();

		await test.step('Edit a specification label', async () => {
			await globalMenuPage.goToCommerce('Specifications');

			await clickAndExpectToBeVisible({
				autoClick: true,
				target: commerceSpecificationsPage.entryRowActionMenuItem(
					'Edit'
				),
				trigger: commerceSpecificationsPage.entryRowActionButton(
					specification1.title.en_US
				),
			});

			await fillLocalizedInput(
				commerceSpecificationsPage.specificationLabel,
				newSpecificationTitle
			);
			await commerceSpecificationsPage.keyContent.fill(
				newSpecificationKey
			);
			await commerceSpecificationsPage.saveButton.click();

			await waitForAlert(page);

			await expect(
				commerceSpecificationsPage.specificationLabel
			).toHaveValue(newSpecificationTitle);
			await expect(commerceSpecificationsPage.keyContent).toHaveValue(
				newSpecificationKey
			);

			await commerceSpecificationsPage.goBack.click();

			await expect(
				commerceSpecificationsPage.entryRow(newSpecificationTitle)
			).toBeVisible();
			await expect(
				commerceSpecificationsPage.defaultGroupCell(
					newSpecificationTitle
				)
			).toHaveText(optionCategory.title.en_US);
		});

		await test.step('Delete a specification label', async () => {
			const confirmation = acceptNextConfirmation(page);

			await clickAndExpectToBeVisible({
				autoClick: true,
				target: commerceSpecificationsPage.entryRowActionMenuItem(
					'Delete'
				),
				trigger: commerceSpecificationsPage.entryRowActionButton(
					specification2.title.en_US
				),
			});

			await waitForAlert(page);

			expect(confirmation.message).toBe(deleteConfirmationMessage);
			await expect(
				commerceSpecificationsPage.entryRow(specification2.title.en_US)
			).toHaveCount(0);
		});

		await test.step('Edit a specification group', async () => {
			await commerceSpecificationsPage.goToSpecificationGroup.click();

			await clickAndExpectToBeVisible({
				autoClick: true,
				target: commerceSpecificationsPage.entryRowActionMenuItem(
					'Edit'
				),
				trigger: commerceSpecificationsPage.entryRowActionButton(
					optionCategory.title.en_US
				),
			});

			await fillLocalizedInput(
				commerceSpecificationsPage.groupTitle,
				newGroupTitle
			);
			await commerceSpecificationsPage.keyContent.fill(newGroupKey);
			await commerceSpecificationsPage.saveButton.click();

			await waitForAlert(page);

			await expect(commerceSpecificationsPage.groupTitle).toHaveValue(
				newGroupTitle
			);
			await expect(commerceSpecificationsPage.keyContent).toHaveValue(
				newGroupKey
			);

			await commerceSpecificationsPage.goBack.click();

			await expect(
				commerceSpecificationsPage.entryRow(newGroupTitle)
			).toBeVisible();
		});

		await test.step('Delete a specification group holding a label', async () => {
			const confirmation = acceptNextConfirmation(page);

			await clickAndExpectToBeVisible({
				autoClick: true,
				target: commerceSpecificationsPage.entryRowActionMenuItem(
					'Delete'
				),
				trigger:
					commerceSpecificationsPage.entryRowActionButton(
						newGroupTitle
					),
			});

			await waitForAlert(page);

			expect(confirmation.message).toBe(deleteConfirmationMessage);
			await expect(
				commerceSpecificationsPage.entryRow(newGroupTitle)
			).toHaveCount(0);

			await commerceSpecificationsPage.goToSpecificationLabel.click();

			await expect(
				commerceSpecificationsPage.entryRow(newSpecificationTitle)
			).toBeVisible();
			await expect(
				commerceSpecificationsPage.defaultGroupCell(
					newSpecificationTitle
				)
			).toHaveText('');
		});
	}
);
