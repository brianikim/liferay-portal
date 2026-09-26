/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../../fixtures/apiHelpersTest';
import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {isolatedSiteTest} from '../../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../../fixtures/loginTest';
import {taxCategoriesPageTest} from '../../../../fixtures/taxCategoriesPageTest';
import {getRandomInt} from '../../../../utils/getRandomInt';
import getRandomString from '../../../../utils/getRandomString';
import performLogin, {
	performLogout,
	userData,
} from '../../../../utils/performLogin';
import {waitForAlert} from '../../../../utils/waitForAlert';

export const test = mergeTests(
	apiHelpersTest,
	commercePagesTest,
	dataApiHelpersTest,
	isolatedSiteTest,
	loginTest(),
	taxCategoriesPageTest
);

test('LPD-13490 Manage channel country visibility from channel page', async ({
	apiHelpers,
	commerceAdminChannelDetailsCountriesPage,
	commerceAdminChannelDetailsPage,
	commerceAdminChannelsPage,
	page,
}) => {
	await page.goto('/');

	const site =
		await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath('guest');

	const channel = await apiHelpers.headlessCommerceAdminChannel.postChannel({
		siteGroupId: site.id,
	});

	await commerceAdminChannelsPage.goto();

	await (
		await commerceAdminChannelsPage.channelsTableRowLink(channel.name)
	).click();

	await commerceAdminChannelDetailsPage.goToCountries();

	await commerceAdminChannelDetailsCountriesPage.addCountryButton.click();

	const countryName1 = 'Afghanistan';
	const countryName2 = 'Aland Islands';

	await (
		await commerceAdminChannelDetailsCountriesPage.countryFrameCountry(
			countryName1
		)
	).check();
	await (
		await commerceAdminChannelDetailsCountriesPage.countryFrameCountry(
			countryName2
		)
	).check();

	await commerceAdminChannelDetailsCountriesPage.addCountryAddButton.click();

	await expect(
		(
			await commerceAdminChannelDetailsCountriesPage.countriesTableRow(
				0,
				countryName1,
				true
			)
		).row
	).toBeVisible();
	await expect(
		(
			await commerceAdminChannelDetailsCountriesPage.countriesTableRow(
				0,
				countryName2,
				true
			)
		).row
	).toBeVisible();

	await (
		await commerceAdminChannelDetailsCountriesPage.countriesTableRowAction(
			countryName1,
			'Remove'
		)
	).click();

	await page.reload();

	expect(
		await commerceAdminChannelDetailsCountriesPage.countriesTableRows()
	).toHaveLength(1);
	await expect(
		(
			await commerceAdminChannelDetailsCountriesPage.countriesTableRow(
				0,
				countryName2,
				true
			)
		).row
	).toBeVisible();
});

test(
	'Requested Delivery Date at Checkout is available and disabled by default',
	{tag: ['@COMMERCE-9323', '@LPD-105600']},
	async ({apiHelpers, commerceAdminChannelDetailsPage, page, site}) => {
		const channel =
			await apiHelpers.headlessCommerceAdminChannel.postChannel({
				siteGroupId: site.id,
			});

		await page.goto('/');

		await commerceAdminChannelDetailsPage.goto();

		await commerceAdminChannelDetailsPage
			.channelNameLink(channel.name)
			.click();

		await expect(
			commerceAdminChannelDetailsPage.requestedDeliveryDateAtCheckoutToggle
		).toBeVisible();
		await expect(
			commerceAdminChannelDetailsPage.requestedDeliveryDateAtCheckoutToggle
		).toBeEnabled();
		await expect(
			commerceAdminChannelDetailsPage.requestedDeliveryDateAtCheckoutToggle
		).not.toBeChecked();
	}
);

