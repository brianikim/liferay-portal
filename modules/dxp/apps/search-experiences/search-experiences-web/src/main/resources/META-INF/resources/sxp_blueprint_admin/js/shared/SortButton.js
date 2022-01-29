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
import ClayIcon from '@clayui/icon';
import getCN from 'classnames';
import React from 'react';

/**
 * A button to change table sorting and direction. Created for the blueprints
 * and elements view pages to display next to the table headers.
 */
const SortButton = ({active, direction, onClick}) => {
	return (
		<ClayButton
			className="sort-button"
			displayType="unstyled"
			onClick={onClick}
		>
			<ClayIcon
				className={getCN({
					active: active && direction === 'asc',
				})}
				symbol="order-arrow-up"
			/>

			<ClayIcon
				className={getCN({
					active: active && direction === 'desc',
				})}
				symbol="order-arrow-down"
			/>
		</ClayButton>
	);
};

export default SortButton;
