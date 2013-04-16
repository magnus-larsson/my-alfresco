var result = new Array();

var filter  = args.filter;

var alfrescoUrl = "/vgr/people?filter=" + encodeURIComponent(filter);
 
var connector = remote.connect("alfresco");

var callresult = connector.get(alfrescoUrl);

var jsonStr = eval('('+callresult+')');

for(var i = 0; i < jsonStr["people"].length; i++) {
  var user = jsonStr["people"][i];
  
  user.representation = user.firstName + " " + user.lastName + " (" + user.userName + ")";
  
  if (user.organization != null) {
    user.representation  = user.representation + " " + user.organization; 
  }
  
  result.push(user);
}

model.result = jsonUtils.toJSONString(result);