test('LPD-30466 Verify users without edit permission cannot click on channel name link', async ({
	apiHelpers,
	commerceAdminChannelDetailsPage,
	page,
}) => {
	const site = await apiHelpers.headlessAdminSite.postSite({
		name: getRandomString(),
	});

	const channel = await apiHelpers.headlessCommerceAdminChannel.postChannel({
		siteGroupId: site.id,
	});

	const companyId = await page.evaluate(() => {
		return Liferay.ThemeDisplay.getCompanyId();
	});

	const role = await apiHelpers.headlessAdminUser.postRole({
		name: 'User' + getRandomString(),
		rolePermissions: [
			{
				actionIds: ['VIEW_CONTROL_PANEL'],
				primaryKey: companyId,
				resourceName: '90',
				scope: 1,
			},
			{
				actionIds: ['VIEW_COMMERCE_CHANNELS'],
				primaryKey: companyId,
				resourceName: 'com.liferay.commerce.channel',
				scope: 1,
			},
			{
				actionIds: ['VIEW'],
				primaryKey: companyId,
				resourceName:
					'com.liferay.commerce.product.model.CommerceChannel',
				scope: 1,
			},
			{
				actionIds: ['ACCESS_IN_CONTROL_PANEL'],
				primaryKey: companyId,
				resourceName:
					'com_liferay_commerce_channel_web_internal_portlet_CommerceChannelsPortlet',
				scope: 1,
			},
		],
	});

	const userAccount = await apiHelpers.headlessAdminUser.postUserAccount();

	userData[userAccount.alternateName] = {
		name: userAccount.givenName,
		password: 'test',
		surname: userAccount.familyName,
	};

	await apiHelpers.headlessAdminUser.postRoleByExternalReferenceCodeUserAccountAssociation(
		role.externalReferenceCode,
		userAccount.id
	);

	await performLogout(page);

	await performLogin(page, userAccount.alternateName);

	await commerceAdminChannelDetailsPage.goto();

	try {
		await expect(page.getByText(channel.name)).toBeVisible();
		await expect(
			commerceAdminChannelDetailsPage.channelNameLink(channel.name)
		).toHaveCount(0);
	}
	finally {
		await performLogout(page);

		await performLogin(page, 'test');
	}
});

test(
	'Payment method eligibility can be set to a payment term and back to no payment terms',
	{tag: '@LPD-106244-Grouped-7'},
	async ({
		apiHelpers,
		commerceAdminChannelDetailsPage,
		commerceAdminChannelsPage,
		page,
		site,
	}) => {
		const channel =
			await apiHelpers.headlessCommerceAdminChannel.postChannel({
				siteGroupId: site.id,
			});

		const paymentTerm =
			await apiHelpers.headlessCommerceAdminOrder.postTerm({
				type: 'payment-terms',
			});

		await commerceAdminChannelsPage.goto();

		await (
			await commerceAdminChannelsPage.channelsTableRowLink(channel.name)
		).click();

		await commerceAdminChannelDetailsPage.activateChannelConfiguration(
			'PayPal',
			'Payment Methods'
		);

		const paymentTermCell = (
			await commerceAdminChannelDetailsPage.sidePanelFrame(
				'Payment Methods'
			)
		).getByRole('cell', {exact: true, name: paymentTerm.name});

		for (const {eligibilityOption, paymentTermCount, paymentTermLabel} of [
			{
				eligibilityOption: 'Specific Payment Terms',
				paymentTermCount: 1,
				paymentTermLabel: paymentTerm.label['en_US'],
			},
			{
				eligibilityOption: 'No Payment Terms',
				paymentTermCount: 0,
				paymentTermLabel: '',
			},
		]) {
			await page.reload();

			await (
				await commerceAdminChannelDetailsPage.generalCommerceAdminChannelTableLink(
					'PayPal'
				)
			).click();

			await commerceAdminChannelDetailsPage.setEntryEligibility(
				eligibilityOption,
				paymentTermLabel,
				'Payment Methods'
			);

			await (
				await commerceAdminChannelDetailsPage.generalCommerceAdminChannelTableLink(
					'PayPal'
				)
			).click();
			await (
				await commerceAdminChannelDetailsPage.eligibilityTab(
					false,
					'Payment Methods'
				)
			).click();

			await expect(paymentTermCell).toHaveCount(paymentTermCount);

			await (
				await commerceAdminChannelDetailsPage.closeSidePanelFrame(
					false,
					'Payment Methods'
				)
			).click();
		}
	}
);

