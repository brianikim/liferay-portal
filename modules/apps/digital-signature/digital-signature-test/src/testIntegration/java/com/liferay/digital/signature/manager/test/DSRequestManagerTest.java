/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.manager.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.batch.engine.test.util.BatchEngineTestUtil;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.constants.DSObjectDefinitionConstants;
import com.liferay.digital.signature.constants.DSRequestConstants;
import com.liferay.digital.signature.exception.DSRequestException;
import com.liferay.digital.signature.manager.DSEnvelopeManager;
import com.liferay.digital.signature.manager.DSRequestManager;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.digital.signature.model.DSRecipient;
import com.liferay.digital.signature.model.DSRequest;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.exception.NoSuchObjectEntryException;
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
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalService;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalService;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.context.ContextUserReplace;
import com.liferay.portal.kernel.test.portlet.MockLiferayResourceRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayResourceResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import jakarta.portlet.PortletException;

import java.io.Serializable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

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
		BatchEngineTestUtil.processBatchEngineUnits(
			"com.liferay.digital.signature.impl", DSRequestManagerTest.class,
			new String[] {
				".com.liferay.digital.signature.internal.batch.01.object." +
					"definition",
				".com.liferay.digital.signature.internal.batch.02.object." +
					"relationship"
			});

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
					DSObjectDefinitionConstants.
						EXTERNAL_REFERENCE_CODE_REQUEST_DOCUMENT,
					TestPropsValues.getCompanyId());
		_dsRequestObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					DSObjectDefinitionConstants.EXTERNAL_REFERENCE_CODE_REQUEST,
					TestPropsValues.getCompanyId());
		_dsRequestRecipientObjectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					DSObjectDefinitionConstants.
						EXTERNAL_REFERENCE_CODE_REQUEST_RECIPIENT,
					TestPropsValues.getCompanyId());
	}

	@After
	public void tearDown() throws Exception {
		_companyConfigurationTemporarySwapper.close();
	}

	@Test
	public void testAddDSRequest() throws Exception {
		DSEnvelope dsEnvelope = _createDSEnvelope();
		DSRecipient dsRecipient = _createDSRecipient();

		User user = TestPropsValues.getUser();

		dsRecipient.setEmailAddress(user.getEmailAddress());

		dsEnvelope.setDSRecipients(
			ListUtil.fromArray(dsRecipient, _createDSRecipient()));

		long fileEntryId = RandomTestUtil.randomInt();

		_dsRequestManager.addDSRequest(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			TestPropsValues.getUserId(), dsEnvelope, new long[] {fileEntryId});

		Map<String, Serializable> dsRequestValues = _getDSRequestValues(
			dsEnvelope);

		Map<String, Serializable> dsRequestValues =
			dsRequestObjectEntry.getValues();

		Assert.assertEquals(
			DSRequestConstants.STATUS_SENT,
			MapUtil.getString(dsRequestValues, "requestStatus"));
		Assert.assertEquals(
			TestPropsValues.getGroupId(),
			MapUtil.getLong(dsRequestValues, "siteGroupId"));
		Assert.assertEquals(
			new HashSet<>(
				TransformUtil.transform(
					dsEnvelope.getDSRecipients(),
					DSRecipient::getEmailAddress)),
			new HashSet<>(
				TransformUtil.transform(
					_getDSRequestRecipientValuesList(dsRequestValues),
					values -> MapUtil.getString(values, "emailAddress"))));

		List<Map<String, Serializable>> dsRequestDocumentValuesList =
			_getValuesList(
				"(fileEntryId eq " + fileEntryId + ")",
				_dsRequestDocumentObjectDefinition);

		Assert.assertEquals(
			dsRequestDocumentValuesList.toString(), 1,
			dsRequestDocumentValuesList.size());

		List<Map<String, Serializable>> dsRequestRecipientValuesList =
			ListUtil.filter(
				_getDSRequestRecipientValuesList(dsRequestValues),
				values -> Objects.equals(
					dsRecipient.getEmailAddress(),
					MapUtil.getString(values, "emailAddress")));

		Assert.assertEquals(
			dsRequestRecipientValuesList.toString(), 1,
			dsRequestRecipientValuesList.size());
		Assert.assertEquals(
			user1.getFullName(),
			MapUtil.getString(dsRequestRecipientValues, "name"));
		Assert.assertEquals(
			user1.getUserId(),
			MapUtil.getLong(
				dsRequestRecipientValuesList.get(0),
				"r_userToDSRequestRecipients_userId"));

		MockLiferayResourceRequest mockLiferayResourceRequest =
			new MockLiferayResourceRequest();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()));
		themeDisplay.setSiteGroupId(TestPropsValues.getGroupId());
		themeDisplay.setUser(user);

		mockLiferayResourceRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		String emailAddress = RandomTestUtil.randomString() + "@liferay.com";

		mockLiferayResourceRequest.setParameter(
			"recipients",
			JSONUtil.putAll(
				JSONUtil.put(
					"email", emailAddress
				).put(
					"fullName", RandomTestUtil.randomString(281)
				)
			).toString());

		String dsEnvelopeId = RandomTestUtil.randomString();

		Queue<Object[]> argumentsList = new LinkedList<>();

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_mvcResourceCommand, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> {
						DSEnvelope sentDSEnvelope = (DSEnvelope)arguments[2];

						sentDSEnvelope.setDSEnvelopeId(dsEnvelopeId);

						return sentDSEnvelope;
					}));

		try {
			PortletException portletException = Assert.assertThrows(
				PortletException.class,
				() -> _mvcResourceCommand.serveResource(
					mockLiferayResourceRequest,
					new MockLiferayResourceResponse()));

			Throwable throwable = portletException.getCause();

			Assert.assertSame(
				ObjectEntryValuesException.ExceedsTextMaxLength.class,
				throwable.getClass());
			Assert.assertEquals(
				"Object entry value exceeds the maximum length of 280 " +
					"characters for object field \"name\"",
				throwable.getMessage());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_mvcResourceCommand, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		Assert.assertEquals(argumentsList.toString(), 1, argumentsList.size());

		Object[] arguments = argumentsList.poll();

		Assert.assertEquals(dsEnvelope2.getDSEnvelopeId(), arguments[2]);

		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(externalReferenceCode eq '" + dsEnvelopeId + "')",
				_dsRequestObjectDefinition));
		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(emailAddress eq '" + emailAddress + "')",
				_dsRequestRecipientObjectDefinition));

		_testAddDSRequestWhenDSEnvelopeIdIsNull();
		_testAddDSRequestWhenDSRequestRecipientUserIsDeleted();
	}

	@Test
	public void testFetchDSRequest() throws Exception {
		DSRequest dsRequest1 = _addDSRequest(
			_createDSEnvelope(), RandomTestUtil.randomInt());

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
	public void testGetLatestFileEntryDSRequests() throws Exception {
		long fileEntryId1 = RandomTestUtil.randomInt();
		long fileEntryId2 = RandomTestUtil.randomInt();

		DSRequest dsRequest1 = _addDSRequest(
			_createDSEnvelope(), fileEntryId1, fileEntryId2);
		DSRequest dsRequest2 = _addDSRequest(_createDSEnvelope(), fileEntryId2);

		Map<Long, DSRequest> dsRequests =
			_dsRequestManager.getLatestFileEntryDSRequests(
				TestPropsValues.getCompanyId(),
				new long[] {fileEntryId1, fileEntryId2});

		Assert.assertEquals(dsRequests.toString(), 2, dsRequests.size());

		DSRequest dsRequest3 = dsRequests.get(fileEntryId1);

		Assert.assertEquals(
			dsRequest1.getDSRequestId(), dsRequest3.getDSRequestId());

		DSRequest dsRequest4 = dsRequests.get(fileEntryId2);

		Assert.assertEquals(
			dsRequest2.getDSRequestId(), dsRequest4.getDSRequestId());

		dsRequests = _dsRequestManager.getLatestFileEntryDSRequests(
			TestPropsValues.getCompanyId(),
			new long[] {RandomTestUtil.randomLong()});

		Assert.assertTrue(dsRequests.isEmpty());
	}

	@Test
	public void testHasPermission() throws Exception {
		User user1 = UserTestUtil.addUser();

		DSEnvelope dsEnvelope = _createDSEnvelope(user1);

		User user2 = UserTestUtil.addUser();

		DSRequest dsRequest = ReflectionTestUtil.invoke(
			_dsRequestManager, "_addDSRequest",
			new Class<?>[] {
				long.class, DSEnvelope.class, long[].class, long.class,
				long.class
			},
			TestPropsValues.getCompanyId(), dsEnvelope,
			new long[] {RandomTestUtil.randomInt()},
			TestPropsValues.getGroupId(), user2.getUserId());

		PermissionChecker permissionChecker =
			PermissionCheckerFactoryUtil.create(user2);

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

		permissionChecker = PermissionCheckerFactoryUtil.create(
			UserTestUtil.addUser());

		Assert.assertFalse(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.UPDATE));
		Assert.assertFalse(
			_dsRequestManager.hasPermission(
				permissionChecker, dsRequest, ActionKeys.VIEW));
	}

	@Test
	public void testUpdateDSRequest() throws Exception {
		LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(
			System.currentTimeMillis() / 1000, 0, ZoneOffset.UTC);

		_testUpdateDSRequest(
			"completed", DSRequestConstants.STATUS_COMPLETED,
			localDateTime.plusDays(1), localDateTime);
		_testUpdateDSRequest(
			RandomTestUtil.randomString(), DSRequestConstants.STATUS_CREATED,
			localDateTime.plusDays(1), localDateTime);
		_testUpdateDSRequest(
			"declined", DSRequestConstants.STATUS_DECLINED,
			localDateTime.plusDays(1), localDateTime);
		_testUpdateDSRequest(
			"voided", DSRequestConstants.STATUS_EXPIRED, localDateTime,
			localDateTime);
		_testUpdateDSRequest(
			"delivered", DSRequestConstants.STATUS_SENT,
			localDateTime.plusDays(1), localDateTime);
		_testUpdateDSRequest(
			"signed", DSRequestConstants.STATUS_SENT, localDateTime.plusDays(1),
			localDateTime);
		_testUpdateDSRequest(
			"voided", DSRequestConstants.STATUS_VOIDED,
			localDateTime.plusDays(1), localDateTime);

		_testUpdateDSRequestWhenObjectEntryIsNotDSRequest();
	}

	private DSRequest _addDSRequest(DSEnvelope dsEnvelope, long... fileEntryIds)
		throws Exception {

		return ReflectionTestUtil.invoke(
			_dsRequestManager, "_addDSRequest",
			new Class<?>[] {
				long.class, DSEnvelope.class, long[].class, long.class,
				long.class
			},
			TestPropsValues.getCompanyId(), dsEnvelope, fileEntryIds,
			TestPropsValues.getGroupId(), TestPropsValues.getUserId());
	}

	private DSRequest _addDSRequest(String emailAddress, String status)
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		List<DSRecipient> dsRecipients = dsEnvelope.getDSRecipients();

		DSRecipient dsRecipient = dsRecipients.get(0);

		dsRecipient.setEmailAddress(emailAddress);

		dsEnvelope.setStatus(status);

		return _addDSRequest(dsEnvelope, RandomTestUtil.randomInt());
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
		return _createDSRecipient(
			RandomTestUtil.randomString() + "@liferay.com");
	}

	private DSRecipient _createDSRecipient(String emailAddress) {
		DSRecipient dsRecipient = new DSRecipient();

		dsRecipient.setEmailAddress(emailAddress);
		dsRecipient.setName(RandomTestUtil.randomString());

		return dsRecipient;
	}

	private List<Map<String, Serializable>> _getDSRequestRecipientValuesList(
			Map<String, Serializable> values)
		throws Exception {

		long dsRequestId = MapUtil.getLong(
			values, _dsRequestObjectDefinition.getPKObjectFieldName());

		return MapUtil.getLong(
			dsRequestDocumentValuesList.get(0),
			_dsRequestDocumentObjectDefinition.getPKObjectFieldName());
	}

	private ObjectEntry _getDSRequestObjectEntry(DSEnvelope dsEnvelope)
		throws Exception {

		return _objectEntryLocalService.getObjectEntry(
			dsEnvelope.getDSEnvelopeId(), 0,
			_dsRequestObjectDefinition.getObjectDefinitionId());
	}

	private List<Map<String, Serializable>> _getDSRequestRecipientValuesList(
			long dsRequestId)
		throws Exception {

		return _getValuesList(
			"(r_dsRequestToDSRequestRecipients_l_dsRequestId eq '" +
				dsRequestId + "')",
			_dsRequestRecipientObjectDefinition);
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

	private void _testAddDSRequestWhenDSEnvelopeIdIsNull() throws Exception {
		DSEnvelope dsEnvelope = _createDSEnvelope();

		dsEnvelope.setDSEnvelopeId(null);

		DSRecipient dsRecipient = _createDSRecipient();

		dsEnvelope.setDSRecipients(ListUtil.fromArray(dsRecipient));

		AssertUtils.assertFailure(
			PortalException.class,
			"A digital signature request requires a digital signature " +
				"envelope ID",
			() -> _dsRequestManager.addDSRequest(
				TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
				TestPropsValues.getUserId(), dsEnvelope,
				new long[] {RandomTestUtil.randomInt()}));

		Assert.assertEquals(
			Collections.emptyList(),
			_getValuesList(
				"(emailAddress eq '" + dsRecipient.getEmailAddress() + "')",
				_dsRequestRecipientObjectDefinition));
	}

	private void _testAddDSRequestWhenDSRequestRecipientUserIsDeleted()
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		DSRecipient dsRecipient = _createDSRecipient();

		User user = UserTestUtil.addUser();

		dsRecipient.setEmailAddress(user.getEmailAddress());

		dsEnvelope.setDSRecipients(ListUtil.fromArray(dsRecipient));

		_addDSRequest(dsEnvelope, RandomTestUtil.randomInt());

		String filterString =
			"(emailAddress eq '" + dsRecipient.getEmailAddress() + "')";

		List<Map<String, Serializable>> dsRequestRecipientValuesList =
			_getValuesList(filterString, _dsRequestRecipientObjectDefinition);

		Assert.assertEquals(
			dsRequestRecipientValuesList.toString(), 1,
			dsRequestRecipientValuesList.size());
		Assert.assertEquals(
			user.getUserId(),
			MapUtil.getLong(
				dsRequestRecipientValuesList.get(0),
				"r_userToDSRequestRecipients_userId"));

		_userLocalService.deleteUser(user);

		dsRequestRecipientValuesList = _getValuesList(
			filterString, _dsRequestRecipientObjectDefinition);

		Assert.assertEquals(
			dsRequestRecipientValuesList.toString(), 1,
			dsRequestRecipientValuesList.size());

		Map<String, Serializable> dsRequestRecipientValues =
			dsRequestRecipientValuesList.get(0);

		Assert.assertEquals(
			user.getEmailAddress(),
			MapUtil.getString(dsRequestRecipientValues, "emailAddress"));
		Assert.assertEquals(
			0,
			MapUtil.getLong(
				dsRequestRecipientValues,
				"r_userToDSRequestRecipients_userId"));
	}

	private void _testFetchDSRequestWhenExpirationDateIsPast()
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		LocalDateTime localDateTime = LocalDateTime.now();

		dsEnvelope.setExpirationLocalDateTime(localDateTime.minusDays(1));

		DSRequest dsRequest = _addDSRequest(
			dsEnvelope, RandomTestUtil.randomInt());

		dsRequest = _dsRequestManager.fetchDSRequest(
			TestPropsValues.getCompanyId(), dsRequest.getDSRequestId());

		Assert.assertTrue(dsRequest.isReplaceable());
		Assert.assertTrue(dsRequest.isTerminal());
		Assert.assertEquals(
			DSRequestConstants.STATUS_EXPIRED, dsRequest.getStatus());
	}

	private void _testFetchDSRequestWhenSiteOverridesInstanceSettings()
		throws Exception {

		DSRequest dsRequest = _addDSRequest(
			_createDSEnvelope(), RandomTestUtil.randomInt());

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

				DSRequest fetchedDSRequest = _dsRequestManager.fetchDSRequest(
					TestPropsValues.getCompanyId(), dsRequest.getDSRequestId());

				Assert.assertEquals(
					dsRequest.getDSRequestId(),
					fetchedDSRequest.getDSRequestId());
			}
		}
	}

	private void _testUpdateDSRequest(
			String dsEnvelopeStatus, String expectedRequestStatus,
			LocalDateTime expirationLocalDateTime,
			LocalDateTime statusLocalDateTime)
		throws Exception {

		DSEnvelope dsEnvelope = _createDSEnvelope();

		dsEnvelope.setStatus(DSRequestConstants.STATUS_CREATED);

		_dsRequestManager.addDSRequest(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			TestPropsValues.getUserId(), dsEnvelope,
			new long[] {RandomTestUtil.randomInt()});

		dsEnvelope.setExpirationLocalDateTime(expirationLocalDateTime);
		dsEnvelope.setStatus(dsEnvelopeStatus);
		dsEnvelope.setStatusLocalDateTime(statusLocalDateTime);

		DSEnvelopeManager dsEnvelopeManager =
			(DSEnvelopeManager)ReflectionTestUtil.getAndSetFieldValue(
				_dsRequestManager, "_dsEnvelopeManager",
				ProxyUtil.newProxyInstance(
					DSEnvelopeManager.class.getClassLoader(),
					new Class<?>[] {DSEnvelopeManager.class},
					(proxy, method, arguments) -> {
						Assert.assertEquals(
							TestPropsValues.getCompanyId(), arguments[0]);
						Assert.assertEquals(
							TestPropsValues.getGroupId(), arguments[1]);
						Assert.assertEquals(
							dsEnvelope.getDSEnvelopeId(), arguments[2]);

						return dsEnvelope;
					}));

		ObjectEntry objectEntry = _objectEntryLocalService.getObjectEntry(
			dsEnvelope.getDSEnvelopeId(), 0,
			_dsRequestObjectDefinition.getObjectDefinitionId());

		User user = UserTestUtil.addUser();

		try (ContextUserReplace contextUserReplace = new ContextUserReplace(
				user, PermissionCheckerFactoryUtil.create(user))) {

			_dsRequestManager.updateDSRequest(objectEntry.getObjectEntryId());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				_dsRequestManager, "_dsEnvelopeManager", dsEnvelopeManager);
		}

		ObjectEntry dsRequestObjectEntry = _getDSRequestObjectEntry(dsEnvelope);

		Map<String, Serializable> dsRequestValues =
			dsRequestObjectEntry.getValues();

		if (expirationLocalDateTime == null) {
			Assert.assertNull(dsRequestValues.get("requestExpirationDate"));
		}
		else {
			Assert.assertEquals(
				Date.from(expirationLocalDateTime.toInstant(ZoneOffset.UTC)),
				dsRequestValues.get("requestExpirationDate"));
		}

		Assert.assertEquals(
			expectedRequestStatus,
			MapUtil.getString(dsRequestValues, "requestStatus"));
		Assert.assertEquals(
			Date.from(statusLocalDateTime.toInstant(ZoneOffset.UTC)),
			dsRequestValues.get("requestStatusDate"));
	}

	private void _testUpdateDSRequestWhenObjectEntryIsNotDSRequest()
		throws Exception {

		long fileEntryId = RandomTestUtil.randomInt();

		_dsRequestManager.addDSRequest(
			TestPropsValues.getCompanyId(), TestPropsValues.getGroupId(),
			TestPropsValues.getUserId(), _createDSEnvelope(),
			new long[] {fileEntryId});

		long dsRequestDocumentId = _getDSRequestDocumentId(fileEntryId);

		AssertUtils.assertFailure(
			NoSuchObjectEntryException.class,
			StringBundler.concat(
				"No ObjectEntry exists with the key {objectDefinitionId=",
				_dsRequestObjectDefinition.getObjectDefinitionId(),
				", objectEntryId=", dsRequestDocumentId, "}"),
			() -> _dsRequestManager.updateDSRequest(dsRequestDocumentId));
	}

	private CompanyConfigurationTemporarySwapper
		_companyConfigurationTemporarySwapper;

	@Inject
	private CompanyLocalService _companyLocalService;

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
	private GroupLocalService _groupLocalService;

	@Inject(filter = "mvc.command.name=/digital_signature/add_ds_envelope")
	private MVCResourceCommand _mvcResourceCommand;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private UserLocalService _userLocalService;

	@Inject
	private WorkflowDefinitionLinkLocalService
		_workflowDefinitionLinkLocalService;

	@Inject
	private WorkflowInstanceLinkLocalService _workflowInstanceLinkLocalService;

}