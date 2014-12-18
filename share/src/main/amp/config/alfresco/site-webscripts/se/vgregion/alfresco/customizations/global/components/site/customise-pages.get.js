function vgr_main() {
   var sitePages = sitedata.getPage("site/" + model.siteId + "/dashboard").properties.sitePages;
   sitePages = eval('(' + sitePages + ')');

   for ( var x = 0; x < sitePages.length; x++) {
      var sitePage = sitePages[x];

      for ( var y = 0; y < model.pages.length; y++) {
         var modelPage = model.pages[y];

         if (sitePage.pageId == modelPage.pageId) {
            modelPage.defaultPage = sitePage.defaultPage ? true : false;
         }
      }
   }

   for ( var x = 0; x < model.pages.length; x++) {
      var page = model.pages[x];

      page.defaultPage = page.defaultPage ? true : false;
   }
   
   var themes = [];
   
   // remove the test and stage themes
   for (var x = 0; x < model.themes.length; x++) {
      var theme = model.themes[x];
      
      if (theme.id == 'testTheme' || theme.id == 'stageTheme') {
         continue;
      }
      
      themes.push(theme);
   }
   
   model.themes = themes;
}

vgr_main();
