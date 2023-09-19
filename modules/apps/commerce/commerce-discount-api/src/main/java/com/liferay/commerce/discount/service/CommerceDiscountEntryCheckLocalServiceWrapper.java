/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.service;

import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.service.persistence.BasePersistence;

/**
 * Provides a wrapper for {@link CommerceDiscountEntryCheckLocalService}.
 *
 * @author Marco Leo
 * @see CommerceDiscountEntryCheckLocalService
 * @generated
 */
public class CommerceDiscountEntryCheckLocalServiceWrapper
	implements CommerceDiscountEntryCheckLocalService,
			   ServiceWrapper<CommerceDiscountEntryCheckLocalService> {

	public CommerceDiscountEntryCheckLocalServiceWrapper() {
		this(null);
	}

	public CommerceDiscountEntryCheckLocalServiceWrapper(
		CommerceDiscountEntryCheckLocalService
			commerceDiscountEntryCheckLocalService) {

		_commerceDiscountEntryCheckLocalService =
			commerceDiscountEntryCheckLocalService;
	}

	/**
	 * Adds the commerce discount entry check to the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect CommerceDiscountEntryCheckLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param commerceDiscountEntryCheck the commerce discount entry check
	 * @return the commerce discount entry check that was added
	 */
	@Override
	public com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
		addCommerceDiscountEntryCheck(
			com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
				commerceDiscountEntryCheck) {

		return _commerceDiscountEntryCheckLocalService.
			addCommerceDiscountEntryCheck(commerceDiscountEntryCheck);
	}

	@Override
	public com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
			addCommerceDiscountEntryCheck(
				long userId, long commerceAccountId, long commerceDiscountId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountEntryCheckLocalService.
			addCommerceDiscountEntryCheck(
				userId, commerceAccountId, commerceDiscountId);
	}

	/**
	 * Creates a new commerce discount entry check with the primary key. Does not add the commerce discount entry check to the database.
	 *
	 * @param commerceDiscountEntryCheckId the primary key for the new commerce discount entry check
	 * @return the new commerce discount entry check
	 */
	@Override
	public com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
		createCommerceDiscountEntryCheck(long commerceDiscountEntryCheckId) {

		return _commerceDiscountEntryCheckLocalService.
			createCommerceDiscountEntryCheck(commerceDiscountEntryCheckId);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel createPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountEntryCheckLocalService.createPersistedModel(
			primaryKeyObj);
	}

	/**
	 * Deletes the commerce discount entry check from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect CommerceDiscountEntryCheckLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param commerceDiscountEntryCheck the commerce discount entry check
	 * @return the commerce discount entry check that was removed
	 */
	@Override
	public com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
		deleteCommerceDiscountEntryCheck(
			com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
				commerceDiscountEntryCheck) {

		return _commerceDiscountEntryCheckLocalService.
			deleteCommerceDiscountEntryCheck(commerceDiscountEntryCheck);
	}

	/**
	 * Deletes the commerce discount entry check with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect CommerceDiscountEntryCheckLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the commerce discount entry check
	 * @return the commerce discount entry check that was removed
	 * @throws PortalException if a commerce discount entry check with the primary key could not be found
	 */
	@Override
	public com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
			deleteCommerceDiscountEntryCheck(long commerceDiscountEntryCheckId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountEntryCheckLocalService.
			deleteCommerceDiscountEntryCheck(commerceDiscountEntryCheckId);
	}

	@Override
	public void deleteCommerceDiscountEntryChecks(long commerceDiscountId)
		throws com.liferay.portal.kernel.exception.PortalException {

		_commerceDiscountEntryCheckLocalService.
			deleteCommerceDiscountEntryChecks(commerceDiscountId);
	}

	@Override
	public void deleteCommerceDiscountEntryChecks(
			long commerceAccountId, long commerceDiscountId)
		throws com.liferay.portal.kernel.exception.PortalException {

		_commerceDiscountEntryCheckLocalService.
			deleteCommerceDiscountEntryChecks(
				commerceAccountId, commerceDiscountId);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel deletePersistedModel(
			com.liferay.portal.kernel.model.PersistedModel persistedModel)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountEntryCheckLocalService.deletePersistedModel(
			persistedModel);
	}

	@Override
	public <T> T dslQuery(com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {
		return _commerceDiscountEntryCheckLocalService.dslQuery(dslQuery);
	}

	@Override
	public int dslQueryCount(
		com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {

		return _commerceDiscountEntryCheckLocalService.dslQueryCount(dslQuery);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery() {
		return _commerceDiscountEntryCheckLocalService.dynamicQuery();
	}

	/**
	 * Performs a dynamic query on the database and returns the matching rows.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the matching rows
	 */
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {

		return _commerceDiscountEntryCheckLocalService.dynamicQuery(
			dynamicQuery);
	}

	/**
	 * Performs a dynamic query on the database and returns a range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.discount.model.impl.CommerceDiscountEntryCheckModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @return the range of matching rows
	 */
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end) {

		return _commerceDiscountEntryCheckLocalService.dynamicQuery(
			dynamicQuery, start, end);
	}

	/**
	 * Performs a dynamic query on the database and returns an ordered range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.discount.model.impl.CommerceDiscountEntryCheckModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching rows
	 */
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end,
		com.liferay.portal.kernel.util.OrderByComparator<T> orderByComparator) {

		return _commerceDiscountEntryCheckLocalService.dynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the number of rows matching the dynamic query
	 */
	@Override
	public long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {

		return _commerceDiscountEntryCheckLocalService.dynamicQueryCount(
			dynamicQuery);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @param projection the projection to apply to the query
	 * @return the number of rows matching the dynamic query
	 */
	@Override
	public long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery,
		com.liferay.portal.kernel.dao.orm.Projection projection) {

		return _commerceDiscountEntryCheckLocalService.dynamicQueryCount(
			dynamicQuery, projection);
	}

	@Override
	public com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
		fetchCommerceDiscountEntryCheck(long commerceDiscountEntryCheckId) {

		return _commerceDiscountEntryCheckLocalService.
			fetchCommerceDiscountEntryCheck(commerceDiscountEntryCheckId);
	}

	@Override
	public com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
		fetchCommerceDiscountEntryCheck(
			long commerceAccountId, long commerceDiscountId) {

		return _commerceDiscountEntryCheckLocalService.
			fetchCommerceDiscountEntryCheck(
				commerceAccountId, commerceDiscountId);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return _commerceDiscountEntryCheckLocalService.
			getActionableDynamicQuery();
	}

	/**
	 * Returns the commerce discount entry check with the primary key.
	 *
	 * @param commerceDiscountEntryCheckId the primary key of the commerce discount entry check
	 * @return the commerce discount entry check
	 * @throws PortalException if a commerce discount entry check with the primary key could not be found
	 */
	@Override
	public com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
			getCommerceDiscountEntryCheck(long commerceDiscountEntryCheckId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountEntryCheckLocalService.
			getCommerceDiscountEntryCheck(commerceDiscountEntryCheckId);
	}

	@Override
	public com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
			getCommerceDiscountEntryCheck(
				long commerceAccountId, long commerceDiscountId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountEntryCheckLocalService.
			getCommerceDiscountEntryCheck(
				commerceAccountId, commerceDiscountId);
	}

	/**
	 * Returns a range of all the commerce discount entry checks.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.commerce.discount.model.impl.CommerceDiscountEntryCheckModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of commerce discount entry checks
	 * @param end the upper bound of the range of commerce discount entry checks (not inclusive)
	 * @return the range of commerce discount entry checks
	 */
	@Override
	public java.util.List
		<com.liferay.commerce.discount.model.CommerceDiscountEntryCheck>
			getCommerceDiscountEntryChecks(int start, int end) {

		return _commerceDiscountEntryCheckLocalService.
			getCommerceDiscountEntryChecks(start, end);
	}

	/**
	 * Returns the number of commerce discount entry checks.
	 *
	 * @return the number of commerce discount entry checks
	 */
	@Override
	public int getCommerceDiscountEntryChecksCount() {
		return _commerceDiscountEntryCheckLocalService.
			getCommerceDiscountEntryChecksCount();
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
		getIndexableActionableDynamicQuery() {

		return _commerceDiscountEntryCheckLocalService.
			getIndexableActionableDynamicQuery();
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _commerceDiscountEntryCheckLocalService.
			getOSGiServiceIdentifier();
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel getPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _commerceDiscountEntryCheckLocalService.getPersistedModel(
			primaryKeyObj);
	}

	/**
	 * Updates the commerce discount entry check in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect CommerceDiscountEntryCheckLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param commerceDiscountEntryCheck the commerce discount entry check
	 * @return the commerce discount entry check that was updated
	 */
	@Override
	public com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
		updateCommerceDiscountEntryCheck(
			com.liferay.commerce.discount.model.CommerceDiscountEntryCheck
				commerceDiscountEntryCheck) {

		return _commerceDiscountEntryCheckLocalService.
			updateCommerceDiscountEntryCheck(commerceDiscountEntryCheck);
	}

	@Override
	public BasePersistence<?> getBasePersistence() {
		return _commerceDiscountEntryCheckLocalService.getBasePersistence();
	}

	@Override
	public CommerceDiscountEntryCheckLocalService getWrappedService() {
		return _commerceDiscountEntryCheckLocalService;
	}

	@Override
	public void setWrappedService(
		CommerceDiscountEntryCheckLocalService
			commerceDiscountEntryCheckLocalService) {

		_commerceDiscountEntryCheckLocalService =
			commerceDiscountEntryCheckLocalService;
	}

	private CommerceDiscountEntryCheckLocalService
		_commerceDiscountEntryCheckLocalService;

}