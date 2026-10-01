/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrameLocator, Locator, Page, expect} from '@playwright/test';

import {
	CommerceDNDTablePage,
	searchTableRowByValue,
} from '../commerceDNDTablePage';
import {
	ProductSubscriptionName,
	SUBSCRIPTION_FIELD_IDS,
} from './commerceAdminProductDetailsPage';

export class CommerceAdminProductDetailsSkusPage extends CommerceDNDTablePage {
	readonly addSkuUOMButton: Locator;
	readonly closeSidePanelFrame: (isNestedFrame: boolean) => Promise<Locator>;
	readonly incrementalOrderQuantity: Locator;
	readonly inventoryTableRow: (
		colPosition: number,
		value: number | string,
		strictEqual?: boolean
	) => Promise<{column: Locator; row: Locator}>;
	readonly inventoryTableRowAction: (pageName: string) => Promise<Locator>;
	readonly page: Page;
	readonly pricinQuantity: Locator;
	readonly sidePanelDetailsSkuExternalReferenceCodeInput: Locator;
	readonly sidePanelDetailsSkuFieldName: Locator;
	readonly sidePanelDetailsSkuPublishButton: Locator;
	readonly sidePanelFrame: FrameLocator;
	readonly sidePanelInventoryTable: Locator;
	readonly sidePanelNestedFrame: FrameLocator;
	readonly sidePanelNestedMenuItem: (action: string) => Locator;
	readonly sidePanelNestedPriceListCurrency: (
		currencyCode: string
	) => Locator;
	readonly sidePanelNestedPriceListPrice: Locator;
	readonly sidePanelNestedSaveButton: Locator;
	readonly sidePanelSaveButton: Locator;
	readonly sidePanelSkuInventoryQuantity: Locator;
	readonly sidePanelSkuPriceTableRowLink: (priceListName: string) => Locator;
	readonly sidePanelSubscriptionEnabledLabel: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly sidePanelSubscriptionLengthInput: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly sidePanelSubscriptionOverrideToggle: Locator;
	readonly sidePanelSubscriptionSection: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly sidePanelSubscriptionTypeSelect: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly sidePanelSubscriptionYearlyModeSelect: (
		subscriptionName: ProductSubscriptionName
	) => Locator;
	readonly sidePanelTierPriceAddButton: Locator;
	readonly sidePanelTierPriceRow: (price: string) => Locator;
	readonly skuAddButton: Locator;
	readonly skuAddModal: FrameLocator;
	readonly skuAddModalSkuInput: Locator;
	readonly skuAddModalSkuPublishButton: Locator;
	readonly skuAddModalSkuPurchasableToggle: Locator;
	readonly skuAddModalSuccessMessage: Locator;
	readonly skuPriceAddButton: Locator;
	readonly skuPriceAddModal: FrameLocator;
	readonly skuPriceAddModalAddButton: Locator;
	readonly skuPriceAddModalAddEntryButton: Locator;
	readonly skuPriceAddModalCancelButton: Locator;
	readonly skuPriceAddModalCurrency: (currencyCode: string) => Locator;
	readonly skuPriceAddModalUnitOfMeasureSelect: Locator;
	readonly skuPriceAddModalUnitPriceInput: Locator;
	readonly skuPriceFrame: FrameLocator;
	readonly skuPriceListSelect: Locator;
	readonly skuPriceMenuItem: (action: string) => Locator;
	readonly skuPriceTableRow: (priceListName: string) => Locator;
	readonly skuTab: (tabName: string) => Locator;
	readonly skusLink: Locator;
	readonly skusTable: Locator;
	readonly skusTableRowBasePrice: (price: string) => Promise<Locator>;
	readonly skusTableRow: (
		colPosition: number,
		value: number | string,
		strictEqual?: boolean
	) => Promise<{column: Locator; row: Locator}>;
	readonly skusTableRowLink: (skuName: string) => Locator;
	readonly skuUOMFrame: FrameLocator;
	readonly skuUOMModal: FrameLocator;
	readonly skuUOMTab: Locator;
	readonly skuUOMTable: Locator;
	readonly skuUOMTableRow: (
		colPosition: number,
		value: number | string,
		strictEqual?: boolean
	) => Promise<{column: Locator; row: Locator}>;
	readonly skuUOMTableRowAction: (skuUOMName: string) => Promise<Locator>;
	readonly skuUOMActionMenuItem: (actionName: string) => Locator;
	readonly skuUOMFrameCancelButton: Locator;
	readonly tierPriceModalFrame: FrameLocator;
	readonly tierPriceModalPriceInput: Locator;
	readonly tierPriceModalQuantityInput: Locator;
	readonly tierPriceModalSubmitButton: Locator;
	readonly uomTableRowLink: (uomName: string) => Locator;

