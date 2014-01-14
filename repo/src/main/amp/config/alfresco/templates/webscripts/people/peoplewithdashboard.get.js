function main() {
   var filter = args.filter;
   
   var userPagesNode = search.findNode('avm://sitestore/-1|alfresco|site-data|pages|user');
   
   var userNodes = userPagesNode.children;
   
   var users = new Array();
   
   for (var x = 0; x < userNodes.length; x++) {
      var userNode = userNodes[x];
      
      var userName = userNode.properties['cm:owner'];
      
      if (filter && filter.length > 0) {
         if (userName.indexOf(filter) === 0) {
            pushuser(users, userName);
         }
      } else {
         pushuser(users, userName);
      }
   }
   
   model.users = users;
}

function pushuser(users, username) {
   /*
   if (username.indexOf('_') >= 0) {
     return;
   }
   */
  
   var user = people.getPerson(username);
  
   if (!user) {
     return;
   }
  
   users.push(username);
}

main();