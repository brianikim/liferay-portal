/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.util.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.service.AssetCategoryLocalServiceUtil;
import com.liferay.commerce.product.catalog.CPCatalogEntry;
import com.liferay.commerce.product.catalog.CPQuery;
import com.liferay.commerce.product.data.source.CPDataSourceResult;
import com.liferay.commerce.product.helper.CPDefinitionHelper;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistry;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.frutilla.FrutillaRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Luca Pellizzon
 */
@RunWith(Arquillian.class)
public class CPDefinitionHelperTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@BeforeClass
	public static void setUpClass() throws Exception {
		_company = CompanyTestUtil.addCompany();
	}

	@Before
	public void setUp() throws Exception {
		_commerceCatalog = _commerceCatalogLocalService.addCommerceCatalog(
			null, RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			LocaleUtil.toLanguageId(LocaleUtil.US),
			ServiceContextTestUtil.getServiceContext(_company.getGroupId()));
	}

	@After
	public void tearDown() throws Exception {
		List<CPDefinition> cpDefinitions =
			_cpDefinitionLocalService.getCPDefinitions(
				_commerceCatalog.getGroupId(), WorkflowConstants.STATUS_ANY,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		for (CPDefinition cpDefinition : cpDefinitions) {
			_cpDefinitionLocalService.deleteCPDefinition(cpDefinition);
		}

		_commerceCatalogLocalService.deleteCommerceCatalogs(
			_company.getCompanyId());
	}

	@Test
	public void testSearch() throws Exception {
		frutillaRule.scenario(
			"Search for CPDefinitions by keywords"
		).given(
			"A collection of CPDefinitions"
		).when(
			"I search for CPDefinitions given keywords"
		).then(
			"The results will contain only the products matching the keywords"
		);

		_testSearchWithAssetCategoryNames();
		_testSearchWithDraftCPDefinition();
		_testSearchWithPartialSku();
		_testSearchWithSku();
	}

	@Test
	public void testSearchAllCPDefinitions() throws PortalException {
		frutillaRule.scenario(
			"Search for CPDefinitions without filters"
		).given(
			"A collection of CPDefinitions"
		).when(
			"I search for CPDefinitions without filters"
		).then(
			"The results will contain all the products available"
		);

		CPInstance[] cpInstances = _addCPInstances(
			_commerceCatalog.getGroupId(), _CP_INSTANCES_COUNT);

		CPDataSourceResult cpDataSourceResult = _cpDefinitionHelper.search(
			_commerceCatalog.getGroupId(),
			CPTestUtil.getSearchContext(
				null, WorkflowConstants.STATUS_APPROVED,
				_commerceCatalog.getGroup()),
			new CPQuery(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		List<Long> actualCPDefinitionIds = TransformUtil.transform(
			cpDataSourceResult.getCPCatalogEntries(),
			cpCatalogEntry -> cpCatalogEntry.getCPDefinitionId());

		List<Long> cpDefinitionIds = TransformUtil.transformToList(
			cpInstances,
			cpInstance -> {
				CPDefinition cpDefinition = cpInstance.getCPDefinition();

				return cpDefinition.getCPDefinitionId();
			});

		Assert.assertTrue(actualCPDefinitionIds.containsAll(cpDefinitionIds));
	}

	@Test
	public void testSearchCPDefinitionsByCategory() throws PortalException {
		frutillaRule.scenario(
			"Search for CPDefinition by Category"
		).given(
			"A collection of CPDefinitions"
		).and(
			"A category"
		).and(
			"Some of the CPDefinitions are associated to that category"
		).when(
			"I search for CPDefinitions given the category as a search filter"
		).then(
			"The results will contain only those product linked to the category"
		);

		CPInstance[] cpInstances = _addCPInstances(
			_commerceCatalog.getGroupId(), _CP_INSTANCES_COUNT);

		int counter = 0;
		int position = 0;

		long[] cpDefinitionIds = new long[_CP_INSTANCES_COUNT / 2];

		for (CPInstance cpInstance : cpInstances) {
			if ((counter % 2) == 0) {
				CPDefinition cpDefinition = cpInstance.getCPDefinition();

				cpDefinitionIds[position] = cpDefinition.getCPDefinitionId();

				position++;
			}

			counter++;
		}

		AssetCategory assetCategory = CPTestUtil.addCategoryToCPDefinitions(
			_commerceCatalog.getGroupId(), cpDefinitionIds);

		SearchContext searchContext = CPTestUtil.getSearchContext(
			null, WorkflowConstants.STATUS_APPROVED,
			_commerceCatalog.getGroup());

		CPQuery cpQuery = new CPQuery();

		cpQuery.setAllCategoryIds(new long[] {assetCategory.getCategoryId()});

		CPDataSourceResult cpDataSourceResult = _cpDefinitionHelper.search(
			_commerceCatalog.getGroupId(), searchContext, cpQuery,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		List<CPCatalogEntry> cpCatalogEntries =
			cpDataSourceResult.getCPCatalogEntries();

		Assert.assertEquals(
			cpCatalogEntries.toString(), cpDefinitionIds.length,
			cpCatalogEntries.size());

		List<Long> actualCPDefinitionIds = TransformUtil.transform(
			cpCatalogEntries,
			cpCatalogEntry -> cpCatalogEntry.getCPDefinitionId());

		List<Long> cpDefinitionIdsList = TransformUtil.transformToList(
			cpDefinitionIds, cpDefinitionId -> cpDefinitionId);

		Assert.assertTrue(
			actualCPDefinitionIds.containsAll(cpDefinitionIdsList));

		AssetCategoryLocalServiceUtil.deleteCategory(
			assetCategory.getCategoryId());
	}

	@Test
	public void testSearchCPDefinitionsByName() throws PortalException {
		frutillaRule.scenario(
			"Search for CPDefinitions by name"
		).given(
			"A collection of CPDefinitions"
		).when(
			"I search for CPDefinitions given the name as a search filter"
		).then(
			"The results will contain only those product with the name equal " +
				"to the one in the search filter. In this specific case 1"
		);

		CPInstance[] cpInstances = _addCPInstances(
			_commerceCatalog.getGroupId(), _CP_INSTANCES_COUNT);

		int random = (int)(Math.random() * (_CP_INSTANCES_COUNT - 1));

		CPInstance randomCPInstance = cpInstances[random];

		CPDefinition cpDefinition = randomCPInstance.getCPDefinition();

		CPDataSourceResult cpDataSourceResult = _cpDefinitionHelper.search(
			_commerceCatalog.getGroupId(),
			CPTestUtil.getSearchContext(
				cpDefinition.getName(), WorkflowConstants.STATUS_APPROVED,
				_commerceCatalog.getGroup()),
			new CPQuery(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		List<CPCatalogEntry> cpCatalogEntries =
			cpDataSourceResult.getCPCatalogEntries();

		Assert.assertEquals(
			cpCatalogEntries.toString(), 1, cpCatalogEntries.size());

		CPCatalogEntry cpCatalogEntry = cpCatalogEntries.get(0);

		Assert.assertEquals(
			cpDefinition.getCPDefinitionId(),
			cpCatalogEntry.getCPDefinitionId());
	}

	@Rule
	public final FrutillaRule frutillaRule = new FrutillaRule();

	private CPInstance[] _addCPInstances(long groupId, int iterations)
		throws PortalException {

		CPInstance[] cpInstances = new CPInstance[iterations];

		for (int i = 0; i < iterations; i++) {
			cpInstances[i] = CPTestUtil.addCPInstanceFromCatalog(groupId);
		}

		return cpInstances;
	}

	private Set<Long> _searchCPDefinitionIds(String keywords) throws Exception {
		CPDataSourceResult cpDataSourceResult = _cpDefinitionHelper.search(
			_commerceCatalog.getGroupId(),
			CPTestUtil.getSearchContext(
				keywords, WorkflowConstants.STATUS_APPROVED,
				_commerceCatalog.getGroup()),
			new CPQuery(), QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		return SetUtil.fromList(
			TransformUtil.transform(
				cpDataSourceResult.getCPCatalogEntries(),
				cpCatalogEntry -> cpCatalogEntry.getCPDefinitionId()));
	}

	private void _testSearchWithAssetCategoryNames() throws Exception {
		CPInstance[] cpInstances = _addCPInstances(
			_commerceCatalog.getGroupId(), 3);

		AssetCategory assetCategory1 = CPTestUtil.addCategoryToCPDefinitions(
			_commerceCatalog.getGroupId(), cpInstances[0].getCPDefinitionId());
		AssetCategory assetCategory2 = CPTestUtil.addCategoryToCPDefinitions(
			_commerceCatalog.getGroupId(), cpInstances[1].getCPDefinitionId());

		Assert.assertEquals(
			Collections.singleton(cpInstances[0].getCPDefinitionId()),
			_searchCPDefinitionIds(assetCategory1.getName()));
		Assert.assertEquals(
			SetUtil.fromArray(
				new long[] {
					cpInstances[0].getCPDefinitionId(),
					cpInstances[1].getCPDefinitionId()
				}),
			_searchCPDefinitionIds(
				assetCategory1.getName() + StringPool.SPACE +
					assetCategory2.getName()));

		AssetCategoryLocalServiceUtil.deleteCategory(
			assetCategory1.getCategoryId());
		AssetCategoryLocalServiceUtil.deleteCategory(
			assetCategory2.getCategoryId());
	}

	private void _testSearchWithDraftCPDefinition() throws Exception {
		CPInstance cpInstance = CPTestUtil.addCPInstanceFromCatalog(
			_commerceCatalog.getGroupId());

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_commerceCatalog.getGroupId());

		CPDefinition cpDefinition = _cpDefinitionLocalService.updateStatus(
			TestPropsValues.getUserId(), cpInstance.getCPDefinitionId(),
			WorkflowConstants.STATUS_DRAFT, serviceContext,
			Collections.emptyMap());

		Assert.assertEquals(
			Collections.emptySet(),
			_searchCPDefinitionIds(cpDefinition.getName()));

		_cpDefinitionLocalService.updateStatus(
			TestPropsValues.getUserId(), cpDefinition.getCPDefinitionId(),
			WorkflowConstants.STATUS_APPROVED, serviceContext,
			Collections.emptyMap());

		Assert.assertEquals(
			Collections.singleton(cpDefinition.getCPDefinitionId()),
			_searchCPDefinitionIds(cpDefinition.getName()));
	}

	private void _testSearchWithPartialSku() throws Exception {
		Set<Long> cpDefinitionIds = new HashSet<>();

		Indexer<CPDefinition> indexer = _indexerRegistry.getIndexer(
			CPDefinition.class);
		String skuPrefix = RandomTestUtil.randomString();

		for (int i = 0; i < 4; i++) {
			CPInstance cpInstance = CPTestUtil.addCPInstanceFromCatalog(
				_commerceCatalog.getGroupId(), BigDecimal.TEN, skuPrefix + i);

			indexer.reindex(cpInstance.getCPDefinition());

			cpDefinitionIds.add(cpInstance.getCPDefinitionId());
		}

		Assert.assertEquals(cpDefinitionIds, _searchCPDefinitionIds(skuPrefix));

		CPInstance cpInstance =
			CPTestUtil.addCPInstanceWithRandomSkuFromCatalog(
				_commerceCatalog.getGroupId());

		String sku = cpInstance.getSku();

		Assert.assertEquals(
			Collections.singleton(cpInstance.getCPDefinitionId()),
			_searchCPDefinitionIds(sku.substring(3)));
	}

	private void _testSearchWithSku() throws Exception {
		CPInstance cpInstance1 =
			CPTestUtil.addCPInstanceWithRandomSkuFromCatalog(
				_commerceCatalog.getGroupId());
		CPInstance cpInstance2 =
			CPTestUtil.addCPInstanceWithRandomSkuFromCatalog(
				_commerceCatalog.getGroupId());

		CPTestUtil.addCPInstanceWithRandomSkuFromCatalog(
			_commerceCatalog.getGroupId());

		Assert.assertEquals(
			Collections.singleton(cpInstance1.getCPDefinitionId()),
			_searchCPDefinitionIds(cpInstance1.getSku()));
		Assert.assertEquals(
			SetUtil.fromArray(
				new long[] {
					cpInstance1.getCPDefinitionId(),
					cpInstance2.getCPDefinitionId()
				}),
			_searchCPDefinitionIds(
				cpInstance1.getSku() + StringPool.SPACE +
					cpInstance2.getSku()));
	}

	private static final int _CP_INSTANCES_COUNT = 10;

	private static Company _company;

	private CommerceCatalog _commerceCatalog;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@Inject
	private CPDefinitionHelper _cpDefinitionHelper;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Inject
	private IndexerRegistry _indexerRegistry;

}