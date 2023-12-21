/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ApplicationsMenuPage} from '../product-navigation-applications-menu/applicationsMenu.page';

export class PaymentsPage {
	constructor(page) {
		this.applicationsMenuPage = new ApplicationsMenuPage(page);
		this.page = page;
		this.title = page.getByRole('heading', { name: 'Payments' })
	}

	async goto() {
		await this.applicationsMenuPage.goToPayments();
	}
}
