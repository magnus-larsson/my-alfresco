var siteId = page.url.templateArgs.site;
var p = sitedata.getPage("site/" + siteId + "/dashboard");

// Don't try to simplify this - it won't work.
var usedPages = eval('(' + p.properties.sitePages + ')');
usedPages = usedPages != null ? usedPages : [];

var defaultPage = 'dashboard';
for ( var i = 0; i < usedPages.length; i++) {
   if (usedPages[i].defaultPage) {
      defaultPage = usedPages[i].pageId;
      break;
   }
}

model.defaultPage = defaultPage;
model.shortName = siteId;
