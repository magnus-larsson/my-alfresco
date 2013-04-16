<#include "/org/alfresco/components/component.head.inc">
<@markup id="vgr-yui" action="replace" target="yui" scope="global">
   <!-- YUI -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/css/yui-fonts-grids.css" />
   <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/yui/columnbrowser/assets/columnbrowser.css" />
   <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/yui/columnbrowser/assets/skins/default/columnbrowser-skin.css" />
   <#if theme = 'default'>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/yui/assets/skins/default/skin.css" />
   <#else>
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/themes/${theme}/yui/assets/skin.css" />
   </#if>
   <#if DEBUG>
   <script type="text/javascript" src="${url.context}/res/js/log4javascript.v1.4.1.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/yahoo/yahoo-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/event/event-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/dom/dom-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/dragdrop/dragdrop-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/animation/animation-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/logger/logger-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/connection/connection-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/element/element-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/get/get-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/yuiloader/yuiloader-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/button/button-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/container/container-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/menu/menu-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/json/json-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/selector/selector-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/datasource/datasource-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/autocomplete/autocomplete-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/paginator/paginator-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/datatable/datatable-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/history/history-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/treeview/treeview-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/autocomplete/autocomplete-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/cookie/cookie-debug.js"></script>
   <script type="text/javascript" src="${url.context}/res/yui/yui-patch.js"></script>
   <script type="text/javascript">//<![CDATA[
      YAHOO.util.Event.throwErrors = true;
   //]]></script>
   <#else>
   <script type="text/javascript" src="${url.context}/res/js/yui-common.js"></script>
   </#if>
   <@script type="text/javascript" src="${url.context}/res/js/bubbling.js"></@script>
   <script type="text/javascript">//<![CDATA[
      YAHOO.Bubbling.unsubscribe = function(layer, handler, scope)
      {
         this.bubble[layer].unsubscribe(handler, scope);
      };
   //]]></script>
</@>
