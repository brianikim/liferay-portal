/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.manager;

import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.digital.signature.constants.DSRequestConstants;
import com.liferay.digital.signature.constants.DSRequestRecipientConstants;
import com.liferay.digital.signature.mail.DSEnvelopeEmailNotificationSender;
import com.liferay.digital.signature.manager.DSEnvelopeManager;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.document.library.kernel.model.DLVersionNumberIncrease;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.entry.util.ObjectEntryThreadLocal;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian I. Kim
 */
@Component(service = DSRequestManager.class)
public class DSRequestManagerImpl implements DSRequestManager {

	@Override
	public void addDSRequest(
			long companyId, long groupId, long userId, DSEnvelope dsEnvelope,
			long[] fileEntryIds)
		throws PortalException {

		if (!_isEnabled(companyId, groupId) || (dsEnvelope == null) ||
			(fileEntryIds == null)) {

			return;
		}

		ObjectDefinition dsRequestDocumentObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_DOCUMENT", companyId);
		ObjectDefinition dsRequestRecipientObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_RECIPIENT", companyId);
		ObjectDefinition dsRequestObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		if ((dsRequestDocumentObjectDefinition == null) ||
			(dsRequestRecipientObjectDefinition == null) ||
			(dsRequestObjectDefinition == null)) {

			return;
		}

		try {
			TransactionInvokerUtil.invoke(
				_transactionConfig,
				() -> {
					String documentFieldName = _getRelationshipFieldName(
						dsRequestObjectDefinition,
						"dsRequestToDSRequestDocuments");
					String recipientFieldName = _getRelationshipFieldName(
						dsRequestObjectDefinition,
						"dsRequestToDSRequestRecipients");

					if ((documentFieldName == null) ||
						(recipientFieldName == null)) {

						return null;
					}

					ServiceContext serviceContext = _getServiceContext(
						companyId, groupId, userId);

					ObjectEntry dsRequestObjectEntry =
						_objectEntryLocalService.addObjectEntry(
							0, userId,
							dsRequestObjectDefinition.getObjectDefinitionId(),
							ObjectEntryFolderConstants.
								PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
							null,
							HashMapBuilder.<String, Serializable>put(
								"emailSubject", dsEnvelope.getEmailSubject()
							).put(
								"providerKey", "docusign"
							).put(
								"providerRequestId",
								dsEnvelope.getDSEnvelopeId()
							).put(
								"requestExpirationDate",
								() -> _toDate(
									dsEnvelope.getExpireLocalDateTime())
							).put(
								"requestStatus", _getRequestStatus(dsEnvelope)
							).build(),
							serviceContext);

					for (long fileEntryId : fileEntryIds) {
						_objectEntryLocalService.addObjectEntry(
							0, userId,
							dsRequestDocumentObjectDefinition.
								getObjectDefinitionId(),
							ObjectEntryFolderConstants.
								PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
							null,
							HashMapBuilder.<String, Serializable>put(
								documentFieldName,
								dsRequestObjectEntry.getObjectEntryId()
							).put(
								"fileEntryId", fileEntryId
							).build(),
							serviceContext);
					}

					for (DSRecipient dsRecipient :
							dsEnvelope.getDSRecipients()) {

						_objectEntryLocalService.addObjectEntry(
							0, userId,
							dsRequestRecipientObjectDefinition.
								getObjectDefinitionId(),
							ObjectEntryFolderConstants.
								PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
							null,
							HashMapBuilder.<String, Serializable>put(
								recipientFieldName,
								dsRequestObjectEntry.getObjectEntryId()
							).put(
								"emailAddress", dsRecipient.getEmailAddress()
							).put(
								"name", dsRecipient.getName()
							).put(
								"providerRecipientId",
								dsRecipient.getDSRecipientId()
							).put(
								"r_userToDSRequestRecipients_userId",
								_getRecipientUserId(
									companyId, dsRecipient.getEmailAddress())
							).put(
								"requestRecipientStatus",
								_getRequestRecipientStatus(
									dsRecipient.getStatus())
							).put(
								"sentDate",
								() -> _toDate(
									dsRecipient.getSentLocalDateTime())
							).put(
								"signingOrder", dsRecipient.getRoutingOrder()
							).build(),
							serviceContext);
					}

					return null;
				});
		}
		catch (Throwable throwable) {
			try {
				_dsEnvelopeManager.voidDSEnvelope(
					companyId, groupId, dsEnvelope.getDSEnvelopeId(),
					"Unable to record the signature request");
			}
			catch (Exception exception) {
				throwable.addSuppressed(exception);
			}

			throw new PortalException(
				"Unable to record the signature request for envelope " +
					dsEnvelope.getDSEnvelopeId(),
				throwable);
		}
	}

