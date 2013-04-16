/**
 * Common functions
 */

/**
 * Params are missing
 */
var required_missing = function(param) {
   status.code = 400;
   status.message = "Required parameters are missing: "+param;
   status.redirect = true;
   return [];
};


//check alfresco-global.properties for configuration of key value pairs
//i.e. one field with text and one .id field with the actual id. we only display the text one, 
//but both needs to be inherited
var pairs = (function(){
  
   var pairslist = (''+properties.get('vgr.metadata.inheritance.pairs','')).split(',');
   var keys = {};
   var vals = {};
   for each (pair in pairslist) {
      var parts = pair.split('|');
      if (parts.length === 2) {
         var key   = parts[0].indexOf('{http://www.vgregion.se/model/1.0}') === 0  ? parts[0]:'{http://www.vgregion.se/model/1.0}'+parts[0];
         var value = parts[1].indexOf('{http://www.vgregion.se/model/1.0}') === 0  ? parts[1]:'{http://www.vgregion.se/model/1.0}'+parts[1];
         keys[key]   = value;
         vals[value] = key;
      }
   }
   return [keys,vals];
})();
var keys = pairs[0];
var vals = pairs[1];


/**
 * Find file and target nodes, validate them and
 * execute a function for each vgr property
 * 
 */ 
var forEachProperty = function(refs,target_ref,func) {    
   //blacklisted properties are ignored
   var blacklist = properties.get('vgr.metadata.inheritance.blacklist','').split(',');
   var blacklisted = {};
   for each (black in blacklist) {
      if (black.indexOf('{http://www.vgregion.se/model/1.0}') === 0) {
         blacklisted[black] = true;
      } else {
         blacklisted['{http://www.vgregion.se/model/1.0}'+black] = true;
      }
   }
   
   
   var target = search.findNode(target_ref);
   
   if (!target) { 
      return required_missing('target');
   }
   
   //check if the folder has a {http://www.vgregion.se/model/1.0}metadata aspect
   //if not nothing can intersect
   if (!target.hasAspect('{http://www.vgregion.se/model/1.0}metadata')) {
      debug.push('no aspect')
      return;
   }
   
   //find all files
   var files  = [];
   for each (var ref in refs) {
      var n = search.findNode(ref);
      if (n && n.hasAspect('{http://www.vgregion.se/model/1.0}metadata') && n.isDocument) {
         files.push(n);
      }
   }

   if (files.length === 0) {
         return;
   }

   //loop over target properties
   for (var prop in target.properties) {
      //only vgr document props
      if (prop.indexOf('{http://www.vgregion.se/model/1.0}') === 0 && !blacklisted[prop]) {
        for each (var f in files) {
            func(f,target,prop);
         }
      }
   }
};