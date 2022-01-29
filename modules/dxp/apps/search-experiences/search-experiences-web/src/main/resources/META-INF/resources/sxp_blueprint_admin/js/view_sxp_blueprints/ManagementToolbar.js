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

import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import {ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayManagementToolbar, {
	ClayResultsBar,
} from '@clayui/management-toolbar';
import getCN from 'classnames';
import React, {useContext, useState} from 'react';

import {sub} from '../utils/language';
import AddSXPBlueprintModal from './AddSXPBlueprintModal';
import PortletContext from './PortletContext';

const ManagementToolbar = ({
	loading,
	onBulkDelete,
	onSearch,
	onSelectAll,
	onSelectClear,
	searchValue,
	selected,
	totalCount,
	totalCurrentPageCount,
}) => {
	const {hasAddSXPBlueprintPermission} = useContext(PortletContext);

	const [searchInputValue, setSearchInputValue] = useState(searchValue);
	const [searchMobile, setSearchMobile] = useState(false);

	const _handleClearSearch = () => {
		onSearch('');
		onSelectClear();
		setSearchInputValue('');
	};

	return (
		<>
			<ClayManagementToolbar
				className={getCN({
					hide: selected.length,
				})}
			>
				<ClayManagementToolbar.Search showMobile={searchMobile}>
					<ClayInput.Group>
						<ClayInput.GroupItem>
							<ClayInput
								aria-label={Liferay.Language.get('search')}
								className="form-control input-group-inset input-group-inset-after"
								disabled={loading}
								onChange={(event) => {
									setSearchInputValue(event.target.value);
								}}
								onKeyDown={(event) => {
									if (event.key === 'Enter') {
										event.preventDefault();

										onSearch(searchInputValue);
									}
								}}
								placeholder={Liferay.Language.get('search')}
								type="text"
								value={searchInputValue}
							/>

							<ClayInput.GroupInsetItem after tag="span">
								<ClayButtonWithIcon
									className="navbar-breakpoint-d-none"
									displayType="unstyled"
									onClick={() => setSearchMobile(false)}
									symbol="times"
								/>

								<ClayButtonWithIcon
									disabled={loading}
									displayType="unstyled"
									onClick={() => onSearch(searchInputValue)}
									symbol="search"
								/>
							</ClayInput.GroupInsetItem>
						</ClayInput.GroupItem>
					</ClayInput.Group>
				</ClayManagementToolbar.Search>

				<ClayManagementToolbar.ItemList>
					<ClayManagementToolbar.Item className="navbar-breakpoint-d-none">
						<ClayButton
							className="nav-link nav-link-monospaced"
							displayType="unstyled"
							onClick={() => setSearchMobile(true)}
						>
							<ClayIcon symbol="search" />
						</ClayButton>
					</ClayManagementToolbar.Item>
				</ClayManagementToolbar.ItemList>

				{hasAddSXPBlueprintPermission && (
					<ClayManagementToolbar.ItemList>
						<ClayManagementToolbar.Item>
							<AddSXPBlueprintModal>
								<ClayButtonWithIcon
									className="nav-btn nav-btn-monospaced"
									symbol="plus"
								/>
							</AddSXPBlueprintModal>
						</ClayManagementToolbar.Item>
					</ClayManagementToolbar.ItemList>
				)}
			</ClayManagementToolbar>

			<ClayManagementToolbar
				className={getCN('management-bar-primary', {
					hide: !selected.length,
				})}
			>
				<ClayManagementToolbar.ItemList>
					<ClayManagementToolbar.Item className="navbar-form">
						<span className="component-text text-truncate-inline">
							<span className="text-truncate">
								{sub(Liferay.Language.get('x-of-x-selected'), [
									selected.length,
									totalCurrentPageCount,
								])}
							</span>
						</span>
					</ClayManagementToolbar.Item>

					{selected.length !== totalCurrentPageCount && (
						<ClayManagementToolbar.Item className="navbar-form">
							<ClayButton
								className="text-primary"
								displayType="link"
								onClick={onSelectAll}
								small
							>
								{Liferay.Language.get('select-all')}
							</ClayButton>
						</ClayManagementToolbar.Item>
					)}
				</ClayManagementToolbar.ItemList>

				<ClayManagementToolbar.ItemList>
					<ClayManagementToolbar.Item className="navbar-form">
						<ClayButton
							borderless
							displayType="secondary"
							monospaced
							onClick={onBulkDelete}
							small
						>
							<ClayIcon symbol="trash" />
						</ClayButton>
					</ClayManagementToolbar.Item>
				</ClayManagementToolbar.ItemList>
			</ClayManagementToolbar>

			{!!searchValue && !loading && (
				<ClayResultsBar>
					<ClayResultsBar.Item>
						<span className="component-text text-truncate-inline">
							<span className="text-truncate">
								{sub(
									totalCount === 1
										? Liferay.Language.get('x-result-for-x')
										: Liferay.Language.get(
												'x-results-for-x'
										  ),
									[totalCount, searchValue]
								)}
							</span>
						</span>
					</ClayResultsBar.Item>

					<ClayResultsBar.Item>
						<ClayButton
							className="component-link tbar-link"
							displayType="unstyled"
							onClick={_handleClearSearch}
						>
							{Liferay.Language.get('clear')}
						</ClayButton>
					</ClayResultsBar.Item>
				</ClayResultsBar>
			)}
		</>
	);
};

export default ManagementToolbar;
