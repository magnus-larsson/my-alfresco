function main() {
  
   if (!json.has("target") || !json.has("srcs")) {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Unsufficient arguments");
      model.result = "Unsufficient arguments";
      return;
   }
   
   var targets = [];
   var target = search.findNode(json.get("target"));
   var srcs   = json.get("srcs");
   
   if (!target) {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not find target node");
      model.result = "Could not find target node";
      return;
   }
   
   if (!srcs || srcs.length() == 0) {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not find source node");
      model.result = "Could not find source node"
      return;
   }
   
   //validate that parents are of the same list type   
   var srcType = search.findNode(srcs.get(0)).parent.properties['{http://www.alfresco.org/model/datalist/1.0}dataListItemType'];
   if (srcType != target.properties['{http://www.alfresco.org/model/datalist/1.0}dataListItemType']) {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Lists are not of the same itemType");
      model.result = "Lists are not of the same itemType";
      return;
   }
   
   var success = true;
   
   for (var i=0; i<srcs.length(); i++) {
      //move node
      var n = search.findNode(srcs.get(i));
      success = success && n.move(target);
      if (!success) {
         status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Could not move node: "+srcs.get(i));
         model.result = "Could not move node: " + srcs.get(i);
         return;
      }
   }
   
   model.result = jsonUtils.toJSONString({success:success, len: srcs.length() });
}

main();
