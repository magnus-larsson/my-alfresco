<#assign controlId = fieldHtmlId + "-treeselect-cntrl">

<#if field.control.params.ds?exists>
    <#assign ds=field.control.params.ds>
<#else>
    <#assign ds=''>
</#if>

<#if field.control.params.get?exists>
    <#assign get=field.control.params.get>
<#else>
    <#assign get=''>
</#if>

<#if field.control.params.multiple?exists>
    <#assign multiple=field.control.params.multiple>
<#else>
    <#assign multiple=field.endpointMany>
</#if>

<#if field.control.params.selectableLevels?exists>
    <#assign selectableLevels=field.control.params.selectableLevels>
<#else>
    <#assign selectableLevels=0>
</#if>

<#if field.control.params.optionSeparator??>
    <#assign optionSeparator=field.control.params.optionSeparator>
<#else>
    <#assign optionSeparator=",">
</#if>

<#if field.control.params.dflt??>
    <#assign dflt=field.control.params.dflt>
<#else>
    <#assign dflt="">
</#if>

<#if field.control.params.onlySelectLeaf??>
    <#assign onlySelectLeaf=field.control.params.onlySelectLeaf>
<#else>
    <#assign onlySelectLeaf=false>
</#if>

<#if field.control.params.fetchKeywords??>
    <#assign fetchKeywords=field.control.params.fetchKeywords>
<#else>
    <#assign fetchKeywords=false>
</#if>

<#if form.mode == "view">
    <#assign componentHtmlId=fieldHtmlId + "-list">
<#else>
    <#assign componentHtmlId=fieldHtmlId + "-treeselect">
</#if>


<div class="form-field">

    <script type="text/javascript">//<![CDATA[

    <#if form.mode == "view" || !context.properties.nodeRef??>
    var version = 0.1;
    var nodeRef = "";
    <#else>
    var properties = ${context.properties['forms.cache./api/metadata?nodeRef=${context.properties.nodeRef}&shortQNames=true']}.properties;
    var version = parseFloat(properties['cm:versionLabel']);
    var nodeRef = '${context.properties.nodeRef}';
    </#if>

    new Alfresco.thirdparty.TreeSelect("${componentHtmlId}", "${fieldHtmlId}", "${url.context}${ds}").setOptions({
        optionSeparator: "${optionSeparator}",
        multipleSelectMode: ${multiple?string},
        selectableLevels: ${selectableLevels?string},
        disabled: <#if field.disabled>true<#else>false</#if>,
        mandatory: <#if field.mandatory>true<#else>false</#if>,
        tooltipDataLoaderUrl: "${url.context}${get}",
        viewmode: <#if form.mode == "view">true<#else>false</#if>,
        values: "${field.value?html}",
        dflt: "${dflt}",
        version: version,
        onlySelectLeaf: ${onlySelectLeaf?string},
        nodeRef: nodeRef
    });

    //]]></script>


<#if form.mode == "view">

    <div class="viewmode-field">
        <#if field.mandatory && field.value?string == "">
        <span class="incomplete-warning"><img src="${url.context}/components/form/images/warning-16.png" title="${msg("form.field.incomplete")?html}" /><span>
        </#if>
        <span class="viewmode-label">${field.label?html}:</span>
        <ul id="${fieldHtmlId}-list" class="viewmode-value undecorated">
            <#list field.value?split("${optionSeparator}") as fieldValue>
                <#assign idx=fieldValue?index_of("|")>
                <#if idx == -1>
                    <li>${fieldValue}</li>
                <#else>
                    <li id="${fieldHtmlId}//${fieldValue?substring(0,idx)}">
                    ${fieldValue?substring(idx+1)}
                    </li>
                </#if>
            </#list>
        </ul>
    </div>

<#else>

    <label class="viewmode-label">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if><@formLib.renderFieldHelp field=field /></label>
    <input id="${fieldHtmlId}" type="hidden" name="${field.name}" value="${field.value?string}" />
    <div id="${fieldHtmlId}-treeselect" class="treeselect">
        <table>
            <tbody>
            <tr>
                <td id="${fieldHtmlId}-treeselect-left" class="treeselect-left">
                    <div id="${fieldHtmlId}-treeselect-treeview" class="treeview">
                    </div>
                </td>
                <td id="${fieldHtmlId}-treeselect-middle" class="treeselect-middle">
                    <button id="${fieldHtmlId}-treeselect-add" class="treeselect-add"><img src="${url.context}/themes/default/images/right.png" /></button>
                    <button id="${fieldHtmlId}-treeselect-remove"><img src="${url.context}/themes/default/images/left.png" /></button>
                    <button id="${fieldHtmlId}-treeselect-clear"><img src="${url.context}/themes/default/images/edit-clear.png" /></button>
                </td>
                <td id="${fieldHtmlId}-treeselect-right" class="treeselect-right">
                    <ul id="${fieldHtmlId}-treeselect-selected" tabindex="0" <#if field.description?exists>title="${field.description?html}"</#if> <#if field.control.params.styleClass?exists>class="${field.control.params.styleClass}"</#if>>

                    </ul>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

    <#if fetchKeywords?string != "false">
        <div class="fetch-keywords-button">
            <button id="${fieldHtmlId}-treeselect-fetch-keywords-button" class="fetch-keywords-button">${msg("form.control.fetch-keywords")}</button>
        </div>
    </#if>
</#if>
</div>
