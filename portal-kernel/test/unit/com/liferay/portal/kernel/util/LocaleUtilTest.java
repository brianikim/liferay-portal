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

package com.liferay.portal.kernel.util;

import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.test.CaptureHandler;
import com.liferay.portal.kernel.test.JDKLoggerTestUtil;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;
import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Wesley Gong
 */
@PrepareForTest(LanguageUtil.class)
@RunWith(PowerMockRunner.class)
public class LocaleUtilTest extends PowerMockito {

	@Test
	public void testFromLanguageId() {
		mockStatic(LanguageUtil.class);

		when(
			LanguageUtil.isAvailableLocale(Locale.US)
		).thenReturn(
			true
		);

		Mockito.when(
			LanguageUtil.isAvailableLanguageCode("en")
		).thenReturn(
			false
		);

		Mockito.when(
			LanguageUtil.isAvailableLanguageCode("fr")
		).thenReturn(
			true
		);

		try (CaptureHandler captureHandler =
				JDKLoggerTestUtil.configureJDKLogger(
					LocaleUtil.class.getName(), Level.WARNING)) {

			List<LogRecord> logRecords = captureHandler.getLogRecords();

			Assert.assertEquals(Locale.US, LocaleUtil.fromLanguageId("en_US"));
			Assert.assertEquals(logRecords.toString(), 0, logRecords.size());

			logRecords.clear();

			LocaleUtil.fromLanguageId("en");

			Assert.assertEquals(logRecords.toString(), 1, logRecords.size());

			LogRecord logRecord = logRecords.get(0);

			Assert.assertEquals(
				"en is not a valid language id", logRecord.getMessage());

			logRecords.clear();

			Assert.assertEquals(Locale.FRENCH, LocaleUtil.fromLanguageId("fr"));
			Assert.assertEquals(logRecords.toString(), 0, logRecords.size());
		}
	}

	@Test
	public void testFromLanguageIdBCP47() {
		mockStatic(LanguageUtil.class);

		when(
			LanguageUtil.isAvailableLocale(Locale.US)
		).thenReturn(
			true
		);

		Assert.assertEquals(Locale.US, LocaleUtil.fromLanguageId("en-US"));

		when(
			LanguageUtil.isAvailableLocale(Locale.SIMPLIFIED_CHINESE)
		).thenReturn(
			true
		);

		Assert.assertEquals(
			Locale.SIMPLIFIED_CHINESE, LocaleUtil.fromLanguageId("zh-Hans-CN"));

		when(
			LanguageUtil.isAvailableLocale(Locale.TRADITIONAL_CHINESE)
		).thenReturn(
			true
		);

		Assert.assertEquals(
			Locale.TRADITIONAL_CHINESE,
			LocaleUtil.fromLanguageId("zh-Hant-TW"));
	}

	@Test
	public void testFromLanguageIdLocaleIsCreatedAndRetrievableWhenNoValidationDone() {
		LanguageUtil languageUtil = new LanguageUtil();

		Language language = Mockito.mock(Language.class);

		languageUtil.setLanguage(language);

		Mockito.when(
			language.isAvailableLocale(Locale.ITALY)
		).thenReturn(
			false
		);

		Assert.assertNotNull(LocaleUtil.fromLanguageId("it_IT", false));

		Assert.assertSame(
			LocaleUtil.fromLanguageId("it_IT", false),
			LocaleUtil.fromLanguageId("it_IT", false));
	}

	@Test
	public void testFromLanguageValidation() {
		LanguageUtil languageUtil = new LanguageUtil();

		Language language = Mockito.mock(Language.class);

		languageUtil.setLanguage(language);

		Mockito.when(
			language.isAvailableLocale(Locale.GERMANY)
		).thenReturn(
			false
		);

		Assert.assertEquals(
			Locale.GERMANY, LocaleUtil.fromLanguageId("de_DE", false, false));
		Assert.assertNull(LocaleUtil.fromLanguageId("de_DE", true, false));
	}

	@Test
	public void testGetLongDisplayName() {
		mockStatic(LanguageUtil.class);

		when(
			LanguageUtil.isBetaLocale(Matchers.anyObject())
		).thenReturn(
			false
		);

		Set<String> duplicateLanguages = Collections.singleton("ca");

		Assert.assertEquals(
			"English",
			LocaleUtil.getLongDisplayName(Locale.US, duplicateLanguages));

		Locale catalanLocale = new Locale("ca", "ES");

		Assert.assertEquals(
			"catal\u00e0 (Espanya)",
			LocaleUtil.getLongDisplayName(catalanLocale, duplicateLanguages));

		Locale catalanValenciaLocale = new Locale("ca", "ES", "VALENCIA");

		Assert.assertEquals(
			"catal\u00e0 (Espanya, VALENCIA)",
			LocaleUtil.getLongDisplayName(
				catalanValenciaLocale, duplicateLanguages));
	}

}