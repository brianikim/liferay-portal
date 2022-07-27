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
	$id: 'sort-configuration.schema.json',
	$schema: 'http://json-schema.org/draft-07/schema#',
	definitions: {
		Geopoint: {
			items: {
				maxItems: 2,
				minItems: 2,
				type: ['string', 'number'],
			},
			type: 'array',
		},
		NestedSort: {
			properties: {
				filter: {
					type: 'object',
				},
				nested: {
					$ref: '#/definitions/NestedSort',
				},
				path: {
					type: 'string',
				},
			},
			required: ['path'],
			type: 'object',
		},
		Script: {
			anyOf: [
				{
					properties: {
						id: {
							type: 'string',
						},
						params: {
							type: 'object',
						},
					},
					required: ['id'],
					type: 'object',
				},
				{
					properties: {
						_options: {
							type: 'object',
						},
						lang: {
							enum: [
								'expression',
								'java',
								'mustache',
								'painless',
							],
							type: 'string',
						},
						params: {
							type: 'object',
						},
						source: {
							type: 'string',
						},
					},
					required: ['source'],
					type: 'object',
				},
			],
			type: 'object',
		},
		Sort: {
			properties: {
				missing: {
					type: 'string',
				},
				mode: {
					enum: ['avg', 'max', 'median', 'min', 'sum'],
					type: 'string',
				},
				nested: {
					$ref: '#/definitions/NestedSort',
				},
				order: {
					$ref: '#/definitions/SortOrder',
				},
			},
			type: ['string', 'object'],
		},
		SortOrder: {
			enum: ['asc', 'desc'],
			type: 'string',
		},
		Sorts: {
			properties: {
				_geo_distance: {
					allOf: [
						{
							$ref: '#/definitions/Sort',
						},
					],
					properties: {
						distance_type: {
							enum: ['arc', 'plane'],
							type: 'string',
						},
						field: {
							type: 'string',
						},
						locations: {
							items: {
								$ref: '#/definitions/Geopoint',
							},
							type: 'array',
						},
						unit: {
							enum: [
								'cm',
								'ft',
								'in',
								'km',
								'm',
								'mi',
								'mm',
								'yd',
							],
							type: 'string',
						},
					},
					required: ['field', 'locations'],
					type: 'object',
				},
				_score: {
					$ref: '#/definitions/Sort',
				},
				_script: {
					allOf: [
						{
							$ref: '#/definitions/Sort',
						},
					],
					properties: {
						script: {
							$ref: '#/definitions/Script',
						},
						type: {
							enum: ['number', 'string'],
							type: 'string',
						},
					},
					required: ['script', 'type'],
					type: 'object',
				},
			},
			type: 'object',
		},
	},
	properties: {
		sorts: {
			items: {
				$ref: '#/definitions/Sorts',
			},
			type: 'array',
		},
	},
	type: 'object',
};
