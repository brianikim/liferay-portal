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
	readonly modalFieldTypeSelect: Locator;
	readonly modalFrameLocator: FrameLocator;
	readonly modalKeyInput: Locator;
	readonly modalNameInput: Locator;
	readonly modalSubmitButton: Locator;
	readonly nameInput: Locator;
	readonly optionLink: (optionName: string) => Locator;
	readonly optionValueCustomFieldInput: (customFieldName: string) => Locator;
	readonly optionValueSidePanelFrame: FrameLocator;
	readonly optionValueSidePanelSaveButton: Locator;
	readonly page: Page;
	readonly rowActionsButton: (rowName: string) => Locator;
	readonly rowActionsMenuItem: (action: string) => Locator;

	constructor(page: Page) {
		this.addOptionButton = page
			.getByTestId('managementToolbar')
			.locator('[data-testid="fdsCreationActionButton"]');
		this.fieldTypeSelect = page.getByLabel('Option Field Type Required');
		this.keyInput = page.getByLabel('Key Required');
		this.managementToolbarSearchInput = page
			.getByTestId('managementToolbar')
			.getByPlaceholder('Search', {exact: true});
		this.modalFrameLocator = page.frameLocator('.fds-modal-body iframe');
		this.modalFieldTypeSelect = this.modalFrameLocator.getByLabel(
			'Option Field Type Required'
		);
		this.modalKeyInput = this.modalFrameLocator.getByLabel('Key Required');
		this.modalNameInput =
			this.modalFrameLocator.getByLabel('Name Required');
		this.modalSubmitButton = this.modalFrameLocator.getByRole('button', {
			exact: true,
			name: 'Submit',
		});
		this.nameInput = page.getByLabel('Name Required');
		this.optionLink = (optionName: string) =>
			page.getByRole('link', {exact: true, name: optionName});
		this.optionValueSidePanelFrame = page.frameLocator(
			'.fds-side-panel.is-visible iframe'
		);
		this.optionValueCustomFieldInput = (customFieldName: string) =>
			this.optionValueSidePanelFrame.locator(
				`input[id$="_${customFieldName}"]`
			);
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
	}

	async createOption(fieldType: string, key: string, name: string) {
		await this.addOptionButton.click();

		await this.modalNameInput.fill(name);
		await this.modalFieldTypeSelect.selectOption({label: fieldType});
		await this.modalKeyInput.fill(key);

		await this.modalSubmitButton.click();

		await expect(this.keyInput).toHaveValue(key);
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

		await waitForAlert(this.optionValueSidePanelFrame);
	}
}
