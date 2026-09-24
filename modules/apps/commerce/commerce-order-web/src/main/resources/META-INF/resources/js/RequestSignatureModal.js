/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayForm, {ClayCheckbox, ClayInput, ClaySelect} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayModal from '@clayui/modal';
import {openToast} from 'frontend-js-components-web';
import {fetch} from 'frontend-js-web';
import PropTypes from 'prop-types';
import React, {useState} from 'react';

function moveRecipient(recipients, index, offset) {
	const movedRecipients = [...recipients];

	[movedRecipients[index], movedRecipients[index + offset]] = [
		movedRecipients[index + offset],
		movedRecipients[index],
	];

	return movedRecipients;
}

export default function RequestSignatureModal({
	attachments,
	closeModal,
	signatureRequest,
}) {
	const {
		accountUsers,
		addDSRequestsURL,
		buyerUserId,
		commerceOrderId,
		portletNamespace,
		searchUsersURL,
	} = signatureRequest;

	const [accountUserId, setAccountUserId] = useState('');
	const [emailMessage, setEmailMessage] = useState('');
	const [emailSubject, setEmailSubject] = useState(
		Liferay.Language.get('please-sign-this-document')
	);
	const [expireAfter, setExpireAfter] = useState('');
	const [keywords, setKeywords] = useState('');
	const [recipients, setRecipients] = useState(() =>
		accountUsers.filter(({userId}) => userId === buyerUserId)
	);
	const [searchUsers, setSearchUsers] = useState(null);
	const [sequential, setSequential] = useState(true);
	const [submitting, setSubmitting] = useState(false);

	const accountUserIds = accountUsers.map(({userId}) => userId);

	const availableAccountUsers = accountUsers.filter(
		({userId}) =>
			!recipients.some((recipient) => recipient.userId === userId)
	);

	const hasCountersigner = recipients.some(
		({userId}) => !accountUserIds.includes(userId)
	);

	const onAddAccountUser = () => {
		const accountUser = accountUsers.find(
			({userId}) => String(userId) === accountUserId
		);

		if (accountUser) {
			setRecipients([...recipients, accountUser]);
		}

		setAccountUserId('');
	};

	const onAddCountersigner = (user) => {
		setKeywords('');
		setRecipients([...recipients, user]);
		setSearchUsers(null);
	};

	const onSearch = () => {
		const url = new URL(searchUsersURL, window.location.href);

		url.searchParams.set(
			`${portletNamespace}commerceOrderId`,
			commerceOrderId
		);
		url.searchParams.set(`${portletNamespace}keywords`, keywords);

		fetch(url.toString())
			.then((response) => response.json())
			.then((users) =>
				setSearchUsers(
					users.filter(
						({userId}) =>
							!accountUserIds.includes(userId) &&
							!recipients.some(
								(recipient) => recipient.userId === userId
							)
					)
				)
			)
			.catch(() => setSearchUsers([]));
	};

	const onSubmit = (event) => {
		event.preventDefault();

		const formData = new FormData();

		attachments.forEach(({id}) =>
			formData.append(`${portletNamespace}commerceOrderAttachmentIds`, id)
		);

		formData.append(`${portletNamespace}commerceOrderId`, commerceOrderId);
		formData.append(`${portletNamespace}emailMessage`, emailMessage);
		formData.append(`${portletNamespace}emailSubject`, emailSubject);
		formData.append(`${portletNamespace}expireAfter`, expireAfter);

		formData.append(`${portletNamespace}sequential`, sequential);

		recipients.forEach(({userId}) =>
			formData.append(`${portletNamespace}recipientUserIds`, userId)
		);

		setSubmitting(true);

		fetch(addDSRequestsURL, {body: formData, method: 'POST'})
			.then((response) => {
				if (!response.ok) {
					throw new Error(response.statusText);
				}

				return response.json();
			})
			.then(({count}) => {
				if (!count) {
					throw new Error();
				}

				closeModal();

				openToast({
					message: Liferay.Language.get(
						'your-request-completed-successfully'
					),
					type: 'success',
				});

				window.location.reload();
			})
			.catch(() => {
				setSubmitting(false);

				openToast({
					message: Liferay.Language.get(
						'an-unexpected-error-occurred'
					),
					type: 'danger',
				});
			});
	};

	return (
		<ClayForm onSubmit={onSubmit}>
			<ClayModal.Header>
				{Liferay.Language.get('request-signature')}
			</ClayModal.Header>

			<ClayModal.Body>
				<h5>{Liferay.Language.get('documents')}</h5>

				<ul className="mb-4">
					{attachments.map(({id, title}) => (
						<li key={id}>{title}</li>
					))}
				</ul>

				<h5>{Liferay.Language.get('recipients')}</h5>

				<div className="mb-3">
					<ClayCheckbox
						checked={sequential}
						label={Liferay.Language.get(
							'recipients-sign-in-the-order-listed'
						)}
						onChange={() => setSequential(!sequential)}
					/>
				</div>

				<ol className="list-group mb-3">
					{recipients.map((recipient, index) => (
						<li
							className="align-items-center d-flex list-group-item"
							key={recipient.userId}
						>
							<div className="flex-grow-1">
								<div className="font-weight-semi-bold">
									{recipient.name}

									{!accountUserIds.includes(
										recipient.userId
									) && (
										<span className="label label-secondary ml-2">
											{Liferay.Language.get(
												'countersigner'
											)}
										</span>
									)}
								</div>

								<div className="text-secondary">
									{recipient.emailAddress}
								</div>
							</div>

							{sequential && (
								<>
									<ClayButton
										aria-label={Liferay.Language.get(
											'move-up'
										)}
										disabled={!index}
										displayType="unstyled"
										onClick={() =>
											setRecipients(
												moveRecipient(
													recipients,
													index,
													-1
												)
											)
										}
										title={Liferay.Language.get('move-up')}
									>
										<ClayIcon symbol="angle-up" />
									</ClayButton>

									<ClayButton
										aria-label={Liferay.Language.get(
											'move-down'
										)}
										disabled={
											index === recipients.length - 1
										}
										displayType="unstyled"
										onClick={() =>
											setRecipients(
												moveRecipient(
													recipients,
													index,
													1
												)
											)
										}
										title={Liferay.Language.get(
											'move-down'
										)}
									>
										<ClayIcon symbol="angle-down" />
									</ClayButton>
								</>
							)}

							<ClayButton
								aria-label={Liferay.Language.get('remove')}
								displayType="unstyled"
								onClick={() =>
									setRecipients(
										recipients.filter(
											(_, recipientIndex) =>
												recipientIndex !== index
										)
									)
								}
								title={Liferay.Language.get('remove')}
							>
								<ClayIcon symbol="times-circle" />
							</ClayButton>
						</li>
					))}
				</ol>

				{!!availableAccountUsers.length && (
					<ClayForm.Group className="d-flex">
						<ClaySelect
							aria-label={Liferay.Language.get('add-recipient')}
							className="mr-2"
							onChange={(event) =>
								setAccountUserId(event.target.value)
							}
							value={accountUserId}
						>
							<ClaySelect.Option label="" value="" />

							{availableAccountUsers.map(({name, userId}) => (
								<ClaySelect.Option
									key={userId}
									label={name}
									value={String(userId)}
								/>
							))}
						</ClaySelect>

						<ClayButton
							disabled={!accountUserId}
							displayType="secondary"
							onClick={onAddAccountUser}
						>
							{Liferay.Language.get('add-recipient')}
						</ClayButton>
					</ClayForm.Group>
				)}

				{!hasCountersigner && (
					<ClayForm.Group>
						<label htmlFor={`${portletNamespace}countersigner`}>
							{Liferay.Language.get('add-countersigner')}
						</label>

						<div className="d-flex">
							<ClayInput
								className="mr-2"
								id={`${portletNamespace}countersigner`}
								onChange={(event) =>
									setKeywords(event.target.value)
								}
								placeholder={Liferay.Language.get(
									'search-users'
								)}
								value={keywords}
							/>

							<ClayButton
								disabled={!keywords}
								displayType="secondary"
								onClick={onSearch}
							>
								{Liferay.Language.get('search')}
							</ClayButton>
						</div>

						{searchUsers &&
							(searchUsers.length ? (
								<ul className="list-group mt-2">
									{searchUsers.map((user) => (
										<li
											className="list-group-item"
											key={user.userId}
										>
											<ClayButton
												displayType="unstyled"
												onClick={() =>
													onAddCountersigner(user)
												}
											>
												{user.name}

												<span className="ml-2 text-secondary">
													{user.emailAddress}
												</span>
											</ClayButton>
										</li>
									))}
								</ul>
							) : (
								<div className="mt-2 text-secondary">
									{Liferay.Language.get(
										'no-users-were-found'
									)}
								</div>
							))}
					</ClayForm.Group>
				)}

				<ClayForm.Group>
					<label htmlFor={`${portletNamespace}emailSubject`}>
						{Liferay.Language.get('email-subject')}
					</label>

					<ClayInput
						id={`${portletNamespace}emailSubject`}
						onChange={(event) =>
							setEmailSubject(event.target.value)
						}
						required
						value={emailSubject}
					/>
				</ClayForm.Group>

				<ClayForm.Group>
					<label htmlFor={`${portletNamespace}emailMessage`}>
						{Liferay.Language.get('email-message')}
					</label>

					<ClayInput
						component="textarea"
						id={`${portletNamespace}emailMessage`}
						onChange={(event) =>
							setEmailMessage(event.target.value)
						}
						value={emailMessage}
					/>
				</ClayForm.Group>

				<ClayForm.Group>
					<label htmlFor={`${portletNamespace}expireAfter`}>
						{Liferay.Language.get('days-until-expiration')}
					</label>

					<ClayInput
						id={`${portletNamespace}expireAfter`}
						min="1"
						onChange={(event) => setExpireAfter(event.target.value)}
						type="number"
						value={expireAfter}
					/>
				</ClayForm.Group>
			</ClayModal.Body>

			<ClayModal.Footer
				last={
					<ClayButton.Group spaced>
						<ClayButton
							displayType="secondary"
							onClick={closeModal}
						>
							{Liferay.Language.get('cancel')}
						</ClayButton>

						<ClayButton
							disabled={
								submitting ||
								!recipients.length ||
								!emailSubject.trim()
							}
							type="submit"
						>
							{Liferay.Language.get('send')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</ClayForm>
	);
}

RequestSignatureModal.propTypes = {
	attachments: PropTypes.arrayOf(
		PropTypes.shape({
			id: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
			title: PropTypes.string,
		})
	).isRequired,
	closeModal: PropTypes.func.isRequired,
	signatureRequest: PropTypes.shape({
		accountUsers: PropTypes.arrayOf(
			PropTypes.shape({
				emailAddress: PropTypes.string,
				name: PropTypes.string,
				userId: PropTypes.number,
			})
		),
		addDSRequestsURL: PropTypes.string,
		buyerUserId: PropTypes.number,
		commerceOrderId: PropTypes.number,
		portletNamespace: PropTypes.string,
		searchUsersURL: PropTypes.string,
	}).isRequired,
};
