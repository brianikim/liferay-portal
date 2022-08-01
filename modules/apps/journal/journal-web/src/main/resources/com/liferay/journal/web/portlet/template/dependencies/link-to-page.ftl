<#include "init.ftl">

<#assign variableName = name + ".getFriendlyUrl()" />

<#if stringUtil.equals(language, "ftl")>
${r"<#if"} (${variableName})??>
	<a data-senna-off="true" href="${getVariableReferenceCode(variableName)}">
		${label}
	</a>
${r"</#if>"}
<#else>
#if (($${variableName})??)
	<a data-senna-off="true" href="${getVariableReferenceCode(variableName)}">
		${label}
	</a>
#end
</#if>