<import resource="classpath:alfresco/templates/webscripts/se/vgregion/inheritance/util.lib.js">

//disable behaviours, if required
if (json.has('disableBehaviours')) {
   serviceUtils.disableAllBehaviours();
}

var debug = [];

var isEmpty = function(ob){
         for(var i in ob){ if(ob.hasOwnProperty(i)){return false;}}
         return true;
}; 



//Checks if target folder has properties that will overwrite, if so returns them
var copy_props = function(folder_ref,props) {
   
   //bail out early if we have nothing to do
   if (!props || isEmpty(props)) {
      debug.push('bailing out');
      return;
   }
   
   //loop over files
   var valid = {};
   var refs = [];
   for (f in props) {
     if (props.hasOwnProperty(f)) {
       refs.push(f);
       for each (p in props[f]) {
          //debug.push('validprop: '+p);
          if (!valid[f]) {
            valid[f] = {};
          }
          valid[f][p] = true;
       }
     }
   }
   debug.push('');
   
   
   var tosave = {};
   debug.push('refs: '+refs)
   forEachProperty(refs,folder_ref,function(node,folder,prop){
      debug.push('prop:'+prop+' node: '+node.name+' valid:'+valid[node.nodeRef][prop]);
      if (valid[node.nodeRef] && valid[node.nodeRef][prop]) {
         //copy prop value
         debug.push('yes')
         tosave[node.nodeRef] = node;
         node.properties[prop] = folder.properties[prop];
         
         //check for key-value pair, we have the value
         if (vals[prop]) {
            tosave[node.nodeRef] = node;
            node.properties[vals[prop]] = folder.properties[vals[prop]];
         
         }
      }
   });
   
   //save properties
   for (var ref in tosave) {
      if (tosave.hasOwnProperty(ref)) {
         debug.push('saving: '+ref);
         tosave[ref].save();
         
         //remove aspect "donottouch"
         if (tosave[ref].hasAspect('{http://www.vgregion.se/model/1.0}donottouch')) {
            tosave[ref].removeAspect('{http://www.vgregion.se/model/1.0}donottouch');
         }
            
      }
   }
};


//java json library really sucks when using it from javascript
var to_js = function(json_array) {
   var l = [];
   for (var i=0;i<json_array.length(); i++) {
      l.push(''+json_array.get(i));
   }
   return l;
};


if (json.has('target') && json.has('props')) {
   var props = {};
   var json_props = json.getJSONObject('props');
   var names = json_props.names();
   for (var i=0;i<names.length(); i++){
      var name = ''+names.getString(i); 
      if (!props[name]) {
        props[name] = [];
      }
    
      props[name] = to_js(json_props.getJSONArray(names.getString(i)));
   }
   
   copy_props(
      ''+json.getString('target'),
      props
   );
} else {
   required_missing('either refs or target');
}
model.debug = jsonUtils.toJSONString(debug);