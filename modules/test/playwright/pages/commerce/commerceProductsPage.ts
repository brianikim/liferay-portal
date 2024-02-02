/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// @ts-ignore

import {Locator, Page} from '@playwright/test';
import {ApplicationsMenuPage} from '../product-navigation-applications-menu/ApplicationsMenuPage';

export class commerceProductsMenuPage {
    readonly applicationsMenuPage: ApplicationsMenuPage;
    readonly deleteMenuItem: Locator;
    readonly itemOptionMenu: Locator;
    readonly modalAddButton: Locator;
    readonly modalCloseButton: Locator;
    readonly newMenuButton: Locator;
    readonly spareProductMenuButton: Locator;
    readonly page: Page;

    constructor(page: Page) {
        
        this.applicationsMenuPage = new ApplicationsMenuPage(page);
        this.deleteMenuItem = page.getByRole('menuitem', {name: 'Delete'});
        this.itemOptionMenu = page.getByTestId('itemActionDropdown');
        this.modalAddButton = page.locator('modal-item-last').getByRole('button', {name: 'Add'});
        this.modalCloseButton = page.locator('modal-header').getByRole('button', {name: 'close'});
        this.newMenuButton = page.getByTestId('creationMenuNewButton');
        this.spareProductMenuButton = page.getByRole('menuitem', {
            exact: true,
            name: 'Add Spare Product'
        });
    }

    async addSpareProductRelation() {
        await this.goToProductRelations();
        await this.newMenuButton.click();

        if (await this.spareProductMenuButton.isVisible()) {
			await this.spareProductMenuButton.click();
		}
    }

    async goto() {
        await this.applicationsMenuPage.goToProducts();
    }

    async goToProductRelations() {
        await this.page.getByRole('link', { name: 'Product Relations' }).click();
    }

    async goToSpecificProductMenu(productName : String) {
        await this.page.getByRole('link', {name: productName}).click();
    }
}