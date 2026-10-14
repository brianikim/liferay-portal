/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.digital.signature.configuration.DigitalSignatureConfiguration;
import com.liferay.digital.signature.configuration.DigitalSignatureConfigurationUtil;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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

	@Test
	public void testProcessAction() throws Exception {
		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						TestPropsValues.getCompanyId(),
						DigitalSignatureConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"signatureReminderEnabled", false
						).build())) {

			MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
				new MockLiferayPortletActionRequest();

			ThemeDisplay themeDisplay = new ThemeDisplay();

			themeDisplay.setCompany(
				_companyLocalService.getCompany(
					TestPropsValues.getCompanyId()));

			mockLiferayPortletActionRequest.setAttribute(
				WebKeys.THEME_DISPLAY, themeDisplay);

			mockLiferayPortletActionRequest.addParameter(
				"signatureReminderEnabled", Boolean.TRUE.toString());

			_mvcActionCommand.processAction(
				mockLiferayPortletActionRequest,
				new MockLiferayPortletActionResponse());

			DigitalSignatureConfiguration digitalSignatureConfiguration =
				DigitalSignatureConfigurationUtil.
					getDigitalSignatureConfiguration(
						TestPropsValues.getCompanyId(), 0);

			Assert.assertTrue(
				digitalSignatureConfiguration.signatureReminderEnabled());
		}
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject(
		filter = "mvc.command.name=/digital_signature/save_company_configuration"
	)
	private MVCActionCommand _mvcActionCommand;

}