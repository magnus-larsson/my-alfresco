<div class="form-field">
   <#if form.mode == "view">
      <div class="viewmode-field">
         <span class="viewmode-label">${field.label?html}:</span>
         <span class="viewmode-value"><#if field.value?is_number>${field.value?c}<#else>${field.value?html}</#if></span>
      </div>
   <#else>
      <label for="${fieldHtmlId}">${field.label?html}:</label>
      <input id="${fieldHtmlId}" type="text" value="<#if field.value?is_number>${field.value?c}<#else>${field.value?html}</#if>" disabled="disabled"
             title="${msg("form.field.not.editable")?html}"
             <#if field.control.params.styleClass?exists>class="${field.control.params.styleClass}"</#if> />
      <@formLib.renderFieldHelp field=field />
   </#if>
</div>