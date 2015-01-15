// @override projects/slingshot/config/alfresco/site-webscripts/org/alfresco/share/header/share-header.get.js 

function addTyckTillMenuItem() {
  var userMenuBar = widgetUtils.findObject(model.jsonModel, "id", "HEADER_USER_MENU_BAR");
  userMenuBar.config.widgets.push({ 
    id: "HEADER_TYCK_TILL",
    name: "tycktill/TyckTillMenuItem",
    config: {
      id: "HEADER_TYCK_TILL",
      label: "header.menu.tycktill.label"
    }
  });
}

addTyckTillMenuItem();

