/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.model;

import com.liferay.portal.kernel.model.ModelWrapper;
import com.liferay.portal.kernel.model.wrapper.BaseModelWrapper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class is a wrapper for {@link CommerceDiscountEntryCheck}.
 * </p>
 *
 * @author Marco Leo
 * @see CommerceDiscountEntryCheck
 * @generated
 */
public class CommerceDiscountEntryCheckWrapper
	extends BaseModelWrapper<CommerceDiscountEntryCheck>
	implements CommerceDiscountEntryCheck,
			   ModelWrapper<CommerceDiscountEntryCheck> {

	public CommerceDiscountEntryCheckWrapper(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck) {

		super(commerceDiscountEntryCheck);
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("mvccVersion", getMvccVersion());
		attributes.put(
			"commerceDiscountEntryCheckId", getCommerceDiscountEntryCheckId());
		attributes.put("companyId", getCompanyId());
		attributes.put("userId", getUserId());
		attributes.put("userName", getUserName());
		attributes.put("createDate", getCreateDate());
		attributes.put("modifiedDate", getModifiedDate());
		attributes.put("commerceAccountId", getCommerceAccountId());
		attributes.put("commerceDiscountId", getCommerceDiscountId());

		return attributes;
	}

	@Override
	public void setModelAttributes(Map<String, Object> attributes) {
		Long mvccVersion = (Long)attributes.get("mvccVersion");

		if (mvccVersion != null) {
			setMvccVersion(mvccVersion);
		}

		Long commerceDiscountEntryCheckId = (Long)attributes.get(
			"commerceDiscountEntryCheckId");

		if (commerceDiscountEntryCheckId != null) {
			setCommerceDiscountEntryCheckId(commerceDiscountEntryCheckId);
		}

		Long companyId = (Long)attributes.get("companyId");

		if (companyId != null) {
			setCompanyId(companyId);
		}

		Long userId = (Long)attributes.get("userId");

		if (userId != null) {
			setUserId(userId);
		}

		String userName = (String)attributes.get("userName");

		if (userName != null) {
			setUserName(userName);
		}

		Date createDate = (Date)attributes.get("createDate");

		if (createDate != null) {
			setCreateDate(createDate);
		}

		Date modifiedDate = (Date)attributes.get("modifiedDate");

		if (modifiedDate != null) {
			setModifiedDate(modifiedDate);
		}

		Long commerceAccountId = (Long)attributes.get("commerceAccountId");

		if (commerceAccountId != null) {
			setCommerceAccountId(commerceAccountId);
		}

		Long commerceDiscountId = (Long)attributes.get("commerceDiscountId");

		if (commerceDiscountId != null) {
			setCommerceDiscountId(commerceDiscountId);
		}
	}

	@Override
	public CommerceDiscountEntryCheck cloneWithOriginalValues() {
		return wrap(model.cloneWithOriginalValues());
	}

	/**
	 * Returns the commerce account ID of this commerce discount entry check.
	 *
	 * @return the commerce account ID of this commerce discount entry check
	 */
	@Override
	public long getCommerceAccountId() {
		return model.getCommerceAccountId();
	}

	/**
	 * Returns the commerce discount entry check ID of this commerce discount entry check.
	 *
	 * @return the commerce discount entry check ID of this commerce discount entry check
	 */
	@Override
	public long getCommerceDiscountEntryCheckId() {
		return model.getCommerceDiscountEntryCheckId();
	}

	/**
	 * Returns the commerce discount ID of this commerce discount entry check.
	 *
	 * @return the commerce discount ID of this commerce discount entry check
	 */
	@Override
	public long getCommerceDiscountId() {
		return model.getCommerceDiscountId();
	}

	/**
	 * Returns the company ID of this commerce discount entry check.
	 *
	 * @return the company ID of this commerce discount entry check
	 */
	@Override
	public long getCompanyId() {
		return model.getCompanyId();
	}

	/**
	 * Returns the create date of this commerce discount entry check.
	 *
	 * @return the create date of this commerce discount entry check
	 */
	@Override
	public Date getCreateDate() {
		return model.getCreateDate();
	}

	/**
	 * Returns the modified date of this commerce discount entry check.
	 *
	 * @return the modified date of this commerce discount entry check
	 */
	@Override
	public Date getModifiedDate() {
		return model.getModifiedDate();
	}

	/**
	 * Returns the mvcc version of this commerce discount entry check.
	 *
	 * @return the mvcc version of this commerce discount entry check
	 */
	@Override
	public long getMvccVersion() {
		return model.getMvccVersion();
	}

	/**
	 * Returns the primary key of this commerce discount entry check.
	 *
	 * @return the primary key of this commerce discount entry check
	 */
	@Override
	public long getPrimaryKey() {
		return model.getPrimaryKey();
	}

	/**
	 * Returns the user ID of this commerce discount entry check.
	 *
	 * @return the user ID of this commerce discount entry check
	 */
	@Override
	public long getUserId() {
		return model.getUserId();
	}

	/**
	 * Returns the user name of this commerce discount entry check.
	 *
	 * @return the user name of this commerce discount entry check
	 */
	@Override
	public String getUserName() {
		return model.getUserName();
	}

	/**
	 * Returns the user uuid of this commerce discount entry check.
	 *
	 * @return the user uuid of this commerce discount entry check
	 */
	@Override
	public String getUserUuid() {
		return model.getUserUuid();
	}

	@Override
	public void persist() {
		model.persist();
	}

	/**
	 * Sets the commerce account ID of this commerce discount entry check.
	 *
	 * @param commerceAccountId the commerce account ID of this commerce discount entry check
	 */
	@Override
	public void setCommerceAccountId(long commerceAccountId) {
		model.setCommerceAccountId(commerceAccountId);
	}

	/**
	 * Sets the commerce discount entry check ID of this commerce discount entry check.
	 *
	 * @param commerceDiscountEntryCheckId the commerce discount entry check ID of this commerce discount entry check
	 */
	@Override
	public void setCommerceDiscountEntryCheckId(
		long commerceDiscountEntryCheckId) {

		model.setCommerceDiscountEntryCheckId(commerceDiscountEntryCheckId);
	}

	/**
	 * Sets the commerce discount ID of this commerce discount entry check.
	 *
	 * @param commerceDiscountId the commerce discount ID of this commerce discount entry check
	 */
	@Override
	public void setCommerceDiscountId(long commerceDiscountId) {
		model.setCommerceDiscountId(commerceDiscountId);
	}

	/**
	 * Sets the company ID of this commerce discount entry check.
	 *
	 * @param companyId the company ID of this commerce discount entry check
	 */
	@Override
	public void setCompanyId(long companyId) {
		model.setCompanyId(companyId);
	}

	/**
	 * Sets the create date of this commerce discount entry check.
	 *
	 * @param createDate the create date of this commerce discount entry check
	 */
	@Override
	public void setCreateDate(Date createDate) {
		model.setCreateDate(createDate);
	}

	/**
	 * Sets the modified date of this commerce discount entry check.
	 *
	 * @param modifiedDate the modified date of this commerce discount entry check
	 */
	@Override
	public void setModifiedDate(Date modifiedDate) {
		model.setModifiedDate(modifiedDate);
	}

	/**
	 * Sets the mvcc version of this commerce discount entry check.
	 *
	 * @param mvccVersion the mvcc version of this commerce discount entry check
	 */
	@Override
	public void setMvccVersion(long mvccVersion) {
		model.setMvccVersion(mvccVersion);
	}

	/**
	 * Sets the primary key of this commerce discount entry check.
	 *
	 * @param primaryKey the primary key of this commerce discount entry check
	 */
	@Override
	public void setPrimaryKey(long primaryKey) {
		model.setPrimaryKey(primaryKey);
	}

	/**
	 * Sets the user ID of this commerce discount entry check.
	 *
	 * @param userId the user ID of this commerce discount entry check
	 */
	@Override
	public void setUserId(long userId) {
		model.setUserId(userId);
	}

	/**
	 * Sets the user name of this commerce discount entry check.
	 *
	 * @param userName the user name of this commerce discount entry check
	 */
	@Override
	public void setUserName(String userName) {
		model.setUserName(userName);
	}

	/**
	 * Sets the user uuid of this commerce discount entry check.
	 *
	 * @param userUuid the user uuid of this commerce discount entry check
	 */
	@Override
	public void setUserUuid(String userUuid) {
		model.setUserUuid(userUuid);
	}

	@Override
	public String toXmlString() {
		return model.toXmlString();
	}

	@Override
	protected CommerceDiscountEntryCheckWrapper wrap(
		CommerceDiscountEntryCheck commerceDiscountEntryCheck) {

		return new CommerceDiscountEntryCheckWrapper(
			commerceDiscountEntryCheck);
	}

}