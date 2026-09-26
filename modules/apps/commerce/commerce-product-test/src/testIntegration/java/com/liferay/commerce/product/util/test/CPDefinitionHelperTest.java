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
import com.liferay.commerce.product.model.CPSpecificationOption;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPDefinitionSpecificationOptionValueLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.Collections;
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

		long[] cpDefinitionIds1 = new long[_CP_INSTANCES_COUNT / 2];
		long[] cpDefinitionIds2 = new long[_CP_INSTANCES_COUNT / 2];

		for (int i = 0; i < cpInstances.length; i++) {
			CPInstance cpInstance = cpInstances[i];

			if ((i % 2) == 0) {
				cpDefinitionIds1[i / 2] = cpInstance.getCPDefinitionId();
			}
			else {
				cpDefinitionIds2[i / 2] = cpInstance.getCPDefinitionId();
			}
		}

		AssetCategory assetCategory1 = CPTestUtil.addCategoryToCPDefinitions(
			_commerceCatalog.getGroupId(), cpDefinitionIds1);

		SearchContext searchContext = CPTestUtil.getSearchContext(
			null, WorkflowConstants.STATUS_APPROVED,
			_commerceCatalog.getGroup());

		CPQuery cpQuery = new CPQuery();

		cpQuery.setAllCategoryIds(new long[] {assetCategory1.getCategoryId()});

		CPDataSourceResult cpDataSourceResult = _cpDefinitionHelper.search(
			_commerceCatalog.getGroupId(), searchContext, cpQuery,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		List<CPCatalogEntry> cpCatalogEntries =
			cpDataSourceResult.getCPCatalogEntries();

		Assert.assertEquals(
			cpCatalogEntries.toString(), cpDefinitionIds1.length,
			cpCatalogEntries.size());

		List<Long> actualCPDefinitionIds = TransformUtil.transform(
			cpCatalogEntries,
			cpCatalogEntry -> cpCatalogEntry.getCPDefinitionId());

		List<Long> cpDefinitionIdsList = TransformUtil.transformToList(
			cpDefinitionIds1, cpDefinitionId -> cpDefinitionId);

		Assert.assertTrue(
			actualCPDefinitionIds.containsAll(cpDefinitionIdsList));

		Assert.assertEquals(
			SetUtil.fromArray(cpDefinitionIds1),
			_searchCPDefinitionIds(assetCategory1.getName()));

		AssetCategory assetCategory2 = CPTestUtil.addCategoryToCPDefinitions(
			_commerceCatalog.getGroupId(), cpDefinitionIds2);

		Assert.assertEquals(
			SetUtil.fromArray(
				ArrayUtil.append(cpDefinitionIds1, cpDefinitionIds2)),
			_searchCPDefinitionIds(
				assetCategory1.getName() + StringPool.SPACE +
					assetCategory2.getName()));

		AssetCategoryLocalServiceUtil.deleteCategory(
			assetCategory1.getCategoryId());
		AssetCategoryLocalServiceUtil.deleteCategory(
			assetCategory2.getCategoryId());
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

	@Test
	public void testSearchCPDefinitionsBySku() throws PortalException {
		frutillaRule.scenario(
			"Search for CPDefinitions by SKU"
		).given(
			"A collection of CPDefinitions with random SKUs"
		).when(
			"I search for CPDefinitions given full or partial SKUs as keywords"
		).then(
			"The results will contain only the products with matching SKUs"
		);

		CPInstance cpInstance1 =
			CPTestUtil.addCPInstanceWithRandomSkuFromCatalog(
				_commerceCatalog.getGroupId());

		Assert.assertEquals(
			Collections.singleton(cpInstance1.getCPDefinitionId()),
			_searchCPDefinitionIds(cpInstance1.getSku()));

		CPInstance cpInstance2 =
			CPTestUtil.addCPInstanceWithRandomSkuFromCatalog(
				_commerceCatalog.getGroupId());

		Assert.assertEquals(
			SetUtil.fromArray(
				new long[] {
					cpInstance1.getCPDefinitionId(),
					cpInstance2.getCPDefinitionId()
				}),
			_searchCPDefinitionIds(
				cpInstance1.getSku() + StringPool.SPACE +
					cpInstance2.getSku()));

		CPInstance cpInstance3 =
			CPTestUtil.addCPInstanceWithRandomSkuFromCatalog(
				_commerceCatalog.getGroupId());

		String sku = cpInstance3.getSku();

		Assert.assertEquals(
			Collections.singleton(cpInstance3.getCPDefinitionId()),
			_searchCPDefinitionIds(sku.substring(3)));
	}

	@Test
	public void testSearchCPDefinitionsBySpecificationValue()
		throws PortalException {

		frutillaRule.scenario(
			"Search for CPDefinitions by specification value"
		).given(
			"A collection of CPDefinitions with different specification values"
		).when(
			"I search for CPDefinitions given specification values as keywords"
		).then(
			"The results will contain only the products with matching values"
		);

		_cpSpecificationOption = CPTestUtil.addCPSpecificationOption(
			_commerceCatalog.getGroupId(), true);

		CPInstance[] cpInstances = _addCPInstances(
			_commerceCatalog.getGroupId(), 3);

		String[] values = new String[cpInstances.length];

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				_commerceCatalog.getGroupId());

		for (int i = 0; i < cpInstances.length; i++) {
			values[i] = RandomTestUtil.randomString();

			_cpDefinitionSpecificationOptionValueLocalService.
				addCPDefinitionSpecificationOptionValue(
					StringPool.BLANK, cpInstances[i].getCPDefinitionId(),
					_cpSpecificationOption.getCPSpecificationOptionId(), 0,
					RandomTestUtil.randomDouble(),
					Collections.singletonMap(LocaleUtil.US, values[i]), true,
					serviceContext);
		}

		Assert.assertEquals(
			Collections.singleton(cpInstances[0].getCPDefinitionId()),
			_searchCPDefinitionIds(values[0]));
		Assert.assertEquals(
			SetUtil.fromArray(
				new long[] {
					cpInstances[0].getCPDefinitionId(),
					cpInstances[1].getCPDefinitionId()
				}),
			_searchCPDefinitionIds(values[0] + StringPool.SPACE + values[1]));
	}

	@Test
	public void testSearchCPDefinitionsByStatus() throws Exception {
		frutillaRule.scenario(
			"Search for CPDefinitions by status"
		).given(
			"A draft CPDefinition"
		).when(
			"I search for approved CPDefinitions given its name as keywords"
		).then(
			"The results will contain the product only after it is approved"
		);

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

	private Set<Long> _searchCPDefinitionIds(String keywords)
		throws PortalException {

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
	private CPDefinitionSpecificationOptionValueLocalService
		_cpDefinitionSpecificationOptionValueLocalService;

	@DeleteAfterTestRun
	private CPSpecificationOption _cpSpecificationOption;

}