<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
String redirect = ParamUtil.getString(request, "redirect", assetCategoriesDisplayContext.getEditCategoryRedirect());

String redirectOriginal = ParamUtil.getString(request, "redirectOriginal", redirect);

long categoryId = ParamUtil.getLong(request, "categoryId");

AssetCategory category = AssetCategoryLocalServiceUtil.fetchCategory(categoryId);

long groupId = ParamUtil.getLong(request, "groupId", scopeGroupId);

long parentCategoryId = BeanParamUtil.getLong(category, request, "parentCategoryId");

long vocabularyId = ParamUtil.getLong(request, "vocabularyId");

portletDisplay.setShowBackIcon(true);
portletDisplay.setURLBack(redirect);

String title = LanguageUtil.get(request, "add-new-category");

if (category != null) {
	title = category.getTitle(locale);
}
else if (parentCategoryId > 0) {
	title = LanguageUtil.get(request, "add-new-subcategory");
}

renderResponse.setTitle(title);

PortletURL addCategoryURL = assetCategoriesDisplayContext.getAddCategoryRedirectPortletURL();

addCategoryURL.setParameter("redirectOriginal", redirectOriginal);
%>

<portlet:actionURL name="editCategory" var="editCategoryURL">
	<portlet:param name="mvcPath" value="/edit_category.jsp" />
	<portlet:param name="groupId" value="<%= String.valueOf(groupId) %>" />
	<portlet:param name="vocabularyId" value="<%= String.valueOf(vocabularyId) %>" />
</portlet:actionURL>

<liferay-frontend:edit-form
	action="<%= editCategoryURL %>"
	name="fm"
