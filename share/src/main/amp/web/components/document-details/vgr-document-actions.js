(function(onActionEditOffline) {

   Alfresco.DocumentActions.prototype.onActionEditOffline = function(asset) {
      var self = this;
      
      if (asset.node.size == 0) {
         Alfresco.util.PopupManager.displayMessage({
            text : this.msg("message.edit-offline.nocontent", asset.displayName)
         });

         return;
      }

      Alfresco.util.PopupManager.displayPrompt({
         title: this.msg("message.edit-offline.observe"),
         text: this.msg("message.edit-offline.observe.information"),
         modal: true,
         buttons: [{
            text: self.msg("button.ok"),
            handler: function () {
               this.destroy();
               
               onActionEditOffline.call(self, asset);
            },
            isDefault: true
         }]
      });
   };

}(Alfresco.DocumentActions.prototype.onActionEditOffline));
