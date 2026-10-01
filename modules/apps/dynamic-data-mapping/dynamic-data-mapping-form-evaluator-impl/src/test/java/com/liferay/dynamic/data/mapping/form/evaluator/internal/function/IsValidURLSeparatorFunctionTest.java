/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.dynamic.data.mapping.form.evaluator.internal.function;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Brian I. Kim
 */
public class IsValidURLSeparatorFunctionTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testApply() {
		IsValidURLSeparatorFunction isValidURLSeparatorFunction =
			new IsValidURLSeparatorFunction();

		Assert.assertTrue(isValidURLSeparatorFunction.apply("p"));
		Assert.assertFalse(isValidURLSeparatorFunction.apply("-"));
		Assert.assertFalse(isValidURLSeparatorFunction.apply("b"));
		Assert.assertFalse(isValidURLSeparatorFunction.apply("d"));
		Assert.assertFalse(isValidURLSeparatorFunction.apply("w"));
		Assert.assertFalse(isValidURLSeparatorFunction.apply("~"));
		Assert.assertFalse(isValidURLSeparatorFunction.apply("À"));
	}

}