// @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/people-finder/people-finder.get.js

function vgr_main() {
  /**
  * Replace all standard people serches with custom VGR script where it has not yet been customized.
  * This customization changes the default search from "firstname or lastname" to "firstname and lastname"
  **/
  if (model.widgets[0].options.dataWebScript.indexOf("vgr")===-1) {
    model.widgets[0].options.dataWebScript = "vgr/people";
  }
}

vgr_main();
