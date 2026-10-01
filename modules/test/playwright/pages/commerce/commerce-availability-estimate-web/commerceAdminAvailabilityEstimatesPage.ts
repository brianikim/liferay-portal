/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Locator, Page} from '@playwright/test';

import {waitForAlert} from '../../../utils/waitForAlert';
import {GlobalMenuPage} from '../../product-navigation-applications-menu/GlobalMenuPage';

export class CommerceAdminAvailabilityEstimatesPage {
	readonly addAvailabilityEstimateLink: Locator;
	readonly availabilityEstimateActionsButton: (title: string) => Locator;
	readonly availabilityEstimateLink: (title: string) => Locator;
	readonly deleteMenuItem: Locator;
	readonly globalMenuPage: GlobalMenuPage;
	readonly page: Page;
	readonly saveButton: Locator;
	readonly titleInput: Locator;

	constructor(page: Page) {
		this.addAvailabilityEstimateLink = page.getByRole('link', {
			name: 'Add Availability Estimate',
		});
		this.availabilityEstimateActionsButton = (title: string) =>
			page.getByRole('row').filter({hasText: title}).getByRole('button');
		this.availabilityEstimateLink = (title: string) =>
			page.getByRole('link', {exact: true, name: title});
		this.deleteMenuItem = page.getByRole('menuitem', {
			exact: true,
			name: 'Delete',
		});
		this.globalMenuPage = new GlobalMenuPage(page);
		this.page = page;
		this.saveButton = page.getByRole('button', {exact: true, name: 'Save'});
		this.titleInput = page.getByLabel('Title', {exact: true});
	}

	async addAvailabilityEstimate(title: string) {
		await this.addAvailabilityEstimateLink.click();

		await this.titleInput.fill(title);

		await this.saveButton.click();

		await waitForAlert(this.page);
	}

	async deleteAvailabilityEstimate(title: string) {
		this.page.once('dialog', (dialog) => dialog.accept());

		await this.availabilityEstimateActionsButton(title).click();
		await this.deleteMenuItem.click();

		await waitForAlert(this.page);
	}

	async goto() {
		await this.globalMenuPage.goToCommerce('Availability Estimates');
	}
}
