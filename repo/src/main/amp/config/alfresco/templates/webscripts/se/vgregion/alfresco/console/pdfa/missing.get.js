function main() {
   var max = args.max != null ? args.max : -1;
   var skip = args.skip != null ? args.skip : 0;

   max = parseInt(max);
   skip = parseInt(skip);

   var documents = getMissing();

   var nodes = [];

   for (var x = 0; x < documents.length; x++) {
      if (skip > x) {
         break;
      }

      if (x >= (max + skip)) {
         break;
      }

      var document = documents[x];

      var sourcePath = getSourcePath(document);

      var name = document.properties['vgr:dc.title'];

      if (!name || name.length == 0) {
         name = document.name;
      }

      var filename = document.properties['vgr:dc.title.filename.native'];
      if (!filename || filename.length == 0) {
         filename = document.properties['vgr:dc.title.filename'];
      }

      nodes.push({
         "nodeRef": document.nodeRef + "",
         "name": name,
         "sourcePath": sourcePath,
         "storagePath": document.displayPath,
         "filename": filename
      });
   }

   model.nodes = nodes;
   model.recordsReturned = nodes.length;
   model.startIndex = parseInt(skip);
   model.pageSize = parseInt(max);
   model.totalRecords = documents.length;
}

function getMissing() {
   var query = 'TYPE:"vgr:document" AND ASPECT:"vgr:published"';

   var documents = search.luceneSearch(query);

   var nodes = [];

   for (var x = 0; x < documents.length; x++) {
      var document = documents[x];

      if (document.properties.content.mimetype == 'application/pdf') {
         continue;
      }

      if (!storage.pdfaRendable(document.nodeRef)) {
         continue;
      }

      var rendition = renditionService.getRenditionByName(document, 'cm:pdfa');

      if (!rendition) {
         nodes.push(document);
      }
   }

   return nodes;
}

function getSourcePath(node) {
   var sourceId = node.properties['vgr:dc.source.documentid'];

   var path = "";

   if (sourceId && sourceId.length > 10) {
      var source = search.findNode(sourceId);

      if (source) {
         path = source.displayPath;
      }
   }

   return path;
}

main();