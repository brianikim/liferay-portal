/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// @ts-ignore

import {Locator, Page} from '@playwright/test';

import {clickAndExpectToBeVisible} from '../../utils/clickAndExpectToBeVisible';
import {ApplicationsMenuPage} from '../product-navigation-applications-menu/ApplicationsMenuPage';

export const searchTableRowByValue = async function (
	tableLocator: Locator,
	colPosition: number,
	value: string,
	strictEqual: boolean = false
) {
	await tableLocator.elementHandle();

	const rows = await tableLocator.getByRole('row').all();

	for await (const row of rows) {
		const column = row.getByRole('cell').nth(colPosition).first();

		const colValue = (await column.allInnerTexts()).join('');

		if (
			(strictEqual && colValue === value) ||
			(!strictEqual &&
				colValue.toLowerCase().indexOf(value.toLowerCase()) >= 0)
		) {
			return {column, row};
		}
	}

	throw new Error(`Cannot locate table row with value ${value}`);
};

export class UsersAndOrganizationsPage {
	readonly applicationsMenuPage: ApplicationsMenuPage;
	readonly contactLink: Locator;
	readonly editOrgLaborIconMenu: Locator;
	readonly optionsMenu: Locator;
	readonly page: Page;
	readonly pageTitle: Locator;
	readonly exportUsersOptionsMenuItem: Locator;
	readonly manageCustomFieldsOptionsMenuItem: Locator;
	readonly openingHoursLink: Locator;
	readonly organizationActionsMenu: (
		organizationName: string
	) => Promise<Locator>;
	readonly organizationEditMenuItem: Locator;
	readonly organizationsLink: Locator;
	readonly organizationsTable: Locator;
	readonly organizationsTableRow: (
		colPosition: number,
		value: string,
		strictEqual?: boolean
	) => Promise<{column: Locator; row: Locator}>;
	readonly orgLaborListTypeSelectedValue: Locator;
	readonly exportImportOptionsMenuItem: Locator;
	readonly usersTableRow: (
		colPosition: number,
		value: string,
		strictEqual?: boolean
	) => Promise<{column: Locator; row: Locator}>;
	readonly usersTableRowLink: (screenName: string) => Promise<Locator>;
	readonly usersLink: Locator;
	readonly usersTable: Locator;

	constructor(page: Page) {
		this.applicationsMenuPage = new ApplicationsMenuPage(page);
		this.contactLink = page.getByRole('link', {name: 'Contact'});
		this.editOrgLaborIconMenu = page.getByTestId('editOrgLaborIconMenu');
		this.exportImportOptionsMenuItem = page.getByRole('menuitem', {
			name: 'Export / Import',
		});
		this.exportUsersOptionsMenuItem = page.getByRole('menuitem', {
			name: 'Export Users',
		});
		this.manageCustomFieldsOptionsMenuItem = page.getByRole('menuitem', {
			name: 'Manage Custom Fields',
		});
		this.openingHoursLink = page.getByRole('link', {name: 'Opening Hours'});
		this.optionsMenu = page
			.getByTestId('headerOptions')
			.getByLabel('Options');
		this.organizationActionsMenu = async (organizationName: string) => {
			const organizationsTableRow = await this.organizationsTableRow(
				1,
				organizationName,
				true
			);

			if (organizationsTableRow && organizationsTableRow.row) {
				const organizationActionsMenu =
					organizationsTableRow.row.getByLabel('Show Actions');

				if (organizationActionsMenu) {
					return organizationActionsMenu;
				}
			}
			else {
				throw new Error(
					`Cannot locate organization row with organizationName ${organizationName}`
				);
			}

			throw new Error(`Cannot locate button with label: Show Actions`);
		};
		this.organizationEditMenuItem = page.getByRole('menuitem', {
			name: 'Edit',
		});
		this.organizationsLink = page.getByRole('link', {
			name: 'Organizations',
		});
		this.organizationsTable = page.locator(
			'#_com_liferay_users_admin_web_portlet_UsersAdminPortlet_organizationsSearchContainer'
		);
		this.organizationsTableRow = async (
			colPosition: number,
			value: string,
			strictEqual: boolean = false
		) => {
			return await searchTableRowByValue(
				this.organizationsTable,
				colPosition,
				value,
				strictEqual
			);
		};
		this.orgLaborListTypeSelectedValue = page
			.locator(
				'#_com_liferay_users_admin_web_portlet_UsersAdminPortlet_orgLaborListTypeId'
			)
			.locator('option[selected=""]');
		this.page = page;
		this.pageTitle = page.getByTestId('headerTitle');
		this.usersTableRow = async (
			colPosition: number,
			value: string,
			strictEqual: boolean = false
		) => {
			return await searchTableRowByValue(
				this.usersTable,
				colPosition,
				value,
				strictEqual
			);
		};
		this.usersTableRowLink = async (screenName: string) => {
			const usersTableRow = await this.usersTableRow(2, screenName, true);

			if (usersTableRow && usersTableRow.column) {
				return usersTableRow.column.getByRole('link', {
					name: screenName,
				});
			}

			throw new Error(
				`Cannot locate user row with screenName ${screenName}`
			);
		};
		this.usersLink = page.getByRole('link', {name: 'Users'});
		this.usersTable = page.locator(
			'#_com_liferay_users_admin_web_portlet_UsersAdminPortlet_usersSearchContainer'
		);
	}

	async goto() {
		await this.applicationsMenuPage.goToUsersAndOrganizations();
	}

	async gotoOrganizationEditOpeningHoursTab(organizationName: string) {
		await (await this.organizationActionsMenu(organizationName)).click();
		await this.organizationEditMenuItem.click();
		await this.contactLink.click();
		await this.openingHoursLink.click();
		await clickAndExpectToBeVisible({
			autoClick: true,
			target: this.organizationEditMenuItem,
			trigger: this.editOrgLaborIconMenu,
		});
	}

	async goToOrganizations() {
		await this.goto();
		await Promise.all([
			this.organizationsLink.click(),
			this.page.waitForResponse(
				(resp) =>
					resp.status() === 200 &&
					resp
						.url()
						.includes('screenNavigationCategoryKey=organizations')
			),
		]);
	}

	async goToUsers() {
		await this.goto();
		await Promise.all([
			this.usersLink.click(),
			this.page.waitForResponse(
				(resp) =>
					resp.status() === 200 &&
					resp.url().includes('screenNavigationCategoryKey=users')
			),
		]);
	}

	async openOptionsMenu() {
		await this.optionsMenu
			.and(this.page.locator('[aria-haspopup]'))
			.click();
	}
}
