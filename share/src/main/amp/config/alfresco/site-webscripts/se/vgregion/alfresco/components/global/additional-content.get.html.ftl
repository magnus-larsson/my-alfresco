<@markup id="css" >
  <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/global/notifications.css" group="additionalContent" />
</@>

<@markup id="js">
  <@script type="text/javascript" src="${page.url.context}/res/yui/cookie/cookie.js" group="additionalContent" />
  <@script type="text/javascript" src="${page.url.context}/res/components/global/notifications.js" group="additionalContent" />
</@>

<@markup id="widgets">
  <@createWidgets group="additionalContent"/>
</@>