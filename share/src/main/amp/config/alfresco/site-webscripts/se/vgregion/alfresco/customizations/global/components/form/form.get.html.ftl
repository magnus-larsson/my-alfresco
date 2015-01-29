<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/form/form.get.html.ftl -->

<@markup id="vgr-css" target="css" action="after">
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/form/controls/treeselect.css" group="form" />
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/form/controls/ajaxselectone.css" group="form" />
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/form/controls/search.css" group="form" />
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/form/vgr-form.css" group="form" />
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/accordion/accordion.css" group="form"/>
</@>
   
<@markup id="vgr-js" target="js" action="after">
  <@script type="text/javascript" src="${url.context}/res/components/form/controls/selecthsacode.js" group="form"/>

  <@script type="text/javascript" src="${url.context}/res/components/form/controls/treeselect.js" group="form"/>
  <@script type="text/javascript" src="${url.context}/res/components/form/controls/treeselectnode.js" group="form"/>
  <@script type="text/javascript" src="${url.context}/res/components/form/controls/treeselecttooltip.js" group="form"/>
  
  <@script type="text/javascript" src="${url.context}/res/components/form/controls/ajaxselectone.js" group="form"/>
  
  <@script type="text/javascript" src="${url.context}/res/components/form/controls/selectstatus.js" group="form"/>
  
  <@script type="text/javascript" src="${url.context}/res/components/form/controls/search.js" group="form"/>
  
  <@script type="text/javascript" src="${url.context}/res/components/form/vgr-form.js" group="form"/>
  
  <@script type="text/javascript" src="${url.context}/res/js/accordion/accordion.js" group="form"/>
  
  <@script type="text/javascript" src="${url.context}/res/components/object-finder/vgr-object-finder.js" group="form"/>
</@>

