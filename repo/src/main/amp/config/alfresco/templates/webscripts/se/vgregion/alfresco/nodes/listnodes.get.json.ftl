<#compress>
   [
      <#list nodes as node>
      {
         "nodeRef": "${node.nodeRef}",
         "extension": "${node.properties['vgr:dc.format.extension']}",
         "filename": "${node.properties['vgr:dc.title.filename']}"
      }<#if node_has_next>,</#if>
      </#list>
   ]
</#compress>