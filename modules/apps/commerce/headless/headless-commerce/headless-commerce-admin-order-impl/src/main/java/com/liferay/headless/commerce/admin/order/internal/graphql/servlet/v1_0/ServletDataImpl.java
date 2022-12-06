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

package com.liferay.headless.commerce.admin.order.internal.graphql.servlet.v1_0;

import com.liferay.headless.commerce.admin.order.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.headless.commerce.admin.order.internal.graphql.query.v1_0.Query;
import com.liferay.headless.commerce.admin.order.internal.resource.v1_0.AccountResourceImpl;
import com.liferay.headless.commerce.admin.order.internal.resource.v1_0.BillingAddressResourceImpl;
import com.liferay.headless.commerce.admin.order.internal.resource.v1_0.ChannelResourceImpl;
import com.liferay.headless.commerce.admin.order.internal.resource.v1_0.OrderItemResourceImpl;
import com.liferay.headless.commerce.admin.order.internal.resource.v1_0.OrderNoteResourceImpl;
import com.liferay.headless.commerce.admin.order.internal.resource.v1_0.OrderResourceImpl;
import com.liferay.headless.commerce.admin.order.internal.resource.v1_0.ShippingAddressResourceImpl;
import com.liferay.headless.commerce.admin.order.resource.v1_0.AccountResource;
import com.liferay.headless.commerce.admin.order.resource.v1_0.BillingAddressResource;
import com.liferay.headless.commerce.admin.order.resource.v1_0.ChannelResource;
import com.liferay.headless.commerce.admin.order.resource.v1_0.OrderItemResource;
import com.liferay.headless.commerce.admin.order.resource.v1_0.OrderNoteResource;
import com.liferay.headless.commerce.admin.order.resource.v1_0.OrderResource;
import com.liferay.headless.commerce.admin.order.resource.v1_0.ShippingAddressResource;
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
		Mutation.setBillingAddressResourceComponentServiceObjects(
			_billingAddressResourceComponentServiceObjects);
		Mutation.setOrderResourceComponentServiceObjects(
			_orderResourceComponentServiceObjects);
		Mutation.setOrderItemResourceComponentServiceObjects(
			_orderItemResourceComponentServiceObjects);
		Mutation.setOrderNoteResourceComponentServiceObjects(
			_orderNoteResourceComponentServiceObjects);
		Mutation.setShippingAddressResourceComponentServiceObjects(
			_shippingAddressResourceComponentServiceObjects);

		Query.setAccountResourceComponentServiceObjects(
			_accountResourceComponentServiceObjects);
		Query.setBillingAddressResourceComponentServiceObjects(
			_billingAddressResourceComponentServiceObjects);
		Query.setChannelResourceComponentServiceObjects(
			_channelResourceComponentServiceObjects);
		Query.setOrderResourceComponentServiceObjects(
			_orderResourceComponentServiceObjects);
		Query.setOrderItemResourceComponentServiceObjects(
			_orderItemResourceComponentServiceObjects);
		Query.setOrderNoteResourceComponentServiceObjects(
			_orderNoteResourceComponentServiceObjects);
		Query.setShippingAddressResourceComponentServiceObjects(
			_shippingAddressResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.Headless.Commerce.Admin.Order";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/headless-commerce-admin-order-graphql/v1_0";
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
						"mutation#patchOrderByExternalReferenceCodeBillingAddress",
						new ObjectValuePair<>(
							BillingAddressResourceImpl.class,
							"patchOrderByExternalReferenceCodeBillingAddress"));
					put(
						"mutation#patchOrderIdBillingAddress",
						new ObjectValuePair<>(
							BillingAddressResourceImpl.class,
							"patchOrderIdBillingAddress"));
					put(
						"mutation#createOrder",
						new ObjectValuePair<>(
							OrderResourceImpl.class, "postOrder"));
					put(
						"mutation#createOrderBatch",
						new ObjectValuePair<>(
							OrderResourceImpl.class, "postOrderBatch"));
					put(
						"mutation#deleteOrderByExternalReferenceCode",
						new ObjectValuePair<>(
							OrderResourceImpl.class,
							"deleteOrderByExternalReferenceCode"));
					put(
						"mutation#patchOrderByExternalReferenceCode",
						new ObjectValuePair<>(
							OrderResourceImpl.class,
							"patchOrderByExternalReferenceCode"));
					put(
						"mutation#deleteOrder",
						new ObjectValuePair<>(
							OrderResourceImpl.class, "deleteOrder"));
					put(
						"mutation#deleteOrderBatch",
						new ObjectValuePair<>(
							OrderResourceImpl.class, "deleteOrderBatch"));
					put(
						"mutation#patchOrder",
						new ObjectValuePair<>(
							OrderResourceImpl.class, "patchOrder"));
					put(
						"mutation#deleteOrderItemByExternalReferenceCode",
						new ObjectValuePair<>(
							OrderItemResourceImpl.class,
							"deleteOrderItemByExternalReferenceCode"));
					put(
						"mutation#patchOrderItemByExternalReferenceCode",
						new ObjectValuePair<>(
							OrderItemResourceImpl.class,
							"patchOrderItemByExternalReferenceCode"));
					put(
						"mutation#deleteOrderItem",
						new ObjectValuePair<>(
							OrderItemResourceImpl.class, "deleteOrderItem"));
					put(
						"mutation#deleteOrderItemBatch",
						new ObjectValuePair<>(
							OrderItemResourceImpl.class,
							"deleteOrderItemBatch"));
					put(
						"mutation#patchOrderItem",
						new ObjectValuePair<>(
							OrderItemResourceImpl.class, "patchOrderItem"));
					put(
						"mutation#createOrderByExternalReferenceCodeOrderItem",
						new ObjectValuePair<>(
							OrderItemResourceImpl.class,
							"postOrderByExternalReferenceCodeOrderItem"));
					put(
						"mutation#createOrderIdOrderItem",
						new ObjectValuePair<>(
							OrderItemResourceImpl.class,
							"postOrderIdOrderItem"));
					put(
						"mutation#createOrderIdOrderItemBatch",
						new ObjectValuePair<>(
							OrderItemResourceImpl.class,
							"postOrderIdOrderItemBatch"));
					put(
						"mutation#deleteOrderNoteByExternalReferenceCode",
						new ObjectValuePair<>(
							OrderNoteResourceImpl.class,
							"deleteOrderNoteByExternalReferenceCode"));
					put(
						"mutation#patchOrderNoteByExternalReferenceCode",
						new ObjectValuePair<>(
							OrderNoteResourceImpl.class,
							"patchOrderNoteByExternalReferenceCode"));
					put(
						"mutation#deleteOrderNote",
						new ObjectValuePair<>(
							OrderNoteResourceImpl.class, "deleteOrderNote"));
					put(
						"mutation#deleteOrderNoteBatch",
						new ObjectValuePair<>(
							OrderNoteResourceImpl.class,
							"deleteOrderNoteBatch"));
					put(
						"mutation#patchOrderNote",
						new ObjectValuePair<>(
							OrderNoteResourceImpl.class, "patchOrderNote"));
					put(
						"mutation#createOrderByExternalReferenceCodeOrderNote",
						new ObjectValuePair<>(
							OrderNoteResourceImpl.class,
							"postOrderByExternalReferenceCodeOrderNote"));
					put(
						"mutation#createOrderIdOrderNote",
						new ObjectValuePair<>(
							OrderNoteResourceImpl.class,
							"postOrderIdOrderNote"));
					put(
						"mutation#createOrderIdOrderNoteBatch",
						new ObjectValuePair<>(
							OrderNoteResourceImpl.class,
							"postOrderIdOrderNoteBatch"));
					put(
						"mutation#patchOrderByExternalReferenceCodeShippingAddress",
						new ObjectValuePair<>(
							ShippingAddressResourceImpl.class,
							"patchOrderByExternalReferenceCodeShippingAddress"));
					put(
						"mutation#patchOrderIdShippingAddress",
						new ObjectValuePair<>(
							ShippingAddressResourceImpl.class,
							"patchOrderIdShippingAddress"));

					put(
						"query#orderByExternalReferenceCodeAccount",
						new ObjectValuePair<>(
							AccountResourceImpl.class,
							"getOrderByExternalReferenceCodeAccount"));
					put(
						"query#orderIdAccount",
						new ObjectValuePair<>(
							AccountResourceImpl.class, "getOrderIdAccount"));
					put(
						"query#orderByExternalReferenceCodeBillingAddress",
						new ObjectValuePair<>(
							BillingAddressResourceImpl.class,
							"getOrderByExternalReferenceCodeBillingAddress"));
					put(
						"query#orderIdBillingAddress",
						new ObjectValuePair<>(
							BillingAddressResourceImpl.class,
							"getOrderIdBillingAddress"));
					put(
						"query#orderByExternalReferenceCodeChannel",
						new ObjectValuePair<>(
							ChannelResourceImpl.class,
							"getOrderByExternalReferenceCodeChannel"));
					put(
						"query#orderIdChannel",
						new ObjectValuePair<>(
							ChannelResourceImpl.class, "getOrderIdChannel"));
					put(
						"query#orders",
						new ObjectValuePair<>(
							OrderResourceImpl.class, "getOrdersPage"));
					put(
						"query#orderByExternalReferenceCode",
						new ObjectValuePair<>(
							OrderResourceImpl.class,
							"getOrderByExternalReferenceCode"));
					put(
						"query#order",
						new ObjectValuePair<>(
							OrderResourceImpl.class, "getOrder"));
					put(
						"query#orderItemByExternalReferenceCode",
						new ObjectValuePair<>(
							OrderItemResourceImpl.class,
							"getOrderItemByExternalReferenceCode"));
					put(
						"query#orderItem",
						new ObjectValuePair<>(
							OrderItemResourceImpl.class, "getOrderItem"));
					put(
						"query#orderByExternalReferenceCodeOrderItems",
						new ObjectValuePair<>(
							OrderItemResourceImpl.class,
							"getOrderByExternalReferenceCodeOrderItemsPage"));
					put(
						"query#orderIdOrderItems",
						new ObjectValuePair<>(
							OrderItemResourceImpl.class,
							"getOrderIdOrderItemsPage"));
					put(
						"query#orderNoteByExternalReferenceCode",
						new ObjectValuePair<>(
							OrderNoteResourceImpl.class,
							"getOrderNoteByExternalReferenceCode"));
					put(
						"query#orderNote",
						new ObjectValuePair<>(
							OrderNoteResourceImpl.class, "getOrderNote"));
					put(
						"query#orderByExternalReferenceCodeOrderNotes",
						new ObjectValuePair<>(
							OrderNoteResourceImpl.class,
							"getOrderByExternalReferenceCodeOrderNotesPage"));
					put(
						"query#orderIdOrderNotes",
						new ObjectValuePair<>(
							OrderNoteResourceImpl.class,
							"getOrderIdOrderNotesPage"));
					put(
						"query#orderByExternalReferenceCodeShippingAddress",
						new ObjectValuePair<>(
							ShippingAddressResourceImpl.class,
							"getOrderByExternalReferenceCodeShippingAddress"));
					put(
						"query#orderIdShippingAddress",
						new ObjectValuePair<>(
							ShippingAddressResourceImpl.class,
							"getOrderIdShippingAddress"));
				}
			};

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<BillingAddressResource>
		_billingAddressResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<OrderResource>
		_orderResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<OrderItemResource>
		_orderItemResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<OrderNoteResource>
		_orderNoteResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<ShippingAddressResource>
		_shippingAddressResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountResource>
		_accountResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<ChannelResource>
		_channelResourceComponentServiceObjects;

}