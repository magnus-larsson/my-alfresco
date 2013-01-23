// Get the args
var debug = [];

function d(txt) {
    //debug.push(txt);
}

var filter = args["filter"];
var maxResults = args["maxResults"];
var siteFilter = args["siteFilter"];

d(args["filter"]);
d(args["maxResults"]);
d(args["siteFilter"]);

siteFilter = siteFilter === 'true'; //default to false


function doQuery(filter,maxResults) {
    var query = "TYPE:\"cm:person\" AND (firstName:'" + filter + "' OR lastName:'" + filter + "' OR userName:'" + filter + "' OR email:'" + filter + "')";

    var parts = filter != null ? filter.split(" ") : null;

    if ((parts != null) && (parts.length == 2)) {
	    var firstName = parts[0];
	    var lastName =  parts[1];
	
	    query = "TYPE:\"cm:person\" AND (firstName:'" + firstName + "' AND lastName:'" + lastName + "')";
    }

    var sort1 =
      {
         column: "@{http://www.alfresco.org/model/content/1.0}firstName",
         ascending: false
      };
      var sort2 =
      {
         column: "@{http://www.alfresco.org/model/content/1.0}lastName",
         ascending: false
      };
      var paging =
      {
         maxItems: 500,
         skipCount: 0
      };
      
      var def =
      {
         query: query,
         store: "workspace://SpacesStore",
         language: "fts-alfresco",
         sort: [sort1, sort2],
         page: paging
      };
    return search.query(def);
}  


//since we can't check for site membership via a fts query we use the siteService
//instead
function siteQuery(filter) {
    d("Site query");
    if (args["site"]) {
        var site = siteService.getSite(args["site"])
        var members = site.listMembers('','',0,true);   
        
        var ppl = [];
        for (var user in members) {
            d(user);
            ppl.push(people.getPerson(user));
        }
        
        //lets filter them!
        var filtered = [];
        var parts = filter != null ? filter.split(" ") : null;
        //if user supplies two words we assume they are trying to match firstname  and lastname (ANDing them)
        if (parts && parts.length == 2) {
            d("Two parts");
            //we support * wildcard syntax
            var firstName = new RegExp(parts[0].replace(/\*/g,'.*')+'$','i');
            var lastName  = new RegExp(parts[1].replace(/\*/g,'.*')+'$','i');          
        
            for each (person in ppl) {
                if (firstName.test(person.properties.firstName) && lastName.test(person.properties.lastName)) {
                    filtered.push(person);
                }
            }    
            
        } else {
            //when one word (or three...) is specified we do an OR on name,username and email
            //we suppot * syntax
            d(filter.replace(/\*/g,'.*')+'$')
            var f = new RegExp(filter.replace(/\*/g,'.*')+'$','i');
            for each (person in ppl) {                
                if (f.test(person.properties.firstName) || f.test(person.properties.lastName) || f.test(person.properties.userName) || f.test(person.properties.email)) {
                    filtered.push(person);
                }
            }
        }
    
        return filtered;
    }
    
    d("No site specified, doing ordinary query");
    return doQuery(filter,maxResults);
}

d(siteFilter);
//filter on site TODO: there must be a better and faster way to do this!
if (siteFilter) {
    model.peoplelist = siteQuery(filter);
} else {
    model.peoplelist = doQuery(filter,maxResults);
}
model.siteFilter = siteFilter;
model.debug = debug.join(', ');
