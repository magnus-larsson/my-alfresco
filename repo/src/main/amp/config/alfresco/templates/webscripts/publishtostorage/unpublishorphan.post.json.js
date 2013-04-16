<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/published.lib.js">

script: {

   if (!json.has("nodeRef")) {
      status.code = 404;
      status.redirect = true;
      break script;
   }
   
   var nodeRef = json.get('nodeRef') + "";
   
   var node = search.findNode(nodeRef);
   
   if (!node) {
      status.code = 500;
      status.redirect = true;
      break script;
   }
   
   var origin = node.properties['vgr:dc.source.origin'];
   var orphan = false;
   
   if (origin === 'Alfresco') {
      var orphan = Published.noSourceAndPublished(node);
   } else {
      var orphan = Published.unknownSourceAndPublished(node);
   }
   
   if (!orphan) {
      status.code = 500;
      status.redirect = true;
      status.message = 'Document that is to be unpublished is no true orphan!';
      break script;
   }
   
   node.properties['vgr:dc.date.availableto'] = new Date();
   node.save();
   
   model.result = true;
}