	constructor(page: Page) {
		super(
			page,
			'#_com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet_fm .fds table'
		);
		this.sidePanelFrame = page.frameLocator('.is-visible iframe');
		this.addSkuUOMButton = this.sidePanelFrame
			.getByTestId('managementToolbar')
			.locator('[data-testid="fdsCreationActionButton"]');
		this.closeSidePanelFrame = async (isNestedFrame: boolean) => {
			if (isNestedFrame) {
				return this.sidePanelNestedFrame
					.locator('.side-panel-iframe-header')
					.getByRole('button');
			}

			return this.sidePanelFrame
				.locator('.side-panel-iframe-header')
				.getByRole('button');
		};
		this.inventoryTableRow = async (
			colPosition: number,
			value: number | string,
			strictEqual: boolean = false
		) => {
			return await searchTableRowByValue(
				this.sidePanelInventoryTable,
				colPosition,
				String(value),
				strictEqual
			);
		};
		this.incrementalOrderQuantity = page
			.frameLocator('iframe')
			.first()
			.frameLocator('iframe')
			.getByLabel('Base Unit Quantity');
		this.inventoryTableRowAction = async (warehouseName: string) => {
			const inventoriesTableRow = await this.inventoryTableRow(
				0,
				warehouseName,
				true
			);

			if (inventoriesTableRow && inventoriesTableRow.column) {
				return inventoriesTableRow.row.getByRole('textbox', {
					name: 'commerce-inventory-warehouse-',
				});
			}

			throw new Error(
				`Cannot locate table row with name ${warehouseName}`
			);
		};
		this.page = page;
		this.pricinQuantity = page
			.frameLocator('iframe')
			.first()
			.frameLocator('iframe')
			.getByLabel('Pricing Quantity');
		this.sidePanelDetailsSkuExternalReferenceCodeInput =
			this.sidePanelFrame.locator('input[id$="_externalReferenceCode"]');
		this.sidePanelDetailsSkuFieldName =
			this.sidePanelFrame.getByLabel('SKU Required');
		this.sidePanelDetailsSkuPublishButton = this.sidePanelFrame.getByRole(
			'button',
			{exact: true, name: 'Publish'}
		);
		this.sidePanelInventoryTable = this.sidePanelFrame.locator(
			'#p_p_id_com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet_ table'
		);
		this.sidePanelNestedFrame =
			this.sidePanelFrame.frameLocator('.is-visible iframe');
		this.sidePanelNestedMenuItem = (action: string) =>
			this.sidePanelNestedFrame.getByRole('menuitem', {
				exact: true,
				name: action,
			});
		this.sidePanelNestedPriceListCurrency = (currencyCode: string) =>
			this.sidePanelNestedFrame
				.locator('.input-group-text', {hasText: currencyCode})
				.first();
		this.sidePanelNestedPriceListPrice =
			this.sidePanelNestedFrame.getByLabel('Price List');
		this.sidePanelNestedSaveButton = this.sidePanelNestedFrame.getByRole(
			'button',
			{exact: true, name: 'Save'}
		);
		this.sidePanelSaveButton = this.sidePanelFrame.getByRole('button', {
			exact: true,
			name: 'Save',
		});
		this.sidePanelSkuInventoryQuantity = this.sidePanelFrame.getByRole(
			'textbox',
			{name: 'commerce-inventory-warehouse-'}
		);
		this.sidePanelSkuPriceTableRowLink = (priceListName: string) =>
			this.sidePanelFrame.getByRole('link', {
				exact: true,
				name: priceListName,
			});
		this.sidePanelSubscriptionEnabledLabel = (
			subscriptionName: ProductSubscriptionName
		) =>
			this.sidePanelFrame.locator(
				`label[for$="_${SUBSCRIPTION_FIELD_IDS[subscriptionName].enabled}"]`
			);
		this.sidePanelSubscriptionLengthInput = (
			subscriptionName: ProductSubscriptionName
		) =>
			this.sidePanelFrame.locator(
				`input[id$="_${SUBSCRIPTION_FIELD_IDS[subscriptionName].length}"]`
			);
		this.sidePanelSubscriptionOverrideToggle =
			this.sidePanelFrame.getByLabel('Override Subscription Settings');
		this.sidePanelSubscriptionSection = (
			subscriptionName: ProductSubscriptionName
		) => this.sidePanelFrame.getByText(subscriptionName);
		this.sidePanelSubscriptionTypeSelect = (
			subscriptionName: ProductSubscriptionName
		) =>
			this.sidePanelFrame.locator(
				`select[id$="_${SUBSCRIPTION_FIELD_IDS[subscriptionName].type}"]`
			);
		this.sidePanelSubscriptionYearlyModeSelect = (
			subscriptionName: ProductSubscriptionName
		) =>
			this.sidePanelFrame.locator(
				`select[name$="_${SUBSCRIPTION_FIELD_IDS[subscriptionName].yearlyMode}"]`
			);
		this.sidePanelTierPriceAddButton = this.sidePanelNestedFrame.locator(
			'[data-testid="fdsCreationActionButton"]'
		);
		this.sidePanelTierPriceRow = (price: string) =>
			this.sidePanelNestedFrame.getByRole('row').filter({hasText: price});
		this.skusLink = page.getByRole('link', {
			exact: true,
			name: 'SKUs',
		});
		this.skuAddButton = page.locator(
			'[data-testid="fdsCreationActionButton"]'
		);
		this.skuAddModal = page.frameLocator('iframe').first();
		this.skuAddModalSkuInput = this.skuAddModal.getByLabel('SKU Required');
		this.skuAddModalSkuPublishButton = this.skuAddModal.getByRole(
			'button',
			{
				name: 'Publish',
			}
		);
		this.skuAddModalSkuPurchasableToggle = this.skuAddModal.getByLabel(
			'Purchasable',
			{exact: true}
		);
		this.skuAddModalSuccessMessage = this.skuAddModal.getByText(
			'Success:Your request completed successfully.'
		);
		this.skuPriceFrame = page.frameLocator('iframe').first();
		this.skuPriceAddButton = this.skuPriceFrame.locator(
			'[data-testid="fdsCreationActionButton"]'
		);
		this.skuPriceAddModal = page.frameLocator('iframe[title="Add Price"]');
		this.skuPriceAddModalAddButton = page
			.locator('.modal-footer')
			.getByRole('button', {exact: true, name: 'Add'});
		this.skuPriceAddModalAddEntryButton = this.skuPriceAddModal.getByRole(
			'button',
			{name: 'Add Entry'}
		);
		this.skuPriceAddModalCancelButton = page
			.locator('.modal-footer')
			.getByRole('button', {exact: true, name: 'Cancel'});
		this.skuPriceAddModalCurrency = (currencyCode: string) =>
			this.skuPriceAddModal.getByText(currencyCode, {exact: true});
		this.skuPriceAddModalUnitOfMeasureSelect =
			this.skuPriceAddModal.getByLabel('Unit of Measure');
		this.skuPriceAddModalUnitPriceInput =
			this.skuPriceAddModal.getByLabel('Unit Price');
		this.skuPriceListSelect =
			this.skuPriceAddModal.getByLabel('Price List');
		this.skuPriceMenuItem = (action: string) =>
			this.skuPriceFrame.getByRole('menuitem', {
				exact: true,
				name: action,
			});
		this.skuPriceTableRow = (priceListName: string) =>
			this.skuPriceFrame
				.getByRole('row')
				.filter({hasText: priceListName});
		this.skuTab = (skuTab) =>
			this.skuPriceFrame.getByRole('link', {
				exact: true,
				name: skuTab,
			});
		this.skusTable = page.locator(
			'#_com_liferay_commerce_product_definitions_web_internal_portlet_CPDefinitionsPortlet_fm .fds table'
		);
		this.skusTableRow = async (
			colPosition: number,
			value: number | string,
			strictEqual: boolean = false
		) => {
			return await searchTableRowByValue(
				this.skusTable,
				colPosition,
				String(value),
				strictEqual
			);
		};
		this.skusTableRowBasePrice = async (price: string) => {
			const shipmentTableRow = await this.skusTableRow(2, price, true);

			if (shipmentTableRow && shipmentTableRow.column) {
				return shipmentTableRow.row.getByText(price, {exact: true});
			}

			throw new Error(`Cannot locate shipment row with value ${price}`);
		};
		this.skusTableRowLink = (sku: string) =>
			page.getByRole('link', {exact: true, name: sku});
		this.skuUOMFrame = page.frameLocator('iframe').first();
		this.skuUOMModal = page.frameLocator('iframe >> nth=1');
		this.skuUOMTab = this.sidePanelFrame.getByRole('link', {
			name: 'Units of Measure',
		});
		this.skuUOMTable = this.sidePanelFrame.locator('.fds table');
		this.skuUOMTableRow = async (
			colPosition: number,
			value: number | string,
			strictEqual: boolean = false
		) => {
			return await searchTableRowByValue(
				this.skuUOMTable,
				colPosition,
				String(value),
				strictEqual
			);
		};
		this.skuUOMTableRowAction = async (skuUOMName: string) => {
			const skuUOMTableRow = await this.skuUOMTableRow(
				1,
				skuUOMName,
				true
			);

			if (skuUOMTableRow && skuUOMTableRow.column) {
				return skuUOMTableRow.row.getByRole('button', {
					name: 'Actions',
				});
			}
			throw new Error(
				`Cannot locate sku UOM row with name ${skuUOMName}`
			);
		};
		this.skuUOMActionMenuItem = (actionName: string) =>
			this.sidePanelFrame.getByRole('menuitem', {
				exact: true,
				name: actionName,
			});

		this.skuUOMFrameCancelButton = this.sidePanelFrame
			.frameLocator('iframe')
			.getByRole('button', {name: 'Cancel'});
		this.tierPriceModalFrame = page.frameLocator('.modal-body iframe');
		this.tierPriceModalPriceInput = this.tierPriceModalFrame.getByLabel(
			'Tier Price Required'
		);
		this.tierPriceModalQuantityInput =
			this.tierPriceModalFrame.getByLabel('Quantity Required');
		this.tierPriceModalSubmitButton = this.tierPriceModalFrame.getByRole(
			'button',
			{exact: true, name: 'Submit'}
		);
		this.uomTableRowLink = (uom: string) =>
			this.sidePanelFrame.getByRole('link', {exact: true, name: uom});
	}

