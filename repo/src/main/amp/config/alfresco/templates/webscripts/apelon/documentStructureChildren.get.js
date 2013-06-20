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
    def.query = 'PATH:"/app:company_home/app:dictionary/app:apelon/app:documentStructure"';
  } else {
    def.query = 'TYPE:"apelon:documentStructure" AND apelon:internalid:"' + code + '"';
  }

  // Pass the queried sites to the template
  var result = search.query(def);
  model.values = [];

  if (result.length > 0) {
    var children = result[0].children;

    for each (child in children) {
      if (child.typeShort != 'apelon:documentStructure') {
        continue;
      }

      model.values.push(child);
    }

    model.values.sort(function (ds1, ds2) {
      var name1 = ds1.properties['apelon:name'].toLowerCase();
      var name2 = ds2.properties['apelon:name'].toLowerCase();

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

main();