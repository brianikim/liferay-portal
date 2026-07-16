/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.request;

import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.model.DSDocument;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.digital.signature.request.DSRequestManager;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Kim
 */
@Component(service = DSRequestManager.class)
public class DSRequestManagerImpl implements DSRequestManager {

	@Override
	public void addDSRequests(
		long companyId, long groupId, long userId, DSEnvelope dsEnvelope) {

		if (!_isEnabled(companyId) || (dsEnvelope == null)) {
			return;
		}

		ObjectDefinition requestObjectDefinition = _fetchObjectDefinition(
			companyId, "L_DS_REQUEST");
		ObjectDefinition recipientObjectDefinition = _fetchObjectDefinition(
			companyId, "L_DS_REQUEST_RECIPIENT");

		if ((requestObjectDefinition == null) ||
			(recipientObjectDefinition == null)) {

			return;
		}

		try {
			String fieldName = _getRelationshipFieldName(
				requestObjectDefinition);

			if (fieldName == null) {
				return;
			}

			ServiceContext serviceContext = _createServiceContext(
				companyId, groupId, userId);

			String languageId = LocaleUtil.toLanguageId(
				LocaleUtil.getSiteDefault());

			for (DSDocument dsDocument : dsEnvelope.getDSDocuments()) {
				ObjectEntry requestObjectEntry =
					_objectEntryLocalService.addObjectEntry(
						0, userId,
						requestObjectDefinition.getObjectDefinitionId(), 0,
						languageId,
						HashMapBuilder.<String, Serializable>put(
							"emailSubject", dsEnvelope.getEmailSubject()
						).put(
							"fileEntryId",
							GetterUtil.getLong(dsDocument.getDSDocumentId())
						).put(
							"providerKey", _PROVIDER_KEY
						).put(
							"providerRequestId", dsEnvelope.getDSEnvelopeId()
						).put(
							"requestStatus",
							_toRequestStatus(dsEnvelope.getStatus())
						).build(),
						serviceContext);

				for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
					_objectEntryLocalService.addObjectEntry(
						0, userId,
						recipientObjectDefinition.getObjectDefinitionId(), 0,
						languageId,
						HashMapBuilder.<String, Serializable>put(
							fieldName, requestObjectEntry.getObjectEntryId()
						).put(
							"emailAddress", dsRecipient.getEmailAddress()
						).put(
							"name", dsRecipient.getName()
						).put(
							"providerRecipientId",
							dsRecipient.getDSRecipientId()
						).put(
							"recipientUserId",
							_getRecipientUserId(
								companyId, dsRecipient.getEmailAddress())
						).put(
							"requestRecipientStatus",
							_toRecipientStatus(dsRecipient.getStatus())
						).build(),
						serviceContext);
				}
			}
		}
		catch (Exception exception) {
			_log.error(
				"Unable to record the signature request for envelope " +
					dsEnvelope.getDSEnvelopeId(),
				exception);
		}
	}

	private ServiceContext _createServiceContext(
		long companyId, long groupId, long userId) {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(companyId);
		serviceContext.setScopeGroupId(groupId);
		serviceContext.setUserId(userId);

		return serviceContext;
	}

	private ObjectDefinition _fetchObjectDefinition(
		long companyId, String externalReferenceCode) {

		return _objectDefinitionLocalService.
			fetchObjectDefinitionByExternalReferenceCode(
				externalReferenceCode, companyId);
	}

	private long _getRecipientUserId(long companyId, String emailAddress) {
		if (Validator.isNull(emailAddress)) {
			return 0;
		}

		User user = _userLocalService.fetchUserByEmailAddress(
			companyId, emailAddress);

		if (user == null) {
			return 0;
		}

		return user.getUserId();
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

	private boolean _isEnabled(long companyId) {
		if (!FeatureFlagManagerUtil.isEnabled(companyId, "LPD-69290")) {
			return false;
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				companyId, 0);

		if ((digitalSignatureConfiguration == null) ||
			!digitalSignatureConfiguration.enabled()) {

			return false;
		}

		return true;
	}

	private String _toRecipientStatus(String status) {
		if (Validator.isNull(status)) {
			return "sent";
		}

		if (Objects.equals(status, "created")) {
			return "draft";
		}

		if (Objects.equals(status, "delivered")) {
			return "viewed";
		}

		if (Objects.equals(status, "signed") ||
			Objects.equals(status, "completed") ||
			Objects.equals(status, "declined") ||
			Objects.equals(status, "sent") ||
			Objects.equals(status, "viewed") ||
			Objects.equals(status, "draft")) {

			return status;
		}

		return "sent";
	}

	private String _toRequestStatus(String status) {
		if (Validator.isNull(status)) {
			return "sent";
		}

		if (Objects.equals(status, "created")) {
			return "draft";
		}

		if (Objects.equals(status, "delivered")) {
			return "viewed";
		}

		if (Objects.equals(status, "completed") ||
			Objects.equals(status, "declined") ||
			Objects.equals(status, "voided") ||
			Objects.equals(status, "expired") ||
			Objects.equals(status, "sent") ||
			Objects.equals(status, "viewed") ||
			Objects.equals(status, "draft")) {

			return status;
		}

		return "sent";
	}

	private static final String _PROVIDER_KEY = "docusign";

	private static final Log _log = LogFactoryUtil.getLog(
		DSRequestManagerImpl.class);

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