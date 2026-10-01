/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page, expect} from '@playwright/test';

export type ProductSubscriptionName =
	| 'Delivery Subscription'
	| 'Payment Subscription';

export const SUBSCRIPTION_FIELD_IDS = {
	'Delivery Subscription': {
		cycleLengthContainer: 'deliveryCycleLengthContainer',
		enabled: 'deliverySubscriptionEnabled',
		length: 'deliverySubscriptionLength',
		type: 'deliverySubscriptionType',
		yearlyMode:
			'deliverySubscriptionTypeSettings--yearly--deliveryYearlyMode--',
		yearlyMonth:
			'deliverySubscriptionTypeSettings--yearly--deliveryMonth--',
		yearlyMonthDay:
			'deliverySubscriptionTypeSettings--yearly--deliveryMonthDay--',
	},
	'Payment Subscription': {
		cycleLengthContainer: 'cycleLengthContainer',
		enabled: 'subscriptionEnabled',
		length: 'subscriptionLength',
		type: 'subscriptionType',
		yearlyMode: 'subscriptionTypeSettings--yearly--yearlyMode--',
		yearlyMonth: 'subscriptionTypeSettings--yearly--month--',
		yearlyMonthDay: 'subscriptionTypeSettings--yearly--monthDay--',
	},
};

export class CommerceAdminProductDetailsPage {
	readonly addExistingSpecificationValueTextbox: Locator;
	readonly addSpecification: Locator;
	readonly addSpecificationFrame: FrameLocator;
	readonly backLink: Locator;
	readonly closeEditFrame: Locator;
	readonly createNewSpecificationProduct: Locator;
	readonly createNewValueSpecificationProduct: Locator;
	readonly dropdownProductSpecification: (
		chooseEditOrDelete: string
	) => Promise<Locator>;
	readonly duplicateFrame: FrameLocator;
	readonly duplicateFrameCatalogInput: Locator;
	readonly duplicateFrameCatalogMenuItem: (catalogName: string) => Locator;
	readonly duplicateFrameSubmitButton: Locator;
	readonly editFrameSaveButton: Locator;
	readonly editFrameSpecificationProduct: (
		specificationValue: string
	) => Promise<string[]>;
	readonly editFrameSpecificationProductValue: Locator;
	readonly editSuccessMessage: Locator;
	readonly ellipsisProductSpecification: (
		specificationName: string
	) => Locator;
	readonly ellipsisFrameProductSpecification: FrameLocator;
	readonly expirationDateInput: Locator;
	readonly frameChooseSpecification: (specificationName: string) => Locator;
	readonly frameChooseSpecificationValue: (
		specificationValue: string
	) => Promise<string[]>;
	readonly frameDropdownSpecification: Locator;
	readonly frameSubmitSpecification: Locator;
	readonly headerActionsButton: Locator;
	readonly headerActionsMenuItem: (name: string) => Locator;
	readonly menuItemSpecification: (chooseAddOrCreate: string) => Locator;
	readonly nameInput: Locator;
	readonly nameInputLocaleMenuItem: (locale: string) => Locator;
	readonly nameInputLocaleSelector: Locator;
	readonly neverExpireToggle: Locator;
	readonly page: Page;
	readonly productConfigurationLink: Locator;
	readonly productDetailsInput: (inputName: string) => Promise<Locator>;
	readonly productDetailsLink: Locator;
	readonly productDiagramLink: Locator;
	readonly productGroupsLink: Locator;
	readonly productId: Locator;
	readonly productMediaLink: Locator;
	readonly productOptionsLink: Locator;
	readonly productRelationsLink: Locator;
	readonly productSkusLink: Locator;
	readonly productSubscriptionLink: Locator;
	readonly productVisibilityLink: Locator;
	readonly publishLink: Locator;
	readonly saveAsDraftLink: Locator;
	readonly selectedCatalogOption: Locator;
	readonly shortDescriptionInput: Locator;
	readonly subscriptionEnabledInput: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly subscriptionEnabledLabel: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly subscriptionLengthInput: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly subscriptionLengthSuffix: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly subscriptionTypeSelect: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly subscriptionYearlyModeSelect: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly subscriptionYearlyMonthDayInput: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly subscriptionYearlyMonthSelect: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly textTableCell: (text: string) => Locator;
	readonly visibleToggle: Locator;
	readonly workflowStatus: (label: string) => Locator;

