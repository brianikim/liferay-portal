/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {CommerceContext, liferayNavigate} from 'commerce-frontend-js';
import {fetch} from 'frontend-js-web';
import React from 'react';

function Header({actions}) {
	const onClick = (event, action) => {
		event.preventDefault();

		if (action.label === 'checkout') {
			setCurrentAccount(CommerceContext.account.accountId, action);
			liferayNavigate(action.href);
		}
		else if (action.label === 'reorder') {

			// todo postOrder or postCart

			setCurrentAccount(CommerceContext.account.accountId);
			setCurrentOrder(CommerceContext.order.orderId);
			liferayNavigate(action.href);
		}
		else if (action.label === 'submit') {

			// portletResponse is null within fragment context.

			fetch(action.href).then((response) => {
				if (response.ok) {
					window.location.reload();
				}
			});
		}
	};

	const setCurrent = (actionURL, id, name) => {
		const endpointURL = new URL(
			actionURL,
			Liferay.ThemeDisplay.getPortalURL()
		);

		endpointURL.searchParams.append(
			'groupId',
			Liferay.ThemeDisplay.getScopeGroupId()
		);
		endpointURL.searchParams.append('p_auth', Liferay.authToken);

		const body = new FormData();

		body.append(name, id);

		return fetch(endpointURL, {body, method: 'POST'});
	};

	const setCurrentAccount = (accountId) => {
		setCurrent(
			'/o/commerce-ui/set-current-account',
			accountId,
			'accountId'
		);
	};

	const setCurrentOrder = (orderId) => {
		setCurrent('/o/commerce-ui/set-current-order', orderId, 'orderId');
	};

	return (
		<div className="align-items-center c-py-3 c-py-lg-2 d-lg-flex">
			<div className="align-items-center c-ml-auto d-flex justify-content-end">
				<div>
					{actions.map((action) => (
						<div key={action.label}>
							<ClayButton
								className="btn c-mb-1 c-mb-sm-0"
								displayType="primary"
								label={action.label}
								onClick={(event) => onClick(event, action)}
							/>
						</div>
					))}
				</div>
			</div>
		</div>
	);
}

Header.defaultProps = {
	actions: {},
};

export default Header;
