<#assign el=args.htmlid?html>
<div id="${el}-dialog" class="move-dialog">
    <div id="${el}-title" class="hd">${msg("header.title")}</div>
    <div class="bd">
        <p>${msg("description")}</p>
        
        <div id="${el}-wrapper" class="wrapper">
            <div class="bd list-wrapper">
              <ul id="${el}-move-dialog-datalists" class="move-dialog-datalists">
               <li>${msg("please.wait")}</li>
              </ul>
            </div>
            <div class="bdft">
                <input type="button" id="${el}-select" value="${msg("button.select")}" />
                <input type="button" id="${el}-cancel" value="${msg("button.cancel")}" />
            </div>
        </div>
        
    </div>
    <script type="text/javascript">//<![CDATA[
        Alfresco.util.addMessages(${messages}, "Alfresco.component.DataGrid");
    //]]></script>
</div>
