<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/document_library/init.jsp" %>

<%
DLSignatureDetailDisplayContext dlSignatureDetailDisplayContext = (DLSignatureDetailDisplayContext)request.getAttribute("info_panel_signature.jsp-dlSignatureDetailDisplayContext");

DSRequestDetail dsRequestDetail = dlSignatureDetailDisplayContext.getDSRequestDetail();
%>

<div class="sidebar-section">
	<clay:label
		displayType="<%= dlSignatureDetailDisplayContext.getStatusDisplayType(dsRequestDetail.getRequestStatus()) %>"
		label="<%= LanguageUtil.get(request, dsRequestDetail.getRequestStatus()) %>"
	/>
</div>

<dl class="sidebar-dl sidebar-section">
	<dt class="sidebar-dt">
		<liferay-ui:message key="requester" />
	</dt>
	<dd class="sidebar-dd">
		<%= HtmlUtil.escape(dsRequestDetail.getRequesterName()) %>

		<c:if test="<%= Validator.isNotNull(dsRequestDetail.getRequesterEmailAddress()) %>">
			<div class="text-3 text-secondary">
				<%= HtmlUtil.escape(dsRequestDetail.getRequesterEmailAddress()) %>
			</div>
		</c:if>
	</dd>

	<c:if test="<%= Validator.isNotNull(dsRequestDetail.getProviderRequestId()) %>">
		<dt class="sidebar-dt">
			<liferay-ui:message key="envelope-id" />
		</dt>
		<dd class="sidebar-dd">
			<span class="text-break text-monospace">
				<%= HtmlUtil.escape(dsRequestDetail.getProviderRequestId()) %>
			</span>
		</dd>
	</c:if>

	<c:if test="<%= Validator.isNotNull(dsRequestDetail.getEmailSubject()) %>">
		<dt class="sidebar-dt">
			<liferay-ui:message key="email-subject" />
		</dt>
		<dd class="sidebar-dd">
			<%= HtmlUtil.escape(dsRequestDetail.getEmailSubject()) %>
		</dd>
	</c:if>
</dl>

<div class="sidebar-section">
	<h4 class="component-subtitle text-uppercase">
		<liferay-ui:message key="recipients" />
	</h4>

	<table class="show-quick-actions-on-hover table table-autofit table-list">
		<thead>
			<tr>
				<th><liferay-ui:message key="name" /></th>
				<th><liferay-ui:message key="status" /></th>
				<th><liferay-ui:message key="date" /></th>
			</tr>
		</thead>

		<tbody>

			<%
			for (DSRequestRecipientDetail dsRequestRecipientDetail : dsRequestDetail.getRecipientDetails()) {
			%>

				<tr>
					<td>
						<div class="font-weight-semi-bold">
							<%= HtmlUtil.escape(dsRequestRecipientDetail.getName()) %>
						</div>

						<c:if test="<%= Validator.isNotNull(dsRequestRecipientDetail.getEmailAddress()) %>">
							<div class="text-3 text-secondary">
								<%= HtmlUtil.escape(dsRequestRecipientDetail.getEmailAddress()) %>
							</div>
						</c:if>
					</td>
					<td>
						<clay:label
							displayType="<%= dlSignatureDetailDisplayContext.getStatusDisplayType(dsRequestRecipientDetail.getRequestRecipientStatus()) %>"
							label="<%= LanguageUtil.get(request, dsRequestRecipientDetail.getRequestRecipientStatus()) %>"
						/>
					</td>
					<td class="text-secondary">
						<%= dlSignatureDetailDisplayContext.getStatusLabel(dsRequestRecipientDetail.getStatusDate()) %>
					</td>
				</tr>

			<%
			}
			%>

		</tbody>
	</table>
</div>

<div class="sidebar-section">
	<h4 class="component-subtitle text-uppercase">
		<liferay-ui:message key="activity" />
	</h4>

	<ul class="list-unstyled">

		<%
		for (DLSignatureDetailDisplayContext.SignatureActivity signatureActivity : dlSignatureDetailDisplayContext.getSignatureActivities()) {
		%>

			<li class="autofit-row mb-3 <%= signatureActivity.isPending() ? "text-muted" : StringPool.BLANK %>">
				<div class="autofit-col mr-3 text-<%= signatureActivity.getDisplayType() %>">
					<clay:icon
						symbol="simple-circle"
					/>
				</div>

				<div class="autofit-col autofit-col-expand">
					<div class="font-weight-semi-bold">
						<%= HtmlUtil.escape(signatureActivity.getTitle()) %>
					</div>

					<c:if test="<%= Validator.isNotNull(signatureActivity.getMeta()) %>">
						<div class="text-3 text-secondary">
							<%= HtmlUtil.escape(signatureActivity.getMeta()) %>
						</div>
					</c:if>
				</div>
			</li>

		<%
		}
		%>

	</ul>
</div>