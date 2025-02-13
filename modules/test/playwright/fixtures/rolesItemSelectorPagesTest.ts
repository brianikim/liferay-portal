/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {RolesItemSelectorPage} from '../pages/roles-item-selector-web/RolesItemSelectorPage';

const rolesItemSelectorPagesTest = test.extend<{
	rolesItemSelectorPage: RolesItemSelectorPage;
}>({
	rolesItemSelectorPage: async ({page}, use) => {
		await use(new RolesItemSelectorPage(page));
	},
});

export {rolesItemSelectorPagesTest};
