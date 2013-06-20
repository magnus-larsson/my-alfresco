<#compress>
<#list dictionaryNodes as d>
${d?replace('store://?', '', 'r')}
</#list>
<#list workspaceSystemNodes as ws>
${ws?replace('store://?', '', 'r')}
</#list>
<#list siteNodes as s>
${s?replace('store://?', '', 'r')}
</#list>
<#list avmNodes as a>
${a?replace('store://?', '', 'r')}
</#list>
<#list userNodes as u>
${u?replace('store://?', '', 'r')}
</#list>
<#list systemNodes as sys>
${sys?replace('store://?', '', 'r')}
</#list>
</#compress>