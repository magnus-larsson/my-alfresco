<@markup id="css" >
  <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/console/site-statistics-console.css"  group="console" />
  <@link href="${url.context}/res/modules/documentlibrary/global-folder.css" group="console"/>
</@>

<@markup id="js">
	<@script type="text/javascript" src="${url.context}/res/yui/datasource/datasource.js" group="console" />
	<@script type="text/javascript" src="${url.context}/res/components/console/consoletool.js" group="console" />
	<@script type="text/javascript" src="${url.context}/res/components/console/site-statistics-console.js"  group="console" />
	<@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/global-folder.js" group="console" />
	<@script type="text/javascript" src="${url.context}/res/yui/resize/resize.js" group="console" />
</@>

<@markup id="widgets">
  <@createWidgets group="console"/>
</@>

<#compress>

<#assign el=args.htmlid?html>

<div id="${el}-body" class="statistics-console">
    <div id="${el}-main" class="hidden">
        <div>
            <div class="header-bar">${msg("statistics-sites.label")}</div>

            <div>
                <div id="${el}-statistics-sites-list" class="-statistics-sites-list"></div>
            </div>

            <div class="button-bar">
                <button type="button" name="${el}-refresh-button-sites" id="${el}-refresh-button-sites">${msg("button.refresh-sites")}</button>
            </div>
        </div>
    </div>
</div>

</#compress>