>
	<aui:input name="redirect" type="hidden" value="<%= redirect %>" />
	<aui:input name="redirectOriginal" type="hidden" value="<%= redirectOriginal %>" />
	<aui:input name="categoryId" type="hidden" value="<%= categoryId %>" />

	<liferay-frontend:edit-form-body>
		<liferay-ui:error exception="<%= AssetCategoryNameException.class %>" message="please-enter-a-valid-name" />
		<liferay-ui:error exception="<%= DuplicateCategoryException.class %>" message="please-enter-a-unique-name" />

		<aui:model-context bean="<%= category %>" model="<%= AssetCategory.class %>" />

		<liferay-frontend:fieldset-group>
			<liferay-frontend:fieldset
				collapsed="<%= false %>"
				collapsible="<%= true %>"
				label="details"
			>
				<aui:input label="name" localized="<%= true %>" name="title" placeholder="name" type="text" value="<%= (category == null) ? StringPool.BLANK : assetCategoriesDisplayContext.getCategoryLocalizationXML(category) %>">
					<aui:validator name="maxLength"><%= ModelHintsUtil.getMaxLength(AssetCategory.class.getName(), "name") %></aui:validator>
					<aui:validator name="required" />
				</aui:input>

				<aui:input name="description" placeholder="description" />

				<c:choose>
					<c:when test="<%= assetCategoriesDisplayContext.isFlattenedNavigationAllowed() %>">

						<%
						AssetCategory parentCategory = AssetCategoryLocalServiceUtil.fetchCategory(parentCategoryId);
						%>

						<aui:field-wrapper label="parent-category">
							<div>
								<div id="<portlet:namespace />parentCategoryContainer">
									<div class="field-content">
										<div class="form-group" id="namespace_assetCategoriesSelector_<%= vocabularyId %>">
											<div class="input-group">
												<div class="input-group-item">
													<div class="form-control form-control-tag-group input-group">
														<div class="input-group-item">
															<c:if test="<%= parentCategory != null %>">
																<clay:label
																	closeable="<%= true %>"
																	label="<%= parentCategory.getTitle(locale) %>"
																/>

																<input name="parentCategoryId" type="hidden" value="<%= parentCategoryId %>" />
															</c:if>

															<input class="form-control-inset" type="text" value="" />
														</div>
													</div>
												</div>

												<div class="input-group-item input-group-item-shrink">
													<button class="btn btn-secondary" type="button">
														<liferay-ui:message key="select" />
													</button>
												</div>
											</div>
										</div>
									</div>
								</div>

								<%
								List<Map<String, Object>> selectedCategories = new ArrayList<>();

								if (parentCategory != null) {
									selectedCategories.add(
										HashMapBuilder.<String, Object>put(
											"label", parentCategory.getTitle(locale)
										).put(
											"value", parentCategory.getCategoryId()
										).build());
								}
								%>

								<react:component
									module="js/AssetCategoriesSelectorTag.es"
									props='<%=
										HashMapBuilder.<String, Object>put(
											"categoryIds", Collections.singletonList(parentCategoryId)
										).put(
											"groupIds", Collections.singletonList(scopeGroupId)
										).put(
											"namespace", liferayPortletResponse.getNamespace()
										).put(
											"portletURL", assetCategoriesDisplayContext.getCategorySelectorURL()
										).put(
											"selectedCategories", selectedCategories
										).put(
											"vocabularyIds", Collections.singletonList(vocabularyId)
										).build()
									%>'
								/>
							</div>
						</aui:field-wrapper>
					</c:when>
					<c:otherwise>
						<aui:input name="parentCategoryId" type="hidden" value="<%= parentCategoryId %>" />
					</c:otherwise>
				</c:choose>
			</liferay-frontend:fieldset>

			<c:if test="<%= (category == null) && !assetCategoriesDisplayContext.isItemSelector() %>">
				<liferay-frontend:fieldset
					collapsed="<%= true %>"
					collapsible="<%= true %>"
					label="permissions"
				>
					<liferay-ui:input-permissions
						modelName="<%= AssetCategory.class.getName() %>"
					/>
				</liferay-frontend:fieldset>
			</c:if>
		</liferay-frontend:fieldset-group>
	</liferay-frontend:edit-form-body>

	<c:choose>
		<c:when test="<%= !assetCategoriesDisplayContext.isItemSelector() %>">
			<liferay-frontend:edit-form-footer>
				<aui:button type="submit" />

				<aui:button cssClass="btn-secondary" onClick='<%= liferayPortletResponse.getNamespace() + "saveAndAddNew();" %>' value="save-and-add-a-new-one" />

				<aui:button cssClass="btn-secondary" href="<%= redirect %>" type="cancel" />
			</liferay-frontend:edit-form-footer>
		</c:when>
		<c:otherwise>
			<aui:script>
				var formSheet = document.querySelector('.lfr-form-content .sheet');

				formSheet.classList.add('border-0');

				var dialog = Liferay.Util.getWindow(
					'<%= assetCategoriesDisplayContext.getItemSelectorEventName() %>'
				);
				var footer = dialog.getToolbar('footer');

				dialog.headerNode
					.one('.modal-title')
					.text(
						dialog.get('initialTitle') + ' - <liferay-ui:message key="add-new" />'
					);

				var controlButtons = footer
					.get('boundingBox')
					.all('.add-category-toolbar-button');

				var cancelButton, saveAndAddNewButton, submitButton;

				if (controlButtons.size() > 0) {
					cancelButton = footer
						.get('boundingBox')
						.one('#<portlet:namespace />cancelButton')._node;
					saveAndAddNewButton = footer
						.get('boundingBox')
						.one('#<portlet:namespace />saveAndAddNewButton')._node;
					submitButton = footer
						.get('boundingBox')
						.one('#<portlet:namespace />submitButton')._node;

					<portlet:namespace />controlButtonsAddEventListeners();

					controlButtons.show();
				}
				else {
					cancelButton = document.createElement('button');
					cancelButton.setAttribute(
						'class',
						'add-category-toolbar-button btn btn-link ml-3'
					);
					cancelButton.setAttribute('type', 'button');
					cancelButton.setAttribute('id', '<portlet:namespace />cancelButton');
					cancelButton.innerText = '<liferay-ui:message key="cancel" />';

					footer.get('boundingBox').append(cancelButton);

					saveAndAddNewButton = document.createElement('button');
					saveAndAddNewButton.setAttribute(
						'class',
						'add-category-toolbar-button btn btn-secondary ml-3'
					);
					saveAndAddNewButton.setAttribute('type', 'submit');
					saveAndAddNewButton.setAttribute(
						'id',
						'<portlet:namespace />saveAndAddNewButton'
					);
					saveAndAddNewButton.innerText =
						'<liferay-ui:message key="save-and-add-a-new-one" />';

					footer.get('boundingBox').append(saveAndAddNewButton);

					submitButton = document.createElement('button');
					submitButton.setAttribute(
						'class',
						'add-category-toolbar-button btn btn-primary ml-3'
					);
					submitButton.setAttribute('type', 'submit');
					submitButton.setAttribute('id', '<portlet:namespace />submitButton');
					submitButton.innerText = '<liferay-ui:message key="save" />';

					<portlet:namespace />controlButtonsAddEventListeners();

					footer.get('boundingBox').append(submitButton);
				}
			</aui:script>
		</c:otherwise>
	</c:choose>
</liferay-frontend:edit-form>

<aui:script>
	function <portlet:namespace />controlButtonsAddEventListeners() {
		cancelButton.addEventListener('click', <portlet:namespace />cancel);

		saveAndAddNewButton.addEventListener(
			'click',
			<portlet:namespace />saveAndAddNew
		);

		submitButton.addEventListener('click', <portlet:namespace />save);
	}

	function <portlet:namespace />controlButtonsRemoveEventListeners() {
		cancelButton.removeEventListener('click', <portlet:namespace />cancel);
		saveAndAddNewButton.removeEventListener(
			'click',
			<portlet:namespace />saveAndAddNew
		);
		submitButton.removeEventListener('click', <portlet:namespace />save);
	}

	function <portlet:namespace />cancel() {
		footer.get('boundingBox').all('.add-category-toolbar-button').hide();
		<portlet:namespace />controlButtonsRemoveEventListeners();
		Liferay.Util.navigate('<%= HtmlUtil.escapeJS(redirectOriginal) %>');
	}

	function <portlet:namespace />saveAndAddNew() {
		document.querySelector('#<portlet:namespace />redirect').value =
			'<%= addCategoryURL.toString() %>';

		submitForm(document.querySelector('#<portlet:namespace />fm'));
	}

	function <portlet:namespace />save() {
		document.querySelector('#<portlet:namespace />redirect').value =
			'<%= redirectOriginal.toString() %>';
		submitForm(document.querySelector('#<portlet:namespace />fm'));
	}
</aui:script>