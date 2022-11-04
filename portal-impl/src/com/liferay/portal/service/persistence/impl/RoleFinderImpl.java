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

package com.liferay.portal.service.persistence.impl;

import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.DSLFunctionFactoryUtil;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.sql.dsl.expression.Expression;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.petra.sql.dsl.query.FromStep;
import com.liferay.petra.sql.dsl.query.OrderByStep;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryPos;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.SQLQuery;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.dao.orm.Type;
import com.liferay.portal.kernel.exception.NoSuchRoleException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceAction;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleTable;
import com.liferay.portal.kernel.model.Team;
import com.liferay.portal.kernel.model.TeamTable;
import com.liferay.portal.kernel.security.permission.InlineSQLHelperUtil;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.service.ResourceActionLocalServiceUtil;
import com.liferay.portal.kernel.service.persistence.RoleFinder;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.impl.RoleImpl;
import com.liferay.util.dao.orm.CustomSQLUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Brian Wing Shun Chan
 * @author Marcellus Tavares
 * @author Connor McKay
 */
public class RoleFinderImpl extends RoleFinderBaseImpl implements RoleFinder {

	public static final String COUNT_BY_ORGANIZATION =
		RoleFinder.class.getName() + ".countByOrganization";

	public static final String COUNT_BY_ORGANIZATION_SITE =
		RoleFinder.class.getName() + ".countByOrganizationSite";

	public static final String COUNT_BY_SITE =
		RoleFinder.class.getName() + ".countBySite";

	public static final String COUNT_BY_USER =
		RoleFinder.class.getName() + ".countByUser";

	public static final String COUNT_BY_USER_GROUP =
		RoleFinder.class.getName() + ".countByUserGroup";

	public static final String COUNT_BY_USER_GROUP_GROUP_ROLE =
		RoleFinder.class.getName() + ".countByUserGroupGroupRole";

	public static final String COUNT_BY_USER_GROUP_SITE =
		RoleFinder.class.getName() + ".countByUserGroupSite";

	public static final String COUNT_BY_U_G_R =
		RoleFinder.class.getName() + ".countByU_G_R";

	public static final String COUNT_BY_C_N_D_T =
		RoleFinder.class.getName() + ".countByC_N_D_T";

	public static final String FIND_BY_SYSTEM =
		RoleFinder.class.getName() + ".findBySystem";

	public static final String FIND_BY_TEAMS_USER =
		RoleFinder.class.getName() + ".findByTeamsUser";

	public static final String FIND_BY_USER_GROUP_GROUP_ROLE =
		RoleFinder.class.getName() + ".findByUserGroupGroupRole";

	public static final String FIND_BY_USER_GROUP_ROLE =
		RoleFinder.class.getName() + ".findByUserGroupRole";

	public static final String FIND_BY_C_N =
		RoleFinder.class.getName() + ".findByC_N";

	public static final String FIND_BY_U_G =
		RoleFinder.class.getName() + ".findByU_G";

	public static final String FIND_BY_C_N_D_T =
		RoleFinder.class.getName() + ".findByC_N_D_T";

	public static final String FIND_BY_C_N_S_P =
		RoleFinder.class.getName() + ".findByC_N_S_P";

	public static final String FIND_BY_C_N_S_P_A =
		RoleFinder.class.getName() + ".findByC_N_S_P_A";

	public static final String JOIN_BY_USERS_ROLES =
		RoleFinder.class.getName() + ".joinByUsersRoles";

	@Override
	public int countByGroupRoleAndTeamRole(
		long companyId, String keywords, List<String> excludedNames,
		int[] types, long excludedTeamRoleId, long teamGroupId) {

		return doCountByGroupRoleAndTeamRole(
			companyId, keywords, excludedNames, types, excludedTeamRoleId,
			teamGroupId, false);
	}

	@Override
	public int countByKeywords(
		long companyId, String keywords, Integer[] types) {

		return countByKeywords(
			companyId, keywords, types, new LinkedHashMap<String, Object>());
	}

