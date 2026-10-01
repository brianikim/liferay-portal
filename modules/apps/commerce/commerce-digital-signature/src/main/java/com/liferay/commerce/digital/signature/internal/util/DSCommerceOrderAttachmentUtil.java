/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.internal.util;

import com.liferay.account.service.AccountEntryUserRelLocalService;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderAttachment;
import com.liferay.commerce.service.CommerceOrderAttachmentService;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HttpComponentsUtil;
import com.liferay.portal.kernel.util.Portal;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brian I. Kim
 */
public class DSCommerceOrderAttachmentUtil {

	public static List<User> getAccountUsers(CommerceOrder commerceOrder) {
		AccountEntryUserRelLocalService accountEntryUserRelLocalService =
			_accountEntryUserRelLocalServiceSnapshot.get();

		UserLocalService userLocalService = _userLocalServiceSnapshot.get();

		return TransformUtil.transform(
			accountEntryUserRelLocalService.
				getAccountEntryUserRelsByAccountEntryId(
					commerceOrder.getCommerceAccountId()),
			accountEntryUserRel -> {
				User user = userLocalService.fetchUser(
					accountEntryUserRel.getAccountUserId());

				if ((user == null) || !user.isActive()) {
					return null;
				}

				return user;
			});
	}

	public static String getActionURL(String path, ThemeDisplay themeDisplay) {
		String url = HttpComponentsUtil.addParameter(
			StringBundler.concat(
				themeDisplay.getPortalURL(), Portal.PATH_MODULE, path),
			"backURL", themeDisplay.getURLCurrent());

		return url + "&commerceOrderAttachmentId={id}";
	}

	public static Map<Long, DSRequest> getDSRequests(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		if (!isEnabled(commerceOrder)) {
			return Collections.emptyMap();
		}

		String key =
			DSCommerceOrderAttachmentUtil.class.getName() +
				commerceOrder.getCommerceOrderId();

		Map<Long, DSRequest> dsRequests =
			(Map<Long, DSRequest>)httpServletRequest.getAttribute(key);

		if (dsRequests != null) {
			return dsRequests;
		}

		CommerceOrderAttachmentService commerceOrderAttachmentService =
			_commerceOrderAttachmentServiceSnapshot.get();

		List<CommerceOrderAttachment> commerceOrderAttachments = null;

		try {
			commerceOrderAttachments =
				commerceOrderAttachmentService.getCommerceOrderAttachments(
					commerceOrder.getCommerceOrderId(), QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, null);
		}
		catch (PortalException portalException) {
			throw new SystemException(portalException);
		}

		Map<Long, Long> fileEntryIdsByCommerceOrderAttachmentId =
			new LinkedHashMap<>();

		for (CommerceOrderAttachment commerceOrderAttachment :
				commerceOrderAttachments) {

			fileEntryIdsByCommerceOrderAttachmentId.put(
				commerceOrderAttachment.getCommerceOrderAttachmentId(),
				commerceOrderAttachment.getFileEntryId());
		}

		DSRequestManager dsRequestManager = _dsRequestManagerSnapshot.get();

		Map<Long, DSRequest> dsRequestsByFileEntryId =
			dsRequestManager.getDSRequests(
				commerceOrder.getCompanyId(),
				fileEntryIdsByCommerceOrderAttachmentId.values());

		dsRequests = new LinkedHashMap<>();

		for (Map.Entry<Long, Long> entry :
				fileEntryIdsByCommerceOrderAttachmentId.entrySet()) {

			DSRequest dsRequest = dsRequestsByFileEntryId.get(entry.getValue());

			if (dsRequest != null) {
				dsRequests.put(entry.getKey(), dsRequest);
			}
		}

		httpServletRequest.setAttribute(key, dsRequests);

		return dsRequests;
	}

	public static Map<String, String> getSignatureStatuses(
		Map<Long, DSRequest> dsRequests) {

		Map<String, String> signatureStatuses = new HashMap<>();

		for (Map.Entry<Long, DSRequest> entry : dsRequests.entrySet()) {
			DSRequest dsRequest = entry.getValue();

			signatureStatuses.put(
				String.valueOf(entry.getKey()), dsRequest.getStatus());
		}

		return signatureStatuses;
	}

	public static JSONObject getUserJSONObject(User user) {
		return JSONUtil.put(
			"emailAddress", user.getEmailAddress()
		).put(
			"name", user.getFullName()
		).put(
			"userId", user.getUserId()
		);
	}

	public static boolean isEnabled(CommerceOrder commerceOrder) {
		return DigitalSignatureConfigurationUtil.isEmbeddedViewEnabled(
			commerceOrder.getCompanyId(), commerceOrder.getGroupId());
	}

	private static final Snapshot<AccountEntryUserRelLocalService>
		_accountEntryUserRelLocalServiceSnapshot = new Snapshot<>(
			DSCommerceOrderAttachmentUtil.class,
			AccountEntryUserRelLocalService.class);
	private static final Snapshot<CommerceOrderAttachmentService>
		_commerceOrderAttachmentServiceSnapshot = new Snapshot<>(
			DSCommerceOrderAttachmentUtil.class,
			CommerceOrderAttachmentService.class);
	private static final Snapshot<DSRequestManager> _dsRequestManagerSnapshot =
		new Snapshot<>(
			DSCommerceOrderAttachmentUtil.class, DSRequestManager.class);
	private static final Snapshot<UserLocalService> _userLocalServiceSnapshot =
		new Snapshot<>(
			DSCommerceOrderAttachmentUtil.class, UserLocalService.class);

}