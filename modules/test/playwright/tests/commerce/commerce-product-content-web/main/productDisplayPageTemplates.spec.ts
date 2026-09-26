/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';
import {createReadStream, readFileSync} from 'fs';
import path from 'path';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {customFieldsPagesTest} from '../../../../fixtures/customFieldsPagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {displayPageTemplatesPagesTest} from '../../../../fixtures/displayPageTemplatesPagesTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {pageEditorPagesTest} from '../../../../fixtures/pageEditorPagesTest';
import {DataApiHelpers} from '../../../../helpers/ApiHelpers';
import {PageEditorPage} from '../../../../pages/layout-content-page-editor-web/PageEditorPage';
import {DisplayPageTemplatesPage} from '../../../../pages/layout-page-template-admin-web/DisplayPageTemplatesPage';
import getGlobalSiteId from '../../../../utils/getGlobalSiteId';
import {getRandomInt} from '../../../../utils/getRandomInt';
import getRandomString from '../../../../utils/getRandomString';
import {waitForAlert} from '../../../../utils/waitForAlert';
import {
	apiStorefrontSetUp,
	deployProductFragmentsOnDefaultDPT,
} from '../../utils/commerce';

export const test = mergeTests(
	commercePagesTest,
	customFieldsPagesTest,
	dataApiHelpersTest,
	displayPageTemplatesPagesTest,
	loginTest(),
	pageEditorPagesTest
);

async function deployCollectionDisplayOnDefaultDPT(
	apiHelpers: DataApiHelpers,
	{
		collectionName,
		displayPageTemplatesPage,
		listItemStyle,
		pageEditorPage,
		site,
	}: {
		collectionName: string;
		displayPageTemplatesPage: DisplayPageTemplatesPage;
		listItemStyle?: string;
		pageEditorPage: PageEditorPage;
		site: Site;
	}
) {
	await deployProductFragmentsOnDefaultDPT(apiHelpers, {
		displayPageTemplatesPage,
		fragmentNames: [],
		onFragmentsAdded: async () => {
			await pageEditorPage.addFragment(
				'Content Display',
				'Collection Display'
			);

			const collectionDisplayId =
				await pageEditorPage.getFragmentId('Collection Display');

			await pageEditorPage.selectFragment(collectionDisplayId);

			await pageEditorPage.chooseCollectionDisplayCollection(
				'Related Items Collection Providers',
				collectionName
			);

			await pageEditorPage.waitForChangesSaved();

			await pageEditorPage.selectFragment(collectionDisplayId);

			await pageEditorPage.changeConfiguration({
				fieldLabel: 'Style Display',
				tab: 'General',
				value: 'Bordered List',
			});

			if (listItemStyle) {
				await pageEditorPage.changeConfiguration({
					fieldLabel: 'List Item Style',
					tab: 'General',
					value: listItemStyle,
				});
			}
		},
		pageEditorPage,
		site,
	});
}

async function gotoProductPage(
	page: Page,
	site: Site,
	product: {urls?: {[key: string]: string}}
) {
	await page.goto(`/web${site.friendlyUrlPath}/p/${product.urls['en_US']}`);
}

test(
	'Collection Display fragment lists the specifications and the attachments of the displayed product',
	{tag: ['@COMMERCE-9434', '@COMMERCE-9509', '@LPD-106244-Grouped-30']},
	async ({apiHelpers, displayPageTemplatesPage, page, pageEditorPage}) => {
		test.setTimeout(300000);

		const {catalog, site} = await apiStorefrontSetUp(apiHelpers);

		const specification =
			await apiHelpers.headlessCommerceAdminCatalog.postSpecification(
				true,
				0,
				getRandomString()
			);

		const specificationProducts = [];

		for (let index = 0; index < 2; index++) {
			specificationProducts.push(
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId: catalog.id,
					name: {en_US: getRandomString()},
					productSpecifications: [
						{
							specificationKey: specification.key,
							value: {en_US: getRandomString()},
						},
					],
				})
			);
		}

		await deployCollectionDisplayOnDefaultDPT(apiHelpers, {
			collectionName: 'Product Specifications',
			displayPageTemplatesPage,
			pageEditorPage,
			site,
		});

		for (const specificationProduct of specificationProducts) {
			await gotoProductPage(page, site, specificationProduct);

			await expect(
				page
					.locator('li.list-group-item')
					.filter({hasText: specification.title['en_US']})
			).toContainText(
				specificationProduct.productSpecifications[0].value['en_US']
			);
		}

		const attachmentProduct =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				name: {en_US: getRandomString()},
			});

		const attachmentTitles = [];

		for (const fileName of ['attachment.txt', 'liferay.png']) {
			const document = await apiHelpers.headlessDelivery.postDocument(
				site.id,
				createReadStream(
					path.join(__dirname, 'dependencies', fileName)
				),
				{fileName, title: getRandomString()}
			);

			apiHelpers.data.push({id: document.id, type: 'document'});

			const attachment =
				await apiHelpers.headlessCommerceAdminCatalog.postAttachment(
					attachmentProduct.productId,
					document.id,
					document.title
				);

			apiHelpers.data.push({id: attachment.id, type: 'attachment'});

			attachmentTitles.push(attachment.title['en_US']);
		}

		await deployCollectionDisplayOnDefaultDPT(apiHelpers, {
			collectionName: 'Product Attachments',
			displayPageTemplatesPage,
			pageEditorPage,
			site,
		});

		await gotoProductPage(page, site, attachmentProduct);

		for (const attachmentTitle of attachmentTitles) {
			await expect(
				page
					.locator('li.list-group-item')
					.filter({hasText: attachmentTitle})
					.getByRole('link', {name: 'Download'})
			).toBeVisible();
		}
	}
);

