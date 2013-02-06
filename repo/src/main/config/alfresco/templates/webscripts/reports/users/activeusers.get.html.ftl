<#compress>
<html>
   <head>
   </head>
   <body>
      <table style="width: 500px; border: 1px solid black; padding: 20px;">
         <tr>
            <th style="text-align: left;">Username</th>
            <th style="text-align: left;">Full name</th>
            <th style="text-align: left;"># of logins last 30 days</th>
            <th style="text-align: left;">last activity</th>
         </tr>
         <tr>
         	<th colspan="4">Internal Users</th>
         </tr>
         <#list internal.users as user>
         <tr>
            <td>${user.userName}</td>
            <td>${user.fullName}</td>
            <td>${user.logins}</td>
            <td>${user.lastActivity?datetime}</td>
         </tr>
         </#list>
         <tr>
         	<th colspan="4">External Users</th>
         </tr>
         <#list external.users as user>
         <tr>
            <td>${user.userName}</td>
            <td>${user.fullName}</td>
            <td>${user.logins}</td>
            <td>${user.lastActivity?datetime}</td>
         </tr>
         </#list>
      </table>
   </body>
</html>
</#compress>