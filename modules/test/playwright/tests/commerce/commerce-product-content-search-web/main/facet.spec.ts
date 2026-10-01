/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {isolatedSiteTest} from '../../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {DataApiHelpers} from '../../../../helpers/ApiHelpers';
import {createCategories} from '../../../../helpers/CreateCategories';
import {TProduct} from '../../../../helpers/HeadlessCommerceAdminCatalogApiHelper';
import {
	FacetWidget,
	SpecificationFacetsPage,
} from '../../../../pages/commerce/commerce-product-content-search-web/specificationFacetsPage';
import getGlobalSiteId from '../../../../utils/getGlobalSiteId';
import {getRandomInt} from '../../../../utils/getRandomInt';
import getRandomString from '../../../../utils/getRandomString';
import getWidgetDefinition from '../../../layout-content-page-editor-web/main/utils/getWidgetDefinition';
import {commerceSearchPageSetUp} from '../../utils/commerce';

export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	isolatedSiteTest,
	loginTest()
);

type FacetPlan = {
	name: string;
	values: string[][];
};

type SeedFacets = (
	apiHelpers: DataApiHelpers,
	catalogId: number,
	facetPlans: FacetPlan[],
	productNamePrefix?: string
) => Promise<TProduct[]>;

async function seedOptionFacets(
	apiHelpers: DataApiHelpers,
	catalogId: number,
	facetPlans: FacetPlan[],
	productNamePrefix = ''
) {
	const options = [];

	for (const [index, facetPlan] of facetPlans.entries()) {
		options.push(
			await apiHelpers.headlessCommerceAdminCatalog.postOption(
				'select',
				facetPlan.name.toLowerCase(),
				facetPlan.name,
				index,
				true
			)
		);
	}

	const products = [];

	for (const productValues of toProductValues(facetPlans)) {
		const productOptions = [];

		productValues.forEach((values, facetIndex) => {
			if (!values.length) {
				return;
			}

			productOptions.push({
				facetable: true,
				fieldType: 'select',
				key: options[facetIndex].key,
				name: {en_US: facetPlans[facetIndex].name},
				optionId: options[facetIndex].id,
				priceType: 'static',
				priority: facetIndex,
				productOptionValues: values.map((value, valueIndex) => ({
					key: `value-${valueIndex}`,
					name: {en_US: value},
					priority: valueIndex,
				})),
			});
		});

		products.push(
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId,
				name: {en_US: productNamePrefix + getRandomString()},
				productOptions,
			})
		);
	}

	return products;
}

async function seedSpecificationFacets(
	apiHelpers: DataApiHelpers,
	catalogId: number,
	facetPlans: FacetPlan[],
	productNamePrefix = ''
) {
	const specifications = [];

	for (const [index, facetPlan] of facetPlans.entries()) {
		specifications.push(
			await apiHelpers.headlessCommerceAdminCatalog.postSpecification(
				true,
				index,
				facetPlan.name
			)
		);
	}

	const products = [];

	for (const productValues of toProductValues(facetPlans)) {
		const productSpecifications = [];

		productValues.forEach((values, facetIndex) => {
			for (const value of values) {
				productSpecifications.push({
					specificationKey: specifications[facetIndex].key,
					value: {en_US: value},
				});
			}
		});

		products.push(
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId,
				name: {en_US: productNamePrefix + getRandomString()},
				productSpecifications,
			})
		);
	}

	return products;
}

function toProductValues(facetPlans: FacetPlan[]): string[][][] {
	const productCount = Math.max(
		...facetPlans.map((facetPlan) => facetPlan.values.length)
	);

	return Array.from({length: productCount}, (_, productIndex) =>
		facetPlans.map((facetPlan) => facetPlan.values[productIndex] ?? [])
	);
}

async function setUpFacetPage(
	apiHelpers: DataApiHelpers,
	page: Page,
	specificationFacetsPage: SpecificationFacetsPage,
	site: Site
) {
	await apiHelpers.headlessCommerceAdminChannel.postChannel({
		siteGroupId: site.id,
	});

	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
		name: getRandomString(),
	});

	const layout = await apiHelpers.jsonWebServicesLayout.addLayout({
		groupId: site.id,
		title: getRandomString(),
	});

	const url = `/web${site.friendlyUrlPath}${layout.friendlyURL}`;

	await page.goto(url);

	await specificationFacetsPage.addRequiredFacetWidgets();

	await specificationFacetsPage.configureSearchBar();

	await specificationFacetsPage.configureSearchOptions();

	return {catalogId: catalog.id, url};
}

