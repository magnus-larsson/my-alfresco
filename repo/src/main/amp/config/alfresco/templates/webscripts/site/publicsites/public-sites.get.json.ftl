<#import "site.lib.ftl" as siteLib/>

<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/dashlets/my-sites.get.json.ftl -->

[
	<#list sites?sort_by("title") as site>
		<@siteLib.siteJSON site=site/>
		<#if site_has_next>,</#if>
	</#list>
]