// @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/upload/flash-upload.get.js

/**
 * Custom content types
 */
function getContentTypes()
{
   // TODO: Data webscript call to return list of available types
   var contentTypes = [
   {
      id: "vgr:document",
      value: "vgr_document"
   }];

   return contentTypes;
}

model.contentTypes = getContentTypes();