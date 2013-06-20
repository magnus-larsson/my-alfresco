script: {

   var dictionaryNodesQuery = "PATH:\"/app:company_home/app:dictionary//*\" AND TYPE:\"cm:content\"";
   var systemNodesQuery = "PATH:\"/sys:system//*\" AND ASPECT:\"cm:preferences\"";

   // check if a site has been provided, if so add the site content to the
   // exclude list...
   if (args.site == undefined || args.site == null || args.site.length == 0) {
      logger.log("Listing Data Dictionary and avm:sitestore content");

      model.dictionaryNodes = getWorkspaceNodes(dictionaryNodesQuery);
      model.workspaceSystemNodes =getWorkspaceNodes(systemNodesQuery);
      
      model.avmNodes = getStoreNodes("avm://sitestore");
      
      model.userNodes = getStoreNodes("user://alfrescoUserStore");
      
      model.systemNodes = getStoreNodes("system://system");
      
      model.siteNodes = [];
   } else if (siteService.getSite(args.site) != null) {
      logger.log("Listing content in site " + args.site);
      var siteNodesQuery = "PATH:\"/app:company_home/st:sites/cm:" + args.site + "//*\" AND TYPE:\"cm:content\"";
      var nodes = search.luceneSearch(siteNodesQuery);
      var siteNodes = [];
      for ( var i = 0; i < nodes.length; i++) {
         if (nodes[i] != null) {
            siteNodes.push(contentUrlResolver.getContentUrl(nodes[i].nodeRef));
         } else {
            logger.log("Found null node in site " + args.site);
         }
      }
      model.siteNodes = siteNodes;
      model.avmNodes = [];
      model.userNodes = [];
      model.dictionaryNodes = [];
      model.workspaceSystemNodes = [];
      model.systemNodes = [];
   } else {
      status.code = 404;
      status.message = "Could not find site " + args.site;
      status.redirect = true;
      break script;
   }

}

function getWorkspaceNodes(query) {
   // add all content in data dictionary to the exclude list
   var nodes = search.luceneSearch(query);
   
   var result = [];
   
   for ( var i = 0; i < nodes.length; i++) {
      if (nodes[i] != null) {
         result.push(contentUrlResolver.getContentUrl(nodes[i].nodeRef));
      } else {
         logger.log("Found null node in Data Dictionary!");
      }
   }
   
   return result;
}

function getStoreNodes(storeName) {
   // add all content in avm:sitestore to the exclude list (to keep site
   // settings correct)
   var xpathnodes = search.xpathSearch(storeName, "/");
   
   var storeNodes = [];
   
   if (xpathnodes[0] == undefined || xpathnodes[0] == null) {
      status.code = 404;
      
      status.message = "Store " + storeName + " not found.";
      
      status.redirect = true;
   } else {
      logger.log("Found store " + storeName);
      
      addNode(xpathnodes[0], storeNodes);
   }
   
   return storeNodes;
}

function addNode(parentNode, nodes) {
   if (parentNode.isContainer && parentNode.childAssocs["cm:contains"] != null) {
      for ( var i = 0; i < parentNode.childAssocs["cm:contains"].length; i++) {
         addNode(parentNode.childAssocs["cm:contains"][i], nodes);
      }
   } else if (parentNode.isDocument) {
      nodes.push(contentUrlResolver.getContentUrl(parentNode.nodeRef));
   }
}