	@Override
	public Map<Long, String> getRequestStatusesByFileEntryId(
		long companyId, Collection<Long> fileEntryIds) {

		Map<Long, String> requestStatusesByFileEntryId = new HashMap<>();

		if (!_isEnabled(companyId, 0) || (fileEntryIds == null) ||
			fileEntryIds.isEmpty()) {

			return requestStatusesByFileEntryId;
		}

		ObjectDefinition dsRequestDocumentObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_DOCUMENT", companyId);
		ObjectDefinition dsRequestObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		if ((dsRequestDocumentObjectDefinition == null) ||
			(dsRequestObjectDefinition == null)) {

			return requestStatusesByFileEntryId;
		}

		try {
			Map<Long, Long> requestIdsByFileEntryId =
				_getRequestIdsByFileEntryId(
					companyId, dsRequestDocumentObjectDefinition,
					dsRequestObjectDefinition, fileEntryIds);

			for (Map.Entry<Long, Long> entry :
					requestIdsByFileEntryId.entrySet()) {

				ObjectEntry dsRequestObjectEntry =
					_objectEntryLocalService.fetchObjectEntry(entry.getValue());

				if (dsRequestObjectEntry == null) {
					continue;
				}

				Map<String, Serializable> requestValues =
					dsRequestObjectEntry.getValues();

				requestStatusesByFileEntryId.put(
					entry.getKey(),
					GetterUtil.getString(requestValues.get("requestStatus")));
			}
		}
		catch (Exception exception) {
			_log.error(
				"Unable to load signature request statuses for company " +
					companyId,
				exception);
		}

		return requestStatusesByFileEntryId;
	}

	@Override
	public void resendDSRequestNotifications(
		long companyId, long groupId, String providerRequestId) {

		if (!_isEnabled(companyId, groupId) ||
			Validator.isNull(providerRequestId)) {

			return;
		}

		ObjectDefinition dsRequestRecipientObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_RECIPIENT", companyId);
		ObjectDefinition dsRequestObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		if ((dsRequestRecipientObjectDefinition == null) ||
			(dsRequestObjectDefinition == null)) {

			return;
		}

		try {
			String recipientFieldName = _getRelationshipFieldName(
				dsRequestObjectDefinition, "dsRequestToDSRequestRecipients");

			if (recipientFieldName == null) {
				return;
			}

			for (Map<String, Serializable> requestValues :
					_getValuesList(
						companyId,
						StringBundler.concat(
							"(providerRequestId eq '", providerRequestId, "')"),
						dsRequestObjectDefinition, null)) {

				long requestId = GetterUtil.getLong(
					requestValues.get(
						dsRequestObjectDefinition.getPKObjectFieldName()));

				String emailSubject = GetterUtil.getString(
					requestValues.get("emailSubject"));

				for (Map<String, Serializable> recipientValues :
						_getValuesList(
							companyId,
							StringBundler.concat(
								"(", recipientFieldName, " eq '", requestId,
								"') and (requestRecipientStatus eq 'sent')"),
							dsRequestRecipientObjectDefinition, null)) {

					String emailAddress = GetterUtil.getString(
						recipientValues.get("emailAddress"));

					if (Validator.isNull(emailAddress)) {
						continue;
					}

					DSRecipient dsRecipient = new DSRecipient();

					dsRecipient.setEmailAddress(emailAddress);

					_dsEnvelopeEmailNotificationSender.sendNotification(
						companyId, groupId, providerRequestId, dsRecipient,
						emailSubject, null);
				}
			}
		}
		catch (Exception exception) {
			_log.error(
				"Unable to resend the signature request for envelope " +
					providerRequestId,
				exception);
		}
	}

