// @overridden projects/web-framework-commons/source/web/js/forms-runtime.js

/**
 * TODO write some docs
 */
(function(_runValidations) {

   Alfresco.forms.Form.prototype._runValidations = function(event, fieldId, notificationLevel) {
      var valid = _runValidations.call(this, event, fieldId, notificationLevel);
      
      var warningText = Dom.get(this.formId + "-warningtext");

      // if the form is valid, hide the warning text, otherwise show it
      if (valid) {
         Dom.setStyle(warningText, "display", "none");
      } else {
         Dom.setStyle(warningText, "display", "block");
      }
      
      return valid;
   };

}(Alfresco.forms.Form.prototype._runValidations));
