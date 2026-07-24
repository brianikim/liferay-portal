/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.provider.DSRequestDetail;
import com.liferay.digital.signature.provider.DSRequestDetailProvider;
import com.liferay.digital.signature.provider.DSRequestRecipientDetail;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.io.Serializable;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Brian Kim
 */
@FeatureFlags(featureFlags = @FeatureFlag(value = "LPD-69290"))
@RunWith(Arquillian.class)
public class DSRequestDetailProviderTest {

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
				"enabled", true
			).build());

		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testGetRequestDetail() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		long fileEntryId = RandomTestUtil.randomInt();

		ObjectEntry requestObjectEntry = _addRequestObjectEntry(
			companyId, fileEntryId);

		_addRecipientObjectEntry(
			companyId, requestObjectEntry.getObjectEntryId());

		DSRequestDetail dsRequestDetail =
			_dsRequestDetailProvider.getRequestDetail(companyId, fileEntryId);

		Assert.assertNotNull(dsRequestDetail);
		Assert.assertEquals("sent", dsRequestDetail.getRequestStatus());
		Assert.assertEquals(
			"test-" + fileEntryId, dsRequestDetail.getProviderRequestId());
		Assert.assertEquals(
			TestPropsValues.getUserId(), dsRequestDetail.getRequesterUserId());

		List<DSRequestRecipientDetail> dsRequestRecipientDetails =
			dsRequestDetail.getRecipientDetails();

		Assert.assertEquals(
			dsRequestRecipientDetails.toString(), 1,
			dsRequestRecipientDetails.size());

		DSRequestRecipientDetail dsRequestRecipientDetail =
			dsRequestRecipientDetails.get(0);

		Assert.assertEquals(
			"recipient@liferay.com",
			dsRequestRecipientDetail.getEmailAddress());
		Assert.assertEquals("Recipient", dsRequestRecipientDetail.getName());
		Assert.assertEquals(
			"signed", dsRequestRecipientDetail.getRequestRecipientStatus());
	}

	@Test
	public void testGetRequestDetailReturnsNullForMissingRequest()
		throws Exception {

		Assert.assertNull(
			_dsRequestDetailProvider.getRequestDetail(
				TestPropsValues.getCompanyId(), RandomTestUtil.randomLong()));
	}

	private void _addRecipientObjectEntry(long companyId, long requestId)
		throws Exception {

		ObjectDefinition recipientObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST_RECIPIENT", companyId);

		ObjectDefinition requestObjectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		ObjectRelationship objectRelationship =
			_objectRelationshipLocalService.fetchObjectRelationship(
				requestObjectDefinition.getObjectDefinitionId(),
				"dsRequestToDSRequestRecipients");

		ObjectField objectField = _objectFieldLocalService.getObjectField(
			objectRelationship.getObjectFieldId2());

		_objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			recipientObjectDefinition.getObjectDefinitionId(), 0,
			LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()),
			HashMapBuilder.<String, Serializable>put(
				"emailAddress", "recipient@liferay.com"
			).put(
				"name", "Recipient"
			).put(
				"providerRecipientId", "recipient-1"
			).put(
				"recipientUserId", TestPropsValues.getUserId()
			).put(
				"requestRecipientStatus", "signed"
			).put(
				objectField.getName(), requestId
			).build(),
			ServiceContextTestUtil.getServiceContext(
				_group.getGroupId(), TestPropsValues.getUserId()));
	}

	private ObjectEntry _addRequestObjectEntry(long companyId, long fileEntryId)
		throws Exception {

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		Assert.assertNotNull(
			"The L_DS_REQUEST object definition must exist when the feature " +
				"flag is enabled",
			objectDefinition);

		// L_DS_REQUEST is company-scoped, so the group ID must be 0

		return _objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(), 0,
			LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()),
			HashMapBuilder.<String, Serializable>put(
				"emailSubject", "Please sign"
			).put(
				"fileEntryId", fileEntryId
			).put(
				"providerKey", "docusign"
			).put(
				"providerRequestId", "test-" + fileEntryId
			).put(
				"requestStatus", "sent"
			).build(),
			ServiceContextTestUtil.getServiceContext(
				_group.getGroupId(), TestPropsValues.getUserId()));
	}

	@Inject
	private ConfigurationProvider _configurationProvider;

	@Inject
	private DSRequestDetailProvider _dsRequestDetailProvider;

	private Group _group;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private ObjectFieldLocalService _objectFieldLocalService;

	@Inject
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

}