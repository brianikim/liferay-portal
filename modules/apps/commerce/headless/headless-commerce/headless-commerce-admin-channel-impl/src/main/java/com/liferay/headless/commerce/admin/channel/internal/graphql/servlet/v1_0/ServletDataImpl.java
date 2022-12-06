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

package com.liferay.headless.commerce.admin.channel.internal.graphql.servlet.v1_0;

import com.liferay.headless.commerce.admin.channel.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.headless.commerce.admin.channel.internal.graphql.query.v1_0.Query;
import com.liferay.headless.commerce.admin.channel.internal.resource.v1_0.ChannelResourceImpl;
import com.liferay.headless.commerce.admin.channel.internal.resource.v1_0.TaxCategoryResourceImpl;
import com.liferay.headless.commerce.admin.channel.resource.v1_0.ChannelResource;
import com.liferay.headless.commerce.admin.channel.resource.v1_0.TaxCategoryResource;
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
 * @author Andrea Sbarra
 * @generated
 */
@Component(service = ServletData.class)
@Generated("")
public class ServletDataImpl implements ServletData {

	@Activate
	public void activate(BundleContext bundleContext) {
		Mutation.setChannelResourceComponentServiceObjects(
			_channelResourceComponentServiceObjects);

		Query.setChannelResourceComponentServiceObjects(
			_channelResourceComponentServiceObjects);
		Query.setTaxCategoryResourceComponentServiceObjects(
			_taxCategoryResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.Headless.Commerce.Admin.Channel";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/headless-commerce-admin-channel-graphql/v1_0";
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
						"mutation#createChannel",
						new ObjectValuePair<>(
							ChannelResourceImpl.class, "postChannel"));
					put(
						"mutation#createChannelBatch",
						new ObjectValuePair<>(
							ChannelResourceImpl.class, "postChannelBatch"));
					put(
						"mutation#deleteChannel",
						new ObjectValuePair<>(
							ChannelResourceImpl.class, "deleteChannel"));
					put(
						"mutation#deleteChannelBatch",
						new ObjectValuePair<>(
							ChannelResourceImpl.class, "deleteChannelBatch"));
					put(
						"mutation#patchChannel",
						new ObjectValuePair<>(
							ChannelResourceImpl.class, "patchChannel"));
					put(
						"mutation#updateChannel",
						new ObjectValuePair<>(
							ChannelResourceImpl.class, "putChannel"));
					put(
						"mutation#updateChannelBatch",
						new ObjectValuePair<>(
							ChannelResourceImpl.class, "putChannelBatch"));

					put(
						"query#channels",
						new ObjectValuePair<>(
							ChannelResourceImpl.class, "getChannelsPage"));
					put(
						"query#channel",
						new ObjectValuePair<>(
							ChannelResourceImpl.class, "getChannel"));
					put(
						"query#taxCategories",
						new ObjectValuePair<>(
							TaxCategoryResourceImpl.class,
							"getTaxCategoriesPage"));
					put(
						"query#taxCategory",
						new ObjectValuePair<>(
							TaxCategoryResourceImpl.class, "getTaxCategory"));
				}
			};

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<ChannelResource>
		_channelResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<TaxCategoryResource>
		_taxCategoryResourceComponentServiceObjects;

}