/**
 * Custom content types for flash-upload template
 */
function getContentTypes()
{
   var contentTypes = [
   {
      id: "vgr:document",
      value: "vgr_document"
   }];

   return contentTypes;
}

model.contentTypes = getContentTypes();
model.site = page.url.templateArgs.site;
model.user = user.name;