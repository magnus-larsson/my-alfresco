<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/documentlibrary/documentlist.get.head.ftl -->

<#include "../component.head.inc">
<#include "../form/form.get.head.ftl">
<!-- Document List -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/documentlibrary/documentlist.css" />
<@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/documentlist.js"></@script>

<!-- Publish To Storage Action -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/documentlibrary/publish-to-storage-action.css" />

<!--[if lte IE 7]> 
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/documentlibrary/publish-to-storage-action-ie.css" />
<![endif]-->

<@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/publish-to-storage.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/publish-to-storage-action.js"></@script>

<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/documentlibrary/copy-move-to.css" />