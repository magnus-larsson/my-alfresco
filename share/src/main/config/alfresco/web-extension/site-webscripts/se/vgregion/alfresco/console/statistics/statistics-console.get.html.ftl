<#compress>

<#assign el=args.htmlid?html>

<script type="text/javascript">
    //<![CDATA[
        new RL.StatisticsConsole("${el}").setMessages(${messages});
    //]]>
</script>

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
            <!--
            <div class="header-bar">${msg("statistics-users.label")}</div>
            
            <div>
                <div id="${el}-statistics-users-list" class="-statistics-users-list"></div>
            </div>

            <div class="button-bar">
                <button type="button" name="${el}-refresh-button-users" id="${el}-refresh-button-users">${msg("button.refresh-users")}</button>
            </div>
            -->
        </div>
    </div>
</div>

</#compress>