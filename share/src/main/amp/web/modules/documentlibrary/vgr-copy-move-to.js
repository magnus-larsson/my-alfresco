// @overridden projects/slingshot/source/web/modules/documentlibrary/copy-move-to.js

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
 * Document Library "Copy- and Move-To" module for Document Library.
 * 
 * @namespace Alfresco.module
 * @class Alfresco.module.DoclibCopyMoveTo
 */
(function()
{
   
   var Dom = YAHOO.util.Dom;
   
   var isEmpty = function(ob){
         for(var i in ob){ if(ob.hasOwnProperty(i)){return false;}}
         return true;
   }; 
   
   
   Alfresco.module.DoclibCopyMoveTo = function(htmlId)
   {
      Alfresco.module.DoclibCopyMoveTo.superclass.constructor.call(this, htmlId);

      // Re-register with our own name
      this.name = "Alfresco.module.DoclibCopyMoveTo";
      Alfresco.util.ComponentManager.reregister(this);


      this.options = YAHOO.lang.merge(this.options,
      {
         allowedViewModes:
         [
            Alfresco.module.DoclibGlobalFolder.VIEW_MODE_SITE,
            Alfresco.module.DoclibGlobalFolder.VIEW_MODE_REPOSITORY,
            Alfresco.module.DoclibGlobalFolder.VIEW_MODE_USERHOME
         ],
         extendedTemplateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/documentlibrary/copy-move-to",
         checkInheritanceUrl: Alfresco.constants.PROXY_URI + "vgr/inheritance/check",
         inheritUrl: Alfresco.constants.PROXY_URI + "vgr/inheritance/inherit"
      });

      return this;
   };

   YAHOO.extend(Alfresco.module.DoclibCopyMoveTo, Alfresco.module.DoclibGlobalFolder,
   {
      /**
       * Set multiple initialization options at once.
       *
       * @method setOptions
       * @override
       * @param obj {object} Object literal specifying a set of options
       * @return {Alfresco.module.DoclibMoveTo} returns 'this' for method chaining
       */
      setOptions: function DLCMT_setOptions(obj)
      {
         var myOptions = {};

         if (typeof obj.mode !== "undefined")
         {
            var dataWebScripts =
            {
               copy: "copy-to",
               move: "move-to"
            };
            if (typeof dataWebScripts[obj.mode] == "undefined")
            {
               throw new Error("Alfresco.module.CopyMoveTo: Invalid mode '" + obj.mode + "'");
            }
            myOptions.dataWebScript = dataWebScripts[obj.mode];
         }
         
         myOptions.viewMode = Alfresco.util.isValueSet(this.options.siteId) ? Alfresco.module.DoclibGlobalFolder.VIEW_MODE_SITE : Alfresco.module.DoclibGlobalFolder.VIEW_MODE_REPOSITORY;
         // Actions module
         this.modules.actions = new Alfresco.module.DoclibActions();

         return Alfresco.module.DoclibCopyMoveTo.superclass.setOptions.call(this, YAHOO.lang.merge(myOptions, obj));
      },

      /**
       * Event callback when superclass' dialog template has been loaded
       *
       * @method onTemplateLoaded
       * @override
       * @param response {object} Server response from load template XHR request
       */
      onTemplateLoaded: function DLCMT_onTemplateLoaded(response)
      {
         // Load the UI template, which only will bring in new i18n-messages, from the server
         Alfresco.util.Ajax.request(
         {
            url: this.options.extendedTemplateUrl,
            dataObj:
            {
               htmlid: this.id
            },
            successCallback:
            {
               fn: this.onExtendedTemplateLoaded,
               obj: response,
               scope: this
            },
            failureMessage: "Could not load 'copy-move-to' template:" + this.options.extendedTemplateUrl,
            execScripts: true
         });
      },

      /**
       * Event callback when this class' template has been loaded
       *
       * @method onExtendedTemplateLoaded
       * @override
       * @param response {object} Server response from load template XHR request
       */
      onExtendedTemplateLoaded: function DLCMT_onExtendedTemplateLoaded(response, superClassResponse)
      {
         // Now that we have loaded this components i18n messages let the original template get rendered.
         Alfresco.module.DoclibCopyMoveTo.superclass.onTemplateLoaded.call(this, superClassResponse);
      },

      /**
       * YUI WIDGET EVENT HANDLERS
       * Handlers for standard events fired from YUI widgets, e.g. "click"
       */

      /**
       * Dialog OK button event handler
       *
       * @method onOK
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onOK: function DLCMT_onOK(e, p_obj)
      {
         var files, multipleFiles = [], i, j;
         var target = this.selectedNode.data.nodeRef;

         // Single/multi files into array of nodeRefs
         if (YAHOO.lang.isArray(this.options.files)) {
            files = this.options.files;
         } else {
            files = [this.options.files];
         }
         
         for (i = 0, j = files.length; i < j; i++) {
            multipleFiles.push(files[i].node.nodeRef);
         }
         
         //check if any metadata on target is possibly inherited
         //and if so, if it will overwrite any previous metadata
         Alfresco.util.Ajax.jsonPost({
            url: this.options.checkInheritanceUrl,
            dataObj: { refs: multipleFiles, target: target },
            successCallback: {
                  fn: function(data) {
                           if (data.json.intersect && data.json.intersect.length > 0) {
                              //Present user with a warning dialog                       
                              this._showIntersectDialog(multipleFiles,target,data.json.intersect);
                           } else {
                              //else just do the copy/move
                              this._execute(multipleFiles,target);
                           }
                  },
                  scope: this
            },
            failureMessage: "Error while checking possible property inheritance clashes"
         });
      },
      
      _showIntersectDialog: function DLCMT__showIntersectDialog(multipleFiles,target,intersecting) {
         this.widgets.dialog.hide();
         
         //some values are dates
         var humanize = function(v){
            if (v && v.milliseconds) {
               var d = new Date(v.milliseconds);
               return YAHOO.util.Date.format(d,{format:'%Y-%m-%d %H:%M'});
            }
            return v;
         };
         
         //create html for dialog
         var html = ['<div id="copy-move-to"><p>',this.msg('message.intersecting'),'</p><div id="copy-move-intersectionlist"><form id="intersection-form">'];
         
         for (var i=0; i<intersecting.length;i++) {
                      
            var f = intersecting[i];
            
            html.push('<table class="copy-move-intersecting"><thead><tr class="name"><th colspan="4">');
            html.push(this.msg('label.document.name')+' ');
            html.push(f.name);
            html.push('</th></tr><tr class="labels"><th class="metadata label-name" colspan="2">');
            html.push(this.msg('label.property'));
            html.push('</th><th class="label-old">');
            html.push(this.msg('label.oldvalue'));
            html.push('</th><th class="label-new">');
            html.push(this.msg('label.newvalue'));
            html.push('</th></tr></thead><tbody>');
            
            //translate
            var prs = f.props;
            for (var j=0; j<prs.length; j++) {
               prs[j].translated = this.msg('vgr.'+prs[j].name.substring(34));
            }
            
            //...and sort
            var prs = prs.sort(function(a,b){  
               return a.translated.localeCompare(b.translated); 
            });
            
            //then create html
            for (var j=0;j<prs.length;j++) {
               html.push('<tr><td class="metadata"><input type="checkbox" name="');
               html.push(f.id); //save which document
               html.push('|||');
               html.push(prs[j].name);///...and what prop
               html.push('" checked /></td><td class="metadata">');
               html.push(prs[j].translated);
               html.push('</td><td>');
               html.push(humanize(prs[j].old));
               html.push('</td><td>');
               html.push(humanize(prs[j]['new']));
               html.push('</td></tr>');
            }
            html.push('</tbody></table>');
         }
         html.push('</form></div></div>');
         
         var me = this;
         Alfresco.util.PopupManager.displayPrompt({
            title: this.msg('title.intersecting'),
            text:  html.join(''),
            modal: true,
            noEscape: true,
            buttons: [{ 
                        text: this.msg("button.inherit"),
                        handler: function() {
                                    //get checkboxes                  
                                    var form = Dom.get('intersection-form');
                                    var boxes = form.elements;
                                    var filtered = {};
                                    for (var i=0;i<boxes.length; i++) {
                                       if (boxes[i].checked === true) {
                                          var parts = boxes[i].name.split('|||');
                                          
                                          if (!filtered[parts[0]]) {
                                            filtered[parts[0]] = [];
                                          }
                                          
                                          filtered[parts[0]].push(parts[1]);
                                       }
                                    }
                           
                                    this.destroy();
                                    //do the copy/move then set properties
                                    me._execute(multipleFiles,target,function(data){
                                      
                                      var results = data.json.results;
                                      if (results && data.json.overallSuccess) {
                                        
                                        //if it's a copy we've changed nodeRefs, otherwise we can just use the ones we have
                                        if (this.options.mode === 'copy') {
                                          
                                          var copy = {};
                                          for (var i=0;i<results.length; i++) {
                                            var from = results[i].from;
                                            if (from && filtered[from]) {
                                              copy[results[i].nodeRef] = filtered[from];
                                            }
                                          }
                                          me._inheritProperties(target,copy);
                                          
                                        } else {
                                          //a move
                                          me._inheritProperties(target,filtered);
                                        }
                                      }
                                      
                                      
                                    })
                                    
                                 },
                        isDefualt: true
                      },
                      {
                        text: this.msg("button.cancel"),
                        handler: function() {
                                    this.destroy();
                                 }
                      }]
         });
         
         //IE7 width fix
         Dom.setStyle('prompt','width','926px');
      },
      
      /**
       * Do an ajax request to set inheritance of choosen parameters
       * @method _inheritProperties
       * @param inherited {array} list of files and their props that should be inherited
       */
      _inheritProperties: function(target,inherited,callback) {
        
         //no props to inherit? then just copy/move
         if (!inherited || isEmpty(inherited)) { 
            return;         
         }
         
         var me = this;
         var data = { target: target, props: inherited };
         
         //if this is a move we wan't to bump the version otherwise disableBehaviours
         if (this.options.mode !== 'move') {
            data['disableBehaviours'] = true;
         }
         
         //issue request to inherit props
          Alfresco.util.Ajax.jsonPost({
            url: this.options.inheritUrl,
            dataObj: data,
            successCallback: {
                  fn: function(data) {
                              if (callback) {
                                callback.call(me,data);
                              }
                              //refresh page
                              YAHOO.Bubbling.fire("metadataRefresh")
                              
                  },
                  scope: this
            },
            failureMessage: "Error while copying properties"
         });
      },
      
      
      /**
       * Actually execute the copy/move
       * @method _execute
       * @param multipleFiles {array} List of nodeRefs 
       */
      _execute: function DLCMT__execute(multipleFiles,target,callback) { 
         var  eventSuffix = { copy: "Copied", move: "Moved" };
         var me = this;
         
         // Success callback function
         var fnSuccess = function DLCMT__onOK_success(p_data)
         {
            var result,
               successCount = p_data.json.successCount,
               failureCount = p_data.json.failureCount;

            this.widgets.dialog.hide();

            // Did the operation succeed?
            if (!p_data.json.overallSuccess)
            {
               Alfresco.util.PopupManager.displayMessage(
               {
                  text: this.msg("message.failure")
               });
               return;
            }

            //do callback
            if (callback) {
              callback.call(me,p_data);
            }

            YAHOO.Bubbling.fire("files" + eventSuffix[this.options.mode],
            {
               destination: this.currentPath,
               successCount: successCount,
               failureCount: failureCount
            });
            
            for (var i = 0, j = p_data.json.totalResults; i < j; i++)
            {
               result = p_data.json.results[i];
               
               if (result.success)
               {
                  YAHOO.Bubbling.fire((result.type == "folder" ? "folder" : "file") + eventSuffix[this.options.mode],
                  {
                     multiple: true,
                     nodeRef: result.nodeRef,
                     destination: this.currentPath
                  });
               }
            }

            Alfresco.util.PopupManager.displayMessage(
            {
               text: this.msg("message.success", successCount)
            });
            
            YAHOO.Bubbling.fire("metadataRefresh");
         };

         // Failure callback function
         var fnFailure = function DLCMT__onOK_failure(p_data)
         {
            this.widgets.dialog.hide();

            Alfresco.util.PopupManager.displayMessage(
            {
               text: this.msg("message.failure")
            });
         };

         // Construct webscript URI based on current viewMode
         var webscriptName = this.options.dataWebScript + "/node/{nodeRef}",
            nodeRef = new Alfresco.util.NodeRef(target);
         
         // Construct the data object for the genericAction call
         this.modules.actions.genericAction(
         {
            success:
            {
               callback:
               {
                  fn: fnSuccess,
                  scope: this
               }
            },
            failure:
            {
               callback:
               {
                  fn: fnFailure,
                  scope: this
               }
            },
            webscript:
            {
               method: Alfresco.util.Ajax.POST,
               name: webscriptName,
               params:
               {
                  nodeRef: nodeRef.uri
               }
            },
            wait:
            {
               message: this.msg("message.please-wait")
            },
            config:
            {
               requestContentType: Alfresco.util.Ajax.JSON,
               dataObj:
               {
                  nodeRefs: multipleFiles,
                    parentId: this.options.parentId
               }
            }
         });
         
         this.widgets.okButton.set("disabled", true);
         this.widgets.cancelButton.set("disabled", true);
      },

      /**
       * Gets a custom message depending on current view mode
       * and use superclasses
       *
       * @method msg
       * @param messageId {string} The messageId to retrieve
       * @return {string} The custom message
       * @override
       */
      msg: function DLCMT_msg(messageId)
      {
         var result = Alfresco.util.message.call(this, this.options.mode + "." + messageId, this.name, Array.prototype.slice.call(arguments).slice(1));
         if (result ==  (this.options.mode + "." + messageId))
         {
            result = Alfresco.util.message.call(this, messageId, this.name, Array.prototype.slice.call(arguments).slice(1))
         }
         if (result == messageId)
         {
            result = Alfresco.util.message(messageId, "Alfresco.module.DoclibGlobalFolder", Array.prototype.slice.call(arguments).slice(1));
         }
         return result;
      },

      
      /**
       * PRIVATE FUNCTIONS
       */

      /**
       * Internal show dialog function
       * @method _showDialog
       * @override
       */
      _showDialog: function DLCMT__showDialog()
      {
         this.widgets.okButton.set("label", this.msg("button"));
         return Alfresco.module.DoclibCopyMoveTo.superclass._showDialog.apply(this, arguments);
      }
   });

   /* Dummy instance to load optional YUI components early */
   var dummyInstance = new Alfresco.module.DoclibCopyMoveTo("null");
})();