	@Override
	public int sendSignatureReminders(long companyId) {
		if (!_isEnabled(companyId, 0)) {
			return 0;
		}

		DigitalSignatureConfiguration digitalSignatureConfiguration =
			DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(
				companyId, 0);

		if (!digitalSignatureConfiguration.signatureReminderEnabled()) {
			return 0;
		}

		ObjectDefinition dsRequestRecipientObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_RECIPIENT", companyId);
		ObjectDefinition dsRequestObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		if ((dsRequestRecipientObjectDefinition == null) ||
			(dsRequestObjectDefinition == null)) {

			return 0;
		}

		int count = 0;

		try {
			String recipientFieldName = _getRelationshipFieldName(
				dsRequestObjectDefinition, "dsRequestToDSRequestRecipients");

			if (recipientFieldName == null) {
				return 0;
			}

			for (Map<String, Serializable> recipientValues :
					_getValuesList(
						companyId, "(requestRecipientStatus eq 'sent')",
						dsRequestRecipientObjectDefinition, null)) {

				ObjectEntry dsRequestObjectEntry =
					_objectEntryLocalService.fetchObjectEntry(
						GetterUtil.getLong(
							recipientValues.get(recipientFieldName)));

				if (dsRequestObjectEntry == null) {
					continue;
				}

				Map<String, Serializable> requestValues =
					dsRequestObjectEntry.getValues();

				if (ArrayUtil.contains(
						_TERMINAL_REQUEST_STATUSES,
						GetterUtil.getString(
							requestValues.get("requestStatus")))) {

					continue;
				}

				String emailAddress = GetterUtil.getString(
					recipientValues.get("emailAddress"));

				if (Validator.isNull(emailAddress)) {
					continue;
				}

				DSRecipient dsRecipient = new DSRecipient();

				dsRecipient.setEmailAddress(emailAddress);

				_dsEnvelopeEmailNotificationSender.sendNotification(
					companyId, 0,
					GetterUtil.getString(
						requestValues.get("providerRequestId")),
					dsRecipient,
					GetterUtil.getString(requestValues.get("emailSubject")),
					null);

				count++;
			}
		}
		catch (Exception exception) {
			_log.error(
				"Unable to send signature reminders for company " + companyId,
				exception);
		}

		return count;
	}

