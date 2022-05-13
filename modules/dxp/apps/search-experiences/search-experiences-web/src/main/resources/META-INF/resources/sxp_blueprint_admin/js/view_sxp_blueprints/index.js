/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

import ClayButton from '@clayui/button';
import {useResource} from '@clayui/data-provider';
import {ClayDropDownWithItems} from '@clayui/drop-down';
import ClayEmptyState from '@clayui/empty-state';
import {ClayCheckbox} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import ClayLink from '@clayui/link';
import ClayLoadingIndicator from '@clayui/loading-indicator';
import {ClayPaginationBarWithBasicItems} from '@clayui/pagination-bar';
import ClayTable from '@clayui/table';
import {fetch} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

import SortButton from '../shared/SortButton';
import {DEFAULT_ERROR} from '../utils/constants';
import {sub} from '../utils/language';
import {checkPermission} from '../utils/permissions';
import {formatDate, truncateString} from '../utils/strings';
import {openErrorToast, openSuccessToast} from '../utils/toasts';
import ManagementToolbar from './ManagementToolbar';
import PortletContext from './PortletContext';

const DEFAULT_DELTA = 20;
const DELTAS = [10, 20, 30, 50];
const TRUNCATE_LENGTH = 200;

const ViewSXPBlueprints = ({
	apiURL, // See ViewSXPBlueprintsDisplayContext#getAPIURL
	defaultLocale, // See view_sxp_blueprints.jsp
	deleteSXPBlueprintURL, // See view_sxp_blueprints.jsp
	editSXPBlueprintURL, // See view_sxp_blueprints.jsp
	formName, // See view_sxp_blueprints.jsp
	hasAddSXPBlueprintPermission, // See ViewSXPBlueprintsDisplayContext#hasAddSXPBlueprintPermission
	namespace, // See view_sxp_blueprints.jsp
}) => {
	const [activePage, setActivePage] = useState(1);
	const [delta, setDelta] = useState(DEFAULT_DELTA);
	const [search, setSearch] = useState('');
	const [selected, setSelected] = useState([]);
	const [sort, setSort] = useState('modifiedDate');
	const [sortOrder, setSortOrder] = useState('desc');

	const [networkState, setNetworkState] = useState(() => ({
		error: false,
		loading: false,
		networkStatus: 4,
	}));

	/**
	 * Immediately show loading spinner whenever a new search is performed.
	 * This is needed otherwise there is a delay before the spinner is shown.
	 */
	useEffect(() => {
		setNetworkState({
			error: false,
			loading: true,
			networkStatus: 4,
		});
	}, [activePage, delta, search, sort, sortOrder]);

	const {refetch, resource} = useResource({
		fetchOptions: {
			credentials: 'include',
			headers: new Headers({'x-csrf-token': Liferay.authToken}),
			method: 'GET',
		},
		fetchRetry: {
			attempts: 0,
		},
		link: `${
			window.location.origin
		}${themeDisplay.getPathContext()}${apiURL}`,
		onNetworkStatusChange: (status) => {
			setNetworkState({
				error: status === 5,
				loading: status < 4,
				networkStatus: status,
			});
		},
		variables: {
			page: activePage,
			pageSize: delta,
			search,
			sort: `${sort}:${sortOrder}`,
		},
	});

	const _setLoading = (loading) => {
		setNetworkState({
			error: false,
			loading,
			networkStatus: 4,
		});
	};

	const _handleActionDelete = (id, title) => () => {
		if (
			confirm(
				Liferay.Language.get('are-you-sure-you-want-to-delete-this')
			)
		) {
			try {
				_setLoading(true);

				fetch(`${apiURL}/${id}`, {
					method: 'DELETE',
				}).then(() => {
					openSuccessToast({
						message: sub(
							Liferay.Language.get('x-was-deleted-successfully'),
							[title]
						),
					});

					refetch();
				});
			}
			catch {
				openErrorToast();

				_setLoading(false);
			}
		}
	};

	const _handleActionCopy = (id) => () => {
		fetch(`${apiURL}/${id}/copy`, {
			method: 'POST',
		})
			.then(() => {
				openSuccessToast();

				refetch();
			})
			.catch(() => {
				openErrorToast();
			});
	};

	const _handleActionDownload = (id, title) => () => {
		fetch(`${apiURL}/${id}/export`, {
			method: 'GET',
		})
			.then((response) => {
				if (!response.ok) {
					throw DEFAULT_ERROR;
				}

				return response.blob();
			})
			.then((responseBlob) => {
				const downloadElement = document.createElement('a');

				downloadElement.download = title + '.json';
				downloadElement.href = URL.createObjectURL(responseBlob);

				document.body.appendChild(downloadElement);

				downloadElement.click();

				openSuccessToast();
			})
			.catch(() => {
				openErrorToast();
			});
	};

	const _handleBulkDelete = () => {
		if (
			confirm(
				selected.length > 1
					? Liferay.Language.get(
							'are-you-sure-you-want-to-delete-the-selected-entries'
					  )
					: Liferay.Language.get(
							'are-you-sure-you-want-to-delete-the-selected-entry'
					  )
			)
		) {
			Liferay.Util.postForm(
				document.getElementById(`${namespace}${formName}`),
				{

					// This depends on the hidden input field `${namespace}id`
					// instead of the `data` property. The `data` property is
					// still included since without it, the post request isn't
					// performed.

					data: {},
					url: deleteSXPBlueprintURL,
				}
			);
		}
	};

	const _handleSearch = (value) => {
		setSearch(value);
	};

	const _handleSelect = (id) => () => {
		setSelected(
			selected.includes(id)
				? selected.filter((preselectedId) => preselectedId !== id)
				: [...selected, id]
		);
	};

	const _handleSelectAll = () => {
		if (resource.items.length === selected.length) {
			setSelected([]);
		}
		else {
			setSelected(resource.items.map((item) => item.id));
		}
	};

	const _handleSelectClear = () => {
		setSelected([]);
	};

	const _handleSort = (selectedSort) => () => {
		setSort(selectedSort);
		setSortOrder(sortOrder === 'desc' ? 'asc' : 'desc');
	};

	const _getEditURL = (id) => {
		const url = new URL(editSXPBlueprintURL);

		url.searchParams.set(`${namespace}sxpBlueprintId`, id);

		return url.toString();
	};

	/**
	 * Used for conditionally rendering dropdown items.
	 * @param {object} item The item returned from the REST API.
	 * @return {Array} items for ClayDropDownWithItems
	 */
	const _getRowActionItems = ({actions, id, title}) => {
		const items = [];

		if (checkPermission('update', actions)) {
			items.push({
				href: _getEditURL(id),
				label: Liferay.Language.get('edit'),
				symbolLeft: 'pencil',
			});
		}

		if (checkPermission('create', actions)) {
			items.push({
				label: Liferay.Language.get('copy'),
				onClick: _handleActionCopy(id, title),
				symbolLeft: 'copy',
			});
		}

		if (checkPermission('get', actions)) {
			items.push({
				label: Liferay.Language.get('export'),
				onClick: _handleActionDownload(id, title),
				symbolLeft: 'download',
			});
		}

		if (checkPermission('delete', actions)) {
			items.push({
				label: Liferay.Language.get('delete'),
				onClick: _handleActionDelete(id, title),
				symbolLeft: 'trash',
			});
		}

		return items;
	};

	/**
	 * Handles what is displayed depending on loading/error/results/no results.
	 * @return The JSX to be rendered.
	 */
	const _renderDataTable = () => {

		// Loading

		if (networkState.loading) {
			return <ClayLoadingIndicator className="my-7" />;
		}

		// Error

		if (
			networkState.error ||
			resource?.status === 500 ||
			resource?.status === 504 ||
			resource?.status === 400
		) {
			return (
				<ClayEmptyState
					description={Liferay.Language.get(
						'an-error-has-occurred-and-we-were-unable-to-load-the-results'
					)}
					imgProps={{
						alt: Liferay.Language.get('unable-to-load-content'),
						title: Liferay.Language.get('unable-to-load-content'),
					}}
					imgSrc="/o/admin-theme/images/states/empty_state.gif"
					title={Liferay.Language.get('unable-to-load-content')}
				/>
			);
		}

		// Has Results

		if (resource?.totalCount > 0 && resource?.items.length) {
			return (
				<>
					<ClayTable
						borderedColumns
						headingNoWrap
						hover={false}
						striped
					>
						<ClayTable.Head>
							<ClayTable.Row>
								<ClayTable.Cell headingCell>
									<ClayCheckbox
										aria-label={Liferay.Language.get(
											'checkbox'
										)}
										checked={
											resource.items.length ===
											selected.length
										}
										indeterminate={
											selected.length &&
											resource.items.length !==
												selected.length
										}
										onChange={_handleSelectAll}
									/>
								</ClayTable.Cell>

								<ClayTable.Cell expanded headingCell>
									{Liferay.Language.get('title')}

									<SortButton
										active={sort === 'title'}
										direction={sortOrder}
										onClick={_handleSort('title')}
									/>
								</ClayTable.Cell>

								<ClayTable.Cell expanded headingCell>
									{Liferay.Language.get('description')}
								</ClayTable.Cell>

								<ClayTable.Cell
									className="table-cell-expand-smallest"
									headingCell
								>
									{Liferay.Language.get('id')}
								</ClayTable.Cell>

								<ClayTable.Cell
									className="table-cell-expand-smallest"
									headingCell
								>
									{Liferay.Language.get('author')}
								</ClayTable.Cell>

								<ClayTable.Cell
									className="table-cell-expand-smallest"
									headingCell
								>
									{Liferay.Language.get('created')}

									<SortButton
										active={sort === 'createDate'}
										direction={sortOrder}
										onClick={_handleSort('createDate')}
									/>
								</ClayTable.Cell>

								<ClayTable.Cell
									className="table-cell-expand-smallest"
									headingCell
								>
									{Liferay.Language.get('modified')}

									<SortButton
										active={sort === 'modifiedDate'}
										direction={sortOrder}
										onClick={_handleSort('modifiedDate')}
									/>
								</ClayTable.Cell>

								<ClayTable.Cell headingCell></ClayTable.Cell>
							</ClayTable.Row>
						</ClayTable.Head>

						<ClayTable.Body>
							{resource?.items?.map((item) => (
								<ClayTable.Row key={item.id}>
									<ClayTable.Cell>
										<ClayCheckbox
											aria-label={Liferay.Language.get(
												'checkbox'
											)}
											checked={selected.includes(item.id)}
											onChange={_handleSelect(item.id)}
										/>
									</ClayTable.Cell>

									<ClayTable.Cell expanded headingTitle>
										<ClayLink href={_getEditURL(item.id)}>
											{item.title}
										</ClayLink>
									</ClayTable.Cell>

									{item.description?.length >
									TRUNCATE_LENGTH ? (
										<ClayTable.Cell
											expanded
											title={item.description}
										>
											{truncateString(
												item.description,
												TRUNCATE_LENGTH
											)}
										</ClayTable.Cell>
									) : (
										<ClayTable.Cell expanded>
											{item.description}
										</ClayTable.Cell>
									)}

									<ClayTable.Cell>{item.id}</ClayTable.Cell>

									<ClayTable.Cell className="table-cell-expand-smallest">
										{item.userName}
									</ClayTable.Cell>

									<ClayTable.Cell className="table-cell-expand-smallest">
										{formatDate(item.createDate)}
									</ClayTable.Cell>

									<ClayTable.Cell className="table-cell-expand-smallest">
										{formatDate(item.modifiedDate)}
									</ClayTable.Cell>

									<ClayTable.Cell align="right">
										<ClayDropDownWithItems
											items={_getRowActionItems(item)}
											trigger={
												<ClayButton
													aria-label={Liferay.Language.get(
														'toggle-dropdown'
													)}
													className="component-action"
													displayType="unstyled"
													monospaced
												>
													<ClayIcon symbol="ellipsis-v" />
												</ClayButton>
											}
										/>
									</ClayTable.Cell>
								</ClayTable.Row>
							))}
						</ClayTable.Body>
					</ClayTable>

					<ClayPaginationBarWithBasicItems
						activeDelta={delta}
						activePage={activePage}
						deltas={DELTAS.map((delta) => ({
							label: delta,
						}))}
						ellipsisBuffer={3}
						onDeltaChange={setDelta}
						onPageChange={setActivePage}
						totalItems={resource?.totalCount || 0}
					/>
				</>
			);
		}

		// No Results

		return (
			<div className="sheet">
				<div className="border-0 pt-0 sheet taglib-empty-result-message">
					<div className="taglib-empty-result-message-header"></div>

					<div className="sheet-text text-center">
						{Liferay.Language.get('no-blueprints-were-found')}
					</div>
				</div>
			</div>
		);
	};

	return (
		<PortletContext.Provider
			value={{
				apiURL,
				defaultLocale,
				editSXPBlueprintURL,
				hasAddSXPBlueprintPermission,
				namespace,
			}}
		>
			<input
				name={`${namespace}id`}
				type="hidden"
				value={selected.join(',')}
			/>

			<ManagementToolbar
				loading={networkState.loading}
				onBulkDelete={_handleBulkDelete}
				onSearch={_handleSearch}
				onSelectAll={_handleSelectAll}
				onSelectClear={_handleSelectClear}
				searchValue={search}
				selected={selected}
				sortOrder={sortOrder}
				totalCount={resource?.totalCount}
				totalCurrentPageCount={resource?.items?.length}
			/>

			<ClayLayout.ContainerFluid view>
				{_renderDataTable()}
			</ClayLayout.ContainerFluid>
		</PortletContext.Provider>
	);
};

export default ViewSXPBlueprints;
