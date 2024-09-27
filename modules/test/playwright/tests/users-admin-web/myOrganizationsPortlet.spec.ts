/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {dataApiHelpersTest} from '../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../fixtures/loginTest';
import {usersAndOrganizationsPagesTest} from '../../fixtures/usersAndOrganizationsPagesTest';
import {UsersAndOrganizationsPage} from '../../pages/users-admin-web/UsersAndOrganizationsPage';
import getRandomString from '../../utils/getRandomString';

export const test = mergeTests(
	dataApiHelpersTest,
	loginTest(),
	usersAndOrganizationsPagesTest
);

test('LPD-35306 Breadcrumb in My Organizations does not have a link if user does not have view permission of the Organization', async ({
	apiHelpers,
	context,
	usersAndOrganizationsPage,
}) => {
	const organization = await apiHelpers.headlessAdminUser.postOrganization();
	const organization2 = await apiHelpers.headlessAdminUser.postOrganization({
		parentOrganization: {
			externalReferenceCode: organization.externalReferenceCode,
		},
	});
	const organization3 = await apiHelpers.headlessAdminUser.postOrganization({
		parentOrganization: {
			externalReferenceCode: organization2.externalReferenceCode,
		},
	});

	const user = await apiHelpers.headlessAdminUser.postUserAccount();

	await apiHelpers.headlessAdminUser.assignUserToOrganizationByEmailAddress(
		organization2.id,
		user.emailAddress
	);

	const role = await apiHelpers.headlessAdminUser.getRoleByName(
		'Organization Administrator'
	);

	await apiHelpers.headlessAdminUser.assignUserToOrganizationRole(
		String(role.id),
		user.id,
		organization2.id
	);

	await usersAndOrganizationsPage.goToUsers();

	await (
		await usersAndOrganizationsPage.usersTableRowActions(
			`${user.alternateName}`
		)
	).click();

	const pagePromise = context.waitForEvent('page');

	await usersAndOrganizationsPage.impersonateUserMenuItem.click();

	const newPage = await pagePromise;
	const newPageUsersAndOrganizationsPage = new UsersAndOrganizationsPage(
		newPage
	);

	await newPageUsersAndOrganizationsPage.goToMyOrganizations();
	await (
		await newPageUsersAndOrganizationsPage.myOrganizationsTableRowLink(
			organization3.name
		)
	).click();

	await expect(
		await newPageUsersAndOrganizationsPage.myOrganizationsBreadcrumbLink(
			organization.name
		)
	).toHaveCount(0);

	await expect(
		await newPageUsersAndOrganizationsPage.myOrganizationsBreadcrumbLink(
			organization2.name
		)
	).toHaveCount(1);
});

test('LPD-37376 Suborganizations in My Organizations portlet should be able to be edited with the "Update Suborganizations" permission', async ({
	apiHelpers,
	context,
	page,
	usersAndOrganizationsPage,
}) => {
	const organization = await apiHelpers.headlessAdminUser.postOrganization();
	const organization2 = await apiHelpers.headlessAdminUser.postOrganization({
		parentOrganization: {
			externalReferenceCode: organization.externalReferenceCode,
		},
	});

	const user = await apiHelpers.headlessAdminUser.postUserAccount();

	await apiHelpers.headlessAdminUser.assignUserToOrganizationByEmailAddress(
		organization.id,
		user.emailAddress
	);

	const companyId = await page.evaluate(() => {
		return Liferay.ThemeDisplay.getCompanyId();
	});

	const role = await apiHelpers.headlessAdminUser.postRole({
		name: getRandomString(),
		rolePermissions: [
			{
				actionIds: [
					'MANAGE_ORGANIZATIONS',
					'MANAGE_USERS',
					'UPDATE',
					'VIEW',
					'VIEW_ORGANIZATIONS',
				],
				primaryKey: companyId,
				resourceName: 'com.liferay.account.model.AccountEntry',
				scope: 1,
			},
			{
				actionIds: [
					'MANAGE_AVAILABLE_ACCOUNTS',
					'UPDATE_SUBORGANIZATIONS',
				],
				primaryKey: companyId,
				resourceName: 'com.liferay.portal.kernel.model.Organization',
				scope: 1,
			},
			{
				actionIds: ['ACCESS_IN_CONTROL_PANEL'],
				primaryKey: companyId,
				resourceName:
					'com_liferay_account_admin_web_internal_portlet_AccountEntriesAdminPortlet',
				scope: 1,
			},
		],
		roleType: 'organization',
	});

	await apiHelpers.headlessAdminUser.assignUserToOrganizationRole(
		role.id,
		user.id,
		organization.id
	);

	await usersAndOrganizationsPage.goToUsers();

	await (
		await usersAndOrganizationsPage.usersTableRowActions(
			`${user.alternateName}`
		)
	).click();

	const pagePromise = context.waitForEvent('page');

	await usersAndOrganizationsPage.impersonateUserMenuItem.click();

	const newPage = await pagePromise;
	const newPageUsersAndOrganizationsPage = new UsersAndOrganizationsPage(
		newPage
	);

	await newPageUsersAndOrganizationsPage.goToMyOrganizations();

	await expect(
		usersAndOrganizationsPage.myOrganizationsTableRowLink(organization)
	).toBeVisible();

	await expect(
		usersAndOrganizationsPage.myOrganizationsTableRowLink(organization2)
	).toBeVisible();
});
