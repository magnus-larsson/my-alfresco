// Get the args

var searchBase = args.searchBase ? args.searchBase : "";

// get the list from kiv 
var values = kiv.findOrganisationalUnits(searchBase);

// pass the list of values to the template
model.values = values;
