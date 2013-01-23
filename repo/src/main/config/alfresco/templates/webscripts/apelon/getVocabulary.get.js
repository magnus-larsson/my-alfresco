// Get the args
var path = args["path"];

// get the list from apelon based on path and sorted
var values = apelon.getVocabulary(path, true);

// pass the list of values to the template
model.values = values;
