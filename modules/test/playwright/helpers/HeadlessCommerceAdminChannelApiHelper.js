/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export class HeadlessCommerceAdminChannelApiHelper {
	constructor(apiHelpers, dataHelper) {
		this.apiHelpers = apiHelpers;
		this.basePath = 'headless-commerce-admin-channel/v1.0/';
		this.dataHelper = dataHelper;
	}

	async deleteChannel(
		apiHelpers = this.apiHelpers,
		basePath = this.basePath,
		channelId
	) {
		return apiHelpers.delete(
			`${apiHelpers.baseUrl}${basePath}/channels/${channelId}`
		);
	}

	async getChannel(channelId) {
		return this.apiHelpers.get(
			`${this.apiHelpers.baseUrl}${this.basePath}/channels/${channelId}`
		);
	}

	async postChannel(channel) {
		const postChannel = await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/channels`,
			channel
		);

		this.dataHelper.addDataObject({
			basePath: this.basePath,
			handleDelete: this.deleteChannel,
			id: postChannel.id,
		});

		return postChannel;
	}
}
