function main() {
    // Get the list of sites
    var sites = siteService.listSites(null, null);
    
    var result = [];
    
    for each (var site in sites) {
      var size = getSiteSize(site);
      
      logger.warn(size);
      
      result.push({
         shortName: site.shortName,
         title: site.title,
         size: size / (1024 * 1024)
      });
    }
    
    model.sites = result;
}

function getSiteSize(site) {
   var siteChildren = site.node.children;
   
   var total = 0;
   
   for each(var child in siteChildren) {
      if (child.isContainer) {
         total += getFolderSize(child);
      }
      
      if (child.isDocument) {
         total += getFileSize(child);
      }
   }
   
   return total;
}

function getFolderSize(folder) {
   var total = 0;
   
   for each(var child in folder.children) {
      if (child.isContainer) {
         total += getFolderSize(child);
      }
      
      if (child.isDocument) {
         total += getFileSize(child);
      }
   }
   
   return total;
}

function getFileSize(file) {
   if (!file.isDocument) {
      return 0;
   }
   
   return file.properties.content.size;
}

main();