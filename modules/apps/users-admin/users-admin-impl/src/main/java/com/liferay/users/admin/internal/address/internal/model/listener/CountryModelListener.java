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

package com.liferay.users.admin.internal.address.internal.model.listener;

import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.RegionService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.permission.SimplePermissionChecker;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Drew Brokke
 */
@Component(immediate = true, service = ModelListener.class)
public class CountryModelListener extends BaseModelListener<Country> {

	@Override
	public void onAfterCreate(Country country) throws ModelListenerException {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		try {
			PermissionThreadLocal.setPermissionChecker(_permissionChecker);

			_processCountryRegions(country);
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
		}
	}

	private JSONArray _getJSONArray(String filePath) throws Exception {
		String regionsJSON = StringUtil.read(_classLoader, filePath, false);

		return _jsonFactory.createJSONArray(regionsJSON);
	}

	private void _processCountryRegions(Country country) {
		String a2 = country.getA2();

		try {
			String path =
				"com/liferay/users/admin/internal/address/dependencies/regions/" + a2 +
					".json";

			if (_classLoader.getResource(path) == null) {
				return;
			}

			JSONArray regionsJSONArray = _getJSONArray(path);

			if (_log.isDebugEnabled()) {
				_log.debug("Regions found for country " + a2);
			}

			for (int i = 0; i < regionsJSONArray.length(); i++) {
				try {
					JSONObject regionJSONObject =
						regionsJSONArray.getJSONObject(i);

					_regionService.addRegion(
						country.getCountryId(),
						regionJSONObject.getString("regionCode"),
						regionJSONObject.getString("name"),
						regionJSONObject.getBoolean("active"));
				}
				catch (PortalException portalException) {
					if (_log.isWarnEnabled()) {
						_log.warn(portalException, portalException);
					}
				}
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug("No regions found for country " + a2);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CountryModelListener.class);

	private static final PermissionChecker _permissionChecker =
		new SimplePermissionChecker() {

			@Override
			public boolean isOmniadmin() {
				return true;
			}

		};

	private final ClassLoader _classLoader =
		CountryModelListener.class.getClassLoader();

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private RegionService _regionService;

	@Reference
	private UserLocalService _userLocalService;

}