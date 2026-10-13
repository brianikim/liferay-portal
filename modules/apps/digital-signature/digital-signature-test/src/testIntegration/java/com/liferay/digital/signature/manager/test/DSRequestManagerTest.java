/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.manager.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.test.util.BatchEngineTestUtil;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.constants.DSRequestConstants;
import com.liferay.digital.signature.manager.DSEnvelopeManager;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.mail.kernel.model.MailMessage;
import com.liferay.mail.kernel.service.MailService;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.exception.ObjectEntryValuesException;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.configuration.test.util.GroupConfigurationTemporarySwapper;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepository;
import com.liferay.portal.kernel.repository.model.FileVersion;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.constants.TestDataConstants;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
@FeatureFlag("LPD-69290")
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
		String prefix = ".com.liferay.digital.signature.internal.batch.";

		BatchEngineTestUtil.processBatchEngineUnits(
			"com.liferay.digital.signature.impl", DSRequestManagerTest.class,
			new String[] {prefix + "01.object.definition"});

		_companyConfigurationTemporarySwapper =
			new CompanyConfigurationTemporarySwapper(
				TestPropsValues.getCompanyId(),
				DigitalSignatureConfiguration.class.getName(),
				HashMapDictionaryBuilder.<String, Object>put(
					"accountBaseURI", RandomTestUtil.randomString()
				).put(
					"apiAccountId", RandomTestUtil.randomString()
				).put(
					"apiUsername", RandomTestUtil.randomString()
				).put(
					"embeddedViewEnabled", true
				).put(
					"enabled", true
				).put(
					"environment", RandomTestUtil.randomString()
				).put(
					"integrationKey", RandomTestUtil.randomString()
				).put(
					"rsaPrivateKey", RandomTestUtil.randomString()
				).put(
					"siteSettingsStrategy", "always-inherit"
				).build());

		_dsRequestDocumentObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_DOCUMENT", TestPropsValues.getCompanyId());
		_dsRequestObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", TestPropsValues.getCompanyId());
		_dsRequestRecipientObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_RECIPIENT", TestPropsValues.getCompanyId());
	}

	@After
	public void tearDown() throws Exception {
		_companyConfigurationTemporarySwapper.close();
	}

	@Test
	public void testAddDSRequest() throws Exception {
		DSRecipient dsRecipient1 = _createDSRecipient();

		User user = TestPropsValues.getUser();

		dsRecipient1.setEmailAddress(user.getEmailAddress());

		DSEnvelope dsEnvelope1 = _createDSEnvelope();

		dsEnvelope1.setDSRecipients(
			ListUtil.fromArray(dsRecipient1, _createDSRecipient()));

		long fileEntryId1 = RandomTestUtil.randomLong();

		DSRequest dsRequest = _addDSRequest(dsEnvelope1, fileEntryId1);

		Assert.assertEquals(
			Collections.singletonList(fileEntryId1),
			dsRequest.getFileEntryIds());
		Assert.assertEquals(
			dsEnvelope1.getDSEnvelopeId(), dsRequest.getProviderRequestId());
		Assert.assertEquals(
			TestPropsValues.getGroupId(), dsRequest.getSiteGroupId());

		Map<String, Serializable> requestValues = _getRequestValues(
			dsEnvelope1);

		Assert.assertEquals(
			DSRequestConstants.STATUS_SENT, requestValues.get("requestStatus"));

		List<Map<String, Serializable>> documentValuesList = _getValuesList(
			"(fileEntryId eq " + fileEntryId1 + ")",
			_dsRequestDocumentObjectDefinition);

		Assert.assertEquals(
			documentValuesList.toString(), 1, documentValuesList.size());

		Assert.assertEquals(
			new HashSet<>(
				TransformUtil.transform(
					dsEnvelope1.getDSRecipients(),
					DSRecipient::getEmailAddress)),
			new HashSet<>(
				TransformUtil.transform(
					_getRecipientValuesList(requestValues),
					recipientValues -> MapUtil.getString(
						recipientValues, "emailAddress"))));

		List<Map<String, Serializable>> recipientValuesList = _getValuesList(
			"(providerRecipientId eq '" + dsRecipient1.getDSRecipientId() +
				"')",
			_dsRequestRecipientObjectDefinition);

		Assert.assertEquals(
			recipientValuesList.toString(), 1, recipientValuesList.size());
		Assert.assertEquals(
			user.getUserId(),
			MapUtil.getLong(
				recipientValuesList.get(0),
				"r_userToDSRequestRecipients_userId"));

		DSRecipient dsRecipient2 = _createDSRecipient();

		dsRecipient2.setDSRecipientId(RandomTestUtil.randomString(281));
		dsRecipient2.setEmailAddress(user.getEmailAddress());

		DSEnvelope dsEnvelope2 = _createDSEnvelope();

		dsEnvelope2.setDSRecipients(ListUtil.fromArray(dsRecipient2));

		Group group = GroupTestUtil.addGroup();

		FileEntry fileEntry = _dlAppLocalService.addFileEntry(
			null, TestPropsValues.getUserId(), group.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString() + ".txt", ContentTypes.TEXT_PLAIN,
			TestDataConstants.TEST_BYTE_ARRAY, null, null, null,
			ServiceContextTestUtil.getServiceContext(group.getGroupId()));

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> dsEnvelope2));

		try {
			AssertUtils.assertFailure(
				ObjectEntryValuesException.ExceedsTextMaxLength.class,
				"Object entry value exceeds the maximum length of 280 " +
					"characters for object field \"providerRecipientId\"",
				() -> _dsRequestManager.addDSRequest(
					TestPropsValues.getCompanyId(), group.getGroupId(),
					TestPropsValues.getUserId(), dsEnvelope2,
					new long[] {fileEntry.getFileEntryId()}));
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(providerRequestId eq '" + dsEnvelope2.getDSEnvelopeId() +
					"')",
				_dsRequestObjectDefinition));
		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(fileEntryId eq " + fileEntry.getFileEntryId() + ")",
				_dsRequestDocumentObjectDefinition));
		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(providerRecipientId eq '" + dsRecipient2.getDSRecipientId() +
					"')",
				_dsRequestRecipientObjectDefinition));

		_testAddDSRequestWhenEmbeddedViewIsDisabled();
		_testAddDSRequestWhenExpirationWarningIsNotBeforeExpiration();
		_testAddDSRequestWhenFileEntryHasActiveRequest();
		_testAddDSRequestWhenFileEntryIdsIsEmpty();
		_testAddDSRequestWhenRecipientIsNotUser();
	}

	@Test
	public void testFetchDSRequest() throws Exception {
		DSRequest dsRequest1 = _addDSRequest(
			_createDSEnvelope(), RandomTestUtil.randomLong());

		DSRequest dsRequest2 = _dsRequestManager.fetchDSRequest(
			TestPropsValues.getCompanyId(), dsRequest1.getDSRequestId());

		Assert.assertEquals(
			dsRequest1.getDSRequestId(), dsRequest2.getDSRequestId());
		Assert.assertEquals(
			DSRequestConstants.STATUS_SENT, dsRequest2.getStatus());

		Assert.assertNull(
			_dsRequestManager.fetchDSRequest(
				RandomTestUtil.randomLong(), dsRequest1.getDSRequestId()));

		_testFetchDSRequestWhenExpirationDateIsPast();
		_testFetchDSRequestWhenSiteOverridesInstanceSettings();
	}

	@Test
	public void testGetDSRequests() throws Exception {
		long fileEntryId1 = RandomTestUtil.randomLong();
		long fileEntryId2 = RandomTestUtil.randomLong();

		_addDSRequest(_createDSEnvelope(), fileEntryId1, fileEntryId2);

		Map<Long, DSRequest> dsRequests = _dsRequestManager.getDSRequests(
			TestPropsValues.getCompanyId(),
			ListUtil.fromArray(fileEntryId1, fileEntryId2));

		Assert.assertEquals(dsRequests.toString(), 2, dsRequests.size());

		DSRequest dsRequest1 = dsRequests.get(fileEntryId1);
		DSRequest dsRequest2 = dsRequests.get(fileEntryId2);

		Assert.assertEquals(
			dsRequest1.getProviderRequestId(),
			dsRequest2.getProviderRequestId());

		dsRequests = _dsRequestManager.getDSRequests(
			TestPropsValues.getCompanyId(),
			Collections.singletonList(RandomTestUtil.randomLong()));

		Assert.assertTrue(dsRequests.isEmpty());
	}

	@Test
	public void testGetFileEntryDSRequests() throws Exception {
		String emailAddress = RandomTestUtil.randomString() + "@liferay.com";
		long fileEntryId = RandomTestUtil.randomInt();

		_addDSRequestObjectEntries(
			emailAddress, fileEntryId, DSRequestConstants.STATUS_SENT,
			DSRequestConstants.STATUS_VOIDED, TestPropsValues.getUserId());
		_addDSRequestObjectEntries(
			emailAddress, fileEntryId, DSRequestConstants.STATUS_SENT,
			DSRequestConstants.STATUS_SENT, TestPropsValues.getUserId());

		List<DSRequest> dsRequests = _dsRequestManager.getFileEntryDSRequests(
			TestPropsValues.getCompanyId(), fileEntryId);

		Assert.assertEquals(dsRequests.toString(), 2, dsRequests.size());

		DSRequest dsRequest1 = dsRequests.get(0);

		Assert.assertEquals(
			DSRequestConstants.STATUS_SENT, dsRequest1.getStatus());

		DSRequest dsRequest2 = dsRequests.get(1);

		Assert.assertEquals(
			DSRequestConstants.STATUS_VOIDED, dsRequest2.getStatus());
	}

	@Test
	public void testHasPermission() throws Exception {
		User user1 = UserTestUtil.addUser();

		DSRequest dsRequest = _addDSRequest(
			user1.getEmailAddress(), DSRequestConstants.STATUS_SENT);

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(TestPropsValues.getUser());

		Assert.assertTrue(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.UPDATE));
		Assert.assertTrue(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.VIEW));

		permissionChecker = PermissionCheckerFactoryUtil.create(user1);

		Assert.assertFalse(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.UPDATE));
		Assert.assertTrue(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.VIEW));

		User user2 = UserTestUtil.addUser();

		permissionChecker = PermissionCheckerFactoryUtil.create(user2);

		Assert.assertFalse(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.UPDATE));
		Assert.assertFalse(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.VIEW));
	}

	@Test
	public void testSendDSRequestNotifications() throws Exception {
		List<MailMessage> mailMessages = new ArrayList<>();

		MailService mailService =
			(MailService)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_mailService",
				ProxyUtil.newProxyInstance(
					MailService.class.getClassLoader(),
					new Class<?>[] {MailService.class},
					(proxy, method, arguments) -> {
						if (Objects.equals(method.getName(), "sendEmail")) {
							mailMessages.add((MailMessage)arguments[0]);
						}

						return null;
					}));

		User user = UserTestUtil.addUser();

		try {
			_dsRequestManager.sendDSRequestNotifications(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				_addDSRequest(
					user.getEmailAddress(), DSRequestConstants.STATUS_VOIDED));

			Assert.assertEquals(
				mailMessages.toString(), 0, mailMessages.size());

			_dsRequestManager.sendDSRequestNotifications(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				_addDSRequest(
					user.getEmailAddress(), DSRequestConstants.STATUS_SENT));

			Assert.assertEquals(
				mailMessages.toString(), 1, mailMessages.size());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_mailService", mailService);
		}
	}

	@Test
	public void testSendSignatureReminders() throws Exception {
		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						TestPropsValues.getCompanyId(),
						DigitalSignatureConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"embeddedViewEnabled", true
						).put(
							"enabled", true
						).put(
							"signatureReminderEnabled", true
						).put(
							"siteSettingsStrategy", "always-inherit"
						).build())) {

			int count = _dsRequestManager.sendSignatureReminders(
				TestPropsValues.getCompanyId());

			_addDSRequestObjectEntries(
				RandomTestUtil.randomString() + "@liferay.com",
				RandomTestUtil.randomInt(), DSRequestConstants.STATUS_SENT,
				DSRequestConstants.STATUS_SENT, TestPropsValues.getUserId());
			_addDSRequestObjectEntries(
				RandomTestUtil.randomString() + "@liferay.com",
				RandomTestUtil.randomInt(), DSRequestConstants.STATUS_SENT,
				DSRequestConstants.STATUS_VOIDED, TestPropsValues.getUserId());

			Assert.assertEquals(
				count + 1,
				_dsRequestManager.sendSignatureReminders(
					TestPropsValues.getCompanyId()));
		}
	}

	@Test
	public void testUpdateDSRequest() throws Exception {
		LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(
			System.currentTimeMillis() / 1000, 0, ZoneOffset.UTC);

		_testUpdateDSRequest(
			DSRequestConstants.STATUS_COMPLETED,
			DSRequestConstants.STATUS_COMPLETED, localDateTime.plusDays(1),
			"completed", "completed", localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_CREATED,
			DSRequestConstants.STATUS_CREATED, localDateTime.plusDays(1),
			"autoresponded", RandomTestUtil.randomString(), localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_DECLINED,
			DSRequestConstants.STATUS_DECLINED, localDateTime.plusDays(1),
			"declined", "declined", localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_SENT, DSRequestConstants.STATUS_SENT,
			localDateTime.plusDays(1), "delivered", "delivered", localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_SIGNED, DSRequestConstants.STATUS_EXPIRED,
			localDateTime, "signed", "voided", localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_SIGNED, DSRequestConstants.STATUS_SENT,
			localDateTime.plusDays(1), "signed", "signed", localDateTime);
		_testUpdateDSRequest(
			DSRequestConstants.STATUS_SIGNED, DSRequestConstants.STATUS_VOIDED,
			localDateTime.plusDays(1), "signed", "voided", localDateTime);

		_testUpdateDSRequestWhenRequestIsCompleted();
	}

	private DSRequest _addDSRequest(DSEnvelope dsEnvelope, long... fileEntryIds)
		throws Exception {

		return ReflectionTestUtil.invoke(
			_dsRequestManager, "_addDSRequest",
			new Class<?>[] {
				long.class, long.class, long.class, DSEnvelope.class,
				long[].class
			},
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			TestPropsValues.getUserId(), dsEnvelope, fileEntryIds);
	}

	private DSRequest _addDSRequest(String emailAddress, String status)
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		List<DSRecipient> dsRecipients = dsEnvelope.getDSRecipients();

		DSRecipient dsRecipient = dsRecipients.get(0);

		dsRecipient.setEmailAddress(emailAddress);

		dsEnvelope.setStatus(status);

		return _addDSRequest(dsEnvelope, RandomTestUtil.randomLong());
	}

	private void _addDSRequestObjectEntries(
			String emailAddress, long fileEntryId, String recipientStatus,
			String requestStatus, long userId)
		throws Exception {

		String languageId = LocaleUtil.toLanguageId(
			LocaleUtil.getSiteDefault());
		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				TestPropsValues.getGroupId(), userId);

		ObjectEntry dsRequestObjectEntry =
			_objectEntryLocalService.addObjectEntry(
				0, userId, _dsRequestObjectDefinition.getObjectDefinitionId(),
				0, languageId,
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
			_dsRequestDocumentObjectDefinition.getObjectDefinitionId(), 0,
			languageId,
			HashMapBuilder.<String, Serializable>put(
				"fileEntryId", fileEntryId
			).put(
				"r_dsRequestToDSRequestDocuments_l_dsRequestId",
				dsRequestObjectEntry.getObjectEntryId()
			).build(),
			serviceContext);

		_objectEntryLocalService.addObjectEntry(
			0, userId,
			_dsRequestRecipientObjectDefinition.getObjectDefinitionId(), 0,
			languageId,
			HashMapBuilder.<String, Serializable>put(
				"emailAddress", emailAddress
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"providerRecipientId", RandomTestUtil.randomString()
			).put(
				"r_dsRequestToDSRequestRecipients_l_dsRequestId",
				dsRequestObjectEntry.getObjectEntryId()
			).put(
				"r_userToDSRequestRecipients_userId", userId
			).put(
				"requestRecipientStatus", recipientStatus
			).build(),
			serviceContext);
	}

	private DSEnvelope _createDSEnvelope() {
		DSEnvelope dsEnvelope = new DSEnvelope();

		dsEnvelope.setDSEnvelopeId(RandomTestUtil.randomString());
		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(_createDSRecipient(), _createDSRecipient()));
		dsEnvelope.setEmailSubject(RandomTestUtil.randomString());
		dsEnvelope.setStatus("sent");

		return dsEnvelope;
	}

	private DSRecipient _createDSRecipient() {
		DSRecipient dsRecipient = new DSRecipient();

		dsRecipient.setDSRecipientId(RandomTestUtil.randomString());
		dsRecipient.setEmailAddress(
			RandomTestUtil.randomString() + "@liferay.com");
		dsRecipient.setName(RandomTestUtil.randomString());

		return dsRecipient;
	}

	private List<Map<String, Serializable>> _getRecipientValuesList(
			Map<String, Serializable> values)
		throws Exception {

		long dsRequestId = MapUtil.getLong(
			values, _dsRequestObjectDefinition.getPKObjectFieldName());

		return _getValuesList(
			"(r_dsRequestToDSRequestRecipients_l_dsRequestId eq '" +
				dsRequestId + "')",
			_dsRequestRecipientObjectDefinition);
	}

	private Map<String, Serializable> _getRequestValues(DSEnvelope dsEnvelope)
		throws Exception {

		List<Map<String, Serializable>> valuesList = _getValuesList(
			"(providerRequestId eq '" + dsEnvelope.getDSEnvelopeId() + "')",
			_dsRequestObjectDefinition);

		Assert.assertEquals(valuesList.toString(), 1, valuesList.size());

		return valuesList.get(0);
	}

	private List<Map<String, Serializable>> _getValuesList(
			String filterString, ObjectDefinition objectDefinition)
		throws Exception {

		return _objectEntryLocalService.getValuesList(
			0, TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			_filterFactory.create(filterString, objectDefinition), null,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	private void _testAddDSRequestWhenEmbeddedViewIsDisabled()
		throws Exception {

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						TestPropsValues.getCompanyId(),
						DigitalSignatureConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"embeddedViewEnabled", false
						).put(
							"enabled", true
						).put(
							"siteSettingsStrategy", "always-inherit"
						).build())) {

			AssertUtils.assertFailure(
				PortalException.class,
				"Digital signatures are not enabled for group " +
					TestPropsValues.getGroupId(),
				() -> _dsRequestManager.addDSRequest(
					TestPropsValues.getCompanyId(),
					TestPropsValues.getGroupId(), TestPropsValues.getUserId(),
					_createDSEnvelope(),
					new long[] {RandomTestUtil.randomLong()}));
		}
	}

	private void _testAddDSRequestWhenExpirationWarningIsNotBeforeExpiration()
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		dsEnvelope.setExpireAfter(5);
		dsEnvelope.setExpireWarn(5);

		AssertUtils.assertFailure(
			PortalException.class,
			"Days to warn signers must be fewer than days until expiration",
			() -> _dsRequestManager.addDSRequest(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				TestPropsValues.getUserId(), dsEnvelope,
				new long[] {RandomTestUtil.randomLong()}));
	}

	private void _testAddDSRequestWhenFileEntryHasActiveRequest()
		throws Exception {

		long fileEntryId = RandomTestUtil.randomLong();

		_addDSRequest(_createDSEnvelope(), fileEntryId);

		AssertUtils.assertFailure(
			PortalException.class,
			StringBundler.concat(
				"File entry ", fileEntryId,
				" already has a signature request with status \"",
				DSRequestConstants.STATUS_SENT, "\""),
			() -> _dsRequestManager.addDSRequest(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				TestPropsValues.getUserId(), _createDSEnvelope(),
				new long[] {fileEntryId}));
	}

	private void _testAddDSRequestWhenFileEntryIdsIsEmpty() throws Exception {
		AssertUtils.assertFailure(
			PortalException.class,
			"A signature request must have at least one file entry",
			() -> _dsRequestManager.addDSRequest(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				TestPropsValues.getUserId(), new DSEnvelope(), new long[0]));
	}

	private void _testAddDSRequestWhenRecipientIsNotUser() throws Exception {
		DSRecipient dsRecipient = _createDSRecipient();

		DSEnvelope dsEnvelope = _createDSEnvelope();

		dsEnvelope.setDSRecipients(ListUtil.fromArray(dsRecipient));

		AssertUtils.assertFailure(
			NoSuchUserException.class,
			StringBundler.concat(
				"No User exists with the key {companyId=",
				TestPropsValues.getCompanyId(), ", emailAddress=",
				StringUtil.toLowerCase(dsRecipient.getEmailAddress()), "}"),
			() -> _dsRequestManager.addDSRequest(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				TestPropsValues.getUserId(), dsEnvelope,
				new long[] {RandomTestUtil.randomLong()}));
	}

	private void _testFetchDSRequestWhenExpirationDateIsPast()
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		LocalDateTime localDateTime = LocalDateTime.now();

		dsEnvelope.setExpireLocalDateTime(localDateTime.minusDays(1));

		DSRequest dsRequest = _addDSRequest(
			dsEnvelope, RandomTestUtil.randomLong());

		dsRequest = _dsRequestManager.fetchDSRequest(
			TestPropsValues.getCompanyId(), dsRequest.getDSRequestId());

		Assert.assertEquals(
			DSRequestConstants.STATUS_EXPIRED, dsRequest.getStatus());
		Assert.assertTrue(dsRequest.isRequestable());
		Assert.assertTrue(dsRequest.isTerminal());
	}

	private void _testFetchDSRequestWhenSiteOverridesInstanceSettings()
		throws Exception {

		DSRequest dsRequest = _addDSRequest(
			_createDSEnvelope(), RandomTestUtil.randomLong());

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						TestPropsValues.getCompanyId(),
						DigitalSignatureConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"embeddedViewEnabled", false
						).put(
							"enabled", true
						).put(
							"siteSettingsStrategy", "always-override"
						).build())) {

			Assert.assertNull(
				_dsRequestManager.fetchDSRequest(
					TestPropsValues.getCompanyId(),
					dsRequest.getDSRequestId()));

			try (GroupConfigurationTemporarySwapper
					groupConfigurationTemporarySwapper =
						new GroupConfigurationTemporarySwapper(
							TestPropsValues.getGroupId(),
							DigitalSignatureConfiguration.class.getName(),
							HashMapDictionaryBuilder.<String, Object>put(
								"embeddedViewEnabled", true
							).put(
								"enabled", true
							).build())) {

				Assert.assertNotNull(
					_dsRequestManager.fetchDSRequest(
						TestPropsValues.getCompanyId(),
						dsRequest.getDSRequestId()));
			}
		}
	}

	private void _testUpdateDSRequest(
			String expectedRequestRecipientStatus, String expectedRequestStatus,
			LocalDateTime expireLocalDateTime, String requestRecipientStatus,
			String requestStatus, LocalDateTime statusChangedLocalDateTime)
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		dsEnvelope.setStatus(DSRequestConstants.STATUS_CREATED);

		for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
			dsRecipient.setStatus(DSRequestConstants.STATUS_CREATED);
		}

		_addDSRequest(dsEnvelope, RandomTestUtil.randomLong());

		dsEnvelope.setExpireLocalDateTime(expireLocalDateTime);
		dsEnvelope.setStatus(requestStatus);
		dsEnvelope.setStatusChangedLocalDateTime(statusChangedLocalDateTime);

		for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
			dsRecipient.setStatus(requestRecipientStatus);
			dsRecipient.setStatusLocalDateTime(statusChangedLocalDateTime);
		}

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> dsEnvelope));

		User user = UserTestUtil.addUser();

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user, PermissionCheckerFactoryUtil.create(user))) {

			_dsRequestManager.updateDSRequest(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				dsEnvelope.getDSEnvelopeId());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		Map<String, Serializable> requestValues = _getRequestValues(dsEnvelope);

		Assert.assertEquals(
			Date.from(expireLocalDateTime.toInstant(ZoneOffset.UTC)),
			requestValues.get("requestExpirationDate"));
		Assert.assertEquals(
			expectedRequestStatus, requestValues.get("requestStatus"));
		Assert.assertEquals(
			Date.from(statusChangedLocalDateTime.toInstant(ZoneOffset.UTC)),
			requestValues.get("requestStatusDate"));

		List<Map<String, Serializable>> recipientValuesList =
			_getRecipientValuesList(requestValues);

		Assert.assertEquals(
			recipientValuesList.toString(), 2, recipientValuesList.size());

		for (Map<String, Serializable> recipientValues : recipientValuesList) {
			Assert.assertEquals(
				expectedRequestRecipientStatus,
				recipientValues.get("requestRecipientStatus"));
			Assert.assertEquals(
				Date.from(statusChangedLocalDateTime.toInstant(ZoneOffset.UTC)),
				recipientValues.get("requestRecipientStatusDate"));
		}
	}

	private void _testUpdateDSRequestWhenRequestIsCompleted() throws Exception {
		Group group = GroupTestUtil.addGroup();

		DSEnvelope dsEnvelope = _createDSEnvelope();
		FileEntry fileEntry = _portletFileRepository.addPortletFileEntry(
			group.getGroupId(), TestPropsValues.getUserId(),
			Group.class.getName(), group.getGroupId(),
			RandomTestUtil.randomString(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			TestDataConstants.TEST_BYTE_ARRAY,
			RandomTestUtil.randomString() + ".txt", ContentTypes.TEXT_PLAIN,
			false);

		_addDSRequest(dsEnvelope, fileEntry.getFileEntryId());

		dsEnvelope.setStatus(DSRequestConstants.STATUS_COMPLETED);

		byte[] bytes = RandomTestUtil.randomBytes();

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> {
						if (Objects.equals(
								method.getName(), "getSignedDocument")) {

							return bytes;
						}

						return dsEnvelope;
					}));

		try {
			_dsRequestManager.updateDSRequest(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				dsEnvelope.getDSEnvelopeId());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		fileEntry = _dlAppLocalService.getFileEntry(fileEntry.getFileEntryId());

		Assert.assertArrayEquals(
			bytes, FileUtil.getBytes(fileEntry.getContentStream()));
		Assert.assertEquals("2.0", fileEntry.getVersion());

		FileVersion fileVersion = fileEntry.getLatestFileVersion();

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED, fileVersion.getStatus());
	}

	private CompanyConfigurationTemporarySwapper
		_companyConfigurationTemporarySwapper;

	@Inject
	private DLAppLocalService _dlAppLocalService;

	private ObjectDefinition _dsRequestDocumentObjectDefinition;

	@Inject
	private DSRequestManager _dsRequestManager;

	private ObjectDefinition _dsRequestObjectDefinition;
	private ObjectDefinition _dsRequestRecipientObjectDefinition;

	@Inject(
		filter = "filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT
	)
	private FilterFactory<Predicate> _filterFactory;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private PortletFileRepository _portletFileRepository;

}