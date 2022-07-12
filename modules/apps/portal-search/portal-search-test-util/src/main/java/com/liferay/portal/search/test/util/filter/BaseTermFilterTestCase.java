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

package com.liferay.portal.search.test.util.filter;

import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.filter.TermFilter;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author André de Oliveira
 * @author Eric Yan
 */
public abstract class BaseTermFilterTestCase extends BaseIndexingTestCase {

	@Test
	public void testBasicSearch() {
		index("One");

		assertTermFilterValue("One", "One");

		assertTermFilterValue("one", "");
	}

	@Test
	public void testFilterWithEmptyStringValue() {
		index("One");

		assertTermFilterValue("", "");
	}

	@Test
	public void testLuceneSpecialCharacters() {
		String value = "One\\+-!():^[]\"{}~*?|&/Two";

		index(value);

		assertTermFilterValue(value, value);
	}

	@Test
	public void testSpacedFieldName() {
		String fieldName = "expando__keyword__custom_fields__spaced name";
		String value = "one";

		index(fieldName, value);

		assertTermFilterFieldName(fieldName, value);
	}

	@Test
	public void testSpaces() {
		index("One Two");

		assertTermFilterValue("One Two", "One Two");

		assertTermFilterValue("One", "");
		assertTermFilterValue("Two", "");
	}

	@Test
	public void testSpecialCharacters() {
		String value = "One\\+-!():^[]\"{}~*?|&/; Two";

		index(value);

		assertTermFilterValue(value, value);
	}

	protected void assertTermFilterFieldName(String fieldName, String value) {
		assertSearch(
			indexingTestHelper -> {
				indexingTestHelper.setFilter(new TermFilter(fieldName, value));

				indexingTestHelper.search();

				StringBuilder sb = new StringBuilder(3);

				sb.append("Expected \"");
				sb.append(fieldName);
				sb.append("\" to be escaped in Solr and return a result.");

				Assert.assertEquals(
					sb.toString(), 1, indexingTestHelper.searchCount());
			});
	}

	protected void assertTermFilterValue(
		String filterValue, String expectedValue) {

		assertSearch(
			indexingTestHelper -> {
				indexingTestHelper.setFilter(
					new TermFilter(_FIELD, filterValue));

				indexingTestHelper.search();

				indexingTestHelper.assertValues(
					_FIELD, Arrays.asList(expectedValue));
			});
	}

	protected void index(String value) {
		index(_FIELD, value);
	}

	protected void index(String fieldName, String value) {
		addDocument(DocumentCreationHelpers.singleKeyword(fieldName, value));
	}

	private static final String _FIELD = Field.FOLDER_ID;

}