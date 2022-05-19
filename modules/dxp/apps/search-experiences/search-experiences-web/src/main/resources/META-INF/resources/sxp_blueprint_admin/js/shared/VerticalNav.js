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
import {ClayDropDownWithItems} from '@clayui/drop-down';
import ClayIcon from '@clayui/icon';
import getCN from 'classnames';
import React from 'react';

/**
 * A simple implementation of ClayVerticalNav. This is used in replacement
 * of the ClayVerticalNav in @clayui/nav, which currently throws an error due to
 * a missing function in the @clayui/shared package. See LPS-153048 for more
 * details.
 */
function VerticalNav({items}) {
	const _getActiveLabel = () => {
		return items.filter((item) => item.active)[0].label;
	};

	const _handleClick = (onClick) => (event) => {
		event.preventDefault();

		onClick();
	};

	return (
		<nav className="menubar menubar-transparent menubar-vertical-expand-md">
			<ClayDropDownWithItems
				items={items}
				trigger={
					<ClayButton
						className="menubar-toggler"
						displayType={null}
						role="button"
					>
						{_getActiveLabel()}

						<ClayIcon
							className="inline-item inline-item-after"
							symbol="caret-bottom"
						/>
					</ClayButton>
				}
			/>

			<div className="collapse menubar-collapse">
				<ul className="nav nav-nested-margins">
					{items.map(({active, label, onClick}, index) => (
						<li className="nav-item" key={index}>
							<button
								className={getCN(
									'btn',
									'btn-unstyled',
									'nav-link',
									{active}
								)}
								onClick={_handleClick(onClick)}
							>
								{label}
							</button>
						</li>
					))}
				</ul>
			</div>
		</nav>
	);
}

export default VerticalNav;
