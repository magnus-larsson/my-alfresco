(function() {

   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Selector = YAHOO.util.Selector;

   var dependencies = [ "button", "container", "json" ];
   
   var options = {
      width : "400px"
   };

   Alfresco.service.DataListActions.prototype.onActionMove = function(items, owner) {

      if (!YAHOO.lang.isArray(items)) {
         items = [ items ];
      }

      var id = this.id;

      // depending on of this action is executed from the toolbar
      // 'this' refers to either Alfresco.component.DataGrid or a toolbar object
      if (this.datalistMeta) {
         // DataGrid
         var dataGrid = this;
      } else {
         // toolbar
         var dataGrid = this.modules.dataGrid;
      }
      var msg = function(msgid) {
         return dataGrid.msg(msgid);
      };

      var itemType = dataGrid.datalistMeta.itemType; // type of the list
      var nodeRef = dataGrid.datalistMeta.nodeRef; // noderef of the list

      var siteId = this.options.siteId;

      // check that modules are loaded
      Alfresco.util.YUILoaderHelper.require(dependencies, function() {
         // do an ajax call to get template
         // Load the UI template from the server
         Alfresco.util.Ajax.request({
            url : Alfresco.constants.URL_SERVICECONTEXT + "modules/data-lists/move",
            dataObj : {
               htmlId : id,
               htmlid : id
            },
            successCallback : {
               fn : function(response) {

                  // Inject the template from the XHR request into a new DIV element
                  var containerDiv = document.createElement("div");
                  containerDiv.setAttribute("style", "display:none");
                  containerDiv.innerHTML = response.serverResponse.responseText;

                  // The panel is created from the HTML returned in the XHR request, not the container
                  var dialogDiv = Dom.getFirstChild(containerDiv);

                  // Create and render the YUI dialog
                  var dialog = Alfresco.util.createYUIPanel(dialogDiv, {
                     width : options.width
                  });

                  // OK/select button
                  var me = this;
                  var okButton = Alfresco.util.createYUIButton(this, "select", function() {
                     // get selected data list
                     var selected = Selector.query("#" + id + "-move-dialog-datalists li.selected")[0];
                     dialog.hide();
                     if (selected) {
                        // show "moving..." dialog
                        var popup = Alfresco.util.PopupManager.displayMessage({
                           text : msg("message.moving"),
                           spanClass : "wait",
                           displayTime : 0
                        // infinite
                        });
                        // remove selected class and extract target nodeRef
                        Dom.removeClass(selected, 'selected');
                        var trgt = selected.className;

                        var srcs = [];
                        for ( var i = 0; i < items.length; i++) {
                           srcs.push(items[i].nodeRef);
                        }

                        // issue a move
                        if (items && trgt) {
                           // do an ajax request to move
                           Alfresco.util.Ajax.request({
                              url : Alfresco.constants.PROXY_URI + "vgr/data-lists/move",
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
                                    dataGrid.onDataItemsDeleted(null, [ null, {
                                       items : items
                                    } ]);
                                 },
                                 scope : me
                              },
                              successMessage : msg("message.moving.success"),
                              failureCallback : {
                                 fn : popup.hide,
                                 scope : popup
                              },
                              failureMessage : msg("message.moving.error"),
                              execScripts : false
                           });
                        } else {
                           popup.hide();
                           Alfresco.util.PopupManager.displayMessage({
                              text : msg("error")
                           });
                        }
                     }
                  });

                  // disable ok button until all is loaded
                  okButton.set("disabled", true);

                  // Cancel button
                  var cancelButton = Alfresco.util.createYUIButton(this, "cancel", function() {
                     dialog.hide();
                  });

                  // show dialog
                  dialog.show();

                  // make an ajax request to load data lists
                  Alfresco.util.Ajax.request({
                     url : Alfresco.constants.PROXY_URI + "/vgr/data-lists/bytype/site/" + siteId + '/' + itemType.substr(3),
                     method : Alfresco.util.Ajax.GET,
                     successCallback : {
                        fn : function(res) {
                           var lists = YAHOO.lang.JSON.parse(res.serverResponse.responseText).result;
                           var ul = Dom.get(id + "-move-dialog-datalists");

                           var html = [];
                           for ( var i = 0; i < lists.length; i++) {
                              if (nodeRef != lists[i].nodeRef) {
                                 html.push('<li class="' + lists[i].nodeRef + '">');
                                 html.push(lists[i].name);
                                 html.push('</li>');
                              }
                           }
                           if (html.length > 0) {
                              ul.innerHTML = html.join("");
                           } else {
                              ul.innerHTML = '<li class="message">' + msg("no.results") + '</li>';
                           }

                           // set up events for selection
                           var lis = Dom.getChildren(ul);
                           Event.addListener(lis, 'click', function(e) {
                              Dom.removeClass(lis, "selected");
                              Dom.addClass(Event.getTarget(e), "selected");

                              // now we can press OK
                              okButton.set("disabled", false);
                           });
                        },
                        scope : this
                     },
                     failureMessage : "Could not load datalists:",
                     execScripts : false
                  });

               },
               scope : this
            },
            failureMessage : "Could not load template",
            execScripts : false
         });
      }, this);
   };

   // load needed YUI modules in background
   Alfresco.util.YUILoaderHelper.require(dependencies, function() {
   }, this);

})();
