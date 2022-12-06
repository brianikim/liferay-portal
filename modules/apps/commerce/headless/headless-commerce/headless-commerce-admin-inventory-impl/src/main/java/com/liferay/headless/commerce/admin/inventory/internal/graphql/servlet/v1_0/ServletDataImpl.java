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

package com.liferay.headless.commerce.admin.inventory.internal.graphql.servlet.v1_0;

import com.liferay.headless.commerce.admin.inventory.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.headless.commerce.admin.inventory.internal.graphql.query.v1_0.Query;
import com.liferay.headless.commerce.admin.inventory.internal.resource.v1_0.WarehouseItemResourceImpl;
import com.liferay.headless.commerce.admin.inventory.internal.resource.v1_0.WarehouseResourceImpl;
import com.liferay.headless.commerce.admin.inventory.resource.v1_0.WarehouseItemResource;
import com.liferay.headless.commerce.admin.inventory.resource.v1_0.WarehouseResource;
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
		Mutation.setWarehouseResourceComponentServiceObjects(
			_warehouseResourceComponentServiceObjects);
		Mutation.setWarehouseItemResourceComponentServiceObjects(
			_warehouseItemResourceComponentServiceObjects);

		Query.setWarehouseResourceComponentServiceObjects(
			_warehouseResourceComponentServiceObjects);
		Query.setWarehouseItemResourceComponentServiceObjects(
			_warehouseItemResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.Headless.Commerce.Admin.Inventory";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/headless-commerce-admin-inventory-graphql/v1_0";
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
						"mutation#deleteWarehousByExternalReferenceCode",
						new ObjectValuePair<>(
							WarehouseResourceImpl.class,
							"deleteWarehousByExternalReferenceCode"));
					put(
						"mutation#patchWarehousByExternalReferenceCode",
						new ObjectValuePair<>(
							WarehouseResourceImpl.class,
							"patchWarehousByExternalReferenceCode"));
					put(
						"mutation#deleteWarehousId",
						new ObjectValuePair<>(
							WarehouseResourceImpl.class, "deleteWarehousId"));
					put(
						"mutation#patchWarehousId",
						new ObjectValuePair<>(
							WarehouseResourceImpl.class, "patchWarehousId"));
					put(
						"mutation#createWarehous",
						new ObjectValuePair<>(
							WarehouseResourceImpl.class, "postWarehous"));
					put(
						"mutation#deleteWarehouseItemByExternalReferenceCode",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"deleteWarehouseItemByExternalReferenceCode"));
					put(
						"mutation#patchWarehouseItemByExternalReferenceCode",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"patchWarehouseItemByExternalReferenceCode"));
					put(
						"mutation#createWarehouseItemByExternalReferenceCode",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"postWarehouseItemByExternalReferenceCode"));
					put(
						"mutation#deleteWarehouseItem",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"deleteWarehouseItem"));
					put(
						"mutation#deleteWarehouseItemBatch",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"deleteWarehouseItemBatch"));
					put(
						"mutation#patchWarehouseItem",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"patchWarehouseItem"));
					put(
						"mutation#createWarehousByExternalReferenceCodeWarehouseItem",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"postWarehousByExternalReferenceCodeWarehouseItem"));
					put(
						"mutation#createWarehousIdWarehouseItem",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"postWarehousIdWarehouseItem"));

					put(
						"query#warehousByExternalReferenceCode",
						new ObjectValuePair<>(
							WarehouseResourceImpl.class,
							"getWarehousByExternalReferenceCode"));
					put(
						"query#warehousId",
						new ObjectValuePair<>(
							WarehouseResourceImpl.class, "getWarehousId"));
					put(
						"query#warehouses",
						new ObjectValuePair<>(
							WarehouseResourceImpl.class, "getWarehousesPage"));
					put(
						"query#warehouseItemByExternalReferenceCode",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"getWarehouseItemByExternalReferenceCode"));
					put(
						"query#warehouseItem",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"getWarehouseItem"));
					put(
						"query#warehousByExternalReferenceCodeWarehouseItems",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"getWarehousByExternalReferenceCodeWarehouseItemsPage"));
					put(
						"query#warehousIdWarehouseItems",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"getWarehousIdWarehouseItemsPage"));
					put(
						"query#warehouseItemsUpdated",
						new ObjectValuePair<>(
							WarehouseItemResourceImpl.class,
							"getWarehouseItemsUpdatedPage"));
				}
			};

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<WarehouseResource>
		_warehouseResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<WarehouseItemResource>
		_warehouseItemResourceComponentServiceObjects;

}