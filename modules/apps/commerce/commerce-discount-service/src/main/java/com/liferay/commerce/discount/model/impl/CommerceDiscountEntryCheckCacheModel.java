/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.model.impl;

import com.liferay.commerce.discount.model.CommerceDiscountEntryCheck;
import com.liferay.petra.lang.HashUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.kernel.model.MVCCModel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Date;

/**
 * The cache model class for representing CommerceDiscountEntryCheck in entity cache.
 *
 * @author Marco Leo
 * @generated
 */
public class CommerceDiscountEntryCheckCacheModel
	implements CacheModel<CommerceDiscountEntryCheck>, Externalizable,
			   MVCCModel {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof CommerceDiscountEntryCheckCacheModel)) {
			return false;
		}

		CommerceDiscountEntryCheckCacheModel
			commerceDiscountEntryCheckCacheModel =
				(CommerceDiscountEntryCheckCacheModel)object;

		if ((commerceDiscountEntryCheckId ==
				commerceDiscountEntryCheckCacheModel.
					commerceDiscountEntryCheckId) &&
			(mvccVersion == commerceDiscountEntryCheckCacheModel.mvccVersion)) {

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = HashUtil.hash(0, commerceDiscountEntryCheckId);

		return HashUtil.hash(hashCode, mvccVersion);
	}

	@Override
	public long getMvccVersion() {
		return mvccVersion;
	}

	@Override
	public void setMvccVersion(long mvccVersion) {
		this.mvccVersion = mvccVersion;
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(19);

		sb.append("{mvccVersion=");
		sb.append(mvccVersion);
		sb.append(", commerceDiscountEntryCheckId=");
		sb.append(commerceDiscountEntryCheckId);
		sb.append(", companyId=");
		sb.append(companyId);
		sb.append(", userId=");
		sb.append(userId);
		sb.append(", userName=");
		sb.append(userName);
		sb.append(", createDate=");
		sb.append(createDate);
		sb.append(", modifiedDate=");
		sb.append(modifiedDate);
		sb.append(", commerceAccountId=");
		sb.append(commerceAccountId);
		sb.append(", commerceDiscountId=");
		sb.append(commerceDiscountId);
		sb.append("}");

		return sb.toString();
	}

	@Override
	public CommerceDiscountEntryCheck toEntityModel() {
		CommerceDiscountEntryCheckImpl commerceDiscountEntryCheckImpl =
			new CommerceDiscountEntryCheckImpl();

		commerceDiscountEntryCheckImpl.setMvccVersion(mvccVersion);
		commerceDiscountEntryCheckImpl.setCommerceDiscountEntryCheckId(
			commerceDiscountEntryCheckId);
		commerceDiscountEntryCheckImpl.setCompanyId(companyId);
		commerceDiscountEntryCheckImpl.setUserId(userId);

		if (userName == null) {
			commerceDiscountEntryCheckImpl.setUserName("");
		}
		else {
			commerceDiscountEntryCheckImpl.setUserName(userName);
		}

		if (createDate == Long.MIN_VALUE) {
			commerceDiscountEntryCheckImpl.setCreateDate(null);
		}
		else {
			commerceDiscountEntryCheckImpl.setCreateDate(new Date(createDate));
		}

		if (modifiedDate == Long.MIN_VALUE) {
			commerceDiscountEntryCheckImpl.setModifiedDate(null);
		}
		else {
			commerceDiscountEntryCheckImpl.setModifiedDate(
				new Date(modifiedDate));
		}

		commerceDiscountEntryCheckImpl.setCommerceAccountId(commerceAccountId);
		commerceDiscountEntryCheckImpl.setCommerceDiscountId(
			commerceDiscountId);

		commerceDiscountEntryCheckImpl.resetOriginalValues();

		return commerceDiscountEntryCheckImpl;
	}

	@Override
	public void readExternal(ObjectInput objectInput) throws IOException {
		mvccVersion = objectInput.readLong();

		commerceDiscountEntryCheckId = objectInput.readLong();

		companyId = objectInput.readLong();

		userId = objectInput.readLong();
		userName = objectInput.readUTF();
		createDate = objectInput.readLong();
		modifiedDate = objectInput.readLong();

		commerceAccountId = objectInput.readLong();

		commerceDiscountId = objectInput.readLong();
	}

	@Override
	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		objectOutput.writeLong(mvccVersion);

		objectOutput.writeLong(commerceDiscountEntryCheckId);

		objectOutput.writeLong(companyId);

		objectOutput.writeLong(userId);

		if (userName == null) {
			objectOutput.writeUTF("");
		}
		else {
			objectOutput.writeUTF(userName);
		}

		objectOutput.writeLong(createDate);
		objectOutput.writeLong(modifiedDate);

		objectOutput.writeLong(commerceAccountId);

		objectOutput.writeLong(commerceDiscountId);
	}

	public long mvccVersion;
	public long commerceDiscountEntryCheckId;
	public long companyId;
	public long userId;
	public String userName;
	public long createDate;
	public long modifiedDate;
	public long commerceAccountId;
	public long commerceDiscountId;

}