/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.provider.DSRequestStatusProvider;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
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

import java.util.Collections;
import java.util.Map;

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
public class DSRequestStatusProviderTest {

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
	public void testGetRequestStatuses() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"L_DS_REQUEST", companyId);

		Assert.assertNotNull(
			"The L_DS_REQUEST object definition must exist when the feature " +
				"flag is enabled",
			objectDefinition);

		long fileEntryId = RandomTestUtil.randomInt();

		// L_DS_REQUEST is company-scoped, so the group ID must be 0

		_objectEntryLocalService.addObjectEntry(
			0, TestPropsValues.getUserId(),
			objectDefinition.getObjectDefinitionId(), 0,
			LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()),
			HashMapBuilder.<String, Serializable>put(
				"fileEntryId", fileEntryId
			).put(
				"providerKey", "docusign"
			).put(
				"providerRequestId", "test-" + fileEntryId
			).put(
				"requestStatus", "completed"
			).build(),
			ServiceContextTestUtil.getServiceContext(
				_group.getGroupId(), TestPropsValues.getUserId()));

		Map<Long, String> requestStatuses =
			_dsRequestStatusProvider.getRequestStatuses(
				companyId, Collections.singletonList(fileEntryId));

		Assert.assertEquals("completed", requestStatuses.get(fileEntryId));
	}

	@Test
	public void testGetRequestStatusesReturnsEmptyForMissingRequest()
		throws Exception {

		Map<Long, String> requestStatuses =
			_dsRequestStatusProvider.getRequestStatuses(
				TestPropsValues.getCompanyId(),
				Collections.singletonList(RandomTestUtil.randomLong()));

		Assert.assertTrue(requestStatuses.isEmpty());
	}

	@Inject
	private ConfigurationProvider _configurationProvider;

	@Inject
	private DSRequestStatusProvider _dsRequestStatusProvider;

	private Group _group;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

}