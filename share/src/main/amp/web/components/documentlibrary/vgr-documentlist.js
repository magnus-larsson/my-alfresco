/**
 * Change the behaviour of the inline edit for the name, shouldn't edit cm:name, but instead edit vgr:dc.title and also remove the file extension as that is added in a behaviour to cm:name later when
 * the property is replicated.
 */

(function(renderCellDescription) {

   Alfresco.DocumentListViewRenderer.prototype.renderCellDescription = function(scope, elCell, oRecord, oColumn, oData) {
      renderCellDescription.call(this, scope, elCell, oRecord, oColumn, oData);

      for (x = 0; x < scope.insituEditors.length; x++) {
         var editor = scope.insituEditors[x];

         if (editor.params.name !== 'prop_cm_name') {
            continue;
         }

         editor.params.name = "prop_vgr_dc#dot#title";

         editor.params.fnSelect = function fnSelect(elInput, value) {
            // If the file has an extension, omit it from the edit selection
            var extnPos = value.lastIndexOf(Alfresco.util.getFileExtension(value)) - 1;

            if (extnPos > 0) {
               elInput.value = value.substring(0, extnPos);
            }

            elInput.select();
         };
      }
   };

}(Alfresco.DocumentListViewRenderer.prototype.renderCellDescription));
