import {expect} from '@playwright/test';

/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export class CommerceLayoutsPage {
	constructor(page) {
		this.addPageButton = page.getByRole('button', {
			name: 'New',
		});
		this.addPageModal = page.frameLocator('iframe[title="Add Page"]');
		this.addPanelSearchForm = page.getByRole('textbox', {
			name: 'Search Form',
		});
		this.addWidgetButton = page.getByLabel('Add', {exact: true});
		this.closeProductMenuButton = page.getByLabel('Close Product Menu');
		this.createPageButton = page.getByRole('menuitem', {
			exact: true,
			name: 'Page',
		});
		this.openProductMenuButton = page.getByLabel('Open Product Menu');
		this.page = page;

		this.pagesButton = page.getByRole('menuitem', {
			name: 'Pages',
		});
		this.siteBuilderButton = page.getByRole('menuitem', {
			name: 'Site Builder',
		});
		this.widgetPageTemplateButton = page.getByRole('button', {
			exact: true,
			name: 'Widget Page',
		});
	}

	async addWidgetsToPage(pageName, widgetNames) {
		await this.goto();
		await this.page
			.getByRole('menuitem', {
				name: pageName,
			})
			.click();

		await expect(
			this.page.getByRole('heading', {name: 'Specification Facet Page'})
		).toBeVisible({timeout: 15000});

		await this.addWidgetButton.click();

		for (const widgetName of widgetNames) {
			await this.addPanelSearchForm.click();
			await this.addPanelSearchForm.fill(widgetName);
			const regexWidgetName = new RegExp(`^${widgetName}$`);

			await this.page
				.locator('li')
				.filter({hasText: regexWidgetName})
				.getByLabel('Add Content')
				.click();
		}
	}

	async createWidgetPage(pageName) {
		await this.goToPages();
		await this.addPageButton.click();
		await this.createPageButton.click();
		await this.widgetPageTemplateButton.click();
		await this.addPageModal.getByLabel('Name').click();
		await this.addPageModal.getByLabel('Name').fill(pageName);
		await this.addPageModal.getByRole('button', {name: 'Add'}).click();
	}

	async goto() {
		await this.page.goto('/');
	}

	async goToPages() {
		await this.goto();
		if (await this.closeProductMenuButton.isVisible()) {
			await this.siteBuilderButton.click();
			await this.pagesButton.click();
		}
		else if (await this.openProductMenuButton.isVisible()) {
			await this.openProductMenuButton.click();
			await this.siteBuilderButton.click();
			await this.pagesButton.click();
		}
	}
}
