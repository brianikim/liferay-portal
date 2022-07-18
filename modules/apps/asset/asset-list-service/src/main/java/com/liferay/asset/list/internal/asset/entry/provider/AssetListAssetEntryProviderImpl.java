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

package com.liferay.asset.list.internal.asset.entry.provider;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.model.ClassType;
import com.liferay.asset.kernel.model.ClassTypeReader;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.asset.kernel.service.persistence.AssetEntryQuery;
import com.liferay.asset.list.asset.entry.provider.AssetListAssetEntryProvider;
import com.liferay.asset.list.asset.entry.query.processor.AssetListAssetEntryQueryProcessor;
import com.liferay.asset.list.constants.AssetListEntryTypeConstants;
import com.liferay.asset.list.internal.configuration.AssetListConfiguration;
import com.liferay.asset.list.internal.dynamic.data.mapping.util.DDMIndexerUtil;
import com.liferay.asset.list.model.AssetListEntry;
import com.liferay.asset.list.model.AssetListEntryAssetEntryRel;
import com.liferay.asset.list.model.AssetListEntryAssetEntryRelModel;
import com.liferay.asset.list.model.AssetListEntrySegmentsEntryRel;
import com.liferay.asset.list.model.AssetListEntrySegmentsEntryRelModel;
import com.liferay.asset.list.service.AssetListEntryAssetEntryRelLocalService;
import com.liferay.asset.list.service.AssetListEntrySegmentsEntryRelLocalService;
import com.liferay.asset.util.AssetHelper;
import com.liferay.asset.util.AssetRendererFactoryClassProvider;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalService;
import com.liferay.dynamic.data.mapping.kernel.DDMStructure;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.segments.constants.SegmentsEntryConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Sarai Díaz
 */
