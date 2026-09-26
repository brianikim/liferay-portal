/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Page, expect, mergeTests} from '@playwright/test';
import {createReadStream} from 'fs';
import path from 'path';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {displayPageTemplatesPagesTest} from '../../../../fixtures/displayPageTemplatesPagesTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {pageEditorPagesTest} from '../../../../fixtures/pageEditorPagesTest';
import {DataApiHelpers} from '../../../../helpers/ApiHelpers';
import {PageEditorPage} from '../../../../pages/layout-content-page-editor-web/PageEditorPage';
import {DisplayPageTemplatesPage} from '../../../../pages/layout-page-template-admin-web/DisplayPageTemplatesPage';
import getRandomString from '../../../../utils/getRandomString';
import {
	apiStorefrontSetUp,
	deployProductFragmentsOnDefaultDPT,
} from '../../utils/commerce';

export const test = mergeTests(
	commercePagesTest,
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
