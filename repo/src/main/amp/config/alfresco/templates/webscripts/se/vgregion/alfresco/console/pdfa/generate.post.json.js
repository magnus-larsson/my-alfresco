function main() {
   var nodeRef = null;

   if (json.has("nodeRef")) {
      var nodeRef = json.get('nodeRef') + '';
   }

   logger.warn(nodeRef);

   model.count = 0;

   if (nodeRef && nodeRef.length > 0) {
      var node = search.findNode(nodeRef);

      node.createThumbnail("pdfa", true);

      model.count = 1;
   } else {
      model.count = storage.createMissingPdfRenditions();
   }
}

main();
