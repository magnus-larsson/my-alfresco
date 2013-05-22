// @overridden projects/web-framework-commons/source/web/components/form/form.js

/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Form UI component.
 *
 * @namespace Alfresco
 * @class Alfresco.FormUI
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Element = YAHOO.util.Element,
      Selector = YAHOO.util.Selector;

   /**
    * FormUI constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.FormUI} The new FormUI instance
    * @constructor
    */
   Alfresco.FormUI = function FormUI_consructor(htmlId, parentId)
   {
      this.name = "Alfresco.FormUI";
      this.id = htmlId;
      this.parentId = parentId;

      /* Register this component */
      Alfresco.util.ComponentManager.register(this);

      /* Load YUI Components */
      Alfresco.util.YUILoaderHelper.require(["button", "menu", "container"], this.onComponentsLoaded, this);

      // Initialise prototype properties
      this.buttons = {};
      
      // Create the runtime instance
      this.formsRuntime = new Alfresco.forms.Form(this.id);

      /* Decoupled event listeners */
      YAHOO.Bubbling.on("metadataRefresh", this.onFormRefresh, this);
      YAHOO.Bubbling.on("mandatoryControlValueUpdated", this.onMandatoryControlValueUpdated, this);
      YAHOO.Bubbling.on("registerValidationHandler", this.onRegisterValidationHandler, this);
      YAHOO.Bubbling.on("addSubmitElement", this.onAddSubmitElement, this);

      return this;
   };

   Alfresco.FormUI.prototype =
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
         /**
          * Mode the current form is in, can be "view", "edit" or "create", defaults to "edit".
          *
          * @property mode
          * @type string
          */
         mode: "edit",

         /**
          * Encoding type to be used when the form is submitted, can be "multipart/form-data",
          * "application/x-www-form-urlencoded" or "application/json", defaults to "multipart/form-data".
          *
          * @property enctype
          * @type string
          */
         enctype: "multipart/form-data",

         /**
          * List of objects representing the id of each form field
          *
          * @property fields
          * @type array[object]
          */
         fields: [],

         /**
          * List of objects representing the constraints to setup on the form fields
          *
          * @property fieldConstraints
          * @type array[object]
          */
         fieldConstraints: [],

         /**
          * Arguments used to build the form.
          * Used to Ajax-rebuild the form when in "view" mode
          *
          * @property arguments
          * @type object
          */
         arguments: {}
      },

      /**
       * Object container for storing YUI button instances.
       *
       * @property buttons
       * @type object
       */
      buttons: null,

      /**
       * The forms runtime instance.
       *
       * @property
       * @type object
       */
      formsRuntime: null,

      /**
       * Set messages for this component.
       *
       * @method setMessages
       * @param obj {object} Object literal specifying a set of messages
       * @return {Alfresco.Search} returns 'this' for method chaining
       */
      setMessages: function FormUI_setMessages(obj)
      {
         Alfresco.util.addMessages(obj, this.name);
         return this;
      },

      /**
       * Set multiple initialization options at once.
       *
       * @method setOptions
       * @param obj {object} Object literal specifying a set of options
       * @return {Alfresco.Search} returns 'this' for method chaining
       */
      setOptions: function FormUI_setOptions(obj)
      {
         this.options = YAHOO.lang.merge(this.options, obj);
         return this;
      },

      /**
       * Fired by YUILoaderHelper when required component script files have
       * been loaded into the browser.
       *
       * @method onComponentsLoaded
       */
      onComponentsLoaded: function FormUI_onComponentsLoaded()
      {
         Event.onContentReady(this.id, this.onReady, this, true);
      },

      /**
       * Fired by YUI when parent element is available for scripting.
       * Component initialisation, including instantiation of YUI widgets and event listener binding.
       *
       * @method onReady
       */
      onReady: function FormUI_onReady()
      {
         if (this.options.mode !== "view")
         {
            // make buttons YUI buttons

            if (Dom.get(this.id + "-submit") !== null)
            {
               this.buttons.submit = Alfresco.util.createYUIButton(this, "submit", null,
               {
                  type: "submit"
               });

               // force the generated button to have a name of "-" so it gets ignored in
               // JSON submit. TODO: remove this when JSON submit behaviour is configurable
               Dom.get(this.id + "-submit-button").name = "-";
            }

            if (Dom.get(this.id + "-reset") !== null)
            {
               this.buttons.reset = Alfresco.util.createYUIButton(this, "reset", null,
               {
                  type: "reset"
               });

               // force the generated button to have a name of "-" so it gets ignored in
               // JSON submit. TODO: remove this when JSON submit behaviour is configurable
               Dom.get(this.id + "-reset-button").name = "-";
            }

            if (Dom.get(this.id + "-cancel") !== null)
            {
               this.buttons.cancel = Alfresco.util.createYUIButton(this, "cancel", null);

               // force the generated button to have a name of "-" so it gets ignored in
               // JSON submit. TODO: remove this when JSON submit behaviour is configurable
               Dom.get(this.id + "-cancel-button").name = "-";
            }

            // fire event to inform any listening components that the form HTML is ready
            YAHOO.Bubbling.fire("formContentReady", this);

            this.formsRuntime.setShowSubmitStateDynamically(true, false);
            this.formsRuntime.setSubmitElements(this.buttons.submit);

            // setup JSON/AJAX mode if appropriate
            if (this.options.enctype === "application/json")
            {
               this.formsRuntime.setAJAXSubmit(true,
               {
                  successCallback:
                  {
                     fn: this.onJsonPostSuccess,
                     scope: this
                  },
                  failureCallback:
                  {
                     fn: this.onJsonPostFailure,
                     scope: this
                  }
               });
               this.formsRuntime.setSubmitAsJSON(true);
            }

            // add field help
            for (var f = 0; f < this.options.fields.length; f++)
            {
               var ff = this.options.fields[f],
                  iconEl = Dom.get(this.parentId + "_" + ff.id + "-help-icon");
               if (iconEl)
               {
                  Alfresco.util.useAsButton(iconEl, this.toggleHelpText, ff.id, this);
               }
            }

            // add any field constraints present
            for (var c = 0; c < this.options.fieldConstraints.length; c++)
            {
               var fc = this.options.fieldConstraints[c];
               this.formsRuntime.addValidation(fc.fieldId, fc.handler, fc.params, fc.event, fc.message);
            }

            // fire event to inform any listening components that the form is about to be initialised
            YAHOO.Bubbling.fire("beforeFormRuntimeInit",
            {
               component: this,
               runtime: this.formsRuntime
            });

            this.formsRuntime.init();

            // fire event to inform any listening components that the form has finished initialising
            YAHOO.Bubbling.fire("afterFormRuntimeInit",
            {
               component: this,
               runtime: this.formsRuntime
            });
         }
      },

      /**
       * Toggles help text for a field.
       *
       * @method toggleHelpText
       * @param event The user event
       * @param fieldId The id of the field to toggle help text for
       */
      toggleHelpText: function FormUI_toggleHelpText(event, fieldId)
      {
         Alfresco.util.toggleHelpText(this.parentId + "_" + fieldId + "-help");
      },

      /**
       * Default handler used when submit mode is JSON and the sumbission was successful
       *
       * @method onJsonPostSuccess
       * @param response The response from the submission
       */
      onJsonPostSuccess: function FormUI_onJsonPostSuccess(response)
      {
         // TODO: Display the JSON response here by default, when it's returned!

         Alfresco.util.PopupManager.displayPrompt(
         {
            text: response.serverResponse.responseText
         });
      },

      /**
       * Default handler used when submit mode is JSON and the sumbission failed
       *
       * @method onJsonPostFailure
       * @param response The response from the submission
       */
      onJsonPostFailure: function FormUI_onJsonPostFailure(response)
      {
         var errorMsg = this._msg("form.jsonsubmit.failed");
         if (response.json && response.json.message)
         {
            errorMsg = errorMsg + ": " + response.json.message;
         }

         Alfresco.util.PopupManager.displayPrompt(
         {
            text: errorMsg
         });
      },

      /**
       * Form refresh event handler
       *
       * @method onFormRefresh
       * @param layer {object} Event fired
       * @param args {array} Event parameters (depends on event type)
       */
      onFormRefresh: function FormUI_onFormRefresh(layer, args)
      {
         // Can't do anything if basic arguments weren't set
         if (this.options.arguments)
         {
            var itemKind = this.options.arguments.itemKind,
               itemId = this.options.arguments.itemId,
               formId = this.options.arguments.formId;

            if (itemKind && itemId)
            {
               var fnFormLoaded = function(response, p_formUI)
               {
                  Dom.get(p_formUI.parentId).innerHTML = response.serverResponse.responseText;
               };

               var data =
               {
                  htmlid: this.parentId,
                  formUI: false,
                  mode: this.options.mode,
                  itemKind: itemKind,
                  itemId: itemId,
                  formId: formId
               };

               Alfresco.util.Ajax.request(
               {
                  url: Alfresco.constants.URL_SERVICECONTEXT + "components/form",
                  dataObj: data,
                  successCallback:
                  {
                     fn: fnFormLoaded,
                     obj: this,
                     scope: this
                  },
                  scope: this,
                  execScripts: true
               });
            }
         }
      },

      /**
       * Mandatory control value updated event handler
       *
       * @method onMandatoryControlValueUpdated
       * @param layer {object} Event fired
       * @param args {array} Event parameters (depends on event type)
       */
      onMandatoryControlValueUpdated: function FormUI_onMandatoryControlValueUpdated(layer, args)
      {
         // the value of a mandatory control on the page (usually represented by a hidden field)
         // has been updated, force the forms runtime to check if form state is still valid
         this._delay_until_formsRuntime(function(){
             this.formsRuntime.updateSubmitElements();
         });
      },

      /**
       * Register validation handler event handler
       *
       * @method onRegisterValidationHandler
       * @param layer {object} Event fired
       * @param args {array} Event parameters (depends on event type)
       */
      onRegisterValidationHandler: function FormUI_onRegisterValidationHandler(layer, args)
      {
         // extract the validation arguments
         var validation = args[1];

         // check the minimim required data is provided
         if (validation && validation.fieldId && validation.handler)
         {
            // register with the forms runtime instance
            this._delay_until_formsRuntime(function(){
                this.formsRuntime.addValidation(validation.fieldId, validation.handler, validation.args,
                                                validation.when, validation.message);
            });
         }
      },

      /**
       * Adds a submit element to the form runtime instance
       *
       * @method onAddSubmitElement
       * @param layer {object} Event fired
       * @param args {array} Event parameters (depends on event type)
       */
      onAddSubmitElement: function FormUI_onAddSubmitElement(layer, args)
      {
         // extract the submit element to add
         var submitElement = args[1];

         // add to the forms runtime instance
         this.formsRuntime.addSubmitElement(submitElement);
      },

      /**
       * Gets a custom message
       *
       * @method _msg
       * @param messageId {string} The messageId to retrieve
       * @return {string} The custom message
       * @private
       */
      _msg: function FormUI__msg(messageId)
      {
         return Alfresco.util.message.call(this, messageId, "Alfresco.FormUI", Array.prototype.slice.call(arguments).slice(1));
      },

      /**
       * hack to get around timing issues, delays a function until
       * the formsRuntime is initialized
       */
       _delay_until_formsRuntime: function(func) {

            //polling function
            var me = this;
            var f = func;
            var poller = function() {
                if (me.formsRuntime) {
                    f.call(me);
                } else {
                    setTimeout(poller,333); //play it again sam!
                }
            }
            poller();
         }
   };
})();


