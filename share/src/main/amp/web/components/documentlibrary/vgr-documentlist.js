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

(function(onActionEditOffline) {

   Alfresco.DocumentList.prototype.onActionEditOffline = function(record) {
      var self = this;

      if (record.node.size == 0) {
         Alfresco.util.PopupManager.displayMessage({
            text : this.msg("message.edit-offline.nocontent", record.displayName)
         });

         return;
      }

      Alfresco.util.PopupManager.displayPrompt({
         title : this.msg("message.edit-offline.observe"),
         text : this.msg("message.edit-offline.observe.information"),
         modal : true,
         buttons : [ {
            text : self.msg("button.ok"),
            handler : function() {
               this.destroy();

               onActionEditOffline.call(self, record);
            },
            isDefault : true
         } ]
      });
   };

}(Alfresco.DocumentList.prototype.onActionEditOffline));

(function(_resizeRowContainers) {

   Alfresco.DocumentList.prototype._resizeRowContainers = function() {
      var fpanel = Dom.get("alf-filters"), offset = (fpanel ? parseInt(fpanel.style.width, 10) : 160) + 390, width = (Dom.getViewportWidth() - offset) + "px", nodes = YAHOO.util.Selector.query(
               'h3.filename', this.id + "-documents");
      for ( var i = 0; i < nodes.length; i++) {
         if (width == 'NaNpx') {
            width = 'auto';
         }
         
         nodes[i].parentNode.style.width = width;
      }
   };

}(Alfresco.DocumentList.prototype._resizeRowContainers));
