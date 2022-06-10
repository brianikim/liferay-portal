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

package com.liferay.portal.internal.util;

import com.liferay.counter.kernel.service.CounterLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.configuration.Filter;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CustomizedPages;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.LayoutTemplate;
import com.liferay.portal.kernel.model.LayoutTypePortlet;
import com.liferay.portal.kernel.model.LayoutTypePortletConstants;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.model.PortletPreferencesIds;
import com.liferay.portal.kernel.model.PortletWrapper;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletIdCodec;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.PortletLocalServiceUtil;
import com.liferay.portal.kernel.service.PortletPreferencesLocalServiceUtil;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.permission.PortletPermissionUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PropsUtil;
import com.liferay.portal.util.PropsValues;
import com.liferay.util.JS;

import java.text.Format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.portlet.PortletPreferences;

/**
 * @author Michael Bowerman
 * @see com.liferay.portal.model.impl.LayoutTypePortletImpl
 */
public class LayoutTypePortletUtil {

	public static List<Portlet>
		getExplicitlyAddedPortletsWithoutCustomizableColumns(
			LayoutTypePortlet layoutTypePortlet) {

		List<Portlet> portlets = new ArrayList<>();

		List<String> columns = getColumns(layoutTypePortlet);

		for (String columnId : columns) {
			String customizableString =
				layoutTypePortlet.getTypeSettingsProperty(
					CustomizedPages.namespaceColumnId(columnId));

			boolean customizable = GetterUtil.getBoolean(customizableString);

			if (customizable && !isLayoutSetPrototype(layoutTypePortlet)) {
				continue;
			}

			portlets.addAll(layoutTypePortlet.getAllPortlets(columnId));
		}

		return portlets;
	}

	public static List<Portlet> getStaticPortlets(
		LayoutTypePortlet layoutTypePortlet) {

		String[] portletIds = getStaticPortletIds(layoutTypePortlet);

		List<Portlet> portlets = new ArrayList<>();

		for (String portletId : portletIds) {
			if (Validator.isNull(portletId) ||
				hasNonstaticPortletId(layoutTypePortlet, portletId)) {

				continue;
			}

			Layout layout = layoutTypePortlet.getLayout();

			Portlet portlet = PortletLocalServiceUtil.getPortletById(
				layout.getCompanyId(), portletId);

			if (portlet == null) {
				continue;
			}

			Portlet staticPortlet = portlet;

			if (portlet.isInstanceable()) {

				// Instanceable portlets do not need to be cloned because they
				// are already cloned. See the method getPortletById in the
				// class PortletLocalServiceImpl and how it references the
				// method getClonedInstance in the class PortletImpl.

			}
			else {
				staticPortlet = new PortletWrapper(portlet) {

					@Override
					public boolean getStatic() {
						return _staticPortlet;
					}

					@Override
					public boolean getStaticStart() {
						return _staticPortletStart;
					}

					@Override
					public boolean isStatic() {
						return _staticPortlet;
					}

					@Override
					public boolean isStaticStart() {
						return _staticPortletStart;
					}

					@Override
					public void setStatic(boolean staticPortlet) {
						_staticPortlet = staticPortlet;
					}

					@Override
					public void setStaticStart(boolean staticPortletStart) {
						_staticPortletStart = staticPortletStart;
					}

					private boolean _staticPortlet;
					private boolean _staticPortletStart;

				};
			}

			staticPortlet.setStatic(true);

			portlets.add(staticPortlet);
		}

		return portlets;
	}

