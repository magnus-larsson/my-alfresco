// @override projects/slingshot/config/alfresco/site-webscripts/org/alfresco/share/header/sites-menu.get.js 

function replaceDashboardTargetUrlWithDefaultRedirect(widget) {
  widget.config.targetUrl = widget.config.targetUrl.replace("/dashboard", "/default-redirect");
  return widget;
}

function handleWidgets(widgets) {
  if (widgets != null && widgets.length > 0) {
    for (var i=0;i<widgets.length;i++) {
      widgets[i] = replaceDashboardTargetUrlWithDefaultRedirect(widgets[i]);
    }
  }
  return widgets;
}

function changeSitesLandingPage(sitesMenu) {
  sitesMenu.widgetsRecent = handleWidgets(sitesMenu.widgetsRecent);
  sitesMenu.widgetsFavourites = handleWidgets(sitesMenu.widgetsFavourites);
  return sitesMenu;
}

model.sitesMenu = changeSitesLandingPage(model.sitesMenu);