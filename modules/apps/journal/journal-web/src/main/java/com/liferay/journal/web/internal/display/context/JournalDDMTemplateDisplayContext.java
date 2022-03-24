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

package com.liferay.journal.web.internal.display.context;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.dynamic.data.mapping.service.DDMTemplateServiceUtil;
import com.liferay.dynamic.data.mapping.util.comparator.DDMTemplateNameComparator;
import com.liferay.dynamic.data.mapping.util.comparator.TemplateIdComparator;
import com.liferay.dynamic.data.mapping.util.comparator.TemplateModifiedDateComparator;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.web.internal.configuration.JournalWebConfiguration;
import com.liferay.journal.web.internal.servlet.taglib.util.JournalDDMTemplateActionDropdownItemsProvider;
import com.liferay.journal.web.internal.util.SiteConnectedGroupUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Eudaldo Alonso
 */
public class JournalDDMTemplateDisplayContext {

	public JournalDDMTemplateDisplayContext(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		_renderRequest = renderRequest;
		_renderResponse = renderResponse;

		_httpServletRequest = PortalUtil.getHttpServletRequest(renderRequest);

		_journalWebConfiguration =
			(JournalWebConfiguration)_httpServletRequest.getAttribute(
				JournalWebConfiguration.class.getName());

		_themeDisplay = (ThemeDisplay)_httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public long getClassPK() {
		if (_classPK != null) {
			return _classPK;
		}

		_classPK = ParamUtil.getLong(_httpServletRequest, "classPK");

		return _classPK;
	}

	public DDMStructure getDDMStructure() {
		if (_ddmStructure != null) {
			return _ddmStructure;
		}

		if (getClassPK() <= 0) {
			return _ddmStructure;
		}

		_ddmStructure = DDMStructureLocalServiceUtil.fetchStructure(
			getClassPK());

		return _ddmStructure;
	}

	public List<DropdownItem> getDDMTemplateActionDropdownItems(
			DDMTemplate ddmTemplate)
		throws Exception {

		JournalDDMTemplateActionDropdownItemsProvider
			ddmTemplateActionDropdownItems =
				new JournalDDMTemplateActionDropdownItemsProvider(
					ddmTemplate, _renderRequest, _renderResponse);

		return ddmTemplateActionDropdownItems.getActionDropdownItems();
	}

	public SearchContainer<DDMTemplate> getDDMTemplateSearch()
		throws Exception {

		if (_ddmTemplateSearch != null) {
			return _ddmTemplateSearch;
		}

		SearchContainer<DDMTemplate> ddmTemplateSearch = new SearchContainer(
			_renderRequest, _getPortletURL(), null, "there-are-no-templates");

		if (Validator.isNotNull(_getKeywords())) {
			ddmTemplateSearch.setEmptyResultsMessage("no-templates-were-found");
		}

		String orderByCol = getOrderByCol();
		String orderByType = getOrderByType();

		ddmTemplateSearch.setOrderByCol(orderByCol);
		ddmTemplateSearch.setOrderByComparator(_getOrderByComparator());
		ddmTemplateSearch.setOrderByType(orderByType);
		ddmTemplateSearch.setRowChecker(
			new EmptyOnClickRowChecker(_renderResponse));

		long[] groupIds = {_themeDisplay.getScopeGroupId()};

		if (_journalWebConfiguration.showAncestorScopesByDefault()) {
			groupIds =
				SiteConnectedGroupUtil.
					getCurrentAndAncestorSiteAndDepotGroupIds(
						_themeDisplay.getScopeGroupId(), true);
		}

		List<DDMTemplate> results = null;
		int total = 0;

		if (Validator.isNotNull(_getKeywords())) {
			results = DDMTemplateServiceUtil.search(
				_themeDisplay.getCompanyId(), groupIds,
				new long[] {PortalUtil.getClassNameId(DDMStructure.class)},
				_getDDMTemplateClassPKs(),
				PortalUtil.getClassNameId(JournalArticle.class), _getKeywords(),
				StringPool.BLANK, StringPool.BLANK,
				WorkflowConstants.STATUS_ANY, ddmTemplateSearch.getStart(),
				ddmTemplateSearch.getEnd(),
				ddmTemplateSearch.getOrderByComparator());

			total = DDMTemplateServiceUtil.searchCount(
				_themeDisplay.getCompanyId(), groupIds,
				new long[] {PortalUtil.getClassNameId(DDMStructure.class)},
				_getDDMTemplateClassPKs(),
				PortalUtil.getClassNameId(JournalArticle.class), _getKeywords(),
				StringPool.BLANK, StringPool.BLANK,
				WorkflowConstants.STATUS_ANY);
		}
		else {
			results = DDMTemplateServiceUtil.getTemplates(
				_themeDisplay.getCompanyId(), groupIds,
				new long[] {PortalUtil.getClassNameId(DDMStructure.class)},
				_getDDMTemplateClassPKs(),
				PortalUtil.getClassNameId(JournalArticle.class),
				ddmTemplateSearch.getStart(), ddmTemplateSearch.getEnd(),
				ddmTemplateSearch.getOrderByComparator());
			total = DDMTemplateServiceUtil.getTemplatesCount(
				_themeDisplay.getCompanyId(), groupIds,
				new long[] {PortalUtil.getClassNameId(DDMStructure.class)},
				_getDDMTemplateClassPKs(),
				PortalUtil.getClassNameId(JournalArticle.class));
		}

		List<DDMTemplate> sortedResults = new ArrayList<>(results);

		Collections.sort(
			sortedResults, ddmTemplateSearch.getOrderByComparator());

		ddmTemplateSearch.setResults(sortedResults);

		ddmTemplateSearch.setTotal(total);

		_ddmTemplateSearch = ddmTemplateSearch;

		return ddmTemplateSearch;
	}

	public String getDisplayStyle() {
		if (_displayStyle != null) {
			return _displayStyle;
		}

		PortalPreferences portalPreferences =
			PortletPreferencesFactoryUtil.getPortalPreferences(
				_httpServletRequest);

		_displayStyle = ParamUtil.getString(
			_httpServletRequest, "displayStyle");

		if (Validator.isNull(_displayStyle)) {
			_displayStyle = portalPreferences.getValue(
				JournalPortletKeys.JOURNAL + ".ddmTemplates", "display-style",
				_DISPLAY_VIEWS[0]);
		}

		if (!ArrayUtil.contains(_DISPLAY_VIEWS, _displayStyle)) {
			_displayStyle = _DISPLAY_VIEWS[0];
		}

		portalPreferences.setValue(
			JournalPortletKeys.JOURNAL + ".ddmTemplates", "display-style",
			_displayStyle);

		return _displayStyle;
	}

	public String getOrderByCol() {
		if (_orderByCol != null) {
			return _orderByCol;
		}

		_orderByCol = ParamUtil.getString(
			_renderRequest, "orderByCol", "modified-date");

		return _orderByCol;
	}

	public String getOrderByType() {
		if (_orderByType != null) {
			return _orderByType;
		}

		_orderByType = ParamUtil.getString(
			_renderRequest, "orderByType", "desc");

		return _orderByType;
	}

	public String[] getTemplateLanguageTypes() {
		return _journalWebConfiguration.journalDDMTemplateLanguageTypes();
	}

	public boolean isSearch() {
		if (Validator.isNotNull(_getKeywords())) {
			return true;
		}

		return false;
	}

	private long[] _getDDMTemplateClassPKs() {
		if (getClassPK() > 0) {
			return new long[] {getClassPK()};
		}

		return null;
	}

	private String _getKeywords() {
		if (_keywords != null) {
			return _keywords;
		}

		_keywords = ParamUtil.getString(_renderRequest, "keywords");

		return _keywords;
	}

	private OrderByComparator<DDMTemplate> _getOrderByComparator() {
		OrderByComparator<DDMTemplate> orderByComparator = null;

		boolean orderByAsc = false;

		String orderByType = getOrderByType();

		if (orderByType.equals("asc")) {
			orderByAsc = true;
		}

		String orderByCol = getOrderByCol();

		if (orderByCol.equals("id")) {
			orderByComparator = new TemplateIdComparator(orderByAsc);
		}
		else if (orderByCol.equals("modified-date")) {
			orderByComparator = new TemplateModifiedDateComparator(orderByAsc);
		}
		else if (orderByCol.equals("name")) {
			orderByComparator = new DDMTemplateNameComparator(
				orderByAsc, _themeDisplay.getLocale());
		}

		return orderByComparator;
	}

	private PortletURL _getPortletURL() {
		PortletURL portletURL = _renderResponse.createRenderURL();

		portletURL.setParameter("mvcPath", "/view_ddm_templates.jsp");

		String keywords = _getKeywords();

		if (Validator.isNotNull(keywords)) {
			portletURL.setParameter("keywords", keywords);
		}

		String orderByCol = getOrderByCol();

		if (Validator.isNotNull(orderByCol)) {
			portletURL.setParameter("orderByCol", orderByCol);
		}

		String orderByType = getOrderByType();

		if (Validator.isNotNull(orderByType)) {
			portletURL.setParameter("orderByType", orderByType);
		}

		return portletURL;
	}

	private static final String[] _DISPLAY_VIEWS = {"icon", "list"};

	private Long _classPK;
	private DDMStructure _ddmStructure;
	private SearchContainer<DDMTemplate> _ddmTemplateSearch;
	private String _displayStyle;
	private final HttpServletRequest _httpServletRequest;
	private final JournalWebConfiguration _journalWebConfiguration;
	private String _keywords;
	private String _orderByCol;
	private String _orderByType;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final ThemeDisplay _themeDisplay;

}