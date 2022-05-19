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

package com.liferay.analytics.reports.web.internal.model.util;

import com.liferay.analytics.reports.web.internal.model.AcquisitionChannel;
import com.liferay.analytics.reports.web.internal.model.DirectTrafficChannelImpl;
import com.liferay.analytics.reports.web.internal.model.ReferralTrafficChannelImpl;
import com.liferay.analytics.reports.web.internal.model.ReferringURL;
import com.liferay.analytics.reports.web.internal.model.SocialTrafficChannelImpl;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author David Arques
 */
public class TrafficChannelUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testToJSONObject() {
		String helpMessage = RandomTestUtil.randomString();
		String name = RandomTestUtil.randomString();
		String title = RandomTestUtil.randomString();
		long trafficAmount = RandomTestUtil.randomInt();
		double trafficShare = RandomTestUtil.randomDouble();

		Assert.assertEquals(
			JSONUtil.put(
				"helpMessage", helpMessage
			).put(
				"name", name
			).put(
				"share", String.format("%.1f", trafficShare)
			).put(
				"title", title
			).put(
				"value", Math.toIntExact(trafficAmount)
			).toString(),
			String.valueOf(
				TrafficChannelUtil.toJSONObject(
					false, helpMessage, name, title, trafficAmount,
					trafficShare)));
	}

	@Test
	public void testToJSONObjectWithError() {
		String helpMessage = RandomTestUtil.randomString();
		String name = RandomTestUtil.randomString();
		String title = RandomTestUtil.randomString();

		Assert.assertEquals(
			JSONUtil.put(
				"helpMessage", helpMessage
			).put(
				"name", name
			).put(
				"title", title
			).toString(),
			String.valueOf(
				TrafficChannelUtil.toJSONObject(
					true, helpMessage, name, title, 0, 0)));
	}

	@Test
	public void testToTrafficChannelWithDirectName() {
		AcquisitionChannel acquisitionChannel = new AcquisitionChannel(
			"direct", RandomTestUtil.randomInt(),
			RandomTestUtil.randomDouble());

		DirectTrafficChannelImpl directTrafficChannelImpl =
			(DirectTrafficChannelImpl)TrafficChannelUtil.toTrafficChannel(
				acquisitionChannel, null, null, null);

		Assert.assertEquals(
			acquisitionChannel.getTrafficAmount(),
			directTrafficChannelImpl.getTrafficAmount());
		Assert.assertEquals(
			acquisitionChannel.getTrafficShare(),
			directTrafficChannelImpl.getTrafficShare(), 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testToTrafficChannelWithInvalidName() {
		TrafficChannelUtil.toTrafficChannel(
			new AcquisitionChannel(
				"invalid", RandomTestUtil.randomInt(),
				RandomTestUtil.randomDouble()),
			null, null, null);
	}

	@Test
	public void testToTrafficChannelWithReferralName() {
		AcquisitionChannel acquisitionChannel = new AcquisitionChannel(
			"referral", RandomTestUtil.randomInt(),
			RandomTestUtil.randomDouble());

		ReferralTrafficChannelImpl referralTrafficChannelImpl =
			(ReferralTrafficChannelImpl)TrafficChannelUtil.toTrafficChannel(
				acquisitionChannel,
				Collections.singletonList(new ReferringURL(1, "liferay.com")),
				Collections.singletonList(
					new ReferringURL(1, "http://liferay.com/")),
				null);

		Assert.assertEquals(
			acquisitionChannel.getTrafficAmount(),
			referralTrafficChannelImpl.getTrafficAmount());
		Assert.assertEquals(
			acquisitionChannel.getTrafficShare(),
			referralTrafficChannelImpl.getTrafficShare(), 0);

		List<ReferringURL> domainReferringURLs =
			referralTrafficChannelImpl.getDomainReferringURLs();

		Assert.assertEquals(
			new ReferringURL(1, "liferay.com"), domainReferringURLs.get(0));

		List<ReferringURL> pageReferralURLs =
			referralTrafficChannelImpl.getPageReferringURLs();

		Assert.assertEquals(
			new ReferringURL(1, "http://liferay.com/"),
			pageReferralURLs.get(0));
	}

	@Test
	public void testToTrafficChannelWithSocialName() {
		AcquisitionChannel acquisitionChannel = new AcquisitionChannel(
			"social", RandomTestUtil.randomInt(),
			RandomTestUtil.randomDouble());

		SocialTrafficChannelImpl socialTrafficChannelImpl =
			(SocialTrafficChannelImpl)TrafficChannelUtil.toTrafficChannel(
				acquisitionChannel, null, null, null);

		Assert.assertEquals(
			acquisitionChannel.getTrafficAmount(),
			socialTrafficChannelImpl.getTrafficAmount());
		Assert.assertEquals(
			acquisitionChannel.getTrafficShare(),
			socialTrafficChannelImpl.getTrafficShare(), 0);
	}

}