async function setUpFacetSearchPage(
	apiHelpers: DataApiHelpers,
	site: Site,
	widgetNames: string[]
) {
	return commerceSearchPageSetUp(
		apiHelpers,
		site,
		[
			...widgetNames,
			'com_liferay_commerce_product_content_search_web_internal_portlet_CPSortPortlet',
			'com_liferay_commerce_product_content_search_web_internal_portlet_CPSearchResultsPortlet',
		].map((widgetName) =>
			getWidgetDefinition({id: getRandomString(), widgetName})
		)
	);
}

async function goToIndexedFacetPage(
	page: Page,
	specificationFacetsPage: SpecificationFacetsPage,
	url: string,
	widget: FacetWidget,
	facetName: string
) {
	await page.goto(url);

	await expect(async () => {
		await page.reload();

		await expect(
			specificationFacetsPage.facetPanel(widget, facetName)
		).toBeVisible({timeout: 5000});
	}).toPass({timeout: 60000});
}

const FACET_WIDGETS: Array<{
	displayTemplateTerms: string[];
	filterTickets: string[];
	seedFacets: SeedFacets;
	tickets: Record<string, string>;
	widget: FacetWidget;
	widgetName: string;
}> = [
	{
		displayTemplateTerms: ['6', '12', '112'],
		filterTickets: ['@COMMERCE-6165'],
		seedFacets: seedOptionFacets,
		tickets: {
			displayFrequencies: '@COMMERCE-8646',
			displayTemplate: '@COMMERCE-8646',
			frequencyThreshold: '@COMMERCE-8646',
			maxEntities: '@COMMERCE-12895',
			maxTerms: '@COMMERCE-12895',
			maxTermsValidation: '@COMMERCE-8646',
			setupTab: '@COMMERCE-8646',
		},
		widget: 'Option Facet',
		widgetName:
			'com_liferay_commerce_product_content_search_web_internal_portlet_CPOptionFacetsPortlet',
	},
	{
		displayTemplateTerms: ['Cast Iron', 'Neoprene', 'Stainless Steel'],
		filterTickets: ['@COMMERCE-6166', '@COMMERCE-12603'],
		seedFacets: seedSpecificationFacets,
		tickets: {
			displayFrequencies: '@COMMERCE-8403',
			displayTemplate: '@COMMERCE-8401',
			frequencyThreshold: '@COMMERCE-8401',
			maxEntities: '@COMMERCE-12895',
			maxTerms: '@COMMERCE-12895',
			maxTermsValidation: '@COMMERCE-8399',
			setupTab: '@COMMERCE-8384',
		},
		widget: 'Specification Facet',
		widgetName:
			'com_liferay_commerce_product_content_search_web_internal_portlet_CPSpecificationOptionFacetsPortlet',
	},
];

