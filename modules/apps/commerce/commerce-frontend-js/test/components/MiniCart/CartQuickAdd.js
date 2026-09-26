/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '../../tests_utilities/polyfills';

import '@testing-library/jest-dom';
import {act, fireEvent, render, waitFor} from '@testing-library/react';
import fetchMock from 'fetch-mock';
import React from 'react';

import CartQuickAdd from '../../../src/main/resources/META-INF/resources/components/mini_cart/CartQuickAdd';
import MiniCartContext from '../../../src/main/resources/META-INF/resources/components/mini_cart/MiniCartContext';

describe('MiniCart Quick Add', () => {
	const SKU = 'MIN93015';

	afterEach(() => {
		fetchMock.restore();
	});

	it('adds the same SKU again with one more unit when it is already in the cart', async () => {
		fetchMock.get(new RegExp('/channels/1/products'), {
			items: [
				{
					name: 'Brake Pads',
					productConfiguration: {
						allowedOrderQuantities: [],
						maxOrderQuantity: 10000,
						minOrderQuantity: 1,
						multipleOrderQuantity: 1,
					},
					skus: [{id: 101, purchasable: true, sku: SKU}],
					urls: {},
				},
			],
			lastPage: 1,
			page: 1,
			pageSize: 100,
			totalCount: 1,
		});

		const addedQuantities = [];

		fetchMock.post(new RegExp('/carts/1/items'), (url, {body}) => {
			addedQuantities.push(JSON.parse(body).quantity);

			return {};
		});
		fetchMock.get(new RegExp('/carts/1\\?nestedFields=cartItems'), {
			cartItems: [],
			id: 1,
		});

		for (const [index, cartItems] of [
			[],
			[{quantity: 1, sku: SKU}],
		].entries()) {
			const {baseElement, getByLabelText, getByRole, unmount} = render(
				<MiniCartContext.Provider
					value={{
						cartState: {
							accountId: 1,
							cartItems,
							channel: {channel: {id: 1}},
							id: 1,
						},
					}}
				>
					<CartQuickAdd />
				</MiniCartContext.Provider>
			);

			fireEvent.change(getByRole('combobox'), {target: {value: SKU}});

			const skuItem = await waitFor(() => {
				const dropdownItem = [
					...baseElement.querySelectorAll('.dropdown-item'),
				].find((item) => item.textContent.includes(SKU));

				expect(dropdownItem).toBeDefined();

				return dropdownItem;
			});

			await act(async () => {
				fireEvent.click(skuItem);
			});

			expect(baseElement.querySelector('.label')).toHaveTextContent(SKU);

			await act(async () => {
				fireEvent.click(getByLabelText('add-to-cart'));
			});

			await waitFor(() =>
				expect(addedQuantities).toHaveLength(index + 1)
			);

			expect(addedQuantities[index]).toBe(1);

			unmount();
		}
	});
});
