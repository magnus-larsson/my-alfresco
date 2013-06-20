<import resource="classpath:alfresco/templates/webscripts/se/vgregion/inheritance/util.lib.js">
//Checks if target folder has properties that will overwrite, if so returns them
var debug = [];
var intersecting = function(refs,target_ref) {
   
   //loop over and collect intersecting properties
   var objs = {};
   forEachProperty(refs,target_ref,function(f,target,prop){
      
      if (!objs[f.name]) {
         objs[f.name] = { id: f.nodeRef,name: f.name, props: [] };
      }
      var obj = objs[f.name];
      
      //for each intersecting property store name, old and new value
      //TODO: here we might need to do lookups just to get a proper human readable representation
      //we also might need to filter values that are hidden usually, i.e .id values
      //lot's of massaging of data
      
      //only vgr document props
      //check that it is a vgr prop, and that it's set on the folder and that it's not already the same on the document
      //javascript string versions of prop to get expected behaviour out of == etc
      //since we might be dealing with lists etc
      //check that it's not a key value, i.e .id of a pair
      var tp = ''+target.properties[prop];
      var fp = ''+f.properties[prop];
      debug.push(tp);
      debug.push(fp);
      
      if (prop.indexOf('{http://www.vgregion.se/model/1.0}') === 0 &&
          tp != null    && tp !== "null" && 
          tp !== ""     && tp !== "[]"   && 
          tp !== fp     && !keys[prop]) {
             
             obj.props.push({
                  "new":     target.properties[prop],
                  "old":     f.properties[prop],
                  "name":    prop
             });
      }
      
   });
      
   var filtered = [];
   for (var o in objs) {
      if (objs.hasOwnProperty(o) && objs[o].props && objs[o].props.length > 0) {
         filtered.push(objs[o]);
      }
   }
   return filtered;
};


if (json.has('refs') && json.has('target')) {
   //java json library really sucks when using it from javascript
   var refs = [];
   var jarray = json.getJSONArray('refs');
   
   for (var i=0;i<jarray.length(); i++) {
      refs.push(''+jarray.getString(i));
   }
   
   model.intersect = jsonUtils.toJSONString(intersecting(refs,''+json.getString('target')));
} else {
   required_missing('either refs or target');
}
model.debug = jsonUtils.toJSONString(debug);