	@Override
	public void updateDSRequest(
		long companyId, long groupId, String providerRequestId) {

		if (!_isEnabled(companyId, groupId) ||
			Validator.isNull(providerRequestId)) {

			return;
		}

		ObjectDefinition dsRequestDocumentObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_DOCUMENT", companyId);
		ObjectDefinition dsRequestRecipientObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_RECIPIENT", companyId);
		ObjectDefinition dsRequestObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		if ((dsRequestDocumentObjectDefinition == null) ||
			(dsRequestRecipientObjectDefinition == null) ||
			(dsRequestObjectDefinition == null)) {

			return;
		}

		try {
			DSEnvelope dsEnvelope = _dsEnvelopeManager.getDSEnvelope(
				companyId, groupId, providerRequestId);

			if (dsEnvelope == null) {
				return;
			}

			String recipientFieldName = _getRelationshipFieldName(
				dsRequestObjectDefinition, "dsRequestToDSRequestRecipients");

			if (recipientFieldName == null) {
				return;
			}

			Map<String, DSRecipient> dsRecipients = new HashMap<>();

			for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
				dsRecipients.put(dsRecipient.getDSRecipientId(), dsRecipient);
			}

			for (Map<String, Serializable> requestValues :
					_getValuesList(
						companyId,
						StringBundler.concat(
							"(providerRequestId eq '", providerRequestId, "')"),
						dsRequestObjectDefinition, null)) {

				long requestId = GetterUtil.getLong(
					requestValues.get(
						dsRequestObjectDefinition.getPKObjectFieldName()));

				_updateRequestStatus(companyId, groupId, dsEnvelope, requestId);

				_updateRecipientStatuses(
					companyId, groupId, dsRecipients,
					GetterUtil.getString(requestValues.get("emailSubject")),
					dsRequestRecipientObjectDefinition, providerRequestId,
					recipientFieldName, requestId);

				if (Objects.equals(
						_getRequestStatus(dsEnvelope), "completed") &&
					!Objects.equals(
						GetterUtil.getString(
							requestValues.get("requestStatus")),
						"completed")) {

					_archiveSignedDocument(
						companyId, groupId, dsEnvelope,
						dsRequestDocumentObjectDefinition,
						dsRequestObjectDefinition, requestId);
				}
			}
		}
		catch (Exception exception) {
			_log.error(
				"Unable to sync the signature request for envelope " +
					providerRequestId,
				exception);
		}
	}

	@Override
	public void voidDSRequest(
		long companyId, long groupId, String providerRequestId, String reason) {

		if (!_isEnabled(companyId, groupId) ||
			Validator.isNull(providerRequestId)) {

			return;
		}

		_dsEnvelopeManager.voidDSEnvelope(
			companyId, groupId, providerRequestId, reason);

		updateDSRequest(companyId, groupId, providerRequestId);
	}

	private void _archiveSignedDocument(
			long companyId, long groupId, DSEnvelope dsEnvelope,
			ObjectDefinition dsRequestDocumentObjectDefinition,
			ObjectDefinition dsRequestObjectDefinition, long requestId)
		throws Exception {

		List<Long> fileEntryIds = _getRequestFileEntryIds(
			companyId, dsRequestDocumentObjectDefinition,
			dsRequestObjectDefinition, requestId);

		if (fileEntryIds.isEmpty()) {
			return;
		}

		ObjectEntry dsRequestObjectEntry =
			_objectEntryLocalService.fetchObjectEntry(requestId);

		if (dsRequestObjectEntry == null) {
			return;
		}

		long userId = dsRequestObjectEntry.getUserId();

		for (long fileEntryId : fileEntryIds) {
			byte[] bytes = _dsEnvelopeManager.getSignedDocument(
				companyId, groupId, dsEnvelope.getDSEnvelopeId(),
				String.valueOf(fileEntryId));

			if (ArrayUtil.isEmpty(bytes)) {
				continue;
			}

			FileEntry fileEntry = _dlAppLocalService.getFileEntry(fileEntryId);

			_dlAppLocalService.updateFileEntry(
				userId, fileEntryId, fileEntry.getFileName(),
				ContentTypes.APPLICATION_PDF, fileEntry.getTitle(), null, null,
				null, DLVersionNumberIncrease.MAJOR, bytes, null, null, null,
				_getServiceContext(companyId, fileEntry.getGroupId(), userId));
		}
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
			ObjectDefinition objectDefinition, String relationshipName)
		throws Exception {

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.fetchObjectRelationship(
				objectDefinition.getObjectDefinitionId(), relationshipName);

		if (objectRelationship == null) {
			return null;
		}

		ObjectField objectField = _objectFieldLocalService.getObjectField(
			objectRelationship.getObjectFieldId2());

		return objectField.getName();
	}

	private List<Long> _getRequestFileEntryIds(
			long companyId, ObjectDefinition dsRequestDocumentObjectDefinition,
			ObjectDefinition dsRequestObjectDefinition, long requestId)
		throws Exception {

		String documentFieldName = _getRelationshipFieldName(
			dsRequestObjectDefinition, "dsRequestToDSRequestDocuments");

		if (documentFieldName == null) {
			return Collections.emptyList();
		}

		return TransformUtil.transform(
			_getValuesList(
				companyId,
				StringBundler.concat(
					"(", documentFieldName, " eq '", requestId, "')"),
				dsRequestDocumentObjectDefinition, null),
			documentValues -> GetterUtil.getLong(
				documentValues.get("fileEntryId")));
	}

	private Map<Long, Long> _getRequestIdsByFileEntryId(
			long companyId, ObjectDefinition dsRequestDocumentObjectDefinition,
			ObjectDefinition dsRequestObjectDefinition,
			Collection<Long> fileEntryIds)
		throws Exception {

		Map<Long, Long> requestIdsByFileEntryId = new HashMap<>();

		String documentFieldName = _getRelationshipFieldName(
			dsRequestObjectDefinition, "dsRequestToDSRequestDocuments");

		if (documentFieldName == null) {
			return requestIdsByFileEntryId;
		}

		for (Map<String, Serializable> documentValues :
				_getValuesList(
					companyId,
					StringBundler.concat(
						"(fileEntryId in (",
						StringUtil.merge(fileEntryIds, ", "), "))"),
					dsRequestDocumentObjectDefinition,
					new Sort[] {
						new Sort(Field.CREATE_DATE, Sort.LONG_TYPE, true)
					})) {

			requestIdsByFileEntryId.putIfAbsent(
				GetterUtil.getLong(documentValues.get("fileEntryId")),
				GetterUtil.getLong(documentValues.get(documentFieldName)));
		}

		return requestIdsByFileEntryId;
	}

	private String _getRequestRecipientStatus(String status) {
		status = StringUtil.toLowerCase(GetterUtil.getString(status));

		if (ArrayUtil.contains(DSRequestRecipientConstants.STATUSES, status)) {
			return status;
		}

		return DSRequestRecipientConstants.STATUS_SENT;
	}

	private String _getRequestStatus(DSEnvelope dsEnvelope) {
		String status = StringUtil.toLowerCase(
			GetterUtil.getString(dsEnvelope.getStatus()));

		if (Objects.equals(status, DSRequestConstants.STATUS_VOIDED)) {
			LocalDateTime expireLocalDateTime =
				dsEnvelope.getExpireLocalDateTime();
			LocalDateTime statusChangedLocalDateTime =
				dsEnvelope.getStatusChangedLocalDateTime();

			if ((expireLocalDateTime != null) &&
				(statusChangedLocalDateTime != null) &&
				!statusChangedLocalDateTime.isBefore(expireLocalDateTime)) {

				return DSRequestConstants.STATUS_EXPIRED;
			}
		}

		if (ArrayUtil.contains(DSRequestConstants.STATUSES, status)) {
			return status;
		}

		return DSRequestConstants.STATUS_SENT;
	}

	private String _getRequesterEmailAddress(ObjectEntry dsRequestObjectEntry) {
		if (dsRequestObjectEntry == null) {
			return null;
		}

		User user = _userLocalService.fetchUser(
			dsRequestObjectEntry.getUserId());

		if (user == null) {
			return null;
		}

		return user.getEmailAddress();
	}

	private String _getRequesterName(ObjectEntry dsRequestObjectEntry) {
		if (dsRequestObjectEntry == null) {
			return null;
		}

		User user = _userLocalService.fetchUser(
			dsRequestObjectEntry.getUserId());

		if (user == null) {
			return dsRequestObjectEntry.getUserName();
		}

		return user.getFullName();
	}

	private ServiceContext _getServiceContext(
		long companyId, long groupId, long userId) {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(companyId);
		serviceContext.setScopeGroupId(groupId);
		serviceContext.setUserId(userId);

		return serviceContext;
	}

	private List<Map<String, Serializable>> _getValuesList(
			long companyId, String filterString,
			ObjectDefinition objectDefinition, Sort[] sorts)
		throws Exception {

		boolean skipObjectEntryResourcePermission =
			ObjectEntryThreadLocal.isSkipObjectEntryResourcePermission();

		ObjectEntryThreadLocal.setSkipObjectEntryResourcePermission(true);

		try {
			return _objectEntryLocalService.getValuesList(
				0, companyId, objectDefinition.getUserId(),
				objectDefinition.getObjectDefinitionId(),
				_filterFactory.create(filterString, objectDefinition), null,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS, sorts);
		}
		finally {
			ObjectEntryThreadLocal.setSkipObjectEntryResourcePermission(
				skipObjectEntryResourcePermission);
		}
	}

	private boolean _isEnabled(long companyId, long groupId) {
		return DigitalSignatureConfigurationUtil.isEmbeddedViewEnabled(
			companyId, groupId);
	}

	private Date _toDate(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}

		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

	private void _updateRecipientStatuses(
			long companyId, long groupId, Map<String, DSRecipient> dsRecipients,
			String emailSubject, ObjectDefinition objectDefinition,
			String providerRequestId, String recipientFieldName, long requestId)
		throws Exception {

		for (Map<String, Serializable> recipientValues :
				_getValuesList(
					companyId,
					StringBundler.concat(
						"(", recipientFieldName, " eq '", requestId, "')"),
					objectDefinition, null)) {

			DSRecipient dsRecipient = dsRecipients.get(
				GetterUtil.getString(
					recipientValues.get("providerRecipientId")));

			if (dsRecipient == null) {
				continue;
			}

			long recipientId = GetterUtil.getLong(
				recipientValues.get(objectDefinition.getPKObjectFieldName()));

			ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
				recipientId);

			if (objectEntry == null) {
				continue;
			}

			String requestRecipientStatus = _getRequestRecipientStatus(
				dsRecipient.getStatus());

			Map<String, Serializable> values =
				HashMapBuilder.<String, Serializable>putAll(
					objectEntry.getValues()
				).put(
					"requestRecipientStatus", requestRecipientStatus
				).put(
					"requestRecipientStatusDate",
					() -> _toDate(dsRecipient.getStatusLocalDateTime())
				).put(
					"sentDate",
					() -> _toDate(dsRecipient.getSentLocalDateTime())
				).build();

			_objectEntryLocalService.updateObjectEntry(
				objectEntry.getUserId(), recipientId,
				ObjectEntryFolderConstants.
					PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
				values,
				_getServiceContext(
					companyId, groupId, objectEntry.getUserId()));

			if (Objects.equals(
					GetterUtil.getString(
						recipientValues.get("requestRecipientStatus")),
					"created") &&
				Objects.equals(requestRecipientStatus, "sent") &&
				Validator.isNotNull(dsRecipient.getDSClientUserId())) {

				_dsEnvelopeEmailNotificationSender.sendNotification(
					companyId, groupId, providerRequestId, dsRecipient,
					emailSubject, null);
			}
		}
	}

	private void _updateRequestStatus(
			long companyId, long groupId, DSEnvelope dsEnvelope, long requestId)
		throws Exception {

		ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
			requestId);

		if (objectEntry == null) {
			return;
		}

		Map<String, Serializable> values =
			HashMapBuilder.<String, Serializable>putAll(
				objectEntry.getValues()
			).put(
				"requestExpirationDate",
				() -> _toDate(dsEnvelope.getExpireLocalDateTime())
			).put(
				"requestStatus", _getRequestStatus(dsEnvelope)
			).put(
				"requestStatusDate",
				() -> _toDate(dsEnvelope.getStatusChangedLocalDateTime())
			).build();

		_objectEntryLocalService.updateObjectEntry(
			objectEntry.getUserId(), requestId,
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			values,
			_getServiceContext(companyId, groupId, objectEntry.getUserId()));
	}

	private static final String[] _TERMINAL_REQUEST_STATUSES = {
		"completed", "declined", "voided"
	};

	private static final Log _log = LogFactoryUtil.getLog(
		DSRequestManagerImpl.class);

	private static final TransactionConfig _transactionConfig =
		TransactionConfig.Factory.create(
			Propagation.REQUIRED, new Class<?>[] {Exception.class});

	@Reference
	private DLAppLocalService _dlAppLocalService;

	@Reference
	private DSEnvelopeEmailNotificationSender
		_dsEnvelopeEmailNotificationSender;

	@Reference
	private DSEnvelopeManager _dsEnvelopeManager;

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