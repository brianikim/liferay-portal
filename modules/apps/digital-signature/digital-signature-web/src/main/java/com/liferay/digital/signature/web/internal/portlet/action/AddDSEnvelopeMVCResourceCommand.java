/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.web.internal.portlet.action;

import com.liferay.digital.signature.constants.DigitalSignaturePortletKeys;
import com.liferay.digital.signature.manager.DSEnvelopeManager;
import com.liferay.digital.signature.model.DSDocument;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.digital.signature.request.DSRequestManager;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.IntegerWrapper;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.ResourceRequest;
import jakarta.portlet.ResourceResponse;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author José Abelenda
 */
@Component(
	property = {
		"jakarta.portlet.name=" + DigitalSignaturePortletKeys.COLLECT_DIGITAL_SIGNATURE,
		"jakarta.portlet.name=" + DigitalSignaturePortletKeys.DIGITAL_SIGNATURE,
		"mvc.command.name=/digital_signature/add_ds_envelope"
	},
	service = MVCResourceCommand.class
)
public class AddDSEnvelopeMVCResourceCommand extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		User user = themeDisplay.getUser();

		DSEnvelope dsEnvelope = _dsEnvelopeManager.addDSEnvelope(
			themeDisplay.getCompanyId(), themeDisplay.getSiteGroupId(),
			new DSEnvelope() {
				{
					dsDocuments = _getDSDocuments(resourceRequest);
					dsRecipients = _getDSRecipients(
						themeDisplay.getCompanyId(), resourceRequest);
					emailBlurb = ParamUtil.getString(
						resourceRequest, "emailMessage");
					emailSubject = ParamUtil.getString(
						resourceRequest, "emailSubject");
					name = ParamUtil.getString(resourceRequest, "envelopeName");
					senderEmailAddress = user.getEmailAddress();
					status = "sent";
				}
			});

		_dsRequestManager.addDSRequests(
			themeDisplay.getCompanyId(), themeDisplay.getSiteGroupId(),
			user.getUserId(), dsEnvelope);

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse,
			JSONUtil.put("dsEnvelopeId", dsEnvelope.getDSEnvelopeId()));
	}

	private String _getDSClientUserId(long companyId, String emailAddress) {

		// Bind the recipient to a logged-in Liferay user so the provider treats
		// them as a captive recipient and signing happens embedded in Liferay
		// rather than through a provider email link.

		User user = _userLocalService.fetchUserByEmailAddress(
			companyId, emailAddress);

		if (user == null) {
			return null;
		}

		return String.valueOf(user.getUserId());
	}

	private List<DSDocument> _getDSDocuments(ResourceRequest resourceRequest)
		throws Exception {

		return TransformUtil.transformToList(
			ArrayUtil.toLongArray(
				ParamUtil.getLongValues(resourceRequest, "fileEntryIds")),
			fileEntryId -> _toDSDocument(fileEntryId));
	}

	private List<DSRecipient> _getDSRecipients(
			long companyId, ResourceRequest resourceRequest)
		throws Exception {

		IntegerWrapper integerWrapper = new IntegerWrapper();

		return JSONUtil.toList(
			_jsonFactory.createJSONArray(
				ParamUtil.getString(resourceRequest, "recipients")),
			recipientJSONObject -> new DSRecipient() {
				{
					dsClientUserId = _getDSClientUserId(
						companyId, recipientJSONObject.getString("email"));
					dsRecipientId = String.valueOf(integerWrapper.increment());
					emailAddress = recipientJSONObject.getString("email");
					name = recipientJSONObject.getString("fullName");
				}
			});
	}

	private DSDocument _toDSDocument(long fileEntryId) throws Exception {
		FileEntry fileEntry = _dlAppLocalService.getFileEntry(fileEntryId);

		return new DSDocument() {
			{
				data = Base64.encode(
					FileUtil.getBytes(fileEntry.getContentStream()));
				dsDocumentId = String.valueOf(fileEntryId);
				fileExtension = fileEntry.getExtension();
				name = fileEntry.getFileName();
			}
		};
	}

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference
	private DSEnvelopeManager _dsEnvelopeManager;

	@Reference
	private DSRequestManager _dsRequestManager;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private UserLocalService _userLocalService;

}