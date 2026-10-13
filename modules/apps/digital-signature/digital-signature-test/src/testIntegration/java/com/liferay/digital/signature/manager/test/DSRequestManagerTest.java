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
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.exception.ObjectEntryValuesException;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.test.rule.FeatureFlag;
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

		_dsRequestManager.addDSRequest(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			TestPropsValues.getUserId(), dsEnvelope1,
			new long[] {fileEntryId1});

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

		dsRecipient2.setName(RandomTestUtil.randomString(281));

		DSEnvelope dsEnvelope2 = _createDSEnvelope();

		dsEnvelope2.setDSRecipients(ListUtil.fromArray(dsRecipient2));

		long fileEntryId2 = RandomTestUtil.randomLong();

		AssertUtils.assertFailure(
			ObjectEntryValuesException.ExceedsTextMaxLength.class,
			"Object entry value exceeds the maximum length of 280 characters " +
				"for object field \"name\"",
			() -> _dsRequestManager.addDSRequest(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				TestPropsValues.getUserId(), dsEnvelope2,
				new long[] {fileEntryId2}));

		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(providerRequestId eq '" + dsEnvelope2.getDSEnvelopeId() +
					"')",
				_dsRequestObjectDefinition));
		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(fileEntryId eq " + fileEntryId2 + ")",
				_dsRequestDocumentObjectDefinition));
		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(providerRecipientId eq '" + dsRecipient2.getDSRecipientId() +
					"')",
				_dsRequestRecipientObjectDefinition));
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
	}
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

		_dsRequestManager.addDSRequest(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			TestPropsValues.getUserId(), dsEnvelope,
			new long[] {RandomTestUtil.randomLong()});

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

	private CompanyConfigurationTemporarySwapper
		_companyConfigurationTemporarySwapper;
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

}