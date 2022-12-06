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

package com.liferay.headless.admin.user.internal.graphql.servlet.v1_0;

import com.liferay.headless.admin.user.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.headless.admin.user.internal.graphql.query.v1_0.Query;
import com.liferay.headless.admin.user.internal.resource.v1_0.EmailAddressResourceImpl;
import com.liferay.headless.admin.user.internal.resource.v1_0.OrganizationResourceImpl;
import com.liferay.headless.admin.user.internal.resource.v1_0.PhoneResourceImpl;
import com.liferay.headless.admin.user.internal.resource.v1_0.PostalAddressResourceImpl;
import com.liferay.headless.admin.user.internal.resource.v1_0.RoleResourceImpl;
import com.liferay.headless.admin.user.internal.resource.v1_0.SegmentResourceImpl;
import com.liferay.headless.admin.user.internal.resource.v1_0.SegmentUserResourceImpl;
import com.liferay.headless.admin.user.internal.resource.v1_0.SiteResourceImpl;
import com.liferay.headless.admin.user.internal.resource.v1_0.SubscriptionResourceImpl;
import com.liferay.headless.admin.user.internal.resource.v1_0.UserAccountResourceImpl;
import com.liferay.headless.admin.user.internal.resource.v1_0.WebUrlResourceImpl;
import com.liferay.headless.admin.user.resource.v1_0.EmailAddressResource;
import com.liferay.headless.admin.user.resource.v1_0.OrganizationResource;
import com.liferay.headless.admin.user.resource.v1_0.PhoneResource;
import com.liferay.headless.admin.user.resource.v1_0.PostalAddressResource;
import com.liferay.headless.admin.user.resource.v1_0.RoleResource;
import com.liferay.headless.admin.user.resource.v1_0.SegmentResource;
import com.liferay.headless.admin.user.resource.v1_0.SegmentUserResource;
import com.liferay.headless.admin.user.resource.v1_0.SiteResource;
import com.liferay.headless.admin.user.resource.v1_0.SubscriptionResource;
import com.liferay.headless.admin.user.resource.v1_0.UserAccountResource;
import com.liferay.headless.admin.user.resource.v1_0.WebUrlResource;
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
 * @author Javier Gamarra
 * @generated
 */
@Component(service = ServletData.class)
@Generated("")
public class ServletDataImpl implements ServletData {