	protected static void copyPreferences(
		Layout layout, long userId, String sourcePortletId,
		String targetPortletId) {

		try {
			PortletPreferencesIds portletPreferencesIds =
				PortletPreferencesFactoryUtil.getPortletPreferencesIds(
					layout.getGroupId(), 0, layout, sourcePortletId, false);

			PortletPreferences sourcePortletPreferences =
				PortletPreferencesLocalServiceUtil.getStrictPreferences(
					portletPreferencesIds);

			portletPreferencesIds =
				PortletPreferencesFactoryUtil.getPortletPreferencesIds(
					layout.getGroupId(), userId, layout, targetPortletId,
					false);

			UnicodeProperties typeSettingsUnicodeProperties =
				layout.getTypeSettingsProperties();

			String sourcePortletNamespace = PortalUtil.getPortletNamespace(
				sourcePortletId);

			for (Map.Entry<String, String> entry :
					typeSettingsUnicodeProperties.entrySet()) {

				String key = entry.getKey();

				if (key.startsWith(sourcePortletNamespace)) {
					String targetKey =
						PortalUtil.getPortletNamespace(targetPortletId) +
							key.substring(sourcePortletNamespace.length());

					sourcePortletPreferences.setValue(
						targetKey, entry.getValue());
				}
			}

			PortletPreferencesLocalServiceUtil.updatePreferences(
				portletPreferencesIds.getOwnerId(),
				portletPreferencesIds.getOwnerType(),
				portletPreferencesIds.getPlid(),
				portletPreferencesIds.getPortletId(), sourcePortletPreferences);
		}
		catch (Exception exception) {
		}
	}

	protected static void copyResourcePermissions(
		Layout layout, String sourcePortletId, String targetPortletId) {

		Portlet portlet = PortletLocalServiceUtil.getPortletById(
			layout.getCompanyId(), sourcePortletId);

		String sourcePortletPrimaryKey = PortletPermissionUtil.getPrimaryKey(
			layout.getPlid(), sourcePortletId);

		List<ResourcePermission> resourcePermissions =
			ResourcePermissionLocalServiceUtil.getResourcePermissions(
				portlet.getCompanyId(), portlet.getPortletName(),
				PortletKeys.PREFS_OWNER_TYPE_USER, sourcePortletPrimaryKey);

		for (ResourcePermission resourcePermission : resourcePermissions) {
			String targetPortletPrimaryKey =
				PortletPermissionUtil.getPrimaryKey(
					layout.getPlid(), targetPortletId);

			resourcePermission.setResourcePermissionId(
				CounterLocalServiceUtil.increment());
			resourcePermission.setPrimKey(targetPortletPrimaryKey);

			ResourcePermissionLocalServiceUtil.addResourcePermission(
				resourcePermission);
		}
	}

	protected static List<String> getColumns(
		LayoutTypePortlet layoutTypePortlet) {

		List<String> columns = new ArrayList<>();

		Layout layout = layoutTypePortlet.getLayout();

		if (layout.isTypePortlet()) {
			if (Objects.equals(
					layout.getType(),
					LayoutConstants.TYPE_FULL_PAGE_APPLICATION)) {

				columns.add("fullPageApplicationPortlet");
			}
			else {
				LayoutTemplate layoutTemplate =
					layoutTypePortlet.getLayoutTemplate();

				columns.addAll(layoutTemplate.getColumns());

				Collections.addAll(
					columns, getNestedColumns(layoutTypePortlet, layout));
			}
		}
		else if (layout.isTypePanel()) {
			columns.add("panelSelectedPortlets");
		}

		return columns;
	}

	protected static String getColumnValue(
		LayoutTypePortlet layoutTypePortlet, String columnId) {

		PortalPreferences portalPreferences =
			layoutTypePortlet.getPortalPreferences();

		if ((portalPreferences != null) && layoutTypePortlet.isCustomizable() &&
			!layoutTypePortlet.isColumnDisabled(columnId) &&
			!columnId.startsWith(
				PortalUtil.getPortletNamespace(PortletKeys.NESTED_PORTLETS))) {

			return getUserPreference(layoutTypePortlet, columnId);
		}

		if (PortletIdCodec.hasInstanceId(columnId) &&
			PortletIdCodec.hasUserId(columnId)) {

			PortletPreferences portletPreferences =
				_getUserColumnPortletPreferences(layoutTypePortlet, columnId);

			return portletPreferences.getValue(columnId, StringPool.BLANK);
		}

		return layoutTypePortlet.getTypeSettingsProperty(columnId);
	}

