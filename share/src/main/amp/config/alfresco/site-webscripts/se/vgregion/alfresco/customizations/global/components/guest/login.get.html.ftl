<@markup id="vgr-css" action="after" target="css">
   <@link href="${url.context}/res/components/guest/vgr-login.css" group="login"/>
</@>

<@markup id="vgr-footer" action="before" target="footer">
  <div class="vgr-footer">
    <span class="menu-item">
      <a href="http://www.vgregion.se/alfresco" target="_blank">${msg("login.label.trouble-logging-in.link")}</a>
    </span>
  </div>
  <div class="vgr-footer">
    <span class="red">
      ${msg("login.label.trouble-logging-in.description")}
    </span>
  </div>
  <br />
</@>
