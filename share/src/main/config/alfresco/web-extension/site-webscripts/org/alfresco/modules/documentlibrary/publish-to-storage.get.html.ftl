<#assign el=args.htmlId?html>
<script type="text/javascript">//<![CDATA[
   Alfresco.util.addMessages(${messages}, "Alfresco.thirdparty.PublishToStorage");
//]]></script>
<div id="${el}-dialog" class="publish-to-storage">
    <div id="${el}-title" class="hd">${msg("pts.title")}</div>
    <div class="bd">
        <div id="${el}-wrapper" class="wrapper">
            <div class="publish-to-storage-body bd">
                <h3>${msg("pts")}</h3>
                <p id="${el}-publish-to-storage-description">${msg("pts.description")}</p>
                <p id="${el}-publish-to-storage-error" class="errors hidden"><strong>${msg("pts.errors")}</strong></p>
                <div class="tree-width-wrapper">
                    <table id="${el}-publish-to-storage-tree-headers" class="headers">
                        <thead>
                            <tr>
                                <td>${msg("pts.document")}</td>
                                <td class="pub_header">${msg("pts.availablefrom")}</td>
                            </tr>
                        </thead>
                    </table>
                    <div id="${el}-publish-to-storage-tree" 
                         class="publish-to-storage-tree"></div>
                </div>
            </div>
            <div class="bdft">
                <input type="button" id="${el}-ok" value="${msg("button.ok")}" />
                <input type="button" id="${el}-cancel" value="${msg("button.cancel")}" />
            </div>
        </div>
    </div>
</div>

