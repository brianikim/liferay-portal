/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.Dictionary;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Brian I. Kim
 */
@FeatureFlag("LPD-69290")
@RunWith(Arquillian.class)
public class SaveCompanyConfigurationMVCActionCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		Configuration configuration = _getConfiguration();

		if (configuration != null) {
			_originalProperties = configuration.getProperties();
		}
	}

	@After
	public void tearDown() throws Exception {
		if (_originalProperties == null) {
			_configurationProvider.deleteCompanyConfiguration(
				DigitalSignatureConfiguration.class,
				TestPropsValues.getCompanyId());
		}
		else {
			Configuration configuration = _getConfiguration();

			configuration.update(_originalProperties);
		}
	}

	@Test
	public void testProcessAction() throws Exception {
		_processAction(true);

		Assert.assertTrue(_isProperty("embeddedViewEnabled"));

		_processAction(false);

		Assert.assertFalse(_isProperty("embeddedViewEnabled"));
	}

	@FeatureFlag(enable = false, value = "LPD-69290")
	@Test
	public void testProcessActionWhenFeatureFlagIsDisabled() throws Exception {
		_configurationProvider.saveCompanyConfiguration(
			DigitalSignatureConfiguration.class, TestPropsValues.getCompanyId(),
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

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						TestPropsValues.getCompanyId(),
						DigitalSignatureConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"embeddedViewEnabled", true
						).build())) {

			_processAction(false);

			Assert.assertTrue(_isProperty("embeddedViewEnabled"));
		}
	}

	private Configuration _getConfiguration() throws Exception {
		Configuration[] configurations = _configurationAdmin.listConfigurations(
			StringBundler.concat(
				"(&(", ConfigurationAdmin.SERVICE_FACTORYPID, StringPool.EQUAL,
				DigitalSignatureConfiguration.class.getName(),
				".scoped)(companyId=", TestPropsValues.getCompanyId(), "))"));

		if (ArrayUtil.isEmpty(configurations)) {
			return null;
		}

		return configurations[0];
	}

	private boolean _isProperty(String key) throws Exception {
		Configuration configuration = _getConfiguration();

		Dictionary<String, Object> properties = configuration.getProperties();

		return GetterUtil.getBoolean(properties.get(key));
	}

	private void _processAction(boolean enabled) throws Exception {
		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()));

		mockLiferayPortletActionRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		mockLiferayPortletActionRequest.addParameter(
			"embeddedViewEnabled", String.valueOf(enabled));

		_mvcActionCommand.processAction(
			mockLiferayPortletActionRequest,
			new MockLiferayPortletActionResponse());
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private ConfigurationAdmin _configurationAdmin;

	@Inject
	private ConfigurationProvider _configurationProvider;

	@Inject(
		filter = "mvc.command.name=/digital_signature/save_company_configuration"
	)
	private MVCActionCommand _mvcActionCommand;

	private Dictionary<String, Object> _originalProperties;

}