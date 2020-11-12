/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import React, {useCallback} from 'react';

import useSetRef from '../../../core/hooks/useSetRef';
import {getLayoutDataItemPropTypes} from '../../../prop-types/index';
import Topper from '../Topper';
import FragmentContent from '../fragment-content/FragmentContent';
import getAllPortals from './getAllPortals';

const FragmentWithControls = React.forwardRef(({item}, ref) => {
	const getPortals = useCallback((element) => getAllPortals(element), []);

	const [setRef, itemElement] = useSetRef(ref);

	return (
		<Topper item={item} itemElement={itemElement}>
			<FragmentContent
				elementRef={setRef}
				fragmentEntryLinkId={item.config.fragmentEntryLinkId}
				getPortals={getPortals}
				item={item}
				withinTopper
			/>
		</Topper>
	);
});

FragmentWithControls.displayName = 'FragmentWithControls';

FragmentWithControls.propTypes = {
	item: getLayoutDataItemPropTypes().isRequired,
};

export default FragmentWithControls;
