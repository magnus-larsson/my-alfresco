<#assign el=args.htmlid?html>
<script type="text/javascript">//<![CDATA[
   new RL.PushReport("${el}").setMessages(
      ${messages}
   );
//]]></script>

<div id="${el}-body" class="push-report">

	<div id="${el}-main" class="hidden">
      <div>	
	     <div class="header-bar">${msg("push-report-span.label")}</div>
         <div class="datefield">
            <label for="${el}-from-date">${msg("from-date.label")}</label>
            <input id="${el}-from-date" type="text" name="from-date" class="date-entry" maxlength="10" value="${from_date}" />
            <input id="${el}-from-time" type="text" name="from-time" class="time-entry" maxlength="5" value="${from_time}"/>
         </div>
      
         <div class="datefield">
            <label for="${el}-to-date">${msg("to-date.label")}</label>
            <input id="${el}-to-date" type="text" name="to-date" class="date-entry" maxlength="10" value="${to_date}" />
            <input id="${el}-to-time" type="text" name="to-time" class="time-entry" maxlength="5" value="${to_time}" />
         </div>
         
         <div class="publishstatus">
            <label for="${el}-publishstatus">${msg("label.publishStatus")}</label>
            <select id="${el}-publishstatus">
            	<option value="any">${msg("status.any.label")}</option>
            	<option value="">${msg("status.blank.label")}</option>
            	<option value="ok">${msg("status.ok.label")}</option>
            	<option value="error">${msg("status.error.label")}</option>
            </select>  
         </div>
         <div class="unpublishstatus">
            <label for="${el}-unpublishstatus">${msg("label.unpublishStatus")}</label>
            <select id="${el}-unpublishstatus">
            	<option value="any">${msg("status.any.label")}</option>
            	<option value="">${msg("status.blank.label")}</option>
            	<option value="ok">${msg("status.ok.label")}</option>
            	<option value="error">${msg("status.error.label")}</option>
            </select>  
         </div>
         <div>
            <button type="button" name="${el}-report-button" id="${el}-report-button">${msg("button.report")}</button>
         </div>
         <div class="header-bar">${msg("push-report-result-span.label")}</div>
         <div>
         	<div id="${el}-result" class="${el}-result"></div>
         </div>
      </div>
      
	</div>
</div>