@Component(
	configurationPid = "com.liferay.asset.list.internal.configuration.AssetListConfiguration",
	immediate = true, service = AssetListAssetEntryProvider.class
)
public class AssetListAssetEntryProviderImpl
	implements AssetListAssetEntryProvider {

	@Override
	public List<AssetEntry> getAssetEntries(
		AssetListEntry assetListEntry, long segmentsEntryId) {

		return getAssetEntries(
			assetListEntry, segmentsEntryId, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS);
	}

	@Override
	public List<AssetEntry> getAssetEntries(
		AssetListEntry assetListEntry, long segmentsEntryId, int start,
		int end) {

		return getAssetEntries(
			assetListEntry, new long[] {segmentsEntryId}, start, end);
	}

	@Override
	public List<AssetEntry> getAssetEntries(
		AssetListEntry assetListEntry, long[] segmentsEntryIds) {

		return getAssetEntries(
			assetListEntry, segmentsEntryIds, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS);
	}

	@Override
	public List<AssetEntry> getAssetEntries(
		AssetListEntry assetListEntry, long[] segmentsEntryIds, int start,
		int end) {

		return getAssetEntries(
			assetListEntry, segmentsEntryIds, StringPool.BLANK, start, end);
	}

	@Override
	public List<AssetEntry> getAssetEntries(
		AssetListEntry assetListEntry, long[] segmentsEntryIds, String userId) {

		return getAssetEntries(
			assetListEntry, segmentsEntryIds, userId, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS);
	}

	@Override
	public List<AssetEntry> getAssetEntries(
		AssetListEntry assetListEntry, long[] segmentsEntryIds, String userId,
		int start, int end) {

		if (Objects.equals(
				assetListEntry.getType(),
				AssetListEntryTypeConstants.TYPE_MANUAL)) {

			return _getManualAssetEntries(
				assetListEntry, segmentsEntryIds, start, end);
		}

		return _getDynamicAssetEntries(
			assetListEntry, segmentsEntryIds, userId, start, end);
	}

	@Override
	public int getAssetEntriesCount(
		AssetListEntry assetListEntry, long segmentsEntryId) {

		return getAssetEntriesCount(
			assetListEntry, new long[] {segmentsEntryId});
	}

	@Override
	public int getAssetEntriesCount(
		AssetListEntry assetListEntry, long[] segmentsEntryIds) {

		return getAssetEntriesCount(
			assetListEntry, segmentsEntryIds, StringPool.BLANK);
	}

	@Override
	public int getAssetEntriesCount(
		AssetListEntry assetListEntry, long[] segmentsEntryIds, String userId) {

		if (Objects.equals(
				assetListEntry.getType(),
				AssetListEntryTypeConstants.TYPE_MANUAL)) {

			return _assetListEntryAssetEntryRelLocalService.
				getAssetListEntryAssetEntryRelsCount(
					assetListEntry.getAssetListEntryId(),
					_getFirstSegmentsEntryId(assetListEntry, segmentsEntryIds),
					true);
		}

		LongStream longStream = Arrays.stream(
			_getCombinedSegmentsEntryIds(assetListEntry, segmentsEntryIds));

		return _searchCount(
			assetListEntry.getCompanyId(),
			longStream.mapToObj(
				segmentsEntryId -> getAssetEntryQuery(
					assetListEntry, segmentsEntryId, userId)
			).collect(
				Collectors.toList()
			));
	}

	@Override
	public AssetEntryQuery getAssetEntryQuery(
		AssetListEntry assetListEntry, long segmentsEntryId) {

		return getAssetEntryQuery(
			assetListEntry, segmentsEntryId, StringPool.BLANK);
	}

	@Override
	public AssetEntryQuery getAssetEntryQuery(
		AssetListEntry assetListEntry, long[] segmentsEntryIds) {

		return getAssetEntryQuery(
			assetListEntry, segmentsEntryIds, StringPool.BLANK);
	}

	@Override
	public AssetEntryQuery getAssetEntryQuery(
		AssetListEntry assetListEntry, long[] segmentsEntryIds, String userId) {

		return getAssetEntryQuery(
			assetListEntry,
			_getFirstSegmentsEntryId(assetListEntry, segmentsEntryIds), userId);
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties)
		throws ConfigurationException {

		_assetListConfiguration = ConfigurableUtil.createConfigurable(
			AssetListConfiguration.class, properties);
	}

	protected AssetEntryQuery getAssetEntryQuery(
		AssetListEntry assetListEntry, long segmentsEntryId, String userId) {

		UnicodeProperties unicodeProperties = new UnicodeProperties(true);

		unicodeProperties.fastLoad(
			assetListEntry.getTypeSettings(segmentsEntryId));

		return _createAssetEntryQuery(
			assetListEntry, userId, unicodeProperties);
	}

	protected AssetEntryQuery getAssetEntryQuery(
		AssetListEntry assetListEntry, long[] segmentsEntryIds, String userId,
		int end, int start) {

		LongStream longStream = Arrays.stream(segmentsEntryIds);

		List<String> typeSettings = longStream.mapToObj(
			assetListEntry::getTypeSettings
		).collect(
			Collectors.toList()
		);

		UnicodeProperties unicodeProperties = new UnicodeProperties(true);

		unicodeProperties.setProperty(
			"anyAssetType", StringUtil.merge(typeSettings, StringPool.COMMA));

		AssetEntryQuery assetEntryQuery = _createAssetEntryQuery(
			assetListEntry, userId, unicodeProperties);

		assetEntryQuery.setEnd(end);
		assetEntryQuery.setStart(start);

		return assetEntryQuery;
	}

	private AssetEntryQuery _createAssetEntryQuery(
		AssetListEntry assetListEntry, String userId,
		UnicodeProperties unicodeProperties) {

		AssetEntryQuery assetEntryQuery = new AssetEntryQuery();

		_setCategoriesAndTagsAndKeywords(
			assetEntryQuery, unicodeProperties,
			_getAssetCategoryIds(unicodeProperties),
			_getAssetTagNames(unicodeProperties),
			_getKeywords(unicodeProperties));

		long[] groupIds = GetterUtil.getLongValues(
			StringUtil.split(
				unicodeProperties.getProperty("groupIds", StringPool.BLANK)));

		if (ArrayUtil.isEmpty(groupIds)) {
			groupIds = new long[] {assetListEntry.getGroupId()};
		}

		assetEntryQuery.setGroupIds(groupIds);

		boolean anyAssetType = GetterUtil.getBoolean(
			unicodeProperties.getProperty("anyAssetType", null), true);
		long[] availableClassNameIds =
			AssetRendererFactoryRegistryUtil.getClassNameIds(
				assetListEntry.getCompanyId());
		long[] classTypeIds = {};

		if (!anyAssetType) {
			long[] classNameIds = _getClassNameIds(
				unicodeProperties, availableClassNameIds);

			assetEntryQuery.setClassNameIds(classNameIds);

			for (long classNameId : classNameIds) {
				classTypeIds = ArrayUtil.append(
					classTypeIds,
					_getClassTypeIds(
						assetListEntry, unicodeProperties,
						_portal.getClassName(classNameId)));
			}

			assetEntryQuery.setClassTypeIds(classTypeIds);
		}
		else {
			assetEntryQuery.setClassNameIds(availableClassNameIds);
		}

		String ddmStructureFieldName = unicodeProperties.getProperty(
			"ddmStructureFieldName");

		String ddmStructureFieldValue = unicodeProperties.getProperty(
			"ddmStructureFieldValue");

		if (Validator.isNotNull(ddmStructureFieldName) &&
			Validator.isNotNull(ddmStructureFieldValue) &&
			(classTypeIds.length == 1)) {

			DLFileEntryType dlFileEntryType =
				_dlFileEntryTypeLocalService.fetchFileEntryType(
					classTypeIds[0]);

			if (dlFileEntryType != null) {
				List<DDMStructure> ddmStructures =
					dlFileEntryType.getDDMStructures();

				if (!ddmStructures.isEmpty()) {
					DDMStructure ddmStructure = ddmStructures.get(0);

					assetEntryQuery.setAttribute(
						"ddmStructureFieldName",
						DDMIndexerUtil.encodeName(
							ddmStructure.getStructureId(),
							ddmStructureFieldName,
							LocaleUtil.getMostRelevantLocale()));
				}
				else {
					assetEntryQuery.setAttribute(
						"ddmStructureFieldName",
						DDMIndexerUtil.encodeName(
							classTypeIds[0], ddmStructureFieldName,
							LocaleUtil.getMostRelevantLocale()));
				}
			}
			else {
				assetEntryQuery.setAttribute(
					"ddmStructureFieldName",
					DDMIndexerUtil.encodeName(
						classTypeIds[0], ddmStructureFieldName,
						LocaleUtil.getMostRelevantLocale()));
			}

			assetEntryQuery.setAttribute(
				"ddmStructureFieldValue", ddmStructureFieldValue);
		}

		String orderByColumn1 = GetterUtil.getString(
			unicodeProperties.getProperty("orderByColumn1", "priority"));

		assetEntryQuery.setOrderByCol1(orderByColumn1);

		String orderByColumn2 = GetterUtil.getString(
			unicodeProperties.getProperty("orderByColumn2", "modifiedDate"));

		assetEntryQuery.setOrderByCol2(orderByColumn2);

		String orderByType1 = GetterUtil.getString(
			unicodeProperties.getProperty("orderByType1", "ASC"));

		assetEntryQuery.setOrderByType1(orderByType1);

		String orderByType2 = GetterUtil.getString(
			unicodeProperties.getProperty("orderByType2", "ASC"));

		assetEntryQuery.setOrderByType2(orderByType2);

		_processAssetEntryQuery(
			assetListEntry.getCompanyId(), userId, unicodeProperties,
			assetEntryQuery);

		return assetEntryQuery;
	}

	private long[] _filterAssetCategoryIds(long[] assetCategoryIds) {
		List<Long> assetCategoryIdsList = new ArrayList<>();

		for (long assetCategoryId : assetCategoryIds) {
			AssetCategory category =
				_assetCategoryLocalService.fetchAssetCategory(assetCategoryId);

			if (category == null) {
				continue;
			}

			assetCategoryIdsList.add(assetCategoryId);
		}

		return ArrayUtil.toArray(assetCategoryIdsList.toArray(new Long[0]));
	}

	private long[] _getAssetCategoryIds(UnicodeProperties unicodeProperties) {
		long[] assetCategoryIds = new long[0];

		for (int i = 0; true; i++) {
			String[] queryValues = StringUtil.split(
				unicodeProperties.getProperty("queryValues" + i, null));

			if (ArrayUtil.isEmpty(queryValues)) {
				break;
			}

			boolean queryContains = GetterUtil.getBoolean(
				unicodeProperties.getProperty(
					"queryContains" + i, StringPool.BLANK));
			boolean queryAndOperator = GetterUtil.getBoolean(
				unicodeProperties.getProperty(
					"queryAndOperator" + i, StringPool.BLANK));
			String queryName = unicodeProperties.getProperty(
				"queryName" + i, StringPool.BLANK);

			if (Objects.equals(queryName, "assetCategories") && queryContains &&
				(queryAndOperator || (queryValues.length == 1))) {

				assetCategoryIds = ArrayUtil.append(
					assetCategoryIds, GetterUtil.getLongValues(queryValues));
			}
		}

		return assetCategoryIds;
	}

	private String[] _getAssetTagNames(UnicodeProperties unicodeProperties) {
		List<String> allAssetTagNames = new ArrayList<>();

		for (int i = 0; true; i++) {
			String[] queryValues = StringUtil.split(
				unicodeProperties.getProperty("queryValues" + i, null));

			if (ArrayUtil.isEmpty(queryValues)) {
				break;
			}

			boolean queryContains = GetterUtil.getBoolean(
				unicodeProperties.getProperty(
					"queryContains" + i, StringPool.BLANK));
			boolean queryAndOperator = GetterUtil.getBoolean(
				unicodeProperties.getProperty(
					"queryAndOperator" + i, StringPool.BLANK));
			String queryName = unicodeProperties.getProperty(
				"queryName" + i, StringPool.BLANK);

			if (!Objects.equals(queryName, "assetCategories") &&
				queryContains &&
				(queryAndOperator || (queryValues.length == 1))) {

				Collections.addAll(allAssetTagNames, queryValues);
			}
		}

		return allAssetTagNames.toArray(new String[0]);
	}

	private long[] _getClassNameIds(
		UnicodeProperties unicodeProperties, long[] availableClassNameIds) {

		boolean anyAssetType = GetterUtil.getBoolean(
			unicodeProperties.getProperty(
				"anyAssetType", Boolean.TRUE.toString()));

		if (anyAssetType) {
			return availableClassNameIds;
		}

		long defaultClassNameId = GetterUtil.getLong(
			unicodeProperties.getProperty("anyAssetType", null));

		if (defaultClassNameId > 0) {
			return new long[] {defaultClassNameId};
		}

		long[] classNameIds = GetterUtil.getLongValues(
			StringUtil.split(
				unicodeProperties.getProperty("classNameIds", null)));

		if (ArrayUtil.isNotEmpty(classNameIds)) {
			return classNameIds;
		}

		return availableClassNameIds;
	}

	private long[] _getClassTypeIds(
		AssetListEntry assetListEntry, UnicodeProperties unicodeProperties,
		String className) {

		long[] availableClassTypeIds = {};

		AssetRendererFactory<?> assetRendererFactory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				className);

		if (assetRendererFactory == null) {
			return availableClassTypeIds;
		}

		ClassTypeReader classTypeReader =
			assetRendererFactory.getClassTypeReader();

		try {
			List<ClassType> classTypes = classTypeReader.getAvailableClassTypes(
				_portal.getSharedContentSiteGroupIds(
					assetListEntry.getCompanyId(), assetListEntry.getGroupId(),
					assetListEntry.getUserId()),
				LocaleUtil.getDefault());

			Stream<ClassType> stream = classTypes.stream();

			availableClassTypeIds = stream.mapToLong(
				ClassType::getClassTypeId
			).toArray();
		}
		catch (PortalException portalException) {
			_log.error(
				"Unable to get class types for class name " + className,
				portalException);
		}

		Class<? extends AssetRendererFactory> clazz =
			_assetRendererFactoryClassProvider.getClass(assetRendererFactory);

		boolean anyAssetType = GetterUtil.getBoolean(
			unicodeProperties.getProperty(
				"anyClassType" + clazz.getSimpleName(),
				Boolean.TRUE.toString()));

		if (anyAssetType) {
			return availableClassTypeIds;
		}

		long anyClassTypeId = GetterUtil.getLong(
			unicodeProperties.getProperty(
				"anyClassType" + clazz.getSimpleName(), null),
			-1);

		if (anyClassTypeId > -1) {
			return new long[] {anyClassTypeId};
		}

		long[] classTypeIds = StringUtil.split(
			unicodeProperties.getProperty(
				"classTypeIds" + clazz.getSimpleName(), null),
			0L);

		if (classTypeIds != null) {
			return classTypeIds;
		}

		return availableClassTypeIds;
	}

	private long[] _getCombinedSegmentsEntryIds(
		AssetListEntry assetListEntry, long[] segmentEntryIds) {

		LongStream longStream = Arrays.stream(segmentEntryIds);

		if ((segmentEntryIds.length > 1) &&
			ArrayUtil.contains(
				segmentEntryIds, SegmentsEntryConstants.ID_DEFAULT)) {

			longStream = Arrays.stream(
				ArrayUtil.remove(
					segmentEntryIds, SegmentsEntryConstants.ID_DEFAULT));
		}

		long[] combinedSegmentsEntryIds = longStream.mapToObj(
			segmentsEntryId ->
				_assetListEntrySegmentsEntryRelLocalService.
					fetchAssetListEntrySegmentsEntryRel(
						assetListEntry.getAssetListEntryId(), segmentsEntryId)
		).filter(
			Objects::nonNull
		).sorted(
			Comparator.comparing(
				AssetListEntrySegmentsEntryRel::getCreateDate,
				Comparator.reverseOrder())
		).map(
			AssetListEntrySegmentsEntryRelModel::getSegmentsEntryId
		).mapToLong(
			segmentsEntryId -> segmentsEntryId
		).toArray();

		if (combinedSegmentsEntryIds.length == 0) {
			combinedSegmentsEntryIds = new long[] {
				SegmentsEntryConstants.ID_DEFAULT
			};
		}

		return combinedSegmentsEntryIds;
	}

	private List<AssetEntry> _getDynamicAssetEntries(
		AssetListEntry assetListEntry, long[] segmentsEntryIds, String userId,
		int start, int end) {

		if (!_assetListConfiguration.combineAssetsFromAllSegmentsDynamic()) {
			AssetEntryQuery assetEntryQuery = getAssetEntryQuery(
				assetListEntry,
				_getFirstSegmentsEntryId(assetListEntry, segmentsEntryIds),
				userId);

			assetEntryQuery.setEnd(end);
			assetEntryQuery.setStart(start);

			return _search(
				assetListEntry.getCompanyId(),
				Collections.singletonList(assetEntryQuery));
		}

		LongStream longStream = Arrays.stream(
			_getCombinedSegmentsEntryIds(assetListEntry, segmentsEntryIds));

		return _search(
			assetListEntry.getCompanyId(),
			longStream.mapToObj(
				segmentsEntryId -> getAssetEntryQuery(
					assetListEntry, segmentsEntryId, userId)
			).collect(
				Collectors.toList()
			));
	}

	private long _getFirstSegmentsEntryId(
		AssetListEntry assetListEntry, long[] segmentsEntryIds) {

		if (segmentsEntryIds.length == 0) {
			return SegmentsEntryConstants.ID_DEFAULT;
		}

		LongStream longStream = Arrays.stream(segmentsEntryIds);

		Stream<AssetListEntrySegmentsEntryRel>
			assetListEntrySegmentsEntryRelStream = longStream.mapToObj(
				segmentsEntryId ->
					_assetListEntrySegmentsEntryRelLocalService.
						fetchAssetListEntrySegmentsEntryRel(
							assetListEntry.getAssetListEntryId(),
							segmentsEntryId));

		return assetListEntrySegmentsEntryRelStream.filter(
			Objects::nonNull
		).min(
			Comparator.comparing(
				AssetListEntrySegmentsEntryRel::getCreateDate,
				Comparator.reverseOrder())
		).get(
		).getSegmentsEntryId();
	}

	private String[] _getKeywords(UnicodeProperties unicodeProperties) {
		String[] allKeywords = new String[0];

		for (int i = 0; true; i++) {
			String[] queryValues = StringUtil.split(
				unicodeProperties.getProperty("queryValues" + i, null));

			if (ArrayUtil.isEmpty(queryValues)) {
				break;
			}

			boolean queryContains = GetterUtil.getBoolean(
				unicodeProperties.getProperty(
					"queryContains" + i, StringPool.BLANK));
			boolean queryAndOperator = GetterUtil.getBoolean(
				unicodeProperties.getProperty(
					"queryAndOperator" + i, StringPool.BLANK));
			String queryName = unicodeProperties.getProperty(
				"queryName" + i, StringPool.BLANK);

			if (Objects.equals(queryName, "keywords") && queryContains &&
				(queryAndOperator || (queryValues.length == 1))) {

				allKeywords = queryValues;
			}
		}

		return allKeywords;
	}

	private List<AssetEntry> _getManualAssetEntries(
		AssetListEntry assetListEntry, long[] segmentsEntryId, int start,
		int end) {

		List<AssetListEntryAssetEntryRel> assetListEntryAssetEntryRels =
			new ArrayList<>();

		if (_assetListConfiguration.combineAssetsFromAllSegmentsManual()) {
			long[] segmentsEntryIds = _sortSegmentsByPriority(
				assetListEntry,
				_getCombinedSegmentsEntryIds(assetListEntry, segmentsEntryId));

			for (long segmentId : segmentsEntryIds) {
				assetListEntryAssetEntryRels.addAll(
					ListUtil.sort(
						_assetListEntryAssetEntryRelLocalService.
							getAssetListEntryAssetEntryRels(
								assetListEntry.getAssetListEntryId(),
								new long[] {segmentId}, QueryUtil.ALL_POS,
								QueryUtil.ALL_POS),
						Comparator.comparing(
							AssetListEntryAssetEntryRelModel::getPosition)));
			}
		}
		else {
			assetListEntryAssetEntryRels =
				_assetListEntryAssetEntryRelLocalService.
					getAssetListEntryAssetEntryRels(
						assetListEntry.getAssetListEntryId(),
						_getFirstSegmentsEntryId(
							assetListEntry, segmentsEntryId),
						start, end);
		}

		Stream<AssetListEntryAssetEntryRel> stream =
			assetListEntryAssetEntryRels.stream();

		return stream.map(
			assetListEntryAssetEntryRel -> _assetEntryLocalService.fetchEntry(
				assetListEntryAssetEntryRel.getAssetEntryId())
		).collect(
			Collectors.toList()
		);
	}

	private SearchContext _getSearchContext(
		long companyId, AssetEntryQuery assetEntryQuery) {

		SearchContext searchContext = new SearchContext();

		searchContext.setCompanyId(companyId);
		searchContext.setEnd(assetEntryQuery.getEnd());
		searchContext.setKeywords(assetEntryQuery.getKeywords());
		searchContext.setStart(assetEntryQuery.getStart());

		return searchContext;
	}

	private void _processAssetEntryQuery(
		long companyId, String userId, UnicodeProperties unicodeProperties,
		AssetEntryQuery assetEntryQuery) {

		for (AssetListAssetEntryQueryProcessor
				assetListAssetEntryQueryProcessor :
					_assetListAssetEntryQueryProcessors) {

			assetListAssetEntryQueryProcessor.processAssetEntryQuery(
				companyId, userId, unicodeProperties, assetEntryQuery);
		}
	}

	private List<AssetEntry> _search(
		long companyId, List<AssetEntryQuery> assetEntryQueries) {

		try {
			if (ListUtil.isEmpty(assetEntryQueries)) {
				return Collections.emptyList();
			}

			AssetEntryQuery assetEntryQuery = assetEntryQueries.get(0);

			if (assetEntryQueries.size() == 1) {
				Hits hits = _assetHelper.search(
					_getSearchContext(companyId, assetEntryQuery),
					assetEntryQuery, assetEntryQuery.getStart(),
					assetEntryQuery.getEnd());

				return _assetHelper.getAssetEntries(hits);
			}

			SearchHits searchHits = _assetHelper.search(
				_getSearchContext(companyId, assetEntryQueries.get(0)),
				assetEntryQueries, assetEntryQuery.getStart(),
				assetEntryQuery.getEnd());

			return _assetHelper.getAssetEntries(searchHits);
		}
		catch (Exception exception) {
			_log.error("Unable to get asset entries", exception);
		}

		return Collections.emptyList();
	}

	private int _searchCount(
		long companyId, List<AssetEntryQuery> assetEntryQueries) {

		try {
			if (ListUtil.isEmpty(assetEntryQueries)) {
				return 0;
			}

			AssetEntryQuery assetEntryQuery = assetEntryQueries.get(0);

			if (assetEntryQueries.size() == 1) {
				Long count = _assetHelper.searchCount(
					_getSearchContext(companyId, assetEntryQuery),
					assetEntryQuery);

				return count.intValue();
			}

			Long count = _assetHelper.searchCount(
				_getSearchContext(companyId, assetEntryQuery),
				assetEntryQueries, assetEntryQuery.getStart(),
				assetEntryQuery.getEnd());

			return count.intValue();
		}
		catch (Exception exception) {
			_log.error("Unable to get asset entries", exception);
		}

		return 0;
	}

	private void _setCategoriesAndTagsAndKeywords(
		AssetEntryQuery assetEntryQuery, UnicodeProperties unicodeProperties,
		long[] overrideAllAssetCategoryIds, String[] overrideAllAssetTagNames,
		String[] overrideAllKeywords) {

		long[] allAssetCategoryIds = new long[0];
		long[] anyAssetCategoryIds = new long[0];
		long[] notAllAssetCategoryIds = new long[0];
		long[] notAnyAssetCategoryIds = new long[0];

		String[] allAssetTagNames = new String[0];
		String[] anyAssetTagNames = new String[0];
		String[] notAllAssetTagNames = new String[0];
		String[] notAnyAssetTagNames = new String[0];

		String[] allKeywords = new String[0];
		String[] anyKeywords = new String[0];
		String[] notAllKeywords = new String[0];
		String[] notAnyKeywords = new String[0];

		for (int i = 0; true; i++) {
			String[] queryValues = StringUtil.split(
				unicodeProperties.getProperty("queryValues" + i, null));

			if (ArrayUtil.isEmpty(queryValues)) {
				break;
			}

			boolean queryContains = GetterUtil.getBoolean(
				unicodeProperties.getProperty(
					"queryContains" + i, StringPool.BLANK));
			boolean queryAndOperator = GetterUtil.getBoolean(
				unicodeProperties.getProperty(
					"queryAndOperator" + i, StringPool.BLANK));
			String queryName = unicodeProperties.getProperty(
				"queryName" + i, StringPool.BLANK);

			if (Objects.equals(queryName, "assetCategories")) {
				long[] assetCategoryIds = GetterUtil.getLongValues(queryValues);

				if (queryContains && queryAndOperator) {
					allAssetCategoryIds = assetCategoryIds;
				}
				else if (queryContains && !queryAndOperator) {
					anyAssetCategoryIds = assetCategoryIds;
				}
				else if (!queryContains && queryAndOperator) {
					notAllAssetCategoryIds = assetCategoryIds;
				}
				else {
					notAnyAssetCategoryIds = assetCategoryIds;
				}
			}
			else if (Objects.equals(queryName, "keywords")) {
				if (queryContains && queryAndOperator) {
					allKeywords = queryValues;
				}
				else if (queryContains && !queryAndOperator) {
					anyKeywords = queryValues;
				}
				else if (!queryContains && queryAndOperator) {
					notAllKeywords = queryValues;
				}
				else {
					notAnyKeywords = queryValues;
				}
			}
			else {
				if (queryContains && queryAndOperator) {
					allAssetTagNames = queryValues;
				}
				else if (queryContains && !queryAndOperator) {
					anyAssetTagNames = queryValues;
				}
				else if (!queryContains && queryAndOperator) {
					notAllAssetTagNames = queryValues;
				}
				else {
					notAnyAssetTagNames = queryValues;
				}
			}
		}

		if (overrideAllAssetCategoryIds != null) {
			allAssetCategoryIds = overrideAllAssetCategoryIds;
		}

		allAssetCategoryIds = _filterAssetCategoryIds(allAssetCategoryIds);

		assetEntryQuery.setAllCategoryIds(allAssetCategoryIds);

		if (overrideAllKeywords != null) {
			allKeywords = overrideAllKeywords;
		}

		assetEntryQuery.setAllKeywords(allKeywords);

		if (overrideAllAssetTagNames != null) {
			allAssetTagNames = overrideAllAssetTagNames;
		}

		long[] groupIds = GetterUtil.getLongValues(
			StringUtil.split(unicodeProperties.getProperty("groupIds", null)));

		for (String assetTagName : allAssetTagNames) {
			long[] allAssetTagIds = _assetTagLocalService.getTagIds(
				groupIds, assetTagName);

			assetEntryQuery.addAllTagIdsArray(allAssetTagIds);
		}

		anyAssetCategoryIds = _filterAssetCategoryIds(anyAssetCategoryIds);

		assetEntryQuery.setAnyCategoryIds(anyAssetCategoryIds);

		assetEntryQuery.setAnyKeywords(anyKeywords);

		long[] anyAssetTagIds = _assetTagLocalService.getTagIds(
			groupIds, anyAssetTagNames);

		assetEntryQuery.setAnyTagIds(anyAssetTagIds);

		assetEntryQuery.setNotAllCategoryIds(notAllAssetCategoryIds);
		assetEntryQuery.setNotAllKeywords(notAllKeywords);

		for (String assetTagName : notAllAssetTagNames) {
			long[] notAllAssetTagIds = _assetTagLocalService.getTagIds(
				groupIds, assetTagName);

			assetEntryQuery.addNotAllTagIdsArray(notAllAssetTagIds);
		}

		assetEntryQuery.setNotAnyCategoryIds(notAnyAssetCategoryIds);
		assetEntryQuery.setNotAnyKeywords(notAnyKeywords);

		long[] notAnyAssetTagIds = _assetTagLocalService.getTagIds(
			groupIds, notAnyAssetTagNames);

		assetEntryQuery.setNotAnyTagIds(notAnyAssetTagIds);
	}

	private long[] _sortSegmentsByPriority(
		AssetListEntry assetListEntry, long[] segmentsEntryIds) {

		LongStream longStream = Arrays.stream(segmentsEntryIds);

		Stream<AssetListEntrySegmentsEntryRel>
			assetListEntrySegmentsEntryRelStream = longStream.mapToObj(
				segmentsEntryId ->
					_assetListEntrySegmentsEntryRelLocalService.
						fetchAssetListEntrySegmentsEntryRel(
							assetListEntry.getAssetListEntryId(),
							segmentsEntryId));

		return assetListEntrySegmentsEntryRelStream.filter(
			Objects::nonNull
		).sorted(
			Comparator.comparing(
				AssetListEntrySegmentsEntryRel::getCreateDate,
				Comparator.reverseOrder())
		).mapToLong(
			AssetListEntrySegmentsEntryRelModel::getSegmentsEntryId
		).toArray();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AssetListAssetEntryProviderImpl.class);

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private AssetEntryLocalService _assetEntryLocalService;

	@Reference
	private AssetHelper _assetHelper;

	private final List<AssetListAssetEntryQueryProcessor>
		_assetListAssetEntryQueryProcessors = new CopyOnWriteArrayList<>();
	private AssetListConfiguration _assetListConfiguration;

	@Reference
	private AssetListEntryAssetEntryRelLocalService
		_assetListEntryAssetEntryRelLocalService;

	@Reference
	private AssetListEntrySegmentsEntryRelLocalService
		_assetListEntrySegmentsEntryRelLocalService;

	@Reference
	private AssetRendererFactoryClassProvider
		_assetRendererFactoryClassProvider;

	@Reference
	private AssetTagLocalService _assetTagLocalService;

	@Reference
	private DLFileEntryTypeLocalService _dlFileEntryTypeLocalService;

	@Reference
	private Portal _portal;

}