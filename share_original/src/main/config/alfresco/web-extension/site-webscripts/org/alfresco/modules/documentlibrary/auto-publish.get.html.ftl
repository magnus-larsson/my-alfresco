<#assign el=args.htmlId?html>
<script type="text/javascript">
    //<![CDATA[
    Alfresco.util.addMessages(${messages}, "Alfresco.thirdparty.AutoPublish");
    //]]>
</script>
<div id="${el}-dialog" class="auto-publish">
    <div id="${el}-title" class="hd">${msg("auto-publish.title")}</div>
    <div class="bd">
        <div id="${el}-wrapper" class="wrapper">
            <div class="auto-publish-body bd">
                <h3>${msg("auto-publish")}</h3>

                <p id="${el}-auto-publish-description">${msg("auto-publish.description")}</p>

                <p id="${el}-auto-publish-error" class="errors hidden"><strong>${msg("auto-publish.errors")}</strong></p>

                <div class="form-width-wrapper">
                    <div id="${el}-auto-publish-form-headers" class="headers">
                        <div id="${el}-auto-publish-form" class="auto-publish-form"></div>
                    </div>
                </div>
            </div>
            <div class="bdft">
                <input type="button" id="${el}-ok" value="${msg("button.ok")}"/>
                <input type="button" id="${el}-cancel" value="${msg("button.cancel")}"/>
            </div>
        </div>
    </div>
</div>