	protected static String[] getNestedColumns(
		LayoutTypePortlet layoutTypePortlet, Layout layout) {

		String[] nestedColumnIds = StringUtil.split(
			layoutTypePortlet.getTypeSettingsProperty(
				LayoutTypePortletConstants.NESTED_COLUMN_IDS));

		layoutTypePortlet.getPortalPreferences();

		PortalPreferences portalPreferences =
			layoutTypePortlet.getPortalPreferences();

		if (portalPreferences == null) {
			return nestedColumnIds;
		}

		for (String columnId : nestedColumnIds) {
			if (columnId.lastIndexOf(StringPool.DOUBLE_UNDERLINE) != -1) {
				columnId = columnId.substring(
					columnId.lastIndexOf(StringPool.DOUBLE_UNDERLINE) + 2);
			}

			String[] portletIds = StringUtil.split(
				portalPreferences.getValue(
					CustomizedPages.namespacePlid(layout.getPlid()), columnId));

			for (String portletId : portletIds) {
				PortletPreferences portletPreferences =
					PortletPreferencesFactoryUtil.getLayoutPortletSetup(
						layout, portletId);

				nestedColumnIds = ArrayUtil.append(
					nestedColumnIds, _getNestedColumnIds(portletPreferences));
			}
		}

		return nestedColumnIds;
	}

