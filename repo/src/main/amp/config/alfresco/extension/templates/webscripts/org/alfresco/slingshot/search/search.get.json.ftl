<#-- @overridden projects/remote-api/config/alfresco/templates/webscripts/org/alfresco/slingshot/search/search.get.html.ftl -->

<#compress>

<#escape x as jsonUtils.encodeJSONString(x)>
{
  "totalRecords": ${data.paging.totalRecords?c},
  "totalRecordsUpper": ${data.paging.totalRecordsUpper?c},
  "startIndex": ${data.paging.startIndex?c},
	"items":
	[
		<#list data.items as item>
		{
			"nodeRef": "${item.nodeRef}",
			"type": "${item.type}",
			"name": "${item.name!''}",
			"displayName": "${item.displayName!''}",
			<#if item.title??>
			"title": "${item.title}",
			</#if>
			"description": "${item.description!''}",
			"modifiedOn": "${xmldate(item.modifiedOn)}",
			"modifiedByUser": "${item.modifiedByUser}",
			"modifiedBy": "${item.modifiedBy}",
			"size": ${item.size?c},
         "permissions" : {
            "published": <#if item.pub??>${item.pub.published?js_string}<#else>""</#if>,
            "unpublished": <#if item.pub??>${(!item.pub.published)?string}<#else>""</#if>,
            "published-before": <#if item.pub??>${item.pub.hasbeen?string}<#else>""</#if>,
            "will-be-published": <#if item.pub??>${item.pub.future?string}<#else>""</#if>,
            "older-version-published": <#if item.pub??>${item.pub.publishedold?string}<#else>""</#if>,
            "no-source-and-published": <#if item.no_source_and_published??>${item.no_source_and_published?string}<#else>""</#if>,
            "unknown-source-and-published": <#if item.unknown_source_and_published??>${item.unknown_source_and_published?string}<#else>""</#if>
         },
			<#if item.site??>
			"site":
			{
				"shortName": "${item.site.shortName}",
				"title": "${item.site.title}"
			},
			"container": "${item.container}",
			</#if>
			<#if item.path??>
			"path": "${item.path}",
			</#if>
			<#if item.dc_source_origin??>
			"dc_source_origin": "${item.dc_source_origin}",
			</#if>
			"tags": [<#list item.tags as tag>"${tag}"<#if tag_has_next>,</#if></#list>]
		}<#if item_has_next>,</#if>
		</#list>
	]
}
</#escape>

</#compress>