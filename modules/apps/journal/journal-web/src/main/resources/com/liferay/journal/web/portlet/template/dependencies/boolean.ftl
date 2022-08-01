<#include "init.ftl">

<#if stringUtil.equals(language, "ftl")>
${r"<#if"} (${variableName})?? && getterUtil.getBoolean(${variableName})>
	${r"${"}languageUtil.get(locale, "yes")}
${r"<#else>"}
	${r"${"}languageUtil.get(locale, "no")}
${r"</#if>"}
<#else>
#if (($${variableName})?? && $getterUtil.getBoolean($${variableName}))
	$languageUtil.get($$locale, "yes")
#else
	$languageUtil.get($$locale, "no")
#end
</#if>