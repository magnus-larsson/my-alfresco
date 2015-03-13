// @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/console/application.get.js
function remove_themes() {
  var themes = new Array();
  
  for (var i=0, t; i<model.themes.length; i++)
  {
    //Filter out all unwanted themes
    if ((model.themes[i].id=="default") ||
      (model.themes[i].id=="gdocs") ||
      (model.themes[i].id=="greenTheme") ||
      (model.themes[i].id=="greyTheme") ||
      (model.themes[i].id=="hcBlack") ||
      (model.themes[i].id=="yellowTheme")) {
      continue;
    }
    themes.push(model.themes[i]);
  }
  model.themes = themes;
}

remove_themes();