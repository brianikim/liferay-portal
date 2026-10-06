/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.manager.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.constants.DSRequestConstants;
import com.liferay.digital.signature.manager.DSEnvelopeManager;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.rest.filter.factory.FilterFactory;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
		DSEnvelope dsEnvelope = _getDSEnvelope();

		_addDSRequest(dsEnvelope);

		Map<String, Serializable> requestValues = _getRequestValues(dsEnvelope);

		Assert.assertEquals(
			DSRequestConstants.STATUS_SENT, requestValues.get("requestStatus"));

		Set<String> expectedEmailAddresses = new HashSet<>();

		for (DSRecipient dsRecipient : dsEnvelope.getDSRecipients()) {
			expectedEmailAddresses.add(dsRecipient.getEmailAddress());
		}

		Set<String> actualEmailAddresses = new HashSet<>();

		for (Map<String, Serializable> recipientValues :
				_getRecipientValuesList(requestValues)) {

			actualEmailAddresses.add(
				GetterUtil.getString(recipientValues.get("emailAddress")));
		}

		Assert.assertEquals(expectedEmailAddresses, actualEmailAddresses);
	}

	@Test
	public void testUpdateDSRequest() throws Exception {
		LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(
			System.currentTimeMillis() / 1000, 0, ZoneOffset.UTC);

		_testUpdateDSRequest(
			"completed", localDateTime.plusDays(1), "completed", localDateTime);
		_testUpdateDSRequest("expired", localDateTime, "voided", localDateTime);
	}

	private void _addDSRequest(DSEnvelope dsEnvelope) throws Exception {
		_dsRequestManager.addDSRequest(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			TestPropsValues.getUserId(), dsEnvelope,
			new long[] {RandomTestUtil.randomLong()});
	}

	private DSEnvelope _getDSEnvelope() {
		DSEnvelope dsEnvelope = new DSEnvelope();

		dsEnvelope.setDSEnvelopeId(RandomTestUtil.randomString());
		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(_getDSRecipient(), _getDSRecipient()));
		dsEnvelope.setEmailSubject(RandomTestUtil.randomString());
		dsEnvelope.setStatus("sent");

		return dsEnvelope;
	}

	private DSRecipient _getDSRecipient() {
		DSRecipient dsRecipient = new DSRecipient();

		dsRecipient.setDSRecipientId(RandomTestUtil.randomString());
		dsRecipient.setEmailAddress(
			RandomTestUtil.randomString() + "@liferay.com");
		dsRecipient.setName(RandomTestUtil.randomString());

		return dsRecipient;
	}

	private ObjectDefinition _getObjectDefinition(String externalReferenceCode)
		throws Exception {

		return _objectDefinitionLocalService.
			fetchObjectDefinitionByExternalReferenceCode(
				externalReferenceCode, TestPropsValues.getCompanyId());
	}

	private List<Map<String, Serializable>> _getRecipientValuesList(
			Map<String, Serializable> requestValues)
		throws Exception {

		ObjectDefinition dsRequestObjectDefinition = _getObjectDefinition(
			"L_DS_REQUEST");

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.fetchObjectRelationship(
				dsRequestObjectDefinition.getObjectDefinitionId(),
				"dsRequestToDSRequestRecipients");

		ObjectField objectField = _objectFieldLocalService.getObjectField(
			objectRelationship.getObjectFieldId2());

		return _getValuesList(
			StringBundler.concat(
				"(", objectField.getName(), " eq '",
				GetterUtil.getLong(
					requestValues.get(
						dsRequestObjectDefinition.getPKObjectFieldName())),
				"')"),
			_getObjectDefinition("L_DS_REQUEST_RECIPIENT"));
	}

	private Map<String, Serializable> _getRequestValues(DSEnvelope dsEnvelope)
		throws Exception {

		List<Map<String, Serializable>> requestValuesList = _getValuesList(
			"(providerRequestId eq '" + dsEnvelope.getDSEnvelopeId() + "')",
			_getObjectDefinition("L_DS_REQUEST"));

		Assert.assertEquals(
			requestValuesList.toString(), 1, requestValuesList.size());

		return requestValuesList.get(0);
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

	private void _testUpdateDSRequest(
			String expectedRequestStatus, LocalDateTime expireLocalDateTime,
			String status, LocalDateTime statusChangedLocalDateTime)
		throws Exception {

		DSEnvelope dsEnvelope = _getDSEnvelope();

		_addDSRequest(dsEnvelope);

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

		for (Map<String, Serializable> recipientValues :
				_getRecipientValuesList(requestValues)) {

			Assert.assertEquals(
				"completed", recipientValues.get("requestRecipientStatus"));
			Assert.assertEquals(
				Date.from(statusChangedLocalDateTime.toInstant(ZoneOffset.UTC)),
				recipientValues.get("requestRecipientStatusDate"));
		}
	}

	@Inject
	private ConfigurationProvider _configurationProvider;

	@Inject
	private DSRequestManager _dsRequestManager;

	@Inject(
		filter = "filter.factory.key=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT
	)
	private FilterFactory<Predicate> _filterFactory;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private ObjectFieldLocalService _objectFieldLocalService;

	@Inject
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

}