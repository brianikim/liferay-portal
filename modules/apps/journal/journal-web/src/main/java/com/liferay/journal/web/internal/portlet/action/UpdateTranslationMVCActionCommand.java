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

package com.liferay.journal.web.internal.portlet.action;

import com.liferay.info.field.InfoField;
import com.liferay.info.field.InfoFieldValue;
import com.liferay.info.form.InfoForm;
import com.liferay.info.item.InfoItemFieldValues;
import com.liferay.info.item.InfoItemReference;
import com.liferay.info.item.InfoItemServiceTracker;
import com.liferay.info.item.provider.InfoItemFieldValuesProvider;
import com.liferay.info.item.provider.InfoItemFormProvider;
import com.liferay.info.localized.InfoLocalizedValue;
import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.translation.service.TranslationEntryService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alicia Garcia
 */
@Component(
	property = {
		"javax.portlet.name=" + JournalPortletKeys.JOURNAL,
		"mvc.command.name=/journal/update_translation"
	},
	service = MVCActionCommand.class
)
public class UpdateTranslationMVCActionCommand extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		try {
			JournalArticle article = ActionUtil.getArticle(actionRequest);

			InfoItemReference infoItemReference = new InfoItemReference(
				JournalArticle.class.getName(), article.getResourcePrimKey());

			InfoItemFieldValues infoItemFieldValues =
				InfoItemFieldValues.builder(
				).infoItemReference(
					infoItemReference
				).infoFieldValues(
					_getInfoFieldValues(actionRequest, article)
				).build();

			ServiceContext serviceContext = ServiceContextFactory.getInstance(
				actionRequest);

			_translationEntryService.addOrUpdateTranslationEntry(
				article.getGroupId(), _getTargetLanguageId(actionRequest),
				infoItemReference, infoItemFieldValues, serviceContext);
		}
		catch (Exception exception) {
			_log.error(exception, exception);

			SessionErrors.add(actionRequest, exception.getClass(), exception);

			actionResponse.setRenderParameter(
				"mvcRenderCommandName", "/journal/translate");
		}
	}

	private Map<String, String[]> _getInfoFieldParameterValues(
		PortletRequest portletRequest) {

		Map<String, String[]> values = new HashMap<>();

		Map<String, String[]> parameterMap = portletRequest.getParameterMap();

		for (String parameter : parameterMap.keySet()) {
			if (parameter.startsWith(_INFO_FIELD_PREFIX)) {
				if (parameter.contains(_REPEATABLE_FIELD_SEPARATOR)) {
					String repeatableFieldUniqueId =
						StringUtil.split(parameter, _REPEATABLE_FIELD_SEPARATOR)
							[0];

					if (!values.containsKey(
							repeatableFieldUniqueId.substring(
								_INFO_FIELD_PREFIX.length()))) {

						int i = 0;
						List<String> repeatableValues = new ArrayList<>();

						while (parameterMap.containsKey(
									_getRepeatableFieldUniqueId(
										repeatableFieldUniqueId, i))) {

							repeatableValues.add(
								parameterMap.get(
									_getRepeatableFieldUniqueId(
										repeatableFieldUniqueId, i))[0]);
							i++;
						}

						values.put(
							repeatableFieldUniqueId.substring(
								_INFO_FIELD_PREFIX.length()),
							repeatableValues.toArray(new String[0]));
					}
				}
				else {
					values.put(
						parameter.substring(
							_INFO_FIELD_PREFIX.length(),
							parameter.length() - 2),
						portletRequest.getParameterValues(parameter));
				}
			}
		}

		return values;
	}

	private List<InfoField> _getInfoFields(JournalArticle article) {
		InfoItemFormProvider<JournalArticle> infoItemFormProvider =
			_infoItemServiceTracker.getFirstInfoItemService(
				InfoItemFormProvider.class, JournalArticle.class.getName());

		InfoForm infoForm = infoItemFormProvider.getInfoForm(article);

		return infoForm.getAllInfoFields();
	}

	private List<InfoFieldValue<Object>> _getInfoFieldValues(
		ActionRequest actionRequest, JournalArticle article) {

		List<InfoFieldValue<Object>> infoFieldValues = new ArrayList<>();

		Map<String, String[]> infoFieldParameterValues =
			_getInfoFieldParameterValues(actionRequest);

		InfoItemFieldValues infoItemFieldValues = _getInfoItemFieldValues(
			article);

		for (InfoField infoField : _getInfoFields(article)) {
			String[] infoFieldParameterValue = infoFieldParameterValues.get(
				infoField.getUniqueId());

			if (ArrayUtil.isNotEmpty(infoFieldParameterValue)) {
				Locale sourceLocale = _getSourceLocale(actionRequest);

				List<InfoFieldValue<Object>> sourceInfoFieldValues =
					new ArrayList<>(
						infoItemFieldValues.getInfoFieldValues(
							infoField.getUniqueId()));

				for (int i = 0; i < infoFieldParameterValue.length; i++) {
					InfoFieldValue<Object> sourceInfoFieldValue =
						sourceInfoFieldValues.get(i);

					infoFieldValues.add(
						new InfoFieldValue<>(
							infoField,
							InfoLocalizedValue.builder(
							).value(
								_getTargetLocale(actionRequest),
								infoFieldParameterValue[i]
							).value(
								sourceLocale,
								sourceInfoFieldValue.getValue(sourceLocale)
							).build()));
				}
			}
		}

		return infoFieldValues;
	}

	private InfoItemFieldValues _getInfoItemFieldValues(
		JournalArticle article) {

		InfoItemFieldValuesProvider<Object> infoItemFieldValuesProvider =
			_infoItemServiceTracker.getFirstInfoItemService(
				InfoItemFieldValuesProvider.class,
				JournalArticle.class.getName());

		return infoItemFieldValuesProvider.getInfoItemFieldValues(article);
	}

	private String _getRepeatableFieldUniqueId(
		String fieldUniqueId, int iterator) {

		StringBuilder sb = new StringBuilder();

		sb.append(fieldUniqueId);
		sb.append(_REPEATABLE_FIELD_SEPARATOR);
		sb.append(iterator);
		sb.append("--");

		return sb.toString();
	}

	private String _getSourceLanguageId(ActionRequest actionRequest) {
		return ParamUtil.getString(actionRequest, "sourceLanguageId");
	}

	private Locale _getSourceLocale(ActionRequest actionRequest) {
		return LocaleUtil.fromLanguageId(_getSourceLanguageId(actionRequest));
	}

	private String _getTargetLanguageId(ActionRequest actionRequest) {
		return ParamUtil.getString(actionRequest, "targetLanguageId");
	}

	private Locale _getTargetLocale(ActionRequest actionRequest) {
		return LocaleUtil.fromLanguageId(_getTargetLanguageId(actionRequest));
	}

	private static final String _INFO_FIELD_PREFIX = "infoField--";

	private static final String _REPEATABLE_FIELD_SEPARATOR = "_repeatable_";

	private static final Log _log = LogFactoryUtil.getLog(
		UpdateTranslationMVCActionCommand.class);

	@Reference
	private InfoItemServiceTracker _infoItemServiceTracker;

	@Reference
	private TranslationEntryService _translationEntryService;

}