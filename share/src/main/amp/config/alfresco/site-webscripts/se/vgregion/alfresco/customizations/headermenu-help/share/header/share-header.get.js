// @override projects/slingshot/config/alfresco/site-webscripts/org/alfresco/share/header/share-header.get.js 


function getCustomHelpLink(type) {
  var helpConfig = config.scoped["HelpPages"],
    helpLink = "";
  if (helpConfig != null)
  {
    helpConfig = helpConfig["help-pages"];
    helpLink = (helpConfig != null) ? helpConfig.getChildValue("share-help-" + type) : helpConfig.getChildValue("share-help");
  }
  return helpLink;
}

function removeOldHelpLink() {
  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_USER_MENU_HELP");
}

function createHelpMenuItems() {
  var helpMenuWidgets = [];
  helpMenuWidgets.push({
    id: "HEADER_HELP_MENU_HELP_INTERNAL",
    name: "alfresco/header/AlfMenuItem",
    config:
    {
      id: "HEADER_HELP_MENU_HELP_INTERNAL",
      label: "header.help-internal.label",
      iconClass: "alf-user-help-icon",
      targetUrl: getCustomHelpLink("internal"),
      targetUrlType: "FULL_PATH",
      targetUrlLocation: "NEW"
    }
  });

  helpMenuWidgets.push({
    id: "HEADER_HELP_MENU_HELP_EXTERNAL",
    name: "alfresco/header/AlfMenuItem",
    config:
    {
      id: "HEADER_HELP_MENU_HELP_EXTERNAL",
      label: "header.help-external.label",
      iconClass: "alf-user-help-icon",
      targetUrl: getCustomHelpLink("external"),
      targetUrlType: "FULL_PATH",
      targetUrlLocation: "NEW"
    }
  });

  return helpMenuWidgets;
}

function addHelpMenu() {
  var userMenuBar = widgetUtils.findObject(model.jsonModel, "id", "HEADER_USER_MENU_BAR");
  userMenuBar.config.widgets.push({
    id: "HEADER_HELP_MENU_POPUP",
    name: "alfresco/header/AlfMenuBarPopup",
    config: {
      id: "HEADER_HELP_MENU_POPUP",
      label: "help.label",
      widgets: [
        {
            id: "HEADER_HELP_MENU",
            name: "alfresco/menus/AlfMenuGroup",
            config: {
              id: "HEADER_HELP_MENU",
              widgets: createHelpMenuItems()
            }
        }
      ]
    }
  });
}

/*

userMenuWidgets.push({
         id: "HEADER_USER_MENU_HELP",
         name: "alfresco/header/AlfMenuItem",
         config:
         {
            id: "HEADER_USER_MENU_HELP",
            label: "help.label",
            iconClass: "alf-user-help-icon",
            targetUrl: getHelpLink(),
            targetUrlType: "FULL_PATH",
            targetUrlLocation: "NEW"
         }
      });

*/



removeOldHelpLink();
addHelpMenu();