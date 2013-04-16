<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/documentlibrary/toolbar.get.head.ftl -->

<#include "../component.head.inc">
<!-- Document Library Toolbar -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/documentlibrary/toolbar.css" />
<@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/toolbar.js"></@script>

<!-- Export To Mets Action -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/documentlibrary/export-to-mets-action.css" />
<@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/export-to-mets-action.js"></@script>

<!-- Publish To Storage Action -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/documentlibrary/publish-to-storage-action.css" />
<!--[if lte IE 7]>
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/documentlibrary/publish-to-storage-action-ie.css" />
<![endif]-->

<@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/publish-to-storage.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/components/documentlibrary/publish-to-storage-action.js"></@script>

