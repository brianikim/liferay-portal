/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {CommerceLayoutsPage} from '../pages/commerce/layouts.page';
import {SpecificationFacetsPage} from '../pages/commerce/specificationFacets.page';

const commercePagesTest = test.extend<{
	_layoutsPage: CommerceLayoutsPage;
	_specificationFacetsPage: SpecificationFacetsPage;
}>({
	_layoutsPage: async ({page}, use) => {
		await use(new CommerceLayoutsPage(page));
	},
	_specificationFacetsPage: async ({page}, use) => {
		await use(new SpecificationFacetsPage(page));
	},
});

export {commercePagesTest};
