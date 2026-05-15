/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';
import getRandomString from '../../../../utils/getRandomString';
import {performUserSwitch} from '../../../../utils/performLogin';

export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	loginTest()
);

test(
	'Link Catalog to a Supplier is hidden without VIEW_COMMERCE_CATALOGS permission',
	{tag: '@LPD-90454'},
	async ({apiHelpers, commerceAdminCatalogsPage, page}) => {
		const companyId = await page.evaluate(() => {
			return Liferay.ThemeDisplay.getCompanyId();
		});

		const role = await apiHelpers.headlessAdminUser.postRole({
			name: 'Test Role ' + getRandomString(),
			rolePermissions: [
				{
					actionIds: ['VIEW_CONTROL_PANEL'],
					primaryKey: companyId,
					resourceName: '90',
					scope: 1,
				},
				{
					actionIds: [
						'ADD_COMMERCE_CATALOG',
						'VIEW_COMMERCE_CATALOGS',
					],
					primaryKey: companyId,
					resourceName: 'com.liferay.commerce.catalog',
					scope: 1,
				},
				{
					actionIds: ['ACCESS_IN_CONTROL_PANEL', 'VIEW'],
					primaryKey: companyId,
					resourceName:
						'com_liferay_commerce_catalog_web_internal_portlet_CommerceCatalogsPortlet',
					scope: 1,
				},
			],
		});

		const user =
			await apiHelpers.headlessAdminUser.getUserAccountByEmailAddress(
				'demo.unprivileged@liferay.com'
			);

		await apiHelpers.headlessAdminUser.assignUserToRole(
			role.externalReferenceCode,
			user.id
		);

		await performUserSwitch(page, user.alternateName);

		await commerceAdminCatalogsPage.goto();
		await commerceAdminCatalogsPage.addCatalogsButton.click();

		await expect(
			commerceAdminCatalogsPage.modalFrameLocator.getByText(
				'Link Catalog to a Supplier'
			)
		).toBeVisible();

		await performUserSwitch(page, 'test');

		await apiHelpers.jsonWebServicesResourcePermissionApiHelper.removeResourcePermission(
			'VIEW_COMMERCE_CATALOGS',
			companyId,
			'0',
			'com.liferay.commerce.catalog',
			companyId,
			String(role.id),
			'1'
		);

		await performUserSwitch(page, user.alternateName);

		await commerceAdminCatalogsPage.goto();

		await commerceAdminCatalogsPage.addCatalogsButton.click();

		await expect(
			commerceAdminCatalogsPage.modalFrameLocator.getByText(
				'Link Catalog to a Supplier'
			)
		).toBeHidden();
	}
);
