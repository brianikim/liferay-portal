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

package com.liferay.account.rest.internal.graphql.servlet.v1_0;

import com.liferay.account.rest.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.account.rest.internal.graphql.query.v1_0.Query;
import com.liferay.account.rest.internal.resource.v1_0.AccountResourceImpl;
import com.liferay.account.rest.internal.resource.v1_0.AccountRoleResourceImpl;
import com.liferay.account.rest.internal.resource.v1_0.AccountUserResourceImpl;
import com.liferay.account.rest.resource.v1_0.AccountResource;
import com.liferay.account.rest.resource.v1_0.AccountRoleResource;
import com.liferay.account.rest.resource.v1_0.AccountUserResource;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.vulcan.graphql.servlet.ServletData;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

/**
 * @author Drew Brokke
 * @generated
 */
@Component(service = ServletData.class)
@Generated("")
public class ServletDataImpl implements ServletData {

	@Activate
	public void activate(BundleContext bundleContext) {
		Mutation.setAccountResourceComponentServiceObjects(
			_accountResourceComponentServiceObjects);
		Mutation.setAccountRoleResourceComponentServiceObjects(
			_accountRoleResourceComponentServiceObjects);
		Mutation.setAccountUserResourceComponentServiceObjects(
			_accountUserResourceComponentServiceObjects);

		Query.setAccountResourceComponentServiceObjects(
			_accountResourceComponentServiceObjects);
		Query.setAccountRoleResourceComponentServiceObjects(
			_accountRoleResourceComponentServiceObjects);
		Query.setAccountUserResourceComponentServiceObjects(
			_accountUserResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.Account.REST";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/account-rest-graphql/v1_0";
	}

	@Override
	public Query getQuery() {
		return new Query();
	}

	public ObjectValuePair<Class<?>, String> getResourceMethodObjectValuePair(
		String methodName, boolean mutation) {

		if (mutation) {
			return _resourceMethodObjectValuePairs.get(
				"mutation#" + methodName);
		}

		return _resourceMethodObjectValuePairs.get("query#" + methodName);
	}

	private static final Map<String, ObjectValuePair<Class<?>, String>>
		_resourceMethodObjectValuePairs =
			new HashMap<String, ObjectValuePair<Class<?>, String>>() {
				{
					put(
						"mutation#createAccount",
						new ObjectValuePair<>(
							AccountResourceImpl.class, "postAccount"));
					put(
						"mutation#createAccountBatch",
						new ObjectValuePair<>(
							AccountResourceImpl.class, "postAccountBatch"));
					put(
						"mutation#deleteAccountByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountResourceImpl.class,
							"deleteAccountByExternalReferenceCode"));
					put(
						"mutation#patchAccountByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountResourceImpl.class,
							"patchAccountByExternalReferenceCode"));
					put(
						"mutation#updateAccountByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountResourceImpl.class,
							"putAccountByExternalReferenceCode"));
					put(
						"mutation#deleteAccount",
						new ObjectValuePair<>(
							AccountResourceImpl.class, "deleteAccount"));
					put(
						"mutation#deleteAccountBatch",
						new ObjectValuePair<>(
							AccountResourceImpl.class, "deleteAccountBatch"));
					put(
						"mutation#patchAccount",
						new ObjectValuePair<>(
							AccountResourceImpl.class, "patchAccount"));
					put(
						"mutation#updateAccount",
						new ObjectValuePair<>(
							AccountResourceImpl.class, "putAccount"));
					put(
						"mutation#updateAccountBatch",
						new ObjectValuePair<>(
							AccountResourceImpl.class, "putAccountBatch"));
					put(
						"mutation#deleteAccountRoleUserAssociationByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountRoleResourceImpl.class,
							"deleteAccountRoleUserAssociationByExternalReferenceCode"));
					put(
						"mutation#createAccountRoleUserAssociationByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountRoleResourceImpl.class,
							"postAccountRoleUserAssociationByExternalReferenceCode"));
					put(
						"mutation#createAccountRoleByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountRoleResourceImpl.class,
							"postAccountRoleByExternalReferenceCode"));
					put(
						"mutation#createAccountRole",
						new ObjectValuePair<>(
							AccountRoleResourceImpl.class, "postAccountRole"));
					put(
						"mutation#deleteAccountRoleUserAssociation",
						new ObjectValuePair<>(
							AccountRoleResourceImpl.class,
							"deleteAccountRoleUserAssociation"));
					put(
						"mutation#createAccountRoleUserAssociation",
						new ObjectValuePair<>(
							AccountRoleResourceImpl.class,
							"postAccountRoleUserAssociation"));
					put(
						"mutation#createAccountUserByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountUserResourceImpl.class,
							"postAccountUserByExternalReferenceCode"));
					put(
						"mutation#createAccountUser",
						new ObjectValuePair<>(
							AccountUserResourceImpl.class, "postAccountUser"));

					put(
						"query#accounts",
						new ObjectValuePair<>(
							AccountResourceImpl.class, "getAccountsPage"));
					put(
						"query#accountByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountResourceImpl.class,
							"getAccountByExternalReferenceCode"));
					put(
						"query#account",
						new ObjectValuePair<>(
							AccountResourceImpl.class, "getAccount"));
					put(
						"query#accountRolesByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountRoleResourceImpl.class,
							"getAccountRolesByExternalReferenceCodePage"));
					put(
						"query#accountRoles",
						new ObjectValuePair<>(
							AccountRoleResourceImpl.class,
							"getAccountRolesPage"));
					put(
						"query#accountUsersByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountUserResourceImpl.class,
							"getAccountUsersByExternalReferenceCodePage"));
					put(
						"query#accountUsers",
						new ObjectValuePair<>(
							AccountUserResourceImpl.class,
							"getAccountUsersPage"));
				}
			};

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountResource>
		_accountResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountRoleResource>
		_accountRoleResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountUserResource>
		_accountUserResourceComponentServiceObjects;

}