for (const {
	displayTemplateTerms,
	filterTickets,
	seedFacets,
	tickets,
	widget,
	widgetName,
} of FACET_WIDGETS) {
	const maxEntitiesField =
		widget === 'Option Facet' ? 'Max Options' : 'Max Specifications';

	const facetPrefix = widget === 'Option Facet' ? 'OptionFacet' : 'SpecFacet';

	const facetNameAt = (index: number) =>
		`${facetPrefix}${String(index).padStart(2, '0')}`;

	test(
		`${widget} - ${maxEntitiesField} limits how many facets are displayed`,
		{tag: [tickets.maxEntities, '@LPD-105602']},
		async ({apiHelpers, page, site, specificationFacetsPage}) => {
			const facetNames = Array.from({length: 12}, (_, index) =>
				facetNameAt(index + 1)
			);

			const facetPlans = facetNames.map((name, index) =>
				index === 0
					? {name, values: [['Alpha'], ['Beta'], ['Gamma']]}
					: {name, values: [[`Value${index}`]]}
			);

			const {catalogId, url} = await setUpFacetPage(
				apiHelpers,
				page,
				specificationFacetsPage,
				site
			);

			await seedFacets(apiHelpers, catalogId, facetPlans);

			await goToIndexedFacetPage(
				page,
				specificationFacetsPage,
				url,
				widget,
				facetNames[0]
			);

			await test.step('All twelve facets are displayed when the limit is above their count', async () => {
				await specificationFacetsPage.updateFacetConfiguration(widget, {
					maxEntities: 15,
				});

				await specificationFacetsPage.closeFacetConfiguration();

				for (const facetName of facetNames) {
					await expect(
						specificationFacetsPage.facetPanel(widget, facetName)
					).toBeVisible();
				}
			});

			await test.step('Lowering the limit to one leaves only the most frequent facet', async () => {
				await specificationFacetsPage.updateFacetConfiguration(widget, {
					maxEntities: 1,
				});

				await specificationFacetsPage.closeFacetConfiguration();

				await expect(
					specificationFacetsPage.facetPanel(widget, facetNames[0])
				).toBeVisible();

				for (const facetName of facetNames.slice(1)) {
					await expect(
						specificationFacetsPage.facetPanel(widget, facetName)
					).toBeHidden();
				}
			});

			await test.step('The terms of the remaining facet are untouched', async () => {
				await expect(
					specificationFacetsPage.facetTerms(widget, facetNames[0])
				).toHaveCount(3);
			});
		}
	);

	test(
		`${widget} - Max Terms limits how many terms are displayed for each facet`,
		{tag: [tickets.maxTerms, '@LPD-105602']},
		async ({apiHelpers, page, site, specificationFacetsPage}) => {
			const [facetA, facetB] = [facetNameAt(1), facetNameAt(2)];

			const facetPlans = [
				{
					name: facetA,
					values: [
						['Shared'],
						['Shared'],
						['Shared'],
						['Rare1'],
						['Rare2'],
					],
				},
				{
					name: facetB,
					values: Array.from({length: 7}, (_, index) => [
						`Single${index + 1}`,
					]),
				},
			];

			const {catalogId, url} = await setUpFacetPage(
				apiHelpers,
				page,
				specificationFacetsPage,
				site
			);

			await seedFacets(apiHelpers, catalogId, facetPlans);

			await goToIndexedFacetPage(
				page,
				specificationFacetsPage,
				url,
				widget,
				facetA
			);

			await test.step('Every term is displayed when the limit is above their count', async () => {
				await specificationFacetsPage.updateFacetConfiguration(widget, {
					maxTerms: 15,
				});

				await specificationFacetsPage.closeFacetConfiguration();

				await expect(
					specificationFacetsPage.facetTerms(widget, facetA)
				).toHaveCount(3);
				await expect(
					specificationFacetsPage.facetTerms(widget, facetB)
				).toHaveCount(7);
			});

			await test.step('Lowering the limit truncates the terms without dropping the facets', async () => {
				for (const maxTerms of [1, 2]) {
					await specificationFacetsPage.updateFacetConfiguration(
						widget,
						{maxTerms}
					);

					await specificationFacetsPage.closeFacetConfiguration();

					await expect(
						specificationFacetsPage.facetTerms(widget, facetA)
					).toHaveCount(maxTerms);
					await expect(
						specificationFacetsPage.facetTerms(widget, facetB)
					).toHaveCount(maxTerms);

					for (const facetName of [facetA, facetB]) {
						await expect(
							specificationFacetsPage.facetPanel(
								widget,
								facetName
							)
						).toBeVisible();
					}
				}
			});

			await test.step('The frequency threshold drops the facet whose terms are all below it', async () => {
				await specificationFacetsPage.updateFacetConfiguration(widget, {
					frequencyThreshold: 3,
				});

				await specificationFacetsPage.closeFacetConfiguration();

				await expect(
					specificationFacetsPage.facetPanel(widget, facetA)
				).toBeVisible();
				await expect(
					specificationFacetsPage.facetPanel(widget, facetB)
				).toBeHidden();
				await expect(
					specificationFacetsPage.facetTerms(widget, facetA)
				).toHaveCount(1);
				await expect(
					specificationFacetsPage.facetTerms(widget, facetA)
				).toContainText(['Shared']);
			});
		}
	);

	test(
		`${widget} - Display Frequencies hides the term frequencies`,
		{tag: [tickets.displayFrequencies, '@LPD-105602']},
		async ({apiHelpers, page, site, specificationFacetsPage}) => {
			const facetName = facetNameAt(1);

			const facetPlans = [
				{
					name: facetName,
					values: [['Shared'], ['Shared'], ['Single']],
				},
			];

			const {catalogId, url} = await setUpFacetPage(
				apiHelpers,
				page,
				specificationFacetsPage,
				site
			);

			await seedFacets(apiHelpers, catalogId, facetPlans);

			await goToIndexedFacetPage(
				page,
				specificationFacetsPage,
				url,
				widget,
				facetName
			);

			await test.step('Frequencies are displayed by default', async () => {
				await expect(
					specificationFacetsPage.facetTermFrequencies(widget)
				).toHaveCount(2);
				await expect(
					specificationFacetsPage.facetTermFrequencies(widget).first()
				).toHaveText('(2)');
			});

			await test.step('Disabling them keeps the terms but drops the counts', async () => {
				await specificationFacetsPage.updateFacetConfiguration(widget, {
					displayFrequencies: false,
				});

				await specificationFacetsPage.closeFacetConfiguration();

				await expect(
					specificationFacetsPage.facetTerms(widget, facetName)
				).toHaveCount(2);
				await expect(
					specificationFacetsPage.facetTermFrequencies(widget)
				).toHaveCount(0);
			});
		}
	);

	test(
		`${widget} - Display Template renders the terms in the selected layout`,
		{tag: [tickets.displayTemplate, '@LPD-105602']},
		async ({apiHelpers, page, site, specificationFacetsPage}) => {
			const facetName = facetNameAt(1);

			const facetPlans = [
				{
					name: facetName,
					values: displayTemplateTerms.map((term) => [term]),
				},
			];

			const {catalogId, url} = await setUpFacetPage(
				apiHelpers,
				page,
				specificationFacetsPage,
				site
			);

			await seedFacets(apiHelpers, catalogId, facetPlans);

			await goToIndexedFacetPage(
				page,
				specificationFacetsPage,
				url,
				widget,
				facetName
			);

			for (const displayTemplate of [
				'Cloud Layout',
				'Compact Layout',
				'Label Layout',
			]) {
				await specificationFacetsPage.updateFacetConfiguration(widget, {
					displayTemplate,
				});

				await specificationFacetsPage.closeFacetConfiguration();

				for (const term of displayTemplateTerms) {
					await expect(
						specificationFacetsPage.facetTermsByDisplayTemplate(
							widget,
							displayTemplate,
							term
						)
					).toBeVisible();
				}
			}
		}
	);

	test(
		`${widget} - Frequency Threshold hides the terms below the threshold`,
		{tag: [tickets.frequencyThreshold, '@LPD-105602']},
		async ({apiHelpers, page, site, specificationFacetsPage}) => {
			const facetName = facetNameAt(1);

			const facetPlans = [
				{
					name: facetName,
					values: [
						['Shared'],
						['Shared'],
						['Shared'],
						['Rare1'],
						['Rare2'],
					],
				},
			];

			const {catalogId, url} = await setUpFacetPage(
				apiHelpers,
				page,
				specificationFacetsPage,
				site
			);

			await seedFacets(apiHelpers, catalogId, facetPlans);

			await goToIndexedFacetPage(
				page,
				specificationFacetsPage,
				url,
				widget,
				facetName
			);

			await expect(
				specificationFacetsPage.facetTerms(widget, facetName)
			).toHaveCount(3);

			await specificationFacetsPage.updateFacetConfiguration(widget, {
				frequencyThreshold: 3,
			});

			await specificationFacetsPage.closeFacetConfiguration();

			await expect(
				specificationFacetsPage.facetTerms(widget, facetName)
			).toHaveCount(1);
			await expect(
				specificationFacetsPage.facetTerms(widget, facetName)
			).toContainText(['Shared']);
		}
	);

	test(
		`${widget} - Max Terms cannot exceed 100`,
		{tag: [tickets.maxTermsValidation, '@LPD-105602']},
		async ({apiHelpers, page, site, specificationFacetsPage}) => {
			const facetName = facetNameAt(1);

			const {catalogId, url} = await setUpFacetPage(
				apiHelpers,
				page,
				specificationFacetsPage,
				site
			);

			await seedFacets(apiHelpers, catalogId, [
				{name: facetName, values: [['Single']]},
			]);

			await goToIndexedFacetPage(
				page,
				specificationFacetsPage,
				url,
				widget,
				facetName
			);

			await specificationFacetsPage.updateFacetConfiguration(widget, {
				maxTerms: 101,
			});

			await expect(
				specificationFacetsPage.configurationErrorMessage(
					'Maximum terms cannot exceed 100.'
				)
			).toBeVisible();
		}
	);

	test(
		`${widget} - Configuration exposes the facet display settings`,
		{tag: [tickets.setupTab, '@LPD-105602']},
		async ({apiHelpers, page, site, specificationFacetsPage}) => {
			const facetName = facetNameAt(1);

			const {catalogId, url} = await setUpFacetPage(
				apiHelpers,
				page,
				specificationFacetsPage,
				site
			);

			await seedFacets(apiHelpers, catalogId, [
				{name: facetName, values: [['Single']]},
			]);

			await goToIndexedFacetPage(
				page,
				specificationFacetsPage,
				url,
				widget,
				facetName
			);

			await specificationFacetsPage.openFacetConfiguration(widget);

			await expect(
				specificationFacetsPage.displayTemplateSelect
			).toBeVisible();
			await expect(
				specificationFacetsPage.maxEntitiesInput(widget)
			).toBeVisible();
			await expect(specificationFacetsPage.maxTermsInput).toBeVisible();
			await expect(
				specificationFacetsPage.frequencyThresholdInput
			).toBeVisible();
			await expect(
				specificationFacetsPage.displayFrequenciesCheckbox
			).toBeVisible();
		}
	);

	test(
		`${widget} - Max Terms displays up to 100 terms`,
		{tag: [tickets.maxTerms, '@LPD-105602']},
		async ({apiHelpers, page, site, specificationFacetsPage}) => {
			const facetName = facetNameAt(1);

			const facetPlans = [
				{
					name: facetName,
					values: [
						Array.from(
							{length: 100},
							(_, index) =>
								`Term${String(index + 1).padStart(3, '0')}`
						),
					],
				},
			];

			const {catalogId, url} = await setUpFacetPage(
				apiHelpers,
				page,
				specificationFacetsPage,
				site
			);

			await seedFacets(apiHelpers, catalogId, facetPlans);

			await goToIndexedFacetPage(
				page,
				specificationFacetsPage,
				url,
				widget,
				facetName
			);

			await expect(
				specificationFacetsPage.facetTerms(widget, facetName)
			).toHaveCount(10);

			await specificationFacetsPage.updateFacetConfiguration(widget, {
				maxTerms: 100,
			});

			await specificationFacetsPage.closeFacetConfiguration();

			await expect(
				specificationFacetsPage.facetTerms(widget, facetName)
			).toHaveCount(100);
		}
	);

	test(
		`${widget} - Selecting a term narrows the search results and the URL`,
		{tag: [...filterTickets, '@LPD-106244-Grouped-9']},
		async ({apiHelpers, page, site, specificationFacetsPage}) => {
			const facetName = `${facetPrefix}${getRandomInt()}`;

			const {catalogId, keyword, url} = await setUpFacetSearchPage(
				apiHelpers,
				site,
				[widgetName]
			);

			const [alphaProduct, betaProduct] = await seedFacets(
				apiHelpers,
				catalogId,
				[{name: facetName, values: [['Alpha'], ['Beta']]}],
				`${keyword} `
			);

			await goToIndexedFacetPage(
				page,
				specificationFacetsPage,
				url,
				widget,
				facetName
			);

			const parameterName = await specificationFacetsPage
				.facetParameterNameInput(widget, facetName)
				.inputValue();

			for (const {hiddenProduct, term, visibleProduct} of [
				{
					hiddenProduct: betaProduct,
					term: 'Alpha',
					visibleProduct: alphaProduct,
				},
				{
					hiddenProduct: alphaProduct,
					term: 'Beta',
					visibleProduct: betaProduct,
				},
			]) {
				await page.goto(url);

				await specificationFacetsPage
					.facetTermCheckbox(widget, facetName, term)
					.click();

				await expect(page).toHaveURL(
					new RegExp(`[?&]${parameterName}=${term}`)
				);
				await expect(
					specificationFacetsPage.searchResult(
						visibleProduct.name['en_US']
					)
				).toBeVisible();
				await expect(
					specificationFacetsPage.searchResult(
						hiddenProduct.name['en_US']
					)
				).toBeHidden();
			}
		}
	);
}

