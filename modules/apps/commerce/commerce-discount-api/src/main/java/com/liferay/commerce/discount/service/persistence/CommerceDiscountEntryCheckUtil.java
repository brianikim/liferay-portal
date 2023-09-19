/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.service.persistence;

import com.liferay.commerce.discount.model.CommerceDiscountEntryCheck;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.io.Serializable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The persistence utility for the commerce discount entry check service. This utility wraps <code>com.liferay.commerce.discount.service.persistence.impl.CommerceDiscountEntryCheckPersistenceImpl</code> and provides direct access to the database for CRUD operations. This utility should only be used by the service layer, as it must operate within a transaction. Never access this utility in a JSP, controller, model, or other front-end class.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Marco Leo
 * @see CommerceDiscountEntryCheckPersistence
 * @generated
 */
public class CommerceDiscountEntryCheckUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#clearCache()
	 */
	public static void clearCache() {
		getPersistence().clearCache();
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#clearCache(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static void clearCache(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck) {

		getPersistence().clearCache(commerceDiscountEntryCheck);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#countWithDynamicQuery(DynamicQuery)
	 */
	public static long countWithDynamicQuery(DynamicQuery dynamicQuery) {
		return getPersistence().countWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#fetchByPrimaryKeys(Set)
	 */
	public static Map<Serializable, CommerceDiscountEntryCheck>
		fetchByPrimaryKeys(Set<Serializable> primaryKeys) {

		return getPersistence().fetchByPrimaryKeys(primaryKeys);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery)
	 */
	public static List<CommerceDiscountEntryCheck> findWithDynamicQuery(
		DynamicQuery dynamicQuery) {

		return getPersistence().findWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int)
	 */
	public static List<CommerceDiscountEntryCheck> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end) {

		return getPersistence().findWithDynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int, OrderByComparator)
	 */
	public static List<CommerceDiscountEntryCheck> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		return getPersistence().findWithDynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static CommerceDiscountEntryCheck update(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck) {

		return getPersistence().update(commerceDiscountEntryCheck);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel, ServiceContext)
	 */
	public static CommerceDiscountEntryCheck update(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck,
		ServiceContext serviceContext) {

		return getPersistence().update(
			commerceDiscountEntryCheck, serviceContext);
	}

	/**
	 * Returns all the commerce discount entry checks where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @return the matching commerce discount entry checks
	 */
	public static List<CommerceDiscountEntryCheck> findByCommerceDiscountId(
		long commerceDiscountId) {

		return getPersistence().findByCommerceDiscountId(commerceDiscountId);
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
	public static List<CommerceDiscountEntryCheck> findByCommerceDiscountId(
		long commerceDiscountId, int start, int end) {

		return getPersistence().findByCommerceDiscountId(
			commerceDiscountId, start, end);
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
	public static List<CommerceDiscountEntryCheck> findByCommerceDiscountId(
		long commerceDiscountId, int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		return getPersistence().findByCommerceDiscountId(
			commerceDiscountId, start, end, orderByComparator);
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
	public static List<CommerceDiscountEntryCheck> findByCommerceDiscountId(
		long commerceDiscountId, int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByCommerceDiscountId(
			commerceDiscountId, start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a matching commerce discount entry check could not be found
	 */
	public static CommerceDiscountEntryCheck findByCommerceDiscountId_First(
			long commerceDiscountId,
			OrderByComparator<CommerceDiscountEntryCheck> orderByComparator)
		throws com.liferay.commerce.discount.exception.
			NoSuchDiscountEntryCheckException {

		return getPersistence().findByCommerceDiscountId_First(
			commerceDiscountId, orderByComparator);
	}

	/**
	 * Returns the first commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce discount entry check, or <code>null</code> if a matching commerce discount entry check could not be found
	 */
	public static CommerceDiscountEntryCheck fetchByCommerceDiscountId_First(
		long commerceDiscountId,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		return getPersistence().fetchByCommerceDiscountId_First(
			commerceDiscountId, orderByComparator);
	}

	/**
	 * Returns the last commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a matching commerce discount entry check could not be found
	 */
	public static CommerceDiscountEntryCheck findByCommerceDiscountId_Last(
			long commerceDiscountId,
			OrderByComparator<CommerceDiscountEntryCheck> orderByComparator)
		throws com.liferay.commerce.discount.exception.
			NoSuchDiscountEntryCheckException {

		return getPersistence().findByCommerceDiscountId_Last(
			commerceDiscountId, orderByComparator);
	}

	/**
	 * Returns the last commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce discount entry check, or <code>null</code> if a matching commerce discount entry check could not be found
	 */
	public static CommerceDiscountEntryCheck fetchByCommerceDiscountId_Last(
		long commerceDiscountId,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		return getPersistence().fetchByCommerceDiscountId_Last(
			commerceDiscountId, orderByComparator);
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
	public static CommerceDiscountEntryCheck[]
			findByCommerceDiscountId_PrevAndNext(
				long commerceDiscountEntryCheckId, long commerceDiscountId,
				OrderByComparator<CommerceDiscountEntryCheck> orderByComparator)
		throws com.liferay.commerce.discount.exception.
			NoSuchDiscountEntryCheckException {

		return getPersistence().findByCommerceDiscountId_PrevAndNext(
			commerceDiscountEntryCheckId, commerceDiscountId,
			orderByComparator);
	}

	/**
	 * Removes all the commerce discount entry checks where commerceDiscountId = &#63; from the database.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 */
	public static void removeByCommerceDiscountId(long commerceDiscountId) {
		getPersistence().removeByCommerceDiscountId(commerceDiscountId);
	}

	/**
	 * Returns the number of commerce discount entry checks where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @return the number of matching commerce discount entry checks
	 */
	public static int countByCommerceDiscountId(long commerceDiscountId) {
		return getPersistence().countByCommerceDiscountId(commerceDiscountId);
	}

	/**
	 * Returns all the commerce discount entry checks where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @return the matching commerce discount entry checks
	 */
	public static List<CommerceDiscountEntryCheck> findByCAI_CDI(
		long commerceAccountId, long commerceDiscountId) {

		return getPersistence().findByCAI_CDI(
			commerceAccountId, commerceDiscountId);
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
	public static List<CommerceDiscountEntryCheck> findByCAI_CDI(
		long commerceAccountId, long commerceDiscountId, int start, int end) {

		return getPersistence().findByCAI_CDI(
			commerceAccountId, commerceDiscountId, start, end);
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
	public static List<CommerceDiscountEntryCheck> findByCAI_CDI(
		long commerceAccountId, long commerceDiscountId, int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		return getPersistence().findByCAI_CDI(
			commerceAccountId, commerceDiscountId, start, end,
			orderByComparator);
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
	public static List<CommerceDiscountEntryCheck> findByCAI_CDI(
		long commerceAccountId, long commerceDiscountId, int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByCAI_CDI(
			commerceAccountId, commerceDiscountId, start, end,
			orderByComparator, useFinderCache);
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
	public static CommerceDiscountEntryCheck findByCAI_CDI_First(
			long commerceAccountId, long commerceDiscountId,
			OrderByComparator<CommerceDiscountEntryCheck> orderByComparator)
		throws com.liferay.commerce.discount.exception.
			NoSuchDiscountEntryCheckException {

		return getPersistence().findByCAI_CDI_First(
			commerceAccountId, commerceDiscountId, orderByComparator);
	}

	/**
	 * Returns the first commerce discount entry check in the ordered set where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce discount entry check, or <code>null</code> if a matching commerce discount entry check could not be found
	 */
	public static CommerceDiscountEntryCheck fetchByCAI_CDI_First(
		long commerceAccountId, long commerceDiscountId,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		return getPersistence().fetchByCAI_CDI_First(
			commerceAccountId, commerceDiscountId, orderByComparator);
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
	public static CommerceDiscountEntryCheck findByCAI_CDI_Last(
			long commerceAccountId, long commerceDiscountId,
			OrderByComparator<CommerceDiscountEntryCheck> orderByComparator)
		throws com.liferay.commerce.discount.exception.
			NoSuchDiscountEntryCheckException {

		return getPersistence().findByCAI_CDI_Last(
			commerceAccountId, commerceDiscountId, orderByComparator);
	}

	/**
	 * Returns the last commerce discount entry check in the ordered set where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce discount entry check, or <code>null</code> if a matching commerce discount entry check could not be found
	 */
	public static CommerceDiscountEntryCheck fetchByCAI_CDI_Last(
		long commerceAccountId, long commerceDiscountId,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		return getPersistence().fetchByCAI_CDI_Last(
			commerceAccountId, commerceDiscountId, orderByComparator);
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
	public static CommerceDiscountEntryCheck[] findByCAI_CDI_PrevAndNext(
			long commerceDiscountEntryCheckId, long commerceAccountId,
			long commerceDiscountId,
			OrderByComparator<CommerceDiscountEntryCheck> orderByComparator)
		throws com.liferay.commerce.discount.exception.
			NoSuchDiscountEntryCheckException {

		return getPersistence().findByCAI_CDI_PrevAndNext(
			commerceDiscountEntryCheckId, commerceAccountId, commerceDiscountId,
			orderByComparator);
	}

	/**
	 * Removes all the commerce discount entry checks where commerceAccountId = &#63; and commerceDiscountId = &#63; from the database.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 */
	public static void removeByCAI_CDI(
		long commerceAccountId, long commerceDiscountId) {

		getPersistence().removeByCAI_CDI(commerceAccountId, commerceDiscountId);
	}

	/**
	 * Returns the number of commerce discount entry checks where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @return the number of matching commerce discount entry checks
	 */
	public static int countByCAI_CDI(
		long commerceAccountId, long commerceDiscountId) {

		return getPersistence().countByCAI_CDI(
			commerceAccountId, commerceDiscountId);
	}

	/**
	 * Caches the commerce discount entry check in the entity cache if it is enabled.
	 *
	 * @param commerceDiscountEntryCheck the commerce discount entry check
	 */
	public static void cacheResult(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck) {

		getPersistence().cacheResult(commerceDiscountEntryCheck);
	}

	/**
	 * Caches the commerce discount entry checks in the entity cache if it is enabled.
	 *
	 * @param commerceDiscountEntryChecks the commerce discount entry checks
	 */
	public static void cacheResult(
		List<CommerceDiscountEntryCheck> commerceDiscountEntryChecks) {

		getPersistence().cacheResult(commerceDiscountEntryChecks);
	}

	/**
	 * Creates a new commerce discount entry check with the primary key. Does not add the commerce discount entry check to the database.
	 *
	 * @param commerceDiscountEntryCheckId the primary key for the new commerce discount entry check
	 * @return the new commerce discount entry check
	 */
	public static CommerceDiscountEntryCheck create(
		long commerceDiscountEntryCheckId) {

		return getPersistence().create(commerceDiscountEntryCheckId);
	}

	/**
	 * Removes the commerce discount entry check with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the commerce discount entry check
	 * @return the commerce discount entry check that was removed
	 * @throws NoSuchDiscountEntryCheckException if a commerce discount entry check with the primary key could not be found
	 */
	public static CommerceDiscountEntryCheck remove(
			long commerceDiscountEntryCheckId)
		throws com.liferay.commerce.discount.exception.
			NoSuchDiscountEntryCheckException {

		return getPersistence().remove(commerceDiscountEntryCheckId);
	}

	public static CommerceDiscountEntryCheck updateImpl(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck) {

		return getPersistence().updateImpl(commerceDiscountEntryCheck);
	}

	/**
	 * Returns the commerce discount entry check with the primary key or throws a <code>NoSuchDiscountEntryCheckException</code> if it could not be found.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the commerce discount entry check
	 * @return the commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a commerce discount entry check with the primary key could not be found
	 */
	public static CommerceDiscountEntryCheck findByPrimaryKey(
			long commerceDiscountEntryCheckId)
		throws com.liferay.commerce.discount.exception.
			NoSuchDiscountEntryCheckException {

		return getPersistence().findByPrimaryKey(commerceDiscountEntryCheckId);
	}

	/**
	 * Returns the commerce discount entry check with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the commerce discount entry check
	 * @return the commerce discount entry check, or <code>null</code> if a commerce discount entry check with the primary key could not be found
	 */
	public static CommerceDiscountEntryCheck fetchByPrimaryKey(
		long commerceDiscountEntryCheckId) {

		return getPersistence().fetchByPrimaryKey(commerceDiscountEntryCheckId);
	}

	/**
	 * Returns all the commerce discount entry checks.
	 *
	 * @return the commerce discount entry checks
	 */
	public static List<CommerceDiscountEntryCheck> findAll() {
		return getPersistence().findAll();
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
	public static List<CommerceDiscountEntryCheck> findAll(int start, int end) {
		return getPersistence().findAll(start, end);
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
	public static List<CommerceDiscountEntryCheck> findAll(
		int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator) {

		return getPersistence().findAll(start, end, orderByComparator);
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
	public static List<CommerceDiscountEntryCheck> findAll(
		int start, int end,
		OrderByComparator<CommerceDiscountEntryCheck> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findAll(
			start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Removes all the commerce discount entry checks from the database.
	 */
	public static void removeAll() {
		getPersistence().removeAll();
	}

	/**
	 * Returns the number of commerce discount entry checks.
	 *
	 * @return the number of commerce discount entry checks
	 */
	public static int countAll() {
		return getPersistence().countAll();
	}

	public static CommerceDiscountEntryCheckPersistence getPersistence() {
		return _persistence;
	}

	public static void setPersistence(
		CommerceDiscountEntryCheckPersistence persistence) {

		_persistence = persistence;
	}

	private static volatile CommerceDiscountEntryCheckPersistence _persistence;

}