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

package com.liferay.headless.commerce.admin.account.internal.graphql.servlet.v1_0;

import com.liferay.headless.commerce.admin.account.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.headless.commerce.admin.account.internal.graphql.query.v1_0.Query;
import com.liferay.headless.commerce.admin.account.internal.resource.v1_0.AccountAddressResourceImpl;
import com.liferay.headless.commerce.admin.account.internal.resource.v1_0.AccountGroupResourceImpl;
import com.liferay.headless.commerce.admin.account.internal.resource.v1_0.AccountMemberResourceImpl;
import com.liferay.headless.commerce.admin.account.internal.resource.v1_0.AccountOrganizationResourceImpl;
import com.liferay.headless.commerce.admin.account.internal.resource.v1_0.AccountResourceImpl;
import com.liferay.headless.commerce.admin.account.internal.resource.v1_0.UserResourceImpl;
import com.liferay.headless.commerce.admin.account.resource.v1_0.AccountAddressResource;
import com.liferay.headless.commerce.admin.account.resource.v1_0.AccountGroupResource;
import com.liferay.headless.commerce.admin.account.resource.v1_0.AccountMemberResource;
import com.liferay.headless.commerce.admin.account.resource.v1_0.AccountOrganizationResource;
import com.liferay.headless.commerce.admin.account.resource.v1_0.AccountResource;
import com.liferay.headless.commerce.admin.account.resource.v1_0.UserResource;
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
 * @author Alessio Antonio Rendina
 * @generated
 */
@Component(service = ServletData.class)
@Generated("")
public class ServletDataImpl implements ServletData {

