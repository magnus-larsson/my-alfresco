//define search

var sort = {
  column: "@{http://www.vgregion.se/kiv/1.0}ou",
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
    def.query = 'PATH:"/app:company_home/app:dictionary/app:kiv/app:units"';
}
else {
    def.query = 'TYPE:"kiv:unit" AND kiv:hsaidentity:"' + code + '"';
    
}

// Pass the queried sites to the template
var result = search.query(def);

if (result.length > 0) {
    model.values = result[0].children;
    
    model.values.sort(function (unit1, unit2) {
      var ou1 = unit1.properties['kiv:ou'].toLowerCase();
      var ou2 = unit2.properties['kiv:ou'].toLowerCase();
      
      if (ou1 < ou2) {
        return -1;
      }
      
      if (ou1 > ou2) {
        return 1;
      }
      
      return 0;
    });
    
} else {
    model.values = [];
}