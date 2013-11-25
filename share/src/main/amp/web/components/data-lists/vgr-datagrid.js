/*
 * @overridden projects/slingshot/source/web/components/data-lists/datagrid.js
 */

/**
 * TODO write some docs
 */
(function(_setupDataTable) {

   /**
    * Triggered on cellMousedownEvent from the datatable, starts a drag.
    */
   Alfresco.component.DataGrid.prototype.onRowDragStart = function(ev) {
      var par = this.widgets.dataTable.getTrEl(YAHOO.util.Event.getTarget(ev)), srcData, srcIndex, tmpIndex = null, me = this, ddRow = new YAHOO.util.DDProxy(par.id, this.datalistMeta.itemType);

      ddRow.handleMouseDown(ev.event);

      /**
       * Once we start dragging a row, we make the proxyEl look like the src Element. We get also cache all the data related to the
       * 
       * @return void
       * @static
       * @method startDrag
       */
      ddRow.startDrag = function() {
         proxyEl = this.getDragEl();
         srcEl = this.getEl();
         srcData = me.widgets.dataTable.getRecord(srcEl).getData();
         srcIndex = srcEl.sectionRowIndex;

         // Make the proxy look like the source element
         // Dom.setStyle(srcEl, "visibility", "hidden");
         proxyEl.innerHTML = "<table><tbody>" + srcEl.innerHTML + "</tbody></table>";
      };

      /**
       * On end drag, regardless if it was a succesful drop, remove the proxy
       */
      ddRow.endDrag = function(x, y) {
         Dom.setStyle(proxyEl, "visibility", "hidden");
      };

      ddRow.onDragDrop = function(e, target, group) {
         Dom.removeClass(target, "highlight");

         var popup = Alfresco.util.PopupManager.displayMessage({
            text : me.msg("message.moving"),
            spanClass : "wait",
            displayTime : 0
         // infinite
         });

         // figure out source and target
         var src = Dom.get("checkbox-" + this.getEl().id).value;
         var trgt = Dom.get(target).className;

         var srcs = [];
         srcs.push(src);

         if (src && trgt) {
            // do an ajax request to move
            Alfresco.util.Ajax.request({
               url : me.options.moveUrl,
               method : Alfresco.util.Ajax.POST,
               requestContentType : Alfresco.util.Ajax.JSON,
               responseContentType : Alfresco.util.Ajax.JSON,
               dataObj : {
                  srcs : srcs,
                  target : trgt
               },
               successCallback : {
                  fn : function() {
                     popup.hide();
                     me.onDataItemsDeleted(null, [ null, {
                        items : [ {
                           nodeRef : src
                        } ]
                     } ]);
                  },
                  scope : me
               },
               successMessage : me.msg("message.moving.success"),
               failureCallback : {
                  fn : popup.hide,
                  scope : popup
               },
               failureMessage : me.msg("message.moving.error"),
               execScripts : false
            });
         } else {
            popup.hide();
            Alfresco.util.PopupManager.displayMessage({
               text : me.msg("error")
            });
         }
      };

      // highlight possible targets
      ddRow.onDragEnter = function(e, target) {
         Dom.addClass(target, "highlight");
      };

      ddRow.onDragOut = function(e, target) {
         Dom.removeClass(target, "highlight");
      };
   }

   Alfresco.component.DataGrid.prototype._setupDataTable = function(columns) {
      _setupDataTable.call(this, columns);

      Alfresco.util.YUILoaderHelper.require([ "dragdrop" ], null, this);

      this.widgets.dataTable.getColumn("actions").width = 110;

      this.options.splitActionsAt = 4;

      this.options.moveUrl = Alfresco.constants.PROXY_URI + "vgr/data-lists/move";

      // Enable row drag n drop to other lists of same type
      this.widgets.dataTable.subscribe("cellMousedownEvent", this.onRowDragStart, this, true);
   };

}(Alfresco.component.DataGrid.prototype._setupDataTable));
