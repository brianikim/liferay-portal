<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/document_library/init.jsp" %>

<%
FileEntry fileEntry = (FileEntry)request.getAttribute(WebKeys.DOCUMENT_LIBRARY_FILE_ENTRY);

DSRequestManager dsRequestManager = (DSRequestManager)request.getAttribute(DSRequestManager.class.getName());

DLSignatureDetailDisplayContext dlSignatureDetailDisplayContext = new DLSignatureDetailDisplayContext(themeDisplay.getCompanyId(), dsRequestManager, fileEntry.getFileEntryId(), request, locale, themeDisplay.getTimeZone());

DSRequestDetail dsRequestDetail = dlSignatureDetailDisplayContext.getDSRequestDetail();
%>

<c:choose>
	<c:when test="<%= dsRequestDetail == null %>">
		<liferay-frontend:empty-result-message
			description='<%= LanguageUtil.get(request, "this-document-has-no-signature-requests") %>'
			title='<%= LanguageUtil.get(request, "no-signature-requests") %>'
		/>
	</c:when>
	<c:otherwise>
		<div class="ds-signature-detail">
			<div class="align-items-center d-flex mb-4">
				<span class="text-truncate">
					<%= HtmlUtil.escape(fileEntry.getTitle()) %>
				</span>

				<clay:label
					cssClass="c-ml-3 flex-shrink-0"
					displayType="<%= dlSignatureDetailDisplayContext.getStatusDisplayType(dsRequestDetail.getRequestStatus()) %>"
					label="<%= LanguageUtil.get(request, dsRequestDetail.getRequestStatus()) %>"
				/>
			</div>

			<div class="align-items-center bg-light border d-flex ds-signature-card mb-4 p-3 rounded">
				<div class="flex-fill">
					<div class="text-3 text-secondary">
						<liferay-ui:message key="requester" />
					</div>

					<div class="align-items-center c-mt-1 d-flex">
						<span class="sticker sticker-circle sticker-outline-primary sticker-sm">
							<span class="sticker-overlay">
								<%= HtmlUtil.escape(dlSignatureDetailDisplayContext.getInitials(dsRequestDetail.getRequesterName())) %>
							</span>
						</span>

						<div class="c-ml-2 text-truncate">
							<div class="font-weight-semi-bold text-truncate">
								<%= HtmlUtil.escape(dsRequestDetail.getRequesterName()) %>
							</div>

							<c:if test="<%= Validator.isNotNull(dsRequestDetail.getRequesterEmailAddress()) %>">
								<div class="text-3 text-secondary text-truncate">
									<%= HtmlUtil.escape(dsRequestDetail.getRequesterEmailAddress()) %>
								</div>
							</c:if>
						</div>
					</div>
				</div>

				<c:if test="<%= Validator.isNotNull(dsRequestDetail.getProviderRequestId()) %>">
					<div class="flex-shrink-0" style="align-self: stretch; background-color: #e7e7ed; margin: 0 1rem; width: 1px;"></div>

					<div class="flex-fill">
						<div class="text-3 text-secondary">
							<liferay-ui:message key="envelope-id" />
						</div>

						<div class="c-mt-1 text-3 text-break text-monospace">
							<%= HtmlUtil.escape(dsRequestDetail.getProviderRequestId()) %>
						</div>
					</div>
				</c:if>
			</div>

			<div class="mb-4">
				<p style="color: #6b6c7e; font-size: 0.75rem; font-weight: 600; letter-spacing: 0.05em; margin-bottom: 0.75rem; text-transform: uppercase;">
					<liferay-ui:message key="recipients" />
				</p>

				<table class="table table-autofit table-list">
					<tbody>

						<%
						for (DSRequestRecipientDetail dsRequestRecipientDetail : dsRequestDetail.getRecipientDetails()) {
						%>

							<tr>
								<td class="table-cell-expand">
									<div class="align-items-center d-flex">
										<span class="sticker sticker-circle sticker-outline-info sticker-sm">
											<span class="sticker-overlay">
												<%= HtmlUtil.escape(dlSignatureDetailDisplayContext.getInitials(dsRequestRecipientDetail.getName())) %>
											</span>
										</span>

										<div class="c-ml-2 text-truncate">
											<div class="font-weight-semi-bold text-truncate">
												<%= HtmlUtil.escape(dsRequestRecipientDetail.getName()) %>
											</div>

											<c:if test="<%= Validator.isNotNull(dsRequestRecipientDetail.getEmailAddress()) %>">
												<div class="text-3 text-secondary text-truncate">
													<%= HtmlUtil.escape(dsRequestRecipientDetail.getEmailAddress()) %>
												</div>
											</c:if>
										</div>
									</div>
								</td>
								<td class="table-cell-ws-nowrap">
									<clay:label
										displayType="<%= dlSignatureDetailDisplayContext.getStatusDisplayType(dsRequestRecipientDetail.getRequestRecipientStatus()) %>"
										label="<%= LanguageUtil.get(request, dsRequestRecipientDetail.getRequestRecipientStatus()) %>"
									/>
								</td>
								<td class="table-cell-ws-nowrap text-3 text-secondary">
									<%= dlSignatureDetailDisplayContext.getStatusLabel(dsRequestRecipientDetail.getStatusDate()) %>
								</td>
							</tr>

						<%
						}
						%>

					</tbody>
				</table>
			</div>

			<div>
				<p style="color: #6b6c7e; font-size: 0.75rem; font-weight: 600; letter-spacing: 0.05em; margin-bottom: 0.75rem; text-transform: uppercase;">
					<liferay-ui:message key="activity" />
				</p>

				<ul style="list-style: none; margin: 0; padding: 0;">

					<%
					List<DLSignatureDetailDisplayContext.SignatureActivity> signatureActivities = dlSignatureDetailDisplayContext.getSignatureActivities();

					for (int i = 0; i < signatureActivities.size(); i++) {
						DLSignatureDetailDisplayContext.SignatureActivity signatureActivity = signatureActivities.get(i);

						boolean lastActivity = (i == (signatureActivities.size() - 1));
					%>

						<li style="padding: 0 0 <%= lastActivity ? "0" : "1.25rem" %> 2rem; position: relative;<%= signatureActivity.isPending() ? " opacity: 0.5;" : StringPool.BLANK %>">
							<c:if test="<%= !lastActivity %>">
								<span style="background-color: #e7e7ed; bottom: 0; left: 7px; position: absolute; top: 0.625rem; width: 2px;"></span>
							</c:if>

							<span style="background-color: <%= dlSignatureDetailDisplayContext.getActivityColor(signatureActivity.getDisplayType()) %>; border: 2px solid #fff; border-radius: 50%; height: 1rem; left: 0; position: absolute; top: 0.125rem; width: 1rem; z-index: 1;"></span>

							<div style="font-weight: 600;">
								<%= HtmlUtil.escape(signatureActivity.getTitle()) %>
							</div>

							<c:if test="<%= Validator.isNotNull(signatureActivity.getMeta()) %>">
								<div style="color: #6b6c7e; font-size: 0.75rem;">
									<%= HtmlUtil.escape(signatureActivity.getMeta()) %>
								</div>
							</c:if>
						</li>

					<%
					}
					%>

				</ul>
			</div>
		</div>
	</c:otherwise>
</c:choose>