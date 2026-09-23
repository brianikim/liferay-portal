/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLabel from '@clayui/label';
import PropTypes from 'prop-types';
import React from 'react';

const DISPLAY_TYPES = {
	completed: 'success',
	expired: 'warning',
	sent: 'info',
	voided: 'danger',
};

const STATUS_LABELS = {
	completed: Liferay.Language.get('completed'),
	expired: Liferay.Language.get('expired'),
	sent: Liferay.Language.get('sent'),
	voided: Liferay.Language.get('voided'),
};

export default function SignatureStatusDataRenderer({
	itemData,
	signatureStatuses,
}) {
	const status = signatureStatuses?.[itemData?.id];

	if (!status) {
		return null;
	}

	return (
		<ClayLabel displayType={DISPLAY_TYPES[status] || 'secondary'}>
			{STATUS_LABELS[status] || status}
		</ClayLabel>
	);
}

SignatureStatusDataRenderer.propTypes = {
	itemData: PropTypes.object,
	signatureStatuses: PropTypes.object,
};
