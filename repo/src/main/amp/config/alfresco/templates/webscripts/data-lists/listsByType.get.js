function listsByType(site,type) {
   //we know it's the dataLists container
   var lists = siteService.getSite(site)
                          .getContainer('dataLists')
                          .children;
   var sameType = [];                 
   for each (lst in lists) {
      if (type == lst.properties['{http://www.alfresco.org/model/datalist/1.0}dataListItemType'].substr(3)) {
         sameType.push({ nodeRef: lst.nodeRef, name: lst.properties['{http://www.alfresco.org/model/content/1.0}title'] });
      }
   }
   return sameType;
}


if (!url.templateArgs.type || !url.templateArgs.site ) {
   status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Unsufficient arguments");
   model.lists = "[]"
} else {
   model.lists = jsonUtils.toJSONString(listsByType(url.templateArgs.site,url.templateArgs.type));
}


