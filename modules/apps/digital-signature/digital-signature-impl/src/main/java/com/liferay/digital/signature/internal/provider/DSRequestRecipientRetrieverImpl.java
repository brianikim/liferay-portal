/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.provider;

import com.liferay.digital.signature.provider.DSRequestRecipientRetriever;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Kim
 */
@Component(service = DSRequestRecipientRetriever.class)
public class DSRequestRecipientRetrieverImpl
	implements DSRequestRecipientRetriever {

	@Override
	public long getFileEntryId(long companyId, long recipientObjectEntryId)
		throws PortalException {

		ObjectEntry recipientObjectEntry =
			_objectEntryLocalService.fetchObjectEntry(recipientObjectEntryId);

		if (recipientObjectEntry == null) {
			return 0;
		}

		ObjectDefinition requestObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		if (requestObjectDefinition == null) {
			return 0;
		}

		String fieldName = _getRelationshipFieldName(requestObjectDefinition);

		if (fieldName == null) {
			return 0;
		}

		Map<String, Serializable> values = recipientObjectEntry.getValues();

		long requestId = GetterUtil.getLong(values.get(fieldName));

		if (requestId <= 0) {
			return 0;
		}

		ObjectEntry requestObjectEntry =
			_objectEntryLocalService.fetchObjectEntry(requestId);

		if (requestObjectEntry == null) {
			return 0;
		}

		Map<String, Serializable> requestValues =
			requestObjectEntry.getValues();

		return GetterUtil.getLong(requestValues.get("fileEntryId"));
	}

	@Override
	public int getPendingDocumentCount(
		long companyId, long userId, String... statusKeys) {

		ObjectDefinition recipientObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_RECIPIENT", companyId);
		ObjectDefinition requestObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		if ((recipientObjectDefinition == null) ||
			(requestObjectDefinition == null)) {

			return 0;
		}

		try {
			String fieldName = _getRelationshipFieldName(
				requestObjectDefinition);

			if (fieldName == null) {
				return 0;
			}

			Set<Long> requestIds = new HashSet<>();

			for (Map<String, Serializable> values :
					_getValuesList(
						companyId, recipientObjectDefinition,
						StringUtil.merge(
							new String[] {
								"(recipientUserId eq " + userId + ")",
								_getStatusPredicateString(
									"requestRecipientStatus", statusKeys)
							},
							" and "))) {

				requestIds.add(GetterUtil.getLong(values.get(fieldName)));
			}

			return requestIds.size();
		}
		catch (Exception exception) {
			_log.error(
				"Unable to count documents awaiting the signature of user " +
					userId,
				exception);

			return 0;
		}
	}

	@Override
	public Map<Long, Map<Long, String>> getRecipientStatusesByFileEntryId(
		long companyId, Collection<Long> fileEntryIds) {

		if ((fileEntryIds == null) || fileEntryIds.isEmpty()) {
			return Collections.emptyMap();
		}

		ObjectDefinition recipientObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_RECIPIENT", companyId);
		ObjectDefinition requestObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		if ((recipientObjectDefinition == null) ||
			(requestObjectDefinition == null)) {

			return Collections.emptyMap();
		}

		try {
			String fieldName = _getRelationshipFieldName(
				requestObjectDefinition);

			if (fieldName == null) {
				return Collections.emptyMap();
			}

			Map<Long, Long> fileEntryIdsByRequestId = new HashMap<>();

			for (Map<String, Serializable> values :
					_getValuesList(
						companyId, requestObjectDefinition,
						_getOrPredicateString(
							"fileEntryId", fileEntryIds, false))) {

				fileEntryIdsByRequestId.put(
					GetterUtil.getLong(
						values.get(
							requestObjectDefinition.getPKObjectFieldName())),
					GetterUtil.getLong(values.get("fileEntryId")));
			}

			if (fileEntryIdsByRequestId.isEmpty()) {
				return Collections.emptyMap();
			}

			Map<Long, Map<Long, String>> recipientStatusesByFileEntryId =
				new HashMap<>();

			for (Map<String, Serializable> values :
					_getValuesList(
						companyId, recipientObjectDefinition,
						_getOrPredicateString(
							fieldName, fileEntryIdsByRequestId.keySet(),
							true))) {

				Long fileEntryId = fileEntryIdsByRequestId.get(
					GetterUtil.getLong(values.get(fieldName)));

				if (fileEntryId == null) {
					continue;
				}

				Map<Long, String> statusesByUserId =
					recipientStatusesByFileEntryId.computeIfAbsent(
						fileEntryId, key -> new HashMap<>());

				statusesByUserId.put(
					GetterUtil.getLong(values.get("recipientUserId")),
					GetterUtil.getString(values.get("requestRecipientStatus")));
			}

			return recipientStatusesByFileEntryId;
		}
		catch (Exception exception) {
			_log.error(
				"Unable to load signature recipient statuses for company " +
					companyId,
				exception);

			return Collections.emptyMap();
		}
	}

	@Override
	public Map<Long, Set<Long>> getUserIdsByFileEntryId(
		long companyId, Collection<Long> fileEntryIds, String... statusKeys) {

		if ((fileEntryIds == null) || fileEntryIds.isEmpty()) {
			return Collections.emptyMap();
		}

		ObjectDefinition recipientObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_RECIPIENT", companyId);
		ObjectDefinition requestObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		if ((recipientObjectDefinition == null) ||
			(requestObjectDefinition == null)) {

			return Collections.emptyMap();
		}

		try {
			String fieldName = _getRelationshipFieldName(
				requestObjectDefinition);

			if (fieldName == null) {
				return Collections.emptyMap();
			}

			Map<Long, Long> fileEntryIdsByRequestId = new HashMap<>();

			for (Map<String, Serializable> values :
					_getValuesList(
						companyId, requestObjectDefinition,
						_getOrPredicateString(
							"fileEntryId", fileEntryIds, false))) {

				fileEntryIdsByRequestId.put(
					GetterUtil.getLong(
						values.get(
							requestObjectDefinition.getPKObjectFieldName())),
					GetterUtil.getLong(values.get("fileEntryId")));
			}

			if (fileEntryIdsByRequestId.isEmpty()) {
				return Collections.emptyMap();
			}

			Map<Long, Set<Long>> userIdsByFileEntryId = new HashMap<>();

			for (Map<String, Serializable> values :
					_getValuesList(
						companyId, recipientObjectDefinition,
						StringUtil.merge(
							new String[] {
								_getOrPredicateString(
									fieldName, fileEntryIdsByRequestId.keySet(),
									true),
								_getStatusPredicateString(
									"requestRecipientStatus", statusKeys)
							},
							" and "))) {

				Long fileEntryId = fileEntryIdsByRequestId.get(
					GetterUtil.getLong(values.get(fieldName)));

				if (fileEntryId == null) {
					continue;
				}

				Set<Long> userIds = userIdsByFileEntryId.computeIfAbsent(
					fileEntryId, key -> new HashSet<>());

				userIds.add(GetterUtil.getLong(values.get("recipientUserId")));
			}

			return userIdsByFileEntryId;
		}
		catch (Exception exception) {
			_log.error(
				"Unable to load signature recipients for company " + companyId,
				exception);

			return Collections.emptyMap();
		}
	}

	private String _getOrPredicateString(
		String fieldName, Collection<Long> values, boolean quote) {

		List<String> predicateStrings = new ArrayList<>(values.size());

		for (Long value : values) {
			if (quote) {
				predicateStrings.add(
					StringBundler.concat("(", fieldName, " eq '", value, "')"));
			}
			else {
				predicateStrings.add(
					StringBundler.concat("(", fieldName, " eq ", value, ")"));
			}
		}

		return "(" + StringUtil.merge(predicateStrings, " or ") + ")";
	}

	private String _getRelationshipFieldName(
			ObjectDefinition requestObjectDefinition)
		throws PortalException {

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.fetchObjectRelationship(
				requestObjectDefinition.getObjectDefinitionId(),
				"dsRequestToDSRequestRecipients");

		if (objectRelationship == null) {
			return null;
		}

		ObjectField objectField = _objectFieldLocalService.getObjectField(
			objectRelationship.getObjectFieldId2());

		return objectField.getName();
	}

	private String _getStatusPredicateString(
		String fieldName, String... statusKeys) {

		List<String> predicateStrings = new ArrayList<>(statusKeys.length);

		for (String statusKey : statusKeys) {
			predicateStrings.add(
				StringBundler.concat("(", fieldName, " eq '", statusKey, "')"));
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

	private static final Log _log = LogFactoryUtil.getLog(
		DSRequestRecipientRetrieverImpl.class);

	@Reference(
		target = "(filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT + ")"
	)
	private FilterFactory<Predicate> _filterFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

	@Reference
	private UserLocalService _userLocalService;

}