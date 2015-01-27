//system notifications comes from a specific sites data list, configures in alfresco-global.properties
//Will display any dataList item in specified site of type vdl:systemMessage 
/*

vgr.notifications.site=<name of site>

*/

/**
 * Minimal wiki syntax
 * *foobar* <strong>foobar</strong>
 * _foobar_ <em>foobar</em>
 * [foobar](http://alfresco.vgregion.se/foobar)  <a href="alfresco.vgregion.se/foobar">foobar</a>
 * 
 */ 
var minimal = function(txt) {
   var strong = new RegExp('\\*([^*]+)\\*','g');
   var em     = new RegExp('_([^+]+)_','g');
   var link   = new RegExp("\\[([^\\]]+)\\]\\((https{0,1}://(alfresco\\.vgregion\\.se|alfresco-test\\.vgregion\\.se|alfresco-stage\\.vgregion\\.se|localhost:808[01])[a-zA-Z0-9?&:=#/$-_.+!*(),']*)\\)",'g');  
   var white  = new RegExp("[^a-zA-Z0-9åäöÅÄÖ.?+-:';\\[\\] !@$%&/()=_*\"#´`]",'g'); //regexp to whitelist all that is good in the world
   
   txt = txt.replace(white,'')
            .replace(strong,'<strong>$1</strong>')
            .replace(em,'<em>$1</em>')
            .replace(link,'<a href="$2">$1</a>');
   return txt;
}; 


var debug = [];
var d = function(obj) {
    debug.push(obj);
}

var shortName = properties.get('vgr.notifications.site','systemadmin');
d(shortName);

//lets find datalist
var q = 'PATH:"app:company_home/st:sites/cm:'+shortName+'/cm:dataLists/*" AND @dl\:dataListItemType:"vdl:systemMessage"';
var res = search.query({ query: q, language: 'fts-alfresco'});
d(res.length);


if (res && res.length > 0) {
    var now = new Date();
    var msgs = [];
    
    for (var dl=0; dl<res.length; dl++) {
        d(res[dl].title);
        for (var i=0,len = res[dl].children.length; i<len; i++) {
            var item = res[dl].children[i];
            
            var startTime = item.properties['{http://www.vgregion.se/datalist/1.0}systemMessageStartTime'];
            var endTime   = item.properties['{http://www.vgregion.se/datalist/1.0}systemMessageEndTime'];
            d(startTime);
            d(endTime);
            if (startTime <= now && now <= endTime || startTime === null && now <= endTime || startTime <= now && endTime === null || startTime ===null && endTime === null) {
                msgs.push({
                    id: item.id,
                    title: minimal(item.properties['{http://www.vgregion.se/datalist/1.0}systemMessageTitle']),
                    text: minimal(item.properties['{http://www.vgregion.se/datalist/1.0}systemMessageDescription']),
                    type: item.properties['{http://www.vgregion.se/datalist/1.0}systemMessagePriority']
                });
            }
        }
    }
    model.msgs = jsonUtils.toJSONString(msgs);
        
} else {
    model.msgs = '""';
    d('Did not find any items to display');
}
model.debug = '""'; //jsonUtils.toJSONString(debug);
