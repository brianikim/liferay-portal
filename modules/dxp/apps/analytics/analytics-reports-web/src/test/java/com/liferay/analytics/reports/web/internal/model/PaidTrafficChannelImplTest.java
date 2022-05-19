/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.analytics.reports.web.internal.model;

import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author David Arques
 */
public class PaidTrafficChannelImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testToJSONObject() {
		PaidTrafficChannelImpl paidTrafficChannelImpl =
			new PaidTrafficChannelImpl(
				RandomTestUtil.randomInt(), RandomTestUtil.randomDouble());

		Assert.assertEquals(
			JSONUtil.put(
				"helpMessage", paidTrafficChannelImpl.getHelpMessageKey()
			).put(
				"name", paidTrafficChannelImpl.getName()
			).put(
				"share",
				String.format("%.1f", paidTrafficChannelImpl.getTrafficShare())
			).put(
				"title", paidTrafficChannelImpl.getName()
			).put(
				"value",
				Math.toIntExact(paidTrafficChannelImpl.getTrafficAmount())
			).toString(),
			String.valueOf(
				paidTrafficChannelImpl.toJSONObject(
					LocaleUtil.US,
					_getResourceBundle(paidTrafficChannelImpl))));
	}

	@Test
	public void testToJSONObjectWithError() {
		PaidTrafficChannelImpl paidTrafficChannelImpl =
			new PaidTrafficChannelImpl(true);

		Assert.assertEquals(
			JSONUtil.put(
				"helpMessage", paidTrafficChannelImpl.getHelpMessageKey()
			).put(
				"name", paidTrafficChannelImpl.getName()
			).put(
				"title", paidTrafficChannelImpl.getName()
			).toString(),
			String.valueOf(
				paidTrafficChannelImpl.toJSONObject(
					LocaleUtil.US,
					_getResourceBundle(paidTrafficChannelImpl))));
	}

	private ResourceBundle _getResourceBundle(TrafficChannel trafficChannel) {
		return new ResourceBundle() {

			@Override
			public Enumeration<String> getKeys() {
				return Collections.enumeration(
					Arrays.asList(
						trafficChannel.getName(),
						trafficChannel.getHelpMessageKey()));
			}

			@Override
			protected Object handleGetObject(String key) {
				return key;
			}

		};
	}

}