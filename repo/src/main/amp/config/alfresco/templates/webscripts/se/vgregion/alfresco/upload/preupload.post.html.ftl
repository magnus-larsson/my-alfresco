<html>
<head>
   <title>Upload success</title>
</head>
<body>
<#if (args.successCallback?exists)>
   <script type="text/javascript">
      ${args.successCallback}.call(${args.successScope},
      { 
        "status" : "${result!}", 
        "tempFilename" : "${tempFilename!?js_string}", 
        "filename": "${filename!?js_string}",
        "nodeRef":  "${nodeRef!}",
        "updateNodeRef": "${nodeRef!}",
        "majorVersion": ${majorVersion!"false"},
        "description": "${description!?js_string}"
      });
   </script>
</#if>
</body>
</html>