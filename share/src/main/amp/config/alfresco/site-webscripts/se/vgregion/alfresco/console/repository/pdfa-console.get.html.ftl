<@markup id="css" >
  <#-- CSS Dependencies -->
  <@link href="${url.context}/res/components/console/pdfa-console.css" group="console"/>  
  <@link href="${url.context}/res/modules/documentlibrary/global-folder.css" group="console"/>
</@>

<@markup id="js">
  <@script type="text/javascript" src="${url.context}/res/yui/datasource/datasource.js" group="console" />
  <@script type="text/javascript" src="${url.context}/res/components/console/consoletool.js" group="console" />
  <@script type="text/javascript" src="${url.context}/res/components/console/pdfa-console.js" group="console" />
  <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/global-folder.js" group="console" />
  <@script type="text/javascript" src="${url.context}/res/yui/datasource/datasource.js" group="console" />
  <@script type="text/javascript" src="${url.context}/res/yui/resize/resize.js" group="console" />
  
</@>

<@markup id="widgets">
  <@createWidgets group="console"/>
</@>


<#compress>

<#assign el=args.htmlid?html>

<div id="${el}-body" class="pdfa-console">
    <div id="${el}-main" class="hidden">
        <div>
            <div class="header-bar">${msg("possible-missing.label")}</div>

            <div>
                <div id="${el}-pdfa-missing-list" class="pdfa-missing-list"></div>
                <p><i><b>OBS!</b> Då detta är en asynkron handling kommer inte resultatet att synas i webbläsarfönstret.</i></p>
            </div>

            <div class="button-bar">
                <button type="button" name="${el}-pdfa-button" id="${el}-pdfa-button">${msg("button.pdfa")}</button>
                <button type="button" name="${el}-refresh-button" id="${el}-refresh-button">${msg("button.refresh")}</button>
            </div>
        </div>
    </div>
</div>

</#compress>