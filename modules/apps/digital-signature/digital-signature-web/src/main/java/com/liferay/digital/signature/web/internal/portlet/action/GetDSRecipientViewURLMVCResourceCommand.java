/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.web.internal.portlet.action;

import com.liferay.digital.signature.constants.DigitalSignaturePortletKeys;
import com.liferay.digital.signature.manager.DSRecipientViewDefinitionManager;
import com.liferay.digital.signature.model.DSRecipientViewDefinition;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.ResourceRequest;
import jakarta.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Returns the embedded signing ceremony URL for the current user's recipient
 * view of an envelope, so the signing happens inside Liferay rather than
 * through a provider email link.
 *
 * @author Brian Kim
 */
@Component(
	property = {
		"jakarta.portlet.name=" + DigitalSignaturePortletKeys.COLLECT_DIGITAL_SIGNATURE,
		"jakarta.portlet.name=" + DigitalSignaturePortletKeys.DIGITAL_SIGNATURE,
		"mvc.command.name=/digital_signature/get_ds_recipient_view_url"
	},
	service = MVCResourceCommand.class
)
public class GetDSRecipientViewURLMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		User user = themeDisplay.getUser();

		String url =
			_dsRecipientViewDefinitionManager.addDSRecipientViewDefinition(
				themeDisplay.getCompanyId(), themeDisplay.getSiteGroupId(),
				ParamUtil.getString(resourceRequest, "dsEnvelopeId"),
				new DSRecipientViewDefinition() {
					{
						authenticationMethod = "none";
						dsClientUserId = String.valueOf(user.getUserId());
						emailAddress = user.getEmailAddress();
						returnURL = ParamUtil.getString(
							resourceRequest, "returnURL",
							themeDisplay.getURLCurrent());
						userName = user.getFullName();
					}
				});

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse, JSONUtil.put("url", url));
	}

	@Reference
	private DSRecipientViewDefinitionManager _dsRecipientViewDefinitionManager;

}