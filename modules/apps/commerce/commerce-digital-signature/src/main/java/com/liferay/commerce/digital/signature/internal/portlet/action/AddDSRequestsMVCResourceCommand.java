/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.internal.portlet.action;

import com.liferay.commerce.constants.CommercePortletKeys;
import com.liferay.commerce.digital.signature.internal.helper.DSCommerceOrderAttachmentHelper;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderAttachment;
import com.liferay.commerce.service.CommerceOrderAttachmentLocalService;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.digital.signature.manager.DSEnvelopeManager;
import com.liferay.digital.signature.model.DSDocument;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.digital.signature.request.DSRequestManager;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.portlet.ResourceRequest;
import jakarta.portlet.ResourceResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(
	property = {
		"jakarta.portlet.name=" + CommercePortletKeys.COMMERCE_ORDER,
		"mvc.command.name=/commerce_order/add_ds_requests"
	},
	service = MVCResourceCommand.class
)
public class AddDSRequestsMVCResourceCommand extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		CommerceOrder commerceOrder =
			_commerceOrderLocalService.getCommerceOrder(
				ParamUtil.getLong(resourceRequest, "commerceOrderId"));

		_commerceOrderModelResourcePermission.check(
			themeDisplay.getPermissionChecker(), commerceOrder,
			ActionKeys.UPDATE);

		if (!_dsCommerceOrderAttachmentHelper.isEnabled(commerceOrder)) {
			throw new PortalException(
				"Digital signature is not enabled for order " +
					commerceOrder.getCommerceOrderId());
		}

		List<CommerceOrderAttachment> commerceOrderAttachments =
			_getCommerceOrderAttachments(commerceOrder, resourceRequest);

		List<DSRecipient> orderedDSRecipients = _getDSRecipients(
			commerceOrder, resourceRequest);

		int expireAfterDays = ParamUtil.getInteger(
			resourceRequest, "expireAfter");

		for (CommerceOrderAttachment commerceOrderAttachment :
				commerceOrderAttachments) {

			DSEnvelope dsEnvelope = _dsEnvelopeManager.addDSEnvelope(
				commerceOrder.getCompanyId(), commerceOrder.getGroupId(),
				new DSEnvelope() {
					{
						dsDocuments = List.of(
							_toDSDocument(
								commerceOrderAttachment.getFileEntryId()));
						dsRecipients = orderedDSRecipients;
						emailBlurb = ParamUtil.getString(
							resourceRequest, "emailMessage");
						emailSubject = ParamUtil.getString(
							resourceRequest, "emailSubject");
						expireAfter = expireAfterDays;
						name = commerceOrderAttachment.getTitle();
						senderEmailAddress = themeDisplay.getUser(
						).getEmailAddress();
						status = "sent";
					}
				});

			_dsRequestManager.addDSRequest(
				commerceOrder.getCompanyId(), commerceOrder.getGroupId(),
				themeDisplay.getUserId(), dsEnvelope,
				new long[] {commerceOrderAttachment.getFileEntryId()});
		}

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse,
			JSONUtil.put("count", commerceOrderAttachments.size()));
	}

	private List<CommerceOrderAttachment> _getCommerceOrderAttachments(
			CommerceOrder commerceOrder, ResourceRequest resourceRequest)
		throws Exception {

		List<CommerceOrderAttachment> commerceOrderAttachments =
			new ArrayList<>();

		for (long commerceOrderAttachmentId :
				ParamUtil.getLongValues(
					resourceRequest, "commerceOrderAttachmentIds")) {

			CommerceOrderAttachment commerceOrderAttachment =
				_commerceOrderAttachmentLocalService.getCommerceOrderAttachment(
					commerceOrderAttachmentId);

			if (commerceOrderAttachment.getCommerceOrderId() !=
					commerceOrder.getCommerceOrderId()) {

				throw new PortalException(
					StringBundler.concat(
						"Commerce order attachment ", commerceOrderAttachmentId,
						" does not belong to order ",
						commerceOrder.getCommerceOrderId()));
			}

			if (!_dsCommerceOrderAttachmentHelper.isRequestable(
					_dsRequestManager.fetchDSRequest(
						commerceOrder.getCompanyId(),
						commerceOrderAttachment.getFileEntryId()))) {

				throw new PortalException(
					StringBundler.concat(
						"Commerce order attachment ", commerceOrderAttachmentId,
						" already has an active or completed signature ",
						"request"));
			}

			commerceOrderAttachments.add(commerceOrderAttachment);
		}

		if (commerceOrderAttachments.isEmpty()) {
			throw new PortalException(
				"No commerce order attachments were selected");
		}

		return commerceOrderAttachments;
	}

	private List<DSRecipient> _getDSRecipients(
			CommerceOrder commerceOrder, ResourceRequest resourceRequest)
		throws Exception {

		List<DSRecipient> dsRecipients = new ArrayList<>();

		Set<Long> accountUserIds = new HashSet<>();

		for (User user :
				_dsCommerceOrderAttachmentHelper.getAccountUsers(
					commerceOrder)) {

			accountUserIds.add(user.getUserId());
		}

		int countersignerCount = 0;
		boolean sequential = ParamUtil.getBoolean(
			resourceRequest, "sequential", true);

		for (long recipientUserId :
				ParamUtil.getLongValues(resourceRequest, "recipientUserIds")) {

			User user = _userLocalService.getUser(recipientUserId);

			if ((user.getCompanyId() != commerceOrder.getCompanyId()) ||
				!user.isActive()) {

				throw new PortalException(
					StringBundler.concat(
						"User ", recipientUserId, " is not allowed to sign ",
						"order ", commerceOrder.getCommerceOrderId()));
			}

			if (!accountUserIds.contains(recipientUserId)) {
				countersignerCount++;
			}

			if (countersignerCount > 1) {
				throw new PortalException(
					"Only one internal countersigner is allowed");
			}

			int routingOrder = 1;

			if (sequential) {
				routingOrder = dsRecipients.size() + 1;
			}

			DSRecipient dsRecipient = new DSRecipient();

			dsRecipient.setDSRecipientId(
				String.valueOf(dsRecipients.size() + 1));
			dsRecipient.setEmailAddress(user.getEmailAddress());
			dsRecipient.setName(user.getFullName());
			dsRecipient.setRoutingOrder(routingOrder);

			dsRecipients.add(dsRecipient);
		}

		if (dsRecipients.isEmpty()) {
			throw new PortalException("At least one recipient is required");
		}

		return dsRecipients;
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
	private CommerceOrderAttachmentLocalService
		_commerceOrderAttachmentLocalService;

	@Reference
	private CommerceOrderLocalService _commerceOrderLocalService;

	@Reference(
		target = "(model.class.name=com.liferay.commerce.model.CommerceOrder)"
	)
	private ModelResourcePermission<CommerceOrder>
		_commerceOrderModelResourcePermission;

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference
	private DSCommerceOrderAttachmentHelper _dsCommerceOrderAttachmentHelper;

	@Reference
	private DSEnvelopeManager _dsEnvelopeManager;

	@Reference
	private DSRequestManager _dsRequestManager;

	@Reference
	private UserLocalService _userLocalService;

}