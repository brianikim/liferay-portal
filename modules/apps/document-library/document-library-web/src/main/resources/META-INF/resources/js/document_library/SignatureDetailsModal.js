/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayLoadingIndicator from '@clayui/loading-indicator';
import ClayModal from '@clayui/modal';
import {fetch} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

const DISPLAY_TYPES = {
	completed: 'success',
	declined: 'danger',
	expired: 'warning',
	voided: 'danger',
};

function getDisplayType(status) {
	return DISPLAY_TYPES[status] || 'info';
}

function formatDate(time) {
	if (!time) {
		return '';
	}

	return new Date(time).toLocaleString();
}

function StatusLabel({status}) {
	if (!status) {
		return null;
	}

	return (
		<span className={`label label-${getDisplayType(status)}`}>
			{Liferay.Language.get(status)}
		</span>
	);
}

function SignatureDetailsContent({detail}) {
	return (
		<div className="signature-details">
			<div className="signature-details-header">
				<StatusLabel status={detail.requestStatus} />

				<span className="signature-details-envelope text-secondary">
					{detail.providerRequestId}
				</span>
			</div>

			<div className="signature-details-card">
				<div className="signature-details-card-label text-secondary">
					{Liferay.Language.get('requester')}
				</div>

				<div className="signature-details-card-value">
					{detail.requesterName}
				</div>

				<div className="signature-details-card-value text-secondary">
					{detail.requesterEmailAddress}
				</div>
			</div>

			<h5>{Liferay.Language.get('recipients')}</h5>

			<table className="signature-details-table table table-list">
				<thead>
					<tr>
						<th>{Liferay.Language.get('name')}</th>

						<th>{Liferay.Language.get('email')}</th>

						<th>{Liferay.Language.get('status')}</th>

						<th>{Liferay.Language.get('date')}</th>
					</tr>
				</thead>

				<tbody>
					{detail.recipients.map((recipient, index) => (
						<tr key={index}>
							<td>{recipient.name}</td>

							<td>{recipient.emailAddress}</td>

							<td>
								<StatusLabel
									status={recipient.requestRecipientStatus}
								/>
							</td>

							<td>{formatDate(recipient.statusDate)}</td>
						</tr>
					))}
				</tbody>
			</table>

			<h5>{Liferay.Language.get('activity')}</h5>

			<ul className="signature-details-timeline">
				<li className="signature-details-timeline-item">
					<span className="signature-details-timeline-title">
						{Liferay.Language.get('envelope-created')}
					</span>

					<span className="text-secondary">
						{detail.requesterName} {String.fromCharCode(183)}{' '}

						{formatDate(detail.createDate)}
					</span>
				</li>

				{detail.completionDate ? (
					<li className="signature-details-timeline-item">
						<span className="signature-details-timeline-title">
							{Liferay.Language.get('completed')}
						</span>

						<span className="text-secondary">
							{formatDate(detail.completionDate)}
						</span>
					</li>
				) : null}
			</ul>

			<div className="signature-details-linked text-secondary">
				{Liferay.Language.get(
					'contract-quote-order-linking-is-planned-for-a-later-phase'
				)}
			</div>
		</div>
	);
}

export default function SignatureDetailsModal({fileEntryTitle, url}) {
	const [detail, setDetail] = useState(null);
	const [loading, setLoading] = useState(true);

	useEffect(() => {
		let mounted = true;

		fetch(url)
			.then((response) => response.json())
			.then((data) => {
				if (mounted) {
					setDetail(data);
					setLoading(false);
				}
			})
			.catch(() => {
				if (mounted) {
					setLoading(false);
				}
			});

		return () => {
			mounted = false;
		};
	}, [url]);

	return (
		<>
			<ClayModal.Header>
				{fileEntryTitle || Liferay.Language.get('signature-status')}
			</ClayModal.Header>

			<ClayModal.Body>
				{loading ? (
					<ClayLoadingIndicator />
				) : detail && detail.providerRequestId ? (
					<SignatureDetailsContent detail={detail} />
				) : (
					<div className="text-secondary">
						{Liferay.Language.get(
							'no-signature-request-was-found-for-this-document'
						)}
					</div>
				)}
			</ClayModal.Body>
		</>
	);
}
