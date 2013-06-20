<#compress>
<html>
   <head>
   </head>
   <body>
      <table style="width: 100%; border: 1px solid black; padding: 20px;">
         <tr>
            <th style="text-align: left;">Title</th>
            <th style="text-align: left;">Id</th>
            <th style="text-align: left;">Version</th>
            <th style="text-align: left;">Source id</th> 
            <th style="text-align: left;">Pushed for publish</th>
            <th style="text-align: left;">Pushed for unpublish</th>
            <th style="text-align: left;">status</th>
         </tr>
         <#list documents as document>
         <tr>
            <td>${document.title}</td>
            <td>${document.id}</td>
            <td>${document.version}</td>
            <td>${document.sourceId}</td>
            <td><#if document.pushedForPublish??>${document.pushedForPublish?datetime?string("yyyy-MM-dd HH:mm:ss")}<#else></#if></td>
            <td><#if document.pushedForUnpublish??>${document.pushedForUnpublish?datetime?string("yyyy-MM-dd HH:mm:ss")}<#else></#if></td>
            <td><#if document.status??>${document.status}<#else></#if></td>
         </tr>
         </#list>
      </table>
   </body>
</html>
</#compress>