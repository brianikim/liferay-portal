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

package com.liferay.document.library.webserver.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.document.library.test.util.BaseWebServerTestCase;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.webdav.methods.Method;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.webserver.WebServerServlet;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Balázs Sáfrány-Kovalik
 */
@RunWith(Arquillian.class)
public class WebServerFriendlyURLTest extends BaseWebServerTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testHasFilesWithGroupIdUUIDFriendlyURL() throws Exception {
		Assert.assertFalse(
			WebServerServlet.hasFiles(
				_createMockHttpServletRequest(
					String.format(
						"/%s/%s", group.getGroupId(),
						RandomTestUtil.randomString()))));

		FileEntry fileEntry = _addFileEntry();

		Assert.assertTrue(
			WebServerServlet.hasFiles(
				_createMockHttpServletRequest(
					String.format(
						"/%s/%s", group.getGroupId(), fileEntry.getUuid()))));
	}

	private FileEntry _addFileEntry() throws Exception {
		return _dlAppLocalService.addFileEntry(
			TestPropsValues.getUserId(), group.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			RandomTestUtil.randomString(),
			ContentTypes.APPLICATION_OCTET_STREAM,
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), (byte[])null,
			ServiceContextTestUtil.getServiceContext(group.getGroupId()));
	}

	private MockHttpServletRequest _createMockHttpServletRequest(String path)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest(Method.GET, "/documents" + path);

		mockHttpServletRequest.setAttribute(
			WebKeys.USER, TestPropsValues.getUser());
		mockHttpServletRequest.setContextPath("/documents");
		mockHttpServletRequest.setPathInfo(path);
		mockHttpServletRequest.setServletPath(StringPool.BLANK);

		return mockHttpServletRequest;
	}

	@Inject
	private DLAppLocalService _dlAppLocalService;

}