<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/evaluator.lib.js">
<import resource="classpath:alfresco/templates/webscripts/publishtostorage/publish.lib.js">

function revoke(node) {
    if (node.published || node.publishedold) {
        if (!node.isFolder) { //only publish documents
           storage.unpublishFromStorage(node.ref);
           node.published = false;
        }        
    }
    if (node.children) {
        for (var i=0; i<node.children.length; i++) {
            revoke(node.children[i]);
        }
    }
}
var res = Publish.processJSONRevokable(json);
//check if this is a publish action and not just a lookup, and that we have no validation errors
if (json.has("action") && json.get("action") == "revoke" && res.statistics.errors == 0) {
    try {
        for (var i=0; i<res.nodes.length; i++) {
            revoke(res.nodes[i]);
        }  
    } catch(e) {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, e.toString());
   }    
}

model.result = jsonUtils.toJSONString(res.nodes);
model.statistics = jsonUtils.toJSONString(res.statistics);

