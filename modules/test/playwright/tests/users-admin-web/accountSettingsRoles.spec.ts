/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {accountSettingsPageTest} from '../../fixtures/accountSettingsPagesTest';
import {loginTest} from '../../fixtures/loginTest';

import {expect, mergeTests} from '@playwright/test';

export const test = mergeTests(accountSettingsPageTest, loginTest);

test('LPD-15689 - roles in account settings should have no save button', async ({
    accountSettingsPage
}) => {
    await accountSettingsPage.goToAccountSettingsRoles();

    await expect(
        accountSettingsPage.saveButton
    ).toBeHidden({
        timeout: 8 * 1000,
    });

})