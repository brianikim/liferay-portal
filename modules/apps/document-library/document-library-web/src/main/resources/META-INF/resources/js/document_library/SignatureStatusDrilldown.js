/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const SELECTOR = '.ds-signature-status-trigger';

/**
 * Opens the Document Library info panel focused on a single document when its
 * signature status badge is clicked, drilling in to the per-document signature
 * status detail. Selecting the row lets the shared sidebar panel refetch the
 * detail; the panel is then revealed if it is currently closed.
 */
export default function ({infoPanelId, namespace}) {
	const entriesContainer = document.getElementById(
		`${namespace}entriesContainer`
	);

	if (!entriesContainer) {
		return {dispose() {}};
	}

	const handleClick = (event) => {
		const trigger = event.target.closest(SELECTOR);

		if (!trigger) {
			return;
		}

		event.preventDefault();

		const fileEntryId = trigger.getAttribute('data-file-entry-id');

		const checkboxes = document.querySelectorAll(
			`input[name="${namespace}rowIdsFileEntry"]`
		);

		checkboxes.forEach((checkbox) => {
			if (checkbox.checked !== (checkbox.value === fileEntryId)) {
				checkbox.click();
			}
		});

		const sidenav = document.getElementById(`${namespace}${infoPanelId}`);

		if (sidenav && sidenav.classList.contains('closed')) {
			const toggler = document.querySelector(
				`[data-target="#${namespace}${infoPanelId}"], [href="#${namespace}${infoPanelId}"]`
			);

			if (toggler) {
				toggler.click();
			}
		}
	};

	entriesContainer.addEventListener('click', handleClick);

	return {
		dispose() {
			entriesContainer.removeEventListener('click', handleClick);
		},
	};
}
