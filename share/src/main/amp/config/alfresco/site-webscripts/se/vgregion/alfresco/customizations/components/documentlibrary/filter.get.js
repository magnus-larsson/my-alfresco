function main() {
   
   var filters = [];

   // first remove the actions which should not be there
   for each (filter in model.filters) {
      if (filter.id === 'synced') {
         continue;
      }
      
      if (filter.id === 'syncedErrors') {
         continue;
      }
      
      filters.push(filter);
   }

   model.filters = filters;
}

main();
