/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.service.persistence.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.discount.exception.NoSuchDiscountEntryCheckException;
import com.liferay.commerce.discount.model.CommerceDiscountEntryCheck;
import com.liferay.commerce.discount.service.CommerceDiscountEntryCheckLocalServiceUtil;
import com.liferay.commerce.discount.service.persistence.CommerceDiscountEntryCheckPersistence;
import com.liferay.commerce.discount.service.persistence.CommerceDiscountEntryCheckUtil;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.util.IntegerWrapper;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PersistenceTestRule;
import com.liferay.portal.test.rule.TransactionalTestRule;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @generated
 */
@RunWith(Arquillian.class)
public class CommerceDiscountEntryCheckPersistenceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(
				Propagation.REQUIRED, "com.liferay.commerce.discount.service"));

	@Before
	public void setUp() {
		_persistence = CommerceDiscountEntryCheckUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<CommerceDiscountEntryCheck> iterator =
			_commerceDiscountEntryChecks.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		CommerceDiscountEntryCheck commerceDiscountEntryCheck =
			_persistence.create(pk);

		Assert.assertNotNull(commerceDiscountEntryCheck);

		Assert.assertEquals(commerceDiscountEntryCheck.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		CommerceDiscountEntryCheck newCommerceDiscountEntryCheck =
			addCommerceDiscountEntryCheck();

		_persistence.remove(newCommerceDiscountEntryCheck);

		CommerceDiscountEntryCheck existingCommerceDiscountEntryCheck =
			_persistence.fetchByPrimaryKey(
				newCommerceDiscountEntryCheck.getPrimaryKey());

		Assert.assertNull(existingCommerceDiscountEntryCheck);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addCommerceDiscountEntryCheck();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		CommerceDiscountEntryCheck newCommerceDiscountEntryCheck =
			_persistence.create(pk);

		newCommerceDiscountEntryCheck.setMvccVersion(RandomTestUtil.nextLong());

		newCommerceDiscountEntryCheck.setCompanyId(RandomTestUtil.nextLong());

		newCommerceDiscountEntryCheck.setUserId(RandomTestUtil.nextLong());

		newCommerceDiscountEntryCheck.setUserName(
			RandomTestUtil.randomString());

		newCommerceDiscountEntryCheck.setCreateDate(RandomTestUtil.nextDate());

		newCommerceDiscountEntryCheck.setModifiedDate(
			RandomTestUtil.nextDate());

		newCommerceDiscountEntryCheck.setCommerceAccountId(
			RandomTestUtil.nextLong());

		newCommerceDiscountEntryCheck.setCommerceDiscountId(
			RandomTestUtil.nextLong());

		_commerceDiscountEntryChecks.add(
			_persistence.update(newCommerceDiscountEntryCheck));

		CommerceDiscountEntryCheck existingCommerceDiscountEntryCheck =
			_persistence.findByPrimaryKey(
				newCommerceDiscountEntryCheck.getPrimaryKey());

		Assert.assertEquals(
			existingCommerceDiscountEntryCheck.getMvccVersion(),
			newCommerceDiscountEntryCheck.getMvccVersion());
		Assert.assertEquals(
			existingCommerceDiscountEntryCheck.
				getCommerceDiscountEntryCheckId(),
			newCommerceDiscountEntryCheck.getCommerceDiscountEntryCheckId());
		Assert.assertEquals(
			existingCommerceDiscountEntryCheck.getCompanyId(),
			newCommerceDiscountEntryCheck.getCompanyId());
		Assert.assertEquals(
			existingCommerceDiscountEntryCheck.getUserId(),
			newCommerceDiscountEntryCheck.getUserId());
		Assert.assertEquals(
			existingCommerceDiscountEntryCheck.getUserName(),
			newCommerceDiscountEntryCheck.getUserName());
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingCommerceDiscountEntryCheck.getCreateDate()),
			Time.getShortTimestamp(
				newCommerceDiscountEntryCheck.getCreateDate()));
		Assert.assertEquals(
			Time.getShortTimestamp(
				existingCommerceDiscountEntryCheck.getModifiedDate()),
			Time.getShortTimestamp(
				newCommerceDiscountEntryCheck.getModifiedDate()));
		Assert.assertEquals(
			existingCommerceDiscountEntryCheck.getCommerceAccountId(),
			newCommerceDiscountEntryCheck.getCommerceAccountId());
		Assert.assertEquals(
			existingCommerceDiscountEntryCheck.getCommerceDiscountId(),
			newCommerceDiscountEntryCheck.getCommerceDiscountId());
	}

	@Test
	public void testCountByCommerceDiscountId() throws Exception {
		_persistence.countByCommerceDiscountId(RandomTestUtil.nextLong());

		_persistence.countByCommerceDiscountId(0L);
	}

	@Test
	public void testCountByCAI_CDI() throws Exception {
		_persistence.countByCAI_CDI(
			RandomTestUtil.nextLong(), RandomTestUtil.nextLong());

		_persistence.countByCAI_CDI(0L, 0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		CommerceDiscountEntryCheck newCommerceDiscountEntryCheck =
			addCommerceDiscountEntryCheck();

		CommerceDiscountEntryCheck existingCommerceDiscountEntryCheck =
			_persistence.findByPrimaryKey(
				newCommerceDiscountEntryCheck.getPrimaryKey());

		Assert.assertEquals(
			existingCommerceDiscountEntryCheck, newCommerceDiscountEntryCheck);
	}

	@Test(expected = NoSuchDiscountEntryCheckException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<CommerceDiscountEntryCheck>
		getOrderByComparator() {

		return OrderByComparatorFactoryUtil.create(
			"CommerceDiscountEntryCheck", "mvccVersion", true,
			"commerceDiscountEntryCheckId", true, "companyId", true, "userId",
			true, "userName", true, "createDate", true, "modifiedDate", true,
			"commerceAccountId", true, "commerceDiscountId", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		CommerceDiscountEntryCheck newCommerceDiscountEntryCheck =
			addCommerceDiscountEntryCheck();

		CommerceDiscountEntryCheck existingCommerceDiscountEntryCheck =
			_persistence.fetchByPrimaryKey(
				newCommerceDiscountEntryCheck.getPrimaryKey());

		Assert.assertEquals(
			existingCommerceDiscountEntryCheck, newCommerceDiscountEntryCheck);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		CommerceDiscountEntryCheck missingCommerceDiscountEntryCheck =
			_persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingCommerceDiscountEntryCheck);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		CommerceDiscountEntryCheck newCommerceDiscountEntryCheck1 =
			addCommerceDiscountEntryCheck();
		CommerceDiscountEntryCheck newCommerceDiscountEntryCheck2 =
			addCommerceDiscountEntryCheck();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newCommerceDiscountEntryCheck1.getPrimaryKey());
		primaryKeys.add(newCommerceDiscountEntryCheck2.getPrimaryKey());

		Map<Serializable, CommerceDiscountEntryCheck>
			commerceDiscountEntryChecks = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(2, commerceDiscountEntryChecks.size());
		Assert.assertEquals(
			newCommerceDiscountEntryCheck1,
			commerceDiscountEntryChecks.get(
				newCommerceDiscountEntryCheck1.getPrimaryKey()));
		Assert.assertEquals(
			newCommerceDiscountEntryCheck2,
			commerceDiscountEntryChecks.get(
				newCommerceDiscountEntryCheck2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, CommerceDiscountEntryCheck>
			commerceDiscountEntryChecks = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertTrue(commerceDiscountEntryChecks.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		CommerceDiscountEntryCheck newCommerceDiscountEntryCheck =
			addCommerceDiscountEntryCheck();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newCommerceDiscountEntryCheck.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, CommerceDiscountEntryCheck>
			commerceDiscountEntryChecks = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(1, commerceDiscountEntryChecks.size());
		Assert.assertEquals(
			newCommerceDiscountEntryCheck,
			commerceDiscountEntryChecks.get(
				newCommerceDiscountEntryCheck.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, CommerceDiscountEntryCheck>
			commerceDiscountEntryChecks = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertTrue(commerceDiscountEntryChecks.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		CommerceDiscountEntryCheck newCommerceDiscountEntryCheck =
			addCommerceDiscountEntryCheck();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newCommerceDiscountEntryCheck.getPrimaryKey());

		Map<Serializable, CommerceDiscountEntryCheck>
			commerceDiscountEntryChecks = _persistence.fetchByPrimaryKeys(
				primaryKeys);

		Assert.assertEquals(1, commerceDiscountEntryChecks.size());
		Assert.assertEquals(
			newCommerceDiscountEntryCheck,
			commerceDiscountEntryChecks.get(
				newCommerceDiscountEntryCheck.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			CommerceDiscountEntryCheckLocalServiceUtil.
				getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod
				<CommerceDiscountEntryCheck>() {

				@Override
				public void performAction(
					CommerceDiscountEntryCheck commerceDiscountEntryCheck) {

					Assert.assertNotNull(commerceDiscountEntryCheck);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		CommerceDiscountEntryCheck newCommerceDiscountEntryCheck =
			addCommerceDiscountEntryCheck();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CommerceDiscountEntryCheck.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"commerceDiscountEntryCheckId",
				newCommerceDiscountEntryCheck.
					getCommerceDiscountEntryCheckId()));

		List<CommerceDiscountEntryCheck> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		CommerceDiscountEntryCheck existingCommerceDiscountEntryCheck =
			result.get(0);

		Assert.assertEquals(
			existingCommerceDiscountEntryCheck, newCommerceDiscountEntryCheck);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CommerceDiscountEntryCheck.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"commerceDiscountEntryCheckId", RandomTestUtil.nextLong()));

		List<CommerceDiscountEntryCheck> result =
			_persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		CommerceDiscountEntryCheck newCommerceDiscountEntryCheck =
			addCommerceDiscountEntryCheck();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CommerceDiscountEntryCheck.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("commerceDiscountEntryCheckId"));

		Object newCommerceDiscountEntryCheckId =
			newCommerceDiscountEntryCheck.getCommerceDiscountEntryCheckId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"commerceDiscountEntryCheckId",
				new Object[] {newCommerceDiscountEntryCheckId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingCommerceDiscountEntryCheckId = result.get(0);

		Assert.assertEquals(
			existingCommerceDiscountEntryCheckId,
			newCommerceDiscountEntryCheckId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CommerceDiscountEntryCheck.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("commerceDiscountEntryCheckId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"commerceDiscountEntryCheckId",
				new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	protected CommerceDiscountEntryCheck addCommerceDiscountEntryCheck()
		throws Exception {

		long pk = RandomTestUtil.nextLong();

		CommerceDiscountEntryCheck commerceDiscountEntryCheck =
			_persistence.create(pk);

		commerceDiscountEntryCheck.setMvccVersion(RandomTestUtil.nextLong());

		commerceDiscountEntryCheck.setCompanyId(RandomTestUtil.nextLong());

		commerceDiscountEntryCheck.setUserId(RandomTestUtil.nextLong());

		commerceDiscountEntryCheck.setUserName(RandomTestUtil.randomString());

		commerceDiscountEntryCheck.setCreateDate(RandomTestUtil.nextDate());

		commerceDiscountEntryCheck.setModifiedDate(RandomTestUtil.nextDate());

		commerceDiscountEntryCheck.setCommerceAccountId(
			RandomTestUtil.nextLong());

		commerceDiscountEntryCheck.setCommerceDiscountId(
			RandomTestUtil.nextLong());

		_commerceDiscountEntryChecks.add(
			_persistence.update(commerceDiscountEntryCheck));

		return commerceDiscountEntryCheck;
	}

	private List<CommerceDiscountEntryCheck> _commerceDiscountEntryChecks =
		new ArrayList<CommerceDiscountEntryCheck>();
	private CommerceDiscountEntryCheckPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}