function main() {
  // define search
  var sort = {
    column: "apelon:name",
    ascending: true
  };

  var def = {
    query: null,
    store: "workspace://SpacesStore",
    language: "fts-alfresco",
    sort: [sort]
  };

  // find node depending args. We us ethe HSAID code to identify all but the root node
  var code = args["node"];

  if (code == null || code == 'null' || code == undefined || code == "" || code == "root") {
    def.query = 'PATH:"/app:company_home/app:dictionary/app:apelon/app:swemesh"';
  } else {
    def.query = 'TYPE:"apelon:swemesh" AND apelon:internalid:"' + code + '"';
  }

  // Pass the queried sites to the template
  var result = search.query(def);
  model.values = [];

  if (result.length > 0) {
    var children = result[0].children;

    for each (child in children) {
      if (child.typeShort != 'apelon:swemesh') {
        continue;
      }

      model.values.push(child);
    }

    model.values.sort(function (keyword1, keyword2) {
      var name1 = getLabel(keyword1);
      var name2 = getLabel(keyword2);

      if (name1 < name2) {
        return -1;
      }

      if (name1 > name2) {
        return 1;
      }

      return 0;
    });
  } else {
    model.values = [];
  }
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