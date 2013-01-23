<import resource="classpath:alfresco/web-extension/site-webscripts/org/alfresco/components/site/ismanager.lib.js">

// Get clients json request as a "normal" js object literal
var clientRequest = json.toString();
var clientJSON = eval('(' + clientRequest + ')');

// The site and pages we are modifiying
var siteId = clientJSON.siteId;
var newprefs = clientJSON.prefs;


//check if we are site managers!
if (isSiteManager(siteId)) {

   /**
    * The web framework doesn't have a model for pages.
    * Since the dashboard page always exist for a page it can be used to save the prefrences
    * Create a proeprty named "preferences" in the dashboard page's properties object
    * and store a json string representing the prefrences.
    */

   //fetch already stored preferences and do a merge
   var p = sitedata.getPage("site/" + siteId + "/dashboard");

   var prefs = eval('(' + (p.properties.preferences || "{}") + ')');
   for (prop in newprefs) {
      prefs[prop] = newprefs[prop];
   }

   p.properties.preferences = "" + jsonUtils.toJSONString(prefs);
   p.save();

   model.success = true;
   model.preferences = p.properties.preferences;
} else {
   model.success = false;
   model.preferences = "{}";
}

