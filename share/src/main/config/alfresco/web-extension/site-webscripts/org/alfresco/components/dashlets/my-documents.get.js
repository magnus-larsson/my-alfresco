// @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/dashlets/my-documents.get.js

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
model.user = user.name;