// @overridden projects/slingshot/source/web/components/documentlibrary/documentlist.js

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
