/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {CommerceLayoutsPage} from './layouts.page';

export class SpecificationFacetsPage {
	readonly addSearchOptionsLabel: Locator;
	readonly addSpecificationFacetLabel: Locator;
	readonly addWidgetButton: Locator;
	readonly layoutsPage: CommerceLayoutsPage;
	readonly page: Page;
	readonly pageLabel: Locator;
	readonly searchFormInput: Locator;
	readonly searchOptionsAllowEmptySearchesInput: Locator;
	readonly searchOptionsConfigurationEditButton: Locator;
	readonly searchOptionsConfigurationSaveButton: Locator;

	constructor(page: Page) {
		this.addSearchOptionsLabel = page
			.getByTestId('addPanelTabItem')
			.filter({hasText: /^Search Options$/})
			.getByRole('button', {exact: true, name: 'Add Content'});
		this.addSpecificationFacetLabel = page
			.getByTestId('addPanelTabItem')
			.filter({hasText: /^Specification Facet$/})
			.getByRole('button', {exact: true, name: 'Add Content'});
		this.addWidgetButton = page.getByTestId('add');
		this.layoutsPage = new CommerceLayoutsPage(page);
		this.page = page;
		this.pageLabel = page
			.getByTestId('layoutHref')
			.filter({hasText: /^Specification Facet Page$/});
		this.searchFormInput = page.getByRole('textbox', {
			name: 'Search Form',
		});
		this.searchOptionsAllowEmptySearchesInput = page
			.frameLocator('#modalIframe')
			.getByTestId('allowEmptySearches');
		this.searchOptionsConfigurationEditButton =
			page.getByTestId('searchOptionsHref');
		this.searchOptionsConfigurationSaveButton = page
			.frameLocator('#modalIframe')
			.getByTestId('searchOptionsFooter')
			.getByRole('button', {exact: true, name: 'Save'});
	}

	async addSearchOptionsWidget() {
		await this.goToPage();
		await this.addWidgetButton.click();
		await this.searchFormInput.fill('Search Options');
		await this.addSearchOptionsLabel.click();
	}

	async addSpecificationFacetWidget() {
		await this.goToPage();
		await this.addWidgetButton.click();
		await this.searchFormInput.fill('Specification Facet');
		await this.addSpecificationFacetLabel.click();
	}

	async configureSearchOptions() {
		await this.searchOptionsConfigurationEditButton.click();
		await this.searchOptionsAllowEmptySearchesInput.click();
		await this.searchOptionsConfigurationSaveButton.click();
	}

	async goto() {
		await this.layoutsPage.goto();
	}

	async goToPage() {
		await this.layoutsPage.goToPages();
		await this.pageLabel.click();
	}
}
