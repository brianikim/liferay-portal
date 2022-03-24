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
import com.liferay.dynamic.data.mapping.service.DDMStructureServiceUtil;
import com.liferay.dynamic.data.mapping.util.comparator.StructureIdComparator;
import com.liferay.dynamic.data.mapping.util.comparator.StructureModifiedDateComparator;
import com.liferay.dynamic.data.mapping.util.comparator.StructureNameComparator;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.web.internal.configuration.JournalWebConfiguration;
import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.theme.ThemeDisplay;
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
public class JournalDDMStructuresDisplayContext {

	public JournalDDMStructuresDisplayContext(
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

	public SearchContainer<DDMStructure> getDDMStructureSearch()
		throws Exception {

		if (_ddmStructureSearch != null) {
			return _ddmStructureSearch;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)_httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		SearchContainer<DDMStructure> ddmStructureSearch = new SearchContainer(
			_renderRequest, _getPortletURL(), null, "there-are-no-structures");

		if (Validator.isNotNull(_getKeywords())) {
			ddmStructureSearch.setEmptyResultsMessage(
				"no-structures-were-found");
		}

		String orderByCol = getOrderByCol();
		String orderByType = getOrderByType();

		ddmStructureSearch.setOrderByCol(orderByCol);
		ddmStructureSearch.setOrderByComparator(_getOrderByComparator());
		ddmStructureSearch.setOrderByType(orderByType);
		ddmStructureSearch.setRowChecker(
			new EmptyOnClickRowChecker(_renderResponse));

		long[] groupIds = {_themeDisplay.getScopeGroupId()};

		if (_journalWebConfiguration.showAncestorScopesByDefault()) {
			groupIds = PortalUtil.getCurrentAndAncestorSiteGroupIds(
				_themeDisplay.getScopeGroupId());
		}

		List<DDMStructure> results = null;
		int total = 0;

		if (Validator.isNotNull(_getKeywords())) {
			results = DDMStructureServiceUtil.search(
				themeDisplay.getCompanyId(), groupIds,
				PortalUtil.getClassNameId(JournalArticle.class.getName()),
				_getKeywords(), WorkflowConstants.STATUS_ANY,
				ddmStructureSearch.getStart(), ddmStructureSearch.getEnd(),
				ddmStructureSearch.getOrderByComparator());
			total = DDMStructureServiceUtil.searchCount(
				themeDisplay.getCompanyId(), groupIds,
				PortalUtil.getClassNameId(JournalArticle.class.getName()),
				_getKeywords(), WorkflowConstants.STATUS_ANY);
		}
		else {
			results = DDMStructureServiceUtil.getStructures(
				themeDisplay.getCompanyId(), groupIds,
				PortalUtil.getClassNameId(JournalArticle.class.getName()),
				ddmStructureSearch.getStart(), ddmStructureSearch.getEnd(),
				ddmStructureSearch.getOrderByComparator());
			total = DDMStructureServiceUtil.getStructuresCount(
				themeDisplay.getCompanyId(), groupIds,
				PortalUtil.getClassNameId(JournalArticle.class.getName()));
		}

		List<DDMStructure> sortedResults = new ArrayList<>(results);

		Collections.sort(
			sortedResults, ddmStructureSearch.getOrderByComparator());

		ddmStructureSearch.setResults(sortedResults);

		ddmStructureSearch.setTotal(total);

		_ddmStructureSearch = ddmStructureSearch;

		return ddmStructureSearch;
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

	public boolean isSearch() {
		if (Validator.isNotNull(_getKeywords())) {
			return true;
		}

		return false;
	}

	private String _getKeywords() {
		if (_keywords != null) {
			return _keywords;
		}

		_keywords = ParamUtil.getString(_renderRequest, "keywords");

		return _keywords;
	}

	private OrderByComparator<DDMStructure> _getOrderByComparator() {
		OrderByComparator<DDMStructure> orderByComparator = null;

		boolean orderByAsc = false;

		String orderByType = getOrderByType();

		if (orderByType.equals("asc")) {
			orderByAsc = true;
		}

		String orderByCol = getOrderByCol();

		if (orderByCol.equals("id")) {
			orderByComparator = new StructureIdComparator(orderByAsc);
		}
		else if (orderByCol.equals("modified-date")) {
			orderByComparator = new StructureModifiedDateComparator(orderByAsc);
		}
		else if (orderByCol.equals("name")) {
			orderByComparator = new StructureNameComparator(
				orderByAsc, _themeDisplay.getLocale());
		}

		return orderByComparator;
	}

	private PortletURL _getPortletURL() {
		PortletURL portletURL = _renderResponse.createRenderURL();

		portletURL.setParameter("mvcPath", "/view_ddm_structures.jsp");

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

	private SearchContainer<DDMStructure> _ddmStructureSearch;
	private final HttpServletRequest _httpServletRequest;
	private final JournalWebConfiguration _journalWebConfiguration;
	private String _keywords;
	private String _orderByCol;
	private String _orderByType;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final ThemeDisplay _themeDisplay;

}