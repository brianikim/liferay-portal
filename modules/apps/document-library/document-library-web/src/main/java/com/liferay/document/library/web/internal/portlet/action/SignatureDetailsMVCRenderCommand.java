/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.document.library.web.internal.portlet.action;

import com.liferay.digital.signature.request.DSRequestManager;
import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;

import jakarta.portlet.RenderRequest;
import jakarta.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;

/**
 * @author Brian Kim
 */
@Component(
	property = {
		"jakarta.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY,
		"jakarta.portlet.name=" + DLPortletKeys.DOCUMENT_LIBRARY_ADMIN,
		"jakarta.portlet.name=" + DLPortletKeys.MEDIA_GALLERY_DISPLAY,
		"mvc.command.name=/document_library/signature_details"
	},
	service = MVCRenderCommand.class
)
public class SignatureDetailsMVCRenderCommand
	extends BaseFileEntryMVCRenderCommand {

	@Override
	protected String getPath() {
		return "/document_library/signature_details.jsp";
	}

	@Override
	protected void setAttributes(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		DSRequestManager dsRequestManager = _dsRequestManagerSnapshot.get();

		if (dsRequestManager != null) {
			renderRequest.setAttribute(
				DSRequestManager.class.getName(), dsRequestManager);
		}
	}

	private static final Snapshot<DSRequestManager> _dsRequestManagerSnapshot =
		new Snapshot<>(
			SignatureDetailsMVCRenderCommand.class, DSRequestManager.class,
			null, true);

}