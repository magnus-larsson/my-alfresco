// @overridden projects/remote-api/config/alfresco/templates/webscripts/org/alfresco/repository/site/membership/potentialmembers.get.js

function main()
{
   // Get the args
   var siteShortName = url.templateArgs.shortname,
      site = siteService.getSite(siteShortName),
      filter = (args.filter != null) ? args.filter : (args.shortNameFilter != null) ? args.shortNameFilter : "",
      maxResults = (args.maxResults == null) ? 10 : parseInt(args.maxResults, 10),
      authorityType = args.authorityType,
      zone = args.zone;

   if (authorityType != null)
   {
      if (authorityType.match("USER|GROUP") == null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The 'authorityType' argument must be either USER or GROUP.");
         return;
      }
   }

   var peopleFound = [],
      groupsFound = [],
      notAllowed = [],
      i, ii, name, paging,
      siteInvitations = [];

   if (authorityType == null || authorityType == "USER")
   {
      var query = "TYPE:\"cm:person\" AND (firstName:'" + filter + "' OR lastName:'" + filter + "' OR userName:'" + filter + "' OR email:'" + filter + "')";

      var parts = filter.split(" ");

      if (parts.length == 2) {
        var firstName = parts[0];
        var lastName =  parts[1];
        
        query = "TYPE:\"cm:person\" AND (firstName:'" + firstName + "' AND lastName:'" + lastName + "')";
      }
      
      var sort1 = {
         column: "@{http://www.alfresco.org/model/content/1.0}firstName",
         ascending: false
      };
      
      var sort2 = {
         column: "@{http://www.alfresco.org/model/content/1.0}lastName",
         ascending: false
      };
      
      var def = {
         query: query,
         store: "workspace://SpacesStore",
         language: "fts-alfresco",
         sort: [sort1, sort2]
      };
      
      // Get the collection of people
      peopleFound = search.query(def);

      var criteria = {
              resourceName: siteShortName
           };
      siteInvitations = invitations.listInvitations(criteria);

      // Filter this collection for site membership
      for (i = 0, ii = peopleFound.length; i < ii; i++)
      {
         name = search.findNode(peopleFound[i]).properties.userName;
         if (site.getMembersRole(name) != null)
         {
            // User is already a member
            notAllowed.push(name);
         }
         else
         {
            if (contains(siteInvitations, name))
            {
               // User has already got an invitation
               notAllowed.push(name);
            }
         }
      }

      model.peopleFound = peopleFound;
   }

   if (authorityType == null || authorityType == "GROUP")
   {
      // Get the collection of groups
      paging = utils.createPaging(maxResults, -1);
      groupsFound = groups.getGroupsInZone(filter, zone, paging, "displayName");

      // Filter this collection for site membership
      for (i = 0, ii = groupsFound.length; i < ii; i++)
      {
         name = groupsFound[i].fullName;
         if (site.getMembersRole(name) != null)
         {
            // Group is already a member
            notAllowed.push(name);
         }
      }

      model.groupsFound = groupsFound;
   }
   
   model.notAllowed = notAllowed;
}

function contains(arr, value) {
    var i = arr.length;
    while (i--) {
      if (arr[i].inviteeUserName == value) return true;
    }
    return false;
}

main();