/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.channel.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceOrderType;
import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRel;
import com.liferay.commerce.payment.model.CommercePaymentMethodGroupRelQualifier;
import com.liferay.commerce.payment.service.CommercePaymentMethodGroupRelLocalServiceUtil;
import com.liferay.commerce.payment.service.CommercePaymentMethodGroupRelQualifierLocalService;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.service.CommerceOrderTypeLocalServiceUtil;
import com.liferay.commerce.term.constants.CommerceTermEntryConstants;
import com.liferay.commerce.term.model.CommerceTermEntry;
import com.liferay.commerce.term.service.CommerceTermEntryLocalService;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.headless.commerce.admin.channel.client.dto.v1_0.PaymentMethodGroupRelOrderType;
import com.liferay.headless.commerce.admin.channel.client.dto.v1_0.PaymentMethodGroupRelTerm;
import com.liferay.headless.commerce.admin.channel.client.problem.Problem;
import com.liferay.headless.commerce.admin.channel.client.resource.v1_0.PaymentMethodGroupRelTermResource;
import com.liferay.headless.commerce.core.util.DateConfig;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.test.rule.Inject;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Andrea Sbarra
 * @author Crescenzo Rega
 */
@RunWith(Arquillian.class)
public class PaymentMethodGroupRelOrderTypeResourceTest
	extends BasePaymentMethodGroupRelOrderTypeResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());

		_commerceCurrency = CommerceCurrencyTestUtil.addCommerceCurrency(
			testGroup.getCompanyId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			testGroup.getGroupId(), _commerceCurrency.getCode());
	}

	@Ignore
	@Override
	@Test
	public void testBatchEngineDeleteImportTask() throws Exception {
		super.testBatchEngineDeleteImportTask();
	}

	@Override
	@Test
	public void testDeletePaymentMethodGroupRelOrderType() throws Exception {
		PaymentMethodGroupRelOrderType paymentMethodGroupRelOrderType =
			_addPaymentMethodGroupRelOrderType(_getId());

		paymentMethodGroupRelOrderTypeResource.
			deletePaymentMethodGroupRelOrderType(
				paymentMethodGroupRelOrderType.
					getPaymentMethodGroupRelOrderTypeId());
	}

	@Ignore
	@Override
	@Test
	public void testGetPaymentMethodGroupRelIdPaymentMethodGroupRelOrderTypesPageWithFilterDateTimeEquals()
		throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGetPaymentMethodGroupRelIdPaymentMethodGroupRelOrderTypesPageWithFilterDoubleEquals()
		throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGetPaymentMethodGroupRelIdPaymentMethodGroupRelOrderTypesPageWithFilterStringEquals()
		throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGetPaymentMethodGroupRelIdPaymentMethodGroupRelOrderTypesPageWithSortDateTime()
		throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGetPaymentMethodGroupRelIdPaymentMethodGroupRelOrderTypesPageWithSortDouble()
		throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGetPaymentMethodGroupRelIdPaymentMethodGroupRelOrderTypesPageWithSortInteger()
		throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGetPaymentMethodGroupRelIdPaymentMethodGroupRelOrderTypesPageWithSortString()
		throws Exception {
	}

	@Ignore
	@Override
	@Test
	public void testGraphQLDeletePaymentMethodGroupRelOrderType()
		throws Exception {
	}

	@Override
	@Test
	public void testPostPaymentMethodGroupRelIdPaymentMethodGroupRelOrderType()
		throws Exception {

		super.testPostPaymentMethodGroupRelIdPaymentMethodGroupRelOrderType();

		_testPostPaymentMethodGroupRelIdPaymentMethodGroupRelOrderTypeWithDuplicateQualifier();
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {
			"orderTypeExternalReferenceCode", "orderTypeId",
			"paymentMethodGroupRelId", "paymentMethodGroupRelOrderTypeId"
		};
	}

	@Override
	protected PaymentMethodGroupRelOrderType
			randomPaymentMethodGroupRelOrderType()
		throws Exception {

		CommerceOrderType commerceOrderType = _addCommerceOrderType();

		CommercePaymentMethodGroupRelQualifier
			commercePaymentMethodGroupRelQualifier =
				_addCommercePaymentMethodGroupRelQualifier(
					commerceOrderType, _getId());

		_commercePaymentMethodGroupRelQualifiers.add(
			commercePaymentMethodGroupRelQualifier);

		return new PaymentMethodGroupRelOrderType() {
			{
				orderTypeExternalReferenceCode =
					commerceOrderType.getExternalReferenceCode();
				orderTypeId = commerceOrderType.getCommerceOrderTypeId();
				paymentMethodGroupRelId =
					commercePaymentMethodGroupRelQualifier.
						getCommercePaymentMethodGroupRelId();
				paymentMethodGroupRelOrderTypeId =
					commercePaymentMethodGroupRelQualifier.
						getCommercePaymentMethodGroupRelQualifierId();
			}
		};
	}

	@Override
	protected PaymentMethodGroupRelOrderType
			testDeletePaymentMethodGroupRelOrderTypeBatch_addPaymentMethodGroupRelOrderType()
		throws Exception {

		return _addPaymentMethodGroupRelOrderType(_getId());
	}

	@Override
	protected PaymentMethodGroupRelOrderType
			testGetPaymentMethodGroupRelIdPaymentMethodGroupRelOrderTypesPage_addPaymentMethodGroupRelOrderType(
				Long id,
				PaymentMethodGroupRelOrderType paymentMethodGroupRelOrderType)
		throws Exception {

		return _addPaymentMethodGroupRelOrderType(id);
	}

	@Override
	protected Long
			testGetPaymentMethodGroupRelIdPaymentMethodGroupRelOrderTypesPage_getId()
		throws Exception {

		return _getId();
	}

	@Override
	protected PaymentMethodGroupRelOrderType
			testPostPaymentMethodGroupRelIdPaymentMethodGroupRelOrderType_addPaymentMethodGroupRelOrderType(
				PaymentMethodGroupRelOrderType paymentMethodGroupRelOrderType)
		throws Exception {

		return paymentMethodGroupRelOrderType;
	}

	private CommerceOrderType _addCommerceOrderType() throws Exception {
		DateConfig displayDateConfig = DateConfig.toDisplayDateConfig(
			RandomTestUtil.nextDate(), _user.getTimeZone());
		DateConfig expirationDateConfig = DateConfig.toExpirationDateConfig(
			RandomTestUtil.nextDate(), _user.getTimeZone());

		CommerceOrderType commerceOrderType =
			CommerceOrderTypeLocalServiceUtil.addCommerceOrderType(
				RandomTestUtil.randomString(), _user.getUserId(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomBoolean(), displayDateConfig.getMonth(),
				displayDateConfig.getDay(), displayDateConfig.getYear(),
				displayDateConfig.getHour(), displayDateConfig.getMinute(), 0,
				expirationDateConfig.getMonth(), expirationDateConfig.getDay(),
				expirationDateConfig.getYear(), expirationDateConfig.getHour(),
				expirationDateConfig.getMinute(), true, _serviceContext);

		_commerceOrderTypes.add(commerceOrderType);

		return commerceOrderType;
	}

	private CommercePaymentMethodGroupRelQualifier
			_addCommercePaymentMethodGroupRelQualifier(
				CommerceOrderType commerceOrderType,
				long commercePaymentMethodGroupRelId)
		throws Exception {

		CommercePaymentMethodGroupRelQualifier
			commercePaymentMethodGroupRelQualifier =
				_commercePaymentMethodGroupRelQualifierLocalService.
					addCommercePaymentMethodGroupRelQualifier(
						_user.getUserId(), CommerceOrderType.class.getName(),
						commerceOrderType.getCommerceOrderTypeId(),
						commercePaymentMethodGroupRelId);

		_commercePaymentMethodGroupRelQualifiers.add(
			commercePaymentMethodGroupRelQualifier);

		return commercePaymentMethodGroupRelQualifier;
	}

	private PaymentMethodGroupRelOrderType _addPaymentMethodGroupRelOrderType(
			Long commercePaymentMethodGroupRelId)
		throws Exception {

		CommerceOrderType commerceOrderType = _addCommerceOrderType();

		CommercePaymentMethodGroupRelQualifier
			commercePaymentMethodGroupRelQualifier =
				_addCommercePaymentMethodGroupRelQualifier(
					commerceOrderType, commercePaymentMethodGroupRelId);

		_commercePaymentMethodGroupRelQualifiers.add(
			commercePaymentMethodGroupRelQualifier);

		return new PaymentMethodGroupRelOrderType() {
			{
				orderTypeExternalReferenceCode =
					commerceOrderType.getExternalReferenceCode();
				orderTypeId = commerceOrderType.getCommerceOrderTypeId();
				paymentMethodGroupRelId =
					commercePaymentMethodGroupRelQualifier.
						getCommercePaymentMethodGroupRelId();
				paymentMethodGroupRelOrderTypeId =
					commercePaymentMethodGroupRelQualifier.
						getCommercePaymentMethodGroupRelQualifierId();
			}
		};
	}

	private void _assertDuplicateQualifierProblem(
			UnsafeRunnable<Exception> unsafeRunnable)
		throws Exception {

		try {
			unsafeRunnable.run();

			Assert.fail();
		}
		catch (Problem.ProblemException problemException) {
			Problem problem = problemException.getProblem();

			Assert.assertEquals("CONFLICT", problem.getStatus());
			Assert.assertEquals(
				"The payment method group relation qualifier already exists.",
				problem.getTitle());
		}
	}

	private long _getId() throws Exception {
		CommercePaymentMethodGroupRel commercePaymentMethodGroupRel =
			CommercePaymentMethodGroupRelLocalServiceUtil.
				addCommercePaymentMethodGroupRel(
					_user.getUserId(), _commerceChannel.getGroupId(),
					RandomTestUtil.randomLocaleStringMap(),
					RandomTestUtil.randomLocaleStringMap(), true, null,
					RandomTestUtil.randomString(), 99, null);

		_commercePaymentMethodGroupRels.add(commercePaymentMethodGroupRel);

		return commercePaymentMethodGroupRel.
			getCommercePaymentMethodGroupRelId();
	}

	private void _testPostPaymentMethodGroupRelIdPaymentMethodGroupRelOrderTypeWithDuplicateQualifier()
		throws Exception {

		long id = _getId();

		PaymentMethodGroupRelOrderType paymentMethodGroupRelOrderType =
			_addPaymentMethodGroupRelOrderType(id);

		_assertDuplicateQualifierProblem(
			() ->
				paymentMethodGroupRelOrderTypeResource.
					postPaymentMethodGroupRelIdPaymentMethodGroupRelOrderType(
						id, paymentMethodGroupRelOrderType));

		_commerceTermEntry =
			_commerceTermEntryLocalService.addCommerceTermEntry(
				RandomTestUtil.randomString(), _user.getUserId(), true,
				RandomTestUtil.randomLocaleStringMap(), 1, 1, 2022, 12, 0, 0, 0,
				0, 0, 0, true, RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomString(), RandomTestUtil.nextDouble(),
				CommerceTermEntryConstants.TYPE_PAYMENT_TERMS, StringPool.BLANK,
				_serviceContext);

		_commercePaymentMethodGroupRelQualifiers.add(
			_commercePaymentMethodGroupRelQualifierLocalService.
				addCommercePaymentMethodGroupRelQualifier(
					_user.getUserId(), CommerceTermEntry.class.getName(),
					_commerceTermEntry.getCommerceTermEntryId(), id));

		User user = UserTestUtil.getAdminUser(testCompany.getCompanyId());

		PaymentMethodGroupRelTermResource paymentMethodGroupRelTermResource =
			PaymentMethodGroupRelTermResource.builder(
			).authentication(
				user.getEmailAddress(), PropsValues.DEFAULT_ADMIN_PASSWORD
			).endpoint(
				testCompany.getVirtualHostname(),
				PortalUtil.getPortalServerPort(false), "http"
			).locale(
				LocaleUtil.getDefault()
			).build();

		_assertDuplicateQualifierProblem(
			() ->
				paymentMethodGroupRelTermResource.
					postPaymentMethodGroupRelIdPaymentMethodGroupRelTerm(
						id,
						new PaymentMethodGroupRelTerm() {
							{
								paymentMethodGroupRelId = id;
								termId =
									_commerceTermEntry.getCommerceTermEntryId();
							}
						}));
	}

	@DeleteAfterTestRun
	private CommerceChannel _commerceChannel;

	@DeleteAfterTestRun
	private CommerceCurrency _commerceCurrency;

	@DeleteAfterTestRun
	private List<CommerceOrderType> _commerceOrderTypes = new ArrayList<>();

	@Inject
	private CommercePaymentMethodGroupRelQualifierLocalService
		_commercePaymentMethodGroupRelQualifierLocalService;

	@DeleteAfterTestRun
	private List<CommercePaymentMethodGroupRelQualifier>
		_commercePaymentMethodGroupRelQualifiers = new ArrayList<>();

	@DeleteAfterTestRun
	private List<CommercePaymentMethodGroupRel>
		_commercePaymentMethodGroupRels = new ArrayList<>();

	@DeleteAfterTestRun
	private CommerceTermEntry _commerceTermEntry;

	@Inject
	private CommerceTermEntryLocalService _commerceTermEntryLocalService;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}