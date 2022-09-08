/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.search.experiences.internal.upgrade.v2_0_0.util;

import java.sql.Types;

import java.util.HashMap;
import java.util.Map;

/**
 * @author	  Brian Wing Shun Chan
 * @generated
 */
public class SXPElementTable {

	public static final String TABLE_NAME = "SXPElement";

	public static final Object[][] TABLE_COLUMNS = {
		{"mvccVersion", Types.BIGINT}, {"uuid_", Types.VARCHAR},
		{"externalReferenceCode", Types.VARCHAR},
		{"sxpElementId", Types.BIGINT}, {"companyId", Types.BIGINT},
		{"userId", Types.BIGINT}, {"userName", Types.VARCHAR},
		{"createDate", Types.TIMESTAMP}, {"modifiedDate", Types.TIMESTAMP},
		{"description", Types.VARCHAR}, {"elementDefinitionJSON", Types.CLOB},
		{"hidden_", Types.BOOLEAN}, {"readOnly", Types.BOOLEAN},
		{"schemaVersion", Types.VARCHAR}, {"title", Types.VARCHAR},
		{"type_", Types.INTEGER}, {"version", Types.VARCHAR},
		{"status", Types.INTEGER}
	};

	public static final Map<String, Integer> TABLE_COLUMNS_MAP =
new HashMap<String, Integer>();

static {
TABLE_COLUMNS_MAP.put("mvccVersion", Types.BIGINT);

TABLE_COLUMNS_MAP.put("uuid_", Types.VARCHAR);

TABLE_COLUMNS_MAP.put("externalReferenceCode", Types.VARCHAR);

TABLE_COLUMNS_MAP.put("sxpElementId", Types.BIGINT);

TABLE_COLUMNS_MAP.put("companyId", Types.BIGINT);

TABLE_COLUMNS_MAP.put("userId", Types.BIGINT);

TABLE_COLUMNS_MAP.put("userName", Types.VARCHAR);

TABLE_COLUMNS_MAP.put("createDate", Types.TIMESTAMP);

TABLE_COLUMNS_MAP.put("modifiedDate", Types.TIMESTAMP);

TABLE_COLUMNS_MAP.put("description", Types.VARCHAR);

TABLE_COLUMNS_MAP.put("elementDefinitionJSON", Types.CLOB);

TABLE_COLUMNS_MAP.put("hidden_", Types.BOOLEAN);

TABLE_COLUMNS_MAP.put("readOnly", Types.BOOLEAN);

TABLE_COLUMNS_MAP.put("schemaVersion", Types.VARCHAR);

TABLE_COLUMNS_MAP.put("title", Types.VARCHAR);

TABLE_COLUMNS_MAP.put("type_", Types.INTEGER);

TABLE_COLUMNS_MAP.put("version", Types.VARCHAR);

TABLE_COLUMNS_MAP.put("status", Types.INTEGER);

}
	public static final String TABLE_SQL_CREATE =
"create table SXPElement (mvccVersion LONG default 0 not null,uuid_ VARCHAR(75) null,externalReferenceCode VARCHAR(75) null,sxpElementId LONG not null primary key,companyId LONG,userId LONG,userName VARCHAR(75) null,createDate DATE null,modifiedDate DATE null,description STRING null,elementDefinitionJSON TEXT null,hidden_ BOOLEAN,readOnly BOOLEAN,schemaVersion VARCHAR(75) null,title STRING null,type_ INTEGER,version VARCHAR(75) null,status INTEGER)";

	public static final String TABLE_SQL_DROP = "drop table SXPElement";

	public static final String[] TABLE_SQL_ADD_INDEXES = {
		"create unique index IX_D4D87BCC on SXPElement (companyId, externalReferenceCode[$COLUMN_LENGTH:75$])",
		"create index IX_62CF31E7 on SXPElement (companyId, readOnly)",
		"create index IX_2F49914A on SXPElement (companyId, type_, status)",
		"create index IX_34C38FAB on SXPElement (uuid_[$COLUMN_LENGTH:75$], companyId)"
	};

}