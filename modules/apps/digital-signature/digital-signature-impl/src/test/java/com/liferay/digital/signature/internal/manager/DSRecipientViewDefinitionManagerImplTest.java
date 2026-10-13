/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.manager;

import com.liferay.digital.signature.internal.http.DSHttp;
import com.liferay.digital.signature.model.DSRecipientViewDefinition;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Brian I. Kim
 */
public class DSRecipientViewDefinitionManagerImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@Before
	public void setUp() {
		_originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		PermissionChecker permissionChecker = Mockito.mock(
			PermissionChecker.class);

		Mockito.when(
			permissionChecker.isCompanyAdmin(Mockito.anyLong())
		).thenReturn(
			true
		);

		PermissionThreadLocal.setPermissionChecker(permissionChecker);
	}

	@After
	public void tearDown() {
		PermissionThreadLocal.setPermissionChecker(_originalPermissionChecker);
	}

	@Test
	public void testAddDSRecipientViewDefinition() throws Exception {
		_setResponseJSONObject(JSONUtil.put("url", "https://docusign.net"));

		Assert.assertEquals(
			"https://docusign.net",
			_dsRecipientViewDefinitionManagerImpl.addDSRecipientViewDefinition(
				1, 2, "envelopeId", new DSRecipientViewDefinition()));

		_setResponseJSONObject(
			JSONUtil.put(
				"errorCode", "UNKNOWN_ENVELOPE_RECIPIENT"
			).put(
				"message", "The recipient you have identified is not valid."
			));

		try {
			_dsRecipientViewDefinitionManagerImpl.addDSRecipientViewDefinition(
				1, 2, "envelopeId", new DSRecipientViewDefinition());

			Assert.fail();
		}
		catch (PortalException portalException) {
			Assert.assertEquals(
				"Unable to create a recipient view for envelope envelopeId: " +
					"The recipient you have identified is not valid.",
				portalException.getMessage());
		}
	}

	private void _setResponseJSONObject(JSONObject jsonObject) {
		DSHttp dsHttp = Mockito.mock(DSHttp.class);

		Mockito.when(
			dsHttp.post(
				Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(JSONObject.class))
		).thenReturn(
			jsonObject
		);

		ReflectionTestUtil.setFieldValue(
			_dsRecipientViewDefinitionManagerImpl, "_dsHttp", dsHttp);
	}

	private final DSRecipientViewDefinitionManagerImpl
		_dsRecipientViewDefinitionManagerImpl =
			new DSRecipientViewDefinitionManagerImpl();
	private PermissionChecker _originalPermissionChecker;

}