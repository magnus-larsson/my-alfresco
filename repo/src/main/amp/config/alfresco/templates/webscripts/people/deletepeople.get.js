var paging = {
      maxItems: 300
}

var def = {
        query: "TYPE:\"cm:person\"",
        store: "workspace://SpacesStore",
        language: "lucene",
        page: paging
};

var all = search.query(def);

var count = 0;

while (all.length > 0) {
   logger.warn(all.length);

   for each (var p in all) {
           var username = p.properties["cm:userName"];

           if (username == 'admin' || username == 'guest') {
                   continue;
           }

           people.deletePerson(username);

           count++;
   }
   
   logger.warn("Deleted in total " + count + " users");
   
   all = search.query(def);
}

logger.warn("Finished!");