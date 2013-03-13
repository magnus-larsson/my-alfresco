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
 * Admin Console Site Statistics Console
 *
 * @namespace Alfresco
 * @class RL.SiteStatisticsConsole
 */
(function () {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;

   var $html = Alfresco.util.encodeHTML;

   var Bubbling = YAHOO.Bubbling;

   /**
    * SiteStatisticsConsole constructor.
    *
    * @param {String}
    *           htmlId The HTML id of the parent element
    * @return {RL.SiteStatisticsConsole} The new SiteStatisticsConsole instance
    * @constructor
    */
   RL.SiteStatisticsConsole = function (htmlId) {
      this.name = "RL.SiteStatisticsConsole";

      RL.SiteStatisticsConsole.superclass.constructor.call(this, htmlId);

      /* Register this component */
      Alfresco.util.ComponentManager.register(this);

      /* Load YUI Components */
      Alfresco.util.YUILoaderHelper.require([ "button", "container", "datasource", "datatable", "paginator", "json", "history" ], this.onComponentsLoaded, this);

      /* Define panel handlers */
      var parent = this;
      
      var queuedJsonRequests = [];
      var numOfConcurrentRequests = 6;
      var runningRequests = 0;

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
            // Buttons
            parent.widgets.refreshButton = Alfresco.util.createYUIButton(parent, "refresh-button-sites", parent.onRefreshClick);

            // DataTable and DataSource setup
            parent.widgets.dataSource = new YAHOO.util.DataSource(Alfresco.constants.PROXY_URI + "vgr/reports/sites", {
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
         
         _fireJsonRequest: function () {
            var jsonRequest = queuedJsonRequests.shift();
 			Alfresco.util.Ajax.jsonGet(jsonRequest);
 			runningRequests++;
         },
         
         _handleJsonResponse: function() {
        	 if (queuedJsonRequests.length>0 && runningRequests <=0) {
         		for (var i=runningRequests;i<numOfConcurrentRequests;i++) {
         			this._fireJsonRequest();
         		}
         	}
         },

         _setupDataTable: function () {
        	 
        	var localThis = this;
        	 
            var renderCellTitle = function (cell, record, column, data) {
               cell.innerHTML = $html(data);
            };

            var renderCellShortName = function (cell, record, column, data) {
               cell.innerHTML = $html(data);
            };

            var renderCellSize = function (cell, record, column, data) {
               var shortName = record.getData('shortName');
               queuedJsonRequests.push({
                   url : Alfresco.constants.PROXY_URI + "vgr/reports/siteSize?shortName=" + shortName,
                   successCallback : {
                      fn : function(res) {
                    	  runningRequests--;
                    	  localThis._handleJsonResponse();
                    	  cell.innerHTML = $html(Math.round(res.json.siteSize / 1024 / 1024));
                      },
                      scope : this
                   },
                   failureCallback : {
                       fn : function(res) {
                    	   cell.innerHTML = "Failed to get data";
                    	   runningRequests--;
                    	   localThis._handleJsonResponse();
                       },
                       scope : this
                    }
                });
            };

            var renderCellMembers = function (cell, record, column, data) {
               var shortName = record.getData('shortName');
               queuedJsonRequests.push({
            	   url : Alfresco.constants.PROXY_URI + "vgr/reports/numberOfSiteMembers?shortName=" + shortName,
                   successCallback : {
                      fn : function(res) {
                    	  runningRequests--;
                    	  localThis._handleJsonResponse();
                    	  cell.innerHTML = $html(res.json.numberOfSiteMembers);
                      },
                      scope : this
                   },
                   failureCallback : {
                       fn : function(res) {
                    	   cell.innerHTML = "Failed to get data";
                    	   runningRequests--;
                    	   localThis._handleJsonResponse();
                       },
                       scope : this
                    }
                });
            };

            var renderCellLastActivity = function (cell, record, column, data) {
               var shortName = record.getData('shortName');
               queuedJsonRequests.push({
            	   url : Alfresco.constants.PROXY_URI + "vgr/reports/lastActivityOnSite?shortName=" + shortName,
                   successCallback : {
                      fn : function(res) {
                    	  runningRequests--;
                    	  localThis._handleJsonResponse();
                    	  var lastActivityOnSite = res.json.lastActivityOnSite;

                          if (lastActivityOnSite == "") {
                             lastActivityOnSite = Alfresco.util.message("statistics-console.noactivity", "RL.SiteStatisticsConsole");
                          }

                          cell.innerHTML = $html(lastActivityOnSite);
                      },
                      scope : this
                   },
                   failureCallback : {
                       fn : function(res) {
                    	   cell.innerHTML = "Failed to get data";
                    	   runningRequests--;
                    	   localThis._handleJsonResponse();
                       },
                       scope : this
                    }
                });
            };

            var columnDefinitions = [
               { key: "title", label: parent._msg("label.siteTitle"), sortable: false, formatter: renderCellTitle, width: 110 },
               { key: "shortName", label: parent._msg("label.shortName"), sortable: false, formatter: renderCellShortName },
               { key: "size", label: parent._msg("label.size"), sortable: false, formatter: renderCellSize },
               { key: "members", label: parent._msg("label.members"), sortable: false, formatter: renderCellMembers },
               { key: "lastActivity", label: parent._msg("label.lastActivity"), sortable: false, formatter: renderCellLastActivity }
            ];

            // Customize request sent to server to be able to set total # of records 
            var generateRequest = function (oState, oSelf) {
               // Get states or use defaults 
               oState = oState || { pagination: null, sortedBy: null };
               var startIndex = (oState.pagination) ? oState.pagination.recordOffset : 0;
               var results = (oState.pagination) ? oState.pagination.rowsPerPage : 1000000;

               // Build custom request 
               return "?skip=" + startIndex + "&max=" + results;
            };

            parent.widgets.dataTable = new YAHOO.widget.DataTable(parent.id + "-statistics-sites-list", columnDefinitions, parent.widgets.dataSource, {
               sortedBy: {
                  key: "title",
                  dir: "asc"
               },
               MSG_EMPTY: parent._msg("message.empty"),
               dynamicData: true,
               generateRequest: generateRequest,
               initialRequest: generateRequest()
            });
            
            parent.widgets.dataTable.subscribe("postRenderEvent", function (o) {
            	localThis._handleJsonResponse();
            });

            parent.widgets.dataTable.doBeforeLoadData = function (request, response, payload) {
               payload.totalRecords = response.meta.totalRecords;
               //payload.pagination.recordOffset = response.meta.recordOffset;
               return payload;
            }
            
            
         }
      });

      new ListPanelHandler();

      return this;
   };

   YAHOO.extend(RL.SiteStatisticsConsole, Alfresco.ConsoleTool, {

      /**
       * Fired by YUI when parent element is available for scripting. Component initialisation, including instantiation of YUI widgets and event listener binding.
       *
       * @method onReady
       */
      onReady: function () {
         var self = this;

         // Call super-class onReady() method
         RL.SiteStatisticsConsole.superclass.onReady.call(this);

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
         return Alfresco.util.message.call(this, messageId, "RL.SiteStatisticsConsole", Array.prototype.slice.call(arguments).slice(1));
      },

      doSearch: function () {
         // Reset the custom error messages
         this._setDefaultDataTableErrors(this.widgets.dataTable);

         // Display loading message
         this.widgets.dataTable.set("MSG_EMPTY", Alfresco.util.message("statistics-console.searching", "RL.SiteStatisticsConsole"));

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

         this.widgets.dataSource.sendRequest('?skip=0&max=1000000', {
            success: successHandler,
            failure: failureHandler,
            scope: this,
            argument: this.widgets.dataTable.getState()
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
         dataTable.set("MSG_EMPTY", msg("message.empty", "RL.SiteStatisticsConsole"));
         dataTable.set("MSG_ERROR", msg("message.error", "RL.SiteStatisticsConsole"));
      }

   });
})();
