
(function() {

   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Button = YAHOO.widget.Button, query = YAHOO.util.Selector.query;
   var $html = Alfresco.util.encodeHTML;

   // TODO: find proper syntax for an object as argument, proper syntax for describing functions
   /**
    * Alfresco.thirdparty.SearchControl constructor. Takes an object as configuration
    *
    * @param {string}
    *           id the base fieldHtmlId
    * @param {boolen}
    *           viewmode, default false, if true no dialog is created.
    * @param {boolean}
    *           signalChange, (optional) default true, indicates if event should be fired on success
    * @param {string}
    *           editButton, id or DOM Node of edit button
    * @param {string}
    *           panel,id or DOM Node of panel base html
    * @param {multipe}
    *           boolean, is this a multiple select? (default: false)
    * @param {boolean}
    *           autoLoad (optional), if true does a blank search on opening the dialog (default: false)
    * @param {boolean}
    *           siteFiltering (optional), if false don't display radiobuttons (default: true)
    * @param {string}
    *           url of of webservice to call, can/should include tags {filter}, {site} and {siteFilter} that will be replaced with suitable parameters
    * @param {function}
    *           success, a function(value) that is called when the user selects a search hit. Will not be called on cancel. *
    * @param {function}
    *           renderer (optional), a function that renders each row in the result list, see Alfresco.thirdparty.SearchControl.renderers for standard functions (bottom of this src file)
    * @param {function}
    *           parser (optional), function that takes the raw text of ajax response and parses it to a list we can loop over
    * @param {function}
    *           loader (optional) function that loads the presentation of aleady saved data
    * @param
    * @return {Alfresco.thirdparty.SearchControl}
    * @constructor
    */
   Alfresco.thirdparty.SearchControl = function(options) {
      this.name = "Alfresco.thirdparty.SearchControl";

      // default options
      this.options = YAHOO.lang.merge({
         id : "search-control",
         viewmode : false,
         signalChange : true,
         editButton : null,
         panel : 'search-control-panel',
         autoLoad : false,
         multipe : false,
         siteFiltering : true,
         url : null,
         success : function(value) {
         },
         renderer : Alfresco.thirdparty.SearchControl.renderers.standard,
         parser : Alfresco.thirdparty.SearchControl.parsers.standard,
         loader : null,
         separator : ','
      }, options);

      this.htmlId = this.options.id;
      this.id = this.options.id;

      this.widgets = {
         editButton : null,
         radioSite : null,
         panel : null,
         helpText : null,
         list : null,
         input : null,
         searchButton : null
      };

      // Register this component
      Alfresco.util.ComponentManager.register(this);

      // Load YUI Components
      Alfresco.util.YUILoaderHelper.require([ "button", "json" ], this.onComponentsLoaded, this);

      return this;
   };

   Alfresco.thirdparty.SearchControl.prototype = {

      /**
       * Object container for storing YUI widget instances.
       *
       * @property widgets
       * @type object
       */
      widgets : null,

      /**
       * Flag if component is disabled or not
       *
       * @property disabled
       * @type boolean
       */
      disabled : false,

      /**
       * Internal state The search list hits, in order
       */
      _data : [],

      /**
       * Set messages for this module.
       *
       * @method setMessages
       * @param obj
       *           {object} Object literal specifying a set of messages
       * @return {Alfresco.thirdparty.TreeSelect} returns 'this' for method chaining
       */
      setMessages : function(obj) {
         Alfresco.util.addMessages(obj, this.name);
         return this;
      },

      /**
       * Fired by YUILoaderHelper when required component script files have been loaded into the browser.
       *
       * @method onComponentsLoaded
       */
      onComponentsLoaded : function() {
         var me = this;
         YAHOO.util.Event.onDOMReady(function(){ me.onReady(); });
      },
      
      onReady: function(){
         
         var log = function(obj) {
            //var t = document.forms[1]['prop_vgr_dc#dot#description'];
            //t.value = t.value + '\n' + YAHOO.lang.dump(obj);
            //alert(YAHOO.lang.dump(obj));
         };
         log('onReady');
         log(this.options.id);
         
         if (!this.options.viewmode) {
            
            // make a YUI Button of the editButton
            this.widgets.editButton = new Button(this.options.editButton);
            // hook up click event
            Event.addListener(this.options.editButton, 'click', this.onEditClick, this, true);

            // init popup dialog, but don't show it
            this.widgets.panel = Alfresco.util.createYUIPanel(this.options.panel, {
               width : "600px"
            });

            // get all widgets and other nodes
            this.widgets.helpText = Dom.get(this.options.id + "-search-control-helptext");
            this.widgets.input = Dom.get(this.options.id + "-search-control-form-input");
            this.widgets.list = Dom.get(this.options.id + "-search-control-list");
            this.widgets.searchButton = new Button(this.options.id + "-search-control-form-button");
            this.widgets.radioSite = Dom.get(this.options.id + "-search-control-radio-site");
            log(this.widgets.radioSite);
            this.widgets.radioSite.checked = true;
   
            

            if (!this.options.siteFiltering) {
               Dom.get(this.options.id + '-search-control-radio-site-div').style.visibility = "hidden";
            }

            // hook up events in the dialog
            this.widgets.searchButton.addListener('click', this.onSearchClick, this, true);
            Event.addListener(this.widgets.input, 'keyup', function(event, obj) {
               // disable/enable search button, min 3 chars for a search
               if (this.widgets.input.value && this.widgets.input.value.length > 2) {
                  this.widgets.searchButton.set('disabled', false);
               } else {
                  this.widgets.searchButton.set('disabled', true);
               }
               if (event.keyCode == 13) { // enter
                  this.onSearchClick.call(this, event, obj);
               }
            }, this, true);
         }

         // run loader to load values (i.e. a list of noderefs is not that nice)
         if (this.options.loader) {
            this.options.loader.call(this);
         }
      },

      /**
       * Fired when the user clicks the edit button Shows clears and shows the popup window
       *
       * @method onEditClick
       * @param event
       *           {object} an "click" event
       */
      onEditClick : function(event, obj) {
         Event.stopEvent(event);
         if (this.disabled) {
            return;
         }

         // clear popup and init with fresh value
         this.clear();
         // show popup
         this.show();
      },

      /**
       * Clears and resets the dialog window i.e. clear the result list and copy current value into to the search box
       */
      clear : function() {
         Dom.setStyle(this.widgets.list, 'display', 'none');
         YAHOO.util.Event.purgeElement(this.widgets.list, true); // purge events to prevent memory leakage
         this.widgets.list.innerHTML = '';
         this.widgets.input.value = '';
         this.widgets.searchButton.set('disabled', true);
         this.widgets.radioSite.checked = true;
      },

      /**
       *
       * Shows the panel
       *
       */
      show : function() {
         this.widgets.panel.show();

         // do a initial search
         if (this.options.autoLoad) {
            this._search("");
         }
      },

      /**
       * Seach button clicked
       *
       * @method onEditClick
       * @param event
       *           {object} an "click" event
       */
      onSearchClick : function(event, obj) {
         Event.stopEvent(event);
         if (this.disabled) {
            return;
         }

         if (this.widgets.input.value && this.widgets.input.value.length > 2) {
            this._search(this.widgets.input.value);
         } else {
            Alfresco.util.PopupManager.displayMessage({
               text : this.msg('form.search.min'),
               displayTime : 3
            });
         }
      },

      /**
       * Issues a search
       */
      _search : function(value) {
         var popup = Alfresco.util.PopupManager.displayMessage({
            text : this.msg('form.search.loading'),
            displayTime : 60,
            spanClass : "wait"
         });
         // popup hides behind our dialog
         Dom.setStyle(popup.body.parentNode.parentNode, "z-index", "1000");

         var siteFilter = this.widgets.radioSite.checked;
         if (!this.options.siteFiltering) {
            siteFilter = false;
         }

         // figure out site from urlencoded
         var m = window.location.href.match(new RegExp('/share/page/site/([^/]+)/'));
         var site = '';
         if (m && m.length > 1) {
            site = m[1];
         }

         var url = this.options.url;
         url = url.replace('{filter}', encodeURIComponent(value)).replace('{siteFilter}', siteFilter).replace('{site}', site);

         var me = this;
         YAHOO.util.Connect.asyncRequest('GET', url, {
            success : function(o) {
               this.update(this.options.parser.call(me, o.responseText));
               popup.hide();
            },
            failure : function() {
               popup.hide();
            }, // TODO: handle failure
            scope : this
         });

      },

      /**
       *
       * @param {Object}
       *           disabled
       */
      setDisabled : function(disabled) {
         if (disabled != this.disabled) {
            this.disabled = disabled;
            if (disabled) {
               Dom.addClass(this.htmlId, 'searchcontrol-disabled');
            } else {
               Dom.removeClass(this.htmlId, 'searchcontrol-disabled');
            }
         }
      },

      /**
       * Updates the list
       *
       * @param {Array}
       *           data to display
       *
       */
      update : function(data) {
         this._data = data;
         if (data && data.length > 0) {

            // build the actual html
            var renderer = this.options.renderer;
            var html = [];

            for ( var i = 0; i < data.length; i++) {
               renderer.call(this, html, data[i], i % 2 == 1);
            }
            // Dom.setStyle(this.widgets.helpText,'display','none');

            Dom.setStyle(this.widgets.list, 'display', 'block');
            YAHOO.util.Event.purgeElement(this.widgets.list, true); // purge events to prevent memory leakage
            this.widgets.list.innerHTML = html.join('');

            // hook up click events for select
            var children = Dom.getChildren(this.widgets.list);
            for ( var i = 0; i < children.length; i++) {
               (function(index) { // use function to scop index variable
                  Event.addListener(children[index], "click", function(event, obj) {
                     // apply value and close dialog
                     this.options.success.call(this, this._data[index]);
                     this.widgets.panel.hide();
                     if (this.options.signalChange) {
                        YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
                     }
                  }, this, true);
               }).call(this, i);
            }
         } else {
            // empty list?
            Dom.setStyle(this.widgets.list, 'display', 'block');
            this.widgets.list.innerHTML = '<li class="empty">' + this.msg('form.search.none') + '</li>';
         }
      },

      /**
       * Gets a custom message
       *
       * @method msg
       * @param messageId
       *           {string} The messageId to retrieve
       * @return {string} The custom message
       */
      msg : function(messageId) {
         return Alfresco.util.message.call(this, messageId, this.name, Array.prototype.slice.call(arguments).slice(1));
      }
   };

   // renderers
   if (!Alfresco.thirdparty.SearchControl.renderers) {
      Alfresco.thirdparty.SearchControl.renderers = {};
   }

   // merge standard with existing renderers so that you can override
   // standard renderers just by loading their definitions before this
   // Use the configuration "renderer" in the xml file to set this, use the
   // full name: Alfresco.thirdparty.SearchControl.renderers.standard
   //
   // A mapping is a function(html,row,odd,msg) that returns nothing
   // html - list of strings that will be concatenated as HTML, append your li
   // tags to this list
   // row - row from datasource
   // odd - boolean, true if this is an odd row
   // msg - the msg translation function
   Alfresco.thirdparty.SearchControl.renderers = YAHOO.lang.merge({
      standard : function(html, row, odd, remove_button) { // default field mapper: key value
         html.push('<li title="');
         html.push(row.value);
         html.push('" class="');
         html.push(row.value);
         html.push(odd ? ' odd' : ' even');
         html.push('">');

         if (remove_button) {
            html.push('<a href="#" data-ref="');
            html.push(row.nodeRef);
            html.push('" class="remove">&nbsp;</a>');
         }

         if (row.label) {
            html.push($html(row.label));
         } else {
            html.push($html(row.value));
         }

         html.push('</li>');

      },
      people : function(html, row, odd, remove_button) {
         html.push('<li title="');
         html.push(row.userName);
         html.push('" class="');
         html.push(row.userName);
         html.push(odd ? ' odd' : ' even');
         html.push('">');

         if (row.fullName) { // list items
            html.push('<h3 class="itemname">');

            if (remove_button) {
               html.push('<a href="#" data-ref="');
               html.push(row.userName);
               html.push('" class="remove">&nbsp;</a>');
            }

            html.push('<span class="theme-color-1" tabindex="0">');
            html.push(row.fullName);
            html.push(' </span><span class="lighter">(' + $html(row.userName) + ')</span>');
            html.push('</h3>');

         } else {

            if (remove_button) {
               html.push('<a href="#" data-ref="');
               html.push(row.userName);
               html.push('" class="remove">&nbsp;</a>');
            }

            html.push('<h3 class="itemname"><span class="theme-color-1" tabindex="0">');
            var fullName = row.firstName + ' ' + row.lastName;
            html.push(fullName && fullName !== " " ? $html(fullName) : $html(row.userName));
            html.push(' </span><span class="lighter">(' + $html(row.userName) + ')</span></h3>');

            if (row.title) {
               html.push('<div class="detail"><span>' + this.msg("label.title") + ":</span> " + $html(row.jobtitle.replace(/ou{0,1}=/g, '')) + '</div>');
            }
            if (row.email) {
               html.push('<div class="detail"><span>' + this.msg("label.email") + ":</span> " + $html(row.email) + '</div>');
            }

            if (row.organization) {
               html.push('<div class="detail"><span>' + this.msg("label.company") + ":</span> " + $html(row.organization.replace(/ou{0,1}=/g, '').replace(',Org,VGR', '')) + '</div>');
            }

            html.push('</li>');
         }

      }

   }, Alfresco.thirdparty.SearchControl.renderers);

   // parsers: function(o.responseText) -> array
   if (!Alfresco.thirdparty.SearchControl.parsers) {
      Alfresco.thirdparty.SearchControl.parsers = {};
   }

   // standard is a JSON parser
   Alfresco.thirdparty.SearchControl.parsers = YAHOO.lang.merge({
      standard : function(raw_data) {
         var data = YAHOO.lang.JSON.parse(raw_data);
         if (data instanceof Array) {
            return data;
         } else {
            for (var attr in data) { // just take the first array
               if (data[attr] instanceof Array) {
                  return data[attr];
               }
            }
         }
         return [];
      }
   }, Alfresco.thirdparty.SearchControl.parsers);

   // success: function(value) -> sets the actual value in the form input and
   // and inserts the actual user into a list that can be viewed
   if (!Alfresco.thirdparty.SearchControl.success) {
      Alfresco.thirdparty.SearchControl.success = {};
   }

   // standard is a JSON parser
   Alfresco.thirdparty.SearchControl.success = YAHOO.lang.merge({
      people : function(row) {
         var input = YAHOO.util.Dom.get(this.id);
         var idinput = YAHOO.util.Dom.get(this.id + '.id');

         var fullNames = Alfresco.thirdparty.SearchControl.helpers.split_value(input.value, this.options.separator);
         var userNames = idinput ? Alfresco.thirdparty.SearchControl.helpers.split_value(idinput.value, this.options.separator) : [];

         var fullName = row.firstName + ' ' + row.lastName;
         fullName = fullName && fullName !== " " ? $html(fullName) : $html(row.userName);

         if (this.options.multiple) {
            // it might already be there, remove it then to avoid duplicates
            Alfresco.thirdparty.SearchControl.helpers.remove(fullNames, fullName);
            Alfresco.thirdparty.SearchControl.helpers.remove(userNames, row.userName);

            // add them
            fullNames.push(fullName);
            userNames.push(row.userName);

            input.value = fullNames.length > 0 ? fullNames.join(this.options.separator) : '';
            if (idinput) {
               idinput.value = userNames.length > 0 ? userNames.join(this.options.separator) : '';
            }
         } else {
            input.value = fullName;
            if (idinput) {
               idinput.value = row.userName;
            }

         }
         Alfresco.thirdparty.SearchControl.loaders.people.call(this);
      },

      authority : function(row) {
         Alfresco.thirdparty.SearchControl.helpers.manage_authority_inputs.call(this, function(refs, added, removed) {
            var is_in = Alfresco.thirdparty.SearchControl.helpers.is_in;
            var remove = Alfresco.thirdparty.SearchControl.helpers.remove;

            // check if it's already added
            if (!is_in(row.nodeRef, refs) && !is_in(row.nodeRef, added)) {
               if (!this.options.multiple) {
                  added = [ row.nodeRef ]; // remove any other added field

                  // possible that it has been removed before, try removing it
                  remove(removed, row.nodeRef);

                  // in single mode this means that all refs are removed
                  removed = removed.concat(refs);
                  refs = [];
               } else {
                  // add to added
                  added.push(row.nodeRef);

                  // possible that it has been removed before, try removing it
                  remove(removed, row.nodeRef);
               }
            }
            return {
               refs : refs,
               added : added,
               removed : removed
            };
         });
         // ok that's the values, now for the presentation
         Alfresco.thirdparty.SearchControl.loaders.authority.call(this);

      }

   }, Alfresco.thirdparty.SearchControl.success);

   // loaders: function() -> updates presentation of data
   if (!Alfresco.thirdparty.SearchControl.loaders) {
      Alfresco.thirdparty.SearchControl.loaders = {};
   }

   Alfresco.thirdparty.SearchControl.loaders = YAHOO.lang.merge({
      people : function() {
         var fullNames = Dom.get(this.id).value;
         var userNames = "";
         var idinput = Dom.get(this.id + '.id');
         if (idinput) {
            userNames = idinput.value;
         }
         var ul = Dom.get(this.id + '-search-control-value-list');
         
         if (fullNames && fullNames != "") {
            
            fullNames = Alfresco.thirdparty.SearchControl.helpers.split_value(fullNames, this.options.separator);
            userNames = Alfresco.thirdparty.SearchControl.helpers.split_value(userNames, this.options.separator);

            var html = [];
            var length = Math.max(fullNames.length, userNames.length);
            var people = Alfresco.thirdparty.SearchControl.renderers.people;

            if (length > 0) {
               // render
               for ( var i = 0; i < length; i++) {
                  people.call(this, html, {
                     userName : userNames[i] ? userNames[i] : '',
                     fullName : fullNames[i] ? fullNames[i] : ''
                  }, i % 2 == 0 ? false : true, !this.options.viewmode);
               }
               ul.innerHTML = html.join('');
               ul.style.visibility = "visible";
            } else {
               // hide ul if no items are found //might be hidden
               ul.style.visibility = "hidden";
            }

            // hook up eventlisteners
            if (!this.options.viewmode && length > 0) {
               // since YUI selectors can't handle ul with . in its name (parses it as a class) and it can't be escaped we have to hack it
               var oldid = ul.id;
               var safeid = ul.id.replace(/\./g, 'DOT');
               ul.id = safeid;
               var links = query('#' + safeid + ' a.remove');
               ul.id = oldid;

               for ( var i = 0; i < links.length; i++) {
                  (function(a) { // function to bind loop variable
                     Event.addListener(a, 'click', function(event, obj) {
                        // someone clicked remove
                        Event.stopEvent(event);
                        Event.removeListener(a, 'click');
                        
                        // manage input
                        var input = Dom.get(this.id);
                        var idinput = Dom.get(this.id + '.id');
                        if (this.options.multiple) {
                           // find out which child we are
                           var nr = 0;
                           var ul2 = a.parentNode.parentNode.parentNode;
                           for ( var i = 0; ul.children.length; i++) {
                              if (ul2.children[i] == a.parentNode.parentNode) {
                                 nr = i;
                                 break;
                              }
                           }

                           var vals = Alfresco.thirdparty.SearchControl.helpers.split_value(input.value, this.options.separator);
                           vals.splice(nr, 1);
                           input.value = vals.length > 0 ? vals.join(this.options.separator) : '';

                           if (idinput) {
                              vals = Alfresco.thirdparty.SearchControl.helpers.split_value(idinput.value, this.options.separator);
                              vals.splice(nr, 1);
                              idinput.value = vals.length > 0 ? vals.join(this.options.separator) : '';
                           }

                        } else {
                           input.value = "";
                           if (idinput) {
                              idinput.value = "";
                           }
                        }

                        ul.removeChild(a.parentNode.parentNode); // remove li

                        // hide ul if its empty
                        if (ul.children.length == 0) {
                           ul.style.visibility = "hidden";
                        }

                     }, this, true);
                  }).call(this, links[i]);
               }
            }
         } else {
            // hide empty list
            ul.style.visibility = "hidden";
         }

      },
      authority : function() {
         var input = Dom.get(this.id);

         var refs = input.value;

         // check for added as well
         var added_input = Dom.get(this.id + '-cntrl-added');
         if (added_input && added_input.value != "") {
            if (refs.length > 0) {
               refs = refs + ',' + added_input.value;
            } else {
               refs = added_input.value;
            }
         }

         var ul = Dom.get(this.id + '-search-control-value-list');
         if (refs && refs != "") {

            // do ajax call
            YAHOO.util.Connect.asyncRequest('GET', Alfresco.constants.PROXY_URI + "vgr/peoplebyref?refs=" + refs, {
               success : function(o) {
                  var persons = Alfresco.thirdparty.SearchControl.parsers.standard.call(this, o.responseText);
                  var html = [];
                  // loop
                  if (persons.length > 0) {
                     for ( var i = 0; i < persons.length; i++) {
                        Alfresco.thirdparty.SearchControl.renderers.people.call(this, html, persons[i], i % 2 == 0 ? false : true, !this.options.viewmode);
                     }
                     ul.innerHTML = html.join('');
                     ul.style.visibility = "visible";
                  } else {
                     ul.style.visibility = "hidden";
                  }

                  // hook up eventlisteners
                  if (!this.options.viewmode) {
                     // since YUI selectors can't handle ul with . in its name (parses it as a class) and it can't be escaped we have to hack it
                     var oldid = ul.id;
                     var safeid = ul.id.replace(/\./g, 'DOT');
                     ul.id = safeid;
                     var links = query('#' + safeid + ' a.remove');
                     ul.id = oldid;
                     
                     for ( var i = 0; i < links.length; i++) {
                        (function(a) { // function to bind loop variables
                           Event.addListener(a, 'click', function(event, obj) {
                              // someone clicked remove
                              Event.stopEvent(event);
                              Event.removeListener(a, 'click');
                              var ref = Dom.getAttribute(a, 'data-ref');
                              var ul = a.parentNode.parentNode;
                              ul.removeChild(a.parentNode); // remove li
                              if (ul.children.length == 0) {
                                 ul.style.visibility = "hidden";
                              }

                              // manage inputs
                              Alfresco.thirdparty.SearchControl.helpers.manage_authority_inputs.call(this, function(refs, added, removed) {
                                 var is_in = Alfresco.thirdparty.SearchControl.helpers.is_in;
                                 var remove = Alfresco.thirdparty.SearchControl.helpers.remove;

                                 // check if it's in added or was already saved
                                 if (is_in(ref, refs)) {
                                    removed.push(ref);
                                    remove(refs, ref);
                                 } else {
                                    // it was just added, but not saved, we can safely remove it
                                    remove(added, ref);
                                 }

                                 return {
                                    refs : refs,
                                    added : added,
                                    removed : removed
                                 };
                              });
                           }, this, true);
                        }).call(this, links[i]);
                     }
                  }

               },
               failure : function() {
               }, // TODO: handle failure
               scope : this
            });
         } else {
            ul.style.visibility = "hidden";
         }
      }

   }, Alfresco.thirdparty.SearchControl.loaders);

   // some common helper functions
   Alfresco.thirdparty.SearchControl.helpers = {
      split_value : function(val, separator) {
         if (val) {
            return val.indexOf(separator) == -1 ? [ val ] : val.split(separator);
         }
         return [];
      },

      remove : function(lst, val) {
         for ( var i = 0; i < lst.length; i++) {
            if (lst[i] == val) {
               return lst.splice(i, 1);
            }
         }
      },

      is_in : function(val, lst) {
         for ( var i = 0; i < lst.length; i++) {
            if (lst[i] === val) {
               return true;
            }
         }
         return false;
      },
      manage_authority_inputs : function(manage) {
         // authority (usually with objeck picker) uses three input fieldsm but only _added and _removed are actually posted
         var input = Dom.get(this.id);
         var added_input = Dom.get(this.id + '-cntrl-added');
         var removed_input = Dom.get(this.id + '-cntrl-removed');

         // split into lists
         var refs = Alfresco.thirdparty.SearchControl.helpers.split_value(input.value, ','); // separator for authority is always ,
         var added = Alfresco.thirdparty.SearchControl.helpers.split_value(added_input.value, ',');
         var removed = Alfresco.thirdparty.SearchControl.helpers.split_value(removed_input.value, ',');

         var res = manage.call(this, refs, added, removed);
         refs = res.refs;
         added = res.added;
         removed = res.removed;

         // set it on inputs
         input.value = refs.length > 0 ? refs.join(',') : ''; // separator for authority is always ,
         added_input.value = added.length > 0 ? added.join(',') : '';
         removed_input.value = removed.length > 0 ? removed.join(',') : '';
      }

   };

})();
