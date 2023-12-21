/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export class ApplicationsMenuPage {
	constructor(page) {
		this.applicationMenuButton = page.getByLabel(
			'Open Applications MenuCtrl+'
		);
		this.commerceButton = page.getByRole('tab', {
			name: 'Commerce',
		});
		this.controlPanelButton = page.getByRole('tab', {
			name: 'Control Panel',
		});
		this.instanceSettingsMenuItem = page.getByRole('menuitem', {
			name: 'Instance Settings',
		});
		this.objectsMenuItem = page.getByRole('menuitem', {
			exact: true,
			name: 'Objects',
		});
		this.page = page;
		this.paymentsMenuItem = page.getByRole('menuitem', {name: 'Payments'});
		this.signInButton = page.getByRole('button', {name: 'Sign In'});
	}

	async goto() {
		await this.page.goto('/');
	}

	async goToCommerce() {
		await this.goto();
		await this.applicationMenuButton.click();
		await this.commerceButton.click();
	}

	async goToControlPanel() {
		await this.goto();
		await this.applicationMenuButton.click();
		await this.controlPanelButton.click();
	}

	async goToInstanceSettings() {
		await this.goToControlPanel();
		await this.instanceSettingsMenuItem.click();
	}

	async goToObjects() {
		await this.goToControlPanel();
		await this.objectsMenuItem.click();
	}

	async goToPayments() {
		await this.goToCommerce();
		await this.paymentsMenuItem.click();
	}
}
