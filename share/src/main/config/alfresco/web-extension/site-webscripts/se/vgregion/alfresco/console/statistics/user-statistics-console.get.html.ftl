<#compress>

<#assign el=args.htmlid?html>

<script type="text/javascript">
    //<![CDATA[
        new RL.UserStatisticsConsole("${el}").setMessages(${messages});
    //]]>
</script>

<div id="${el}-body" class="statistics-console">
    <div id="${el}-main" class="hidden">
        <div>
            <div class="header-bar">${msg("statistics-users.label")}</div>
            
            <div>
            	<div class="header-bar">${msg("statistics-users-internal.label")}</div>
                <div id="${el}-statistics-internal-users-list" class="-statistics-internal-users-list"></div>
                <div class="header-bar">${msg("statistics-users-external.label")}</div>
                <div id="${el}-statistics-external-users-list" class="-statistics-external-users-list"></div>
            </div>

            <div class="button-bar">
                <button type="button" name="${el}-refresh-button-users" id="${el}-refresh-button-users">${msg("button.refresh-users")}</button>
            </div>
        </div>
    </div>
</div>

</#compress>