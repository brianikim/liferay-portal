/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.mapping.form.web.internal.display.context;

import com.liferay.dynamic.data.mapping.form.field.type.DDMFormFieldTypeServicesTracker;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormRenderer;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormRenderingContext;
import com.liferay.dynamic.data.mapping.form.values.factory.DDMFormValuesFactory;
import com.liferay.dynamic.data.mapping.form.web.internal.configuration.DDMFormWebConfiguration;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceSettings;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceVersion;
import com.liferay.dynamic.data.mapping.model.DDMFormSuccessPageSettings;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.impl.DDMFormInstanceImpl;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceLocalService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordVersionLocalService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceVersionLocalService;
import com.liferay.dynamic.data.mapping.storage.DDMStorageAdapterTracker;
import com.liferay.dynamic.data.mapping.util.DDMFormValuesMerger;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalService;
import com.liferay.portal.kernel.service.permission.PortletPermissionUtil;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.util.PropsImpl;
import com.liferay.portletmvc4spring.test.mock.web.portlet.MockRenderRequest;
import com.liferay.portletmvc4spring.test.mock.web.portlet.MockRenderResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;

import javax.servlet.http.HttpServletRequest;

import org.hamcrest.CoreMatchers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Adam Brandizzi
 */
@PrepareForTest({LocaleUtil.class, PortletPermissionUtil.class})
@RunWith(PowerMockRunner.class)
public class DDMFormDisplayContextTest extends PowerMockito {

	@BeforeClass
	public static void setUpClass() throws Exception {
		PropsUtil.setProps(new PropsImpl());
	}

	@Before
	public void setUp() throws PortalException {
		setUpLanguageUtil();
		setUpLocaleUtil();
		setUpPortalUtil();
	}

	@Test
	public void testAutosaveWithDefaultUser() throws Exception {
		MockRenderRequest renderRequest = mockRenderRequest();

		ThemeDisplay themeDisplay = mock(ThemeDisplay.class);

		renderRequest.setAttribute(WebKeys.THEME_DISPLAY, themeDisplay);

		PortletDisplay portletDisplay = mock(PortletDisplay.class);

		when(
			portletDisplay.getPortletResource()
		).thenReturn(
			null
		);

		when(
			themeDisplay.getPortletDisplay()
		).thenReturn(
			portletDisplay
		);

		User user = mock(User.class);

		when(
			user.isDefaultUser()
		).thenReturn(
			Boolean.TRUE
		);

		when(
			themeDisplay.getUser()
		).thenReturn(
			user
		);

		DDMFormDisplayContext ddmFormDisplayContext =
			createDDMFormDisplayContext(renderRequest);

		Assert.assertFalse(ddmFormDisplayContext.isAutosaveEnabled());
	}

	@Test
	public void testAutosaveWithNondefaultUser1() throws Exception {
		RenderRequest renderRequest =
			mockRenderRequestAutosaveWithNondefaultUser();

		DDMFormInstanceSettings ddmFormInstanceSettings =
			mockDDMFormInstanceSettingsAutosaveWithNondefaultUser();

		when(
			ddmFormInstanceSettings.autosaveEnabled()
		).thenReturn(
			Boolean.FALSE
		);

		DDMFormDisplayContext ddmFormDisplayContext =
			createDDMFormDisplayContext(renderRequest);

		Assert.assertFalse(ddmFormDisplayContext.isAutosaveEnabled());
	}

	@Test
	public void testAutosaveWithNondefaultUser2() throws Exception {
		RenderRequest renderRequest =
			mockRenderRequestAutosaveWithNondefaultUser();

		DDMFormInstanceSettings ddmFormInstanceSettings =
			mockDDMFormInstanceSettingsAutosaveWithNondefaultUser();

		when(
			ddmFormInstanceSettings.autosaveEnabled()
		).thenReturn(
			Boolean.TRUE
		);

		when(
			_ddmFormWebConfiguration.autosaveInterval()
		).thenReturn(
			1
		);

		DDMFormDisplayContext ddmFormDisplayContext =
			createDDMFormDisplayContext(renderRequest);

		Assert.assertTrue(ddmFormDisplayContext.isAutosaveEnabled());
	}

