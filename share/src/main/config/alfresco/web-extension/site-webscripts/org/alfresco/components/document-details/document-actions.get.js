<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/documentlibrary/include/documentlist.lib.js">

// @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/document-details/document-actions.get.js

var siteId = page.url.templateArgs.site;
var containerId = template.properties.container || "documentLibrary";
var connector = remote.connect("alfresco");

var result = connector.get("/slingshot/doclib/container/" + siteId + "/" + containerId);

var containerType = "";

if (result.status == 200) {
   var data = eval('(' + result + ')');
   containerType = data.container.type;
   }

model.containerType = containerType;

doclibCommon();