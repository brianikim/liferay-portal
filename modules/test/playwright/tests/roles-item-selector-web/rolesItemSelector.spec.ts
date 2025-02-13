/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {loginTest} from '../../fixtures/loginTest';
import {productMenuPageTest} from '../../fixtures/productMenuPageTest';
import {rolesItemSelectorPagesTest} from '../../fixtures/rolesItemSelectorPagesTest';
import {segmentEditorPagesTest} from '../../fixtures/segmentEditorPagesTest';
import getRandomString from '../../utils/getRandomString';

export const test = mergeTests(
	loginTest(),
	productMenuPageTest,
	rolesItemSelectorPagesTest,
	segmentEditorPagesTest
);

test('LPD-48734 Multiple roles can be added using the item selector', async ({
	page,
	productMenuPage,
	rolesItemSelectorPage,
	segmentEditorPage,
}) => {
	await productMenuPage.openProductMenuIfClosed();

	await productMenuPage.goToSegments();

	await page.getByRole('link', {name: 'Add New User Segment'}).click();

	await page.getByText('No Conditions yet').waitFor();

	await segmentEditorPage.createSegment(getRandomString(), false, false, {
		user: ['Regular Role'],
	});

	await page.locator('body').click();

	await page.getByRole('button', {name: 'Select'}).click();

	await expect(
		rolesItemSelectorPage.roleLabel('Analytics Administrator')
	).toBeVisible();

	await rolesItemSelectorPage.selectPaginationItemsPerPage(4);

	await expect(
		rolesItemSelectorPage.rolesItemSelector.getByText(
			'Showing 1 to 4 of 9 entries.'
		)
	).toBeVisible();

	await rolesItemSelectorPage.roleLabel('Analytics Administrator').click();

	await rolesItemSelectorPage.selectPaginationPageNumber(2);

	await rolesItemSelectorPage.roleLabel('Publications User').click();

	await rolesItemSelectorPage.selectButton.click();

	await expect(
		page.locator(
			'[aria-label="Regular Role: Select Option"][value="Analytics Administrator"]'
		)
	).toBeVisible();

	await expect(
		page.locator(
			'[aria-label="Regular Role: Select Option"][value="Publications User"]'
		)
	).toBeVisible();
});