	@Test
	public void testAutosaveWithNondefaultUser3() throws Exception {
		RenderRequest renderRequest =
			mockRenderRequestAutosaveWithNondefaultUser();

		DDMFormInstanceSettings ddmFormInstanceSettings =
			mockDDMFormInstanceSettingsAutosaveWithNondefaultUser();

		when(
			ddmFormInstanceSettings.autosaveEnabled()
		).thenReturn(
			Boolean.TRUE
		);

		when(
			_ddmFormWebConfiguration.autosaveInterval()
		).thenReturn(
			0
		);

		DDMFormDisplayContext ddmFormDisplayContext =
			createDDMFormDisplayContext(renderRequest);

		Assert.assertFalse(ddmFormDisplayContext.isAutosaveEnabled());
	}

	@Test
	public void testDDMFormRenderingContextLocaleIsThemeDisplayLocale()
		throws Exception {

		DDMFormDisplayContext ddmFormDisplayContext =
			createDDMFormDisplayContext();

		Locale defaultLocale = LocaleUtil.BRAZIL;

		Set<Locale> availableLocales = new HashSet<>();

		availableLocales.add(defaultLocale);
		availableLocales.add(LocaleUtil.SPAIN);

		DDMForm ddmForm = createDDMForm(availableLocales, defaultLocale);

		_request.addParameter(
			"languageId", LocaleUtil.toLanguageId(LocaleUtil.SPAIN));

		DDMFormRenderingContext ddmFormRenderingContext =
			ddmFormDisplayContext.createDDMFormRenderingContext(ddmForm);

		Assert.assertEquals(
			LocaleUtil.SPAIN, ddmFormRenderingContext.getLocale());
	}

	@Test
	public void testGetFormInstanceWithMissingSettings() throws Exception {
		DDMFormDisplayContext ddmFormDisplayContext =
			createDDMFormDisplayContext();

		DDMFormInstance ddmFormInstance = spy(new DDMFormInstanceImpl());

		String expectedSettings = StringUtil.randomString();

		ddmFormInstance.setSettings(expectedSettings);

		when(
			_ddmFormInstanceService.fetchFormInstance(Matchers.anyLong())
		).thenReturn(
			ddmFormInstance
		);

		when(
			_ddmFormInstanceVersion.getSettings()
		).thenReturn(
			StringPool.BLANK
		);

		ddmFormInstance = ddmFormDisplayContext.getFormInstance();

		Assert.assertThat(
			ddmFormInstance.getSettings(), CoreMatchers.is(expectedSettings));
	}

	@Test
	public void testIsFormAvailableForGuest() throws Exception {
		DDMFormInstance ddmFormInstance = mockDDMFormInstance();

		when(
			_ddmFormInstanceLocalService.fetchFormInstance(Matchers.anyLong())
		).thenReturn(
			ddmFormInstance
		);

		when(
			_ddmFormInstanceService.fetchFormInstance(Matchers.anyLong())
		).thenReturn(
			null
		);

		DDMFormDisplayContext ddmFormDisplayContext =
			createDDMFormDisplayContext();

		Assert.assertFalse(ddmFormDisplayContext.isFormAvailable());
	}

	@Test
	public void testIsFormAvailableForLoggedUser() throws Exception {
		DDMFormInstance ddmFormInstance = mockDDMFormInstance();

		when(
			_ddmFormInstanceLocalService.fetchFormInstance(Matchers.anyLong())
		).thenReturn(
			ddmFormInstance
		);

		when(
			_ddmFormInstanceService.fetchFormInstance(Matchers.anyLong())
		).thenReturn(
			ddmFormInstance
		);

		DDMFormDisplayContext ddmFormDisplayContext =
			createDDMFormDisplayContext();

		Assert.assertTrue(ddmFormDisplayContext.isFormAvailable());
	}

	@Test
	public void testIsSharedFormWithoutPortletSession() throws Exception {
		MockRenderRequest renderRequest = mockRenderRequest();

		Assert.assertNull(renderRequest.getPortletSession(false));

		renderRequest.setParameter("shared", Boolean.TRUE.toString());

		DDMFormDisplayContext createDDMFormDisplayContext =
			createDDMFormDisplayContext(renderRequest);

		Assert.assertTrue(createDDMFormDisplayContext.isFormShared());
	}

