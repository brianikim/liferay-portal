/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {waitFor} from '@testing-library/react';

import mountSigning from '../../../src/main/resources/META-INF/resources/js/sign/mountSigning';

describe('mountSigning', () => {
	afterEach(() => {
		delete window.DocuSign;
		delete navigator.sendBeacon;
	});

	it('records the signing outcome when the page is hidden before the session ends', async () => {
		const mount = jest.fn();

		window.DocuSign = {
			loadDocuSign: () =>
				Promise.resolve({
					signing: () => ({
						mount,
						on: jest.fn(),
					}),
				}),
		};

		navigator.sendBeacon = jest.fn();

		mountSigning({
			backURL: 'http://localhost/back',
			completeURL: 'http://localhost/complete',
			containerId: 'signingContainer',
			integrationKey: 'integrationKey',
			jsURL: 'http://localhost/docusign.js',
			signingURL: 'http://localhost/signing',
		});

		const script = document.head.querySelector(
			'script[src="http://localhost/docusign.js"]'
		);

		script.onload();

		await waitFor(() => expect(mount).toHaveBeenCalled());

		window.dispatchEvent(new Event('pagehide'));

		expect(navigator.sendBeacon).toHaveBeenCalledWith(
			'http://localhost/complete'
		);
	});
});
