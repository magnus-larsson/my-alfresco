//First find node
// find node depending args. We use a comma seperated list of HSAID to identify nodes
model.values = []; //default to empty

if (args['nodes']) {
	var codes = args["nodes"].split('#alf#');
	
	var sort = {
	  column: "kiv:ou",
	  ascending: true
	};
	
	var def = {
	    query: 'TYPE:"kiv:unit" AND ',
	    store: "workspace://SpacesStore",
	    language: "fts-alfresco",
	    sort: [sort]
	};
	
	var querys = [];
	for each (code in codes) {
	    querys.push('kiv:hsaidentity:"'+code+'"');
	}
	def.query = def.query + '(' + querys.join(' OR ') +')';
	
	model.values = search.query(def);
}