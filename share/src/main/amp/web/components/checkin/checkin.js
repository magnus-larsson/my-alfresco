/**
 * CheckIn component Inherits Alfresco.FileUpload Use this when a file dialog for checkin is needed.
 * 
 * Dependencies: file-upload.js, flash-upload.js, html-upload.js and their templates and CSS
 * 
 */
(function() {

   var Dom = YAHOO.util.Dom;
   var isObject = YAHOO.lang.isObject;
   var isFunction = YAHOO.lang.isFunction;

   if (!Alfresco.thirdparty) {
      Alfesco.thirdparty = {};
   }

   /**
    * CheckIn constructor.
    * 
    * CheckIn is, as FileUpload, considered a singleton so constructor should be treated as private, please use Alfresco.thirdparty.getCheckInInstance() or Alfresco.thirdparty.createCheckInButton()
    * 
    * 
    * @param {string}
    *           htmlId The HTML id of the parent element
    * @return {Alfresco.thirdparty.CheckIn} The new CheckIn instance
    * @constructor
    * @private
    */
   Alfresco.thirdparty.CheckIn = function(instanceId) {
      Alfresco.thirdparty.CheckIn.superclass.constructor.call(this, instanceId);
      this.msg_scope = this.name;
      return this;
   };

   YAHOO.extend(Alfresco.thirdparty.CheckIn, Alfresco.component.Base, {

      uploadersReady : false,
      msg_scope : null,
      uploader : null,
      callbacks : {},

      /**
       * createCheckInButton takes a node and makes a YUI button of it that will open a fileupload dialog for checking in of supplied docuemt (the nodeRef)
       * 
       * @param el
       *           {string|DOMNode} the domnode of the button
       * @param nodeRef
       *           {string} nodeRef of destination document (i.e. where to upload)
       * @param displayName
       *           {string} display name of file (i.e. regular file name)
       * @param success
       *           {function} success callback function (nodeRef,filename,data)
       * @param failure
       *           {function} (optional) failure callback function (nodeRef,filename,cause,data), if not specified a generic failure dialog is presented
       * @param scope
       *           {object} (optional) scope for callbacks
       * @return {object} the YUIButton
       */
      createCheckInButton : function(el, record, success, failure, scope) {
         var me = this;

         return Alfresco.util.createYUIButton(this, '', function() {
            me.checkIn(record, success, failure, scope);
         }, {}, el);
      },

      /**
       * Opens a fileupload dialog for checking in of supplied docuemt (the nodeRef)
       * 
       * @param nodeRef
       *           {string} nodeRef of destination document (i.e. where to upload)
       * @param displayName
       *           {string} display name of file (i.e. regular file name)
       * @param success
       *           {function} success callback function (nodeRef,filename,data)
       * @param failure
       *           {function} (optional) failure callback function (nodeRef,filename,cause,data), if not specified a generic failure dialog is presented
       * @param scope
       *           {object} (optional) scope for callbacks
       * @param options
       *           {object} (optional) extra show configurations
       * @return {object} the YUIButton
       */
      checkIn : function(record, success, failure, scope, options) {
         var me = this;
         var displayName = record.displayName,
            nodeRef = record.nodeRef,
            version = record.version;

         if (!this.fileUpload)
         {
            this.fileUpload = Alfresco.getFileUploadInstance();
         }

         // Show uploader for multiple files
         var description = this.msg("label.filter-description", displayName),
            extensions = "*";

         if (displayName && new RegExp(/[^\.]+\.[^\.]+/).exec(displayName))
         {
            // Only add a filtering extension if filename contains a name and a suffix
            extensions = "*" + displayName.substring(displayName.lastIndexOf("."));
         }

         if (record.workingCopy && record.workingCopy.workingCopyVersion)
         {
            version = record.workingCopy.workingCopyVersion;
         }

         var zIndex = 0;
         if (this.fullscreen !== undefined && ( this.fullscreen.isWindowOnly || Dom.hasClass(this.id, 'alf-fullscreen')))
         {
            zIndex = 1000;
         } 

         var singleUpdateConfig =
         {
            updateNodeRef: nodeRef.toString(),
            updateFilename: displayName,
            updateVersion: version,
            overwrite: true,
            filter: [
            {
               description: description,
               extensions: extensions
            }],
            mode: this.fileUpload.MODE_SINGLE_UPDATE,
            onFileUploadComplete:
            {
               fn: this.onUploadCompleteData,
               scope: this
            },
            htmlUploadURL : "vgr/preupload.html",
            flashUploadURL : "vgr/preupload",
         };

         this.fileUpload.options.zIndex = zIndex;

         // store current callbacks, used in onUploadCompleteData
         var scope = isObject(scope) ? scope : (!isFunction(failure) && isObject(failure) ? failure : this);

         this.callbacks.success = function(json, data, res) {
            success.call(scope, res.json.nodeRef, data.fileName, data);
         };

         this.callbacks.failure = YAHOO.lang.isFunction(failure) ? function(cause, data) {
            failure.call(scope, nodeRef, data.fileName, cause, data);
         } : function() {
            me.failure();
         }

       
         this.fileUpload.show(singleUpdateConfig);
      },

      /**
       * File Upload complete event handler
       * 
       * @method onUploadCompleteData
       * @param e
       *           {object} event object or json data
       */
      onUploadCompleteData : function(e) {
         var me = this;
         var json = null;

         
         if (e.successful!==undefined && e.successful.length >= 1) {
            //Try DND-data first
            var uploader = this.fileUpload.uploader;
            if (uploader.fileStore!==undefined && uploader.fileStore["file0"]!==undefined) {
              if (uploader.fileStore["file0"].request !== undefined) {
                //DND uploader
                json = Alfresco.util.parseJSON(uploader.fileStore["file0"].request.responseText);
              } else if (uploader.fileStore["file0"].rawJson !== undefined) {
                //Flash uploader
                json = uploader.fileStore["file0"].rawJson;
              } 
            } else if (e.successful[0].response!==undefined) {
              json = e.successful[0].response;
            }  
         } 

         // close window
         this.close();

         if (json) {
            var result = json.status;
            
            var scb = function(data, res) {
               me.callbacks.success(json, data, res);
            }

            if (result === 'match' || result === 'nometadata') {
               // we have a match or a document that doesn't support metadata, just confirm upload
               this.confirmUpload(json, scb);
            } else if (result === 'nomatch') {
               // no match on noderef, display a dialog and as the user what she wants
               Alfresco.util.PopupManager.displayPrompt({
                  title : this.msg('checkin.nomatch.title'),
                  text : this.msg('checkin.nomatch.text'),
                  noEscape : true,
                  modal : true,
                  buttons : [ {
                     text : this.msg("checkin.button.upload.anyway"),
                     handler : function() {
                        this.destroy();
                        me.confirmUpload(json, scb);
                     }
                  }, {
                     text : this.msg("button.cancel"),
                     handler : function() {
                        this.destroy();
                     },
                     isDefault : true
                  } ]
               });

            } else {
               // unkown failure
               this.callbacks.failure(json.result, json);
            }

         } else {
            // probably something really wrong...
            this.callbacks.failure('unknown', e.data)
         }

      },

      close : function() {
        var uploader = this.fileUpload.uploader;
         // FIXME: implement html
         if (uploader && uploader.name === 'Alfresco.FlashUpload') {
            // Remove all files and references for this upload "session"
            //uploader._clear();

            // Hide the panel
            uploader.hide();

            // Hide the Flash movie
            //Dom.addClass(this.uploader.id + "-flashuploader-div", "hidden");

            // Disable the Esc key listener
            //uploader.widgets.escapeListener.disable();
         }
      },

      /**
       * Generic failure dialog
       */
      failure : function() {
         Alfresco.util.PopupManager.displayPrompt({
            title : this.msg('checkin.failure.title'),
            text : this.msg('checkin.failure.text'),
            modal : true,
            buttons : [ {
               text : this.msg("ok"),
               handler : function() {
                  this.destroy();
               }
            } ]
         });
      },

      /**
       * Sends an ajax post to "confirm" that the preuploaded file should indeed be stored
       * 
       * @param data
       *           {object} post data for upload { updateNodeRef: {string} the noderef filename: {string} filename returned from preupload tempFilename: {string} tempfilename returned from preupload
       *           majorVersion: {boolean} is this a major version bump? }
       * @param callback
       *           {function} callback called when it's done
       */
      confirmUpload : function(data, callback) {
         var popup = Alfresco.util.PopupManager.displayMessage({
            text : this.msg("checkin.msg.confirming"),
            spanClass : "wait",
            displayTime : 0
         // infinite
         });

         Alfresco.util.Ajax.request({
            url : Alfresco.constants.PROXY_URI + 'vgr/preupload/confirm',
            method : Alfresco.util.Ajax.POST,
            requestContentType : Alfresco.util.Ajax.JSON,
            responseContentType : Alfresco.util.Ajax.JSON,
            dataObj : data,
            successCallback : {
               fn : function(res) {
                  popup.hide();
                  
                  callback.call(this, data, res);
                  
                  if (!this.options.suppressRefreshEvent) {
                     YAHOO.Bubbling.fire("metadataRefresh");
                  }
               },
               scope : this
            },
            failureCallback : {
               fn : function(res) {
                  popup.hide();
                  if (window.console) {
                     console.log(YAHOO.lang.dump(res));
                  }

               },
               scope : this
            },
            failureMessage : this.msg('checkin.msg.confirming.failed'),
            execScripts : true
         });
      },

      sameExtensionAs : function(displayName) {
         var description = this.msg("label.filter-description", displayName);
         var extensions = "*"; // default to all

         if (displayName && new RegExp(/[^\.]+\.[^\.]+/).exec(displayName)) {
            // Only add a filtering extension if filename contains a name and a suffix
            extensions = "*" + displayName.substring(displayName.lastIndexOf("."));
         }

         return [ {
            description : description,
            extensions : extensions
         } ];
      },

      msg : function() {
         var args = Array.prototype.slice.call(arguments);
         args.splice(1, 0, this.msg_scope);
         return Alfresco.util.message.apply(Alfresco.util.message, args);
      },

      cancelEditing : function(nodeRef, callback) {
         var popup = Alfresco.util.PopupManager.displayMessage({
            text : this.msg("checkin.msg.cancel.editing"),
            spanClass : "wait",
            displayTime : 0
         // infinite
         });

         nodeRef = new Alfresco.util.NodeRef(nodeRef);

         Alfresco.util.Ajax.request({
            url : Alfresco.constants.PROXY_URI + "slingshot/doclib/action/cancel-checkout/node/" + nodeRef.uri,
            method : Alfresco.util.Ajax.POST,
            requestContentType : Alfresco.util.Ajax.JSON,
            responseContentType : Alfresco.util.Ajax.JSON,
            dataObj : {
               nodeRef : nodeRef
            },
            successCallback : {
               fn : function(res) {
                  popup.hide();
                  callback.call(this, nodeRef, res);
               },
               scope : this
            },
            failureCallback : {
               fn : function(res) {
                  popup.hide();
                  if (window.console) {
                     console.log(YAHOO.lang.dump(res));
                  }
               },
               scope : this
            },
            failureMessage : this.msg('checkin.msg.cancel.failed'),
            execScripts : true
         });
      },

      createCancelEditingButton : function(el, nodeRef, success, scope) {
         var me = this;
         return Alfresco.util.createYUIButton(this, '', function() {
            me.cancelEditing(nodeRef, function() {
               success.apply(scope, arguments);
            });
         }, {}, el);
      }

   }, true);

   Alfresco.thirdparty.getCheckInInstance = function() {
      var instanceId = "alfresco-checkin-instance";
      return Alfresco.util.ComponentManager.get(instanceId) || new Alfresco.thirdparty.CheckIn(instanceId);
   };

   /**
    * See Alfresco.thirdparty.CheckIn.createCheckInButton (above)
    * 
    */
   Alfresco.thirdparty.createCheckInButton = function() {
      var instance = Alfresco.thirdparty.getCheckInInstance();
      instance.createCheckInButton.apply(instance, arguments);
   };

   Alfresco.thirdparty.createCancelEditingButton = function() {
      var instance = Alfresco.thirdparty.getCheckInInstance();
      instance.createCancelEditingButton.apply(instance, arguments);
   };

   /**
    * onActionUploadNewVersion
    */
   Alfresco.thirdparty.onActionCheckInNewVersion = function(record, msg_scope, callback, scope, options) {
      var upload = Alfresco.thirdparty.getCheckInInstance();
      //upload.setOptions(options);

      // do a little dance with the message scope to get messages working
      if (msg_scope) {
         upload.msg_scope = msg_scope;
      }

      upload.checkIn(record, callback, null, scope, options);

   }

})();
