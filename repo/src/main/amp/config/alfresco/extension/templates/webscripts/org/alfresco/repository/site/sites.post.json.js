// @overridden projects/remote-api/config/alfresco/templates/webscripts/org/alfresco/repository/site/sites.post.json.js

function main()
{
   // Ensure the user has Create Site capability
   if (!siteService.hasCreateSitePermissions())
   {
      status.setCode(status.STATUS_FORBIDDEN, "error.noPermissions");
      return;
   }
   
   // Get the details of the site
   if (json.has("shortName") == false || json.get("shortName").length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Short name missing when creating site");
      return;
   }
   var shortName = json.get("shortName");
   
   // See if the shortName is available
   var site = siteService.getSite(shortName);
   if (site != null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "error.duplicateShortName");
      return;
   }
   
   if (json.has("sitePreset") == false || json.get("sitePreset").length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Site preset missing when creating site");
      return;
   }
   var sitePreset = json.get("sitePreset");
   
   var title = null;
   if (json.has("title"))
   {
      title = json.get("title");
   }
      
   var description = null;
   if (json.has("description"))
   {
      description = json.get("description");
   }
   
   var sitetype = null;
   if (json.has("type") == true)
   {
      sitetype = json.get("type");
   }
   
   // Use the visibility flag before the isPublic flag
   var visibility = siteService.PUBLIC_SITE;
   if (json.has("visibility"))
   {
      visibility = json.get("visibility");
   }
   else if (json.has("isPublic"))
   {
      var isPublic = json.getBoolean("isPublic");
      if (isPublic == true)
      {
         visibility = siteService.PUBLIC_SITE;
      }
      else
      {
         visibility = siteService.PRIVATE_SITE;
      }
   }

   // check the responsibility code, if this is not set site creation is not enabled
   // if the user is an admin, site creation should be possible, though
   var responsibilityCode = person.properties["vgr:responsibility_code"];
   var isAdmin = people.isAdmin(person);
   
   if ((responsibilityCode === null || responsibilityCode === '') && !isAdmin) {
      status.code = status.STATUS_FORBIDDEN;
      status.message = "För att kunna skapa en webbplats måste du vara anställd av VGR.";
      status.redirect = true;
      return;
   }
   
   // Create the site 
   var site = null;   
   if (sitetype == null)
   {
      site = siteService.createSite(sitePreset, shortName, title, description, visibility);
   }
   else
   {
      site = siteService.createSite(sitePreset, shortName, title, description, visibility, sitetype);
   }
   
   // Put the created site into the model
   model.site = site;
}

main();