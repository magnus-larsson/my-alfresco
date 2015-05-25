<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-header.lib.js">

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

  //Show the become site manager button for admins which are not members of a site!
  var siteConfig = widgetUtils.findObject(model.jsonModel, "id", "HEADER_SITE_CONFIGURATION_DROPDOWN");
  if (siteConfig != null) {
    var siteData = getSiteData();
    if (siteData != null) {
      if (user.isAdmin && !siteData.userIsMember && !siteData.userIsSiteManager) {
        // If the user is an admin, and a site member, but NOT the site manager then
        // add the menu item to let them become a site manager...
        siteConfig.config.widgets.push({
          id: "HEADER_BECOME_SITE_MANAGER",
          name: "alfresco/menus/AlfMenuItem",
          config: {
            id: "HEADER_BECOME_SITE_MANAGER",
            label: "become_site_manager.label",
            iconClass: "alf-cog-icon",
            publishTopic: "ALF_BECOME_SITE_MANAGER",
            publishPayload: {
              site: page.url.templateArgs.site,
              siteTitle: siteData.profile.title,
              user: user.name,
              userFullName: user.fullName
            }
          }
        });
      }
    }
  }
}());
