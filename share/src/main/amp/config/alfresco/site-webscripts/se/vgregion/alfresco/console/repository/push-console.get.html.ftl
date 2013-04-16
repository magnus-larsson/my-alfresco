<#assign el=args.htmlid?html>
<script type="text/javascript">//<![CDATA[
   new RL.PushConsole("${el}").setMessages(
      ${messages}
   );
//]]></script>

<div id="${el}-body" class="push-console">

	<div id="${el}-main" class="hidden">
      <div>	
	      <div class="header-bar">${msg("push-date-span.label")}</div>
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
         <div>
            <button type="button" name="${el}-push-button" id="${el}-push-button">${msg("button.push")}</button>
         </div>
      </div>
	</div>
</div>