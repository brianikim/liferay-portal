/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.model;

import com.liferay.portal.kernel.annotation.ImplementationClassName;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.util.Accessor;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The extended model interface for the CommerceDiscountEntryCheck service. Represents a row in the &quot;CommerceDiscountEntryCheck&quot; database table, with each column mapped to a property of this class.
 *
 * @author Marco Leo
 * @see CommerceDiscountEntryCheckModel
 * @generated
 */
@ImplementationClassName(
	"com.liferay.commerce.discount.model.impl.CommerceDiscountEntryCheckImpl"
)
@ProviderType
public interface CommerceDiscountEntryCheck
	extends CommerceDiscountEntryCheckModel, PersistedModel {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this interface directly. Add methods to <code>com.liferay.commerce.discount.model.impl.CommerceDiscountEntryCheckImpl</code> and rerun ServiceBuilder to automatically copy the method declarations to this interface.
	 */
	public static final Accessor<CommerceDiscountEntryCheck, Long>
		COMMERCE_DISCOUNT_ENTRY_CHECK_ID_ACCESSOR =
			new Accessor<CommerceDiscountEntryCheck, Long>() {

				@Override
				public Long get(
					CommerceDiscountEntryCheck commerceDiscountEntryCheck) {

					return commerceDiscountEntryCheck.
						getCommerceDiscountEntryCheckId();
				}

				@Override
				public Class<Long> getAttributeClass() {
					return Long.class;
				}

				@Override
				public Class<CommerceDiscountEntryCheck> getTypeClass() {
					return CommerceDiscountEntryCheck.class;
				}

			};

}