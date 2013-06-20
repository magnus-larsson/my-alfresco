(function() {

   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event;

   /**
    * Alfresco.thirdparty.AjaxSelectOne constructor.
    *
    * @param {string}
    *           htmlId i.e. the id of the select
    * @param {string}
    *           ds - datasource url, i.e. the url to the webscript that returns data to fill the select with options. The URL can include "{parent}" if a parent is configured. This will then be
    *           replaced with the value of the parent.
    * @param {string}
    *           value, current value or '' if not set (i.e. ${fieldValue})
    *
    * @param {object}
    *           options
    * @param {string}
    *           emptyTitle - (optional) first options text if there is no value for this field, i.e. a heping text like "Choose..."
    * @param {string}
    *           parent - (optional) Name of parent field. If this is specified the control will wait with it's ajax request until it gets a "formValueChanged" event for that control. Only works with
    *           object finder controls and another ajaxselectone right now. This way you can chain the select to load after another value is already choosen.
    * @param {string}
    *           default_value - (optional) default value
    *
    * @return {Alfresco.thirdparty.AjaxSelectOne} The new AjaxSelectOne instance
    * @constructor
    */
   Alfresco.thirdparty.AjaxSelectOne = function(htmlId, ds, value, options) {
      Alfresco.thirdparty.AjaxSelectOne.superclass.constructor.call(this, "Alfresco.thirdparty.AjaxSelectOne", htmlId, [ "json" ]);

      this.id = htmlId;
      this.ds = ds;
      this.value = value;
      this.setOptions(options);

      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(Alfresco.thirdparty.AjaxSelectOne, Alfresco.component.Base, {

      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options : {
         emptyTitle : null,
         parent : null,
         default_value : null,
         signalChange : true,
         baseHtmlId : '',
         idField : ''
      },

      /**
       * Flag if component is disabled or not
       *
       * @property disabled
       * @type boolean
       */
      disabled : false,

      /**
       * private instance of progress dialog
       *
       */
      _progress : null,

      /**
       * Map of entries
       */
       _entries: {},
      
      /**
       * Select el
       */
       _el: null,
       
       
      /**
       * props ul
       */
       _props_el: null,

      /**
       * Fired by YUILoaderHelper when required component script files have been loaded into the browser.
       *
       * @method onReady
       */
      onReady : function() {
         var me = this;
         this._el = Dom.get(this.id);
         this._props_el = Dom.get(this.id+'-properties');

         YAHOO.util.Event.onDOMReady(function() {
            me.onDOMReady();
         });

         // no parent
         if (!this.options.parent) {
            this.loadOptions();
         } else {
            // clear and add emptyTitle
            this._clear();
            // We don't load right away but instead listen for an event from
            // the parent element

            YAHOO.Bubbling.on('formValueChanged', function(name, ev) {
               // figure out what sent it
               if (ev && ev.length && ev.length > 1) {
                  var obj = ev[1].eventGroup; // TODO: figure out what it all means in the list

                  if (obj && obj.id === me.options.parent) {
                     // this is the field we're intrested in!
                     // let's see if it sent it's value
                     var value;
                     // object-finder.js
                     if (ev[1].selectedItems && ev[1].selectedItems.length > 0) {
                        value = ev[1].selectedItems.join(",");
                     } else if (ev[1].value) { // ajax-select.js
                        value = ev[1].value;
                     } else {
                        // try to get the value from the form element itself
                        value = me._val(obj.id);
                     }
                     me.loadOptions(value);
                  }
               }
            });
         }

         // listen to onchange event ad fire formValueChanged
         Event.addListener(this.id, 'change', function() {
            me._valueChanged(me._val(me.id));
         });
      },

      onDOMReady : function() {

      },

      /**
       * Does the actual ajax request and populates the options
       *
       * @param {string}
       *           parentValue - (optional) value to substitute '{parent}' in url
       *
       */
      loadOptions : function(parentValue) {
         var me = this;

         // if we have a parent configured and parentValue === ""
         // then we should clear the options instead
         if (this.options.parent && parentValue === "") {
            return this._clear;
         }

         var url = this.ds;

         if (parentValue) {
            url = url.replace('{parent}', parentValue);
         }

         this._showProgressDialog();

         Alfresco.util.Ajax.request({
            url : url,
            method : Alfresco.util.Ajax.GET,
            responseContentType : Alfresco.util.Ajax.JSON,
            successCallback : {
               fn : me._successCallback,
               scope : this
            },
            failureCallback : { // TODO: proper error messages
               fn : function() {
                  me._hideProgressDialog();
               },
               scope : this
            },
            scope : this
         });
      },

      _successCallback : function(res) {
         this._entries = {};
         var json = res.json.values;

         var selectElem = Dom.get(this.id);

         var options = new Array();
         var option;

         // add emptyTitle first
         if (this.options.emptyTitle) {
            option = new Option(this.options.emptyTitle, "", false, false);
            options.push(option);
         }

         var entry, value = '', selected = false, i, id;

         for (i = 0; i < json.length; i++) {
            entry = json[i];

            var idField = this._getIdField();

            // check if this option should be selected
            if (idField) {
               id = idField.value;

               if ((id && id === entry.internalId) || (this.options.default_value && this.options.default_value === entry.internalId)) {
                  value = entry.internalId;
                  selected = true;
               } else {
                  selected = false;
               }
            } else {
               if ((this.value && this.value === entry.name) || (this.options.default_value && this.options.default_value === entry.name)) {
                  value = entry.internalId;
                  selected = true;
               } else {
                  selected = false;
               }
            }

            option = new Option(entry.name, entry.name, false, selected);
            option.id = entry.internalId;
            options.push(option);
            
            //store props
            this._entries[entry.internalId] = entry;
         }

         // clear
         selectElem.options.length = 0;

         // add options
         for (i = 0; i < options.length; i++) {
            selectElem.options[i] = options[i];
         }

         if (this.options.signalChange) {
            YAHOO.Bubbling.fire("mandatoryControlValueUpdated");
         }

         // fire a formValueChanged
         // event so any chained select can update
         this._valueChanged(value);

         this._hideProgressDialog();
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
               Dom.addClass(this.id, 'AjaxSelectOne-disabled');
            } else {
               Dom.removeClass(this.id, 'AjaxSelectOne-disabled');
            }
         }
      },

      /**
       * Clears the select
       */
      _clear : function() {
         var selectElem = YAHOO.util.Dom.get(this.id);
         selectElem.options.length = 0;

         if (this.options.emptyTitle) {
            selectElem.options[0] = new Option(this.options.emptyTitle, "", false, false);
         }
      },

      /**
       * Show the progress dialog
       *
       */
      _showProgressDialog : function() {
         this._progress = Alfresco.util.PopupManager.displayMessage({
            displayTime : 0,
            text : Alfresco.util.message(this.msg('message.loading')),
            modal : true,
            visible : true
         });
      },

      _hideProgressDialog : function() {
         if (this._progress) {
            this._progress.hide();
         }
      },

      _valueChanged : function(value) {
         this._setIdField(value);

         YAHOO.Bubbling.fire("formValueChanged", {
            eventGroup : this,
            selectValue : value
         });
      
         //update possible help text, i.e. properties
         this._updateProps(this._entries[value]);
      },
      
      _updateProps: function(entry) {
         var html = [];
         if (entry && entry.properties && this._props_el) {
            var props = entry.properties; 
            html.push('<table><tbody>');
            for (var prop in props) {
               if (props.hasOwnProperty(prop) && props[prop] !== '_' ) {
                  html.push('<tr><td><strong>');
                  html.push(prop);
                  html.push('</strong></td><td>');
                  html.push(props[prop]);
                  html.push('</td></tr>');
               }
            }
            html.push('</tbody></table>');
            
         }
         this._props_el.innerHTML = html.join('');
         Dom.setStyle(this._props_el,'width',this._el.offsetWidth+'px');
      },

      _setIdField : function(value) {
         var idField = this._getIdField();

         if (!idField) {
            return;
         }

         idField.value = value;
      },

      _getIdField : function() {
         return Dom.get(this.options.baseHtmlId + '_prop_' + this.options.idField.replace(':', '_'));
      },

      /**
       * Figures out the value of a form element can handle inputs and select tags
       */
      _val : function(id) {
         var el = Dom.get(id);

         if (el) {
            if (el.options && el.options.length > 0) {
               var o = el.options[el.selectedIndex];

               if (o.id) { // if the options has an id this is the value we want. i.e. the internalid
                  return o.id;
               }

               return o.value;
            }

            if (el.value) {
               return el.value;
            }
         }

         return "";
      }
   });

})();
