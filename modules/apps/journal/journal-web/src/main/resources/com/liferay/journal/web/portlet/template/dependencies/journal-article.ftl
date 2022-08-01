<#include "init.ftl">

<#assign
	variableData = name + ".getData()"
	variableFriendlyUrl = name + ".getFriendlyUrl()"
/>

<#if stringUtil.equals(language, "ftl")>
${r"<#if"} (${variableData})?? && (${variableFriendlyUrl})??>
	${r"<#assign"}
		webContentData = jsonFactoryUtil.createJSONObject(${variableData})
	${r"/>"}

	<#if webContentData?? && webContentData.title>
		<a href="${getVariableReferenceCode(variableFriendlyUrl)}">
			${r"${webContentData.title}"}
		</a>
	</#if>
${r"</#if>"}
<#else>
#if (($${variableData})?? && ($${variableFriendlyUrl})??)
	#set ($webContentData = $jsonFactoryUtil.createJSONObject($${variableData}))

	<#if webContentData?? && webContentData.title>
		<a href="${getVariableReferenceCode(variableFriendlyUrl)}">
			${r"${webContentData.title}"}
		</a>
	</#if>
#end
</#if>