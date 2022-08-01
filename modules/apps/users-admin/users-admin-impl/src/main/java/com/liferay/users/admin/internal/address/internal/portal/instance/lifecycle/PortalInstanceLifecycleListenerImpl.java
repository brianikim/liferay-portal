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

package com.liferay.users.admin.internal.address.internal.portal.instance.lifecycle;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CountryService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.permission.SimplePermissionChecker;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Drew Brokke
 */
@Component(service = PortalInstanceLifecycleListener.class)
public class PortalInstanceLifecycleListenerImpl
	extends BasePortalInstanceLifecycleListener {

	public void portalInstanceRegistered(Company company) throws Exception {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		try {
			PermissionThreadLocal.setPermissionChecker(_permissionChecker);

			doPortalInstanceRegistered(company);
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
		}
	}

	protected void doPortalInstanceRegistered(Company company)
		throws Exception {

		List<Country> countries = _countryService.getCountries();

		if (!countries.isEmpty()) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"Skipping country initialization. Countries are ",
						"already initialized for company ",
						company.getCompanyId(), "."));
			}

			return;
		}

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Initializing countries for company " + company.getCompanyId());
		}

		JSONArray countriesJSONArray = _getJSONArray(
			"com/liferay/users/admin/internal/address/dependencies/countries." +
				"json");

		for (int i = 0; i < countriesJSONArray.length(); i++) {
			JSONObject countryJSONObject = countriesJSONArray.getJSONObject(i);

			try {
				String name = countryJSONObject.getString("name");

				ServiceContext serviceContext = new ServiceContext();

				serviceContext.setCompanyId(company.getCompanyId());

				User defaultUser = company.getDefaultUser();

				serviceContext.setUserId(defaultUser.getUserId());

				_countryService.addCountry(
					name, countryJSONObject.getString("a2"),
					countryJSONObject.getString("a3"),
					countryJSONObject.getString("number"),
					countryJSONObject.getString("idd"),
					countryJSONObject.getBoolean("active"));
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(exception.getMessage());
				}
			}
		}
	}

	private JSONArray _getJSONArray(String filePath) throws Exception {
		String regionsJSON = StringUtil.read(getClassLoader(), filePath, false);

		return _jsonFactory.createJSONArray(regionsJSON);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PortalInstanceLifecycleListenerImpl.class);

	private static final PermissionChecker _permissionChecker =
		new SimplePermissionChecker() {

			@Override
			public boolean isOmniadmin() {
				return true;
			}

		};

	@Reference
	private CountryService _countryService;

	@Reference
	private JSONFactory _jsonFactory;

}