test(
	'Collection Display fragment lists the related diagrams of the displayed product with each list item style',
	{tag: ['@COMMERCE-9392', '@COMMERCE-11201', '@LPD-106244-Grouped-30']},
	async ({apiHelpers, displayPageTemplatesPage, page, pageEditorPage}) => {
		test.setTimeout(300000);

		const {catalog, product, site} = await apiStorefrontSetUp(apiHelpers);

		const diagramProduct =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId: catalog.id,
				diagram: {
					attachmentBase64: {
						attachment: readFileSync(
							path.join(__dirname, 'dependencies', 'liferay.png')
						).toString('base64'),
						title: {en_US: getRandomString()},
					},
					radius: 1,
				},
				name: {en_US: getRandomString()},
				productType: 'diagram',
			});

		const pinnedProducts = [];

		for (const sequence of ['1', '2']) {
			const pinnedProduct =
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId: catalog.id,
					name: {en_US: getRandomString()},
				});

			await apiHelpers.headlessCommerceAdminCatalog.postPin(
				diagramProduct.productId,
				{
					mappedProduct: {
						productId: pinnedProduct.productId,
						quantity: 1,
						sequence,
						sku: pinnedProduct.skus[0].sku,
						skuId: pinnedProduct.skus[0].id,
						type: 'sku',
					},
					sequence,
				}
			);

			pinnedProducts.push(pinnedProduct);
		}

		const diagramLink = page.getByRole('link', {
			name: diagramProduct.name['en_US'],
		});

		for (const listItemStyle of [undefined, 'Diagram Card']) {
			await deployCollectionDisplayOnDefaultDPT(apiHelpers, {
				collectionName: 'Related Diagrams',
				displayPageTemplatesPage,
				listItemStyle,
				pageEditorPage,
				site,
			});

			for (const pinnedProduct of pinnedProducts) {
				await gotoProductPage(page, site, pinnedProduct);

				await diagramLink.first().click();

				await expect(page).toHaveURL(
					new RegExp(`/p/${diagramProduct.urls['en_US']}`)
				);
			}

			await gotoProductPage(page, site, product);

			await expect(page.getByText('No Results Found')).toBeVisible();
			await expect(diagramLink).toHaveCount(0);
		}
	}
);

