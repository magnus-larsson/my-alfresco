(function() {

   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Bubbling = YAHOO.Bubbling;

   /**
    * Alfresco.thirdparty.TreeSelect constructor.
    * 
    * @param {string}
    *           htmlId
    * @param {list}
    *           selected, already selected values
    * @param {function}
    *           data_loader, a function that loads dynamic data, takes a node.id as parent and a callback which takes a list of objects with least id and label attributes
    * @return {Alfresco.thirdparty.TreeSelect} The new TreeSelect self
    * @constructor
    */
   Alfresco.thirdparty.TreeSelect = function(htmlId, fieldHtmlId, dataLoaderUrl) {
      Alfresco.thirdparty.TreeSelect.superclass.constructor.call(this, "Alfresco.thirdparty.TreeSelect", htmlId, [ "button" ]);

      this.name = "Alfresco.thirdparty.TreeSelect";
      this.htmlId = htmlId;
      this.id = htmlId;
      // array holding the values for the list
      this.selected = [];
      // URL to load the data from
      this.dataLoaderUrl = dataLoaderUrl;
      this.fieldHtmlId = fieldHtmlId;
      var self = this;

      this.widgets = {
         treeview : null,
         selectedList : null,
         addButton : null,
         removeButton : null,
         clearAllButton : null,
         tooltip : null,
         fetchKeywordsButton : null
      };

      Bubbling.on('tree-select.remove', this.onValueChanged, this);
      Bubbling.on('tree-select.clear', this.onValueChanged, this);

      Bubbling.on('tree-select.add', function(name, added) {
         self.onValueChanged();
         self.widgets.tooltip.setupTooltips(added[1]);
      });

      Bubbling.on('tree-select.ready', this.onTreeReady, this);

      return this;
   };

   YAHOO.extend(Alfresco.thirdparty.TreeSelect, Alfresco.component.Base, {

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
          * @property multipleSelectMode
          * @type boolean
          * @default true
          */
         multipleSelectMode : true,

         /**
          * The number of levels that are selectable. 0 means all levels
          * 
          * @property selectableLevels
          * @type integer
          * @default 0
          */
         selectableLevels : 0,

         /**
          * @property disabled
          * @type boolean
          * @default false
          */
         disabled : false,

         /**
          * @property mandatory
          * @type boolean
          * @default false
          */
         mandatory : false,

         /**
          * @property viewmode
          * @type boolean
          * @default true
          */
         viewmode : true,

         /**
          * @property values
          * @type string
          * @default ""
          */
         values : "",

         /**
          * The default value to set if no saved value is found.
          * 
          * @property dflt
          * @type string
          * @default ""
          */
         dflt : "",

         /**
          * @property version
          * @type float
          * @default null
          */
         version : null,

         /**
          * @property onlySelectLeaf
          * @type boolean
          * @default false
          */
         onlySelectLeaf : false,

         /**
          * @property fetchKeywordsUrl
          * @type string
          * @default ""
          */
         fetchKeywordsUrl : Alfresco.constants.PROXY_URI_RELATIVE + 'vgr/apelon/keywords',

         /**
          * @property nodeRef
          * @type string
          * @default ""
          */
         nodeRef : ""

      },

      onDOMReady : function() {
         var self = this;

         Alfresco.util.Ajax.jsonGet({
            url : Alfresco.constants.PROXY_URI + "/vgr/kiv/userunit?format=json",
            successCallback : {
               fn : function(res) {
                  if (res.json) {
                     var hsa_id = res.json.hsa_id;
                     var hsa_value = res.json.hsa_value;

                     if (self.options.dflt === "{user_unit}") {
                        self.options.dflt = hsa_id + "|" + hsa_value;
                     }
                  } else {
                     self.options.dflt = "";
                  }

                  self.afterDOMReady();
               },
               scope : this
            },
            failureCallback : {
               fn : function(res) {
                  self.afterDOMReady();
               },
               scope : this
            }
         });
      },

      afterDOMReady : function() {
         this._parseSelected();

         this._setDisabled();

         this._initComponentsLoaded();
      },

      _parseSelected : function() {
         var selectedList = new Array();

         var hiddenSelectedIdsInput = YAHOO.util.Dom.get(this.fieldHtmlId + ".id");
         var hiddenSelectedInput = YAHOO.util.Dom.get(this.fieldHtmlId);

         var emptyValues = this.options.values === "";

         if (hiddenSelectedIdsInput) {
            emptyValues = emptyValues || hiddenSelectedIdsInput.value === "";
         }

         if (emptyValues && this.options.dflt != "" && (!this.options.viewmode) && this.options.version === 0.1) {
            var defaults = this.options.dflt.split("|");

            if (hiddenSelectedIdsInput) {
               hiddenSelectedIdsInput.value = defaults[0];

               if (hiddenSelectedInput) {
                  hiddenSelectedInput.value = defaults[1];
               }

               selectedList.push(defaults[1]);
            } else {
               selectedList.push(this.options.dflt);

               if (hiddenSelectedInput) {
                  hiddenSelectedInput.value = this.options.dflt;
               }
            }
         }

         if (selectedList.length === 0 || selectedList[0] === "") {
            selectedList = this.options.values.split(this.options.optionSeparator);
         }

         if (selectedList.length === 0 || selectedList[0] === "") {
            return [];
         }

         var hiddenSelectedIdsInput = YAHOO.util.Dom.get(this.fieldHtmlId + ".id");

         var selected = [], selectedIdList = [], i;

         if (hiddenSelectedIdsInput) {
            selectedIdList = hiddenSelectedIdsInput.value.split(this.options.optionSeparator);

            for (i = 0; i < selectedList.length; i++) {
               var id = selectedIdList[i];
               var label = selectedList[i];
               var pair = label.split('|');

               selected.push({
                  label : pair.length > 1 ? pair[1] : label,
                  id : id
               });
            }
         } else {
            for (i = 0; i < selectedList.length; i++) {
               var pair = selectedList[i].split('|');

               if (pair.length > 1) {
                  selected.push({
                     label : pair[1],
                     id : pair[0]
                  });
               }
            }
         }

         this.selected = selected;
      },

      /**
       * Fired by YUILoaderHelper when required component script files have been loaded into the browser.
       * 
       * @method onComponentsLoaded
       */
      _initComponentsLoaded : function() {
         this.widgets.selectedList = this.options.viewmode ? Dom.get(this.htmlId) : Dom.get(this.htmlId + '-selected');

         // populate selected
         var selList = this.selected;
         this.selected = {};
         var html = [];
         var i;

         for (i = 0; i < selList.length; i++) {
            html.push('<li id="' + this.htmlId + "//" + selList[i].id + '" class="' + (i % 2 == 0 ? 'odd' : 'even') + '" ' + (selList[i].name ? 'name="' + selList[i].name + '"' : '') + '>');
            html.push(selList[i].label);
            html.push('</li>');

            this.selected[selList[i].id] = selList[i];
         }

         this.widgets.selectedList.innerHTML = html.join('');

         if (this.options.viewmode) {
            this._initViewComponent();
         } else {
            this._initEditableComponent();
         }
      },

      _initViewComponent : function() {
         Bubbling.fire('tree-select.ready', this);
      },

      _initEditableComponent : function() {
         var tree = Dom.get(this.htmlId + '-treeview'), i, x;

         // listen to accordion events, there is a bug in IE7 that hides all li's
         Bubbling.on('accordion.expanded', function(e, dl) {
            var lis = YAHOO.util.Selector.query(".treeselect li", dl[1]);

            for (i = 0; i < lis.length; i++) {
               lis[i].style.zoom = 1;
            }
         });

         // hook up events for selected
         var lis = Dom.getChildren(this.widgets.selectedList);

         for (i = 0; i < lis.length; i++) {
            Event.addListener(lis[i], 'click', this.onSelectClick, lis[i], this);
         }

         // load initial tree data and then setup tree, this way we get rid of that annoying
         // root node
         var self = this;

         this._dataLoader('root', function(res) {
            // create the TreeView
            // set type to our custom type
            for (x = 0; x < res.length; x++) {
               res[x].type = Alfresco.thirdparty.TreeSelectNode;
            }

            self.widgets.treeview = new YAHOO.widget.TreeView(tree, res);
            // self.widgets.treeview.subscribe('clickEvent', self.widgets.treeview.onEventToggleHighlight);
            self.widgets.treeview.subscribe('clickEvent', self.onEventToggleHighlight);
            self.widgets.treeview.singleNodeHighlight = !self.options.multipleSelectMode;
            self.widgets.treeview.options = self.options;

            // set the dynamic loader
            self.widgets.treeview.setDynamicLoad(function(node, onCompleteCallback) {
               self._dataLoader(node.data.id, function(res) {
                  for (i = 0; i < res.length; i++) {
                     var selectableLevels = self.options.selectableLevels > 0;
                     var leaf = (node.depth + 2) >= self.options.selectableLevels;

                     if (selectableLevels && leaf) {
                        res[i].isLeaf = true;
                     }

                     new Alfresco.thirdparty.TreeSelectNode(res[i], node);
                  }

                  onCompleteCallback();
               });

            }, 0);

            // generate the tree
            self.widgets.treeview.render();

            Bubbling.fire('tree-select.ready', this);
         });

         // var b = new YAHOO.widget.Button(this.htmlId + '-fetch-keywords-button');
         // b.addClass('fetch-keywords-button');

         Alfresco.util.createYUIButton(this, "fetch-keywords-button", this.onFetchKeywordsButtonClick);

         // hook up buttons
         Event.addListener(this.htmlId + '-add', 'click', this.onAddButtonClick, this, true);
         Event.addListener(this.htmlId + '-remove', 'click', this.onRemoveButtonClick, this, true);
         Event.addListener(this.htmlId + '-clear', 'click', this.onClearButtonClick, this, true);
      },

      onComponentsLoaded : function() {
         var self = this;

         Event.onDOMReady(function() {
            self.onDOMReady();
         });
      },

      /**
       * Fired when the user clicks the remove button Removes the selected items from the selected list
       * 
       * @method onRemoveButtonClick
       * @param event
       *           {object} an "click" event
       */
      onRemoveButtonClick : function(event, obj) {
         var i;

         Event.stopEvent(event);

         if (this.options.disabled) {
            return;
         }

         var s = this._getSelected();

         var removed = [];

         for (i = 0; i < s.length; i++) {
            var id = s[i].id;

            var id = id.split("//")[1];

            delete this.selected[id];

            removed.push(id);

            this.widgets.selectedList.removeChild(s[i]);
         }

         Dom.removeClass(this.htmlId, 'treeselect-full');

         Bubbling.fire('tree-select.remove', removed);
      },

      _showProgressDialog : function(msg) {
         this._progress = Alfresco.util.PopupManager.displayMessage({
            displayTime : 0,
            text : Alfresco.util.message(msg),
            modal : true,
            visible : true
         });
      },

      _hideProgressDialog : function() {
         if (this._progress) {
            this._progress.hide();
         }
      },

      onFetchKeywordsButtonClick : function(event, obj) {
         this._showProgressDialog(this.msg('message.loading.keywords'));

         Alfresco.util.Ajax.request({
            url : this.options.fetchKeywordsUrl,
            method : Alfresco.util.Ajax.GET,
            dataObj : {
               nodeRef : this.options.nodeRef
            },
            responseContentType : Alfresco.util.Ajax.JSON,
            successCallback : {
               fn : function(res) {
                  var added = [];

                  var values = res.json.values;

                  if (values.length == 0) {
                     Alfresco.util.PopupManager.displayMessage({
                        displayTime : 3,
                        text : Alfresco.util.message(this.msg('message.loading.no-keywords')),
                        modal : true,
                        visible : true
                     });
                  }

                  for ( var x = 0; x < values.length; x++) {
                     var value = values[x];

                     if (!this.selected[value.id]) { // check so we haven't added it before
                        var obj = value;

                        this.selected[obj.id] = obj;

                        // add a new li
                        var li = document.createElement('li');

                        li.id = this.htmlId + '//' + obj.id; // we add htmlid to make it unique if more than one control is present on the page

                        if (obj.dn) {
                           li.name = obj.dn;
                        }

                        li.innerHTML = obj.label;

                        var children = Dom.getChildren(this.widgets.selectedList);

                        if (children.length % 2 == 0) {
                           Dom.addClass(li, 'odd');
                        } else {
                           Dom.addClass(li, 'even');
                        }

                        this.widgets.selectedList.appendChild(li);

                        added.push(li);

                        // hook up an event listener for click events
                        Event.addListener(li, 'click', this.onSelectClick, li, this);
                     }
                  }

                  Bubbling.fire('tree-select.add', added);

                  this._hideProgressDialog();
               },
               scope : this
            },
            failureCallback : { // TODO: proper error messages
               fn : function(res) {
                  this._hideProgressDialog();

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

      /**
       * Fired when the user clicks the add button Adds the selected items from thre tree to the selected list
       * 
       * @method onAddButtonClick
       * @param event
       *           {object} an "click" event
       */
      onAddButtonClick : function(event, obj) {
         var i;

         Event.stopEvent(event);

         if (this.options.disabled) {
            return;
         }

         if (!this.options.multipleSelectMode && Dom.getChildren(this.widgets.selectedList).length > 0) {
            return;
         }

         var nodes = this.widgets.treeview.getNodesByProperty('highlightState', 1);

         if (!nodes) {
            return;
         }

         // if multipleSelectMode is false we only add the first
         if (!this.options.multipleSelectMode && nodes.length > 1) {
            nodes = [ nodes[0] ];
         }

         var added = [];

         for (i = 0; i < nodes.length; i++) {
            var node = nodes[i];

            // if the node has no ID, it can't be selected
            if (!node.data.id || node.data.id.length == 0) {
               continue;
            }

            if (!this.selected[node.data.id]) { // check so we haven't added it before
               var obj = node.data;

               obj.label = node.label;

               this.selected[obj.id] = obj;

               // add a new li
               var li = document.createElement('li');

               li.id = this.htmlId + '//' + obj.id; // we add htmlid to make it unique if more than one control is present on the page

               if (obj.dn) {
                  li.name = obj.dn;
               }

               li.innerHTML = obj.label;

               var children = Dom.getChildren(this.widgets.selectedList);

               if (children.length % 2 == 0) {
                  Dom.addClass(li, 'odd');
               } else {
                  Dom.addClass(li, 'even');
               }

               this.widgets.selectedList.appendChild(li);

               added.push(li);

               // hook up an event listener for click events
               Event.addListener(li, 'click', this.onSelectClick, li, this);

            }

            node.unhighlight();
         }

         // disable add button if not in multipleSelectMode
         if (!this.options.multipleSelectMode && added.length > 0) {
            Dom.addClass(this.htmlId, 'treeselect-full');
         }

         Bubbling.fire('tree-select.add', added);
      },

      /**
       * Fired when the user clicks the clear all button Clears the selected items list
       * 
       * @method onClearButtonClick
       * @param event
       *           {object} an "click" event
       */
      onClearButtonClick : function(event, obj) {
         Event.stopEvent(event);

         if (this.options.disabled) {
            return;
         }

         this.widgets.selectedList.innerHTML = "";

         this.selected = {};

         Dom.removeClass(this.htmlId, 'treeselect-full');

         Bubbling.fire('tree-select.clear', this);
      },

      /**
       * Fired when the user clicks on a li in the selected items list
       * 
       * @method onClearButtonClick
       * @param event
       *           {object} an "click" event
       */
      onSelectClick : function(event, obj) {
         if (this.options.disabled) {
            return;
         }

         if (Dom.hasClass(obj, 'selected')) {
            Dom.removeClass(obj, 'selected');
         } else {
            Dom.addClass(obj, 'selected');
         }
      },

      _filterOnClass : function(cls, lst) {
         var l = [];
         var i;

         for (i = 0; i < lst.length; i++) {
            if (Dom.hasClass(lst[i], cls)) {
               l.push(lst[i]);
            }
         }

         return l;
      },

      _getSelected : function() {
         return this._filterOnClass('selected', Dom.getChildren(this.widgets.selectedList));
      },

      _setDisabled : function() {
         if (this.options.disabled) {
            Dom.addClass(this.htmlId, 'treeselect-disabled');
         } else {
            Dom.removeClass(this.htmlId, 'treeselect-disabled');
         }
      },

      _dataLoader : function(id, callback) {
         Alfresco.util.Ajax.request({
            url : this.dataLoaderUrl,
            method : Alfresco.util.Ajax.GET,
            dataObj : {
               node : id
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

      onTreeReady : function(event, args) {
         var htmlid = this.options.viewmode ? this.htmlId : this.htmlId + '-selected';

         var tooltip = new Alfresco.thirdparty.TreeSelectTooltip(htmlid, this.fieldHtmlId);
         tooltip.setOptions(this.options);

         this.widgets.tooltip = tooltip;
      },

      onValueChanged : function(event, args) {
         Alfresco.util.updateListValue(this.htmlId + '-selected', this.fieldHtmlId, this.options.mandatory);
      },

      /**
       * Override this method in order to make nodes without a valid ID unselectable
       */
      onEventToggleHighlight : function(args) {
         var node;

         if ('node' in args && args.node instanceof YAHOO.widget.Node) {
            node = args.node;
         } else if (args instanceof YAHOO.widget.Node) {
            node = args;
         } else {
            return false;
         }

         if (!node.isLeaf && this.options.onlySelectLeaf) {
            return false;
         }

         if (!node.data.id || node.data.id.length == 0) {
            return false;
         }

         node.toggleHighlight();

         return false;
      }

   });

   /**
    * Helper function to add the current state of the given list to the given hidden field.
    * 
    * @method updateListValue
    * @param list
    *           {string} The id of the ul|ol element
    * @param hiddenField
    *           {string} The id of the hidden field to populate the value with
    * @param signalChange
    *           {boolean} If true a bubbling event is sent to inform any interested listeners that the hidden field value changed
    * @static
    */
   Alfresco.util.updateListValue = function(list, hiddenField, signalChange) {
      var listElement = YUIDom.get(list);

      if (listElement !== null) {
         var values = [];
         var ids = [];

         var children = YUIDom.getChildren(listElement);

         var hiddenIdInput = YUIDom.get(hiddenField + ".id");

         for ( var j = 0, jj = children.length; j < jj; j++) {
            var id = children[j].id;
            var value = children[j].innerHTML;

            if (id.indexOf('//') != -1 && id.indexOf('//') + 2 < id.length) {
               id = id.split('//')[1]; // id of li is ${fieldHtmlId}//ID to make it unique to that control
            }

            if (hiddenIdInput) {
               values.push(value);
               ids.push(id);
            } else {
               values.push(id + '|' + value);
            }
         }

         YUIDom.get(hiddenField).value = values.join("#alf#");

         if (hiddenIdInput) {
            YUIDom.get(hiddenIdInput).value = ids.join("#alf#");
         }

         if (signalChange) {
            YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
         }
      }
   };

})();
