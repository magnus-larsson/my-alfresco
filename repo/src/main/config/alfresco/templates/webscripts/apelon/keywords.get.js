function main() {
   // First find node
   // find node depending args. We use a comma separated list of ids to identify nodes
   model.values = []; //default to empty
   model.paths= []; //default to empty

   var result = null;

   if (args['nodes']) {
      result = getNodes();
   } else if (args['nodeRef']) {
      result = getKeywords();
   }

   if (result != null) {
      model.values = [];
      model.paths = [];

      result.sort(function (item1, item2) {
         var name1 = getLabel(item1);
         var name2 = getLabel(item2);

         if (name1 < name2) {
            return -1;
         }

         if (name1 > name2) {
            return 1;
         }

         return 0;
      });

      for each (item in result) {
         var path = getPath(item);

         model.values.push(item);
         model.paths.push(path);
      }
   }
}

function getKeywords() {
   var nodeRef = args['nodeRef'];

   var codes = apelon.getKeywords(nodeRef);

   return doSearch(codes);
}

function getNodes() {
   var codes = args["nodes"].split('#alf#');

   return doSearch(codes);
}

function doSearch(codes) {
   if (codes.length == 0) {
      return null;
   }

   var sort = {
      column: "apelon:name",
      ascending: true
   };

   var def = {
      query: 'TYPE:"apelon:swemesh" AND ',
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
   var swemesh = true;

   var path = "";

   var item = node;

   while (swemesh) {
      if (path != "") {
         path = '#alf#' + path;
      }

      path = getLabel(item) + path;

      swemesh = item.parent.typeShort == 'apelon:swemesh';

      item = item.parent;
   }

   return path;
}

function getLabel(item) {
   var result = item.properties['apelon:name'];

   var children = item.children;

   for each (var child in children) {
      if (child.name != 'preferredSynonym') {
         continue;
      }

      var values = child.properties['apelon:value'];

      for each (var value in values) {
         return value;
      }
   }

   return result;
}

main();