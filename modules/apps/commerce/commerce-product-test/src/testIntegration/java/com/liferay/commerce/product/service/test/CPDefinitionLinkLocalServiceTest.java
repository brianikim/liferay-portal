/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.constants.CPDefinitionLinkTypeConstants;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionLink;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPDefinitionLinkLocalService;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalServiceUtil;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.CalendarFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.util.Calendar;
import java.util.List;

import org.frutilla.FrutillaRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Brian I. Kim
 */
@RunWith(Arquillian.class)
public class CPDefinitionLinkLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		Company company = _companyLocalService.getCompany(
			TestPropsValues.getCompanyId());

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			company.getGroupId(), TestPropsValues.getUserId());

		ServiceContextThreadLocal.pushServiceContext(_serviceContext);

		_commerceCatalog = CommerceCatalogLocalServiceUtil.addCommerceCatalog(
			null, RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			LocaleUtil.US.getDisplayLanguage(), _serviceContext);
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
	}

	@Test
	public void testAddCPDefinitionLinkByCProductId() throws Exception {
		frutillaRule.scenario(
			"Add a definition link by product ID"
		).given(
			"Two product definitions"
		).when(
			"an incompatible in bundle or a requires in bundle definition " +
				"link is added from the first product definition to the second"
		).then(
			"only the incompatible in bundle definition link adds the " +
				"reverse definition link"
		).and(
			"deleting the reverse definition link deletes the original one"
		);

		_testAddCPDefinitionLinkByCProductIdWithIncompatibleInBundleType();
		_testAddCPDefinitionLinkByCProductIdWithRequiresInBundleType();
	}

	@Rule
	public final FrutillaRule frutillaRule = new FrutillaRule();

	private void _addCPDefinitionLink(
			CPDefinition cpDefinition1, CPDefinition cpDefinition2, String type)
		throws Exception {

		Calendar displayCalendar = CalendarFactoryUtil.getCalendar();

		displayCalendar.setTime(cpDefinition1.getDisplayDate());

		_cpDefinitionLinkLocalService.addCPDefinitionLinkByCProductId(
			cpDefinition1.getCPDefinitionId(), cpDefinition2.getCProductId(),
			displayCalendar.get(Calendar.MONTH),
			displayCalendar.get(Calendar.DAY_OF_MONTH),
			displayCalendar.get(Calendar.YEAR),
			displayCalendar.get(Calendar.HOUR_OF_DAY),
			displayCalendar.get(Calendar.MINUTE), 0, 0, 0, 0, 0, true, 0D, type,
			_serviceContext);
	}

	private void _testAddCPDefinitionLinkByCProductIdWithIncompatibleInBundleType()
		throws Exception {

		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);
		CPDefinition cpDefinition2 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);

		_addCPDefinitionLink(
			cpDefinition1, cpDefinition2,
			CPDefinitionLinkTypeConstants.INCOMPATIBLE_IN_BUNDLE);

		Assert.assertNotNull(
			_cpDefinitionLinkLocalService.fetchCPDefinitionLink(
				cpDefinition1.getCPDefinitionId(),
				cpDefinition2.getCProductId(),
				CPDefinitionLinkTypeConstants.INCOMPATIBLE_IN_BUNDLE));

		CPDefinitionLink cpDefinitionLink =
			_cpDefinitionLinkLocalService.fetchCPDefinitionLink(
				cpDefinition2.getCPDefinitionId(),
				cpDefinition1.getCProductId(),
				CPDefinitionLinkTypeConstants.INCOMPATIBLE_IN_BUNDLE);

		Assert.assertNotNull(cpDefinitionLink);

		_cpDefinitionLinkLocalService.deleteCPDefinitionLink(cpDefinitionLink);

		Assert.assertNull(
			_cpDefinitionLinkLocalService.fetchCPDefinitionLink(
				cpDefinition1.getCPDefinitionId(),
				cpDefinition2.getCProductId(),
				CPDefinitionLinkTypeConstants.INCOMPATIBLE_IN_BUNDLE));
	}

	private void _testAddCPDefinitionLinkByCProductIdWithRequiresInBundleType()
		throws Exception {

		CPDefinition cpDefinition1 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);
		CPDefinition cpDefinition2 = CPTestUtil.addCPDefinitionFromCatalog(
			_commerceCatalog.getGroupId(), SimpleCPTypeConstants.NAME, false,
			false);

		_addCPDefinitionLink(
			cpDefinition1, cpDefinition2,
			CPDefinitionLinkTypeConstants.REQUIRES_IN_BUNDLE);

		Assert.assertNotNull(
			_cpDefinitionLinkLocalService.fetchCPDefinitionLink(
				cpDefinition1.getCPDefinitionId(),
				cpDefinition2.getCProductId(),
				CPDefinitionLinkTypeConstants.REQUIRES_IN_BUNDLE));
		Assert.assertNull(
			_cpDefinitionLinkLocalService.fetchCPDefinitionLink(
				cpDefinition2.getCPDefinitionId(),
				cpDefinition1.getCProductId(),
				CPDefinitionLinkTypeConstants.REQUIRES_IN_BUNDLE));
	}

	private CommerceCatalog _commerceCatalog;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private CPDefinitionLinkLocalService _cpDefinitionLinkLocalService;

	@Inject
	private CPDefinitionLocalService _cpDefinitionLocalService;

	private ServiceContext _serviceContext;

}