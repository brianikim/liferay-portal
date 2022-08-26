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

package com.liferay.portal.service.impl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.model.LayoutSetPrototype;
import com.liferay.portal.kernel.service.LayoutSetPrototypeLocalServiceUtil;
import com.liferay.portal.kernel.service.persistence.LayoutSetPrototypeUtil;
import com.liferay.portal.kernel.service.persistence.LayoutSetUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;

import java.util.Date;

/**
 * @author Raymond Augé
 */
public class LayoutSetPrototypeLayoutSetModelListener
	extends BaseModelListener<LayoutSet> {

	@Override
	public void onAfterUpdate(LayoutSet layoutSet) {
		updateLayoutSetPrototype(layoutSet, layoutSet.getModifiedDate());
	}

	@Override
	public void onBeforeUpdate(LayoutSet layoutSet) {
		if (layoutSet == null) {
			return;
		}

		Group group = _getGroup(layoutSet);

		if (group == null) {
			return;
		}

		try {
			LayoutSetPrototype layoutSetPrototype =
				LayoutSetPrototypeLocalServiceUtil.getLayoutSetPrototype(
					group.getClassPK());

			LayoutSet originalLayoutSet = layoutSetPrototype.getLayoutSet();

			UnicodeProperties originalSettingsUnicodeProperties =
				originalLayoutSet.getSettingsProperties();

			int originalMergeFailCount = GetterUtil.getInteger(
				originalSettingsUnicodeProperties.getProperty(
					"merge-fail-count"));

			UnicodeProperties settingsUnicodeProperties =
				layoutSet.getSettingsProperties();

			int mergeFailCount = GetterUtil.getInteger(
				settingsUnicodeProperties.getProperty("merge-fail-count"));

			if ((mergeFailCount == originalMergeFailCount) &&
				(mergeFailCount != 0)) {

				settingsUnicodeProperties.setProperty(
					"remove-merge-fail-count", "true");
			}

			LayoutSetUtil.updateImpl(originalLayoutSet);
		}
		catch (Exception exception) {
			_log.error(exception, exception);
		}
	}

	protected void updateLayoutSetPrototype(
		LayoutSet layoutSet, Date modifiedDate) {

		if (layoutSet == null) {
			return;
		}

		Group group = _getGroup(layoutSet);

		if (group == null) {
			return;
		}

		try {
			LayoutSetPrototype layoutSetPrototype =
				LayoutSetPrototypeLocalServiceUtil.getLayoutSetPrototype(
					group.getClassPK());

			layoutSetPrototype.setModifiedDate(layoutSet.getModifiedDate());

			LayoutSetPrototypeUtil.update(layoutSetPrototype);

			UnicodeProperties settingsUnicodeProperties =
				layoutSet.getSettingsProperties();

			boolean removeMergeFailCount = GetterUtil.getBoolean(
				settingsUnicodeProperties.getProperty(
					"remove-merge-fail-count"));

			if (removeMergeFailCount) {
				settingsUnicodeProperties.remove("merge-fail-count");
			}

			settingsUnicodeProperties.remove("remove-merge-fail-count");

			LayoutSetUtil.updateImpl(layoutSet);
		}
		catch (Exception exception) {
			_log.error(exception, exception);
		}
	}

	private Group _getGroup(LayoutSet layoutSet) {
		Group group = null;

		try {
			group = layoutSet.getGroup();

			if (!group.isLayoutSetPrototype()) {
				return null;
			}
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException, portalException);
			}

			return null;
		}

		return group;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LayoutSetPrototypeLayoutSetModelListener.class);

}