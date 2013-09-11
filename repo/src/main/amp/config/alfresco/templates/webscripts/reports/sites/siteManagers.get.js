function main() {
   var shortName = args.shortName;
   
   var site = siteService.getSite(shortName);
   
   var managers = site.listMembers(null, "SiteManager", 1000, false);
   
   model.managers = [];
   
   for (var manager in managers) {
      var user = groups.getUser(manager);
      
      model.managers.push({
         "userid" : manager,
         "fullname": user.fullName
      });
   }
}

main();
