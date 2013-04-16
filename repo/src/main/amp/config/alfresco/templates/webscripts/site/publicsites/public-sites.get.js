// @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/dashlets/my-sites.get.js
function main()
{
    // Get the filter parameters
    var nameFilter = args["nf"];
    var sitePreset = args["spf"];
    var sizeString = args["size"];
    var publicSites = [];
    
    // Get the list of sites
    var sites = serviceUtils.listPublicSites(nameFilter, sitePreset, sizeString != null ? parseInt(sizeString) : 0);
    
    for each (var site in sites) {
      if (site.visibility != "PUBLIC") {
        continue;
      }
      
      publicSites.push(site);
    }
    
    model.sites = publicSites;
}

main();