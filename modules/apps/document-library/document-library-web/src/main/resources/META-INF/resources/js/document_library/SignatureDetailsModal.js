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
	signed: 'success',
	voided: 'danger',
};

const SEPARATOR = ` ${String.fromCharCode(183)} `;

function getDisplayType(status) {
	return DISPLAY_TYPES[status] || 'info';
}

function formatDate(time) {
	if (!time) {
		return '';
	}

	return new Date(time).toLocaleString();
}

function getActivities(detail) {
	const activities = [
		{
			detail:
				detail.requesterName +
				SEPARATOR +
				formatDate(detail.createDate),
			title: Liferay.Language.get('envelope-created'),
			type: 'success',
		},
	];

	detail.recipients.forEach((recipient) => {
		activities.push({
			detail:
				Liferay.Language.get(recipient.requestRecipientStatus) +
				SEPARATOR +
				formatDate(recipient.statusDate),
			title: recipient.name,
			type: getDisplayType(recipient.requestRecipientStatus),
		});
	});

	if (detail.completionDate) {
		activities.push({
			detail: formatDate(detail.completionDate),
			title: Liferay.Language.get('completed'),
			type: 'success',
		});
	}

	return activities;
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
			<div className="mb-4">
				<StatusLabel status={detail.requestStatus} />
			</div>

			<div className="bg-light border mb-4 p-3 rounded">
				<div className="small text-secondary text-uppercase">
					{Liferay.Language.get('requester')}
				</div>

				<div className="font-weight-semi-bold">
					{detail.requesterName}
				</div>

				<div className="mb-3 text-secondary">
					{detail.requesterEmailAddress}
				</div>

				<div className="small text-secondary text-uppercase">
					{Liferay.Language.get('envelope-id')}
				</div>

				<div>{detail.providerRequestId}</div>
			</div>

			<h5>{Liferay.Language.get('recipients')}</h5>

			<table className="mb-4 table table-list">
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

			<div className="timeline">
				{getActivities(detail).map((activity, index) => (
					<div className="timeline-item" key={index}>
						<div className="timeline-increment">
							<span
								className={`signature-details-dot bg-${activity.type}`}
							/>
						</div>

						<div className="timeline-item-label">
							<div className="font-weight-semi-bold">
								{activity.title}
							</div>

							<div className="text-secondary">
								{activity.detail}
							</div>
						</div>
					</div>
				))}
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