test(
	'Heading fragments mapped to product fields show the product values on the product page',
	{
		tag: [
			'@COMMERCE-7277',
			'@COMMERCE-9461',
			'@COMMERCE-9696',
			'@LPD-106244-Grouped-30',
		],
	},
	async ({
		addCustomFieldPage,
		apiHelpers,
		commerceLayoutsPage,
		displayPageTemplatesPage,
		page,
		pageEditorPage,
		viewAttributesPage,
	}) => {
		test.setTimeout(300000);

		const {catalog, channel, site} = await apiStorefrontSetUp(apiHelpers);

		const globalSiteId = String(await getGlobalSiteId(apiHelpers));

		const vocabularies = [];

		for (const categoryNames of [
			[getRandomString()],
			[getRandomString()],
			[getRandomString(), getRandomString()],
		]) {
			const vocabulary =
				await apiHelpers.headlessAdminTaxonomy.postSiteTaxonomyVocabulary(
					{
						name: getRandomString(),
						siteId: globalSiteId,
					}
				);

			const categories = [];

			for (const categoryName of categoryNames) {
				const category =
					await apiHelpers.headlessAdminTaxonomy.postTaxonomyVocabularyTaxonomyCategory(
						{
							name: categoryName,
							vocabularyId: vocabulary.id,
						}
					);

				categories.push({id: category.id, name: categoryName});
			}

			vocabularies.push({categories, name: vocabulary.name});
		}

		const customFields = [
			{name: getRandomString(), value: getRandomString()},
			{name: getRandomString(), value: getRandomString()},
		];

		for (const customField of customFields) {
			await addCustomFieldPage.addCustomField({
				fieldName: customField.name,
				fieldType: 'textArea',
				resource: 'Product',
			});
		}

		try {
			const product =
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId: catalog.id,
					categories: vocabularies.flatMap(
						(vocabulary) => vocabulary.categories
					),
					customFields: customFields.map((customField) => ({
						customValue: {data: customField.value},
						name: customField.name,
					})),
					description: {en_US: getRandomString()},
					name: {en_US: getRandomString()},
					shortDescription: {en_US: getRandomString()},
					skus: [
						{
							cost: 0,
							price: 24,
							published: true,
							purchasable: true,
							sku: getRandomString(),
						},
					],
				});

			const warehouse =
				await apiHelpers.headlessCommerceAdminInventoryApiHelper.postWarehouses(
					{
						active: true,
						latitude: getRandomInt(),
						longitude: getRandomInt(),
						warehouseItems: [
							{
								quantity: 120,
								sku: product.skus[0].sku,
							},
						],
					}
				);

			await apiHelpers.headlessCommerceAdminInventoryApiHelper.postWarehousesChannels(
				warehouse.id,
				channel.id
			);

			const mappedFields = [
				{field: 'Availability Status', text: 'available'},
				{field: 'Description', text: product.description['en_US']},
				{field: 'Final Price', text: '$ 24.00'},
				{field: 'Inventory', text: '120'},
				{field: 'Name', text: product.name['en_US']},
				{field: 'Product Type', text: 'simple'},
				{
					field: 'Short Description',
					text: product.shortDescription['en_US'],
				},
				{field: 'SKU', text: product.skus[0].sku},
				{field: 'Author Name', text: 'Test Test'},
				...vocabularies.map((vocabulary) => ({
					field: vocabulary.name,
					text: vocabulary.categories.map(
						(category) => category.name
					),
				})),
				...customFields.map((customField) => ({
					field: customField.name,
					text: customField.value,
				})),
			];

			await deployProductFragmentsOnDefaultDPT(apiHelpers, {
				displayPageTemplatesPage,
				fragmentNames: [],
				onFragmentsAdded: async () => {
					for (const [index, {field}] of mappedFields.entries()) {
						await pageEditorPage.addFragment(
							'Basic Components',
							'Heading'
						);

						await pageEditorPage.selectEditable(
							await pageEditorPage.getFragmentId(
								'Heading',
								index
							),
							'element-text'
						);

						await commerceLayoutsPage.labelField.selectOption(
							field
						);

						await pageEditorPage.waitForChangesSaved();
					}
				},
				pageEditorPage,
				site,
			});

			await gotoProductPage(page, site, product);

			const headings = page.locator('.component-heading');

			for (const [index, {text}] of mappedFields.entries()) {
				for (const expectedText of [text].flat()) {
					await expect(headings.nth(index)).toContainText(
						expectedText
					);
				}
			}
		}
		finally {
			for (const customField of customFields) {
				await viewAttributesPage.deleteCustomField(
					customField.name,
					'Product'
				);
			}
		}
	}
);

test(
	'Marking and unmarking a product display page template as default switches the product page',
	{tag: '@LPD-106244-Grouped-30'},
	async ({
		apiHelpers,
		displayPageTemplatesPage,
		page,
		pageEditorPage,
		productDetailsPage,
	}) => {
		const {product, site} = await apiStorefrontSetUp(apiHelpers, [
			{
				title: getRandomString(),
				widgetName:
					'com_liferay_commerce_product_content_web_internal_portlet_CPContentPortlet',
			},
		]);

		const headingText = getRandomString();

		const displayPageTemplateName =
			await deployProductFragmentsOnDefaultDPT(apiHelpers, {
				displayPageTemplatesPage,
				fragmentNames: [],
				onFragmentsAdded: async () => {
					await pageEditorPage.addFragment(
						'Basic Components',
						'Heading'
					);

					await pageEditorPage.editTextEditable(
						await pageEditorPage.getFragmentId('Heading'),
						'element-text',
						headingText
					);
				},
				pageEditorPage,
				site,
			});

		const heading = page.locator('.component-heading', {
			hasText: headingText,
		});

		await gotoProductPage(page, site, product);

		await expect(heading).toBeVisible();

		await displayPageTemplatesPage.goto(site.friendlyUrlPath);

		page.once('dialog', async (dialog) => {
			expect(dialog.message()).toBe(
				'Some assets might be set to use the default display page. Are you sure you want to unmark this?'
			);

			await dialog.accept();
		});

		await displayPageTemplatesPage.clickMoreActions(
			displayPageTemplateName,
			'Unmark as Default'
		);

		await waitForAlert(page);

		await gotoProductPage(page, site, product);

		await expect(heading).toHaveCount(0);
		await expect(
			await productDetailsPage.nameField(product.name['en_US'])
		).toBeVisible();
		await expect(
			await productDetailsPage.skuField(product.skus[0].sku)
		).toBeVisible();
		await expect(
			await productDetailsPage.priceField('$ 10.00')
		).toBeVisible();

		await displayPageTemplatesPage.goto(site.friendlyUrlPath);

		await displayPageTemplatesPage.markAsDefault(displayPageTemplateName);

		await gotoProductPage(page, site, product);

		await expect(heading).toBeVisible();
	}
);