	protected static String[] getStaticPortletIds(
		LayoutTypePortlet layoutTypePortlet) {

		Layout layout = layoutTypePortlet.getLayout();

		Group group = null;

		try {
			group = layout.getGroup();
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception, exception);
			}
		}

		if (group == null) {
			_log.error("Unable to get group " + layout.getGroupId());

			return new String[0];
		}

		String selector1 = StringPool.BLANK;

		if (group.isUser()) {
			selector1 = LayoutTypePortletConstants.STATIC_PORTLET_USER_SELECTOR;
		}
		else if (group.isOrganization()) {
			selector1 =
				LayoutTypePortletConstants.STATIC_PORTLET_ORGANIZATION_SELECTOR;
		}
		else if (group.isRegularSite()) {
			selector1 =
				LayoutTypePortletConstants.STATIC_PORTLET_REGULAR_SITE_SELECTOR;
		}

		String selector2 = layout.getFriendlyURL();

		String[] portletIds = PropsUtil.getArray(
			PropsKeys.LAYOUT_STATIC_PORTLETS_ALL,
			new Filter(selector1, selector2));

		for (int i = 0; i < portletIds.length; i++) {
			portletIds[i] = JS.getSafeName(portletIds[i]);
		}

		return portletIds;
	}

	protected static String getUserPreference(
		LayoutTypePortlet layoutTypePortlet, String key) {

		String value = StringPool.BLANK;

		PortalPreferences portalPreferences =
			layoutTypePortlet.getPortalPreferences();

		if (portalPreferences == null) {
			return value;
		}

		Layout layout = layoutTypePortlet.getLayout();

		value = portalPreferences.getValue(
			CustomizedPages.namespacePlid(layout.getPlid()), key,
			StringPool.NULL);

		if (!value.equals(StringPool.NULL)) {
			return value;
		}

		value = layoutTypePortlet.getTypeSettingsProperty(key);

		if (Validator.isNull(value)) {
			return value;
		}

		List<String> newPortletIds = new ArrayList<>();

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		String[] portletIds = StringUtil.split(value);

		for (String portletId : portletIds) {
			try {
				if (!PortletPermissionUtil.contains(
						permissionChecker, layoutTypePortlet.getLayout(),
						portletId, ActionKeys.VIEW, true)) {

					continue;
				}

				String rootPortletId = PortletIdCodec.decodePortletName(
					portletId);

				if (!PortletPermissionUtil.contains(
						permissionChecker, rootPortletId,
						ActionKeys.ADD_TO_PAGE)) {

					continue;
				}
			}
			catch (Exception exception) {
				_log.error(exception, exception);
			}

			String newPortletId = null;

			boolean preferencesUniquePerLayout = false;

			try {
				Portlet portlet = PortletLocalServiceUtil.getPortletById(
					layout.getCompanyId(), portletId);

				preferencesUniquePerLayout =
					portlet.isPreferencesUniquePerLayout();
			}
			catch (SystemException systemException) {
				_log.error(systemException, systemException);
			}

			if (PortletIdCodec.hasInstanceId(portletId) ||
				preferencesUniquePerLayout) {

				String instanceId = null;

				if (PortletIdCodec.hasInstanceId(portletId)) {
					instanceId = PortletIdCodec.generateInstanceId();
				}

				newPortletId = PortletIdCodec.encode(
					PortletIdCodec.decodePortletName(portletId),
					portalPreferences.getUserId(), instanceId);

				copyPreferences(
					layout, portalPreferences.getUserId(), portletId,
					newPortletId);

				copyResourcePermissions(layout, portletId, newPortletId);
			}
			else {
				newPortletId = portletId;
			}

			newPortletIds.add(newPortletId);
		}

		value = StringUtil.merge(newPortletIds);

		setUserPreference(layoutTypePortlet, key, value);

		return value;
	}

	protected static boolean hasNonstaticPortletId(
		LayoutTypePortlet layoutTypePortlet, String portletId) {

		LayoutTemplate layoutTemplate = layoutTypePortlet.getLayoutTemplate();

		List<String> columns = layoutTemplate.getColumns();

		for (String columnId : columns) {
			if (hasNonstaticPortletId(layoutTypePortlet, columnId, portletId)) {
				return true;
			}
		}

		return false;
	}

	protected static boolean hasNonstaticPortletId(
		LayoutTypePortlet layoutTypePortlet, String columnId,
		String portletId) {

		String[] columnValues = StringUtil.split(
			getColumnValue(layoutTypePortlet, columnId));

		for (String nonstaticPortletId : columnValues) {
			if (nonstaticPortletId.equals(portletId)) {
				return true;
			}
		}

		return false;
	}

	protected static boolean isLayoutSetPrototype(
		LayoutTypePortlet layoutTypePortlet) {

		try {
			Layout layout = layoutTypePortlet.getLayout();

			Group group = layout.getGroup();

			return group.isLayoutSetPrototype();
		}
		catch (Exception exception) {
			_log.error(exception, exception);
		}

		return false;
	}

	protected static void setUserPreference(
		LayoutTypePortlet layoutTypePortlet, String key, String value) {

		PortalPreferences portalPreferences =
			layoutTypePortlet.getPortalPreferences();

		Layout layout = layoutTypePortlet.getLayout();

		portalPreferences.setValue(
			CustomizedPages.namespacePlid(layout.getPlid()), key, value);

		Format dateFormat = FastDateFormatFactoryUtil.getSimpleDateFormat(
			PropsValues.INDEX_DATE_FORMAT_PATTERN);

		portalPreferences.setValue(
			CustomizedPages.namespacePlid(layout.getPlid()), _MODIFIED_DATE,
			dateFormat.format(new Date()));
	}

	private static String[] _getNestedColumnIds(
		PortletPreferences portletPreferences) {

		Map<String, String[]> preferencesMap = portletPreferences.getMap();

		String[] columnIds = new String[0];

		for (String key : preferencesMap.keySet()) {
			if (!key.startsWith(
					PortalUtil.getPortletNamespace(
						PortletKeys.NESTED_PORTLETS))) {

				continue;
			}

			columnIds = ArrayUtil.append(columnIds, key);
		}

		return columnIds;
	}

	private static PortletPreferences _getUserColumnPortletPreferences(
		LayoutTypePortlet layoutTypePortlet, String columnId) {

		String instanceId = PortletIdCodec.decodeInstanceId(columnId);

		if (instanceId.indexOf(StringPool.UNDERLINE) != -1) {
			instanceId = instanceId.substring(
				0, instanceId.indexOf(StringPool.UNDERLINE));
		}

		long userId = PortletIdCodec.decodeUserId(columnId);

		String portletName = PortletIdCodec.decodePortletName(columnId);

		if (portletName.startsWith(StringPool.UNDERLINE)) {
			portletName = portletName.substring(1);
		}

		return PortletPreferencesFactoryUtil.getLayoutPortletSetup(
			layoutTypePortlet.getLayout(),
			PortletIdCodec.encode(portletName, userId, instanceId));
	}

	private static final String _MODIFIED_DATE = "modifiedDate";

	private static final Log _log = LogFactoryUtil.getLog(
		LayoutTypePortletUtil.class);

}