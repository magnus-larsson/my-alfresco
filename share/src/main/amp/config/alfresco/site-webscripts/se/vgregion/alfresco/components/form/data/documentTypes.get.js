var alfrescoURL = "/vgr/apelon/documentTypes";

var connector = remote.connect("alfresco");
var callresult = connector.get(alfrescoURL);

model.result = callresult;
