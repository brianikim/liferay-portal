/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export class AccountDisabledError extends Error {}

export default async function throwIfAccountDisabled(
	response: Response
): Promise<void> {
	if (response.status !== 403) {
		return;
	}

	const problem = await response.json().catch(() => null);

	if (problem?.type === 'AIHubAccountEntryDisabledException') {
		throw new AccountDisabledError(
			Liferay.Language.get(
				'this-account-has-been-disabled.-contact-your-liferay-representative-to-enable-it'
			)
		);
	}
}
