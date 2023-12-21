/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ApiHelpers} from './ApiHelpers';

export class DataHelper {
	constructor(page) {
		this.apiHelpers = new ApiHelpers(this, page);
		this.data = [];
	}

	addDataObject(dataObject) {
		if (dataObject && dataObject.handleDelete && dataObject.id) {
			this.data.push(dataObject);
		}
	}

	async clearData() {
		await Promise.all(
			this.data.map((dataObject) => {
				return dataObject.handleDelete(this.apiHelpers, dataObject.id);
			})
		);

		this.data = [];
	}
}
