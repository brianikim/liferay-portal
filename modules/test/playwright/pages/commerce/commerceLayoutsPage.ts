/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

export class CommerceLayoutsPage {
	readonly addPageButton: Locator;
	readonly addPageModalSubmitButton: Locator;
	readonly addPageNameInput: Locator;
	readonly closeProductMenuButton: Locator;
	readonly createPageMenuItem: Locator;
	readonly deleteLayoutModal: Locator;
	readonly deletePageButton: Locator;
	readonly openProductMenuButton: Locator;
	readonly page: Page;
	readonly pagesMenuItem: Locator;
	readonly pageLabel: Locator;
	readonly selectSpecificationFacetPageInput: Locator;
	readonly siteBuilderMenuItem: Locator;
	readonly widgetPageTemplateButton: Locator;

	constructor(page: Page) {
		this.addPageButton = page.getByTestId('creationMenuNewButton');
		this.addPageModalSubmitButton = page
			.frameLocator('#addLayoutDialog_iframe_')
			.getByTestId('addLayoutFooter')
			.getByRole('button', {exact: true, name: 'Add'});
		this.addPageNameInput = page
			.frameLocator('#addLayoutDialog_iframe_')
			.getByTestId('addPageNameInput');
		this.closeProductMenuButton = page.getByRole('tab', {
			exact: true,
			name: 'Close Product Menu',
		});
		this.createPageMenuItem = page
			.getByTestId('dropdownMenu')
			.getByRole('menuitem', {
				exact: true,
				name: 'Page',
			});
		this.deleteLayoutModal = page.locator('#deleteLayoutModalDeleteButton');
		this.deletePageButton = page
			.getByTestId('actionDropdownItem')
			.getByRole('button', {
				exact: true,
				name: 'Delete',
			});
		this.openProductMenuButton = page.getByRole('tab', {
			exact: true,
			name: 'Open Product Menu',
		});
		this.page = page;
		this.pagesMenuItem = page.getByTestId('app').filter({hasText: 'Pages'});
		this.pageLabel = page
			.getByTestId('layoutHref')
			.getByLabel('Specification Facet Page', {exact: true});
		this.selectSpecificationFacetPageInput = page
			.getByTestId('selectLayout')
			.getByLabel('Select Specification Facet Page');
		this.siteBuilderMenuItem = page
			.getByTestId('appGroup')
			.filter({hasText: 'Site Builder'});
		this.widgetPageTemplateButton = page
			.getByTestId('cardPageItemDirectory')
			.getByRole('button', {
				exact: true,
				name: 'Widget Page',
			});
	}

	async createWidgetPage(pageName) {
		await this.goToPages();
		await this.addPageButton.click();
		await this.createPageMenuItem.click();
		await this.widgetPageTemplateButton.click();
		await this.addPageNameInput.click();
		await this.addPageNameInput.fill(pageName);
		await this.addPageModalSubmitButton.click();
	}

	async deleteSpecificationPage() {
		await this.goToPages();
		await this.selectSpecificationFacetPageInput.click();
		await this.deletePageButton.click();
		await this.deleteLayoutModal.click();
	}

	async goto() {
		await this.page.goto('/');
	}

	async goToPages() {
		await this.goto();

		if (await this.closeProductMenuButton.isVisible()) {
			await this.siteBuilderMenuItem.click();
			await this.pagesMenuItem.click();
		}
		else if (await this.openProductMenuButton.isVisible()) {
			await this.openProductMenuButton.click();
			await this.siteBuilderMenuItem.click();
			await this.pagesMenuItem.click();
		}
	}
}
