function main() {
   // First find node
   // find node depending args. We use a comma seperated list of ids to identify nodes
   model.values = []; //default to empty
   model.paths= []; //default to empty

   var result = null;

   if (args['nodes']) {
      result = getNodes();
   }

   if (result != null) {
      model.values = [];
      model.paths = [];

      for each (item in result) {
         var path = getPath(item);

         model.values.push(item);
         model.paths.push(path);
      }
   }
}

function getNodes() {
   var codes = args["nodes"].split('#alf#');

   return doSearch(codes);
}

function doSearch(codes) {
   var sort = {
      column: "apelon:name",
      ascending: true
   };

   var def = {
      query: 'TYPE:"apelon:documentStructure" AND ',
      store: "workspace://SpacesStore",
      language: "fts-alfresco",
      sort: [sort]
   };

   var queries = [];

   for each(code in codes) {
      queries.push('apelon:internalid:"' + code + '"');
   }

   def.query = def.query + '(' + queries.join(' OR ') + ')';

   return search.query(def);
}

function getPath(node) {
   var documentStructure = true;

   var path = "";

   var item = node;

   while (documentStructure) {
      if (path != "") {
         path = '#alf# ' + path;
      }

      path = item.properties['apelon:name'] + path;

      documentStructure = item.parent.typeShort == 'apelon:documentStructure';

      item = item.parent;
   }

   return path;
}

main();