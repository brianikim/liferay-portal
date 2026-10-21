/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.digital.signature.internal.manager;

import com.liferay.digital.signature.exception.DSEnvelopeException;
import com.liferay.digital.signature.internal.http.DSHttp;
import com.liferay.digital.signature.manager.DSCustomFieldManager;
import com.liferay.digital.signature.model.DSEnvelope;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Brian I. Kim
 */
public class DSEnvelopeManagerImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@Test
	public void testAddDSEnvelope() throws Exception {
		DSHttp dsHttp = _mockDSHttp(JSONUtil.put("envelopeId", "envelopeId"));

		ReflectionTestUtil.setFieldValue(
			_dsEnvelopeManagerImpl, "_dsCustomFieldManager",
			Mockito.mock(DSCustomFieldManager.class));

		Mockito.when(
			dsHttp.get(
				Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString())
		).thenReturn(
			JSONUtil.put(
				"errorCode", "ENVELOPE_DOES_NOT_EXIST"
			).put(
				"message", "The envelope could not be read."
			)
		);

		Mockito.when(
			dsHttp.post(
				Mockito.anyLong(), Mockito.anyLong(), Mockito.eq("envelopes"),
				Mockito.any(JSONObject.class))
		).thenReturn(
			JSONUtil.put("envelopeId", "envelopeId")
		);

		SystemException systemException = Assert.assertThrows(
			SystemException.class,
			() -> _dsEnvelopeManagerImpl.addDSEnvelope(1, 2, new DSEnvelope()));

		Assert.assertEquals(
			"Unable to read digital signature envelope envelopeId",
			systemException.getMessage());

		Mockito.verify(
			dsHttp
		).put(
			Mockito.eq(1L), Mockito.eq(2L), Mockito.eq("envelopes/envelopeId"),
			Mockito.any(JSONObject.class)
		);

		Mockito.when(
			dsHttp.put(
				Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(JSONObject.class))
		).thenReturn(
			JSONUtil.put(
				"errorCode", "ENVELOPE_CANNOT_VOID_INVALID_STATE"
			).put(
				"message", "Only envelopes in the sent state may be voided."
			)
		);

		systemException = Assert.assertThrows(
			SystemException.class,
			() -> _dsEnvelopeManagerImpl.addDSEnvelope(1, 2, new DSEnvelope()));

		Throwable[] suppressedThrowables = systemException.getSuppressed();

		Assert.assertEquals(
			Arrays.toString(suppressedThrowables), 1,
			suppressedThrowables.length);
	}

	@Test
	public void testGetDSEnvelopes() throws Exception {
		DSHttp dsHttp = Mockito.mock(DSHttp.class);

		Mockito.when(
			dsHttp.get(
				1, 2,
				"envelopes?envelope_ids=envelopeId1,envelopeId2&include=" +
					"custom_fields,recipients")
		).thenReturn(
			JSONUtil.put(
				"envelopes",
				JSONUtil.putAll(
					JSONUtil.put(
						"envelopeId", "envelopeId1"
					).put(
						"status", "sent"
					),
					JSONUtil.put(
						"envelopeId", "envelopeId2"
					).put(
						"status", "completed"
					)))
		);

		ReflectionTestUtil.setFieldValue(
			_dsEnvelopeManagerImpl, "_dsHttp", dsHttp);

		List<DSEnvelope> dsEnvelopes = _dsEnvelopeManagerImpl.getDSEnvelopes(
			1, 2, "envelopeId1", "envelopeId2");

		Assert.assertEquals(dsEnvelopes.toString(), 2, dsEnvelopes.size());

		DSEnvelope dsEnvelope1 = dsEnvelopes.get(0);

		Assert.assertEquals("envelopeId1", dsEnvelope1.getDSEnvelopeId());

		DSEnvelope dsEnvelope2 = dsEnvelopes.get(1);

		Assert.assertEquals("envelopeId2", dsEnvelope2.getDSEnvelopeId());

		Mockito.when(
			dsHttp.get(
				Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString())
		).thenReturn(
			JSONUtil.put(
				"errorCode", "INVALID_REQUEST_PARAMETER"
			).put(
				"message",
				"The request contained at least one invalid parameter."
			)
		);

		PortalException portalException = Assert.assertThrows(
			PortalException.class,
			() -> _dsEnvelopeManagerImpl.getDSEnvelopes(1, 2, "envelopeId1"));

		Assert.assertEquals(
			"Unable to list digital signature envelopes: The request " +
				"contained at least one invalid parameter.",
			portalException.getMessage());
	}

	@Test
	public void testVoidDSEnvelope() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		String dsEnvelopeId = RandomTestUtil.randomString();
		long groupId = RandomTestUtil.randomLong();
		String reason = RandomTestUtil.randomString();

		DSHttp dsHttp = _mockDSHttp(JSONUtil.put("envelopeId", dsEnvelopeId));

		_dsEnvelopeManagerImpl.voidDSEnvelope(
			companyId, groupId, dsEnvelopeId, reason);

		ArgumentCaptor<JSONObject> argumentCaptor = ArgumentCaptor.forClass(
			JSONObject.class);

		Mockito.verify(
			dsHttp
		).put(
			Mockito.eq(companyId), Mockito.eq(groupId),
			Mockito.eq("envelopes/" + dsEnvelopeId), argumentCaptor.capture()
		);

		JSONObject jsonObject = argumentCaptor.getValue();

		Assert.assertEquals("voided", jsonObject.getString("status"));
		Assert.assertEquals(reason, jsonObject.getString("voidedReason"));

		_mockDSHttp(
			JSONUtil.put(
				"errorCode", "ENVELOPE_CANNOT_VOID_INVALID_STATE"
			).put(
				"message", "Only envelopes in the sent state may be voided."
			));

		DSEnvelopeException.NotVoidable notVoidable = Assert.assertThrows(
			DSEnvelopeException.NotVoidable.class,
			() -> _dsEnvelopeManagerImpl.voidDSEnvelope(
				companyId, groupId, dsEnvelopeId, reason));

		Assert.assertEquals(
			StringBundler.concat(
				"Unable to void digital signature envelope ", dsEnvelopeId,
				": Only envelopes in the sent state may be voided."),
			notVoidable.getMessage());

		_mockDSHttp(
			JSONUtil.put(
				"errorCode", "USER_AUTHENTICATION_FAILED"
			).put(
				"message", "One or both of Username and Password are invalid."
			));

		PortalException portalException = Assert.assertThrows(
			PortalException.class,
			() -> _dsEnvelopeManagerImpl.voidDSEnvelope(
				companyId, groupId, dsEnvelopeId, reason));

		Assert.assertFalse(
			portalException instanceof DSEnvelopeException.NotVoidable);
		Assert.assertEquals(
			StringBundler.concat(
				"Unable to void digital signature envelope ", dsEnvelopeId,
				": One or both of Username and Password are invalid."),
			portalException.getMessage());
	}

	private DSHttp _mockDSHttp(JSONObject jsonObject) {
		DSHttp dsHttp = Mockito.mock(DSHttp.class);

		Mockito.when(
			dsHttp.put(
				Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(JSONObject.class))
		).thenReturn(
			jsonObject
		);

		ReflectionTestUtil.setFieldValue(
			_dsEnvelopeManagerImpl, "_dsHttp", dsHttp);

		return dsHttp;
	}

	private final DSEnvelopeManagerImpl _dsEnvelopeManagerImpl =
		new DSEnvelopeManagerImpl();

}