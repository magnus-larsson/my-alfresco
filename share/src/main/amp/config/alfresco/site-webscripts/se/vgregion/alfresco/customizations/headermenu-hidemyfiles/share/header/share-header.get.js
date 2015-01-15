// @override projects/slingshot/config/alfresco/site-webscripts/org/alfresco/share/header/share-header.get.js 

function hideMyFiles() {
  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_MY_FILES");
}

hideMyFiles();