	@Test
	public void testIsSharedFormWithPortletSession() throws Exception {
		MockRenderRequest renderRequest = mockRenderRequest();

		PortletSession portletSession = renderRequest.getPortletSession(true);

		Assert.assertNotNull(portletSession);

		portletSession.setAttribute("shared", Boolean.TRUE);

		DDMFormDisplayContext createDDMFormDisplayContext =
			createDDMFormDisplayContext(renderRequest);

		Assert.assertTrue(createDDMFormDisplayContext.isFormShared());
	}

	@Test
	public void testIsShowIconInEditMode() throws Exception {
		_mockHttpServletRequest.addParameter("p_l_mode", Constants.EDIT);

		DDMFormDisplayContext ddmFormDisplayContext = createSpy(
			false, false, false);

		Assert.assertFalse(ddmFormDisplayContext.isShowConfigurationIcon());
	}

	@Test
	public void testIsShowIconInPreview() throws Exception {
		DDMFormDisplayContext ddmFormDisplayContext = createSpy(
			false, true, false);

		Assert.assertFalse(ddmFormDisplayContext.isShowConfigurationIcon());
	}

	@Test
	public void testIsShowIconWithPermission() throws Exception {
		mockPortletPermissionUtil();

		DDMFormDisplayContext ddmFormDisplayContext = createSpy(
			false, false, true);

		Assert.assertTrue(ddmFormDisplayContext.isShowConfigurationIcon());
	}

	@Test
	public void testIsShowIconWithSharedForm() throws Exception {
		DDMFormDisplayContext ddmFormDisplayContext = createSpy(
			true, false, true);

		Assert.assertFalse(ddmFormDisplayContext.isShowConfigurationIcon());
	}

	@Test
	public void testIsShowSuccessPage() throws Exception {
		mockDDMFormInstance(mock(DDMFormInstanceSettings.class));

		MockRenderRequest mockRenderRequest = mockRenderRequest();

		SessionMessages.add(
			mockRenderRequest,
			SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_SUCCESS_MESSAGE);

		DDMFormDisplayContext ddmFormDisplayContext =
			createDDMFormDisplayContext(mockRenderRequest);

		Assert.assertTrue(ddmFormDisplayContext.isShowSuccessPage());
	}

	@Test
	public void testIsShowSuccessPageWithRedirectURL() throws Exception {
		DDMFormInstanceSettings ddmFormInstanceSettings = mock(
			DDMFormInstanceSettings.class);

		when(
			ddmFormInstanceSettings.redirectURL()
		).thenReturn(
			"http://localhost:8080/web/forms/shared/-/form/123"
		);

		mockDDMFormInstance(ddmFormInstanceSettings);

		MockRenderRequest mockRenderRequest = mockRenderRequest();

		SessionMessages.add(
			mockRenderRequest,
			SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_SUCCESS_MESSAGE);

		DDMFormDisplayContext ddmFormDisplayContext =
			createDDMFormDisplayContext(mockRenderRequest);

		Assert.assertFalse(ddmFormDisplayContext.isShowSuccessPage());
	}

	protected DDMForm createDDMForm(
		Set<Locale> availableLocales, Locale locale) {

		DDMForm ddmForm = new DDMForm();

		ddmForm.setAvailableLocales(availableLocales);

		DDMFormSuccessPageSettings ddmFormSuccessPageSettings =
			new DDMFormSuccessPageSettings();

		ddmFormSuccessPageSettings.setEnabled(true);

		ddmForm.setDDMFormSuccessPageSettings(ddmFormSuccessPageSettings);

		ddmForm.setDefaultLocale(locale);

		return ddmForm;
	}

	protected DDMFormDisplayContext createDDMFormDisplayContext()
		throws PortalException {

		return createDDMFormDisplayContext(mockRenderRequest());
	}

