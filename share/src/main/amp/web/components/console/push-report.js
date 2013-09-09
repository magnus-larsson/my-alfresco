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
 * Admin Console PuSH Report
 * 
 * @namespace Alfresco
 * @class RL.PushReport
 */
(function() {
  /**
   * YUI Library aliases
   */
  var Dom = YAHOO.util.Dom;

  var $html = Alfresco.util.encodeHTML;

  /**
   * PushReport constructor.
   * 
   * @param {String}
   *          htmlId The HTML id of the parent element
   * @return {RL.PushReport} The new PushReport instance
   * @constructor
   */
  RL.PushReport = function(htmlId) {
    this.name = "RL.PushReport";
    RL.PushReport.superclass.constructor.call(this, htmlId);

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
      onLoad : function onLoad() {
        parent.widgets.fromDateField = Dom.get(parent.id + "-from-date");
        parent.widgets.fromTimeField = Dom.get(parent.id + "-from-time");
        parent.widgets.toDateField = Dom.get(parent.id + "-to-date");
        parent.widgets.toTimeField = Dom.get(parent.id + "-to-time");
        parent.widgets.publishStatusField = Dom.get(parent.id + "-publishstatus");
        parent.widgets.unpublishStatusField = Dom.get(parent.id + "-unpublishstatus");
        // Buttons
        parent.widgets.pushButton = Alfresco.util.createYUIButton(parent, "report-button", parent.onReportClick);

        // DataSource
        parent.widgets.dataSource = new YAHOO.util.DataSource(Alfresco.constants.PROXY_URI + "vgr/publishreport", {
          responseType : YAHOO.util.DataSource.TYPE_JSON,
          responseSchema : {
            resultsList : "documents",
            metaFields : {
              recordOffset : "startIndex",
              totalRecords : "totalRecords"
            }
          }
        });

        // Setup the main datatable
        this._setupDataTable();
      },

      _setupDataTable : function() {

        var localThis = this;

        var renderCellTitle = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };

        var renderCellId = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };

        var renderCellVersion = function(cell, record, column, data) {
          cell.innerHTML = $html(data);
        };

        var renderCellSourceId = function(cell, record, column, data) {
          cell.innerHTML = $html(data);

        };

        var renderCellPushedForPublish = function(cell, record, column, data) {
          cell.innerHTML = $html(data);

        };

        var renderCellPushedForUnpublish = function(cell, record, column, data) {
          cell.innerHTML = $html(data);

        };

        var renderCellPublishStatus = function(cell, record, column, data) {
          cell.innerHTML = $html(data);

        };

        var renderCellUnpublishStatus = function(cell, record, column, data) {
          cell.innerHTML = $html(data);

        };

        var pushButtonCounter = 0;

        var renderCellActions = function(cell, record, column, data) {
          var nodeRef = record._oData.id;
          function onPushButtonClick(p_oEvent) {

            Alfresco.util.Ajax.jsonPost({
              url : Alfresco.constants.PROXY_URI + "vgr/repush?documentId=" + nodeRef,
              successCallback : {
                fn : function(res) {
                  Alfresco.util.PopupManager.displayMessage({
                    displayTime : 2,
                    text : $html(parent.msg("label.repushBtn.success")),
                    noEscape : false
                  });
                },
                scope : this
              },
              failureCallback : {
                fn : function(res) {
                  Alfresco.util.PopupManager.displayMessage({
                    displayTime : 3,
                    text : $html(parent.msg("label.repushBtn.fail")),
                    noEscape : false
                  });
                },
                scope : this
              }
            });
          }
          
          function onSentinelButtonClick(p_oEvent) {
            var url = sentinelUrl.replace("#placeholder#", nodeRef);
            var win=window.open(url, '_blank');
            win.focus();
            
          }

          cell.innerHTML = '<div id="actionbuttons' + (pushButtonCounter) + '-container"></div>';
          var oPushButton = new YAHOO.widget.Button({
            label : parent.msg("label.repushBtn.title"),
            id : "pushbutton" + pushButtonCounter,
            container : "actionbuttons" + (pushButtonCounter) + "-container"
          });
          var oSentinelButton = new YAHOO.widget.Button({
            label : parent.msg("label.sentinelBtn.title"),
            id : "sentinelbutton" + pushButtonCounter,
            container : "actionbuttons" + (pushButtonCounter) + "-container"
          });
          pushButtonCounter = pushButtonCounter + 1;
          oPushButton.on("click", onPushButtonClick);
          oSentinelButton.on("click", onSentinelButtonClick);
        };

        var columnDefinitions = [ {
          key : "title",
          label : parent.msg("label.title"),
          sortable : false,
          formatter : renderCellTitle,
          width : 110
        }, {
          key : "version",
          label : parent.msg("label.version"),
          sortable : false,
          formatter : renderCellVersion
        }, {
          key : "id",
          label : parent.msg("label.id"),
          sortable : false,
          formatter : renderCellId
        }, {
          key : "sourceId",
          label : parent.msg("label.sourceId"),
          sortable : false,
          formatter : renderCellSourceId
        }, {
          key : "pushedForPublish",
          label : parent.msg("label.pushedForPublish"),
          sortable : false,
          formatter : renderCellPushedForPublish
        }, {
          key : "publishStatus",
          label : parent.msg("label.publishStatus"),
          sortable : false,
          formatter : renderCellPublishStatus
        }, {
          key : "pushedForUnpublish",
          label : parent.msg("label.pushedForUnpublish"),
          sortable : false,
          formatter : renderCellPushedForUnpublish
        }, {
          key : "unpublishStatus",
          label : parent.msg("label.unpublishStatus"),
          sortable : false,
          formatter : renderCellUnpublishStatus
        }, {
          key : "actions",
          label : parent.msg("label.actions"),
          sortable : false,
          formatter : renderCellActions
        } ];

        // Customize request sent to server to be able to set total # of
        // records
        var generateRequest = function(oState, oSelf) {
          // Get states or use defaults
          oState = oState || {
            pagination : null,
            sortedBy : null
          };
          var startIndex = (oState.pagination) ? oState.pagination.recordOffset : 0;
          var results = (oState.pagination) ? oState.pagination.rowsPerPage : 1000000;

          var publishStatus = parent.widgets.publishStatusField.value;
          var unpublishStatus = parent.widgets.unpublishStatusField.value;
          var from = parent.widgets.fromDateField.value + "T" + parent.widgets.fromTimeField.value + ":00";
          var to = parent.widgets.toDateField.value + "T" + parent.widgets.toTimeField.value + ":00";
          // Build custom request
          return "?publishstatus=" + publishStatus + "&unpublishstatus=" + unpublishStatus + "&from=" + from + "&to=" + to;
        };

        parent.widgets.dataTable = new YAHOO.widget.DataTable(parent.id + "-result", columnDefinitions, parent.widgets.dataSource, {
          sortedBy : {
            key : "title",
            dir : "asc"
          },
          MSG_EMPTY : parent.msg("message.empty"),
          dynamicData : true,
          generateRequest : generateRequest,
          initialRequest : generateRequest()
        });

        parent.widgets.dataTable.doBeforeLoadData = function(request, response, payload) {
          payload.totalRecords = response.meta.totalRecords;

          return payload;
        }

      }
    });

    new ListPanelHandler();

    return this;
  };

  YAHOO.extend(RL.PushReport, Alfresco.ConsoleTool, {

    /**
     * Fired by YUI when parent element is available for scripting. Component
     * initialisation, including instantiation of YUI widgets and event listener
     * binding.
     * 
     * @method onReady
     */
    onReady : function PR_onReady() {
      // Call super-class onReady() method
      RL.PushReport.superclass.onReady.call(this);
    },

    onReportClick : function PR_onReportClick() {
      this.doUpdate();
    },

    doUpdate : function PR_Update() {
      // Reset the custom error messages
      this._setDefaultDataTableErrors(this.widgets.dataTable);

      // Display loading message
      this.widgets.dataTable.set("MSG_EMPTY", Alfresco.util.message("message.empty", "RL.PushReport"));

      // empty results table
      this.widgets.dataTable.deleteRows(0, this.widgets.dataTable.getRecordSet().getLength());

      // loading message function
      var loadingMessage = null;
      var fnShowLoadingMessage = function() {
        loadingMessage = Alfresco.util.PopupManager.displayMessage({
          displayTime : 0,
          text : '<span class="wait">' + $html(this.msg("message.loading")) + '</span>',
          noEscape : true
        });
      };

      // slow data webscript message
      var timerShowLoadingMessage = YAHOO.lang.later(2000, this, fnShowLoadingMessage);

      var successHandler = function(request, response, payload) {
        if (timerShowLoadingMessage) {
          timerShowLoadingMessage.cancel();
        }

        if (loadingMessage) {
          loadingMessage.destroy();
        }

        this.widgets.dataTable.onDataReturnInitializeTable.call(this.widgets.dataTable, request, response, payload);
      };

      var failureHandler = function(request, response) {
        if (timerShowLoadingMessage) {
          timerShowLoadingMessage.cancel();
        }

        if (loadingMessage) {
          loadingMessage.destroy();
        }

        if (response.status == 401) {
          // Our session has likely timed-out, so refresh to offer the login
          // page
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
      var publishStatus = this.widgets.publishStatusField.value;
      var unpublishStatus = this.widgets.unpublishStatusField.value;
      var from = this.widgets.fromDateField.value + "T" + this.widgets.fromTimeField.value + ":00";
      var to = this.widgets.toDateField.value + "T" + this.widgets.toTimeField.value + ":00";
      this.widgets.dataSource.sendRequest("?publishstatus=" + publishStatus + "&unpublishstatus=" + unpublishStatus + "&from=" + from + "&to=" + to, {
        success : successHandler,
        failure : failureHandler,
        scope : this,
        argument : this.widgets.dataTable.getState()
      });
    },

    /**
     * Resets the YUI DataTable errors to our custom messages
     * 
     * NOTE: Scope could be YAHOO.widget.DataTable, so can't use "this"
     * 
     * @method _setDefaultDataTableErrors
     * @param dataTable
     *          {object} Instance of the DataTable
     */
    _setDefaultDataTableErrors : function(dataTable) {
      var msg = Alfresco.util.message;
      dataTable.set("MSG_EMPTY", msg("message.empty", "RL.PushReport"));
      dataTable.set("MSG_ERROR", msg("message.error", "RL.PushReport"));
    }

  });
})();
