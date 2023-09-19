/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.service.persistence.impl;

import com.liferay.commerce.discount.exception.NoSuchDiscountEntryCheckException;
import com.liferay.commerce.discount.model.CommerceDiscountEntryCheck;
import com.liferay.commerce.discount.model.CommerceDiscountEntryCheckTable;
import com.liferay.commerce.discount.model.impl.CommerceDiscountEntryCheckImpl;
import com.liferay.commerce.discount.model.impl.CommerceDiscountEntryCheckModelImpl;
import com.liferay.commerce.discount.service.persistence.CommerceDiscountEntryCheckPersistence;
import com.liferay.commerce.discount.service.persistence.CommerceDiscountEntryCheckUtil;
import com.liferay.commerce.discount.service.persistence.impl.constants.CommercePersistenceConstants;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.configuration.Configuration;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.dao.orm.FinderCache;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.dao.orm.Query;
import com.liferay.portal.kernel.dao.orm.QueryPos;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.dao.orm.SessionFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.ProxyUtil;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The persistence implementation for the commerce discount entry check service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Marco Leo
 * @generated
 */
@Component(service = CommerceDiscountEntryCheckPersistence.class)
public class CommerceDiscountEntryCheckPersistenceImpl
	extends BasePersistenceImpl<CommerceDiscountEntryCheck>
	implements CommerceDiscountEntryCheckPersistence {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use <code>CommerceDiscountEntryCheckUtil</code> to access the commerce discount entry check persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY =
		CommerceDiscountEntryCheckImpl.class.getName();

	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List1";

	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List2";

	private FinderPath _finderPathWithPaginationFindAll;
	private FinderPath _finderPathWithoutPaginationFindAll;
	private FinderPath _finderPathCountAll;
	private FinderPath _finderPathWithPaginationFindByCommerceDiscountId;
	private FinderPath _finderPathWithoutPaginationFindByCommerceDiscountId;
	private FinderPath _finderPathCountByCommerceDiscountId;

	/**
	 * Returns all the commerce discount entry checks where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @return the matching commerce discount entry checks
	 */
	@Override
	public List<CommerceDiscountEntryCheck> findByCommerceDiscountId(
		long commerceDiscountId) {

		return findByCommerceDiscountId(
			commerceDiscountId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the commerce discount entry checks where commerceDiscountId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceDiscountEntryCheckModelImpl</code>.
	 * </p>
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param start the lower bound of the range of commerce discount entry checks
	 * @param end the upper bound of the range of commerce discount entry checks (not inclusive)
	 * @return the range of matching commerce discount entry checks
	 */
	@Override
	public List<CommerceDiscountEntryCheck> findByCommerceDiscountId(
		long commerceDiscountId, int start, int end) {

		return findByCommerceDiscountId(commerceDiscountId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the commerce discount entry checks where commerceDiscountId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceDiscountEntryCheckModelImpl</code>.
	 * </p>
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param start the lower bound of the range of commerce discount entry checks
	 * @param end the upper bound of the range of commerce discount entry checks (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching commerce discount entry checks
	 */
	@Override
	public List<CommerceDiscountEntryCheck> findByCommerceDiscountId(
		long commerceDiscountId, int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		return findByCommerceDiscountId(
			commerceDiscountId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the commerce discount entry checks where commerceDiscountId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceDiscountEntryCheckModelImpl</code>.
	 * </p>
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param start the lower bound of the range of commerce discount entry checks
	 * @param end the upper bound of the range of commerce discount entry checks (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching commerce discount entry checks
	 */
	@Override
	public List<CommerceDiscountEntryCheck> findByCommerceDiscountId(
		long commerceDiscountId, int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator,
		boolean useFinderCache) {

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath =
					_finderPathWithoutPaginationFindByCommerceDiscountId;
				finderArgs = new Object[] {commerceDiscountId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByCommerceDiscountId;
			finderArgs = new Object[] {
				commerceDiscountId, start, end, orderByComparator
			};
		}

		List<CommerceDiscountEntryCheck> list = null;

		if (useFinderCache) {
			list = (List<CommerceDiscountEntryCheck>)finderCache.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (CommerceDiscountEntryCheck commerceDiscountEntryCheck :
						list) {

					if (commerceDiscountId !=
							commerceDiscountEntryCheck.
								getCommerceDiscountId()) {

						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler sb = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					3 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				sb = new StringBundler(3);
			}

			sb.append(_SQL_SELECT_COMMERCEDISCOUNTENTRYCHECK_WHERE);

			sb.append(_FINDER_COLUMN_COMMERCEDISCOUNTID_COMMERCEDISCOUNTID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else {
				sb.append(CommerceDiscountEntryCheckModelImpl.ORDER_BY_JPQL);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(commerceDiscountId);

				list = (List<CommerceDiscountEntryCheck>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a matching commerce discount entry check could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck findByCommerceDiscountId_First(
			long commerceDiscountId,
			OrderByComparator<CommerceDiscountEntryCheck> orderByComparator)
		throws NoSuchDiscountEntryCheckException {

		CommerceDiscountEntryCheck commerceDiscountEntryCheck =
			fetchByCommerceDiscountId_First(
				commerceDiscountId, orderByComparator);

		if (commerceDiscountEntryCheck != null) {
			return commerceDiscountEntryCheck;
		}

		StringBundler sb = new StringBundler(4);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("commerceDiscountId=");
		sb.append(commerceDiscountId);

		sb.append("}");

		throw new NoSuchDiscountEntryCheckException(sb.toString());
	}

	/**
	 * Returns the first commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce discount entry check, or <code>null</code> if a matching commerce discount entry check could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck fetchByCommerceDiscountId_First(
		long commerceDiscountId,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		List<CommerceDiscountEntryCheck> list = findByCommerceDiscountId(
			commerceDiscountId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a matching commerce discount entry check could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck findByCommerceDiscountId_Last(
			long commerceDiscountId,
			OrderByComparator<CommerceDiscountEntryCheck> orderByComparator)
		throws NoSuchDiscountEntryCheckException {

		CommerceDiscountEntryCheck commerceDiscountEntryCheck =
			fetchByCommerceDiscountId_Last(
				commerceDiscountId, orderByComparator);

		if (commerceDiscountEntryCheck != null) {
			return commerceDiscountEntryCheck;
		}

		StringBundler sb = new StringBundler(4);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("commerceDiscountId=");
		sb.append(commerceDiscountId);

		sb.append("}");

		throw new NoSuchDiscountEntryCheckException(sb.toString());
	}

	/**
	 * Returns the last commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce discount entry check, or <code>null</code> if a matching commerce discount entry check could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck fetchByCommerceDiscountId_Last(
		long commerceDiscountId,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		int count = countByCommerceDiscountId(commerceDiscountId);

		if (count == 0) {
			return null;
		}

		List<CommerceDiscountEntryCheck> list = findByCommerceDiscountId(
			commerceDiscountId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the commerce discount entry checks before and after the current commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the current commerce discount entry check
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a commerce discount entry check with the primary key could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck[] findByCommerceDiscountId_PrevAndNext(
			long commerceDiscountEntryCheckId, long commerceDiscountId,
			OrderByComparator<CommerceDiscountEntryCheck> orderByComparator)
		throws NoSuchDiscountEntryCheckException {

		CommerceDiscountEntryCheck commerceDiscountEntryCheck =
			findByPrimaryKey(commerceDiscountEntryCheckId);

		Session session = null;

		try {
			session = openSession();

			CommerceDiscountEntryCheck[] array =
				new CommerceDiscountEntryCheckImpl[3];

			array[0] = getByCommerceDiscountId_PrevAndNext(
				session, commerceDiscountEntryCheck, commerceDiscountId,
				orderByComparator, true);

			array[1] = commerceDiscountEntryCheck;

			array[2] = getByCommerceDiscountId_PrevAndNext(
				session, commerceDiscountEntryCheck, commerceDiscountId,
				orderByComparator, false);

			return array;
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected CommerceDiscountEntryCheck getByCommerceDiscountId_PrevAndNext(
		Session session, CommerceDiscountEntryCheck commerceDiscountEntryCheck,
		long commerceDiscountId,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator,
		boolean previous) {

		StringBundler sb = null;

		if (orderByComparator != null) {
			sb = new StringBundler(
				4 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			sb = new StringBundler(3);
		}

		sb.append(_SQL_SELECT_COMMERCEDISCOUNTENTRYCHECK_WHERE);

		sb.append(_FINDER_COLUMN_COMMERCEDISCOUNTID_COMMERCEDISCOUNTID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				sb.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				sb.append(_ORDER_BY_ENTITY_ALIAS);
				sb.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						sb.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(WHERE_GREATER_THAN);
					}
					else {
						sb.append(WHERE_LESSER_THAN);
					}
				}
			}

			sb.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				sb.append(_ORDER_BY_ENTITY_ALIAS);
				sb.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						sb.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(ORDER_BY_ASC);
					}
					else {
						sb.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			sb.append(CommerceDiscountEntryCheckModelImpl.ORDER_BY_JPQL);
		}

		String sql = sb.toString();

		Query query = session.createQuery(sql);

		query.setFirstResult(0);
		query.setMaxResults(2);

		QueryPos queryPos = QueryPos.getInstance(query);

		queryPos.add(commerceDiscountId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						commerceDiscountEntryCheck)) {

				queryPos.add(orderByConditionValue);
			}
		}

		List<CommerceDiscountEntryCheck> list = query.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the commerce discount entry checks where commerceDiscountId = &#63; from the database.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 */
	@Override
	public void removeByCommerceDiscountId(long commerceDiscountId) {
		for (CommerceDiscountEntryCheck commerceDiscountEntryCheck :
				findByCommerceDiscountId(
					commerceDiscountId, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					null)) {

			remove(commerceDiscountEntryCheck);
		}
	}

	/**
	 * Returns the number of commerce discount entry checks where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @return the number of matching commerce discount entry checks
	 */
	@Override
	public int countByCommerceDiscountId(long commerceDiscountId) {
		FinderPath finderPath = _finderPathCountByCommerceDiscountId;

		Object[] finderArgs = new Object[] {commerceDiscountId};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs, this);

		if (count == null) {
			StringBundler sb = new StringBundler(2);

			sb.append(_SQL_COUNT_COMMERCEDISCOUNTENTRYCHECK_WHERE);

			sb.append(_FINDER_COLUMN_COMMERCEDISCOUNTID_COMMERCEDISCOUNTID_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(commerceDiscountId);

				count = (Long)query.uniqueResult();

				finderCache.putResult(finderPath, finderArgs, count);
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String
		_FINDER_COLUMN_COMMERCEDISCOUNTID_COMMERCEDISCOUNTID_2 =
			"commerceDiscountEntryCheck.commerceDiscountId = ?";

	private FinderPath _finderPathWithPaginationFindByCAI_CDI;
	private FinderPath _finderPathWithoutPaginationFindByCAI_CDI;
	private FinderPath _finderPathCountByCAI_CDI;

	/**
	 * Returns all the commerce discount entry checks where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @return the matching commerce discount entry checks
	 */
	@Override
	public List<CommerceDiscountEntryCheck> findByCAI_CDI(
		long commerceAccountId, long commerceDiscountId) {

		return findByCAI_CDI(
			commerceAccountId, commerceDiscountId, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the commerce discount entry checks where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceDiscountEntryCheckModelImpl</code>.
	 * </p>
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param start the lower bound of the range of commerce discount entry checks
	 * @param end the upper bound of the range of commerce discount entry checks (not inclusive)
	 * @return the range of matching commerce discount entry checks
	 */
	@Override
	public List<CommerceDiscountEntryCheck> findByCAI_CDI(
		long commerceAccountId, long commerceDiscountId, int start, int end) {

		return findByCAI_CDI(
			commerceAccountId, commerceDiscountId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the commerce discount entry checks where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceDiscountEntryCheckModelImpl</code>.
	 * </p>
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param start the lower bound of the range of commerce discount entry checks
	 * @param end the upper bound of the range of commerce discount entry checks (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching commerce discount entry checks
	 */
	@Override
	public List<CommerceDiscountEntryCheck> findByCAI_CDI(
		long commerceAccountId, long commerceDiscountId, int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		return findByCAI_CDI(
			commerceAccountId, commerceDiscountId, start, end,
			orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the commerce discount entry checks where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceDiscountEntryCheckModelImpl</code>.
	 * </p>
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param start the lower bound of the range of commerce discount entry checks
	 * @param end the upper bound of the range of commerce discount entry checks (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching commerce discount entry checks
	 */
	@Override
	public List<CommerceDiscountEntryCheck> findByCAI_CDI(
		long commerceAccountId, long commerceDiscountId, int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator,
		boolean useFinderCache) {

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByCAI_CDI;
				finderArgs = new Object[] {
					commerceAccountId, commerceDiscountId
				};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByCAI_CDI;
			finderArgs = new Object[] {
				commerceAccountId, commerceDiscountId, start, end,
				orderByComparator
			};
		}

		List<CommerceDiscountEntryCheck> list = null;

		if (useFinderCache) {
			list = (List<CommerceDiscountEntryCheck>)finderCache.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (CommerceDiscountEntryCheck commerceDiscountEntryCheck :
						list) {

					if ((commerceAccountId !=
							commerceDiscountEntryCheck.
								getCommerceAccountId()) ||
						(commerceDiscountId !=
							commerceDiscountEntryCheck.
								getCommerceDiscountId())) {

						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler sb = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					4 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				sb = new StringBundler(4);
			}

			sb.append(_SQL_SELECT_COMMERCEDISCOUNTENTRYCHECK_WHERE);

			sb.append(_FINDER_COLUMN_CAI_CDI_COMMERCEACCOUNTID_2);

			sb.append(_FINDER_COLUMN_CAI_CDI_COMMERCEDISCOUNTID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else {
				sb.append(CommerceDiscountEntryCheckModelImpl.ORDER_BY_JPQL);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(commerceAccountId);

				queryPos.add(commerceDiscountId);

				list = (List<CommerceDiscountEntryCheck>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first commerce discount entry check in the ordered set where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a matching commerce discount entry check could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck findByCAI_CDI_First(
			long commerceAccountId, long commerceDiscountId,
			OrderByComparator<CommerceDiscountEntryCheck> orderByComparator)
		throws NoSuchDiscountEntryCheckException {

		CommerceDiscountEntryCheck commerceDiscountEntryCheck =
			fetchByCAI_CDI_First(
				commerceAccountId, commerceDiscountId, orderByComparator);

		if (commerceDiscountEntryCheck != null) {
			return commerceDiscountEntryCheck;
		}

		StringBundler sb = new StringBundler(6);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("commerceAccountId=");
		sb.append(commerceAccountId);

		sb.append(", commerceDiscountId=");
		sb.append(commerceDiscountId);

		sb.append("}");

		throw new NoSuchDiscountEntryCheckException(sb.toString());
	}

	/**
	 * Returns the first commerce discount entry check in the ordered set where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce discount entry check, or <code>null</code> if a matching commerce discount entry check could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck fetchByCAI_CDI_First(
		long commerceAccountId, long commerceDiscountId,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		List<CommerceDiscountEntryCheck> list = findByCAI_CDI(
			commerceAccountId, commerceDiscountId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last commerce discount entry check in the ordered set where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a matching commerce discount entry check could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck findByCAI_CDI_Last(
			long commerceAccountId, long commerceDiscountId,
			OrderByComparator<CommerceDiscountEntryCheck> orderByComparator)
		throws NoSuchDiscountEntryCheckException {

		CommerceDiscountEntryCheck commerceDiscountEntryCheck =
			fetchByCAI_CDI_Last(
				commerceAccountId, commerceDiscountId, orderByComparator);

		if (commerceDiscountEntryCheck != null) {
			return commerceDiscountEntryCheck;
		}

		StringBundler sb = new StringBundler(6);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("commerceAccountId=");
		sb.append(commerceAccountId);

		sb.append(", commerceDiscountId=");
		sb.append(commerceDiscountId);

		sb.append("}");

		throw new NoSuchDiscountEntryCheckException(sb.toString());
	}

	/**
	 * Returns the last commerce discount entry check in the ordered set where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce discount entry check, or <code>null</code> if a matching commerce discount entry check could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck fetchByCAI_CDI_Last(
		long commerceAccountId, long commerceDiscountId,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		int count = countByCAI_CDI(commerceAccountId, commerceDiscountId);

		if (count == 0) {
			return null;
		}

		List<CommerceDiscountEntryCheck> list = findByCAI_CDI(
			commerceAccountId, commerceDiscountId, count - 1, count,
			orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the commerce discount entry checks before and after the current commerce discount entry check in the ordered set where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the current commerce discount entry check
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a commerce discount entry check with the primary key could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck[] findByCAI_CDI_PrevAndNext(
			long commerceDiscountEntryCheckId, long commerceAccountId,
			long commerceDiscountId,
			OrderByComparator<CommerceDiscountEntryCheck> orderByComparator)
		throws NoSuchDiscountEntryCheckException {

		CommerceDiscountEntryCheck commerceDiscountEntryCheck =
			findByPrimaryKey(commerceDiscountEntryCheckId);

		Session session = null;

		try {
			session = openSession();

			CommerceDiscountEntryCheck[] array =
				new CommerceDiscountEntryCheckImpl[3];

			array[0] = getByCAI_CDI_PrevAndNext(
				session, commerceDiscountEntryCheck, commerceAccountId,
				commerceDiscountId, orderByComparator, true);

			array[1] = commerceDiscountEntryCheck;

			array[2] = getByCAI_CDI_PrevAndNext(
				session, commerceDiscountEntryCheck, commerceAccountId,
				commerceDiscountId, orderByComparator, false);

			return array;
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected CommerceDiscountEntryCheck getByCAI_CDI_PrevAndNext(
		Session session, CommerceDiscountEntryCheck commerceDiscountEntryCheck,
		long commerceAccountId, long commerceDiscountId,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator,
		boolean previous) {

		StringBundler sb = null;

		if (orderByComparator != null) {
			sb = new StringBundler(
				5 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			sb = new StringBundler(4);
		}

		sb.append(_SQL_SELECT_COMMERCEDISCOUNTENTRYCHECK_WHERE);

		sb.append(_FINDER_COLUMN_CAI_CDI_COMMERCEACCOUNTID_2);

		sb.append(_FINDER_COLUMN_CAI_CDI_COMMERCEDISCOUNTID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				sb.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				sb.append(_ORDER_BY_ENTITY_ALIAS);
				sb.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						sb.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(WHERE_GREATER_THAN);
					}
					else {
						sb.append(WHERE_LESSER_THAN);
					}
				}
			}

			sb.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				sb.append(_ORDER_BY_ENTITY_ALIAS);
				sb.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						sb.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(ORDER_BY_ASC);
					}
					else {
						sb.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			sb.append(CommerceDiscountEntryCheckModelImpl.ORDER_BY_JPQL);
		}

		String sql = sb.toString();

		Query query = session.createQuery(sql);

		query.setFirstResult(0);
		query.setMaxResults(2);

		QueryPos queryPos = QueryPos.getInstance(query);

		queryPos.add(commerceAccountId);

		queryPos.add(commerceDiscountId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						commerceDiscountEntryCheck)) {

				queryPos.add(orderByConditionValue);
			}
		}

		List<CommerceDiscountEntryCheck> list = query.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the commerce discount entry checks where commerceAccountId = &#63; and commerceDiscountId = &#63; from the database.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 */
	@Override
	public void removeByCAI_CDI(
		long commerceAccountId, long commerceDiscountId) {

		for (CommerceDiscountEntryCheck commerceDiscountEntryCheck :
				findByCAI_CDI(
					commerceAccountId, commerceDiscountId, QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, null)) {

			remove(commerceDiscountEntryCheck);
		}
	}

	/**
	 * Returns the number of commerce discount entry checks where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @return the number of matching commerce discount entry checks
	 */
	@Override
	public int countByCAI_CDI(long commerceAccountId, long commerceDiscountId) {
		FinderPath finderPath = _finderPathCountByCAI_CDI;

		Object[] finderArgs = new Object[] {
			commerceAccountId, commerceDiscountId
		};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs, this);

		if (count == null) {
			StringBundler sb = new StringBundler(3);

			sb.append(_SQL_COUNT_COMMERCEDISCOUNTENTRYCHECK_WHERE);

			sb.append(_FINDER_COLUMN_CAI_CDI_COMMERCEACCOUNTID_2);

			sb.append(_FINDER_COLUMN_CAI_CDI_COMMERCEDISCOUNTID_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(commerceAccountId);

				queryPos.add(commerceDiscountId);

				count = (Long)query.uniqueResult();

				finderCache.putResult(finderPath, finderArgs, count);
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_CAI_CDI_COMMERCEACCOUNTID_2 =
		"commerceDiscountEntryCheck.commerceAccountId = ? AND ";

	private static final String _FINDER_COLUMN_CAI_CDI_COMMERCEDISCOUNTID_2 =
		"commerceDiscountEntryCheck.commerceDiscountId = ?";

	public CommerceDiscountEntryCheckPersistenceImpl() {
		setModelClass(CommerceDiscountEntryCheck.class);

		setModelImplClass(CommerceDiscountEntryCheckImpl.class);
		setModelPKClass(long.class);

		setTable(CommerceDiscountEntryCheckTable.INSTANCE);
	}

	/**
	 * Caches the commerce discount entry check in the entity cache if it is enabled.
	 *
	 * @param commerceDiscountEntryCheck the commerce discount entry check
	 */
	@Override
	public void cacheResult(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck) {

		entityCache.putResult(
			CommerceDiscountEntryCheckImpl.class,
			commerceDiscountEntryCheck.getPrimaryKey(),
			commerceDiscountEntryCheck);
	}

	private int _valueObjectFinderCacheListThreshold;

	/**
	 * Caches the commerce discount entry checks in the entity cache if it is enabled.
	 *
	 * @param commerceDiscountEntryChecks the commerce discount entry checks
	 */
	@Override
	public void cacheResult(
		List<CommerceDiscountEntryCheck> commerceDiscountEntryChecks) {

		if ((_valueObjectFinderCacheListThreshold == 0) ||
			((_valueObjectFinderCacheListThreshold > 0) &&
			 (commerceDiscountEntryChecks.size() >
				 _valueObjectFinderCacheListThreshold))) {

			return;
		}

		for (CommerceDiscountEntryCheck commerceDiscountEntryCheck :
				commerceDiscountEntryChecks) {

			if (entityCache.getResult(
					CommerceDiscountEntryCheckImpl.class,
					commerceDiscountEntryCheck.getPrimaryKey()) == null) {

				cacheResult(commerceDiscountEntryCheck);
			}
		}
	}

	/**
	 * Clears the cache for all commerce discount entry checks.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache() {
		entityCache.clearCache(CommerceDiscountEntryCheckImpl.class);

		finderCache.clearCache(CommerceDiscountEntryCheckImpl.class);
	}

	/**
	 * Clears the cache for the commerce discount entry check.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck) {

		entityCache.removeResult(
			CommerceDiscountEntryCheckImpl.class, commerceDiscountEntryCheck);
	}

	@Override
	public void clearCache(
		List<CommerceDiscountEntryCheck> commerceDiscountEntryChecks) {

		for (CommerceDiscountEntryCheck commerceDiscountEntryCheck :
				commerceDiscountEntryChecks) {

			entityCache.removeResult(
				CommerceDiscountEntryCheckImpl.class,
				commerceDiscountEntryCheck);
		}
	}

	@Override
	public void clearCache(Set<Serializable> primaryKeys) {
		finderCache.clearCache(CommerceDiscountEntryCheckImpl.class);

		for (Serializable primaryKey : primaryKeys) {
			entityCache.removeResult(
				CommerceDiscountEntryCheckImpl.class, primaryKey);
		}
	}

	/**
	 * Creates a new commerce discount entry check with the primary key. Does not add the commerce discount entry check to the database.
	 *
	 * @param commerceDiscountEntryCheckId the primary key for the new commerce discount entry check
	 * @return the new commerce discount entry check
	 */
	@Override
	public CommerceDiscountEntryCheck create(
		long commerceDiscountEntryCheckId) {

		CommerceDiscountEntryCheck commerceDiscountEntryCheck =
			new CommerceDiscountEntryCheckImpl();

		commerceDiscountEntryCheck.setNew(true);
		commerceDiscountEntryCheck.setPrimaryKey(commerceDiscountEntryCheckId);

		commerceDiscountEntryCheck.setCompanyId(
			CompanyThreadLocal.getCompanyId());

		return commerceDiscountEntryCheck;
	}

	/**
	 * Removes the commerce discount entry check with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the commerce discount entry check
	 * @return the commerce discount entry check that was removed
	 * @throws NoSuchDiscountEntryCheckException if a commerce discount entry check with the primary key could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck remove(long commerceDiscountEntryCheckId)
		throws NoSuchDiscountEntryCheckException {

		return remove((Serializable)commerceDiscountEntryCheckId);
	}

	/**
	 * Removes the commerce discount entry check with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param primaryKey the primary key of the commerce discount entry check
	 * @return the commerce discount entry check that was removed
	 * @throws NoSuchDiscountEntryCheckException if a commerce discount entry check with the primary key could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck remove(Serializable primaryKey)
		throws NoSuchDiscountEntryCheckException {

		Session session = null;

		try {
			session = openSession();

			CommerceDiscountEntryCheck commerceDiscountEntryCheck =
				(CommerceDiscountEntryCheck)session.get(
					CommerceDiscountEntryCheckImpl.class, primaryKey);

			if (commerceDiscountEntryCheck == null) {
				if (_log.isDebugEnabled()) {
					_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
				}

				throw new NoSuchDiscountEntryCheckException(
					_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			return remove(commerceDiscountEntryCheck);
		}
		catch (NoSuchDiscountEntryCheckException noSuchEntityException) {
			throw noSuchEntityException;
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	protected CommerceDiscountEntryCheck removeImpl(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck) {

		Session session = null;

		try {
			session = openSession();

			if (!session.contains(commerceDiscountEntryCheck)) {
				commerceDiscountEntryCheck =
					(CommerceDiscountEntryCheck)session.get(
						CommerceDiscountEntryCheckImpl.class,
						commerceDiscountEntryCheck.getPrimaryKeyObj());
			}

			if (commerceDiscountEntryCheck != null) {
				session.delete(commerceDiscountEntryCheck);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		if (commerceDiscountEntryCheck != null) {
			clearCache(commerceDiscountEntryCheck);
		}

		return commerceDiscountEntryCheck;
	}

	@Override
	public CommerceDiscountEntryCheck updateImpl(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck) {

		boolean isNew = commerceDiscountEntryCheck.isNew();

		if (!(commerceDiscountEntryCheck instanceof
				CommerceDiscountEntryCheckModelImpl)) {

			InvocationHandler invocationHandler = null;

			if (ProxyUtil.isProxyClass(commerceDiscountEntryCheck.getClass())) {
				invocationHandler = ProxyUtil.getInvocationHandler(
					commerceDiscountEntryCheck);

				throw new IllegalArgumentException(
					"Implement ModelWrapper in commerceDiscountEntryCheck proxy " +
						invocationHandler.getClass());
			}

			throw new IllegalArgumentException(
				"Implement ModelWrapper in custom CommerceDiscountEntryCheck implementation " +
					commerceDiscountEntryCheck.getClass());
		}

		CommerceDiscountEntryCheckModelImpl
			commerceDiscountEntryCheckModelImpl =
				(CommerceDiscountEntryCheckModelImpl)commerceDiscountEntryCheck;

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		Date date = new Date();

		if (isNew && (commerceDiscountEntryCheck.getCreateDate() == null)) {
			if (serviceContext == null) {
				commerceDiscountEntryCheck.setCreateDate(date);
			}
			else {
				commerceDiscountEntryCheck.setCreateDate(
					serviceContext.getCreateDate(date));
			}
		}

		if (!commerceDiscountEntryCheckModelImpl.hasSetModifiedDate()) {
			if (serviceContext == null) {
				commerceDiscountEntryCheck.setModifiedDate(date);
			}
			else {
				commerceDiscountEntryCheck.setModifiedDate(
					serviceContext.getModifiedDate(date));
			}
		}

		Session session = null;

		try {
			session = openSession();

			if (isNew) {
				session.save(commerceDiscountEntryCheck);
			}
			else {
				commerceDiscountEntryCheck =
					(CommerceDiscountEntryCheck)session.merge(
						commerceDiscountEntryCheck);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		entityCache.putResult(
			CommerceDiscountEntryCheckImpl.class,
			commerceDiscountEntryCheckModelImpl, false, true);

		if (isNew) {
			commerceDiscountEntryCheck.setNew(false);
		}

		commerceDiscountEntryCheck.resetOriginalValues();

		return commerceDiscountEntryCheck;
	}

	/**
	 * Returns the commerce discount entry check with the primary key or throws a <code>com.liferay.portal.kernel.exception.NoSuchModelException</code> if it could not be found.
	 *
	 * @param primaryKey the primary key of the commerce discount entry check
	 * @return the commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a commerce discount entry check with the primary key could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck findByPrimaryKey(Serializable primaryKey)
		throws NoSuchDiscountEntryCheckException {

		CommerceDiscountEntryCheck commerceDiscountEntryCheck =
			fetchByPrimaryKey(primaryKey);

		if (commerceDiscountEntryCheck == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			throw new NoSuchDiscountEntryCheckException(
				_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
		}

		return commerceDiscountEntryCheck;
	}

	/**
	 * Returns the commerce discount entry check with the primary key or throws a <code>NoSuchDiscountEntryCheckException</code> if it could not be found.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the commerce discount entry check
	 * @return the commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a commerce discount entry check with the primary key could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck findByPrimaryKey(
			long commerceDiscountEntryCheckId)
		throws NoSuchDiscountEntryCheckException {

		return findByPrimaryKey((Serializable)commerceDiscountEntryCheckId);
	}

	/**
	 * Returns the commerce discount entry check with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the commerce discount entry check
	 * @return the commerce discount entry check, or <code>null</code> if a commerce discount entry check with the primary key could not be found
	 */
	@Override
	public CommerceDiscountEntryCheck fetchByPrimaryKey(
		long commerceDiscountEntryCheckId) {

		return fetchByPrimaryKey((Serializable)commerceDiscountEntryCheckId);
	}

	/**
	 * Returns all the commerce discount entry checks.
	 *
	 * @return the commerce discount entry checks
	 */
	@Override
	public List<CommerceDiscountEntryCheck> findAll() {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the commerce discount entry checks.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceDiscountEntryCheckModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of commerce discount entry checks
	 * @param end the upper bound of the range of commerce discount entry checks (not inclusive)
	 * @return the range of commerce discount entry checks
	 */
	@Override
	public List<CommerceDiscountEntryCheck> findAll(int start, int end) {
		return findAll(start, end, null);
	}

	/**
	 * Returns an ordered range of all the commerce discount entry checks.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceDiscountEntryCheckModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of commerce discount entry checks
	 * @param end the upper bound of the range of commerce discount entry checks (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of commerce discount entry checks
	 */
	@Override
	public List<CommerceDiscountEntryCheck> findAll(
		int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		return findAll(start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the commerce discount entry checks.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>CommerceDiscountEntryCheckModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of commerce discount entry checks
	 * @param end the upper bound of the range of commerce discount entry checks (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of commerce discount entry checks
	 */
	@Override
	public List<CommerceDiscountEntryCheck> findAll(
		int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator,
		boolean useFinderCache) {

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindAll;
				finderArgs = FINDER_ARGS_EMPTY;
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindAll;
			finderArgs = new Object[] {start, end, orderByComparator};
		}

		List<CommerceDiscountEntryCheck> list = null;

		if (useFinderCache) {
			list = (List<CommerceDiscountEntryCheck>)finderCache.getResult(
				finderPath, finderArgs, this);
		}

		if (list == null) {
			StringBundler sb = null;
			String sql = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					2 + (orderByComparator.getOrderByFields().length * 2));

				sb.append(_SQL_SELECT_COMMERCEDISCOUNTENTRYCHECK);

				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);

				sql = sb.toString();
			}
			else {
				sql = _SQL_SELECT_COMMERCEDISCOUNTENTRYCHECK;

				sql = sql.concat(
					CommerceDiscountEntryCheckModelImpl.ORDER_BY_JPQL);
			}

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				list = (List<CommerceDiscountEntryCheck>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Removes all the commerce discount entry checks from the database.
	 *
	 */
	@Override
	public void removeAll() {
		for (CommerceDiscountEntryCheck commerceDiscountEntryCheck :
				findAll()) {

			remove(commerceDiscountEntryCheck);
		}
	}

	/**
	 * Returns the number of commerce discount entry checks.
	 *
	 * @return the number of commerce discount entry checks
	 */
	@Override
	public int countAll() {
		Long count = (Long)finderCache.getResult(
			_finderPathCountAll, FINDER_ARGS_EMPTY, this);

		if (count == null) {
			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(
					_SQL_COUNT_COMMERCEDISCOUNTENTRYCHECK);

				count = (Long)query.uniqueResult();

				finderCache.putResult(
					_finderPathCountAll, FINDER_ARGS_EMPTY, count);
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	@Override
	protected EntityCache getEntityCache() {
		return entityCache;
	}

	@Override
	protected String getPKDBName() {
		return "commerceDiscountEntryCheckId";
	}

	@Override
	protected String getSelectSQL() {
		return _SQL_SELECT_COMMERCEDISCOUNTENTRYCHECK;
	}

	@Override
	protected Map<String, Integer> getTableColumnsMap() {
		return CommerceDiscountEntryCheckModelImpl.TABLE_COLUMNS_MAP;
	}

	/**
	 * Initializes the commerce discount entry check persistence.
	 */
	@Activate
	public void activate() {
		_valueObjectFinderCacheListThreshold = GetterUtil.getInteger(
			PropsUtil.get(PropsKeys.VALUE_OBJECT_FINDER_CACHE_LIST_THRESHOLD));

		_finderPathWithPaginationFindAll = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findAll", new String[0],
			new String[0], true);

		_finderPathWithoutPaginationFindAll = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findAll", new String[0],
			new String[0], true);

		_finderPathCountAll = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countAll",
			new String[0], new String[0], false);

		_finderPathWithPaginationFindByCommerceDiscountId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByCommerceDiscountId",
			new String[] {
				Long.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			},
			new String[] {"commerceDiscountId"}, true);

		_finderPathWithoutPaginationFindByCommerceDiscountId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"findByCommerceDiscountId", new String[] {Long.class.getName()},
			new String[] {"commerceDiscountId"}, true);

		_finderPathCountByCommerceDiscountId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"countByCommerceDiscountId", new String[] {Long.class.getName()},
			new String[] {"commerceDiscountId"}, false);

		_finderPathWithPaginationFindByCAI_CDI = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByCAI_CDI",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Integer.class.getName(), Integer.class.getName(),
				OrderByComparator.class.getName()
			},
			new String[] {"commerceAccountId", "commerceDiscountId"}, true);

		_finderPathWithoutPaginationFindByCAI_CDI = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByCAI_CDI",
			new String[] {Long.class.getName(), Long.class.getName()},
			new String[] {"commerceAccountId", "commerceDiscountId"}, true);

		_finderPathCountByCAI_CDI = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByCAI_CDI",
			new String[] {Long.class.getName(), Long.class.getName()},
			new String[] {"commerceAccountId", "commerceDiscountId"}, false);

		CommerceDiscountEntryCheckUtil.setPersistence(this);
	}

	@Deactivate
	public void deactivate() {
		CommerceDiscountEntryCheckUtil.setPersistence(null);

		entityCache.removeCache(CommerceDiscountEntryCheckImpl.class.getName());
	}

	@Override
	@Reference(
		target = CommercePersistenceConstants.SERVICE_CONFIGURATION_FILTER,
		unbind = "-"
	)
	public void setConfiguration(Configuration configuration) {
	}

	@Override
	@Reference(
		target = CommercePersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Override
	@Reference(
		target = CommercePersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	@Reference
	protected EntityCache entityCache;

	@Reference
	protected FinderCache finderCache;

	private static final String _SQL_SELECT_COMMERCEDISCOUNTENTRYCHECK =
		"SELECT commerceDiscountEntryCheck FROM CommerceDiscountEntryCheck commerceDiscountEntryCheck";

	private static final String _SQL_SELECT_COMMERCEDISCOUNTENTRYCHECK_WHERE =
		"SELECT commerceDiscountEntryCheck FROM CommerceDiscountEntryCheck commerceDiscountEntryCheck WHERE ";

	private static final String _SQL_COUNT_COMMERCEDISCOUNTENTRYCHECK =
		"SELECT COUNT(commerceDiscountEntryCheck) FROM CommerceDiscountEntryCheck commerceDiscountEntryCheck";

	private static final String _SQL_COUNT_COMMERCEDISCOUNTENTRYCHECK_WHERE =
		"SELECT COUNT(commerceDiscountEntryCheck) FROM CommerceDiscountEntryCheck commerceDiscountEntryCheck WHERE ";

	private static final String _ORDER_BY_ENTITY_ALIAS =
		"commerceDiscountEntryCheck.";

	private static final String _NO_SUCH_ENTITY_WITH_PRIMARY_KEY =
		"No CommerceDiscountEntryCheck exists with the primary key ";

	private static final String _NO_SUCH_ENTITY_WITH_KEY =
		"No CommerceDiscountEntryCheck exists with the key {";

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceDiscountEntryCheckPersistenceImpl.class);

	@Override
	protected FinderCache getFinderCache() {
		return finderCache;
	}

}