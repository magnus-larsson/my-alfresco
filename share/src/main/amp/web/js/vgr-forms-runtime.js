/**
 * TODO write some docs
 */
(function(_runValidations) {

   Alfresco.forms.Form.prototype._runValidations = function(silent) {
      var valid = _runValidations.call(this, silent);
      
      var warningText = Dom.get(this.formId + "-warningtext");

      // if the form is valid, hide the warning text, otherwise show it
      if (valid) {
         Dom.setStyle(warningText, "visibility", "hidden");
      } else {
         Dom.setStyle(warningText, "visibility", "visible");
      }
      
      return valid;
   };

}(Alfresco.forms.Form.prototype._runValidations));
