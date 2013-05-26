(function() {

   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Bubbling = YAHOO.Bubbling;

   Alfresco.thirdparty.TreeSelectTooltip = function(htmlId, fieldHtmlId) {
      this.name = "Alfresco.thirdparty.TreeSelectTooltip";
      this.htmlId = htmlId;
      this.id = htmlId;
      this.fieldHtmlId = fieldHtmlId;

      Alfresco.thirdparty.TreeSelectTooltip.superclass.constructor.call(this, "Alfresco.thirdparty.TreeSelectTooltip", htmlId, [ "container" ]);

      return this;
   };

   YAHOO.extend(Alfresco.thirdparty.TreeSelectTooltip, Alfresco.component.Base, {

      /**
       * Object container for initialization options
       * 
       * @property options
       * @type object
       */
      options : {

         /**
          * @property optionSeparator
          * @type string
          * @default "#alf#"
          */
         optionSeparator : "#alf#",

         /**
          * @property tooltipDataLoaderUrl
          * @type string
          * @default ""
          */
         tooltipDataLoaderUrl : ""

      },

      onComponentsLoaded : function() {
         var self = this;

         Event.onDOMReady(function() {
            self.onDOMReady();
         });
      },

      onDOMReady : function() {
         this.initTooltipsFromList();
      },

      // parses the name of a unit to a tree structure of parents to use in tooltips
      _parseTitle : function(name) {
         var pairs = name.split(","), i, j, n, parents = [], result;

         for (i = 0; i < pairs.length; i++) {
            n = pairs[i].split('=')[1];

            if (n != 'VGR' && n != 'OrgExtended') { // filter these common root nodes out
               parents.push(n);
            }
         }

         parents.reverse();

         result = [ '<div style="text-align: left; white-space: nowrap;">' ];

         for (i = 0; i < parents.length; i++) {
            if (i > 0 && i < parents.length) {
               for (j = 0; j < i; j++) {
                  result.push("&nbsp;&nbsp;");
               }
            }

            result.push(parents[i]);

            result.push('<br />');
         }

         result.push('</div>');

         return result.join('');
      },

      // parses the name of a apelon node to a tree structure of parents to use in tooltips
      _parseApelonTitle : function(name) {
         var pairs = name.split("#alf#"), i, j, n, parents = [], result;

         result = [ '<div style="text-align: left; white-space: nowrap;">' ];

         for (i = 0; i < pairs.length; i++) {
            if (i > 0 && i < pairs.length) {
               for (j = 0; j < i; j++) {
                  result.push("&nbsp;&nbsp;");
               }
            }

            result.push(pairs[i]);

            result.push('<br />');
         }

         result.push('</div>');

         return result.join('');
      },

      // hooks up tooltips on li elements
      setupTooltips : function(lis) {
         var selected_tooltips = {}, i, self = this;

         for (i = 0; i < lis.length; i++) {
            var li = lis[i];

            if (selected_tooltips[li.id]) {
               continue;
            }

            var tt = new YAHOO.widget.Tooltip(li.id + '-tooltip', {
               context : li
            });

            tt.contextTriggerEvent.subscribe(function(type, args) {
               var li = args[0];

               if (li.name) {
                  if (self.id.indexOf('dc.subject.keywords') < 0 && self.id.indexOf('dc.type.document.structure') < 0) {
                     var text = self._parseTitle(li.name);
                  } else {
                     var text = self._parseApelonTitle(li.name);
                  }
                  this.cfg.setProperty("text", text);
               }
            });

            selected_tooltips[li.id] = tt;
         }
      },

      // ajax loads info about a unit/units
      // more specifically we get the name to use in a tooltip
      _loadUnits : function(ids, callback) {
         Alfresco.util.Ajax.request({
            url : this.options.tooltipDataLoaderUrl,
            method : Alfresco.util.Ajax.GET,
            dataObj : {
               nodes : (typeof ids == "string" ? ids : ids.join(this.options.optionSeparator))
            },
            responseContentType : Alfresco.util.Ajax.JSON,
            successCallback : {
               fn : function(res) {
                  callback(res.json.values);
               },
               scope : this
            },
            failureCallback : { // TODO: proper error messages
               fn : function(res) {
                  var json = Alfresco.util.parseJSON(res.serverResponse.responseText);
                  Alfresco.util.PopupManager.displayPrompt({
                     title : "Fel",
                     text : "Feldetalj:" + json
                  });
               },
               scope : this
            }
         });
      },

      initTooltipsFromList : function() {
         var ids = [], i;

         var lis = YAHOO.util.Dom.getChildren(this.htmlId);

         var liMap = {};

         var self = this;

         for (i = 0; i < lis.length; i++) {
            var id = lis[i].id;

            if (id.indexOf('//') != -1 && id.indexOf('//') + 2 < id.length) {
               id = id.split('//')[1];
            }

            liMap[id] = lis[i];

            ids.push(id);
         }

         this._loadUnits(ids, function(units) {
            for (i = 0; i < units.length; i++) {
               var li = liMap[units[i].id];
               li.name = units[i].dn;
               self.setupTooltips([ li ]);
            }
         });
      }

   });

})();
