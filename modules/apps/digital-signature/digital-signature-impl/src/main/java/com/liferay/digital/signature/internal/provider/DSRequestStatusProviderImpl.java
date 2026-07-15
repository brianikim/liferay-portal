/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.provider;

import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.provider.DSRequestStatusProvider;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Kim
 */
@Component(service = DSRequestStatusProvider.class)
public class DSRequestStatusProviderImpl implements DSRequestStatusProvider {

	@Override
	public Map<Long, String> getRequestStatuses(
		long companyId, Collection<Long> fileEntryIds) {

		if ((fileEntryIds == null) || fileEntryIds.isEmpty() ||
			!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-69290")) {

			return Collections.emptyMap();
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				companyId, 0);

		if ((digitalSignatureConfiguration == null) ||
			!digitalSignatureConfiguration.enabled()) {

			return Collections.emptyMap();
		}

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		if (objectDefinition == null) {
			return Collections.emptyMap();
		}

		try {
			Map<Long, String> requestStatuses = new HashMap<>();

			// Assumes at most one signature request per document, which is the
			// request-level roll-up model in LPD-97581.

			List<Map<String, Serializable>> valuesList =
				_objectEntryLocalService.getValuesList(
					0, companyId, _userLocalService.getGuestUserId(companyId),
					objectDefinition.getObjectDefinitionId(),
					_filterFactory.create(
						_getFilterString(fileEntryIds), objectDefinition),
					null, 0, fileEntryIds.size(), null);

			for (Map<String, Serializable> values : valuesList) {
				requestStatuses.put(
					GetterUtil.getLong(values.get("fileEntryId")),
					GetterUtil.getString(values.get("requestStatus")));
			}

			return requestStatuses;
		}
		catch (Exception exception) {
			_log.error(
				"Unable to load signature request statuses for company " +
					companyId,
				exception);

			return Collections.emptyMap();
		}
	}

	private String _getFilterString(Collection<Long> fileEntryIds) {
		List<String> predicateStrings = new ArrayList<>(fileEntryIds.size());

		for (Long fileEntryId : fileEntryIds) {
			predicateStrings.add("(fileEntryId eq " + fileEntryId + ")");
		}

		return StringUtil.merge(predicateStrings, " or ");
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DSRequestStatusProviderImpl.class);

	@Reference(
		target = "(filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private FilterFactory<Predicate> _filterFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private UserLocalService _userLocalService;

}