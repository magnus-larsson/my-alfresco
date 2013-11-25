// @overridden projects/slingshot/source/web/components/search/search.js

(function(onReady) {

   Alfresco.Search.prototype.onReady = function() {
      onReady.call(this)

      var self = this;

      var renderCellDescription = this.widgets.dataTable.getColumn('summary').formatter;

      this.widgets.dataTable.getColumn('summary').formatter = function(elCell, oRecord, oColumn, oData) {
         renderCellDescription.call(this, elCell, oRecord, oColumn, oData);

         var type = oRecord.getData("type");
         var site = oRecord.getData("site");

         var desc = "";

         if (type === 'document') {
            if (!site) {
               var permissions = oRecord.getData('permissions');
               var noSourceAndPublished = permissions['no-source-and-published'] ? self.msg('label.yes') : self.msg('label.no');
               var unknownSourceAndPublished = permissions['unknown-source-and-published'] ? self.msg('label.yes') : self.msg('label.no');
               var linkId = oRecord.getData().nodeRef + '-unpublish';
               var origin = oRecord.getData().dc_source_origin;

               if (origin === 'Alfresco') {
                  if (permissions['no-source-and-published'] === true) {
                     desc += '<div class="details">' + self.msg("message.no-source-and-published") + ': <a href="javascript:void(0);" id="' + linkId + '">' + noSourceAndPublished + '</a></div>';

                     YAHOO.util.Event.onAvailable(linkId, function(record) {
                        var args = {
                           record : record,
                           scope : self
                        };
                        YAHOO.util.Event.addListener(linkId, "click", self.onUnpublish, args);
                     }, oRecord);
                  } else {
                     desc += '<div class="details">' + self.msg("message.no-source-and-published") + ': ' + noSourceAndPublished + '</div>';
                  }
               } else {
                  if (permissions['unknown-source-and-published'] === true) {
                     desc += '<div class="details">' + self.msg("message.unknown-source-and-published", origin) + ': <a href="javascript:void(0);" id="' + linkId + '">' + unknownSourceAndPublished
                              + '</a></div>';

                     YAHOO.util.Event.onAvailable(linkId, function(record) {
                        var args = {
                           record : record,
                           scope : self
                        };
                        YAHOO.util.Event.addListener(linkId, "click", self.onUnpublish, args);
                     }, oRecord);
                  } else {
                     desc += '<div class="details">' + self.msg("message.unknown-source-and-published", origin) + ': ' + unknownSourceAndPublished + '</div>';
                  }
               }
            }
         }

         elCell.innerHTML += desc;
      };
   };

   Alfresco.Search.prototype.onUnpublish = function(e, args) {
      var record = args.record;
      var self = args.scope;

      Alfresco.util.PopupManager.displayPrompt({
         title : self.msg("message.confirm.unpublish.title"),
         text : self.msg("message.confirm.unpublish"),
         buttons : [ {
            text : self.msg("button.unpublish"),
            handler : function() {
               this.destroy();
               self._unpublishOrphanDocument(record, self);
            }
         }, {
            text : self.msg("button.cancel"),
            handler : function() {
               this.destroy();
            },
            isDefault : true
         } ]
      });
   };

   Alfresco.Search.prototype._unpublishOrphanDocument = function(record, scope) {
      Alfresco.util.Ajax.jsonRequest({
         url : Alfresco.constants.PROXY_URI + "vgr/unpublishorphan",
         method : Alfresco.util.Ajax.POST,
         dataObj : {
            "nodeRef" : record.getData().nodeRef
         },
         successCallback : {
            fn : function(res) {
               Alfresco.util.PopupManager.displayMessage({
                  text : scope.msg('unpublish.success'),
                  displayTime : 3
               });

               var anchor = Dom.get(record.getData().nodeRef + "-unpublish");

               anchor.outerHTML = scope.msg('label.no');

               YAHOO.util.Event.removeListener(anchor, 'click');
            },
            scope : scope
         },
         failureCallback : {
            fn : function() {
               Alfresco.util.PopupManager.displayMessage({
                  text : this.msg('unpublish.failure'),
                  displayTime : 3
               });
            },
            scope : scope
         }
      });
   };

}(Alfresco.Search.prototype.onReady));
