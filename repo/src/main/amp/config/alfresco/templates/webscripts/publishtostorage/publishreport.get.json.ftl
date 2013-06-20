<#compress>
{
"recordsReturned": ${recordsReturned?c},
"totalRecords": ${totalRecords?c},
"startIndex": ${startIndex?c},
"pageSize": ${pageSize?c},
"documents": [
    <#list documents as document>
    {
    "title": <#if document.title??>"${document.title?js_string}"<#else>""</#if>,
    "id": <#if document.id??>"${document.id?js_string}"<#else>""</#if>,
    "version": <#if document.version??>"${document.version?js_string}"<#else>""</#if>,
    "sourceId": <#if document.sourceId??>"${document.sourceId?js_string}"<#else>""</#if>,
	"pushedForPublish": <#if document.pushedForPublish??>"${document.pushedForPublish?datetime?string("yyyy-MM-dd HH:mm:ss")}"<#else>""</#if>,
	"pushedForUnpublish": <#if document.pushedForUnpublish??>"${document.pushedForUnpublish?datetime?string("yyyy-MM-dd HH:mm:ss")}"<#else>""</#if>,
	"publishStatus": <#if document.publishStatus??>"${document.publishStatus?js_string}"<#else>""</#if>,
	"unpublishStatus": <#if document.unpublishStatus??>"${document.unpublishStatus?js_string}"<#else>""</#if>
    }<#if document_has_next>,</#if>
    </#list>
]
}
</#compress>