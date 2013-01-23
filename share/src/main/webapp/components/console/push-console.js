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
 * Admin Console PuSH Console
 *
 * @namespace Alfresco
 * @class RL.PushConsole
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;

   /**
    * PushConsole constructor.
    *
    * @param {String}
    *           htmlId The HTML id of the parent element
    * @return {RL.PushConsole} The new PushConsole instance
    * @constructor
    */
   RL.PushConsole = function(htmlId) {
      this.name = "RL.PushConsole";
      RL.PushConsole.superclass.constructor.call(this, htmlId);

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

            // Buttons
            parent.widgets.pushButton = Alfresco.util.createYUIButton(parent, "push-button", parent.onPushClick);
         }
      });

      new ListPanelHandler();

      return this;
   };

   YAHOO.extend(RL.PushConsole, Alfresco.ConsoleTool, {

      /**
       * Fired by YUI when parent element is available for scripting. Component initialisation, including instantiation of YUI widgets and event listener binding.
       *
       * @method onReady
       */
      onReady : function ACJC_onReady() {
         // Call super-class onReady() method
         RL.PushConsole.superclass.onReady.call(this);
      },

      onPushClick : function ACJC_onPushClick() {
         var self = this;

         Alfresco.util.Ajax.jsonRequest({
            url : Alfresco.constants.PROXY_URI + "se/vgregion/alfresco/console/push/count.json",
            method : Alfresco.util.Ajax.GET,
            dataObj : {
               "fromDate" : self.widgets.fromDateField.value,
               "fromTime" : self.widgets.fromTimeField.value,
               "toDate" : self.widgets.toDateField.value,
               "toTime" : self.widgets.toTimeField.value
            },
            successCallback : {
               fn : function(res) {
                  var count = res.json.count;
                  self.onPushCount(count);
               },
               scope : this
            },
            failureCallback : {
               fn : function() {
                  Alfresco.util.PopupManager.displayMessage({
                     text : this.msg('push.failure.no.count'),
                     displayTime : 3
                  });
               },
               scope : this
            }
         });
      },

      onPushCount : function ACJC_onPushCount(count) {
         if (count < 1) {
            Alfresco.util.PopupManager.displayMessage({
               text : this.msg('push.no.hits'),
               displayTime : 3
            });

            return;
         }

         var self = this;

         Alfresco.util.PopupManager.displayPrompt({
            title : this.msg('push.question.title'),
            text : this.msg('push.question.text', count),
            model : true,
            buttons : [ {
               text : this.msg("button.push"),
               handler : function ACJC_onPushCount_push() {
                  this.destroy();
                  self.sendPuSH();
               },
               isDefault : true
            }, {
               text : this.msg("button.cancel"),
               handler : function ACJC_onPushCount_cancel() {
                  this.destroy();
               }
            } ]
         });
      },

      sendPuSH : function ACJC_sendPuSH() {
         var self = this;
         
         Alfresco.util.Ajax.jsonRequest({
            url : Alfresco.constants.PROXY_URI + "se/vgregion/alfresco/console/push/send.json",
            method : Alfresco.util.Ajax.POST,
            dataObj : {
               "fromDate" : self.widgets.fromDateField.value,
               "fromTime" : self.widgets.fromTimeField.value,
               "toDate" : self.widgets.toDateField.value,
               "toTime" : self.widgets.toTimeField.value
            },
            successCallback : {
               fn : function(res) {
                  Alfresco.util.PopupManager.displayMessage({
                     text : this.msg('push.success'),
                     displayTime : 3
                  });
               },
               scope : this
            },
            failureCallback : {
               fn : function() {
                  Alfresco.util.PopupManager.displayMessage({
                     text : this.msg('push.failure'),
                     displayTime : 3
                  });
               },
               scope : this
            }
         });
      }

   });
})();