test(
	'Specification Facet keeps working when searching products with many specifications',
	{tag: ['@COMMERCE-12191', '@LPD-106244-Grouped-9']},
	async ({apiHelpers, page, site, specificationFacetsPage}) => {
		const {catalogId, url} = await setUpFacetPage(
			apiHelpers,
			page,
			specificationFacetsPage,
			site
		);

		const specifications = [];

		for (let index = 0; index < 21; index++) {
			specifications.push(
				await apiHelpers.headlessCommerceAdminCatalog.postSpecification(
					true,
					index,
					'Spec' + getRandomString()
				)
			);
		}

		const productPlans = [
			{
				keyword: getRandomString(),
				specifications: specifications.slice(0, 20),
			},
			{
				keyword: getRandomString(),
				specifications: [specifications[0], specifications[20]],
			},
		];

		for (const productPlan of productPlans) {
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId,
				name: {en_US: `${productPlan.keyword} Product`},
				productSpecifications: productPlan.specifications.map(
					(specification) => ({
						specificationKey: specification.key,
						value: {en_US: '0'},
					})
				),
			});
		}

		await goToIndexedFacetPage(
			page,
			specificationFacetsPage,
			url,
			'Specification Facet',
			specifications[0].key
		);

		await specificationFacetsPage.updateFacetConfiguration(
			'Specification Facet',
			{maxTerms: 20}
		);

		await specificationFacetsPage.closeFacetConfiguration();

		for (const {keyword} of productPlans) {
			await specificationFacetsPage.searchFormInput.fill(keyword);
			await specificationFacetsPage.searchFormInput.press('Enter');

			await expect(page).toHaveURL(new RegExp(`[?&]q=${keyword}`));
			await expect(
				specificationFacetsPage.facetTerms(
					'Specification Facet',
					specifications[0].key
				)
			).toHaveCount(1);
			await expect(
				specificationFacetsPage.unavailableErrorMessage(
					'Specification Facet'
				)
			).toHaveCount(0);
		}
	}
);

