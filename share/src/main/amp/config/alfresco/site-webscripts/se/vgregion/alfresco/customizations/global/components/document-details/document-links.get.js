// @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/document-details/document-links.get.js

if (model.document!==null) {
  model.widgets[0].options.fileName = model.document.fileName;
}