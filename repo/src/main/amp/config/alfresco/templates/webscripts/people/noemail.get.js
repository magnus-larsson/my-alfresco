var query = "TYPE:\"cm:person\" AND ISUNSET:\"cm:email\"";

var sort1 = {
    column: "@{http://www.alfresco.org/model/content/1.0}userName",
    ascending: false
};

var def = {
    query: query,
    store: "workspace://SpacesStore",
    language: "fts-alfresco"
    // sort: [sort1],
};
  
// Pass the queried sites to the template
model.peoplelist = search.query(def);
