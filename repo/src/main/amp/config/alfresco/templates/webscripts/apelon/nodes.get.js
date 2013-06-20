// Get the args
var nodeType = args["nodeType"];

// get the list from apelon based on path and sorted
var values = apelon.getNodes(nodeType);

// pass the list of values to the template
model.values = values;
