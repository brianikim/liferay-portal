/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.organizations.internal.security.permission.contributor;

import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.security.permission.UserBag;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.permission.contributor.PermissionSQLContributor;

import java.util.Set;

import org.osgi.service.component.annotations.Component;

/**
 * @author Brian I. Kim
 */
@Component(
	property = "model.class.name=com.liferay.portal.kernel.model.Organization",
	service = PermissionSQLContributor.class
)
public class OrganizationPermissionSQLContributor
	implements PermissionSQLContributor {

	@Override
	public void collectPermittedClassPKs(
		String className, long userId, long[] groupIds,
		Set<Long> permittedClassPKs) {

		for (long userOrgId : _getUserOrgIds(null)) {
			permittedClassPKs.add(userOrgId);
		}
	}

	@Override
	public Predicate getPermissionPredicate(
		PermissionChecker permissionChecker, String className,
		Column<?, Long> classPKColumn, long[] groupIds) {

		long[] userOrgIds = _getUserOrgIds(permissionChecker);

		if (userOrgIds.length == 0) {
			return null;
		}

		return classPKColumn.in(ArrayUtil.toArray(userOrgIds));
	}

	@Override
	public String getPermissionSQL(
		String className, String classPKField, String groupIdField,
		long[] groupIds) {

		long[] userOrgIds = _getUserOrgIds(null);

		if (userOrgIds.length == 0) {
			return StringPool.BLANK;
		}

		return StringBundler.concat(
			classPKField, " IN (", StringUtil.merge(userOrgIds), ")");
	}

	private long[] _getUserOrgIds(PermissionChecker permissionChecker) {
		if (permissionChecker == null) {
			permissionChecker = PermissionThreadLocal.getPermissionChecker();
		}

		if (permissionChecker == null) {
			return new long[0];
		}

		try {
			UserBag userBag = permissionChecker.getUserBag();

			return userBag.getUserOrgIds();
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}

			return new long[0];
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OrganizationPermissionSQLContributor.class);

}