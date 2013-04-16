<import resource="classpath:alfresco/module/vgr-repo/context/scripts/utils.js">

function executeCleanup() {
	var sites = getSites();

	for each(var site in sites) {
		// if site does not have the vgr:cleanble aspect, just continue
		if (!site.hasAspect("vgr:cleanable")) {
			if (logger.isLoggingEnabled()) {
				logger.log("Site '" + site.properties.title + "' does not have aspect 'vgr:cleanable'.");
			}
			
			continue;
		}
		
		var firstMessageSent = site.properties["vgr:firstMessageSent"];
		
		if (firstMessageSent == "true" || firstMessageSent == true) {
			if (logger.isLoggingEnabled()) {
				logger.log("First message already sent for site '" + site.properties.title + "'.");
			}
			
			continue;
		}
		
		var userList = getUserList(site);

		// now we have a list of users for the Site which is "SiteManager", lets
		// send an email to them...
		for each(var user in userList) {
			sendEmail(site, user);
			site.properties["vgr:firstMessageSent"] = true;
			site.save();

			if (logger.isLoggingEnabled()) {
				logger.log("First message sent and recorded.");
			}
		}
	}
}

/*
 * Send the email to a user for a site.
 */
function sendEmail(site, user) {
	var mailAction = actions.create("mail");
	var toEmail = user.properties["cm:email"];
	
	mailAction.parameters.to = toEmail;
	// TODO: add localization of message
	mailAction.parameters.subject = "Webbplatsen '" + site.properties.title + "' \u00e4r inaktuell";
	mailAction.parameters.template = companyhome.childByNamePath("Data Dictionary/Email Templates/Notify Email Templates/site_cleanup.ftl");
	mailAction.parameters.text = "";
	mailAction.execute(site);
}

/*
 * Get a list of sites that has not sent out an email yet.
 */
function getSites() {
	// first, create a date, subtract 1 to get one year back in time and then
	// make a ISO date out of it
	// Alfresco only understand ISO date
	var date = new Date();
	// TODO: remove comment the following, comment is done for debug purpose
	// only
	// date.setFullYear(date.getFullYear() - 1);
	date.setFullYear(date.getFullYear() + 1);
	var isoDate = isoDateString(date); 
	
	var def =
	  {
	     query: "SELECT * FROM st:site where cmis:creationDate < TIMESTAMP '" + isoDate + "'",
	     store: "workspace://SpacesStore",
	     language: "cmis-alfresco"
	  };
	
	return search.query(def);
}

/*
 * Get a list of users with SiteManager access rights
 */
function getUserList(site) {
	var userList = [];
	
	var permissions = site.getPermissions();
	
	for each(var permission in permissions) {
		var parts = permission.split(";");
		var allowed = parts[0] == "ALLOWED";
		var authId = parts[1];
		var id = parts[2];
		var auth = null;
		
		// if the permission is not allowed, then continue
		if (!allowed) {
			continue;
		}
		
		// if the permission is NOT the SiteManager permission, continue
		if (id != "SiteManager") {
			continue;
		}
		
        if (authId.indexOf("GROUP_") === 0) {
        	// if it's a group, resolve it properly
           var group = groups.getGroupForFullAuthorityName(authId);
           
           if (group == null) {
        	   continue;
           }
           
           for each(var user in group.getAllUsers()) {
        	   var person = people.getPerson(user.getShortName());
        	   
        	   if (person == null) {
        		   continue;
        	   }
        	   
        	   userList.push(person);
           }
        } else {
           var person = people.getPerson(authId);
           
           if (person == null) {
        	   continue;
           }
           
           userList.push(person);
        }
	}
	
	return userList;
}