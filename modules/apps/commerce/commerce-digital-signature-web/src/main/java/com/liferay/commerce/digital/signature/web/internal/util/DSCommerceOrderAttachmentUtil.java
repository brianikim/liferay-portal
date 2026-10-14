/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.web.internal.util;

import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderAttachment;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalServiceUtil;
import com.liferay.commerce.service.CommerceOrderAttachmentLocalServiceUtil;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.ArrayUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Brian I. Kim
 */
public class DSCommerceOrderAttachmentUtil {

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

		Map<Long, Long> fileEntryIds = new LinkedHashMap<>();

		for (CommerceOrderAttachment commerceOrderAttachment :
				CommerceOrderAttachmentLocalServiceUtil.
					getCommerceOrderAttachments(
						commerceOrder.getCommerceOrderId(), QueryUtil.ALL_POS,
						QueryUtil.ALL_POS, null)) {

				fileEntryIds.put(
					commerceOrderAttachment.getCommerceOrderAttachmentId(),
					commerceOrderAttachment.getFileEntryId());
			}
		}
		catch (PortalException portalException) {
			_log.error(portalException);

			return Collections.emptyMap();
		}

		DSRequestManager dsRequestManager = _dsRequestManagerSnapshot.get();

		Map<Long, DSRequest> latestFileEntryDSRequests =
			dsRequestManager.getLatestFileEntryDSRequests(
				commerceOrder.getCompanyId(),
				ArrayUtil.toLongArray(fileEntryIds.values()));

		dsRequests = new LinkedHashMap<>();

		for (Map.Entry<Long, Long> entry : fileEntryIds.entrySet()) {
			DSRequest dsRequest = latestFileEntryDSRequests.get(entry.getValue());

			if (dsRequest == null) {
				continue;
			}

			dsRequests.put(entry.getKey(), dsRequest);
		}

		httpServletRequest.setAttribute(key, dsRequests);

		return dsRequests;
	}

	public static String getErrorMessage(
		Language language, Locale locale, PortalException portalException) {

		String key = _errorMessageKeys.get(portalException.getClass());

		if (Validator.isNull(key)) {
			return portalException.getMessage();
		}

		return language.get(locale, key);
	}

	public static long getSiteGroupId(CommerceOrder commerceOrder) {
		CommerceChannel commerceChannel =
			CommerceChannelLocalServiceUtil.fetchCommerceChannelByGroupClassPK(
				commerceOrder.getGroupId());

		if (commerceChannel == null) {
			return 0;
		}

		return commerceChannel.getSiteGroupId();
	}

	public static boolean isEnabled(CommerceOrder commerceOrder) {
		if (!FeatureFlagManagerUtil.isEnabled(
				commerceOrder.getCompanyId(), "LPD-69290")) {

			return false;
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				commerceOrder.getCompanyId(), getSiteGroupId(commerceOrder));

		if (digitalSignatureConfiguration.embeddedViewEnabled() &&
			digitalSignatureConfiguration.enabled()) {

			return true;
		}

		return false;
	}

	private static final Snapshot<DSRequestManager> _dsRequestManagerSnapshot =
		new Snapshot<>(
			DSCommerceOrderAttachmentUtil.class, DSRequestManager.class);
	private static final Map<Class<?>, String> _errorMessageKeys =
		HashMapBuilder.<Class<?>, String>put(
			DSCommerceOrderAttachmentException.InvalidRecipient.class,
			"one-or-more-recipients-cannot-sign-this-order"
		).put(
			DSCommerceOrderAttachmentException.MustHaveRecipient.class,
			"at-least-one-recipient-is-required"
		).put(
			DSCommerceOrderAttachmentException.MustHaveDSRequest.class,
			"no-signature-request-was-found-for-this-document"
		).put(
			DSCommerceOrderAttachmentException.MustBeEnabled.class,
			"digital-signature-is-not-enabled-for-this-order"
		).put(
			DSCommerceOrderAttachmentException.MustNotHaveMultipleCountersigners.class,
			"only-one-internal-countersigner-is-allowed"
		).put(
			DSCommerceOrderAttachmentException.InvalidFileExtension.class,
			"this-file-extension-is-not-supported-by-docusign"
		).put(
			DSRequestException.ExpireWarnMustBeLessThanExpireAfter.class,
			"days-to-warn-recipients-must-be-fewer-than-days-until-expiration"
		).put(
			DSRequestException.FileEntryHasDSRequest.class,
			"this-document-already-has-an-active-or-completed-signature-request"
		).put(
			DSRequestException.MustBeActive.class,
			"this-signature-request-is-no-longer-active"
		).put(
			DSRequestException.MustBeEnabled.class,
			"digital-signature-is-not-enabled-for-this-order"
		).build();
	private static final Log _log = LogFactoryUtil.getLog(
		DSCommerceOrderAttachmentUtil.class);

}