	@Activate
	public void activate(BundleContext bundleContext) {
		Mutation.setAccountResourceComponentServiceObjects(
			_accountResourceComponentServiceObjects);
		Mutation.setAccountAddressResourceComponentServiceObjects(
			_accountAddressResourceComponentServiceObjects);
		Mutation.setAccountGroupResourceComponentServiceObjects(
			_accountGroupResourceComponentServiceObjects);
		Mutation.setAccountMemberResourceComponentServiceObjects(
			_accountMemberResourceComponentServiceObjects);
		Mutation.setAccountOrganizationResourceComponentServiceObjects(
			_accountOrganizationResourceComponentServiceObjects);
		Mutation.setUserResourceComponentServiceObjects(
			_userResourceComponentServiceObjects);

		Query.setAccountResourceComponentServiceObjects(
			_accountResourceComponentServiceObjects);
		Query.setAccountAddressResourceComponentServiceObjects(
			_accountAddressResourceComponentServiceObjects);
		Query.setAccountGroupResourceComponentServiceObjects(
			_accountGroupResourceComponentServiceObjects);
		Query.setAccountMemberResourceComponentServiceObjects(
			_accountMemberResourceComponentServiceObjects);
		Query.setAccountOrganizationResourceComponentServiceObjects(
			_accountOrganizationResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.Headless.Commerce.Admin.Account";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/headless-commerce-admin-account-graphql/v1_0";
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
						"mutation#createAccountGroupByExternalReferenceCodeAccount",
						new ObjectValuePair<>(
							AccountResourceImpl.class,
							"postAccountGroupByExternalReferenceCodeAccount"));
					put(
						"mutation#deleteAccountGroupByExternalReferenceCodeAccount",
						new ObjectValuePair<>(
							AccountResourceImpl.class,
							"deleteAccountGroupByExternalReferenceCodeAccount"));
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
						"mutation#createAccountByExternalReferenceCodeLogo",
						new ObjectValuePair<>(
							AccountResourceImpl.class,
							"postAccountByExternalReferenceCodeLogo"));
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
						"mutation#createAccountLogo",
						new ObjectValuePair<>(
							AccountResourceImpl.class, "postAccountLogo"));
					put(
						"mutation#deleteAccountAddressByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"deleteAccountAddressByExternalReferenceCode"));
					put(
						"mutation#patchAccountAddressByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"patchAccountAddressByExternalReferenceCode"));
					put(
						"mutation#deleteAccountAddress",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"deleteAccountAddress"));
					put(
						"mutation#deleteAccountAddressBatch",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"deleteAccountAddressBatch"));
					put(
						"mutation#patchAccountAddress",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"patchAccountAddress"));
					put(
						"mutation#updateAccountAddress",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"putAccountAddress"));
					put(
						"mutation#updateAccountAddressBatch",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"putAccountAddressBatch"));
					put(
						"mutation#createAccountByExternalReferenceCodeAccountAddress",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"postAccountByExternalReferenceCodeAccountAddress"));
					put(
						"mutation#createAccountIdAccountAddress",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"postAccountIdAccountAddress"));
					put(
						"mutation#createAccountIdAccountAddressBatch",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"postAccountIdAccountAddressBatch"));
					put(
						"mutation#createAccountGroup",
						new ObjectValuePair<>(
							AccountGroupResourceImpl.class,
							"postAccountGroup"));
					put(
						"mutation#createAccountGroupBatch",
						new ObjectValuePair<>(
							AccountGroupResourceImpl.class,
							"postAccountGroupBatch"));
					put(
						"mutation#deleteAccountGroupByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountGroupResourceImpl.class,
							"deleteAccountGroupByExternalReferenceCode"));
					put(
						"mutation#patchAccountGroupByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountGroupResourceImpl.class,
							"patchAccountGroupByExternalReferenceCode"));
					put(
						"mutation#deleteAccountGroup",
						new ObjectValuePair<>(
							AccountGroupResourceImpl.class,
							"deleteAccountGroup"));
					put(
						"mutation#deleteAccountGroupBatch",
						new ObjectValuePair<>(
							AccountGroupResourceImpl.class,
							"deleteAccountGroupBatch"));
					put(
						"mutation#patchAccountGroup",
						new ObjectValuePair<>(
							AccountGroupResourceImpl.class,
							"patchAccountGroup"));
					put(
						"mutation#createAccountByExternalReferenceCodeAccountMember",
						new ObjectValuePair<>(
							AccountMemberResourceImpl.class,
							"postAccountByExternalReferenceCodeAccountMember"));
					put(
						"mutation#deleteAccountByExternalReferenceCodeAccountMember",
						new ObjectValuePair<>(
							AccountMemberResourceImpl.class,
							"deleteAccountByExternalReferenceCodeAccountMember"));
					put(
						"mutation#patchAccountByExternalReferenceCodeAccountMember",
						new ObjectValuePair<>(
							AccountMemberResourceImpl.class,
							"patchAccountByExternalReferenceCodeAccountMember"));
					put(
						"mutation#createAccountIdAccountMember",
						new ObjectValuePair<>(
							AccountMemberResourceImpl.class,
							"postAccountIdAccountMember"));
					put(
						"mutation#createAccountIdAccountMemberBatch",
						new ObjectValuePair<>(
							AccountMemberResourceImpl.class,
							"postAccountIdAccountMemberBatch"));
					put(
						"mutation#deleteAccountIdAccountMember",
						new ObjectValuePair<>(
							AccountMemberResourceImpl.class,
							"deleteAccountIdAccountMember"));
					put(
						"mutation#patchAccountIdAccountMember",
						new ObjectValuePair<>(
							AccountMemberResourceImpl.class,
							"patchAccountIdAccountMember"));
					put(
						"mutation#createAccountByExternalReferenceCodeAccountOrganization",
						new ObjectValuePair<>(
							AccountOrganizationResourceImpl.class,
							"postAccountByExternalReferenceCodeAccountOrganization"));
					put(
						"mutation#deleteAccountByExternalReferenceCodeAccountOrganization",
						new ObjectValuePair<>(
							AccountOrganizationResourceImpl.class,
							"deleteAccountByExternalReferenceCodeAccountOrganization"));
					put(
						"mutation#createAccountIdAccountOrganization",
						new ObjectValuePair<>(
							AccountOrganizationResourceImpl.class,
							"postAccountIdAccountOrganization"));
					put(
						"mutation#createAccountIdAccountOrganizationBatch",
						new ObjectValuePair<>(
							AccountOrganizationResourceImpl.class,
							"postAccountIdAccountOrganizationBatch"));
					put(
						"mutation#deleteAccountIdAccountOrganization",
						new ObjectValuePair<>(
							AccountOrganizationResourceImpl.class,
							"deleteAccountIdAccountOrganization"));
					put(
						"mutation#createAccountByExternalReferenceCodeAccountMemberCreateUser",
						new ObjectValuePair<>(
							UserResourceImpl.class,
							"postAccountByExternalReferenceCodeAccountMemberCreateUser"));

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
						"query#accountAddressByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"getAccountAddressByExternalReferenceCode"));
					put(
						"query#accountAddress",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"getAccountAddress"));
					put(
						"query#accountByExternalReferenceCodeAccountAddresses",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"getAccountByExternalReferenceCodeAccountAddressesPage"));
					put(
						"query#accountIdAccountAddresses",
						new ObjectValuePair<>(
							AccountAddressResourceImpl.class,
							"getAccountIdAccountAddressesPage"));
					put(
						"query#accountGroups",
						new ObjectValuePair<>(
							AccountGroupResourceImpl.class,
							"getAccountGroupsPage"));
					put(
						"query#accountGroupByExternalReferenceCode",
						new ObjectValuePair<>(
							AccountGroupResourceImpl.class,
							"getAccountGroupByExternalReferenceCode"));
					put(
						"query#accountGroup",
						new ObjectValuePair<>(
							AccountGroupResourceImpl.class, "getAccountGroup"));
					put(
						"query#accountByExternalReferenceCodeAccountMembers",
						new ObjectValuePair<>(
							AccountMemberResourceImpl.class,
							"getAccountByExternalReferenceCodeAccountMembersPage"));
					put(
						"query#accountByExternalReferenceCodeAccountMember",
						new ObjectValuePair<>(
							AccountMemberResourceImpl.class,
							"getAccountByExternalReferenceCodeAccountMember"));
					put(
						"query#accountIdAccountMembers",
						new ObjectValuePair<>(
							AccountMemberResourceImpl.class,
							"getAccountIdAccountMembersPage"));
					put(
						"query#accountIdAccountMember",
						new ObjectValuePair<>(
							AccountMemberResourceImpl.class,
							"getAccountIdAccountMember"));
					put(
						"query#accountByExternalReferenceCodeAccountOrganizations",
						new ObjectValuePair<>(
							AccountOrganizationResourceImpl.class,
							"getAccountByExternalReferenceCodeAccountOrganizationsPage"));
					put(
						"query#accountByExternalReferenceCodeAccountOrganization",
						new ObjectValuePair<>(
							AccountOrganizationResourceImpl.class,
							"getAccountByExternalReferenceCodeAccountOrganization"));
					put(
						"query#accountIdAccountOrganizations",
						new ObjectValuePair<>(
							AccountOrganizationResourceImpl.class,
							"getAccountIdAccountOrganizationsPage"));
					put(
						"query#accountIdAccountOrganization",
						new ObjectValuePair<>(
							AccountOrganizationResourceImpl.class,
							"getAccountIdAccountOrganization"));
				}
			};

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountResource>
		_accountResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountAddressResource>
		_accountAddressResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountGroupResource>
		_accountGroupResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountMemberResource>
		_accountMemberResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountOrganizationResource>
		_accountOrganizationResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<UserResource>
		_userResourceComponentServiceObjects;

}