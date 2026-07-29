/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.provider;

import com.liferay.digital.signature.provider.DSActiveRequestResolver;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Kim
 */
@Component(service = DSActiveRequestResolver.class)
public class DSActiveRequestResolverImpl implements DSActiveRequestResolver {

	@Override
	public Map<Long, Long> getActiveFileEntryIdsByRequestId(
			long companyId, Collection<Long> fileEntryIds)
		throws Exception {

		Map<Long, Long> fileEntryIdsByRequestId = new HashMap<>();

		ObjectDefinition requestObjectDefinition =
			_fetchRequestObjectDefinition(companyId);

		if (requestObjectDefinition == null) {
			return fileEntryIdsByRequestId;
		}

		for (Map.Entry<Long, Map<String, Serializable>> entry :
				_getActiveRequestValuesByFileEntryId(
					companyId, requestObjectDefinition, fileEntryIds
				).entrySet()) {

			fileEntryIdsByRequestId.put(
				GetterUtil.getLong(
					entry.getValue(
					).get(
						requestObjectDefinition.getPKObjectFieldName()
					)),
				entry.getKey());
		}

		return fileEntryIdsByRequestId;
	}

	@Override
	public Map<Long, Map<String, Serializable>>
			getActiveRequestValuesByFileEntryId(
				long companyId, Collection<Long> fileEntryIds)
		throws Exception {

		ObjectDefinition requestObjectDefinition =
			_fetchRequestObjectDefinition(companyId);

		if (requestObjectDefinition == null) {
			return new HashMap<>();
		}

		return _getActiveRequestValuesByFileEntryId(
			companyId, requestObjectDefinition, fileEntryIds);
	}

	private ObjectDefinition _fetchRequestObjectDefinition(long companyId) {
		return _objectDefinitionLocalService.
			fetchObjectDefinitionByExternalReferenceCode(
				"L_DS_REQUEST", companyId);
	}

	private Map<Long, Map<String, Serializable>>
			_getActiveRequestValuesByFileEntryId(
				long companyId, ObjectDefinition requestObjectDefinition,
				Collection<Long> fileEntryIds)
		throws Exception {

		Map<Long, Map<String, Serializable>> activeRequestValuesByFileEntryId =
			new HashMap<>();

		if ((fileEntryIds == null) || fileEntryIds.isEmpty()) {
			return activeRequestValuesByFileEntryId;
		}

		for (Map<String, Serializable> values :
				_getValuesList(
					companyId, requestObjectDefinition,
					_getOrPredicateString("fileEntryId", fileEntryIds))) {

			long fileEntryId = GetterUtil.getLong(values.get("fileEntryId"));

			Map<String, Serializable> currentValues =
				activeRequestValuesByFileEntryId.get(fileEntryId);

			if ((currentValues == null) ||
				_isMoreActive(requestObjectDefinition, values, currentValues)) {

				activeRequestValuesByFileEntryId.put(fileEntryId, values);
			}
		}

		return activeRequestValuesByFileEntryId;
	}

	private String _getOrPredicateString(
		String fieldName, Collection<Long> values) {

		List<String> predicateStrings = new ArrayList<>(values.size());

		for (Long value : values) {
			predicateStrings.add(
				StringBundler.concat("(", fieldName, " eq ", value, ")"));
		}

		return "(" + StringUtil.merge(predicateStrings, " or ") + ")";
	}

	private List<Map<String, Serializable>> _getValuesList(
			long companyId, ObjectDefinition objectDefinition,
			String filterString)
		throws Exception {

		return _objectEntryLocalService.getValuesList(
			0, companyId, _userLocalService.getGuestUserId(companyId),
			objectDefinition.getObjectDefinitionId(),
			_filterFactory.create(filterString, objectDefinition), null,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	private boolean _isMoreActive(
		ObjectDefinition requestObjectDefinition,
		Map<String, Serializable> candidateValues,
		Map<String, Serializable> currentValues) {

		boolean candidateOpen = _isOpen(candidateValues);

		if (candidateOpen != _isOpen(currentValues)) {
			return candidateOpen;
		}

		String pkObjectFieldName =
			requestObjectDefinition.getPKObjectFieldName();

		if (GetterUtil.getLong(candidateValues.get(pkObjectFieldName)) >
				GetterUtil.getLong(currentValues.get(pkObjectFieldName))) {

			return true;
		}

		return false;
	}

	private boolean _isOpen(Map<String, Serializable> values) {
		return !_terminalRequestStatuses.contains(
			GetterUtil.getString(values.get("requestStatus")));
	}

	private static final Set<String> _terminalRequestStatuses =
		SetUtil.fromArray("completed", "declined", "expired", "voided");

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