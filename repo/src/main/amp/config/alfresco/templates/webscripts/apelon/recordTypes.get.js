// Get the args
var documentTypeId = args["documentTypeId"];

var values = apelon.getRecordTypeList(documentTypeId);

// pass the list of values to the template
model.values = values;