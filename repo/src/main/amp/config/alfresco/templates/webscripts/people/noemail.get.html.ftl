<html>
  <head>
    <title>List of all users in Alfresco without a proper email adress</title>
  </head>
  <body>
    <table style="width: 950px;">
      <tr>
        <th style="text-align: left;">Username</th>
        <th style="text-align: left;">Firstname</th>
        <th style="text-align: left;">Lastname</th>
        <th style="text-align: left;">Organisation</th>
      </tr>
      <#list peoplelist as person>
      <tr>
        <td>${person.properties.userName}</td>
        <td><#if person.properties.firstName??>${person.properties.firstName}<#else>&nbsp;</#if></td>
        <td><#if person.properties.lastName??>${person.properties.lastName}<#else>&nbsp;</#if></td>
        <td><#if person.properties.organization??>${person.properties.organization}<#else>&nbsp;</#if></td>
      </tr>
      </#list>   
    </table>
  </body>
</html>