/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
package com.liferay.headless.commerce.admin.order.internal.jaxrs.exception.mapper;

import com.liferay.commerce.exception.RequiredOrderItemOptionException;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.BaseExceptionMapper;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.Problem;
import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * @author Lianne Louie
 */
@Component(
	property = {
		"osgi.jaxrs.application.select=(osgi.jaxrs.name=Liferay.Headless.Commerce.Admin.Order)",
		"osgi.jaxrs.extension=true",
		"osgi.jaxrs.name=Liferay.Headless.Commerce.Admin.Order.OrderItemOptionExceptionMapper"
	},
	service = ExceptionMapper.class
)
public class OrderItemOptionExceptionMapper 	extends
	BaseExceptionMapper<RequiredOrderItemOptionException> {

	@Override
	protected Problem getProblem(
		RequiredOrderItemOptionException requiredOrderItemOptionException) {

		return new Problem(
			Response.Status.BAD_REQUEST, "Required option value is invalid");
	}
}