	constructor(page: Page) {
		this.addSpecification = page
			.getByTestId('managementToolbar')
			.locator('[data-testid="fdsCreationActionButton"]');
		this.addSpecificationFrame = page.frameLocator('iframe >> nth=1');
		this.addExistingSpecificationValueTextbox =
			this.addSpecificationFrame.getByRole('textbox');
		this.backLink = page.locator('span[title="Back"]');
		this.closeEditFrame = page
			.frameLocator('iframe')
			.getByRole('button')
			.first();
		this.createNewSpecificationProduct =
			this.addSpecificationFrame.getByPlaceholder('Specification');
		this.createNewValueSpecificationProduct = this.addSpecificationFrame
			.getByRole('textbox')
			.nth(1);
		this.dropdownProductSpecification = async (
			chooseEditOrDelete: string
		) => {
			return page.getByRole('menuitem', {name: chooseEditOrDelete});
		};
		this.duplicateFrame = page.frameLocator('iframe[title="Duplicate"]');
		this.duplicateFrameCatalogInput =
			this.duplicateFrame.getByPlaceholder('Type Here');
		this.duplicateFrameCatalogMenuItem = (catalogName: string) =>
			this.duplicateFrame.getByRole('menuitem', {
				exact: true,
				name: catalogName,
			});
		this.duplicateFrameSubmitButton = this.duplicateFrame.getByRole(
			'button',
			{exact: true, name: 'Submit'}
		);
		this.editFrameSpecificationProduct = async (
			specificationValue: string
		) => {
			return this.ellipsisFrameProductSpecification
				.locator(
					'select[name="_com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet_listTypeEntriesSelect"]'
				)
				.selectOption(specificationValue);
		};
		this.ellipsisProductSpecification = (specificationName: string) => {
			return page
				.locator('[data-testid="visualization-mode-table"]')
				.getByRole('button', {
					exact: true,
					name: `${specificationName} Actions`,
				});
		};
		this.ellipsisFrameProductSpecification = page.frameLocator('iframe');
		this.editFrameSaveButton =
			this.ellipsisFrameProductSpecification.getByRole('button', {
				name: 'Save',
			});
		this.editFrameSpecificationProductValue =
			this.ellipsisFrameProductSpecification.locator(
				'[id="_com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet_value"]'
			);
		this.editSuccessMessage =
			this.ellipsisFrameProductSpecification.getByText(
				'Success:Your request completed successfully.'
			);
		this.expirationDateInput = page.locator(
			'input[id*="expirationDate"][placeholder="mm/dd/yyyy"]'
		);
		this.frameChooseSpecification = (specificationName: string) => {
			return this.addSpecificationFrame.getByRole('option', {
				name: specificationName,
			});
		};
		this.frameChooseSpecificationValue = async (
			specificationValue: string
		) => {
			return this.addSpecificationFrame
				.locator('select[name="listTypeEntriesSelect"]')
				.selectOption(specificationValue);
		};
		this.frameDropdownSpecification = this.addSpecificationFrame.getByLabel(
			'SpecificationRequired'
		);
		this.frameSubmitSpecification = this.addSpecificationFrame.getByRole(
			'button',
			{name: 'Submit'}
		);
		this.headerActionsButton = page
			.locator('#dropdown-header-container')
			.getByRole('button', {name: 'Actions'});
		this.headerActionsMenuItem = (name: string) =>
			page.getByRole('menuitem', {exact: true, name});
		this.menuItemSpecification = (chooseAddOrCreate: string) => {
			return page.getByRole('menuitem', {name: chooseAddOrCreate});
		};
		this.nameInput = page.locator('input[id$="_nameMapAsXML"]');
		this.nameInputLocaleMenuItem = (locale: string) =>
			page.getByRole('menuitem', {name: locale});
		this.nameInputLocaleSelector = page.locator(
			'[id$="nameMapAsXMLBoundingBox"] .input-localized-trigger'
		);
		this.neverExpireToggle = page.getByLabel('Never Expire');
		this.page = page;
		this.productConfigurationLink = page.getByRole('link', {
			name: 'Configuration',
		});
		this.productDetailsInput = async (inputName: string) =>
			page.getByLabel(inputName);
		this.productDetailsLink = page.getByRole('link', {
			exact: true,
			name: 'Details',
		});
		this.productDiagramLink = page.getByRole('link', {
			name: 'Diagram',
		});
		this.productGroupsLink = page.getByRole('link', {
			exact: true,
			name: 'Product Groups',
		});
		this.productId = page.locator('span:has-text("ID")+strong');
		this.productMediaLink = page.getByRole('link', {
			name: 'Media',
		});
		this.productOptionsLink = page.getByRole('link', {
			name: 'Options',
		});
		this.productRelationsLink = page.getByRole('link', {
			name: 'Product Relations',
		});
		this.productSkusLink = page.getByRole('link', {
			name: 'Skus',
		});
		this.productSubscriptionLink = page.getByRole('link', {
			exact: true,
			name: 'Subscription',
		});
		this.productVisibilityLink = page.getByRole('link', {
			name: 'Visibility',
		});
		this.publishLink = page.getByRole('link', {name: 'Publish'});
		this.saveAsDraftLink = page.getByRole('link', {
			exact: true,
			name: 'Save as Draft',
		});
		this.selectedCatalogOption = page.locator(
			'select[name$="_commerceCatalogGroupId"] option:checked'
		);
		this.shortDescriptionInput = page.locator(
			'textarea[id$="_shortDescriptionMapAsXML"]'
		);
		this.subscriptionEnabledInput = (
			subscriptionName: ProductSubscriptionName
		) =>
			page.locator(
				`input[id$="_${SUBSCRIPTION_FIELD_IDS[subscriptionName].enabled}"]`
			);
		this.subscriptionEnabledLabel = (
			subscriptionName: ProductSubscriptionName
		) =>
			page.locator(
				`label[for$="_${SUBSCRIPTION_FIELD_IDS[subscriptionName].enabled}"]`
			);
		this.subscriptionLengthInput = (
			subscriptionName: ProductSubscriptionName
		) =>
			page.locator(
				`input[id$="_${SUBSCRIPTION_FIELD_IDS[subscriptionName].length}"]`
			);
		this.subscriptionLengthSuffix = (
			subscriptionName: ProductSubscriptionName
		) =>
			page.locator(
				`[id$="_${SUBSCRIPTION_FIELD_IDS[subscriptionName].cycleLengthContainer}"] .input-group-text`
			);
		this.subscriptionTypeSelect = (
			subscriptionName: ProductSubscriptionName
		) =>
			page.locator(
				`select[id$="_${SUBSCRIPTION_FIELD_IDS[subscriptionName].type}"]`
			);
		this.subscriptionYearlyModeSelect = (
			subscriptionName: ProductSubscriptionName
		) =>
			page.locator(
				`select[name$="_${SUBSCRIPTION_FIELD_IDS[subscriptionName].yearlyMode}"]`
			);
		this.subscriptionYearlyMonthDayInput = (
			subscriptionName: ProductSubscriptionName
		) =>
			page.locator(
				`input[name$="_${SUBSCRIPTION_FIELD_IDS[subscriptionName].yearlyMonthDay}"]`
			);
		this.subscriptionYearlyMonthSelect = (
			subscriptionName: ProductSubscriptionName
		) =>
			page.locator(
				`select[name$="_${SUBSCRIPTION_FIELD_IDS[subscriptionName].yearlyMonth}"]`
			);
		this.textTableCell = (text: string) =>
			this.page.getByRole('cell', {
				exact: true,
				name: text,
			});
		this.visibleToggle = this.ellipsisFrameProductSpecification.getByLabel(
			'Visible',
			{exact: true}
		);
		this.workflowStatus = (label: string) =>
			page.locator('.workflow-status').getByText(label, {exact: true});
	}