/**
 * Helper function to add the current state of the given multi-select list to
 * the given hidden field.
 *
 * @method updateMultiSelectListValue
 * @param list {string} The id of the multi-select element
 * @param hiddenField {string} The id of the hidden field to populate the value with
 * @param signalChange {boolean} If true a bubbling event is sent to inform any
 *        interested listeners that the hidden field value changed
 * @static
 */
Alfresco.util.updateMultiSelectListValue = function(list, hiddenField, signalChange)
{
   var listElement = YUIDom.get(list);

   if (listElement !== null)
   {
      var values = new Array();
      for (var j = 0, jj = listElement.options.length; j < jj; j++)
      {
         if (listElement.options[j].selected)
         {
            values.push(listElement.options[j].value);
         }
      }

      YUIDom.get(hiddenField).value = values.join("#alf#");

      if (signalChange)
      {
         YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
      }
   }
};

/**
 * Helper function to update the current state of the HSA Code multi-select list to the given hidden field.
 *
 * @method updateMultiSelectListValue
 * @param list {string} The id of the multi-select element
 * @param hiddenField {string} The id of the hidden field to populate the value with
 * @param signalChange {boolean} If true a bubbling event is sent to inform any
 *        interested listeners that the hidden field value changed
 * @static
 */
Alfresco.util.updateHsaCodeValue = function(list, hiddenField, signalChange)
{
   var listElement = YUIDom.get(list);
   var hiddenIdElement = YUIDom.get(hiddenField + ".id");

   if (listElement !== null)
   {
      var values = new Array();
      var ids = new Array();

      for (var j = 0, jj = listElement.options.length; j < jj; j++)
      {
         if (listElement.options[j].selected)
         {
            ids.push(listElement.options[j].value);
            values.push(listElement.options[j].text);
         }
      }

      YUIDom.get(hiddenField).value = values.join("#alf#") + "";

      if (hiddenIdElement) {
         hiddenIdElement.value = ids.join("#alf#");
      }

      if (signalChange)
      {
         YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
      }
   }
};

