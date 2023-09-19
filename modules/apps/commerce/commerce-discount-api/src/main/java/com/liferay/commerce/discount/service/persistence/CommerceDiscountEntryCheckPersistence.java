/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.service.persistence;

import com.liferay.commerce.discount.exception.NoSuchDiscountEntryCheckException;
import com.liferay.commerce.discount.model.CommerceDiscountEntryCheck;
import com.liferay.portal.kernel.service.persistence.BasePersistence;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The persistence interface for the commerce discount entry check service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Marco Leo
 * @see CommerceDiscountEntryCheckUtil
 * @generated
 */
@ProviderType
public interface CommerceDiscountEntryCheckPersistence
	extends BasePersistence<CommerceDiscountEntryCheck> {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link CommerceDiscountEntryCheckUtil} to access the commerce discount entry check persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this interface.
	 */

	/**
	 * Returns all the commerce discount entry checks where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @return the matching commerce discount entry checks
	 */
	public java.util.List<CommerceDiscountEntryCheck> findByCommerceDiscountId(
		long commerceDiscountId);

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
	public java.util.List<CommerceDiscountEntryCheck> findByCommerceDiscountId(
		long commerceDiscountId, int start, int end);

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
	public java.util.List<CommerceDiscountEntryCheck> findByCommerceDiscountId(
		long commerceDiscountId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceDiscountEntryCheck> orderByComparator);

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
	public java.util.List<CommerceDiscountEntryCheck> findByCommerceDiscountId(
		long commerceDiscountId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceDiscountEntryCheck> orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a matching commerce discount entry check could not be found
	 */
	public CommerceDiscountEntryCheck findByCommerceDiscountId_First(
			long commerceDiscountId,
			com.liferay.portal.kernel.util.OrderByComparator
				<CommerceDiscountEntryCheck> orderByComparator)
		throws NoSuchDiscountEntryCheckException;

	/**
	 * Returns the first commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce discount entry check, or <code>null</code> if a matching commerce discount entry check could not be found
	 */
	public CommerceDiscountEntryCheck fetchByCommerceDiscountId_First(
		long commerceDiscountId,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceDiscountEntryCheck> orderByComparator);

	/**
	 * Returns the last commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a matching commerce discount entry check could not be found
	 */
	public CommerceDiscountEntryCheck findByCommerceDiscountId_Last(
			long commerceDiscountId,
			com.liferay.portal.kernel.util.OrderByComparator
				<CommerceDiscountEntryCheck> orderByComparator)
		throws NoSuchDiscountEntryCheckException;

	/**
	 * Returns the last commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce discount entry check, or <code>null</code> if a matching commerce discount entry check could not be found
	 */
	public CommerceDiscountEntryCheck fetchByCommerceDiscountId_Last(
		long commerceDiscountId,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceDiscountEntryCheck> orderByComparator);

	/**
	 * Returns the commerce discount entry checks before and after the current commerce discount entry check in the ordered set where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the current commerce discount entry check
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a commerce discount entry check with the primary key could not be found
	 */
	public CommerceDiscountEntryCheck[] findByCommerceDiscountId_PrevAndNext(
			long commerceDiscountEntryCheckId, long commerceDiscountId,
			com.liferay.portal.kernel.util.OrderByComparator
				<CommerceDiscountEntryCheck> orderByComparator)
		throws NoSuchDiscountEntryCheckException;

	/**
	 * Removes all the commerce discount entry checks where commerceDiscountId = &#63; from the database.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 */
	public void removeByCommerceDiscountId(long commerceDiscountId);

	/**
	 * Returns the number of commerce discount entry checks where commerceDiscountId = &#63;.
	 *
	 * @param commerceDiscountId the commerce discount ID
	 * @return the number of matching commerce discount entry checks
	 */
	public int countByCommerceDiscountId(long commerceDiscountId);

	/**
	 * Returns all the commerce discount entry checks where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @return the matching commerce discount entry checks
	 */
	public java.util.List<CommerceDiscountEntryCheck> findByCAI_CDI(
		long commerceAccountId, long commerceDiscountId);

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
	public java.util.List<CommerceDiscountEntryCheck> findByCAI_CDI(
		long commerceAccountId, long commerceDiscountId, int start, int end);

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
	public java.util.List<CommerceDiscountEntryCheck> findByCAI_CDI(
		long commerceAccountId, long commerceDiscountId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceDiscountEntryCheck> orderByComparator);

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
	public java.util.List<CommerceDiscountEntryCheck> findByCAI_CDI(
		long commerceAccountId, long commerceDiscountId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceDiscountEntryCheck> orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first commerce discount entry check in the ordered set where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a matching commerce discount entry check could not be found
	 */
	public CommerceDiscountEntryCheck findByCAI_CDI_First(
			long commerceAccountId, long commerceDiscountId,
			com.liferay.portal.kernel.util.OrderByComparator
				<CommerceDiscountEntryCheck> orderByComparator)
		throws NoSuchDiscountEntryCheckException;

	/**
	 * Returns the first commerce discount entry check in the ordered set where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching commerce discount entry check, or <code>null</code> if a matching commerce discount entry check could not be found
	 */
	public CommerceDiscountEntryCheck fetchByCAI_CDI_First(
		long commerceAccountId, long commerceDiscountId,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceDiscountEntryCheck> orderByComparator);

	/**
	 * Returns the last commerce discount entry check in the ordered set where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a matching commerce discount entry check could not be found
	 */
	public CommerceDiscountEntryCheck findByCAI_CDI_Last(
			long commerceAccountId, long commerceDiscountId,
			com.liferay.portal.kernel.util.OrderByComparator
				<CommerceDiscountEntryCheck> orderByComparator)
		throws NoSuchDiscountEntryCheckException;

	/**
	 * Returns the last commerce discount entry check in the ordered set where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching commerce discount entry check, or <code>null</code> if a matching commerce discount entry check could not be found
	 */
	public CommerceDiscountEntryCheck fetchByCAI_CDI_Last(
		long commerceAccountId, long commerceDiscountId,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceDiscountEntryCheck> orderByComparator);

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
	public CommerceDiscountEntryCheck[] findByCAI_CDI_PrevAndNext(
			long commerceDiscountEntryCheckId, long commerceAccountId,
			long commerceDiscountId,
			com.liferay.portal.kernel.util.OrderByComparator
				<CommerceDiscountEntryCheck> orderByComparator)
		throws NoSuchDiscountEntryCheckException;

	/**
	 * Removes all the commerce discount entry checks where commerceAccountId = &#63; and commerceDiscountId = &#63; from the database.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 */
	public void removeByCAI_CDI(
		long commerceAccountId, long commerceDiscountId);

	/**
	 * Returns the number of commerce discount entry checks where commerceAccountId = &#63; and commerceDiscountId = &#63;.
	 *
	 * @param commerceAccountId the commerce account ID
	 * @param commerceDiscountId the commerce discount ID
	 * @return the number of matching commerce discount entry checks
	 */
	public int countByCAI_CDI(long commerceAccountId, long commerceDiscountId);

	/**
	 * Caches the commerce discount entry check in the entity cache if it is enabled.
	 *
	 * @param commerceDiscountEntryCheck the commerce discount entry check
	 */
	public void cacheResult(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck);

	/**
	 * Caches the commerce discount entry checks in the entity cache if it is enabled.
	 *
	 * @param commerceDiscountEntryChecks the commerce discount entry checks
	 */
	public void cacheResult(
		java.util.List<CommerceDiscountEntryCheck> commerceDiscountEntryChecks);

	/**
	 * Creates a new commerce discount entry check with the primary key. Does not add the commerce discount entry check to the database.
	 *
	 * @param commerceDiscountEntryCheckId the primary key for the new commerce discount entry check
	 * @return the new commerce discount entry check
	 */
	public CommerceDiscountEntryCheck create(long commerceDiscountEntryCheckId);

	/**
	 * Removes the commerce discount entry check with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the commerce discount entry check
	 * @return the commerce discount entry check that was removed
	 * @throws NoSuchDiscountEntryCheckException if a commerce discount entry check with the primary key could not be found
	 */
	public CommerceDiscountEntryCheck remove(long commerceDiscountEntryCheckId)
		throws NoSuchDiscountEntryCheckException;

	public CommerceDiscountEntryCheck updateImpl(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck);

	/**
	 * Returns the commerce discount entry check with the primary key or throws a <code>NoSuchDiscountEntryCheckException</code> if it could not be found.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the commerce discount entry check
	 * @return the commerce discount entry check
	 * @throws NoSuchDiscountEntryCheckException if a commerce discount entry check with the primary key could not be found
	 */
	public CommerceDiscountEntryCheck findByPrimaryKey(
			long commerceDiscountEntryCheckId)
		throws NoSuchDiscountEntryCheckException;

	/**
	 * Returns the commerce discount entry check with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the commerce discount entry check
	 * @return the commerce discount entry check, or <code>null</code> if a commerce discount entry check with the primary key could not be found
	 */
	public CommerceDiscountEntryCheck fetchByPrimaryKey(
		long commerceDiscountEntryCheckId);

	/**
	 * Returns all the commerce discount entry checks.
	 *
	 * @return the commerce discount entry checks
	 */
	public java.util.List<CommerceDiscountEntryCheck> findAll();

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
	public java.util.List<CommerceDiscountEntryCheck> findAll(
		int start, int end);

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
	public java.util.List<CommerceDiscountEntryCheck> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceDiscountEntryCheck> orderByComparator);

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
	public java.util.List<CommerceDiscountEntryCheck> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator
			<CommerceDiscountEntryCheck> orderByComparator,
		boolean useFinderCache);

	/**
	 * Removes all the commerce discount entry checks from the database.
	 */
	public void removeAll();

	/**
	 * Returns the number of commerce discount entry checks.
	 *
	 * @return the number of commerce discount entry checks
	 */
	public int countAll();

}