	async addExistingProductSpecification(
		chooseAddOrEdit: string,
		specificationName: string,
		specificationValue: string
	) {
		await this.addSpecification.click();
		await this.menuItemSpecification(chooseAddOrEdit).click();

		await expect(this.frameDropdownSpecification).toBeVisible();

		await this.frameDropdownSpecification.click();
		await this.frameChooseSpecification(specificationName).click();
		await this.addExistingSpecificationValueTextbox.fill(
			specificationValue
		);
		await this.frameSubmitSpecification.click();
	}

	async addOrEditProductSpecification(
		chooseAddOrEdit: string,
		specificationName: string,
		specificationValue?: string
	) {
		await this.addSpecification.click();
		await this.menuItemSpecification(chooseAddOrEdit).click();

		await expect(this.frameDropdownSpecification).toBeVisible();

		await this.frameDropdownSpecification.click();
		await this.frameChooseSpecification(specificationName).click();
		if (specificationValue) {
			await this.frameChooseSpecificationValue(specificationValue);
		}
		await this.frameSubmitSpecification.click();
	}

	async createSpecificationProduct(
		chooseAddOrCreate: string,
		specificationName: string,
		specificationValue?: string
	) {
		await this.addSpecification.click();
		await (await this.menuItemSpecification(chooseAddOrCreate)).click();
		await this.createNewSpecificationProduct.fill(specificationName);
		if (specificationValue) {
			await this.createNewValueSpecificationProduct.fill(
				specificationValue
			);
		}
		await this.frameSubmitSpecification.click();
	}

