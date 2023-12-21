/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {CommerceLayoutsPage} from './layouts.page';

export class SpecificationFacetsPage {
	constructor(page) {
		this.searchOptionsConfiguration = page
			.frameLocator(
				'iframe[title="\\a \\9 \\9 \\9 \\9 \\9 Search Options\\a \\9 \\9 \\9 \\9  - Configuration"]'
			);
		this.searchOptionsConfigurationEditButton = page.getByRole('link', {
			name: 'Configure additional search options in this page.',
		});

		this.specificationFacetConfigurationEditButton = page.locator('#portlet-topper-toolbar_com_liferay_commerce_product_content_search_web_internal_portlet_CPSpecificationOptionFacetsPortlet');

		this.specificationFacetConfiguration = page
			.frameLocator('iframe[title="\\a \\9 \\9 \\9 \\9 \\9 Specification Facet\\a \\9 \\9 \\9 \\9  - Configuration"]');

		this.layoutsPage = new CommerceLayoutsPage(page);
		this.page = page;
	}

	async configureSearchOptions() {
		await this.searchOptionsConfigurationEditButton.click();
		await this.searchOptionsConfiguration.getByText(
			'Allow Empty Searches When enabled, an empty search query returns all available a'
		).click();
		await this.searchOptionsConfiguration.getByRole('button', { name: 'Save' }).click();
		await this.page.reload();
	}

	async configureSpecificationFacet() {
		await this.specificationFacetConfigurationEditButton.getByLabel('Options').click();
		await 
		await this.specificationFacetConfiguration.getByLabel('Order Specifications By').click();
		await this.specificationFacetConfiguration.getByLabel('Order Specifications By').selectOption('priority:desc');
		await this.specificationFacetConfiguration.getByRole('button', { name: 'Save' }).click();
		await this.page.reload();
	}

	async goto() {
		await this.layoutsPage.goto();
	}
}
