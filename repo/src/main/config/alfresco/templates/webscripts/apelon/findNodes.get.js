// Get the args
var namespace = args["namespace"];
var propertyName = args["propertyName"];
var propertyValue = args["propertyValue"];

// get the list from apelon based on namespace, property name, property value and sorted
var values = apelon.findNodes(namespace, propertyName, propertyValue, true);

// pass the list of values to the template
model.values = values;
