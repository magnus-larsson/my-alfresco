// @overridden projects/slingshot/source/web/components/search/advsearch.js

(function(onBeforeFormRuntimeInit) {

   Alfresco.AdvancedSearch.prototype.onBeforeFormRuntimeInit = function(layer, args) {
      onBeforeFormRuntimeInit.call(this, layer, args)

      var warning_text = Dom.get(this.currentForm.htmlid + "-form-warningtext");

      if (warning_text) {
         warning_text.parentNode.removeChild(warning_text);
      }
   };

}(Alfresco.AdvancedSearch.prototype.onBeforeFormRuntimeInit));
