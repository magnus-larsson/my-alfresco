// @override projects/slingshot/config/alfresco/site-webscripts/org/alfresco/share/header/share-header.get.js 

function hideTasks() {
  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_TASKS");
}

hideTasks();

