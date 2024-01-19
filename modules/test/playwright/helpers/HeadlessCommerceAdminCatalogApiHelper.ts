/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ApiHelpers} from './ApiHelpers';

export class HeadlessCommerceAdminCatalogApiHelper {
	readonly apiHelpers: ApiHelpers;
	readonly basePath: string;

	constructor(apiHelpers: ApiHelpers) {
		this.apiHelpers = apiHelpers;
		this.basePath = 'headless-commerce-admin-catalog/v1.0/';
	}

	async deleteCatalog(
		apiHelpers: ApiHelpers,
		basePath: string,
		catalogId: string
	) {
		return apiHelpers.delete(
			`${apiHelpers.baseUrl}${basePath}/catalog/${catalogId}`
		);
	}

	async deleteOptionCategory(
		apiHelpers: ApiHelpers,
		basePath: string,
		optionCategoryId: string
	) {
		return apiHelpers.delete(
			`${apiHelpers.baseUrl}${basePath}/optionCategories/${optionCategoryId}`
		);
	}

	async deleteProduct(
		apiHelpers: ApiHelpers,
		basePath: string,
		productId: string
	) {
		return apiHelpers.delete(
			`${apiHelpers.baseUrl}${basePath}/products/${productId}`
		);
	}

	async deleteSpecification(
		apiHelpers: ApiHelpers,
		basePath: string,
		specificationId: string
	) {
		return apiHelpers.delete(
			`${apiHelpers.baseUrl}${basePath}/specifications/${specificationId}`
		);
	}

	async getCatalog(catalogId: string) {
		return this.apiHelpers.get(
			`${this.apiHelpers.baseUrl}${this.basePath}/catalogs/${catalogId}`
		);
	}

	async getOptionCategory(optionCategoryId: string) {
		return this.apiHelpers.get(
			`${this.apiHelpers.baseUrl}${this.basePath}/optionCategories/${optionCategoryId}`
		);
	}

	async getProduct(productId: string) {
		return this.apiHelpers.get(
			`${this.apiHelpers.baseUrl}${this.basePath}/products/${productId}`
		);
	}

	async getSpecification(specificationId: string) {
		return this.apiHelpers.get(
			`${this.apiHelpers.baseUrl}${this.basePath}/specifications/${specificationId}`
		);
	}

	async postCatalog(catalogName: string) {
		const postCatalog = await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/catalogs`,
			{
				accountId: 0,
				currencyCode: 'USD',
				defaultLanguageId: 'en_US',
				name: catalogName,
			}
		);

		return postCatalog;
	}

	async postOptionCategory(optionCategoryName: string, priority: number) {
		const postOptionCategory = await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/optionCategories`,
			{
				key: optionCategoryName,
				priority,
				title: {
					en_US: optionCategoryName,
				},
			}
		);

		return postOptionCategory;
	}

	async postProduct(product: DataObject) {
		const postProduct = await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/products`,
			product
		);

		return postProduct;
	}

	async postSpecification(specification: DataObject) {
		const postSpecification = await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/specifications`,
			specification
		);

		return postSpecification;
	}
}
