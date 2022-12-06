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

package com.liferay.app.builder.rest.internal.graphql.servlet.v1_0;

import com.liferay.app.builder.rest.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.app.builder.rest.internal.graphql.query.v1_0.Query;
import com.liferay.app.builder.rest.internal.resource.v1_0.AppResourceImpl;
import com.liferay.app.builder.rest.resource.v1_0.AppResource;
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
 * @author Gabriel Albuquerque
 * @generated
 */
@Component(service = ServletData.class)
@Generated("")
public class ServletDataImpl implements ServletData {

	@Activate
	public void activate(BundleContext bundleContext) {
		Mutation.setAppResourceComponentServiceObjects(
			_appResourceComponentServiceObjects);

		Query.setAppResourceComponentServiceObjects(
			_appResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.App.Builder.REST";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/app-builder-graphql/v1_0";
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
						"mutation#deleteApp",
						new ObjectValuePair<>(
							AppResourceImpl.class, "deleteApp"));
					put(
						"mutation#deleteAppBatch",
						new ObjectValuePair<>(
							AppResourceImpl.class, "deleteAppBatch"));
					put(
						"mutation#updateApp",
						new ObjectValuePair<>(AppResourceImpl.class, "putApp"));
					put(
						"mutation#updateAppBatch",
						new ObjectValuePair<>(
							AppResourceImpl.class, "putAppBatch"));
					put(
						"mutation#updateAppDeploy",
						new ObjectValuePair<>(
							AppResourceImpl.class, "putAppDeploy"));
					put(
						"mutation#updateAppUndeploy",
						new ObjectValuePair<>(
							AppResourceImpl.class, "putAppUndeploy"));
					put(
						"mutation#createDataDefinitionApp",
						new ObjectValuePair<>(
							AppResourceImpl.class, "postDataDefinitionApp"));

					put(
						"query#apps",
						new ObjectValuePair<>(
							AppResourceImpl.class, "getAppsPage"));
					put(
						"query#app",
						new ObjectValuePair<>(AppResourceImpl.class, "getApp"));
					put(
						"query#dataDefinitionApps",
						new ObjectValuePair<>(
							AppResourceImpl.class,
							"getDataDefinitionAppsPage"));
					put(
						"query#siteApps",
						new ObjectValuePair<>(
							AppResourceImpl.class, "getSiteAppsPage"));
				}
			};

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AppResource>
		_appResourceComponentServiceObjects;

}