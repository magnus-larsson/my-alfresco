function main() {
   var nodeRef = null;
   var comment = null;

   if (json.has('nodeRef')) {
      nodeRef = json.get('nodeRef');
   }
   
   if (json.has('comment')) {
      comment = json.get('comment');
   }

   // Ensure mandatory parameters are passed
   if (nodeRef === null) {
      status.code = 400;
      status.message = "Required parameter 'nodeRef' are missing";
      status.redirect = true;
      return;
   }

   nodeRef = String(nodeRef).replace(/\\/g, '');

   var node = search.findNode(nodeRef);

   // Ensure mandatory parameters are passed
   if (node === null) {
      status.code = 404;
      status.message = "Node specified by nodeRef (" + nodeRef + ") not found.";
      status.redirect = true;
      return;
   }
   
   node.createVersion(comment, true);
}

main();
