

<#-- configuration 

ds             -- webscript url
renderer       -- javascript function to render data
success        -- javascript function for handling selection (presentation and storing value in hidden input)
loader         -- javascript function handling loading of data to be presented in view mode
siteFiltering  -- turn off or on siteFiltering, default true
autoLoad       -- do a search with "" as filter on opening the dialog, default false

This control is preconfigured for authority (i.e. people) search.

Should be able to drop in replace places where the authority control is used.

-->

<#if field.control.params.ds?exists>
  <#assign ds=field.control.params.ds>
<#else>
  <#-- Defaults to site filtered people search -->
  <#assign ds='/proxy/alfresco/vgr/people?filter={filter}&site={site}&siteFilter={siteFilter}'>
</#if>


<div class="form-field">
    <#-- hidden inputs does the actual form submit with values back to alfresco, we need one in viewmode to get the actual values -->
    <input id="${fieldHtmlId}" name="-" type="hidden" value="<#if field.value?is_number>${field.value?c}<#else>${field.value?html}</#if>" />
    
    <div class="viewmode-field">
        <#if form.mode == "view">
             <#if field.mandatory && field.value?string == "">
                <span class="incomplete-warning">
                        <img src="${url.context}/components/form/images/warning-16.png" 
                             title="${msg("form.field.incomplete")?html}" />
                </span>
             </#if>
        </#if> 

        <span class="viewmode-label">${field.label?html}:</span>
        <ul id="${fieldHtmlId}-search-control-value-list" class="viewmode-value undecorated search-control-value-list">
            <#if field.value != "">
                <#list field.value?split(",") as fieldValue>
                    <#assign idx=fieldValue?index_of("|")>
                    <#if idx == -1>
                        <li data-ref="${fieldValue}">${fieldValue}</li>
                    <#else>
                        <li id="${fieldHtmlId}//${fieldValue?substring(0,idx)}" data-ref="${fieldValue?substring(0,idx)}">
                            ${fieldValue?substring(idx+1)}
                        </li> 
                    </#if>
                </#list>
            </#if>
        </ul>
                          

        <#if form.mode != "view">            
                 <#-- the rest of the inputs, these are the ones actually posted -->
                 <input id="${fieldHtmlId}-cntrl-added" name="${field.name}_added" type="hidden" value="" />
                 <input id="${fieldHtmlId}-cntrl-removed" name="${field.name}_removed" type="hidden" value="" />


                <button id="${fieldHtmlId}-search-edit">
                    <#if field.control.params.editButtonTitle?exists>
                        ${msg(field.control.params.editButtonTitle)}
                    <#else>
                        <#if field.endpointMany>${msg("form.search.add")}<#else>${msg("form.search.edit")}</#if>
                    </#if>
                </button>
                <@formLib.renderFieldHelp field=field />
            <#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if>
        </#if>
   </div>
</div>


<#if form.mode != "view">
    <#-- The actual dialog -->

    <div id="${fieldHtmlId}-search-control-panel" class="search-control-panel">
        <div class="hd">
            <span><#if field.control.params.panelTitle?exists>${msg(field.control.params.panelTitle)}<#else>${field.label}</#if></span>
        </div>
        <div class="bd">
            <div id="${fieldHtmlId}-search-control-helptext" class="search-control-helptext yui-g first theme-color-2 theme-bg-color-2">
                    <#if field.control.params.helpText?exists>
                        ${msg(field.control.params.helpText)}
                    <#else>
                        ${msg('form.search.help-text')}
                    </#if>
            </div>
            <div id="${fieldHtmlId}-search-control-form" class="search-control-form yui-g">
                <div class="search-field">
                    <input id="${fieldHtmlId}-search-control-form-input" type="text" />
                </div>
                <div class="search-button">
                    <button id="${fieldHtmlId}-search-control-form-button">
                     <#if field.control.params.searchButtonTitle?exists>
                            ${msg(field.control.params.searchButtonTitle)}
                        <#else>
                            ${msg("form.search.search")}
                        </#if>
                    </button>
                </div>
                <div id="${fieldHtmlId}-search-control-radio-site-div" class="search-site-or-all">
                    <input id="${fieldHtmlId}-search-control-radio-site" type="radio" name="search-site" value="site" checked="checked" />
                    <label for="${fieldHtmlId}-search-control-radio-site">${msg('form.search.site')}</label>
                    <input id="${fieldHtmlId}-search-control-radio-all" type="radio" name="search-site" value="all" />
                    <label for="${fieldHtmlId}-search-control-radio-all">${msg('form.search.all')}</label>
                </div>
            </div>
            <div id="${fieldHtmlId}-search-control-result-box" class="search-control-result-box yui-gd">
                <ul id="${fieldHtmlId}-search-control-list" class="search-control-list">
                    
                </ul>
            </div>
        </div>

        <div class="bdft">

        </div>

   </div>
    
</#if>

<#-- javascript to init it all, you can find it in webapp/components/form/search.js -->
<script type="text/javascript">
//<![CDATA[
(function(){
        var params = { 
                        id: '${fieldHtmlId}',
                        viewmode: <#if form.mode == "view">true<#else>false</#if>,
                        signalChange: <#if field.mandatory>true<#else>false</#if>,
                        editButton: '${fieldHtmlId}-search-edit',
                        panel: '${fieldHtmlId}-search-control-panel',
                        separator: ',',
                        url: '${url.context}${ds}',
                        multiple: ${field.endpointMany?string},
                        autoLoad: <#if field.control.params.autoLoad?exists>${field.control.params.autoLoad?string}<#else>false</#if>, //defaults to false
                        siteFiltering: <#if field.control.params.siteFiltering?exists>${field.control.params.siteFiltering?string}<#else>true</#if>, //defaults to true
                        success: <#if field.control.params.success?exists>${field.control.params.success}<#else>Alfresco.thirdparty.SearchControl.success.authority</#if>,
                        renderer: <#if field.control.params.renderer?exists>${field.control.params.renderer}<#else>Alfresco.thirdparty.SearchControl.renderers.people</#if>,
                        loader: <#if field.control.params.loader?exists>${field.control.params.loader}<#else>Alfresco.thirdparty.SearchControl.loaders.authority</#if>
                                
        };
        var sc = new Alfresco.thirdparty.SearchControl(params);
        sc.setMessages(${messages});
        <#if field.disabled>sc.setDisabled(true);</#if>
})();
//]]>
</script>



