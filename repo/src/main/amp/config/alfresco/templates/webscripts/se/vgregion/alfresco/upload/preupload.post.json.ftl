{ 
  "status" : "${result!}", 
  "tempFilename" : "${tempFilename!?js_string}", 
  "fileName": "${filename!?js_string}",
  "nodeRef":  "${nodeRef!}",
  "updateNodeRef": "${updateNodeRef!}",
  "majorVersion": ${majorVersion!"false"},
  "description": "${description!?js_string?replace("\\'","'")}",
  "mimetype": "${mimetype!?js_string}"
}
