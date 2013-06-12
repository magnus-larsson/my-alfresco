function main() {
   var filters = [];

   // first remove the actions which should not be there
   for each (filter in model.filters) {
      if (filter.type === 'synced') {
         continue;
      }
      
      if (filter.type === 'syncedErrors') {
         continue;
      }
      
      filters.push(filter);
   }
   
   // then add another filter for viewing what others are editing
   filters.splice(2, 0, {
      type: "editingOthers",
      parameters: ""
   });

   model.filters = filters;
}

main();

function getContentTypes() {
   return contentTypes = [ {
      id : "vgr:document",
      value : "vgr_document"
   } ];
}

model.contentTypes = getContentTypes();
