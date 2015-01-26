<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/document-details/document-metadata.get.html.ftl -->

<@markup id="vgr-css" target="css" action="after">
	<#-- CSS Dependencies -->
	<@link href="${url.context}/res/components/form/controls/treeselect.css" group="document-details" />
  <@link href="${url.context}/res/components/form/controls/search.css" group="document-details" />
	<@link href="${url.context}/res/css/accordion/accordion.css" group="document-details" />
	<@link href="${url.context}/res/components/form/vgr-form.css" group="document-details" />
</@>

<@markup id="vgr-js" target="js" action="after">
  <#-- JavaScript Dependencies -->
	<@script src="${url.context}/res/components/form/controls/treeselect.js" group="document-details" />
	<@script src="${url.context}/res/components/form/controls/treeselectnode.js" />
	<@script src="${url.context}/res/components/form/controls/treeselecttooltip.js" />
	<@script src="${url.context}/res/components/form/controls/search.js" />
	<@script src="${url.context}/res/js/accordion/accordion.js" />
</@>