	protected DDMFormDisplayContext createDDMFormDisplayContext(
			RenderRequest renderRequest)
		throws PortalException {

		return new DDMFormDisplayContext(
			renderRequest, new MockRenderResponse(),
			mock(DDMFormFieldTypeServicesTracker.class),
			_ddmFormInstanceLocalService,
			mock(DDMFormInstanceRecordService.class),
			mock(DDMFormInstanceRecordVersionLocalService.class),
			_ddmFormInstanceService, _mockDDMFormInstanceVersionLocalService(),
			mock(DDMFormRenderer.class), mock(DDMFormValuesFactory.class),
			mock(DDMFormValuesMerger.class), _ddmFormWebConfiguration,
			mock(DDMStorageAdapterTracker.class), mock(GroupLocalService.class),
			new JSONFactoryImpl(),
			mock(WorkflowDefinitionLinkLocalService.class), mock(Portal.class));
	}

	protected DDMFormDisplayContext createSpy(
			boolean formShared, boolean preview, boolean sharedURL)
		throws PortalException {

		DDMFormDisplayContext ddmFormDisplayContext = spy(
			createDDMFormDisplayContext());

		Mockito.doReturn(
			formShared
		).when(
			ddmFormDisplayContext
		).isFormShared();

		Mockito.doReturn(
			preview
		).when(
			ddmFormDisplayContext
		).isPreview();

		Mockito.doReturn(
			sharedURL
		).when(
			ddmFormDisplayContext
		).isSharedURL();

		return ddmFormDisplayContext;
	}

	protected DDMFormInstance mockDDMFormInstance() throws PortalException {
		DDMFormInstance formInstance = mock(DDMFormInstance.class);

		DDMFormInstanceSettings formInstanceSettings = mock(
			DDMFormInstanceSettings.class);

		when(
			formInstance.getSettingsModel()
		).thenReturn(
			formInstanceSettings
		);

		return formInstance;
	}

	protected void mockDDMFormInstance(
			DDMFormInstanceSettings ddmFormInstanceSettings)
		throws Exception {

		DDMFormInstance ddmFormInstance = mock(DDMFormInstance.class);

		when(
			ddmFormInstance.getSettingsModel()
		).thenReturn(
			ddmFormInstanceSettings
		);

		DDMStructure ddmStructure = _mockDDMStructure();

		when(
			ddmFormInstance.getStructure()
		).thenReturn(
			ddmStructure
		);

		when(
			_ddmFormInstanceService.fetchFormInstance(Matchers.anyLong())
		).thenReturn(
			ddmFormInstance
		);
	}

	protected DDMFormInstanceSettings
			mockDDMFormInstanceSettingsAutosaveWithNondefaultUser()
		throws Exception {

		DDMFormInstance ddmFormInstance = mock(DDMFormInstance.class);

		DDMFormInstanceSettings ddmFormInstanceSettings = mock(
			DDMFormInstanceSettings.class);

		when(
			ddmFormInstance.getSettingsModel()
		).thenReturn(
			ddmFormInstanceSettings
		);

		when(
			_ddmFormInstanceService.fetchFormInstance(Matchers.anyLong())
		).thenReturn(
			ddmFormInstance
		);

		return ddmFormInstanceSettings;
	}

	protected void mockPortletPermissionUtil() throws PortalException {
		mockStatic(PortletPermissionUtil.class);

		when(
			PortletPermissionUtil.contains(
				Matchers.any(PermissionChecker.class),
				Matchers.any(Layout.class), Matchers.anyString(),
				Matchers.anyString())
		).thenReturn(
			true
		);
	}

	protected MockRenderRequest mockRenderRequest() {
		MockRenderRequest mockRenderRequest = new MockRenderRequest();

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setLayout(mock(Layout.class));
		themeDisplay.setLocale(LocaleUtil.SPAIN);

		mockRenderRequest.setAttribute(WebKeys.THEME_DISPLAY, themeDisplay);

		return mockRenderRequest;
	}

	protected RenderRequest mockRenderRequestAutosaveWithNondefaultUser()
		throws Exception {

		MockRenderRequest renderRequest = mockRenderRequest();

		ThemeDisplay themeDisplay = mock(ThemeDisplay.class);

		renderRequest.setAttribute(WebKeys.THEME_DISPLAY, themeDisplay);

		User user = mock(User.class);

		when(
			user.isDefaultUser()
		).thenReturn(
			Boolean.FALSE
		);

		when(
			themeDisplay.getUser()
		).thenReturn(
			user
		);

		PortletDisplay portletDisplay = mock(PortletDisplay.class);

		when(
			portletDisplay.getPortletResource()
		).thenReturn(
			null
		);

		when(
			themeDisplay.getPortletDisplay()
		).thenReturn(
			portletDisplay
		);

		return renderRequest;
	}