test(
	'Channel general settings can be edited and are persisted',
	{tag: ['@COMMERCE-6102', '@LPD-106244-Grouped-27']},
	async ({
		apiHelpers,
		commerceAdminChannelDetailsPage,
		commerceAdminChannelsPage,
		newTaxCategoryPage,
		page,
		site,
		taxCategoriesPage,
	}) => {
		const channel =
			await apiHelpers.headlessCommerceAdminChannel.postChannel({
				siteGroupId: site.id,
			});

		const channelName = getRandomString();
		const taxCategoryName = 'Tax Category ' + getRandomInt();

		const selects = [
			{
				label: 'Euro',
				locator: commerceAdminChannelDetailsPage.channelCurrencySelect,
			},
			{label: 'B2X', locator: commerceAdminChannelsPage.commerceSiteType},
			{
				label: 'Single Approver (Version 1)',
				locator: commerceAdminChannelsPage.buyerOrderApprovalWorkflow,
			},
			{
				label: 'Single Approver (Version 1)',
				locator:
					commerceAdminChannelsPage.sellerOrderAcceptanceWorkflow,
			},
			{
				label: 'Gross Price',
				locator: page.getByLabel('Price Type', {exact: true}),
			},
			{
				label: 'Gross Price',
				locator: page.getByLabel('Discounts Target Price Type'),
			},
		];

		const toggles = [
			commerceAdminChannelDetailsPage.guestCheckoutToggle,
			page.getByLabel('Purchase Order Number'),
		];

		try {
			await test.step('Create a tax category', async () => {
				await taxCategoriesPage.goto();

				await taxCategoriesPage.newButton.click();
				await newTaxCategoryPage.nameInput.fill(taxCategoryName);
				await newTaxCategoryPage.saveButton.click();

				await waitForAlert(page);
			});

			await test.step('Edit every general setting of the channel', async () => {
				await commerceAdminChannelsPage.goto();

				await (
					await commerceAdminChannelsPage.channelsTableRowLink(
						channel.name
					)
				).click();

				await page.getByLabel('Name', {exact: true}).fill(channelName);

				for (const {label, locator} of selects) {
					await locator.selectOption({label});
				}

				for (const toggle of toggles) {
					await toggle.check();
				}

				await commerceAdminChannelDetailsPage.maxOpenOrderAccountInput.fill(
					'1'
				);

				await commerceAdminChannelDetailsPage.taxCategoryInput.fill(
					taxCategoryName
				);
				await commerceAdminChannelDetailsPage
					.searchedEntry(taxCategoryName)
					.click();

				await commerceAdminChannelDetailsPage.saveButton.click();

				await waitForAlert(page);
			});

			await test.step('Every general setting is persisted', async () => {
				await page.reload();

				await expect(
					page.getByLabel('Name', {exact: true})
				).toHaveValue(channelName);

				for (const {label, locator} of selects) {
					await expect(locator.locator('option:checked')).toHaveText(
						label
					);
				}

				for (const toggle of toggles) {
					await expect(toggle).toBeChecked();
				}

				await expect(
					commerceAdminChannelDetailsPage.maxOpenOrderAccountInput
				).toHaveValue('1');
				await expect(
					commerceAdminChannelDetailsPage.taxCategoryInput
				).toHaveValue(taxCategoryName);
			});
		}
		finally {
			page.once('dialog', (dialog) => dialog.accept());

			await taxCategoriesPage.goto();

			await (
				await taxCategoriesPage.taxCategoriesTableRowActions(
					taxCategoryName
				)
			).click();
			await taxCategoriesPage.deleteMenuItem.click();

			await waitForAlert(page);
		}
	}
);
