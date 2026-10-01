/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page, expect} from '@playwright/test';

import {waitForAlert} from '../../../utils/waitForAlert';
import {GlobalMenuPage} from '../../product-navigation-applications-menu/GlobalMenuPage';
import {CommerceDNDTablePage} from '../commerceDNDTablePage';

export class CommerceAdminProductPage extends CommerceDNDTablePage {
	readonly addButton: Locator;
	readonly addVirtualProductFileEntryButton: Locator;
	readonly addVirtualSkuFileEntryButton: Locator;
	readonly globalMenuPage: GlobalMenuPage;
	readonly columnHeader: (name: string) => Locator;
	readonly creationMenuItem: (menuItemName: string) => Locator;
	readonly creationMenuNewButton: Locator;
	readonly deleteMenuItem: Locator;
	readonly managementToolbarItemLink: (productName: string) => Locator;
	readonly errorAlert: Locator;
	readonly productRowActionsButton: (productName: string) => Locator;
	readonly productsTableRow: (productName: string) => Locator;
	readonly productsTableRowImage: (productName: string) => Locator;
	readonly managementToolbarSearchInput: Locator;
	readonly modalAddButton: Locator;
	readonly modalBody: Locator;
	readonly modalCancelButton: Locator;
	readonly page: Page;
	readonly menuItemProductType: (productType: string) => Locator;
	readonly modalFieldName: Locator;
	readonly modalFrameLocator: FrameLocator;
	readonly modalMenuItem: (catalogName: string) => Locator;
	readonly modalPlaceHolder: Locator;
	readonly modalSubmitButton: Locator;
	readonly paginationItemsButton: (itemsCount: number) => Locator;
	readonly paginationNextButton: Locator;
	readonly paginationResults: (
		start: number,
		end: number,
		total: number
	) => Locator;
	readonly productSkuShippingOverrideTitle: Locator;
	readonly productSkuSidePanelFrame: FrameLocator;
	readonly productSkuTableRowLink: (sku: string) => Locator;
	readonly productSkuVirtualActivationStatusSelect: Locator;
	readonly productSkuVirtualBaseInformationButton: Locator;
	readonly productSkuVirtualDurationInput: Locator;
	readonly productSkuVirtualEnableSampleToggle: Locator;
	readonly productSkuVirtualFileEntry: (name: string) => Locator;
	readonly productSkuVirtualFileEntryCancelButton: Locator;
	readonly productSkuVirtualFileEntrySaveButton: Locator;
	readonly productSkuVirtualFileEntryURLInput: Locator;
	readonly productSkuVirtualMaxUsagesInput: Locator;
	readonly productSkuVirtualOverrideToggle: Locator;
	readonly productSkuVirtualSampleButton: Locator;
	readonly productSkuVirtualSampleFileURLInput: Locator;
	readonly productSkuVirtualSaveButton: Locator;
	readonly productSkusLink: Locator;
	readonly productVersionRow: (
		productName: string,
		status: string
	) => Locator;
	readonly productVersionRowLink: (
		productName: string,
		status: string
	) => Locator;
	readonly productVirtualBaseInformationLink: Locator;
	readonly productVirtualEnableSampleToggle: Locator;
	readonly productVirtualFileEntry: (name: string) => Locator;
	readonly productVirtualFileEntryCancelButton: Locator;
	readonly productVirtualFileEntryItemSelectorAddButton: Locator;
	readonly productVirtualFileEntryItemSelectorFileInput: Locator;
	readonly productVirtualFileEntrySaveButton: Locator;
	readonly productVirtualFileEntrySelectButton: Locator;
	readonly productVirtualFileEntryURLInput: Locator;
	readonly productVirtualLink: Locator;
	readonly productVirtualMaxUsagesInput: Locator;
	readonly productVirtualSampleFileEntryNameInput: Locator;
	readonly productVirtualSampleFileEntryRemoveButton: Locator;
	readonly productVirtualSampleFileURLInput: Locator;
	readonly productVirtualSampleItemSelectorAddButton: Locator;
	readonly productVirtualSampleItemSelectorFileInput: Locator;
	readonly productVirtualSampleLink: Locator;
	readonly productVirtualSaveButton: Locator;
	readonly productVirtualSelectSampleFileButton: Locator;
	readonly productsTableRowLink: (productName: string) => Locator;
	readonly spareProductMenuButton: Locator;
	readonly specificProductMenuLink: (productName: string) => Promise<Locator>;
	readonly tableBodyRow: (index: number) => Locator;
	readonly validProductCheckbox: (productName: string) => Promise<Locator>;
	readonly virtualSettingsOverrideLink: Locator;

