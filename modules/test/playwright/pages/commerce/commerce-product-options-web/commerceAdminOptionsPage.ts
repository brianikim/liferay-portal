/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page, expect} from '@playwright/test';

import {waitForAlert} from '../../../utils/waitForAlert';

export class CommerceAdminOptionsPage {
	readonly addOptionButton: Locator;
	readonly modalFieldTypeSelect: Locator;
	readonly modalKeyInput: Locator;
	readonly modalNameInput: Locator;
	readonly modalSaveButton: Locator;
	readonly optionActionsButton: (optionName: string) => Locator;
	readonly optionLink: (optionName: string) => Locator;
	readonly optionNameInput: Locator;
	readonly optionValueActionsButton: (optionValueName: string) => Locator;
	readonly optionValueSidePanelFrame: FrameLocator;
	readonly page: Page;
	readonly rowActionsMenuItem: (action: string) => Locator;

	constructor(page: Page) {
		this.page = page;

		this.addOptionButton = page.getByRole('button', {
			exact: true,
			name: 'Add Option',
		});
		this.modalNameInput = page.getByLabel('Name', {exact: true});
		this.modalFieldTypeSelect = page.getByLabel('Option Field Type');
		this.modalKeyInput = page.getByLabel('Key', {exact: true});
		this.modalSaveButton = page.getByRole('button', {
			exact: true,
			name: 'Save',
		});
		this.optionActionsButton = (optionName: string) =>
			page.getByRole('button', {
				exact: true,
				name: `${optionName} Actions`,
			});
		this.optionLink = (optionName: string) =>
			page.getByRole('link', {exact: true, name: optionName});
		this.optionNameInput = page.getByLabel('Name', {exact: true});
		this.optionValueActionsButton = (optionValueName: string) =>
			page.getByRole('button', {
				exact: true,
				name: `${optionValueName} Actions`,
			});
		this.optionValueSidePanelFrame = page.frameLocator(
			'.fds-side-panel.is-visible iframe'
		);
		this.rowActionsMenuItem = (action: string) =>
			page.getByRole('menuitem', {exact: true, name: action});
	}

	async createOption(name: string, fieldType: string, key: string) {
		await this.addOptionButton.click();

		await this.modalNameInput.fill(name);
		await this.modalFieldTypeSelect.selectOption({label: fieldType});
		await this.modalKeyInput.fill(key);

		await this.modalSaveButton.click();

		await waitForAlert(this.page);
	}

	async deleteOption(optionName: string) {
		this.page.once('dialog', (dialog) => dialog.accept());

		await this.optionActionsButton(optionName).click();
		await this.rowActionsMenuItem('Delete').click();

		await waitForAlert(this.page);
	}

	async setOptionValueCustomField(
		optionValueName: string,
		customFieldName: string,
		customFieldValue: string
	) {
		await this.optionValueActionsButton(optionValueName).click();
		await this.rowActionsMenuItem('Edit').click();

		const customFieldInput =
			this.optionValueSidePanelFrame.getByLabel(customFieldName);

		await expect(customFieldInput).toBeVisible();

		await customFieldInput.fill(customFieldValue);

		await this.optionValueSidePanelFrame
			.getByRole('button', {exact: true, name: 'Save'})
			.click();

		await waitForAlert(this.page);
	}
}