	@Activate
	public void activate(BundleContext bundleContext) {
		Mutation.setOrganizationResourceComponentServiceObjects(
			_organizationResourceComponentServiceObjects);
		Mutation.setRoleResourceComponentServiceObjects(
			_roleResourceComponentServiceObjects);
		Mutation.setSubscriptionResourceComponentServiceObjects(
			_subscriptionResourceComponentServiceObjects);
		Mutation.setUserAccountResourceComponentServiceObjects(
			_userAccountResourceComponentServiceObjects);

		Query.setEmailAddressResourceComponentServiceObjects(
			_emailAddressResourceComponentServiceObjects);
		Query.setOrganizationResourceComponentServiceObjects(
			_organizationResourceComponentServiceObjects);
		Query.setPhoneResourceComponentServiceObjects(
			_phoneResourceComponentServiceObjects);
		Query.setPostalAddressResourceComponentServiceObjects(
			_postalAddressResourceComponentServiceObjects);
		Query.setRoleResourceComponentServiceObjects(
			_roleResourceComponentServiceObjects);
		Query.setSegmentResourceComponentServiceObjects(
			_segmentResourceComponentServiceObjects);
		Query.setSegmentUserResourceComponentServiceObjects(
			_segmentUserResourceComponentServiceObjects);
		Query.setSiteResourceComponentServiceObjects(
			_siteResourceComponentServiceObjects);
		Query.setSubscriptionResourceComponentServiceObjects(
			_subscriptionResourceComponentServiceObjects);
		Query.setUserAccountResourceComponentServiceObjects(
			_userAccountResourceComponentServiceObjects);
		Query.setWebUrlResourceComponentServiceObjects(
			_webUrlResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.Headless.Admin.User";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/headless-admin-user-graphql/v1_0";
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
						"mutation#createOrganization",
						new ObjectValuePair<>(
							OrganizationResourceImpl.class,
							"postOrganization"));
					put(
						"mutation#createOrganizationBatch",
						new ObjectValuePair<>(
							OrganizationResourceImpl.class,
							"postOrganizationBatch"));
					put(
						"mutation#deleteOrganization",
						new ObjectValuePair<>(
							OrganizationResourceImpl.class,
							"deleteOrganization"));
					put(
						"mutation#deleteOrganizationBatch",
						new ObjectValuePair<>(
							OrganizationResourceImpl.class,
							"deleteOrganizationBatch"));
					put(
						"mutation#patchOrganization",
						new ObjectValuePair<>(
							OrganizationResourceImpl.class,
							"patchOrganization"));
					put(
						"mutation#updateOrganization",
						new ObjectValuePair<>(
							OrganizationResourceImpl.class, "putOrganization"));
					put(
						"mutation#updateOrganizationBatch",
						new ObjectValuePair<>(
							OrganizationResourceImpl.class,
							"putOrganizationBatch"));
					put(
						"mutation#deleteRoleUserAccountAssociation",
						new ObjectValuePair<>(
							RoleResourceImpl.class,
							"deleteRoleUserAccountAssociation"));
					put(
						"mutation#createRoleUserAccountAssociation",
						new ObjectValuePair<>(
							RoleResourceImpl.class,
							"postRoleUserAccountAssociation"));
					put(
						"mutation#deleteOrganizationRoleUserAccountAssociation",
						new ObjectValuePair<>(
							RoleResourceImpl.class,
							"deleteOrganizationRoleUserAccountAssociation"));
					put(
						"mutation#createOrganizationRoleUserAccountAssociation",
						new ObjectValuePair<>(
							RoleResourceImpl.class,
							"postOrganizationRoleUserAccountAssociation"));
					put(
						"mutation#deleteSiteRoleUserAccountAssociation",
						new ObjectValuePair<>(
							RoleResourceImpl.class,
							"deleteSiteRoleUserAccountAssociation"));
					put(
						"mutation#createSiteRoleUserAccountAssociation",
						new ObjectValuePair<>(
							RoleResourceImpl.class,
							"postSiteRoleUserAccountAssociation"));
					put(
						"mutation#deleteMyUserAccountSubscription",
						new ObjectValuePair<>(
							SubscriptionResourceImpl.class,
							"deleteMyUserAccountSubscription"));
					put(
						"mutation#createUserAccount",
						new ObjectValuePair<>(
							UserAccountResourceImpl.class, "postUserAccount"));
					put(
						"mutation#createUserAccountBatch",
						new ObjectValuePair<>(
							UserAccountResourceImpl.class,
							"postUserAccountBatch"));
					put(
						"mutation#deleteUserAccount",
						new ObjectValuePair<>(
							UserAccountResourceImpl.class,
							"deleteUserAccount"));
					put(
						"mutation#deleteUserAccountBatch",
						new ObjectValuePair<>(
							UserAccountResourceImpl.class,
							"deleteUserAccountBatch"));
					put(
						"mutation#patchUserAccount",
						new ObjectValuePair<>(
							UserAccountResourceImpl.class, "patchUserAccount"));
					put(
						"mutation#updateUserAccount",
						new ObjectValuePair<>(
							UserAccountResourceImpl.class, "putUserAccount"));
					put(
						"mutation#updateUserAccountBatch",
						new ObjectValuePair<>(
							UserAccountResourceImpl.class,
							"putUserAccountBatch"));

					put(
						"query#emailAddress",
						new ObjectValuePair<>(
							EmailAddressResourceImpl.class, "getEmailAddress"));
					put(
						"query#organizationEmailAddresses",
						new ObjectValuePair<>(
							EmailAddressResourceImpl.class,
							"getOrganizationEmailAddressesPage"));
					put(
						"query#userAccountEmailAddresses",
						new ObjectValuePair<>(
							EmailAddressResourceImpl.class,
							"getUserAccountEmailAddressesPage"));
					put(
						"query#organizations",
						new ObjectValuePair<>(
							OrganizationResourceImpl.class,
							"getOrganizationsPage"));
					put(
						"query#organization",
						new ObjectValuePair<>(
							OrganizationResourceImpl.class, "getOrganization"));
					put(
						"query#organizationOrganizations",
						new ObjectValuePair<>(
							OrganizationResourceImpl.class,
							"getOrganizationOrganizationsPage"));
					put(
						"query#organizationPhones",
						new ObjectValuePair<>(
							PhoneResourceImpl.class,
							"getOrganizationPhonesPage"));
					put(
						"query#phone",
						new ObjectValuePair<>(
							PhoneResourceImpl.class, "getPhone"));
					put(
						"query#userAccountPhones",
						new ObjectValuePair<>(
							PhoneResourceImpl.class,
							"getUserAccountPhonesPage"));
					put(
						"query#organizationPostalAddresses",
						new ObjectValuePair<>(
							PostalAddressResourceImpl.class,
							"getOrganizationPostalAddressesPage"));
					put(
						"query#postalAddress",
						new ObjectValuePair<>(
							PostalAddressResourceImpl.class,
							"getPostalAddress"));
					put(
						"query#userAccountPostalAddresses",
						new ObjectValuePair<>(
							PostalAddressResourceImpl.class,
							"getUserAccountPostalAddressesPage"));
					put(
						"query#roles",
						new ObjectValuePair<>(
							RoleResourceImpl.class, "getRolesPage"));
					put(
						"query#role",
						new ObjectValuePair<>(
							RoleResourceImpl.class, "getRole"));
					put(
						"query#segments",
						new ObjectValuePair<>(
							SegmentResourceImpl.class, "getSiteSegmentsPage"));
					put(
						"query#userAccountSegments",
						new ObjectValuePair<>(
							SegmentResourceImpl.class,
							"getSiteUserAccountSegmentsPage"));
					put(
						"query#segmentUserAccounts",
						new ObjectValuePair<>(
							SegmentUserResourceImpl.class,
							"getSegmentUserAccountsPage"));
					put(
						"query#myUserAccountSites",
						new ObjectValuePair<>(
							SiteResourceImpl.class,
							"getMyUserAccountSitesPage"));
					put(
						"query#byFriendlyUrlPath",
						new ObjectValuePair<>(
							SiteResourceImpl.class,
							"getSiteByFriendlyUrlPath"));
					put(
						"query#site",
						new ObjectValuePair<>(
							SiteResourceImpl.class, "getSite"));
					put(
						"query#myUserAccountSubscriptions",
						new ObjectValuePair<>(
							SubscriptionResourceImpl.class,
							"getMyUserAccountSubscriptionsPage"));
					put(
						"query#myUserAccountSubscription",
						new ObjectValuePair<>(
							SubscriptionResourceImpl.class,
							"getMyUserAccountSubscription"));
					put(
						"query#myUserAccount",
						new ObjectValuePair<>(
							UserAccountResourceImpl.class, "getMyUserAccount"));
					put(
						"query#organizationUserAccounts",
						new ObjectValuePair<>(
							UserAccountResourceImpl.class,
							"getOrganizationUserAccountsPage"));
					put(
						"query#siteUserAccounts",
						new ObjectValuePair<>(
							UserAccountResourceImpl.class,
							"getSiteUserAccountsPage"));
					put(
						"query#userAccounts",
						new ObjectValuePair<>(
							UserAccountResourceImpl.class,
							"getUserAccountsPage"));
					put(
						"query#userAccount",
						new ObjectValuePair<>(
							UserAccountResourceImpl.class, "getUserAccount"));
					put(
						"query#organizationWebUrls",
						new ObjectValuePair<>(
							WebUrlResourceImpl.class,
							"getOrganizationWebUrlsPage"));
					put(
						"query#userAccountWebUrls",
						new ObjectValuePair<>(
							WebUrlResourceImpl.class,
							"getUserAccountWebUrlsPage"));
					put(
						"query#webUrl",
						new ObjectValuePair<>(
							WebUrlResourceImpl.class, "getWebUrl"));
				}
			};

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<OrganizationResource>
		_organizationResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<RoleResource>
		_roleResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<SubscriptionResource>
		_subscriptionResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<UserAccountResource>
		_userAccountResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<EmailAddressResource>
		_emailAddressResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<PhoneResource>
		_phoneResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<PostalAddressResource>
		_postalAddressResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<SegmentResource>
		_segmentResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<SegmentUserResource>
		_segmentUserResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<SiteResource>
		_siteResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<WebUrlResource>
		_webUrlResourceComponentServiceObjects;

}