	protected void setUpLanguageUtil() {
		when(
			_language.getLanguageId(Matchers.eq(_request))
		).thenReturn(
			"es_ES"
		);

		_whenLanguageIsAvailableLocale(LocaleUtil.BRAZIL);
		_whenLanguageIsAvailableLocale(LocaleUtil.SPAIN);

		LanguageUtil languageUtil = new LanguageUtil();

		languageUtil.setLanguage(_language);
	}

	protected void setUpLocaleUtil() {
		mockStatic(LocaleUtil.class);

		when(
			LocaleUtil.fromLanguageId("es_ES")
		).thenReturn(
			LocaleUtil.SPAIN
		);

		when(
			LocaleUtil.fromLanguageId("pt_BR")
		).thenReturn(
			LocaleUtil.BRAZIL
		);
	}

	protected void setUpPortalUtil() {
		PortalUtil portalUtil = new PortalUtil();

		portalUtil.setPortal(mock(Portal.class));

		when(
			PortalUtil.getHttpServletRequest(Matchers.any(RenderRequest.class))
		).thenReturn(
			_request
		);

		when(
			PortalUtil.getLiferayPortletRequest(
				Matchers.any(RenderRequest.class))
		).thenReturn(
			Mockito.mock(LiferayPortletRequest.class)
		);

		when(
			PortalUtil.getOriginalServletRequest(
				Matchers.any(HttpServletRequest.class))
		).thenReturn(
			_mockHttpServletRequest
		);
	}

	private DDMForm _createDDMForm(
		Set<Locale> availableLocales, Locale locale) {

		DDMForm ddmForm = new DDMForm();

		ddmForm.setAvailableLocales(availableLocales);

		DDMFormSuccessPageSettings ddmFormSuccessPageSettings =
			new DDMFormSuccessPageSettings();

		ddmFormSuccessPageSettings.setEnabled(true);

		ddmForm.setDDMFormSuccessPageSettings(ddmFormSuccessPageSettings);

		ddmForm.setDefaultLocale(locale);

		return ddmForm;
	}

	private DDMFormInstanceVersionLocalService
			_mockDDMFormInstanceVersionLocalService()
		throws PortalException {

		when(
			_ddmFormInstanceVersionLocalService.getLatestFormInstanceVersion(
				Matchers.anyLong(), Matchers.anyInt())
		).thenReturn(
			_ddmFormInstanceVersion
		);

		return _ddmFormInstanceVersionLocalService;
	}

	private DDMStructure _mockDDMStructure() throws Exception {
		DDMStructure ddmStructure = mock(DDMStructure.class);

		Locale defaultLocale = LocaleUtil.fromLanguageId(_DEFAULT_LANGUAGE_ID);

		DDMForm ddmForm = _createDDMForm(
			new HashSet<>(Arrays.asList(defaultLocale)), defaultLocale);

		when(
			ddmStructure.getDDMForm()
		).thenReturn(
			ddmForm
		);

		return ddmStructure;
	}

	private void _whenLanguageIsAvailableLocale(Locale locale) {
		when(
			_language.isAvailableLocale(Matchers.eq(locale))
		).thenReturn(
			true
		);

		when(
			_language.isAvailableLocale(
				Matchers.eq(LocaleUtil.toLanguageId(locale)))
		).thenReturn(
			true
		);
	}

	private static final String _DEFAULT_LANGUAGE_ID = "es_ES";

	@Mock
	private DDMFormInstanceLocalService _ddmFormInstanceLocalService;

	@Mock
	private DDMFormInstanceService _ddmFormInstanceService;

	@Mock
	private DDMFormInstanceVersion _ddmFormInstanceVersion;

	@Mock
	private DDMFormInstanceVersionLocalService
		_ddmFormInstanceVersionLocalService;

	@Mock
	private DDMFormWebConfiguration _ddmFormWebConfiguration;

	@Mock
	private Language _language;

	private final MockHttpServletRequest _mockHttpServletRequest =
		new MockHttpServletRequest();

	@Mock
	private MockHttpServletRequest _request;

}