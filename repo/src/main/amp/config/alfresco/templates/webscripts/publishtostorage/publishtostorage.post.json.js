<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/evaluator.lib.js">
<import resource="classpath:alfresco/templates/webscripts/publishtostorage/publish.lib.js">
//evaluator, 343rader

var async = json.has("async") ? json.get("async") : true;

function publish(node) {
    if (!node.published) {
        if (!node.isFolder) { //only publish documents
           storage.publishToStorage(node.ref, async);
           node.published = true;
        }        
        if (node.children) {
            for (var i=0; i<node.children.length; i++) {
                publish(node.children[i]);
            }
        }
    }
}

var res = Publish.processJSONPublishable(json);
//check if this is a publish action and not just a lookup, and that we have no validation errors
if (json.has("action") && json.get("action") == "publish" && res.statistics.errors == 0) {
    try {
        for (var i=0; i<res.nodes.length; i++) {
            publish(res.nodes[i]);
        }  
    } catch(e) {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, e.toString());
   }    
}

model.result = jsonUtils.toJSONString(res.nodes);
model.statistics = jsonUtils.toJSONString(res.statistics);

