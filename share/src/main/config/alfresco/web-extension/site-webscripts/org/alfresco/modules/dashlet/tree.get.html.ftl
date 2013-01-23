<#assign el=args.htmlid?html>
<div id="${el}-dialog" class="imagesummary-dialog">
    <div id="${el}-title" class="hd">${msg("header.title")}</div>
    <div class="bd">
        <p>${msg("description")}</p>
        <p>
            <input name="${el}-recursive" id="${args.htmlid}-recursive" type="checkbox" value="recursive">
            <label for="${el}-recursive">${msg("label.recursive")}</label>
        </p>
        <div id="${el}-wrapper" class="wrapper">
            <div class="bd">
               <p id="${el}-selected"></p> 
               <div class="treeview filter">
                  <div id="${el}-treeview" class="tree"></div>
               </div>
            </div>
            <div class="bdft">
                <input type="button" id="${el}-select" value="${msg("button.select")}" />
                <input type="button" id="${el}-cancel" value="${msg("button.cancel")}" />
            </div>
        </div>
    </div>
</div>



