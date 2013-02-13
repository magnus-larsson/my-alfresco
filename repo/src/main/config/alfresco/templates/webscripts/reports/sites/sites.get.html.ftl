<#compress>
<html>
   <head>
   </head>
   <body>
      <table style="width: 500px; border: 1px solid black; padding: 20px;">
         <tr>
            <th style="text-align: left;">site</th>
            <th style="text-align: left;">id</th>
            <th style="text-align: left;">size (MB)</th>
            <th style="text-align: left;">members</th>
            <th style="text-align: left;">last activity</th>
         </tr>
         <#list sites as site>
         <tr>
            <td>${site.title}</td>
            <td>${site.shortName}</td>
            <td>${((site.size/1024)/1024)?round}</td>
            <td>${site.members?round}</td>
            <td>${site.lastActivity}</td>
         </tr>
         </#list>
      </table>
   </body>
</html>
</#compress>