<html>
<head>
   <title>Upload success</title>
</head>
<body>
<#if (args.success?exists)>
   <script type="text/javascript">
      ${args.success}(
      { 
        "status" : "${result!}", 
        "tempFilename" : "${tempFilename!?js_string}", 
        "fileName": "${filename!?js_string}",
        "nodeRef":  "${nodeRef!}",
        "updateNodeRef": "${updateNodeRef!}",
        "majorVersion": ${majorVersion!"false"},
        "description": "${description!?js_string}",
  		"mimetype": "${mimetype!?js_string}"
      });
   </script>
</#if>
</body>
</html>