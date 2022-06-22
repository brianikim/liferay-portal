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
import com.liferay.portal.kernel.search.filter.TermsFilter;
import com.liferay.portal.search.test.util.indexing.BaseIndexingTestCase;
import com.liferay.portal.search.test.util.indexing.DocumentCreationHelpers;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author André de Oliveira
 */
public abstract class BaseTermsFilterTestCase extends BaseIndexingTestCase {

	@Test
	public void testBasicSearch() throws Exception {
		index("One");
		index("Two");
		index("Three");

		assertTermsFilterValue(new String[] {"Two", "Three"});
	}

	@Test
	public void testFilterWithEmptyStringValue() throws Exception {
		index("One");

		assertTermsFilterValue(new String[] {""});
	}

	@Test
	public void testFilterWithSpacedFieldName() throws Exception {
		String fieldName = "expando__keyword__custom_fields__spaced name";

		index(fieldName, "one");

		assertTermsFilterFieldName(fieldName, new String[] {"one"});
	}

	@Test
	public void testLuceneSpecialCharacters() throws Exception {
		index("One\\+-!():^[]\"{}~*?|&/Two");
		index("Three");

		assertTermsFilterValue(
			new String[] {"One\\+-!():^[]\"{}~*?|&/Two", "Three"});
	}

	@Test
	public void testSolrSpecialCharacters() throws Exception {
		index("One\\+-!():^[]\"{}~*?|&/; Two");
		index("Three");

		assertTermsFilterValue(
			new String[] {"One\\+-!():^[]\"{}~*?|&/; Two", "Three"});
	}

	@Test
	public void testSpaces() throws Exception {
		index("One Two");
		index("Three");

		assertTermsFilterValue(new String[] {"One Two", "Three"});
	}

	protected void assertTermsFilterFieldName(
			String filterFieldName, String[] values)
		throws Exception {

		assertSearch(
			indexingTestHelper -> {
				indexingTestHelper.setFilter(
					new TermsFilter(filterFieldName) {
						{
							addValues(values);
						}
					});

				indexingTestHelper.search();

				StringBuilder sb = new StringBuilder(3);

				sb.append("Expected \"");
				sb.append(filterFieldName);
				sb.append("\" to be escaped in Solr and return a result.");

				Assert.assertEquals(
					sb.toString(), 1, indexingTestHelper.searchCount());
			});
	}

	protected void assertTermsFilterValue(String[] values) throws Exception {
		assertSearch(
			indexingTestHelper -> {
				indexingTestHelper.setFilter(
					new TermsFilter(_FIELD) {
						{
							addValues(values);
						}
					});

				indexingTestHelper.search();

				indexingTestHelper.assertValues(_FIELD, Arrays.asList(values));
			});
	}

	protected void index(String value) throws Exception {
		index(_FIELD, value);
	}

	protected void index(String fieldName, String value) throws Exception {
		addDocument(DocumentCreationHelpers.singleKeyword(fieldName, value));
	}

	private static final String _FIELD = Field.FOLDER_ID;

}