test(
	'Category Facet narrows the search results to the selected category',
	{tag: ['@COMMERCE-6169', '@LPD-106244-Grouped-9']},
	async ({apiHelpers, page, site, specificationFacetsPage}) => {
		const {catalogId, keyword, url} = await setUpFacetSearchPage(
			apiHelpers,
			site,
			[
				'com_liferay_portal_search_web_category_facet_portlet_CategoryFacetPortlet',
			]
		);

		const categoryName = getRandomString();

		const categories = await createCategories({
			apiHelpers,
			categoryNames: [{name: categoryName}],
			siteId: await getGlobalSiteId(apiHelpers),
			vocabularyName: getRandomString(),
		});

		const categorizedProduct =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId,
				categories,
				name: {en_US: `${keyword} ${getRandomString()}`},
			});
		const uncategorizedProduct =
			await apiHelpers.headlessCommerceAdminCatalog.postProduct({
				catalogId,
				name: {en_US: `${keyword} ${getRandomString()}`},
			});

		await expect(async () => {
			await page.goto(url);

			await expect(
				specificationFacetsPage.categoryFacetCheckbox(categoryName)
			).toBeVisible({timeout: 5000});
		}).toPass({timeout: 60000});

		await specificationFacetsPage
			.categoryFacetCheckbox(categoryName)
			.click();

		await expect(
			specificationFacetsPage.searchResult(
				categorizedProduct.name['en_US']
			)
		).toBeVisible();
		await expect(
			specificationFacetsPage.searchResult(
				uncategorizedProduct.name['en_US']
			)
		).toBeHidden();
	}
);

