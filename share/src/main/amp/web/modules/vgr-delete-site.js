(function(_onDeleteClick) {

   Alfresco.module.DeleteSite.prototype._onDeleteClick = function(config) {
      var me = this, c = config;

      // checking if the site has any published documents may take a while
      var feedbackMessage = Alfresco.util.PopupManager.displayMessage({
         text : Alfresco.util.message("message.checking.published", this.name),
         spanClass : "wait",
         displayTime : 0
      });

      var pub = new Alfresco.thirdparty.PublishToStorage(this.id + "-unpublishFromStorage");
      
      pub.setOptions({
         revoke : true,
         successCallback : function() {
            me._onConfirmedDeleteClick.call(me, c);
         },
         files : [ {
            site : c.site.shortName
         } ]
      });

      var std_dialog = {
         title : Alfresco.util.message("title.deleteSite", this.name),
         text : Alfresco.util.message("label.confirmDeleteSiteWithPublishedMaterial", this.name),
         noEscape : true,
         buttons : [ {
            text : this.msg("button.unpublish.delete"),
            handler : function dlA_onActionDeleteChecked_publish() {
               this.destroy();
               pub.showDialog();
            }
         }, {
            text : Alfresco.util.message("button.just.delete", this.name),
            handler : function DeleteSite__oDC_delete() {
               this.destroy();
               me._onConfirmedDeleteClick.call(me, c);
            }
         }, {
            text : Alfresco.util.message("button.cancel", this.name),
            handler : function DeleteSite__oDC_cancel() {
               me.deletePromptActive = false;
               this.destroy();
            },
            isDefault : true
         } ]
      };

      var dialog = function() {
         feedbackMessage.hide();
         Alfresco.util.PopupManager.displayPrompt(std_dialog);
      };

      var simple_text = Alfresco.util.message("label.confirmDeleteSite", this.name);
      var simple_dialog = function() {
         feedbackMessage.hide();
         std_dialog.buttons.splice(0, 1); // remove pub button
         std_dialog.text = simple_text;
         Alfresco.util.PopupManager.displayPrompt(std_dialog);
      };

      // check if site has published documents
      pub.checkPublishedStatus(dialog, simple_dialog, true);
   };

}(Alfresco.module.DeleteSite.prototype._onDeleteClick));
