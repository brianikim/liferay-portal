/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.discount.model;

import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.base.BaseTable;

import java.sql.Types;

import java.util.Date;

/**
 * The table class for the &quot;CommerceDiscountEntryCheck&quot; database table.
 *
 * @author Marco Leo
 * @see CommerceDiscountEntryCheck
 * @generated
 */
public class CommerceDiscountEntryCheckTable
	extends BaseTable<CommerceDiscountEntryCheckTable> {

	public static final CommerceDiscountEntryCheckTable INSTANCE =
		new CommerceDiscountEntryCheckTable();

	public final Column<CommerceDiscountEntryCheckTable, Long> mvccVersion =
		createColumn(
			"mvccVersion", Long.class, Types.BIGINT, Column.FLAG_NULLITY);
	public final Column<CommerceDiscountEntryCheckTable, Long>
		commerceDiscountEntryCheckId = createColumn(
			"commerceDiscountEntryCheckId", Long.class, Types.BIGINT,
			Column.FLAG_PRIMARY);
	public final Column<CommerceDiscountEntryCheckTable, Long> companyId =
		createColumn(
			"companyId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<CommerceDiscountEntryCheckTable, Long> userId =
		createColumn("userId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<CommerceDiscountEntryCheckTable, String> userName =
		createColumn(
			"userName", String.class, Types.VARCHAR, Column.FLAG_DEFAULT);
	public final Column<CommerceDiscountEntryCheckTable, Date> createDate =
		createColumn(
			"createDate", Date.class, Types.TIMESTAMP, Column.FLAG_DEFAULT);
	public final Column<CommerceDiscountEntryCheckTable, Date> modifiedDate =
		createColumn(
			"modifiedDate", Date.class, Types.TIMESTAMP, Column.FLAG_DEFAULT);
	public final Column<CommerceDiscountEntryCheckTable, Long>
		commerceAccountId = createColumn(
			"commerceAccountId", Long.class, Types.BIGINT, Column.FLAG_DEFAULT);
	public final Column<CommerceDiscountEntryCheckTable, Long>
		commerceDiscountId = createColumn(
			"commerceDiscountId", Long.class, Types.BIGINT,
			Column.FLAG_DEFAULT);

	private CommerceDiscountEntryCheckTable() {
		super(
			"CommerceDiscountEntryCheck", CommerceDiscountEntryCheckTable::new);
	}

}