	constructor(page: Page) {
		super(
			page,
			'#_com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet_fm .fds table'
		);
		this.addButton = page
			.getByTestId('managementToolbar')
			.locator('[data-testid="fdsCreationActionButton"]');
		this.addVirtualProductFileEntryButton = page
			.getByRole('button', {exact: true, name: 'Add File Entry'})
			.first();
		this.addVirtualSkuFileEntryButton = page
			.frameLocator('iframe')
			.getByRole('button', {exact: true, name: 'Add File Entry'})
			.first();
		this.globalMenuPage = new GlobalMenuPage(page);
		this.columnHeader = (name: string) =>
			this.table.getByRole('columnheader', {exact: true, name});
		this.creationMenuItem = (menuItemName: string) =>
			page.getByRole('menuitem', {
				exact: true,
				name: menuItemName,
			});
		this.creationMenuNewButton = page
			.getByTestId('managementToolbar')
			.locator('[data-testid="fdsCreationActionButton"]');
		this.deleteMenuItem = page.getByRole('menuitem', {
			exact: true,
			name: 'Delete',
		});
		this.managementToolbarItemLink = (productName: string) =>
			page.getByRole('link', {exact: true, name: productName});
		this.errorAlert = page.locator('.alert-danger');
		this.productsTableRow = (productName: string) =>
			page.getByRole('row').filter({hasText: productName});
		this.productsTableRowImage = (productName: string) =>
			this.productsTableRow(productName).locator('img.sticker-img');
		this.productRowActionsButton = (productName: string) =>
			page
				.getByRole('row')
				.filter({hasText: productName})
				.getByRole('button');
		this.managementToolbarSearchInput = page
			.getByTestId('managementToolbar')
			.getByPlaceholder('Search', {exact: true});
		this.modalAddButton = page.getByRole('button', {name: 'Add'});
		this.modalBody = page.locator('.fds-modal-body');
		this.modalCancelButton = page.getByRole('button', {name: 'Cancel'});
		this.page = page;
		this.menuItemProductType = (productType: string) =>
			page.getByRole('menuitem', {exact: true, name: productType});
		this.modalFrameLocator = page.frameLocator('.fds-modal-body iframe');
		this.modalFieldName =
			this.modalFrameLocator.getByLabel('Name Required');
		this.modalMenuItem = (catalogName: string) =>
			this.modalFrameLocator.getByRole('menuitem', {
				exact: true,
				name: catalogName,
			});
		this.modalPlaceHolder =
			this.modalFrameLocator.getByPlaceholder('Type Here');
		this.modalSubmitButton = this.modalFrameLocator.getByRole('button', {
			exact: true,
			name: 'Submit',
		});
		this.paginationItemsButton = (itemsCount: number) =>
			page.getByRole('button', {name: `${itemsCount} Items`});
		this.paginationNextButton = page.getByTestId('nextArrow');
		this.paginationResults = (start: number, end: number, total: number) =>
			page.getByText(`Showing ${start} to ${end} of ${total} entries.`);
		this.productSkuSidePanelFrame = page.frameLocator('iframe');
		this.productSkuShippingOverrideTitle =
			this.productSkuSidePanelFrame.getByText('Shipping Override', {
				exact: true,
			});
		this.productSkuTableRowLink = (sku: string) =>
			page.getByRole('link', {name: sku});
		this.productSkuVirtualActivationStatusSelect =
			this.productSkuSidePanelFrame.getByLabel('Activation Status');
		this.productSkuVirtualBaseInformationButton =
			this.productSkuSidePanelFrame.getByRole('button', {
				exact: true,
				name: 'Base Information',
			});
		this.productSkuVirtualDurationInput =
			this.productSkuSidePanelFrame.getByLabel('Duration');
		this.productSkuVirtualEnableSampleToggle =
			this.productSkuSidePanelFrame.getByLabel('Enable Sample');
		this.productSkuVirtualFileEntry = (name: string) =>
			this.productSkuSidePanelFrame.getByText(name);
		this.productSkuVirtualFileEntryCancelButton = page
			.frameLocator('iframe')
			.frameLocator('iframe >> nth=1')
			.getByRole('button', {exact: true, name: 'Cancel'});
		this.productSkuVirtualFileEntrySaveButton = page
			.frameLocator('iframe')
			.frameLocator('iframe >> nth=1')
			.getByRole('button', {exact: true, name: 'Save'});
		this.productSkuVirtualFileEntryURLInput = page
			.frameLocator('iframe')
			.frameLocator('iframe >> nth=1')
			.getByLabel('URL');
		this.productSkuVirtualMaxUsagesInput =
			this.productSkuSidePanelFrame.getByLabel('Max Number of Downloads');
		this.productSkuVirtualOverrideToggle = page
			.frameLocator('iframe')
			.getByLabel('Override', {exact: true});
		this.productSkuVirtualSampleButton =
			this.productSkuSidePanelFrame.getByRole('button', {
				exact: true,
				name: 'Sample',
			});
		this.productSkuVirtualSampleFileURLInput =
			this.productSkuSidePanelFrame.getByLabel('Sample File URL');
		this.productSkuVirtualSaveButton =
			this.productSkuSidePanelFrame.getByRole('button', {
				exact: true,
				name: 'Save',
			});
		this.productSkusLink = page.getByRole('link', {
			exact: true,
			name: 'SKUs',
		});
		this.productVersionRow = (productName: string, status: string) =>
			this.productsTableRow(productName).filter({hasText: status});
		this.productVersionRowLink = (productName: string, status: string) =>
			this.productVersionRow(productName, status).getByRole('link', {
				exact: true,
				name: productName,
			});
		this.productVirtualBaseInformationLink = page.locator(
			'fieldset#baseInformation > a'
		);
		this.productVirtualEnableSampleToggle =
			page.getByLabel('Enable Sample');
		this.productVirtualFileEntry = (name: string) => page.getByText(name);
		this.productVirtualFileEntryCancelButton = page
			.frameLocator('iframe >> nth=1')
			.getByRole('button', {exact: true, name: 'Cancel'});
		this.productVirtualFileEntryItemSelectorAddButton = page
			.frameLocator('iframe >> nth=1')
			.frameLocator('iframe[title="Select File"]')
			.getByRole('button', {exact: true, name: 'Add'});
		this.productVirtualFileEntryItemSelectorFileInput = page
			.frameLocator('iframe >> nth=1')
			.frameLocator('iframe[title="Select File"]')
			.locator('input[type="file"]');
		this.productVirtualFileEntrySaveButton = page
			.frameLocator('iframe >> nth=1')
			.getByRole('button', {exact: true, name: 'Save'});
		this.productVirtualFileEntrySelectButton = page
			.frameLocator('iframe >> nth=1')
			.getByRole('button', {exact: true, name: 'Select'});
		this.productVirtualFileEntryURLInput = page
			.frameLocator('iframe >> nth=1')
			.getByLabel('URL');
		this.productVirtualLink = page.getByRole('link', {
			exact: true,
			name: 'Virtual',
		});
		this.productVirtualMaxUsagesInput = page.getByLabel(
			'Max Number of Downloads'
		);
		this.productVirtualSampleFileEntryNameInput = page.locator(
			'[id$="_sampleFileEntryNameInput"]'
		);
		this.productVirtualSampleFileEntryRemoveButton = page
			.locator('[id$="_sampleFileEntryRemove"]')
			.getByRole('button');
		this.productVirtualSampleFileURLInput =
			page.getByLabel('Sample File URL');
		this.productVirtualSampleItemSelectorAddButton = page
			.frameLocator('iframe[title="Select File"]')
			.getByRole('button', {exact: true, name: 'Add'});
		this.productVirtualSampleItemSelectorFileInput = page
			.frameLocator('iframe[title="Select File"]')
			.locator('input[type="file"]');
		this.productVirtualSampleLink = page.locator('fieldset#sample > a');
		this.productVirtualSaveButton = page.getByRole('button', {
			exact: true,
			name: 'Save',
		});
		this.productVirtualSelectSampleFileButton = page.locator(
			'[id$="_selectSampleFile"]'
		);
		this.productsTableRowLink = (productName: string) =>
			page.getByRole('link', {exact: true, name: productName});
		this.spareProductMenuButton = page.getByRole('menuitem', {
			exact: true,
			name: 'Add Spare Product',
		});
		this.specificProductMenuLink = async (productName: string) => {
			return page.getByRole('link', {name: productName});
		};
		this.tableBodyRow = (index: number) =>
			this.table.locator('tbody tr').nth(index);
		this.validProductCheckbox = async (productName: string) => {
			return page
				.frameLocator('#modalIframe')
				.getByTestId('row')
				.filter({hasText: productName})
				.getByRole('checkbox', {disabled: false});
		};
		this.virtualSettingsOverrideLink = page
			.frameLocator('iframe')
			.getByRole('link', {
				exact: true,
				name: 'Virtual Settings Override',
			});
	}

