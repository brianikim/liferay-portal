/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export default async function CommerceCheckoutStep() {
	const commerceCheckoutStepContainer = document.getElementById(
		'_com_liferay_commerce_checkout_web_internal_portlet_CommerceCheckoutPortlet_commerceCheckoutStepContainer'
	);

	const paypalButtonContainerDivElement = document.createElement("div");
	paypalButtonContainerDivElement.setAttribute('id', "paypal-button-container");

	let paypalSdkScript = document.createElement( 'script' );

	paypalButtonContainerDivElement.appendChild( paypalSdkScript );
	commerceCheckoutStepContainer.appendChild(paypalButtonContainerDivElement);
}