// @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/document-details/document-actions.get.js

var connector = remote.connect("alfresco");

var result = connector.get("/slingshot/doclib/container/" + model.site + "/" + model.container);

var containerType = "";

if (result.status == 200) {
   var data = eval('(' + result + ')');
   
   containerType = data.container.type;
}

model.widgets[0].options.containerType = containerType
