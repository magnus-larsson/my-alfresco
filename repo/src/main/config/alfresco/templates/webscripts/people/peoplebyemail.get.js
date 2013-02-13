function main() {
   // Get the args
   var email = args["email"];

   model.peoplelist = [];

   if (!email || email == "") {
      return;
   }

   var query = 'TYPE:"cm:person" AND email:"' + email + '"';

   var def = {
      query: query,
      store: "workspace://SpacesStore",
      language: "fts-alfresco"
   };

   model.peoplelist = search.query(def);
}

main();