/**
 * Helper function to add the current state of the given list to
 * the given hidden field.
 *
 * @method updateListValue
 * @param list {string} The id of the ul|ol element
 * @param hiddenField {string} The id of the hidden field to populate the value with
 * @param signalChange {boolean} If true a bubbling event is sent to inform any
 *        interested listeners that the hidden field value changed
 * @static
 */
Alfresco.util.updateListValue = function(list, hiddenField, signalChange)
{
   var listElement = YUIDom.get(list);

   if (listElement !== null) {
      var values = [];
      var ids = [];

      var children = YUIDom.getChildren(listElement);

      var hiddenIdInput = YUIDom.get(hiddenField + ".id");

      for (var j = 0, jj = children.length; j < jj; j++) {
          var id = children[j].id;
          var value = children[j].innerHTML;

          if (id.indexOf('//') != -1 && id.indexOf('//')+2 < id.length) {
             id = id.split('//')[1]; //id of li is ${fieldHtmlId}//ID to make it unique to that control
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



/**
 * Helper function to toggle the state of the help text element
 * represented by the given id.
 *
 * @method toggleHelpText
 * @param helpTextId The id of the help text element to toggle
 * @static
 */
Alfresco.util.toggleHelpText = function(helpTextId)
{
   var helpElem = YUIDom.get(helpTextId);

   if (helpElem)
   {
      if (helpElem.style.display != "block")
      {
         helpElem.style.display = "block";
      }
      else
      {
         helpElem.style.display = "none";
      }
   }
};

/**
 * Helper function to add the current state of the given multi-property text to
 * the given hidden field.
 *
 * @method updateMultiInputTextValue
 * @param list {string} The id of the multi-select element
 * @param hiddenField {string} The id of the hidden field to populate the value with
 * @param signalChange {boolean} If true a bubbling event is sent to inform any
 *        interested listeners that the hidden field value changed
 * @static
 */
Alfresco.util.updateMultiInputTextValue = function(hiddenField, signalChange)
{
   var elements = Selector.query('input[id^=' + hiddenField + '_]');

   if (elements !== null)
   {
      var values = new Array();

      for (var x = 0; x < elements.length; x++)
      {
         var element = elements[x];

         if (element.value == null || element.value == "") {
           continue;
         }

         values.push(element.value);
      }

      if (values.length == 0) {
        YUIDom.get(hiddenField).value = "";
      } else if (values.length == 1) {
        YUIDom.get(hiddenField).value = values[0];
      } else {
        YUIDom.get(hiddenField).value = values.join("#alf#");
      }

      if (signalChange)
      {
         YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
      }
   }
};

Alfresco.util.removeMultiInputTextValue = function(hiddenField, inputField, signalChange) {
   // delete the LI and all it's children...
  var obj = document.getElementById(inputField);
  var parentLI = obj.parentNode;
  var parentUL = parentLI.parentNode;
  parentUL.removeChild(parentLI);

   // update the hidden input text
   Alfresco.util.updateMultiInputTextValue(hiddenField, signalChange);

   // if there's only one text field left, hide the remove image and link
   var elements = Selector.query('input[id^=' + hiddenField + '_]');

   if (elements.length == 1) {
     var element = elements[0];

     YUIDom.setStyle(element.id + "-image", "display", "none");
     YUIDom.setStyle(element.id + "-remove-link", "display", "none");
   }

   if (signalChange) {
      YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
   }
};

Alfresco.util.addMultiInputTextValue = function(hiddenField, fieldName, signalChange) {
  var elements = Selector.query('input[id^=' + hiddenField + '_]');

  // first create the new element
  if (elements !== null) {
    // get the last element
    var element = elements[elements.length - 1];
    parentLI = element.parentNode;
    var newLI = parentLI.cloneNode(true);

    var newInput = Selector.query('input', newLI);
    var newLink = Selector.query('a', newLI);
    var newImage = Selector.query('img', newLI);

    var inputId = "" + YUIDom.getAttribute(newInput, "id");
    var lastNumber = parseInt(inputId.replace(hiddenField+"_", ""));
    var newNumber = parseInt(inputId.replace(hiddenField+"_", "")) + 1;

    YUIDom.setAttribute(newInput, "id", hiddenField + "_" + newNumber);
    YUIDom.setAttribute(newInput, "name", fieldName + "_" + newNumber);
    YUIDom.setAttribute(newInput, "value", "");

    YUIDom.setAttribute(newLink, "id", hiddenField + "_" + newNumber + "-remove-link");

    YUIDom.setAttribute(newImage, "id", hiddenField + "_" + newNumber + "-image");
    var newImageOnClick = "" + YUIDom.getAttribute(newImage, "onclick");
    newImageOnClick = newImageOnClick.replace(hiddenField + "_" + lastNumber, hiddenField + "_" + newNumber);
    YUIDom.setAttribute(newImage, "onclick", newImageOnClick);

    parentLI.parentNode.appendChild(newLI);

    // give the newly created element focus
    document.getElementById(hiddenField + "_" + newNumber).focus();
  }

  // if there's more than one text field, show the remove image and link
  // if there's only one text field left, hide the remove image and link
  var elements = Selector.query('input[id^=' + hiddenField + '_]');

  if (elements.length > 1) {
    for (var x = 0; x < elements.length; x++) {
      var element = elements[x];

      YUIDom.setStyle(element.id + "-image", "display", "inline");
      YUIDom.setStyle(element.id + "-remove-link", "display", "inline");
    }
  }

  if (signalChange) {
     YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
  }
};