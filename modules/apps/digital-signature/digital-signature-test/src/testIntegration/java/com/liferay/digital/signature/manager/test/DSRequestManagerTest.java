/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.manager.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.manager.DSEnvelopeManager;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSDocument;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.digital.signature.model.DSRequestRecipient;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Brian I. Kim
 */
@RunWith(Arquillian.class)
public class DSRequestManagerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_configurationProvider.saveCompanyConfiguration(
			DigitalSignatureConfiguration.class, TestPropsValues.getCompanyId(),
			HashMapDictionaryBuilder.<String, Object>put(
				"accountBaseURI", RandomTestUtil.randomString()
			).put(
				"apiAccountId", RandomTestUtil.randomString()
			).put(
				"apiUsername", RandomTestUtil.randomString()
			).put(
				"enabled", true
			).put(
				"enableEmbeddedView", true
			).put(
				"environment", RandomTestUtil.randomString()
			).put(
				"integrationKey", RandomTestUtil.randomString()
			).put(
				"rsaPrivateKey", RandomTestUtil.randomString()
			).put(
				"siteSettingsStrategy", "always-inherit"
			).build());
	}

	@After
	public void tearDown() throws Exception {
		_configurationProvider.deleteCompanyConfiguration(
			DigitalSignatureConfiguration.class,
			TestPropsValues.getCompanyId());
	}

	@Test
	public void testAddDSRequest() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		long fileEntryId = RandomTestUtil.randomInt();

		DSEnvelope dsEnvelope = _getDSEnvelope(fileEntryId);

		_dsRequestManager.addDSRequest(
			companyId, TestPropsValues.getGroupId(),
			TestPropsValues.getUserId(), dsEnvelope, new long[] {fileEntryId});

		DSRequest dsRequest = _dsRequestManager.fetchDSRequest(
			companyId, fileEntryId);

		Assert.assertEquals(
			dsEnvelope.getEmailSubject(), dsRequest.getEmailSubject());
		Assert.assertEquals(
			dsEnvelope.getDSEnvelopeId(), dsRequest.getProviderRequestId());
		Assert.assertEquals("sent", dsRequest.getStatus());

		Set<String> expectedEmailAddresses = new HashSet<>();

		for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
			expectedEmailAddresses.add(dsRecipient.getEmailAddress());
		}

		Set<String> actualEmailAddresses = new HashSet<>();

		for (DSRequestRecipient dsRequestRecipient :
				dsRequest.getDSRequestRecipients()) {

			actualEmailAddresses.add(dsRequestRecipient.getEmailAddress());
		}

		Assert.assertEquals(expectedEmailAddresses, actualEmailAddresses);
	}

	@Test
	public void testFetchDSRequest() throws Exception {
		long companyId = TestPropsValues.getCompanyId();
		long userId = TestPropsValues.getUserId();

		long fileEntryId = RandomTestUtil.randomInt();

		_addDSRequestObjectEntries(
			companyId, userId, RandomTestUtil.randomString() + "@liferay.com",
			fileEntryId, "sent", "sent");

		DSRequest dsRequest = _dsRequestManager.fetchDSRequest(
			companyId, fileEntryId);

		Assert.assertEquals("sent", dsRequest.getStatus());
		Assert.assertFalse(dsRequest.isTerminal());

		List<DSRequestRecipient> dsRequestRecipients =
			dsRequest.getDSRequestRecipients();

		Assert.assertEquals(
			dsRequestRecipients.toString(), 1, dsRequestRecipients.size());

		DSRequestRecipient dsRequestRecipient1 = dsRequestRecipients.get(0);

		Assert.assertEquals("sent", dsRequestRecipient1.getStatus());
		Assert.assertEquals(userId, dsRequestRecipient1.getUserId());

		fileEntryId = RandomTestUtil.randomInt();

		_addDSRequestObjectEntries(
			companyId, userId, RandomTestUtil.randomString() + "@liferay.com",
			fileEntryId, "completed", "completed");

		dsRequest = _dsRequestManager.fetchDSRequest(companyId, fileEntryId);

		Assert.assertEquals("completed", dsRequest.getStatus());
		Assert.assertTrue(dsRequest.isTerminal());

		fileEntryId = RandomTestUtil.randomInt();

		DSEnvelope dsEnvelope = _getDSEnvelope(fileEntryId);

		List<DSRecipient> dsRecipients = dsEnvelope.getDSRecipients();

		DSRecipient dsRecipient1 = dsRecipients.get(0);
		DSRecipient dsRecipient2 = dsRecipients.get(1);

		dsRecipient1.setRoutingOrder(2);
		dsRecipient2.setRoutingOrder(1);

		_dsRequestManager.addDSRequest(
			companyId, TestPropsValues.getGroupId(), userId, dsEnvelope,
			new long[] {fileEntryId});

		dsRequest = _dsRequestManager.fetchDSRequest(companyId, fileEntryId);

		dsRequestRecipients = dsRequest.getDSRequestRecipients();

		DSRequestRecipient dsRequestRecipient2 = dsRequestRecipients.get(0);
		DSRequestRecipient dsRequestRecipient3 = dsRequestRecipients.get(1);

		Assert.assertEquals(
			dsRecipient2.getEmailAddress(),
			dsRequestRecipient2.getEmailAddress());
		Assert.assertEquals(1, dsRequestRecipient2.getSigningOrder());
		Assert.assertEquals(
			dsRecipient1.getEmailAddress(),
			dsRequestRecipient3.getEmailAddress());
		Assert.assertEquals(2, dsRequestRecipient3.getSigningOrder());
	}

	@Test
	public void testGetDSRequests() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		long fileEntryId1 = RandomTestUtil.randomInt();
		long fileEntryId2 = RandomTestUtil.randomInt();

		_dsRequestManager.addDSRequest(
			companyId, TestPropsValues.getGroupId(),
			TestPropsValues.getUserId(), _getDSEnvelope(fileEntryId1),
			new long[] {fileEntryId1, fileEntryId2});

		Map<Long, DSRequest> dsRequests = _dsRequestManager.getDSRequests(
			companyId, ListUtil.fromArray(fileEntryId1, fileEntryId2));

		Assert.assertEquals(dsRequests.toString(), 2, dsRequests.size());

		DSRequest dsRequest1 = dsRequests.get(fileEntryId1);
		DSRequest dsRequest2 = dsRequests.get(fileEntryId2);

		Assert.assertEquals(
			dsRequest1.getProviderRequestId(),
			dsRequest2.getProviderRequestId());

		dsRequests = _dsRequestManager.getDSRequests(
			companyId, Collections.singletonList(RandomTestUtil.randomLong()));

		Assert.assertTrue(dsRequests.isEmpty());
	}

	@Test
	public void testGetFileEntryDSRequests() throws Exception {
		long companyId = TestPropsValues.getCompanyId();
		long userId = TestPropsValues.getUserId();

		String emailAddress = RandomTestUtil.randomString() + "@liferay.com";
		long fileEntryId = RandomTestUtil.randomInt();

		_addDSRequestObjectEntries(
			companyId, userId, emailAddress, fileEntryId, "sent", "voided");
		_addDSRequestObjectEntries(
			companyId, userId, emailAddress, fileEntryId, "sent", "sent");

		List<DSRequest> dsRequests = _dsRequestManager.getFileEntryDSRequests(
			companyId, fileEntryId);

		Assert.assertEquals(dsRequests.toString(), 2, dsRequests.size());

		DSRequest dsRequest1 = dsRequests.get(0);
		DSRequest dsRequest2 = dsRequests.get(1);

		Assert.assertEquals("sent", dsRequest1.getStatus());
		Assert.assertEquals("voided", dsRequest2.getStatus());
	}

	@Test
	public void testIsSignatureRequired() throws Exception {
		long companyId = TestPropsValues.getCompanyId();
		long userId = TestPropsValues.getUserId();

		String emailAddress = RandomTestUtil.randomString() + "@liferay.com";
		long fileEntryId = RandomTestUtil.randomInt();

		_addDSRequestObjectEntries(
			companyId, userId, emailAddress, fileEntryId, "sent", "sent");

		DSRequest dsRequest = _dsRequestManager.fetchDSRequest(
			companyId, fileEntryId);

		Assert.assertTrue(dsRequest.isSignatureRequired(emailAddress));
		Assert.assertFalse(
			dsRequest.isSignatureRequired(RandomTestUtil.randomString()));

		fileEntryId = RandomTestUtil.randomInt();

		_addDSRequestObjectEntries(
			companyId, userId, emailAddress, fileEntryId, "created", "sent");

		dsRequest = _dsRequestManager.fetchDSRequest(companyId, fileEntryId);

		Assert.assertFalse(dsRequest.isSignatureRequired(emailAddress));

		fileEntryId = RandomTestUtil.randomInt();

		_addDSRequestObjectEntries(
			companyId, userId, emailAddress, fileEntryId, "completed",
			"completed");

		dsRequest = _dsRequestManager.fetchDSRequest(companyId, fileEntryId);

		Assert.assertFalse(dsRequest.isSignatureRequired(emailAddress));
	}

	@Test
	public void testUpdateDSRequest() throws Exception {
		LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(
			System.currentTimeMillis() / 1000, 0, ZoneOffset.UTC);

		_testUpdateDSRequest(
			"completed", localDateTime.plusDays(1), "completed", localDateTime);
		_testUpdateDSRequest("expired", localDateTime, "voided", localDateTime);
	}

	private void _addDSRequestObjectEntries(
			long companyId, long userId, String emailAddress, long fileEntryId,
			String recipientStatus, String requestStatus)
		throws Exception {

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

		String languageId = LocaleUtil.toLanguageId(
			LocaleUtil.getSiteDefault());

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				TestPropsValues.getGroupId(), userId);

		ObjectEntry dsRequestObjectEntry =
			_objectEntryLocalService.addObjectEntry(
				0, userId, dsRequestObjectDefinition.getObjectDefinitionId(), 0,
				languageId,
				HashMapBuilder.<String, Serializable>put(
					"emailSubject", RandomTestUtil.randomString()
				).put(
					"providerKey", "docusign"
				).put(
					"providerRequestId", RandomTestUtil.randomString()
				).put(
					"requestStatus", requestStatus
				).build(),
				serviceContext);

		_objectEntryLocalService.addObjectEntry(
			0, userId,
			dsRequestDocumentObjectDefinition.getObjectDefinitionId(), 0,
			languageId,
			HashMapBuilder.<String, Serializable>put(
				_getRelationshipFieldName(
					dsRequestObjectDefinition, "dsRequestToDSRequestDocuments"),
				dsRequestObjectEntry.getObjectEntryId()
			).put(
				"fileEntryId", fileEntryId
			).build(),
			serviceContext);

		_objectEntryLocalService.addObjectEntry(
			0, userId,
			dsRequestRecipientObjectDefinition.getObjectDefinitionId(), 0,
			languageId,
			HashMapBuilder.<String, Serializable>put(
				_getRelationshipFieldName(
					dsRequestObjectDefinition,
					"dsRequestToDSRequestRecipients"),
				dsRequestObjectEntry.getObjectEntryId()
			).put(
				"emailAddress", emailAddress
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"providerRecipientId", RandomTestUtil.randomString()
			).put(
				"r_userToDSRequestRecipients_userId", userId
			).put(
				"requestRecipientStatus", recipientStatus
			).build(),
			serviceContext);
	}

	private DSEnvelope _getDSEnvelope(long fileEntryId) {
		DSEnvelope dsEnvelope = new DSEnvelope();

		DSDocument dsDocument = new DSDocument();

		dsDocument.setDSDocumentId(String.valueOf(fileEntryId));

		dsEnvelope.setDSDocuments(ListUtil.fromArray(dsDocument));

		dsEnvelope.setDSEnvelopeId(RandomTestUtil.randomString());
		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(_getDSRecipient("1"), _getDSRecipient("2")));
		dsEnvelope.setEmailSubject(RandomTestUtil.randomString());
		dsEnvelope.setStatus("sent");

		return dsEnvelope;
	}

	private DSRecipient _getDSRecipient(String dsRecipientId) {
		DSRecipient dsRecipient = new DSRecipient();

		dsRecipient.setDSRecipientId(dsRecipientId);
		dsRecipient.setEmailAddress(
			RandomTestUtil.randomString() + "@liferay.com");
		dsRecipient.setName(RandomTestUtil.randomString());
		dsRecipient.setStatus("sent");

		return dsRecipient;
	}

	private String _getRelationshipFieldName(
			ObjectDefinition dsRequestObjectDefinition, String relationshipName)
		throws Exception {

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.fetchObjectRelationship(
				dsRequestObjectDefinition.getObjectDefinitionId(),
				relationshipName);

		ObjectField objectField = _objectFieldLocalService.getObjectField(
			objectRelationship.getObjectFieldId2());

		return objectField.getName();
	}

	private void _testUpdateDSRequest(
			String expectedStatus, LocalDateTime expireLocalDateTime,
			String status, LocalDateTime statusChangedLocalDateTime)
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		long fileEntryId = RandomTestUtil.randomLong();

		DSEnvelope dsEnvelope = _getDSEnvelope(fileEntryId);

		_dsRequestManager.addDSRequest(
			companyId, TestPropsValues.getGroupId(),
			TestPropsValues.getUserId(), dsEnvelope, new long[] {fileEntryId});

		for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
			dsRecipient.setStatus("completed");
			dsRecipient.setStatusLocalDateTime(statusChangedLocalDateTime);
		}

		dsEnvelope.setExpireLocalDateTime(expireLocalDateTime);
		dsEnvelope.setStatus(status);
		dsEnvelope.setStatusChangedLocalDateTime(statusChangedLocalDateTime);

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> dsEnvelope));

		try {
			_dsRequestManager.updateDSRequest(
				companyId, TestPropsValues.getGroupId(),
				dsEnvelope.getDSEnvelopeId());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		DSRequest dsRequest = _dsRequestManager.fetchDSRequest(
			companyId, fileEntryId);

		Assert.assertEquals(
			Date.from(expireLocalDateTime.toInstant(ZoneOffset.UTC)),
			dsRequest.getExpirationDate());
		Assert.assertEquals(expectedStatus, dsRequest.getStatus());
		Assert.assertEquals(
			Date.from(statusChangedLocalDateTime.toInstant(ZoneOffset.UTC)),
			dsRequest.getStatusDate());

		for (DSRequestRecipient dsRequestRecipient :
				dsRequest.getDSRequestRecipients()) {

			Assert.assertEquals("completed", dsRequestRecipient.getStatus());
			Assert.assertEquals(
				Date.from(statusChangedLocalDateTime.toInstant(ZoneOffset.UTC)),
				dsRequestRecipient.getStatusDate());
		}
	}

	@Inject
	private ConfigurationProvider _configurationProvider;

	@Inject
	private DSRequestManager _dsRequestManager;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private ObjectFieldLocalService _objectFieldLocalService;

	@Inject
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

}