/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.digital.signature.web.internal.frontend.data.set.action;

import com.liferay.commerce.digital.signature.web.internal.util.DSCommerceOrderAttachmentUtil;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.order.CommerceOrderAttachmentFDSActionContributor;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemBuilder;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(service = CommerceOrderAttachmentFDSActionContributor.class)
public class DSCommerceOrderAttachmentFDSActionContributor
	implements CommerceOrderAttachmentFDSActionContributor {

	@Override
	public Map<String, Object> getAdditionalProps(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		Map<Long, DSRequest> dsRequests = _getDSRequests(
			commerceOrder, httpServletRequest);

		if (dsRequests.isEmpty()) {
			return Collections.emptyMap();
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return HashMapBuilder.<String, Object>put(
			"dsRequestStatuses",
			DSCommerceOrderAttachmentUtil.getDSRequestStatuses(dsRequests)
		).put(
			"signatureRequiredCommerceOrderAttachmentIds",
			TransformUtil.transform(
				dsRequests.entrySet(),
				entry -> {
					DSRequest dsRequest = entry.getValue();

					if (!dsRequest.isSignatureRequired(
							themeDisplay.getUserId())) {

						return null;
					}

					return String.valueOf(entry.getKey());
				})
		).build();
	}

	@Override
	public List<FDSActionDropdownItem> getFDSActionDropdownItems(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		Map<Long, DSRequest> dsRequests = _getDSRequests(
			commerceOrder, httpServletRequest);

		if (dsRequests.isEmpty()) {
			return Collections.emptyList();
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		boolean signatureRequired = false;

		for (DSRequest dsRequest : dsRequests.values()) {
			if (dsRequest.isSignatureRequired(themeDisplay.getUserId())) {
				signatureRequired = true;

				break;
			}
		}

		List<FDSActionDropdownItem> fdsActionDropdownItems = new ArrayList<>();

		if (signatureRequired) {
			fdsActionDropdownItems.add(
				FDSActionDropdownItemBuilder.putData(
					"signDSRequestURL",
					DSCommerceOrderAttachmentUtil.getActionURL(
						"/commerce-digital-signature/sign", themeDisplay)
				).setHref(
					StringPool.POUND
				).setIcon(
					"pencil"
				).setLabel(
					_language.get(httpServletRequest, "sign")
				).build(
					"sign"
				));
		}

		fdsActionDropdownItems.add(
			FDSActionDropdownItemBuilder.putData(
				"dsRequestDetailsURL",
				DSCommerceOrderAttachmentUtil.getActionURL(
					"/commerce-digital-signature/signature-details",
					themeDisplay)
			).setHref(
				StringPool.POUND
			).setIcon(
				"list-ul"
			).setLabel(
				_language.get(httpServletRequest, "view-signature-details")
			).build(
				"view-signature-details"
			));

		return fdsActionDropdownItems;
	}

	private Map<Long, DSRequest> _getDSRequests(
		CommerceOrder commerceOrder, HttpServletRequest httpServletRequest) {

		try {
			return DSCommerceOrderAttachmentUtil.getDSRequests(
				commerceOrder, httpServletRequest);
		}
		catch (PortalException portalException) {
			_log.error(portalException);

			return Collections.emptyMap();
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DSCommerceOrderAttachmentFDSActionContributor.class);

	@Reference
	private Language _language;

}