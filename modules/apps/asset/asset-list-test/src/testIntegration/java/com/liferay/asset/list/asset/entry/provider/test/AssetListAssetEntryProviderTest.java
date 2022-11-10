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

package com.liferay.asset.list.asset.entry.provider.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.asset.list.asset.entry.provider.AssetListAssetEntryProvider;
import com.liferay.asset.list.constants.AssetListEntryTypeConstants;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.service.AssetListEntryLocalService;
import com.liferay.asset.list.util.AssetListTestUtil;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.segments.criteria.Criteria;
import com.liferay.segments.criteria.CriteriaSerializer;
import com.liferay.segments.criteria.contributor.SegmentsCriteriaContributor;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.test.util.SegmentsTestUtil;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Eudaldo Alonso
 */
@RunWith(Arquillian.class)
public class AssetListAssetEntryProviderTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE,
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getGroupId(), TestPropsValues.getUserId());
	}

	@Test
	public void testCombineSegmentsOfDynamicCollection() throws Exception {
		_setCombinedAssetForDynamicCollections(true);

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				TestPropsValues.getUserId(), _group.getGroupId(),
				"Dynamic title", AssetListEntryTypeConstants.TYPE_DYNAMIC, null,
				_serviceContext);

		User userTest = TestPropsValues.getUser();

		String userName = "RandomName";

		User user = UserTestUtil.addUser(
			TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			StringPool.BLANK, userName + "@liferay.com", userName,
			LocaleUtil.getDefault(), userName, RandomTestUtil.randomString(),
			null, ServiceContextTestUtil.getServiceContext());

		SegmentsEntry segmentsEntry1 = _addSegmentsEntryByFirstName(
			_group.getGroupId(), userTest);
		SegmentsEntry segmentsEntry2 = _addSegmentsEntryByFirstName(
			_group.getGroupId(), user);

		JournalArticle journalArticle = _addJournalArticle(
			new long[0], TestPropsValues.getUserId());

		_addJournalArticle(new long[0], TestPropsValues.getUserId());
		_addJournalArticle(new long[0], user.getUserId());

		long[] segmentsEntryIds = {
			segmentsEntry1.getSegmentsEntryId(),
			segmentsEntry2.getSegmentsEntryId()
		};

		AssetListTestUtil.addAssetListEntrySegmentsEntryRel(
			_group.getGroupId(), assetListEntry,
			segmentsEntry1.getSegmentsEntryId(),
			_getTypeSettings(userTest.getFirstName()));

		AssetListTestUtil.addAssetListEntrySegmentsEntryRel(
			_group.getGroupId(), assetListEntry,
			segmentsEntry2.getSegmentsEntryId(), _getTypeSettings(userName));

		List<AssetEntry> assetEntries =
			_assetListAssetEntryProvider.getAssetEntries(
				assetListEntry, segmentsEntryIds);

		Assert.assertEquals(assetEntries.toString(), 3, assetEntries.size());

		AssetEntry firstAssetEntry = assetEntries.get(0);

		Assert.assertEquals(
			firstAssetEntry.getTitle(LocaleUtil.US),
			journalArticle.getTitle(LocaleUtil.US));
	}

	@Test
	public void testCombineSegmentsOfDynamicCollectionWithoutDuplications()
		throws Exception {

		_setCombinedAssetForDynamicCollections(true);

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				TestPropsValues.getUserId(), _group.getGroupId(),
				"Dynamic title", AssetListEntryTypeConstants.TYPE_DYNAMIC, null,
				_serviceContext);

		User userTest = TestPropsValues.getUser();

		SegmentsEntry segmentsEntry1 = _addSegmentsEntryByFirstName(
			_group.getGroupId(), userTest);
		SegmentsEntry segmentsEntry2 = _addSegmentsEntryByFirstName(
			_group.getGroupId(), userTest);

		JournalArticle journalArticle = _addJournalArticle(
			new long[0], TestPropsValues.getUserId());

		_addJournalArticle(new long[0], TestPropsValues.getUserId());
		_addJournalArticle(new long[0], TestPropsValues.getUserId());

		long[] segmentsEntryIds = {
			segmentsEntry1.getSegmentsEntryId(),
			segmentsEntry2.getSegmentsEntryId()
		};

		AssetListTestUtil.addAssetListEntrySegmentsEntryRel(
			_group.getGroupId(), assetListEntry,
			segmentsEntry1.getSegmentsEntryId(),
			_getTypeSettings(userTest.getFirstName()));

		AssetListTestUtil.addAssetListEntrySegmentsEntryRel(
			_group.getGroupId(), assetListEntry,
			segmentsEntry2.getSegmentsEntryId(),
			_getTypeSettings(userTest.getFirstName()));

		List<AssetEntry> assetEntries =
			_assetListAssetEntryProvider.getAssetEntries(
				assetListEntry, segmentsEntryIds);

		Assert.assertEquals(assetEntries.toString(), 3, assetEntries.size());

		AssetEntry firstAssetEntry = assetEntries.get(0);

		Assert.assertEquals(
			firstAssetEntry.getTitle(LocaleUtil.US),
			journalArticle.getTitle(LocaleUtil.US));
	}

	@Test
	public void testNotCombineSegmentsOfDynamicCollection() throws Exception {
		_setCombinedAssetForDynamicCollections(false);

		AssetListEntry assetListEntry =
			_assetListEntryLocalService.addAssetListEntry(
				TestPropsValues.getUserId(), _group.getGroupId(),
				"Dynamic title", AssetListEntryTypeConstants.TYPE_DYNAMIC, null,
				_serviceContext);

		User userTest = TestPropsValues.getUser();

		String userName = "RandomName";

		User user = UserTestUtil.addUser(
			TestPropsValues.getCompanyId(), TestPropsValues.getUserId(),
			StringPool.BLANK, userName + "@liferay.com", userName,
			LocaleUtil.getDefault(), userName, RandomTestUtil.randomString(),
			null, ServiceContextTestUtil.getServiceContext());

		SegmentsEntry segmentsEntry1 = _addSegmentsEntryByFirstName(
			_group.getGroupId(), userTest);
		SegmentsEntry segmentsEntry2 = _addSegmentsEntryByFirstName(
			_group.getGroupId(), user);

		JournalArticle journalArticle = _addJournalArticle(
			new long[0], TestPropsValues.getUserId());

		_addJournalArticle(new long[0], TestPropsValues.getUserId());
		_addJournalArticle(new long[0], user.getUserId());

		long[] segmentsEntryIds = {
			segmentsEntry1.getSegmentsEntryId(),
			segmentsEntry2.getSegmentsEntryId()
		};

		AssetListTestUtil.addAssetListEntrySegmentsEntryRel(
			_group.getGroupId(), assetListEntry,
			segmentsEntry2.getSegmentsEntryId(), _getTypeSettings(userName));

		AssetListTestUtil.addAssetListEntrySegmentsEntryRel(
			_group.getGroupId(), assetListEntry,
			segmentsEntry1.getSegmentsEntryId(),
			_getTypeSettings(userTest.getFirstName()));

		List<AssetEntry> assetEntries =
			_assetListAssetEntryProvider.getAssetEntries(
				assetListEntry, segmentsEntryIds);

		Assert.assertEquals(assetEntries.toString(), 2, assetEntries.size());

		AssetEntry firstAssetEntry = assetEntries.get(0);

		Assert.assertEquals(
			firstAssetEntry.getTitle(LocaleUtil.US),
			journalArticle.getTitle(LocaleUtil.US));
	}

	private JournalArticle _addJournalArticle(
			long[] assetCategories, long userId)
		throws Exception {

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_group.getGroupId(), userId, assetCategories);

		return JournalTestUtil.addArticle(
			_group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID, serviceContext);
	}

	private SegmentsEntry _addSegmentsEntryByFirstName(long groupId, User user)
		throws Exception {

		Criteria criteria = new Criteria();

		_segmentsCriteriaContributor.contribute(
			criteria, String.format("(firstName eq '%s')", user.getFirstName()),
			Criteria.Conjunction.AND);

		return SegmentsTestUtil.addSegmentsEntry(
			groupId, CriteriaSerializer.serialize(criteria),
			User.class.getName());
	}

	private String _getTypeSettings(String queryValue) {
		UnicodeProperties unicodeProperties = new UnicodeProperties(true);

		unicodeProperties.setProperty(
			"anyAssetType",
			String.valueOf(_portal.getClassNameId(JournalArticle.class)));
		unicodeProperties.setProperty(
			"classNameIds", JournalArticle.class.getName());
		unicodeProperties.setProperty(
			"groupIds", String.valueOf(_group.getGroupId()));
		unicodeProperties.setProperty("orderByColumn1", "modifiedDate");
		unicodeProperties.setProperty("orderByColumn2", "title");
		unicodeProperties.setProperty("orderByType1", "ASC");
		unicodeProperties.setProperty("orderByType2", "ASC");
		unicodeProperties.setProperty("queryContains0", "true");
		unicodeProperties.setProperty("queryName0", "keywords");
		unicodeProperties.setProperty("queryValues0", queryValue);

		return unicodeProperties.toString();
	}

	private void _setCombinedAssetForDynamicCollections(boolean active)
		throws Exception {

		_assetListAssetEntryConfiguration =
			_configurationAdmin.getConfiguration(
				"com.liferay.asset.list.internal.configuration." +
					"AssetListConfiguration",
				StringPool.QUESTION);

		ConfigurationTestUtil.saveConfiguration(
			_assetListAssetEntryConfiguration,
			HashMapDictionaryBuilder.<String, Object>put(
				"combineAssetsFromAllSegmentsDynamic", active
			).build());
	}

	private static Configuration _assetListAssetEntryConfiguration;

	@Inject
	private static ConfigurationAdmin _configurationAdmin;

	@Inject
	private AssetEntryLocalService _assetEntryLocalService;

	@Inject
	private AssetListAssetEntryProvider _assetListAssetEntryProvider;

	@Inject
	private AssetListEntryLocalService _assetListEntryLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private Portal _portal;

	@Inject(
		filter = "segments.criteria.contributor.key=user",
		type = SegmentsCriteriaContributor.class
	)
	private SegmentsCriteriaContributor _segmentsCriteriaContributor;

	private ServiceContext _serviceContext;

}