	@Override
	public int countByKeywords(
		long companyId, String keywords, Integer[] types,
		LinkedHashMap<String, Object> params) {

		String[] names = null;
		String[] descriptions = null;
		boolean andOperator = false;

		if (Validator.isNotNull(keywords)) {
			names = CustomSQLUtil.keywords(keywords);
			descriptions = CustomSQLUtil.keywords(keywords);
		}
		else {
			andOperator = true;
		}

		return countByC_N_D_T(
			companyId, names, descriptions, types, params, andOperator);
	}

	@Override
	public int countByUserGroupGroupRole(long userId, long groupId) {
		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(COUNT_BY_USER_GROUP_GROUP_ROLE);

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addScalar(COUNT_COLUMN_NAME, Type.LONG);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			queryPos.add(groupId);
			queryPos.add(userId);

			Iterator<Long> iterator = sqlQuery.iterate();

			if (iterator.hasNext()) {
				Long count = iterator.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	public int countByR_U(long roleId, long userId) {
		Session session = null;

		try {
			session = openSession();

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(
				getCountByR_U_SQL());

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			for (int i = 0; i < 6; i++) {
				queryPos.add(roleId);
				queryPos.add(userId);
			}

			List<Role> roles = sqlQuery.list();

			return roles.size();
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	public int countByU_G_R(long userId, long groupId, long roleId) {
		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(COUNT_BY_U_G_R);

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addScalar(COUNT_COLUMN_NAME, Type.LONG);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			queryPos.add(roleId);
			queryPos.add(groupId);
			queryPos.add(userId);

			Iterator<Long> iterator = sqlQuery.iterate();

			if (iterator.hasNext()) {
				Long count = iterator.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	public int countByC_N_D_T(
		long companyId, String name, String description, Integer[] types,
		LinkedHashMap<String, Object> params, boolean andOperator) {

		String[] names = CustomSQLUtil.keywords(name);
		String[] descriptions = CustomSQLUtil.keywords(description);

		return countByC_N_D_T(
			companyId, names, descriptions, types, params, andOperator);
	}

	@Override
	public int countByC_N_D_T(
		long companyId, String[] names, String[] descriptions, Integer[] types,
		LinkedHashMap<String, Object> params, boolean andOperator) {

		return doCountByC_N_D_T(
			companyId, names, descriptions, types, params, andOperator, false);
	}

	@Override
	public int filterCountByGroupRoleAndTeamRole(
		long companyId, String keywords, List<String> excludedNames,
		int[] types, long excludedTeamRoleId, long teamGroupId) {

		return doCountByGroupRoleAndTeamRole(
			companyId, keywords, excludedNames, types, excludedTeamRoleId,
			teamGroupId, true);
	}

	@Override
	public int filterCountByKeywords(
		long companyId, String keywords, Integer[] types,
		LinkedHashMap<String, Object> params) {

		String[] names = null;
		String[] descriptions = null;
		boolean andOperator = false;

		if (Validator.isNotNull(keywords)) {
			names = CustomSQLUtil.keywords(keywords);
			descriptions = CustomSQLUtil.keywords(keywords);
		}
		else {
			andOperator = true;
		}

		return filterCountByC_N_D_T(
			companyId, names, descriptions, types, params, andOperator);
	}

	@Override
	public int filterCountByC_N_D_T(
		long companyId, String name, String description, Integer[] types,
		LinkedHashMap<String, Object> params, boolean andOperator) {

		String[] names = CustomSQLUtil.keywords(name);
		String[] descriptions = CustomSQLUtil.keywords(description);

		return filterCountByC_N_D_T(
			companyId, names, descriptions, types, params, andOperator);
	}

	@Override
	public int filterCountByC_N_D_T(
		long companyId, String[] names, String[] descriptions, Integer[] types,
		LinkedHashMap<String, Object> params, boolean andOperator) {

		return doCountByC_N_D_T(
			companyId, names, descriptions, types, params, andOperator, true);
	}

	@Override
	public List<Role> filterFindByGroupRoleAndTeamRole(
		long companyId, String keywords, List<String> excludedNames,
		int[] types, long excludedTeamRoleId, long teamGroupId, int start,
		int end) {

		return doFindByGroupRoleAndTeamRole(
			companyId, keywords, excludedNames, types, excludedTeamRoleId,
			teamGroupId, start, end, true);
	}

	@Override
	public List<Role> filterFindByKeywords(
		long companyId, String keywords, Integer[] types,
		LinkedHashMap<String, Object> params, int start, int end,
		OrderByComparator<Role> orderByComparator) {

		String[] names = null;
		String[] descriptions = null;
		boolean andOperator = false;

		if (Validator.isNotNull(keywords)) {
			names = CustomSQLUtil.keywords(keywords);
			descriptions = CustomSQLUtil.keywords(keywords);
		}
		else {
			andOperator = true;
		}

		return filterFindByC_N_D_T(
			companyId, names, descriptions, types, params, andOperator, start,
			end, orderByComparator);
	}

	@Override
	public List<Role> filterFindByC_N_D_T(
		long companyId, String name, String description, Integer[] types,
		LinkedHashMap<String, Object> params, boolean andOperator, int start,
		int end, OrderByComparator<Role> orderByComparator) {

		String[] names = CustomSQLUtil.keywords(name);
		String[] descriptions = CustomSQLUtil.keywords(description);

		return filterFindByC_N_D_T(
			companyId, names, descriptions, types, params, andOperator, start,
			end, orderByComparator);
	}

	@Override
	public List<Role> filterFindByC_N_D_T(
		long companyId, String[] names, String[] descriptions, Integer[] types,
		LinkedHashMap<String, Object> params, boolean andOperator, int start,
		int end, OrderByComparator<Role> orderByComparator) {

		return doFindByC_N_D_T(
			companyId, names, descriptions, types, params, andOperator, start,
			end, orderByComparator, true);
	}

	@Override
	public List<Role> findByGroupRoleAndTeamRole(
		long companyId, String keywords, List<String> excludedNames,
		int[] types, long excludedTeamRoleId, long teamGroupId, int start,
		int end) {

		return doFindByGroupRoleAndTeamRole(
			companyId, keywords, excludedNames, types, excludedTeamRoleId,
			teamGroupId, start, end, false);
	}

	@Override
	public List<Role> findByKeywords(
		long companyId, String keywords, Integer[] types, int start, int end,
		OrderByComparator<Role> orderByComparator) {

		return findByKeywords(
			companyId, keywords, types, new LinkedHashMap<String, Object>(),
			start, end, orderByComparator);
	}

	@Override
	public List<Role> findByKeywords(
		long companyId, String keywords, Integer[] types,
		LinkedHashMap<String, Object> params, int start, int end,
		OrderByComparator<Role> orderByComparator) {

		String[] names = null;
		String[] descriptions = null;
		boolean andOperator = false;

		if (Validator.isNotNull(keywords)) {
			names = CustomSQLUtil.keywords(keywords);
			descriptions = CustomSQLUtil.keywords(keywords);
		}
		else {
			andOperator = true;
		}

		return findByC_N_D_T(
			companyId, names, descriptions, types, params, andOperator, start,
			end, orderByComparator);
	}

	/**
	 * @deprecated As of Athanasius (7.3.x), with no direct replacement
	 */
	@Deprecated
	@Override
	public List<Role> findBySystem(long companyId) {
		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(FIND_BY_SYSTEM);

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addEntity("Role_", RoleImpl.class);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			queryPos.add(companyId);

			return sqlQuery.list(true);
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	public List<Role> findByTeamsUser(long userId, long groupId) {
		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(FIND_BY_TEAMS_USER);

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addEntity("Role_", RoleImpl.class);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			queryPos.add(groupId);
			queryPos.add(userId);
			queryPos.add(groupId);
			queryPos.add(userId);

			return sqlQuery.list(true);
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	public List<Role> findByUserGroupGroupRole(long userId, long groupId) {
		return findByUserGroupGroupRole(
			userId, groupId, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
	}

	@Override
	public List<Role> findByUserGroupGroupRole(
		long userId, long groupId, int start, int end) {

		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(FIND_BY_USER_GROUP_GROUP_ROLE);

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addEntity("Role_", RoleImpl.class);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			queryPos.add(userId);
			queryPos.add(groupId);

			return (List<Role>)QueryUtil.list(
				sqlQuery, getDialect(), start, end);
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	public List<Role> findByUserGroupRole(long userId, long groupId) {
		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(FIND_BY_USER_GROUP_ROLE);

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addEntity("Role_", RoleImpl.class);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			queryPos.add(userId);
			queryPos.add(groupId);

			return sqlQuery.list(true);
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	public Role findByC_N(long companyId, String name)
		throws NoSuchRoleException {

		name = StringUtil.lowerCase(name);

		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(FIND_BY_C_N);

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addEntity("Role_", RoleImpl.class);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			queryPos.add(companyId);
			queryPos.add(name);

			List<Role> roles = sqlQuery.list();

			if (!roles.isEmpty()) {
				return roles.get(0);
			}
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}

		StringBundler sb = new StringBundler(5);

		sb.append("No Role exists with the key {companyId=");
		sb.append(companyId);
		sb.append(", name=");
		sb.append(name);
		sb.append("}");

		throw new NoSuchRoleException(sb.toString());
	}

	@Override
	public List<Role> findByU_G(long userId, List<Group> groups) {
		long[] groupIds = new long[groups.size()];

		for (int i = 0; i < groups.size(); i++) {
			Group group = groups.get(i);

			groupIds[i] = group.getGroupId();
		}

		return findByU_G(userId, groupIds);
	}

	@Override
	public List<Role> findByU_G(long userId, long groupId) {
		return findByU_G(userId, new long[] {groupId});
	}

	@Override
	public List<Role> findByU_G(long userId, long[] groupIds) {
		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(FIND_BY_U_G);

			sql = StringUtil.replace(
				sql, "[$GROUP_ID$]", getGroupIds(groupIds, "Groups_Roles"));

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addEntity("Role_", RoleImpl.class);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			queryPos.add(userId);

			return sqlQuery.list(true);
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	public List<Role> findByC_N_D_T(
		long companyId, String name, String description, Integer[] types,
		LinkedHashMap<String, Object> params, boolean andOperator, int start,
		int end, OrderByComparator<Role> orderByComparator) {

		String[] names = CustomSQLUtil.keywords(name);
		String[] descriptions = CustomSQLUtil.keywords(description);

		return findByC_N_D_T(
			companyId, names, descriptions, types, params, andOperator, start,
			end, orderByComparator);
	}

	@Override
	public List<Role> findByC_N_D_T(
		long companyId, String[] names, String[] descriptions, Integer[] types,
		LinkedHashMap<String, Object> params, boolean andOperator, int start,
		int end, OrderByComparator<Role> orderByComparator) {

		return doFindByC_N_D_T(
			companyId, names, descriptions, types, params, andOperator, start,
			end, orderByComparator, false);
	}

	@Override
	public Map<String, List<String>> findByC_N_S_P(
		long companyId, String name, int scope, String primKey) {

		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(FIND_BY_C_N_S_P);

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addScalar("roleName", Type.STRING);
			sqlQuery.addScalar("actionId", Type.STRING);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			queryPos.add(companyId);
			queryPos.add(name);
			queryPos.add(scope);
			queryPos.add(primKey);

			Map<String, List<String>> roleMap = new HashMap<>();

			Iterator<Object[]> iterator = sqlQuery.iterate();

			while (iterator.hasNext()) {
				Object[] array = iterator.next();

				String roleName = (String)array[0];
				String actionId = (String)array[1];

				List<String> roleList = roleMap.get(roleName);

				if (roleList == null) {
					roleList = new ArrayList<>();
				}

				roleList.add(actionId);

				roleMap.put(roleName, roleList);
			}

			return roleMap;
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	public List<Role> findByC_N_S_P_A(
		long companyId, String name, int scope, String primKey,
		String actionId) {

		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(FIND_BY_C_N_S_P_A);

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addEntity("Role_", RoleImpl.class);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			queryPos.add(companyId);
			queryPos.add(name);
			queryPos.add(scope);
			queryPos.add(primKey);

			ResourceAction resourceAction =
				ResourceActionLocalServiceUtil.getResourceAction(
					name, actionId);

			queryPos.add(resourceAction.getBitwiseValue());
			queryPos.add(resourceAction.getBitwiseValue());

			return sqlQuery.list(true);
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected int doCountByGroupRoleAndTeamRole(
		long companyId, String keywords, List<String> excludedNames,
		int[] types, long excludedTeamRoleId, long teamGroupId,
		boolean inlineSQLHelper) {

		if ((types == null) || (types.length == 0)) {
			return 0;
		}

		Session session = null;

		try {
			session = openSession();

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(
				_getOrderByStep(
					DSLQueryFactoryUtil.select(
						DSLFunctionFactoryUtil.count(
							RoleTable.INSTANCE.roleId
						).as(
							COUNT_COLUMN_NAME
						)),
					companyId, keywords, excludedNames, types,
					excludedTeamRoleId, teamGroupId, inlineSQLHelper));

			sqlQuery.addScalar(COUNT_COLUMN_NAME, Type.LONG);

			Iterator<Long> iterator = sqlQuery.iterate();

			if (iterator.hasNext()) {
				Long count = iterator.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected int doCountByC_N_D_T(
		long companyId, String[] names, String[] descriptions, Integer[] types,
		LinkedHashMap<String, Object> params, boolean andOperator,
		boolean inlineSQLHelper) {

		names = CustomSQLUtil.keywords(names, true);
		descriptions = CustomSQLUtil.keywords(descriptions, true);

		if (types == null) {
			types = new Integer[0];
		}

		if (params == null) {
			params = new LinkedHashMap<>();
		}

		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(COUNT_BY_C_N_D_T);

			long classNameId = GetterUtil.getLong(
				params.get("classNameId"),
				ClassNameLocalServiceUtil.getClassNameId(Role.class));

			sql = StringUtil.replace(
				sql, "[$CLASS_NAME_ID$]", String.valueOf(classNameId));

			sql = CustomSQLUtil.replaceKeywords(
				sql, "LOWER(Role_.name)", StringPool.LIKE, false, names);
			sql = CustomSQLUtil.replaceKeywords(
				sql, "LOWER(Role_.description)", StringPool.LIKE, true,
				descriptions);
			sql = StringUtil.replace(sql, "[$TYPE$]", getTypes(types.length));
			sql = StringUtil.replace(sql, "[$JOIN$]", getJoin(params));
			sql = StringUtil.replace(sql, "[$WHERE$]", getWhere(params));
			sql = CustomSQLUtil.replaceAndOperator(sql, andOperator);

			if (inlineSQLHelper &&
				InlineSQLHelperUtil.isEnabled(companyId, 0)) {

				sql = InlineSQLHelperUtil.replacePermissionCheck(
					sql, Role.class.getName(), "Role_.roleId", null, null,
					new long[] {0}, null);
			}

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addScalar(COUNT_COLUMN_NAME, Type.LONG);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			setJoin(queryPos, params);

			queryPos.add(companyId);
			queryPos.add(names, 2);
			queryPos.add(descriptions, 2);
			queryPos.add(types);

			Iterator<Long> iterator = sqlQuery.iterate();

			if (iterator.hasNext()) {
				Long count = iterator.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected List<Role> doFindByGroupRoleAndTeamRole(
		long companyId, String keywords, List<String> excludedNames,
		int[] types, long excludedTeamRoleId, long teamGroupId, int start,
		int end, boolean inlineSQLHelper) {

		if ((types == null) || (types.length == 0)) {
			return Collections.emptyList();
		}

		Session session = null;

		try {
			session = openSession();

			OrderByStep orderByStep = _getOrderByStep(
				DSLQueryFactoryUtil.select(RoleTable.INSTANCE), companyId,
				keywords, excludedNames, types, excludedTeamRoleId, teamGroupId,
				inlineSQLHelper);

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(
				orderByStep.orderBy(RoleTable.INSTANCE.name.ascending()));

			sqlQuery.addEntity("Role_", RoleImpl.class);

			return (List<Role>)QueryUtil.list(
				sqlQuery, getDialect(), start, end);
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected List<Role> doFindByC_N_D_T(
		long companyId, String[] names, String[] descriptions, Integer[] types,
		LinkedHashMap<String, Object> params, boolean andOperator, int start,
		int end, OrderByComparator<Role> orderByComparator,
		boolean inlineSQLHelper) {

		names = CustomSQLUtil.keywords(names, true);
		descriptions = CustomSQLUtil.keywords(descriptions, true);

		if (types == null) {
			types = new Integer[0];
		}

		if (params == null) {
			params = new LinkedHashMap<>();
		}

		Session session = null;

		try {
			session = openSession();

			String sql = CustomSQLUtil.get(FIND_BY_C_N_D_T);

			long classNameId = GetterUtil.getLong(
				params.get("classNameId"),
				ClassNameLocalServiceUtil.getClassNameId(Role.class));

			sql = StringUtil.replace(
				sql, "[$CLASS_NAME_ID$]", String.valueOf(classNameId));

			sql = CustomSQLUtil.replaceKeywords(
				sql, "LOWER(Role_.name)", StringPool.LIKE, false, names);
			sql = CustomSQLUtil.replaceKeywords(
				sql, "LOWER(Role_.description)", StringPool.LIKE, true,
				descriptions);
			sql = StringUtil.replace(sql, "[$TYPE$]", getTypes(types.length));
			sql = StringUtil.replace(sql, "[$JOIN$]", getJoin(params));
			sql = StringUtil.replace(sql, "[$WHERE$]", getWhere(params));
			sql = CustomSQLUtil.replaceAndOperator(sql, andOperator);
			sql = CustomSQLUtil.replaceOrderBy(sql, orderByComparator);

			if (inlineSQLHelper &&
				InlineSQLHelperUtil.isEnabled(companyId, 0)) {

				sql = InlineSQLHelperUtil.replacePermissionCheck(
					sql, Role.class.getName(), "Role_.roleId", null, null,
					new long[] {0}, null);
			}

			SQLQuery sqlQuery = session.createSynchronizedSQLQuery(sql);

			sqlQuery.addEntity("Role_", RoleImpl.class);

			QueryPos queryPos = QueryPos.getInstance(sqlQuery);

			setJoin(queryPos, params);

			queryPos.add(companyId);
			queryPos.add(names, 2);
			queryPos.add(descriptions, 2);
			queryPos.add(types);

			return (List<Role>)QueryUtil.list(
				sqlQuery, getDialect(), start, end);
		}
		catch (Exception exception) {
			throw new SystemException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected String getCountByR_U_SQL() {
		if (_countByR_U == null) {
			StringBundler sb = new StringBundler(13);

			sb.append(StringPool.OPEN_PARENTHESIS);
			sb.append(CustomSQLUtil.get(COUNT_BY_ORGANIZATION));
			sb.append(") UNION (");
			sb.append(CustomSQLUtil.get(COUNT_BY_ORGANIZATION_SITE));
			sb.append(") UNION (");
			sb.append(CustomSQLUtil.get(COUNT_BY_SITE));
			sb.append(") UNION (");
			sb.append(CustomSQLUtil.get(COUNT_BY_USER));
			sb.append(") UNION (");
			sb.append(CustomSQLUtil.get(COUNT_BY_USER_GROUP));
			sb.append(") UNION (");
			sb.append(CustomSQLUtil.get(COUNT_BY_USER_GROUP_SITE));
			sb.append(StringPool.CLOSE_PARENTHESIS);

			_countByR_U = sb.toString();
		}

		return _countByR_U;
	}

	protected String getExcludedNames(List<String> excludedNames) {
		if (ListUtil.isEmpty(excludedNames)) {
			return StringPool.BLANK;
		}

		StringBundler sb = new StringBundler(excludedNames.size() + 1);

		sb.append(" AND (");

		for (int i = 0; i < (excludedNames.size() - 1); i++) {
			sb.append("Role_.name != ? AND ");
		}

		sb.append("Role_.name != ?)");

		return sb.toString();
	}

	protected String getGroupIds(long[] groupIds, String table) {
		if (groupIds.length == 0) {
			return StringPool.BLANK;
		}

		StringBundler sb = new StringBundler((groupIds.length * 3) + 1);

		sb.append(StringPool.OPEN_PARENTHESIS);

		for (int i = 0; i < groupIds.length; i++) {
			sb.append(table);
			sb.append(".groupId = ");
			sb.append(groupIds[i]);

			if ((i + 1) < groupIds.length) {
				sb.append(" OR ");
			}
		}

		sb.append(StringPool.CLOSE_PARENTHESIS);

		return sb.toString();
	}

	protected String getJoin(LinkedHashMap<String, Object> params) {
		if ((params == null) || params.isEmpty()) {
			return StringPool.BLANK;
		}

		StringBundler sb = new StringBundler(params.size());

		for (Map.Entry<String, Object> entry : params.entrySet()) {
			if (Validator.isNotNull(entry.getValue())) {
				sb.append(getJoin(entry.getKey()));
			}
		}

		return sb.toString();
	}

	protected String getJoin(String key) {
		String join = StringPool.BLANK;

		if (key.equals("usersRoles")) {
			join = CustomSQLUtil.get(JOIN_BY_USERS_ROLES);
		}

		if (Validator.isNotNull(join)) {
			int pos = join.indexOf("WHERE");

			if (pos != -1) {
				join = join.substring(0, pos);
			}
		}

		return join;
	}

	protected String getTypes(int size) {
		if (size == 0) {
			return StringPool.BLANK;
		}

		StringBundler sb = new StringBundler(size + 1);

		sb.append(" AND (");

		for (int i = 0; i < (size - 1); i++) {
			sb.append("Role_.type_ = ? OR ");
		}

		sb.append("Role_.type_ = ?)");

		return sb.toString();
	}

	protected String getWhere(LinkedHashMap<String, Object> params) {
		if ((params == null) || params.isEmpty()) {
			return StringPool.BLANK;
		}

		StringBundler sb = new StringBundler(params.size());

		for (Map.Entry<String, Object> entry : params.entrySet()) {
			if (Validator.isNotNull(entry.getValue())) {
				sb.append(getWhere(entry.getKey()));
			}
		}

		return sb.toString();
	}

	protected String getWhere(String key) {
		String join = StringPool.BLANK;

		if (key.equals("usersRoles")) {
			join = CustomSQLUtil.get(JOIN_BY_USERS_ROLES);
		}

		if (Validator.isNotNull(join)) {
			int pos = join.indexOf("WHERE");

			if (pos != -1) {
				join = join.substring(pos + 5);

				join = join.concat(" AND ");
			}
			else {
				join = StringPool.BLANK;
			}
		}

		return join;
	}

	protected void setJoin(
		QueryPos queryPos, LinkedHashMap<String, Object> params) {

		if (params == null) {
			return;
		}

		for (Map.Entry<String, Object> entry : params.entrySet()) {
			if (Objects.equals(entry.getKey(), "classNameId")) {
				continue;
			}

			Object value = entry.getValue();

			if (value instanceof Long) {
				Long valueLong = (Long)value;

				if (Validator.isNotNull(valueLong)) {
					queryPos.add(valueLong);
				}
			}
			else if (value instanceof String) {
				String valueString = (String)value;

				if (Validator.isNotNull(valueString)) {
					queryPos.add(valueString);
				}
			}
		}
	}

	private Predicate _getKeywordsPredicate(
		Column<?, String> column, String[] keywords) {

		Expression<String> expression = DSLFunctionFactoryUtil.lower(column);

		Predicate keywordsPredicate = null;

		for (String keyword : keywords) {
			if (keyword == null) {
				continue;
			}

			Predicate keywordPredicate = expression.like(keyword);

			if (keywordsPredicate == null) {
				keywordsPredicate = keywordPredicate;
			}
			else {
				keywordsPredicate = keywordsPredicate.or(keywordPredicate);
			}
		}

		return keywordsPredicate;
	}

	private OrderByStep _getOrderByStep(
		FromStep fromStep, long companyId, String keywords,
		List<String> excludedNames, int[] types, long excludedTeamRoleId,
		long teamGroupId, boolean inlineSQLHelper) {

		Predicate wherePredicate = RoleTable.INSTANCE.companyId.eq(companyId);

		if (inlineSQLHelper && InlineSQLHelperUtil.isEnabled()) {
			wherePredicate = wherePredicate.and(
				InlineSQLHelperUtil.getPermissionWherePredicate(
					Role.class, RoleTable.INSTANCE.roleId, new long[] {0})
			).withParentheses();
		}

		Predicate rolesWherePredicate = null;
		Predicate teamsSubqueryPredicate = TeamTable.INSTANCE.groupId.eq(
			teamGroupId);

		if (Validator.isNotNull(keywords)) {
			String[] keywordsArray = CustomSQLUtil.keywords(keywords, true);

			rolesWherePredicate = _getKeywordsPredicate(
				RoleTable.INSTANCE.name, keywordsArray
			).or(
				_getKeywordsPredicate(RoleTable.INSTANCE.title, keywordsArray)
			).or(
				_getKeywordsPredicate(
					RoleTable.INSTANCE.description, keywordsArray)
			).withParentheses();

			teamsSubqueryPredicate = teamsSubqueryPredicate.and(
				_getKeywordsPredicate(
					TeamTable.INSTANCE.name, keywordsArray
				).or(
					_getKeywordsPredicate(
						TeamTable.INSTANCE.description, keywordsArray)
				).withParentheses());
		}

		if (ListUtil.isNotEmpty(excludedNames)) {
			Predicate excludedNamesWherePredicate = RoleTable.INSTANCE.name.neq(
				excludedNames.get(0));

			for (int i = 1; i < excludedNames.size(); i++) {
				excludedNamesWherePredicate = excludedNamesWherePredicate.and(
					RoleTable.INSTANCE.name.neq(excludedNames.get(i)));
			}

			if (rolesWherePredicate == null) {
				rolesWherePredicate =
					excludedNamesWherePredicate.withParentheses();
			}
			else {
				rolesWherePredicate = rolesWherePredicate.and(
					excludedNamesWherePredicate.withParentheses());
			}
		}

		Predicate typesWherePredicate = RoleTable.INSTANCE.type.eq(types[0]);

		for (int i = 1; i < types.length; i++) {
			typesWherePredicate = typesWherePredicate.or(
				RoleTable.INSTANCE.type.eq(types[i]));
		}

		if (rolesWherePredicate == null) {
			rolesWherePredicate = typesWherePredicate.withParentheses();
		}
		else {
			rolesWherePredicate = rolesWherePredicate.and(
				typesWherePredicate.withParentheses());
		}

		if (rolesWherePredicate != null) {
			wherePredicate = wherePredicate.and(
				rolesWherePredicate.withParentheses());
		}

		DSLQuery teamsSubquery = DSLQueryFactoryUtil.select(
			TeamTable.INSTANCE.teamId
		).from(
			TeamTable.INSTANCE
		).where(
			teamsSubqueryPredicate
		);

		Predicate teamsWherePredicate = RoleTable.INSTANCE.classNameId.eq(
			ClassNameLocalServiceUtil.getClassNameId(Team.class)
		).and(
			RoleTable.INSTANCE.roleId.neq(excludedTeamRoleId)
		).and(
			RoleTable.INSTANCE.classPK.in(teamsSubquery)
		);

		return fromStep.from(
			RoleTable.INSTANCE
		).where(
			wherePredicate.or(teamsWherePredicate.withParentheses())
		);
	}

	private String _countByR_U;

}