test(
	'Commerce facets filter cumulatively without the Category Facet and can be cleared',
	{tag: ['@COMMERCE-9253', '@COMMERCE-10881', '@LPD-106244-Grouped-9']},
	async ({apiHelpers, page, site, specificationFacetsPage}) => {
		const {catalogId, keyword, url} = await setUpFacetSearchPage(
			apiHelpers,
			site,
			[
				'com_liferay_commerce_product_content_search_web_internal_portlet_CPOptionFacetsPortlet',
				'com_liferay_commerce_product_content_search_web_internal_portlet_CPPriceRangeFacetsPortlet',
				'com_liferay_commerce_product_content_search_web_internal_portlet_CPSpecificationOptionFacetsPortlet',
			]
		);

		const optionName = `Option${getRandomInt()}`;

		const option = await apiHelpers.headlessCommerceAdminCatalog.postOption(
			'select',
			optionName.toLowerCase(),
			optionName,
			0,
			true
		);

		const specificationName = `Spec${getRandomInt()}`;

		const specification =
			await apiHelpers.headlessCommerceAdminCatalog.postSpecification(
				true,
				0,
				specificationName
			);

		const products = [];

		for (const {optionValue, price, specificationValue} of [
			{optionValue: 'Alpha', price: 150, specificationValue: 'Warranty'},
			{optionValue: 'Beta', price: 150, specificationValue: 'Warranty'},
			{optionValue: 'Alpha', price: 30, specificationValue: 'Warranty'},
			{optionValue: 'Alpha', price: 150, specificationValue: 'Other'},
		]) {
			products.push(
				await apiHelpers.headlessCommerceAdminCatalog.postProduct({
					catalogId,
					name: {en_US: `${keyword} ${getRandomString()}`},
					productOptions: [
						{
							facetable: true,
							fieldType: 'select',
							key: option.key,
							name: option.name,
							optionId: option.id,
							priceType: 'static',
							priority: 0,
							productOptionValues: [
								{
									key: 'value-0',
									name: {en_US: optionValue},
									priority: 0,
								},
							],
						},
					],
					productSpecifications: [
						{
							specificationKey: specification.key,
							value: {en_US: specificationValue},
						},
					],
					skus: [
						{
							cost: 0,
							price,
							published: true,
							purchasable: true,
							sku: getRandomString(),
						},
					],
				})
			);
		}

		await expect(async () => {
			await page.goto(url);

			await expect(
				specificationFacetsPage.productsAvailableLabel(products.length)
			).toBeVisible({timeout: 5000});
		}).toPass({timeout: 60000});

		const facetFilters = [
			{
				checkbox: specificationFacetsPage.facetTermCheckbox(
					'Specification Facet',
					specificationName,
					'Warranty'
				),
				clearedProductCount: 4,
				hiddenProducts: [products[3]],
				portlet: specificationFacetsPage.specificationFacetPortlet,
				visibleProducts: [products[0], products[1], products[2]],
			},
			{
				checkbox: specificationFacetsPage.priceRangeFacetCheckbox(
					'$ 100.00 - $ 199.99'
				),
				clearedProductCount: 3,
				hiddenProducts: [products[2], products[3]],
				portlet: specificationFacetsPage.priceRangeFacetPortlet,
				visibleProducts: [products[0], products[1]],
			},
			{
				checkbox: specificationFacetsPage.facetTermCheckbox(
					'Option Facet',
					optionName,
					'Alpha'
				),
				clearedProductCount: 2,
				hiddenProducts: [products[1], products[2], products[3]],
				portlet: specificationFacetsPage.optionFacetPortlet,
				visibleProducts: [products[0]],
			},
		];

		await test.step('Each facet narrows the results of the previous facets', async () => {
			for (const {
				checkbox,
				hiddenProducts,
				visibleProducts,
			} of facetFilters) {
				await checkbox.click();

				await expect(checkbox).toBeChecked();
				await expect(
					specificationFacetsPage.productsAvailableLabel(
						visibleProducts.length
					)
				).toBeVisible();

				for (const product of visibleProducts) {
					await expect(
						specificationFacetsPage.searchResult(
							product.name['en_US']
						)
					).toBeVisible();
				}

				for (const product of hiddenProducts) {
					await expect(
						specificationFacetsPage.searchResult(
							product.name['en_US']
						)
					).toBeHidden();
				}
			}
		});

		await test.step('Clearing each facet restores the results it filtered out', async () => {
			for (const {checkbox, clearedProductCount, portlet} of [
				...facetFilters,
			].reverse()) {
				await specificationFacetsPage.facetClearButton(portlet).click();

				await expect(checkbox).not.toBeChecked();
				await expect(
					specificationFacetsPage.productsAvailableLabel(
						clearedProductCount
					)
				).toBeVisible();
			}
		});
	}
);
