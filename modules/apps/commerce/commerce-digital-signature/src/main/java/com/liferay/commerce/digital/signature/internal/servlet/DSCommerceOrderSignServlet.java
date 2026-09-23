/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.internal.servlet;

import com.liferay.commerce.model.CommerceOrderAttachment;
import com.liferay.commerce.service.CommerceOrderAttachmentService;
import com.liferay.digital.signature.constants.DigitalSignaturePortletKeys;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.digital.signature.url.SignDSURLProvider;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactory;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(
	property = {
		"osgi.http.whiteboard.context.path=/commerce-digital-signature",
		"osgi.http.whiteboard.servlet.name=com.liferay.commerce.digital.signature.internal.servlet.DSCommerceOrderSignServlet",
		"osgi.http.whiteboard.servlet.pattern=/commerce-digital-signature/sign/*"
	},
	service = Servlet.class
)
public class DSCommerceOrderSignServlet extends HttpServlet {

	@Override
	protected void doGet(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		String backURL = _portal.escapeRedirect(
			ParamUtil.getString(httpServletRequest, "backURL"));

		if (Validator.isNull(backURL)) {
			backURL = _portal.getPortalURL(httpServletRequest);
		}

		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();
		String name = PrincipalThreadLocal.getName();

		try {
			User user = _portal.getUser(httpServletRequest);

			if (user == null) {
				httpServletResponse.sendRedirect(backURL);

				return;
			}

			PrincipalThreadLocal.setName(user.getUserId());
			PermissionThreadLocal.setPermissionChecker(
				_permissionCheckerFactory.create(user));

			CommerceOrderAttachment commerceOrderAttachment =
				_commerceOrderAttachmentService.fetchCommerceOrderAttachment(
					ParamUtil.getLong(
						httpServletRequest, "commerceOrderAttachmentId"));

			long companyId = _portal.getCompanyId(httpServletRequest);

			DSRequest dsRequest = null;

			if (commerceOrderAttachment != null) {
				dsRequest = _dsRequestManager.fetchDSRequest(
					companyId, commerceOrderAttachment.getFileEntryId());
			}

			if ((dsRequest == null) ||
				!dsRequest.isSignatureRequired(user.getEmailAddress())) {

				httpServletResponse.sendRedirect(backURL);

				return;
			}

			String portletNamespace = _portal.getPortletNamespace(
				DigitalSignaturePortletKeys.SIGN_DIGITAL_SIGNATURE);

			httpServletResponse.sendRedirect(
				HttpComponentsUtil.addParameter(
					_signDSURLProvider.getURL(
						companyId, dsRequest.getProviderRequestId()),
					portletNamespace + "backURL", backURL));
		}
		catch (PrincipalException principalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(principalException);
			}

			httpServletResponse.sendRedirect(backURL);
		}
		catch (Exception exception) {
			throw new IOException(exception);
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
			PrincipalThreadLocal.setName(name);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DSCommerceOrderSignServlet.class);

	@Reference
	private CommerceOrderAttachmentService _commerceOrderAttachmentService;

	@Reference
	private DSRequestManager _dsRequestManager;

	@Reference
	private PermissionCheckerFactory _permissionCheckerFactory;

	@Reference
	private Portal _portal;

	@Reference
	private SignDSURLProvider _signDSURLProvider;

}