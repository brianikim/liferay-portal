/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.provider;

import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.provider.DSActiveRequestResolver;
import com.liferay.digital.signature.provider.DSRequestDetail;
import com.liferay.digital.signature.provider.DSRequestDetailProvider;
import com.liferay.digital.signature.provider.DSRequestRecipientDetail;
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
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Kim
 */
@Component(service = DSRequestDetailProvider.class)
public class DSRequestDetailProviderImpl implements DSRequestDetailProvider {

	@Override
	public DSRequestDetail getRequestDetail(long companyId, long fileEntryId) {
		if (!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-69290")) {
			return null;
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				companyId, 0);

		if ((digitalSignatureConfiguration == null) ||
			!digitalSignatureConfiguration.enabled()) {

			return null;
		}

		ObjectDefinition requestObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);
		ObjectDefinition recipientObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_RECIPIENT", companyId);

		if ((requestObjectDefinition == null) ||
			(recipientObjectDefinition == null)) {

			return null;
		}

		try {

			// A document can have more than one signature request over time;
			// show the active one.

			Map<String, Serializable> requestValues =
				_dsActiveRequestResolver.getActiveRequestValuesByFileEntryId(
					companyId, Collections.singletonList(fileEntryId)
				).get(
					fileEntryId
				);

			if (requestValues == null) {
				return null;
			}

			long requestId = GetterUtil.getLong(
				requestValues.get(
					requestObjectDefinition.getPKObjectFieldName()));

			ObjectEntry requestObjectEntry =
				_objectEntryLocalService.fetchObjectEntry(requestId);

			return new DSRequestDetail(
				_toDate(requestValues.get("completionDate")),
				(requestObjectEntry == null) ? null :
					requestObjectEntry.getCreateDate(),
				GetterUtil.getString(requestValues.get("emailSubject")),
				GetterUtil.getString(requestValues.get("providerRequestId")),
				_getRecipientDetails(
					companyId, recipientObjectDefinition,
					requestObjectDefinition, requestId),
				_getRequesterEmailAddress(requestObjectEntry),
				_getRequesterName(requestObjectEntry),
				(requestObjectEntry == null) ? 0 :
					requestObjectEntry.getUserId(),
				GetterUtil.getString(requestValues.get("requestStatus")));
		}
		catch (Exception exception) {
			_log.error(
				"Unable to load the signature request detail for file entry " +
					fileEntryId,
				exception);

			return null;
		}
	}

	private List<DSRequestRecipientDetail> _getRecipientDetails(
			long companyId, ObjectDefinition recipientObjectDefinition,
			ObjectDefinition requestObjectDefinition, long requestId)
		throws Exception {

		List<DSRequestRecipientDetail> recipientDetails = new ArrayList<>();

		String fieldName = _getRelationshipFieldName(requestObjectDefinition);

		if (fieldName == null) {
			return recipientDetails;
		}

		for (Map<String, Serializable> values :
				_getValuesList(
					companyId, recipientObjectDefinition,
					StringBundler.concat(
						"(", fieldName, " eq '", requestId, "')"))) {

			ObjectEntry recipientObjectEntry =
				_objectEntryLocalService.fetchObjectEntry(
					GetterUtil.getLong(
						values.get(
							recipientObjectDefinition.getPKObjectFieldName())));

			recipientDetails.add(
				new DSRequestRecipientDetail(
					GetterUtil.getString(values.get("emailAddress")),
					GetterUtil.getString(values.get("name")),
					GetterUtil.getLong(values.get("recipientUserId")),
					GetterUtil.getString(values.get("requestRecipientStatus")),
					(recipientObjectEntry == null) ? null :
						recipientObjectEntry.getModifiedDate()));
		}

		return recipientDetails;
	}

	private String _getRelationshipFieldName(
			ObjectDefinition requestObjectDefinition)
		throws Exception {

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

	private String _getRequesterEmailAddress(ObjectEntry requestObjectEntry) {
		if (requestObjectEntry == null) {
			return null;
		}

		User user = _userLocalService.fetchUser(requestObjectEntry.getUserId());

		if (user == null) {
			return null;
		}

		return user.getEmailAddress();
	}

	private String _getRequesterName(ObjectEntry requestObjectEntry) {
		if (requestObjectEntry == null) {
			return null;
		}

		User user = _userLocalService.fetchUser(requestObjectEntry.getUserId());

		if (user != null) {
			return user.getFullName();
		}

		return requestObjectEntry.getUserName();
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

	private Date _toDate(Serializable value) {
		if (value instanceof Date) {
			return (Date)value;
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DSRequestDetailProviderImpl.class);

	@Reference
	private DSActiveRequestResolver _dsActiveRequestResolver;

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