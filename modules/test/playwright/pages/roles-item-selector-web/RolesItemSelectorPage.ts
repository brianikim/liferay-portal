/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page, expect} from '@playwright/test';

export class RolesItemSelectorPage {
	readonly page: Page;
	readonly roleLabel: (roleLabel: string) => Locator;
	readonly rolesItemSelector: FrameLocator;
	readonly rolesItemSelectorLabel: Locator;
	readonly rolesPaginationBar: Locator;
	readonly rolesPaginationItemsPerPageDropdownButton: Locator;
	readonly rolesPaginationItemsPerPageDropdownEntry: (
		delta: number
	) => Locator;
	readonly selectButton: Locator;

	constructor(page: Page) {
		this.page = page;
		this.roleLabel = (roleLabel: string) =>
			this.rolesItemSelector.getByLabel(roleLabel);
		this.rolesItemSelector = page.frameLocator(
			'iframe[title="Select Role"]'
		);
		this.rolesItemSelectorLabel = page.getByLabel('Select Role');
		this.rolesPaginationBar = this.rolesItemSelector.locator('.pagination');
		this.rolesPaginationItemsPerPageDropdownButton =
			this.rolesItemSelector.getByLabel('Items per Page');
		this.rolesPaginationItemsPerPageDropdownEntry = (delta: number) =>
			this.rolesItemSelector.getByRole('option', {
				name: `${delta.toString()}  Entries per Page`,
			});
		this.selectButton = this.rolesItemSelectorLabel.getByRole('button', {
			name: 'Select',
		});
	}

	async selectPaginationItemsPerPage(delta: number) {
		await this.rolesPaginationItemsPerPageDropdownButton.click();
		await this.rolesPaginationItemsPerPageDropdownEntry(delta).click();
	}

	async selectPaginationPageNumber(pageNumber: number) {
		await this.rolesPaginationBar
			.getByText(pageNumber.toString())
			.first()
			.click();

		await expect(
			this.rolesPaginationBar.getByText(pageNumber.toString()).first()
		).toHaveAttribute('aria-current', 'page');
	}
}
