// @overridden projects/slingshot/source/web/components/documentlibrary/actions.js

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
 * Document Library Actions module
 *
 * @namespace Alfresco.doclib
 * @class Alfresco.doclib.Actions
 */
(function () {

   var Cookie   = YAHOO.util.Cookie;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML, $combine = Alfresco.util.combinePaths, $siteURL = Alfresco.util.siteURL;

   /**
    * Alfresco.doclib.Actions implementation
    */
   Alfresco.doclib.Actions = {};
   Alfresco.doclib.Actions.prototype = {
      /**
       * Asset metadata.
       *
       * @override
       * @method onActionDetails
       * @param asset {object} Object literal representing one file or folder to be actioned
       */
      onActionDetails: function dlA_onActionDetails(asset) {
         var scope = this;

         // Intercept before dialog show
         var doBeforeDialogShow = function dlA_onActionDetails_doBeforeDialogShow(p_form, p_dialog) {
            // Dialog title
            var fileSpan = '<span class="light">' + $html(asset.displayName) + '</span>';

            Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle", scope.msg("edit-details.title", fileSpan) ]);

            // Edit metadata link button
            this.widgets.editMetadata = Alfresco.util.createYUIButton(p_dialog, "editMetadata", null, {
               type: "link",
               label: scope.msg("edit-details.label.edit-metadata"),
               href: $siteURL("edit-metadata?nodeRef=" + asset.nodeRef)
            });
         };

         var templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&destination={destination}&mode={mode}&submitType={submitType}&formId={formId}&showCancelButton=true", {
            itemKind: "node",
            itemId: asset.nodeRef,
            mode: "edit",
            submitType: "json",
            formId: "doclib-simple-metadata"
         });

         // Using Forms Service, so always create new instance
         var editDetails = new Alfresco.module.SimpleDialog(this.id + "-editDetails-" + Alfresco.util.generateDomId());

         editDetails.setOptions({
            width: "40em",
            templateUrl: templateUrl,
            actionUrl: null,
            destroyOnHide: true,
            doBeforeDialogShow: {
               fn: doBeforeDialogShow,
               scope: this
            },
            onSuccess: {
               fn: function dlA_onActionDetails_success(response) {
                  // Reload the node's metadata
                  var nodeRef = asset.custom && asset.custom.isWorkingCopy ? asset.custom.workingCopyOriginal : asset.nodeRef;
                  Alfresco.util.Ajax.request({
                     url: Alfresco.constants.PROXY_URI + "slingshot/doclib/node/" + new Alfresco.util.NodeRef(nodeRef).uri,
                     successCallback: {
                        fn: function dlA_onActionDetails_refreshSuccess(response) {
                           var file = response.json.item;

                           // Fire "renamed" event
                           YAHOO.Bubbling.fire(asset.type == "folder" ? "folderRenamed" : "fileRenamed", {
                              file: file
                           });

                           // Fire "metadataRefresh" event so list is refreshed since rules might have been triggered on update
                           YAHOO.Bubbling.fire("metadataRefresh");

                           // Fire "tagRefresh" event
                           YAHOO.Bubbling.fire("tagRefresh");

                           // Display success message
                           Alfresco.util.PopupManager.displayMessage({
                              text: this.msg("message.details.success")
                           });
                        },
                        scope: this
                     },
                     failureCallback: {
                        fn: function dlA_onActionDetails_refreshFailure(response) {
                           Alfresco.util.PopupManager.displayMessage({
                              text: this.msg("message.details.failure")
                           });
                        },
                        scope: this
                     }
                  });
               },
               scope: this
            },
            onFailure: {
               fn: function dLA_onActionDetails_failure(response) {
                  Alfresco.util.PopupManager.displayMessage({
                     text: this.msg("message.details.failure")
                  });
               },
               scope: this
            }
         }).show();
      },

      /**
       * Locate folder.
       *
       * @method onActionLocate
       * @param asset {object} Object literal representing one file or folder to be actioned
       */
      onActionLocate: function dlA_onActionLocate(asset) {
         var path = asset.isFolder ? Alfresco.util.combinePaths("/", asset.location.path.substring(0, asset.location.path.lastIndexOf("/"))) : asset.location.path, file = asset.isLink ? asset.linkedDisplayName : asset.displayName;

         if (this.options.workingMode === Alfresco.doclib.MODE_SITE && asset.location.site !== this.options.siteId) {
            window.location = $siteURL("documentlibrary?file=" + encodeURIComponent(file) + "&path=" + encodeURIComponent(path), {
               site: asset.location.site
            });
         } else {
            this.options.highlightFile = file;

            // Change active filter to path
            YAHOO.Bubbling.fire("changeFilter", {
               filterId: "path",
               filterData: path
            });
         }
      },

      /**
       * Delete asset.
       *
       * @method onActionDelete
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionDelete: function dlA_onActionDelete(asset) {
         var me = this;

         Alfresco.util.PopupManager.displayPrompt({
            title: this.msg("message.confirm.delete.title"),
            text: this.msg("message.confirm.delete", asset.displayName),
            buttons: [
               {
                  text: this.msg("button.delete"),
                  handler: function dlA_onActionDelete_delete() {
                     this.destroy();
                     me._onActionDeleteConfirm.call(me, asset);
                  }
               },
               {
                  text: this.msg("button.cancel"),
                  handler: function dlA_onActionDelete_cancel() {
                     this.destroy();
                  },
                  isDefault: true
               }
            ]
         });
      },

      /**
       * Delete asset confirmed.
       *
       * @method _onActionDeleteConfirm
       * @param asset {object} Object literal representing the file or folder to be actioned
       * @private
       */
      _onActionDeleteConfirm: function dlA__onActionDeleteConfirm(asset) {
         var path = asset.location.path, fileName = asset.fileName, filePath = $combine(path, fileName), displayName = asset.displayName, nodeRef = new Alfresco.util.NodeRef(asset.nodeRef);

         this.modules.actions.genericAction({
            success: {
               activity: {
                  siteId: this.options.siteId,
                  activityType: "file-deleted",
                  page: "documentlibrary",
                  activityData: {
                     fileName: fileName,
                     path: path,
                     nodeRef: nodeRef.toString()
                  }
               },
               event: {
                  name: asset.isFolder ? "folderDeleted" : "fileDeleted",
                  obj: {
                     path: filePath
                  }
               },
               message: this.msg("message.delete.success", displayName)
            },
            failure: {
               message: this.msg("message.delete.failure", displayName)
            },
            webscript: {
               method: Alfresco.util.Ajax.DELETE,
               name: "file/node/{nodeRef}",
               params: {
                  nodeRef: nodeRef.uri
               }
            }
         });
      },

      /**
       * Edit Offline.
       * NOTE: Placeholder only, clients MUST implement their own editOffline action
       *
       * @method onActionEditOffline
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionEditOffline: function dlA_onActionEditOffline(asset) {
         Alfresco.logger.error("onActionEditOffline", "Abstract implementation not overridden");
      },

      /**
       * Valid online edit mimetypes, mapped to application ProgID.
       * Currently allowed are Microsoft Office 2003 and 2007 mimetypes for Excel, PowerPoint and Word only
       *
       * @property onlineEditMimetypes
       * @type object
       */
      onlineEditMimetypes:
      {
         "application/msword": "Word.Document",
         "application/vnd.openxmlformats-officedocument.wordprocessingml.document": "Word.Document",
         "application/vnd.ms-word.document.macroenabled.12": "Word.Document",
         "application/vnd.openxmlformats-officedocument.wordprocessingml.template": "Word.Document",
         "application/vnd.ms-word.template.macroenabled.12": "Word.Document",

         "application/vnd.ms-powerpoint": "PowerPoint.Slide",
         "application/vnd.openxmlformats-officedocument.presentationml.presentation": "PowerPoint.Slide",
         "application/vnd.ms-powerpoint.presentation.macroenabled.12": "PowerPoint.Slide",
         "application/vnd.openxmlformats-officedocument.presentationml.slideshow": "PowerPoint.Slide",
         "application/vnd.ms-powerpoint.slideshow.macroenabled.12": "PowerPoint.Slide",
         "application/vnd.openxmlformats-officedocument.presentationml.template": "PowerPoint.Slide",
         "application/vnd.ms-powerpoint.template.macroenabled.12": "PowerPoint.Slide",
         "application/vnd.ms-powerpoint.addin.macroenabled.12": "PowerPoint.Slide",
         "application/vnd.openxmlformats-officedocument.presentationml.slide": "PowerPoint.Slide",
         "application/vnd.ms-powerpoint.slide.macroEnabled.12": "PowerPoint.Slide",

         "application/vnd.ms-excel": "Excel.Sheet",
         "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": "Excel.Sheet",
         "application/vnd.openxmlformats-officedocument.spreadsheetml.template": "Excel.Sheet",
         "application/vnd.ms-excel.sheet.macroenabled.12": "Excel.Sheet",
         "application/vnd.ms-excel.template.macroenabled.12": "Excel.Sheet",
         "application/vnd.ms-excel.addin.macroenabled.12": "Excel.Sheet",
         "application/vnd.ms-excel.sheet.binary.macroenabled.12": "Excel.Sheet"
      },

      /**
       * Edit Online.
       *
       * @method onActionEditOnline
       * @param asset {object} Object literal representing file or folder to be actioned
       */
      onActionEditOnline: function dlA_onActionEditOnline(asset) {
         if (this._launchOnlineEditor(asset)) {
            YAHOO.Bubbling.fire("metadataRefresh");
         }
      },

      /**
       * Opens the appropriate Microsoft Office application for online editing.
       * Supports: Microsoft Office 2003, 2007 & 2010.
       *
       * @method Alfresco.util.sharePointOpenDocument
       * @param asset {object} Object literal representing file or folder to be actioned
       * @return {boolean} True if the action was completed successfully, false otherwise.
       */
      _launchOnlineEditor: function dlA__launchOnlineEditor(asset) {
         var controlProgID = "SharePoint.OpenDocuments",
            mimetype = asset.mimetype,
            appProgID = null,
            activeXControl = null,
            extensionMap =
            {
               doc: "application/msword",
               docx: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
               docm: "application/vnd.ms-word.document.macroenabled.12",
               dotx: "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
               dotm: "application/vnd.ms-word.template.macroenabled.12",

               ppt: "application/vnd.ms-powerpoint",
               pptx: "application/vnd.openxmlformats-officedocument.presentationml.presentation",
               pptm: "application/vnd.ms-powerpoint.presentation.macroenabled.12",
               ppsx: "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
               ppsm: "application/vnd.ms-powerpoint.slideshow.macroenabled.12",
               potx: "application/vnd.openxmlformats-officedocument.presentationml.template",
               potm: "application/vnd.ms-powerpoint.template.macroenabled.12",
               ppam: "application/vnd.ms-powerpoint.addin.macroenabled.12",
               sldx: "application/vnd.openxmlformats-officedocument.presentationml.slide",
               sldm: "application/vnd.ms-powerpoint.slide.macroEnabled.12",

               xls: "application/vnd.ms-excel",
               xlsx: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
               xltx: "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
               xlsm: "application/vnd.ms-excel.sheet.macroenabled.12",
               xltm: "application/vnd.ms-excel.template.macroenabled.12",
               xlam: "application/vnd.ms-excel.addin.macroenabled.12",
               xlsb: "application/vnd.ms-excel.sheet.binary.macroenabled.12"
            };

         // Try to resolve the asset to an application ProgID; by mimetype first, then file extension.
         if (this.onlineEditMimetypes.hasOwnProperty(mimetype)) {
            appProgID = this.onlineEditMimetypes[mimetype];
         } else {
            var extn = Alfresco.util.getFileExtension(asset.location.file);
            if (extn !== null) {
               extn = extn.toLowerCase();
               if (extensionMap.hasOwnProperty(extn)) {
                  mimetype = extensionMap[extn];
                  if (this.onlineEditMimetypes.hasOwnProperty(mimetype)) {
                     appProgID = this.onlineEditMimetypes[mimetype];
                  }
               }
            }
         }

         var onlineEditUrl = asset.onlineEditUrl;

         // extract the SiteMinder session cookie
         var smsessionid = this._readCookie("SMSESSION");

         // if there is a cookie, set it as a URL parameter
         if (smsessionid.length > 0) {
            if (onlineEditUrl.indexOf("?") >= 0) {
               onlineEditUrl += "&";
            } else {
               onlineEditUrl += "?";
            }

            onlineEditUrl += "SMSESSION=" + smsessionid[0];
         }

         if (appProgID !== null) {
            // Try each version of the SharePoint control in turn, newest first
            try {
               activeXControl = new ActiveXObject(controlProgID + ".3");

               this._launchOnlineEditorDisplayPrompt(function () {
                  return activeXControl.EditDocument3(window, onlineEditUrl, true, appProgID);
               });

               return;
            } catch (e) {
               try {
                  activeXControl = new ActiveXObject(controlProgID + ".2");

                  this._launchOnlineEditorDisplayPrompt(function () {
                     return activeXControl.EditDocument2(window, onlineEditUrl, appProgID);
                  });

                  return;
               } catch (e1) {
                  try {
                     activeXControl = new ActiveXObject(controlProgID + ".1");

                     this._launchOnlineEditorDisplayPrompt(function () {
                        return activeXControl.EditDocument(onlineEditUrl, appProgID);
                     });

                     return;
                  } catch (e2) {
                     // Do nothing
                  }
               }
            }
         }

         // No success in launching application via ActiveX control; launch the WebDAV URL anyway
         return window.open(onlineEditUrl, "_blank");
      },

      _readCookie: function(name) {
         var read = YAHOO.util.Cookie.get(name);

         read = read ? read.split(',') : [];

         return read;
      },

      _launchOnlineEditorDisplayPrompt: function (callback) {
         Alfresco.util.PopupManager.displayPrompt({
            title: this.msg("actions.document.edit-online"),
            text: "<span style='color: red'>" + this.msg("actions.document.edit-online.information") + "</span>",
            modal: true,
            noEscape: true,
            buttons: [
               {
                  text: this.msg("button.ok"),
                  handler: function () {
                     this.destroy();
                     callback();
                  },
                  isDefault: true
               }
            ]
         });
      },

      /**
       * Checkout to Google Docs.
       * NOTE: Placeholder only, clients MUST implement their own checkoutToGoogleDocs action
       *
       * @method onActionCheckoutToGoogleDocs
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionCheckoutToGoogleDocs: function dlA_onActionCheckoutToGoogleDocs(asset) {
         Alfresco.logger.error("onActionCheckoutToGoogleDocs", "Abstract implementation not overridden");
      },

      /**
       * Check in a new version from Google Docs.
       * NOTE: Placeholder only, clients MUST implement their own checkinFromGoogleDocs action
       *
       * @method onActionCheckinFromGoogleDocs
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionCheckinFromGoogleDocs: function dlA_onActionCheckinFromGoogleDocs(asset) {
         Alfresco.logger.error("onActionCheckinFromGoogleDocs", "Abstract implementation not overridden");
      },

      /**
       * Rules.
       *
       * @method onActionRules
       * @param assets {object} Object literal representing one or more file(s) or folder(s) to be actioned
       */
      onActionRules: function dlA_onActionRules(assets) {
         if (!this.modules.details) {
            this.modules.details = new Alfresco.module.DoclibDetails(this.id + "-details");
         }

         this.modules.details.setOptions({
            siteId: this.options.siteId,
            file: assets
         }).showDialog();
      },

      /**
       * Simple Workflow: Approve.
       *
       * @method onActionSimpleApprove
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionSimpleApprove: function dlA_onActionSimpleApprove(asset) {
         var displayName = asset.displayName;

         this.modules.actions.genericAction({
            success: {
               event: {
                  name: "metadataRefresh"
               },
               message: this.msg("message.simple-workflow.approved", displayName)
            },
            failure: {
               message: this.msg("message.simple-workflow.failure", displayName)
            },
            webscript: {
               method: Alfresco.util.Ajax.POST,
               stem: Alfresco.constants.PROXY_URI + "api/",
               name: "actionQueue"
            },
            config: {
               requestContentType: Alfresco.util.Ajax.JSON,
               dataObj: {
                  actionedUponNode: asset.nodeRef,
                  actionDefinitionName: "accept-simpleworkflow"
               }
            }
         });
      },

      /**
       * Simple Workflow: Reject.
       *
       * @method onActionSimpleReject
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionSimpleReject: function dlA_onActionSimpleReject(asset) {
         var displayName = asset.displayName;

         this.modules.actions.genericAction({
            success: {
               event: {
                  name: "metadataRefresh"
               },
               message: this.msg("message.simple-workflow.rejected", displayName)
            },
            failure: {
               message: this.msg("message.simple-workflow.failure", displayName)
            },
            webscript: {
               method: Alfresco.util.Ajax.POST,
               stem: Alfresco.constants.PROXY_URI + "api/",
               name: "actionQueue"
            },
            config: {
               requestContentType: Alfresco.util.Ajax.JSON,
               dataObj: {
                  actionedUponNode: asset.nodeRef,
                  actionDefinitionName: "reject-simpleworkflow"
               }
            }
         });
      },

      /**
       * Upload new version.
       *
       * @method onActionUploadNewVersion
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionUploadNewVersion: function dlA_onActionUploadNewVersion(asset) {
         var displayName = asset.displayName, nodeRef = new Alfresco.util.NodeRef(asset.nodeRef), version = asset.version;

         if (!this.fileUpload) {
            this.fileUpload = Alfresco.getFileUploadInstance();
         }

         // Show uploader for multiple files
         var description = this.msg("label.filter-description", displayName), extensions = "*";

         if (displayName && new RegExp(/[^\.]+\.[^\.]+/).exec(displayName)) {
            // Only add a filtering extension if filename contains a name and a suffix
            extensions = "*" + displayName.substring(displayName.lastIndexOf("."));
         }

         if (asset.custom && asset.custom.workingCopyVersion) {
            version = asset.custom.workingCopyVersion;
         }

         var singleUpdateConfig = {
            updateNodeRef: nodeRef.toString(),
            updateFilename: displayName,
            updateVersion: version,
            overwrite: true,
            filter: [
               {
                  description: description,
                  extensions: extensions
               }
            ],
            mode: this.fileUpload.MODE_SINGLE_UPDATE,
            onFileUploadComplete: {
               fn: this.onNewVersionUploadComplete,
               scope: this
            }
         };
         if (this.options.workingMode == Alfresco.doclib.MODE_SITE) {
            singleUpdateConfig.siteId = this.options.siteId;
            singleUpdateConfig.containerId = this.options.containerId;
         }
         this.fileUpload.show(singleUpdateConfig);
      },

      /**
       * Same as onActionUploadNewVersion, but performs an document id check.
       * User can still choose to overwrite.
       */
      onActionCheckInNewVersion: function (asset) {
         if (Alfresco.thirdparty && Alfresco.thirdparty.onActionCheckInNewVersion) {
            Alfresco.thirdparty.onActionCheckInNewVersion(asset, this.name, function (nodeRef, filename) {
               this.onNewVersionUploadComplete({ successful: [
                  { nodeRef: nodeRef, fileName: asset.displayName }
               ] });
            }, this, {});
         }
      },

      /**
       * Called from the uploader component after a the new version has been uploaded.
       *
       * @method onNewVersionUploadComplete
       * @param complete {object} Object literal containing details of successful and failed uploads
       */
      onNewVersionUploadComplete: function dlA_onNewVersionUploadComplete(complete) {
         var success = complete.successful.length, activityData, file;
         if (success > 0) {
            if (success < this.options.groupActivitiesAt || 5) {
               // Below cutoff for grouping Activities into one
               for (var i = 0; i < success; i++) {
                  file = complete.successful[i];
                  activityData = {
                     fileName: file.fileName,
                     nodeRef: file.nodeRef
                  };
                  this.modules.actions.postActivity(this.options.siteId, "file-updated", "document-details", activityData);
               }
            } else {
               // grouped into one message
               activityData = {
                  fileCount: success,
                  path: this.currentPath,
                  parentNodeRef: this.doclistMetadata.parent.nodeRef
               };
               this.modules.actions.postActivity(this.options.siteId, "files-updated", "documentlibrary", activityData);
            }
         }
      },

      /**
       * Cancel editing.
       *
       * @method onActionCancelEditing
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionCancelEditing: function dlA_onActionCancelEditing(asset) {
         var displayName = asset.displayName, nodeRef = new Alfresco.util.NodeRef(asset.nodeRef);

         this.modules.actions.genericAction({
            success: {
               event: {
                  name: "metadataRefresh"
               },
               message: this.msg("message.edit-cancel.success", displayName)
            },
            failure: {
               message: this.msg("message.edit-cancel.failure", displayName)
            },
            webscript: {
               method: Alfresco.util.Ajax.POST,
               name: "cancel-checkout/node/{nodeRef}",
               params: {
                  nodeRef: nodeRef.uri
               }
            }
         });
      },

      /**
       * Copy single document or folder.
       *
       * @method onActionCopyTo
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionCopyTo: function dlA_onActionCopyTo(asset) {
         this._copyMoveTo("copy", asset);
      },

      /**
       * Move single document or folder.
       *
       * @method onActionMoveTo
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionMoveTo: function dlA_onActionMoveTo(asset) {
         this._copyMoveTo("move", asset);
      },

      /**
       * Copy/Move To implementation.
       *
       * @method _copyMoveTo
       * @param mode {String} Operation mode: copy|move
       * @param asset {object} Object literal representing the file or folder to be actioned
       * @private
       */
      _copyMoveTo: function dlA__copyMoveTo(mode, asset) {
         // Check mode is an allowed one
         if (!mode in
         {
            copy: true,
            move: true
         }) {
            throw new Error("'" + mode + "' is not a valid Copy/Move to mode.");
         }

         if (!this.modules.copyMoveTo) {
            this.modules.copyMoveTo = new Alfresco.module.DoclibCopyMoveTo(this.id + "-copyMoveTo");
         }

         this.modules.copyMoveTo.setOptions({
            mode: mode,
            siteId: this.options.siteId,
            containerId: this.options.containerId,
            path: this.currentPath,
            files: asset,
            workingMode: this.options.workingMode,
            rootNode: this.options.rootNode,
            parentId: this.doclistMetadata.parent.nodeRef
         }).showDialog();
      },

      /**
       * Assign workflow.
       *
       * @method onActionAssignWorkflow
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionAssignWorkflow: function dlA_onActionAssignWorkflow(asset) {
         var nodeRefs = "", destination = null;

         if (YAHOO.lang.isArray(asset)) {
            var sameParent = true;
            for (var i = 0, il = asset.length; i < il; i++) {
               nodeRefs += (i === 0 ? "" : ",") + asset[i].nodeRef;
               if (sameParent && i > 0) {
                  sameParent = asset[i - 1].location.parent.nodeRef == asset[i].location.parent.nodeRef;
               }
            }
            if (sameParent && asset.length > 0) {
               destination = asset[i - 1].location.parent.nodeRef;
            } else {
               destination = this.doclistMetadata.container;
            }
         } else {
            nodeRefs = asset.nodeRef;
            destination = asset.location.parent.nodeRef;
         }
         var postBody = {
            selectedItems: nodeRefs
         };
         if (destination) {
            postBody.destination = destination;
         }
         Alfresco.util.navigateTo($siteURL("start-workflow"), "POST", postBody);
      },

      /**
       * Set permissions on a single document or folder.
       *
       * @method onActionManagePermissions
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionManagePermissions: function dlA_onActionManagePermissions(asset) {
         if (!this.modules.permissions) {
            this.modules.permissions = new Alfresco.module.DoclibPermissions(this.id + "-permissions");
         }

         this.modules.permissions.setOptions({
            siteId: this.options.siteId,
            containerId: this.options.containerId,
            path: this.currentPath,
            files: asset
         }).showDialog();
      },

      /**
       * Manage aspects.
       *
       * @method onActionManageAspects
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionManageAspects: function dlA_onActionManageAspects(asset) {
         if (!this.modules.aspects) {
            this.modules.aspects = new Alfresco.module.DoclibAspects(this.id + "-aspects");
         }

         this.modules.aspects.setOptions({
            file: asset
         }).show();
      },

      /**
       * Change Type
       *
       * @method onActionChangeType
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      onActionChangeType: function dlA_onActionChangeType(asset) {
         var nodeRef = asset.nodeRef, currentType = asset.nodeType, displayName = asset.displayName, actionUrl = Alfresco.constants.PROXY_URI + $combine("slingshot/doclib/type/node", nodeRef.replace(":/", ""));

         var doSetupFormsValidation = function dlA_oACT_doSetupFormsValidation(p_form) {
            // Validation
            p_form.addValidation(this.id + "-changeType-type", function fnValidateType(field, args, event, form, silent, message) {
               return field.options[field.selectedIndex].value !== "-";
            }, null, "change");
            p_form.setShowSubmitStateDynamically(true, false);
         };

         // Always create a new instance
         this.modules.changeType = new Alfresco.module.SimpleDialog(this.id + "-changeType").setOptions({
            width: "30em",
            templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/documentlibrary/change-type?currentType=" + encodeURIComponent(currentType),
            actionUrl: actionUrl,
            doSetupFormsValidation: {
               fn: doSetupFormsValidation,
               scope: this
            },
            firstFocus: this.id + "-changeType-type",
            onSuccess: {
               fn: function dlA_onActionChangeType_success(response) {
                  YAHOO.Bubbling.fire("metadataRefresh", {
                     highlightFile: displayName
                  });
                  Alfresco.util.PopupManager.displayMessage({
                     text: this.msg("message.change-type.success", displayName)
                  });
               },
               scope: this
            },
            onFailure: {
               fn: function dlA_onActionChangeType_failure(response) {
                  Alfresco.util.PopupManager.displayMessage({
                     text: this.msg("message.change-type.failure", displayName)
                  });
               },
               scope: this
            }
         });
         this.modules.changeType.show();
      },

      /**
       * View in source Repository URL helper
       *
       * @method viewInSourceRepositoryURL
       * @param asset {object} Object literal representing the file or folder to be actioned
       */
      viewInSourceRepositoryURL: function dlA_viewInSourceRepositoryURL(asset) {
         var nodeRef = asset.nodeRef, type = asset.type, repoId = asset.location.repositoryId, urlMapping = this.options.replicationUrlMapping, siteUrl;

         if (!repoId || !urlMapping || !urlMapping[repoId]) {
            return "#";
         }

         // Generate a URL to the relevant details page
         siteUrl = Alfresco.util.siteURL(type + "-details?nodeRef=" + nodeRef);
         // Strip off this webapp's context as the mapped one might be different
         siteUrl = siteUrl.substring(Alfresco.constants.URL_CONTEXT.length);

         return $combine(urlMapping[repoId], "/", siteUrl);
      },

      performDownload: function vgrActions_performDownload(displayName, downloadUrl, promptText, promptTextIE, alwaysPrompt) {
         if (YAHOO.env.ua.ie > 6 || alwaysPrompt) {
            // MSIE7 blocks the download and gets the wrong URL in the "manual download bar"
            Alfresco.util.PopupManager.displayPrompt({
               title: this.msg(promptText, displayName),
               text: this.msg(promptTextIE),
               buttons: [
                  {
                     text: this.msg("button.download"),
                     handler: function DocumentActions_oAEO_success_download() {
                        window.location = downloadUrl;
                        this.destroy();
                     },
                     isDefault: true
                  },
                  {
                     text: this.msg("button.close"),
                     handler: function DocumentActions_oAEO_success_close() {
                        this.destroy();
                     }
                  }
               ]
            });
         } else {
            Alfresco.util.PopupManager.displayMessage({
               text: this.msg(promptText, displayName)
            });
            // Kick off the download 3 seconds after the confirmation message
            YAHOO.lang.later(3000, this, function () {
               window.location = downloadUrl;
            });
         }
      },

      /**
       * Download
       *
       * @override
       * @method onActionDownload
       * @param asset {object} Object literal representing file or folder to be actioned
       */
      onActionDownload: function vgrActions_onActionDownload(asset) {
         var displayName = asset.displayName;

         if (asset.size == 0) {
            Alfresco.util.PopupManager.displayMessage({
               text: this.msg("message.download.nocontent", displayName)
            });

            return;
         }

         var downloadUrl = Alfresco.constants.PROXY_URI + asset.contentUrl + "?a=true";

         this.performDownload(displayName, downloadUrl, "message.download.success", "message.download.success.ie7");
      }

   };
})();
