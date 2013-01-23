function isSiteManager(siteId) {
   var userIsSiteManager = false;
   //Call the repository to see if the user is site manager or not
   userIsSiteManager = false;
   var obj = context.properties["memberships"];
   if (!obj)
   {
	   var json = remote.call("/api/sites/" + siteId + "/memberships/" + stringUtils.urlEncode(user.name));
	   if (json.status == 200)
	   {
	      obj = eval('(' + json + ')');
	   }
	}
	if (obj)
	{
      userIsSiteManager = (obj.role == "SiteManager");
	}
   return userIsSiteManager;   
}

