<#compress>

<#assign el=args.htmlid?html>

<script type="text/javascript">
    //<![CDATA[
        new RL.PdfaConsole("${el}").setMessages(${messages});
    //]]>
</script>

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