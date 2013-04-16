/**
 * RL root namespace.
 *
 * @namespace RL
 */
// Ensure RL root object exists
if (typeof RL == "undefined" || !RL) {
   var RL = {};
}

/**
 * Admin Console PDF/A Console
 *
 * @namespace Alfresco
 * @class RL.PdfaConsole
 */
(function () {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;

   var $html = Alfresco.util.encodeHTML;

   var Bubbling = YAHOO.Bubbling;

   /**
    * PdfaConsole constructor.
    *
    * @param {String}
      *           htmlId The HTML id of the parent element
    * @return {RL.PdfaConsole} The new PdfaConsole instance
    * @constructor
    */
   RL.PdfaConsole = function (htmlId) {
      this.name = "RL.PdfaConsole";

      RL.PdfaConsole.superclass.constructor.call(this, htmlId);

      /* Register this component */
      Alfresco.util.ComponentManager.register(this);

      /* Load YUI Components */
      Alfresco.util.YUILoaderHelper.require([ "button", "container", "datasource", "datatable", "paginator", "json", "history" ], this.onComponentsLoaded, this);

      /* Define panel handlers */
      var parent = this;

      /* File List Panel Handler */
      ListPanelHandler = function ListPanelHandler_constructor() {
         ListPanelHandler.superclass.constructor.call(this, "main");
      };

      YAHOO.extend(ListPanelHandler, Alfresco.ConsolePanelHandler, {
         /**
          * Called by the ConsolePanelHandler when this panel shall be loaded
          *
          * @method onLoad
          */
         onLoad: function onLoad() {
            parent.widgets.missing_count = Dom.get(parent.id + "-pdfa-missing-count");

            // Buttons
            parent.widgets.pdfaButton = Alfresco.util.createYUIButton(parent, "pdfa-button", parent.onPdfaClick);
            parent.widgets.refreshButton = Alfresco.util.createYUIButton(parent, "refresh-button", parent.onRefreshClick);

            // DataTable and DataSource setup
            parent.widgets.dataSource = new YAHOO.util.DataSource(Alfresco.constants.PROXY_URI + "se/vgregion/alfresco/console/pdfa/missing.json", {
               responseType: YAHOO.util.DataSource.TYPE_JSON,
               responseSchema: {
                  resultsList: "documents",
                  metaFields: {
                     recordOffset: "startIndex",
                     totalRecords: "totalRecords"
                  }
               }
            });

            // Setup the main datatable
            this._setupDataTable();
         },

         _setupDataTable: function () {
            var renderCellName = function (cell, record, column, data) {
               cell.innerHTML = $html(data);
            };

            var renderCellFileName = function (cell, record, column, data) {
               cell.innerHTML = $html(data);
            };

            var renderCellSourcePath = function (cell, record, column, data) {
               cell.innerHTML = $html(data);
            };

            var renderCellStoragePath = function (cell, record, column, data) {
               cell.innerHTML = $html(data);
            };

            var renderCellActions = function (cell, record, column, data) {
               cell.innerHTML = '<div class="onActionCreate" style="display: block;"><a href="" class="action-link" title="Skapa PDF/A rendrering"><span>Skapa PDF/A rendrering</span></a></div>';
            };

            var columnDefinitions = [
               { key: "name", label: parent._msg("label.name"), sortable: false, formatter: renderCellName, width: 70 },
               { key: "filename", label: parent._msg("label.filename"), sortable: false, formatter: renderCellFileName },
               { key: "sourcePath", label: parent._msg("label.source_path"), sortable: false, formatter: renderCellSourcePath },
               { key: "storagePath", label: parent._msg("label.storage_path"), sortable: false, formatter: renderCellStoragePath },
               { key: "actions", label: parent._msg("label.column.actions"), sortable: false, formatter: renderCellActions, width: 110 }
            ];

            // Customize request sent to server to be able to set total # of records 
            var generateRequest = function (oState, oSelf) {
               // Get states or use defaults 
               oState = oState || { pagination: null, sortedBy: null };
               var startIndex = (oState.pagination) ? oState.pagination.recordOffset : 0;
               var results = (oState.pagination) ? oState.pagination.rowsPerPage : 25;

               // Build custom request 
               return "?skip=" + startIndex + "&max=" + results;
            };

            parent.widgets.dataTable = new YAHOO.widget.DataTable(parent.id + "-pdfa-missing-list", columnDefinitions, parent.widgets.dataSource, {
               sortedBy: {
                  key: "name",
                  dir: "asc"
               },
               MSG_EMPTY: parent._msg("message.empty"),
               dynamicData: true,
               generateRequest: generateRequest,
               initialRequest: generateRequest(),
               paginator: new YAHOO.widget.Paginator({
                  rowsPerPage: 25
               })
            });

            parent.widgets.dataTable.doBeforeLoadData = function (request, response, payload) {
               payload.totalRecords = response.meta.totalRecords;
               payload.pagination.recordOffset = response.meta.recordOffset;
               return payload;
            }
         }
      });

      new ListPanelHandler();

      return this;
   };

   YAHOO.extend(RL.PdfaConsole, Alfresco.ConsoleTool, {

      /**
       * Fired by YUI when parent element is available for scripting. Component initialisation, including instantiation of YUI widgets and event listener binding.
       *
       * @method onReady
       */
      onReady: function () {
         var self = this;

         // Call super-class onReady() method
         RL.PdfaConsole.superclass.onReady.call(this);

         // Hook action events
         var fnActionHandler = function (layer, args) {
            var owner = Bubbling.getOwnerByTagName(args[1].anchor, "div");
            if (owner !== null) {
               if (typeof self[owner.className] == "function") {
                  args[1].stop = true;
                  var asset = self.widgets.dataTable.getRecord(args[1].target.offsetParent).getData();
                  self[owner.className].call(self, asset, owner);
               }
            }
            return true;
         };

         Bubbling.addDefaultAction("action-link", fnActionHandler);
      },

      onActionCreate: function(asset) {
        this.onPdfaClick(asset.nodeRef);
      },

      onPdfaClick: function (nodeRef) {
         if (typeof nodeRef != 'string') {
            nodeRef = '';
         }

         var self = this;

         Alfresco.util.Ajax.jsonRequest({
            url: Alfresco.constants.PROXY_URI + "se/vgregion/alfresco/console/pdfa/generate.json",
            dataObj: {
               nodeRef: (nodeRef || nodeRef.length > 0) ? nodeRef : ''
            },
            method: Alfresco.util.Ajax.POST,
            successCallback: {
               fn: function (res) {
                  var count = res.json.count;

                  Alfresco.util.PopupManager.displayMessage({
                     text: self.msg('pdfa.success', count),
                     displayTime: 3
                  });
               },
               scope: this
            },
            failureCallback: {
               fn: function () {
                  Alfresco.util.PopupManager.displayMessage({
                     text: self.msg('pdfa.failure'),
                     displayTime: 3
                  });
               },
               scope: this
            }
         });
      },

      onRefreshClick: function () {
         this.doSearch();
      },

      /**
       * Gets a custom message
       *
       * @method _msg
       * @param messageId {string} The messageId to retrieve
       * @return {string} The custom message
       * @private
       */
      _msg: function (messageId) {
         return Alfresco.util.message.call(this, messageId, "RL.PdfaConsole", Array.prototype.slice.call(arguments).slice(1));
      },

      doSearch: function () {
         // Reset the custom error messages
         this._setDefaultDataTableErrors(this.widgets.dataTable);

         // Display loading message
         this.widgets.dataTable.set("MSG_EMPTY", Alfresco.util.message("pdfa-console.searching", "RL.PdfaConsole"));

         // empty results table
         this.widgets.dataTable.deleteRows(0, this.widgets.dataTable.getRecordSet().getLength());

         // loading message function
         var loadingMessage = null;
         var fnShowLoadingMessage = function () {
            loadingMessage = Alfresco.util.PopupManager.displayMessage({
               displayTime: 0,
               text: '<span class="wait">' + $html(this.msg("message.loading")) + '</span>',
               noEscape: true
            });
         };

         // slow data webscript message
         var timerShowLoadingMessage = YAHOO.lang.later(2000, this, fnShowLoadingMessage);

         var successHandler = function (request, response, payload) {
            if (timerShowLoadingMessage) {
               timerShowLoadingMessage.cancel();
            }

            if (loadingMessage) {
               loadingMessage.destroy();
            }

            this.widgets.dataTable.onDataReturnInitializeTable.call(this.widgets.dataTable, request, response, payload);
         };

         var failureHandler = function (request, response) {
            if (timerShowLoadingMessage) {
               timerShowLoadingMessage.cancel();
            }

            if (loadingMessage) {
               loadingMessage.destroy();
            }

            if (response.status == 401) {
               // Our session has likely timed-out, so refresh to offer the login page
               window.location.reload();
            } else {
               try {
                  var response = YAHOO.lang.JSON.parse(response.responseText);
                  this.widgets.dataTable.set("MSG_ERROR", response.message);
                  this.widgets.dataTable.showTableMessage(response.message, YAHOO.widget.DataTable.CLASS_ERROR);
               } catch (e) {
                  this._setDefaultDataTableErrors(this.widgets.dataTable);
               }
            }
         };

         this.widgets.dataSource.sendRequest('?skip=0&max=25', {
            success: successHandler,
            failure: failureHandler,
            scope: this
         });
      },

      /**
       * Resets the YUI DataTable errors to our custom messages
       *
       * NOTE: Scope could be YAHOO.widget.DataTable, so can't use "this"
       *
       * @method _setDefaultDataTableErrors
       * @param dataTable {object} Instance of the DataTable
       */
      _setDefaultDataTableErrors: function (dataTable) {
         var msg = Alfresco.util.message;
         dataTable.set("MSG_EMPTY", msg("message.empty", "RL.PdfaConsole"));
         dataTable.set("MSG_ERROR", msg("message.error", "RL.PdfaConsole"));
      }

   });
})();
