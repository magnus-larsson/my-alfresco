(function () {

  var userMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_USER_MENU");
  var userMenuWidgets = userMenu.config.widgets;

  var logoutMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_USER_MENU_LOGOUT");

  // the menu is not added if external authentication is used, so we have to
  // re-add it.
  if (!logoutMenu) {
    logoutMenu = {
      id: "HEADER_USER_MENU_LOGOUT",
      name: "alfresco/header/AlfMenuItem",
      config: {
        id: "HEADER_USER_MENU_LOGOUT",
        label: "logout.label",
        iconClass: "alf-user-logout-icon",
        publishTopic: "ALF_DOLOGOUT"
      }
    };

    userMenuWidgets.push(logoutMenu);
  }

}());
