/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export class HeadlessCommerceAdminCatalogApiHelper {
	constructor(apiHelpers, dataHelper) {
		this.apiHelpers = apiHelpers;
		this.basePath = 'headless-commerce-admin-catalog/v1.0/';
		this.dataHelper = dataHelper;
	}

	async deleteCatalog(
		apiHelpers = this.apiHelpers,
		basePath = this.basePath,
		catalogId
	) {
		return apiHelpers.delete(
			`${apiHelpers.baseUrl}${basePath}/catalog/${catalogId}`
		);
	}

	async deleteOptionCategory(
		apiHelpers = this.apiHelpers,
		basePath = this.basePath,
		optionCategoryId
	) {
		return apiHelpers.delete(
			`${apiHelpers.baseUrl}${basePath}/optionCategories/${optionCategoryId}`
		);
	}

	async deleteProduct(
		apiHelpers = this.apiHelpers,
		basePath = this.basePath,
		productId
	) {
		return apiHelpers.delete(
			`${apiHelpers.baseUrl}${basePath}/products/${productId}`
		);
	}

	async deleteSpecification(
		apiHelpers = this.apiHelpers,
		basePath = this.basePath,
		specificationId
	) {
		return apiHelpers.delete(
			`${apiHelpers.baseUrl}${basePath}/specifications/${specificationId}`
		);
	}

	async getCatalog(catalogId) {
		return this.apiHelpers.get(
			`${this.apiHelpers.baseUrl}${this.basePath}/catalogs/${catalogId}`
		);
	}

	async getOptionCategory(optionCategoryId) {
		return this.apiHelpers.get(
			`${this.apiHelpers.baseUrl}${this.basePath}/optionCategories/${optionCategoryId}`
		);
	}

	async getProduct(productId) {
		return this.apiHelpers.get(
			`${this.apiHelpers.baseUrl}${this.basePath}/products/${productId}`
		);
	}

	async getSpecification(specificationId) {
		return this.apiHelpers.get(
			`${this.apiHelpers.baseUrl}${this.basePath}/specifications/${specificationId}`
		);
	}

	async postCatalog(catalog) {
		const postCatalog = await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/catalogs`,
			catalog
		);

		this.dataHelper.addDataObject({
			basePath: this.basePath,
			handleDelete: this.deleteCatalog,
			id: postCatalog.id,
		});

		return postCatalog;
	}

	async postOptionCategory(optionCategory) {
		const postOptionCategory = await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/optionCategories`,
			optionCategory
		);

		this.dataHelper.addDataObject({
			basePath: this.basePath,
			handleDelete: this.deleteOptionCategory,
			id: postOptionCategory.id,
		});

		return postOptionCategory;
	}

	async postProduct(product) {
		const postProduct = await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/products`,
			product
		);

		this.dataHelper.addDataObject({
			basePath: this.basePath,
			handleDelete: this.deleteProduct,
			id: postProduct.productId,
		});

		return postProduct;
	}

	async postSpecification(specification) {
		const postSpecification = await this.apiHelpers.post(
			`${this.apiHelpers.baseUrl}${this.basePath}/specifications`,
			specification
		);

		this.dataHelper.addDataObject({
			basePath: this.basePath,
			handleDelete: this.deleteSpecification,
			id: postSpecification.id,
		});

		return postSpecification;
	}
}
