<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<liferay-portlet:resourceURL copyCurrentRenderParameters="<%= false %>" var="baseResourceURL" />

<%
DigitalSignatureConfiguration digitalSignatureConfiguration = DigitalSignatureConfigurationUtil.getDigitalSignatureConfiguration(themeDisplay.getCompanyId(), themeDisplay.getScopeGroupId());
%>

<div class="digital-signature">
	<react:component
		module="{DigitalSignature} from digital-signature-web"
		props='<%=
			HashMapBuilder.<String, Object>put(
				"allowedFileExtensions", StringUtil.merge(DigitalSignatureConstants.ALLOWED_FILE_EXTENSIONS)
			).put(
				"baseResourceURL", String.valueOf(baseResourceURL)
			).put(
				"requireSignInBeforeSigning", digitalSignatureConfiguration.requireSignInBeforeSigning()
			).build()
		%>'
	/>
</div>