	async generateSkus() {
		await this.productSkusLink.click();

		if (await this.creationMenuNewButton.isHidden()) {
			await this.productSkusLink.click();
		}

		await expect(this.creationMenuNewButton).toBeVisible();

		await this.creationMenuNewButton.click();
		await this.creationMenuItem('Generate All SKU Combinations').click();
		await waitForAlert(this.page);
	}

	async addSku(skuName: string, option = {name: '', value: ''}) {
		await this.productSkusLink.click();

		if (await this.creationMenuNewButton.isHidden()) {
			await this.productSkusLink.click();
		}

		await this.creationMenuNewButton.click();
		await this.creationMenuItem('Add SKU').click();

		await this.page.waitForLoadState('load');

		await this.page
			.frameLocator('iframe')
			.getByLabel('SKU Required')
			.fill(skuName);

		if (option.name !== '' && option.value !== '') {
			await this.page
				.frameLocator('iframe')
				.getByLabel(option.name)
				.selectOption(option.value);
		}

		await this.page
			.frameLocator('iframe')
			.getByRole('button', {exact: true, name: 'Publish'})
			.click();
	}

	async goto() {
		await this.globalMenuPage.goToCommerce('Products');
	}

	async gotoProduct(productName: string) {
		await expect(async () => {
			await this.goto();
			await this.table.waitFor({state: 'visible'});
			await this.managementToolbarSearchInput.fill(productName);
			await this.managementToolbarSearchInput.press('Enter');
			await this.productsTableRowLink(productName).click();
		}).toPass();
	}

	async search(keywords: string) {
		await this.managementToolbarSearchInput.fill(keywords);
		await this.managementToolbarSearchInput.press('Enter');
	}
}
