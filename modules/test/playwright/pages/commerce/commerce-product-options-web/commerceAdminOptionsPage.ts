/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page, expect} from '@playwright/test';

import {waitForAlert} from '../../../utils/waitForAlert';

export class CommerceAdminOptionsPage {
	readonly addOptionButton: Locator;
	readonly fieldTypeSelect: Locator;
	readonly keyInput: Locator;
	readonly managementToolbarSearchInput: Locator;
	readonly nameInput: Locator;
	readonly optionLink: (optionName: string) => Locator;
	readonly optionValueCustomFieldInput: (customFieldName: string) => Locator;
	readonly optionValueSidePanelFrame: FrameLocator;
	readonly optionValueSidePanelSaveButton: Locator;
	readonly page: Page;
	readonly rowActionsButton: (rowName: string) => Locator;
	readonly rowActionsMenuItem: (action: string) => Locator;
	readonly saveButton: Locator;

	constructor(page: Page) {
		this.addOptionButton = page.getByRole('button', {
			exact: true,
			name: 'Add Option',
		});
		this.fieldTypeSelect = page.getByLabel('Option Field Type');
		this.keyInput = page.getByLabel('Key', {exact: true});
		this.managementToolbarSearchInput = page
			.getByTestId('managementToolbar')
			.getByPlaceholder('Search', {exact: true});
		this.nameInput = page.getByLabel('Name', {exact: true});
		this.optionLink = (optionName: string) =>
			page.getByRole('link', {exact: true, name: optionName});
		this.optionValueSidePanelFrame = page.frameLocator(
			'.fds-side-panel.is-visible iframe'
		);
		this.optionValueCustomFieldInput = (customFieldName: string) =>
			this.optionValueSidePanelFrame.getByLabel(customFieldName);
		this.optionValueSidePanelSaveButton =
			this.optionValueSidePanelFrame.getByRole('button', {
				exact: true,
				name: 'Save',
			});
		this.page = page;
		this.rowActionsButton = (rowName: string) =>
			page.getByRole('button', {
				exact: true,
				name: `${rowName} Actions`,
			});
		this.rowActionsMenuItem = (action: string) =>
			page.getByRole('menuitem', {exact: true, name: action});
		this.saveButton = page.getByRole('button', {
			exact: true,
			name: 'Save',
		});
	}

	async createOption(fieldType: string, key: string, name: string) {
		await this.addOptionButton.click();

		await this.nameInput.fill(name);
		await this.fieldTypeSelect.selectOption({label: fieldType});
		await this.keyInput.fill(key);

		await this.saveButton.click();

		await waitForAlert(this.page);
	}

	async deleteOption(optionName: string) {
		this.page.once('dialog', (dialog) => dialog.accept());

		await this.rowActionsButton(optionName).click();
		await this.rowActionsMenuItem('Delete').click();

		await waitForAlert(this.page);
	}

	async search(optionName: string) {
		await this.managementToolbarSearchInput.fill(optionName);
		await this.managementToolbarSearchInput.press('Enter');
	}

	async setOptionValueCustomField(
		optionValueName: string,
		customFieldName: string,
		customFieldValue: string
	) {
		await this.rowActionsButton(optionValueName).click();
		await this.rowActionsMenuItem('Edit').click();

		await expect(
			this.optionValueCustomFieldInput(customFieldName)
		).toBeVisible();

		await this.optionValueCustomFieldInput(customFieldName).fill(
			customFieldValue
		);

		await this.optionValueSidePanelSaveButton.click();

		await waitForAlert(this.page);
	}
}
