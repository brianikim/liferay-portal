/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {ApplicationsMenuPage} from '../product-navigation-applications-menu/ApplicationsMenuPage';

export class CommerceProductAdminPage {
	readonly applicationsMenuPage: ApplicationsMenuPage;
	readonly creationMenuNewButton: Locator;
	readonly deleteMenuItem: Locator;
	readonly generateSkusMenuItem: Locator;
	readonly itemOptionMenu: Locator;
	readonly managementToolbarSearchInput: Locator;
	readonly modalAddButton: Locator;
	readonly modalCloseButton: Locator;
	readonly newMenuButton: Locator;
	readonly page: Page;
	readonly productRelationsLink: Locator;
	readonly productSkusLink: Locator;
	readonly spareProductMenuButton: Locator;

	constructor(page: Page) {
		this.applicationsMenuPage = new ApplicationsMenuPage(page);
		this.creationMenuNewButton = page.getByTestId('creationMenuNewButton');
		this.deleteMenuItem = page.getByRole('menuitem', {name: 'Delete'});
		this.generateSkusMenuItem = page.getByRole('menuitem', {
			exact: true,
			name: 'Generate All SKU Combinations',
		});
		this.itemOptionMenu = page.getByTestId('itemActionDropdown');
		this.managementToolbarSearchInput = page
			.getByTestId('management-toolbar')
			.getByPlaceholder('Search', {exact: true});
		this.modalAddButton = page
			.locator('modal-item-last')
			.getByRole('button', {name: 'Add'});
		this.modalCloseButton = page
			.locator('modal-header')
			.getByRole('button', {name: 'close'});
		this.newMenuButton = page.getByTestId('creationMenuNewButton');
		this.productRelationsLink = page.getByRole('link', {
			exact: true,
			name: 'Product Relations',
		});
		this.productSkusLink = page.getByRole('link', {
			exact: true,
			name: 'SKUs',
		});
		this.spareProductMenuButton = page.getByRole('menuitem', {
			exact: true,
			name: 'Add Spare Product',
		});
	}

	async addSpareProductRelation() {
		await this.goToProductRelations();
		await this.creationMenuNewButton.click();

		if (await this.spareProductMenuButton.isVisible()) {
			await this.spareProductMenuButton.click();
		}
	}

	async generateSkus() {
		await this.productSkusLink.click();

		if (await this.creationMenuNewButton.isHidden()) {
			await this.productSkusLink.click();
		}

		await this.creationMenuNewButton.click();
		await this.generateSkusMenuItem.click();
	}

	async goto() {
		await this.applicationsMenuPage.goToProducts();
	}

	async goToProductRelations() {
		await this.productRelationsLink.click();
	}

	async goToSpecificProductMenu(productName: string) {
		await this.page.getByRole('link', {name: productName}).click();
	}
}