	async addUOMEntry({
		baseUnitQuantity = '1',
		decimalAllowed = '0',
		skipModalOpening = false,
		uomKey,
		uomName,
	}) {
		if (!skipModalOpening) {
			await this.addSkuUOMButton.click();
		}

		await expect(
			this.skuUOMModal.getByRole('heading', {name: 'Add Unit of Measure'})
		).toBeVisible();

		await expect(
			this.skuUOMModal.getByLabel('Unit of Measure Required')
		).toBeVisible();

		await this.skuUOMModal
			.getByLabel('Unit of Measure Required')
			.fill(uomName);

		await this.skuUOMModal.getByLabel('Key Required').fill(uomKey);

		await this.skuUOMModal
			.getByLabel('Decimal Allowed (Precision)')
			.fill(decimalAllowed);

		await this.skuUOMModal
			.getByLabel('Base Unit Quantity Required')
			.fill(baseUnitQuantity);

		await this.skuUOMModal.getByRole('button', {name: 'Add'}).click();
	}

	async addWarehouseQuantity(quantity: string, warehouseName: string) {
		await (
			await this.inventoryTableRowAction(warehouseName)
		).fill(quantity);

		await (
			await this.inventoryTableRow(0, warehouseName, true)
		).row
			.getByRole('button', {
				exact: true,
				name: 'Save',
			})
			.click();
	}

	async clickSkuPriceAction(action: string, priceListName: string) {
		await this.skuPriceTableRow(priceListName)
			.getByRole('button', {name: /Actions$/})
			.click();
		await this.skuPriceMenuItem(action).click();
	}

	async clickTierPriceAction(action: string, price: string) {
		await this.sidePanelTierPriceRow(price)
			.getByRole('button', {name: /Actions$/})
			.click();
		await this.sidePanelNestedMenuItem(action).click();
	}

	async goToSkuUOM() {
		await this.skuUOMTab.click();
	}

	async goToSkuTab(skuTab: string) {
		await this.skuTab(skuTab).click();
	}

	async openSkuTab(sku: string, skuTab: string) {
		await this.skusTableRowLink(sku).click();

		await this.goToSkuTab(skuTab);
	}

	async submitTierPrice(price: string, quantity: string = '1') {
		await this.tierPriceModalQuantityInput.fill(quantity);
		await this.tierPriceModalPriceInput.fill(price);
		await this.tierPriceModalSubmitButton.click();
	}
}
