/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

/**
 * Since json-loader isn't supported in 7.3.x, this is a copy of
 * sxp-query-element.schema.json as a JavaScript object so it can be imported
 * by another JavaScript file.
 */
export default {
	$id: 'advanced-configuration.schema.json',
	$schema: 'http://json-schema.org/draft-07/schema#',
	properties: {
		source: {
			properties: {
				excludes: {
					items: {
						type: 'string',
					},
					type: 'array',
				},
				fetchSource: {
					type: 'boolean',
				},
				includes: {
					items: {
						type: 'string',
					},
					type: 'array',
				},
			},
			type: 'object',
		},
	},
	type: 'object',
};
