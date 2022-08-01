<#include "init.ftl">

<#assign localeVariable = "locale" />

<#if stringUtil.equals(language, "vm")>
	<#assign localeVariable = "$" + localeVariable />
</#if>

<#assign labelName = "languageUtil.format(" + localeVariable + ", \"download-x\", \"" + label + "\", false)" />

<#if stringUtil.equals(language, "ftl")>
${r"<#if"} (${variableName})?? && (${labelName})??>
	<a href="${getVariableReferenceCode(variableName)}">
		${getVariableReferenceCode(labelName)}
	</a>
${r"</#if>"}
<#else>
#if (($${variableName})?? && ($${labelName})??)
	<a href="${getVariableReferenceCode(variableName)}">
		${getVariableReferenceCode(labelName)}
	</a>
#end
</#if>