	async duplicate(catalogName: string) {
		await this.headerActionsButton.click();
		await this.headerActionsMenuItem('Duplicate').click();
		await this.duplicateFrameCatalogInput.fill(catalogName);
		await this.duplicateFrameCatalogMenuItem(catalogName).click();
		await this.duplicateFrameSubmitButton.click();
	}

	async editOrDeleteProductSpecification(
		chooseEditOrDelete: string,
		specificationTitle: string,
		specificationValue: string
	) {
		await this.ellipsisProductSpecification(specificationTitle).click();
		await (
			await this.dropdownProductSpecification(chooseEditOrDelete)
		).click();
		await this.editFrameSpecificationProduct(specificationValue);
		await this.editFrameSaveButton.click();
	}

	async goToProductConfiguration() {
		await this.productConfigurationLink.click();
	}

	async goToProductDiagram() {
		await this.productDiagramLink.click();
	}

	async goToProductGroups() {
		await this.productGroupsLink.click();
	}

	async goToProductOptions() {
		await this.productOptionsLink.click();
	}

	async goToProductRelations() {
		await this.productRelationsLink.click();
	}

	async goToProductSkus() {
		await this.productSkusLink.click();
	}

	async goToProductVisibility() {
		await this.productVisibilityLink.click();
	}

	async publish() {
		const publishAlert = this.page
			.locator('.alert-success')
			.filter({
				hasText: 'Success:Your request completed successfully.',
			})
			.first();

		await expect(async () => {
			if (await publishAlert.isHidden()) {
				await this.publishLink.click({timeout: 5000});
			}

			await expect(publishAlert).toBeVisible({timeout: 5000});
		}).toPass({timeout: 30000});

		return publishAlert;
	}
}
