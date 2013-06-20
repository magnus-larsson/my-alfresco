<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/published.lib.js">

function main() {
  var nodeRef = args.nodeRef;
  
  var node = search.findNode(nodeRef);
  
  if (!node) {
    status.code = 404;
    status.message = "No existing node supplied for get publish status";
    status.redirect = true;
    return;
  }
  
  var p = Published.isPublished(node);

  model.published = p.published;
  model.publishedold =  p.publishedold;
  model.future = p.future;
  model.hasbeen = p.hasbeen;
}

main();