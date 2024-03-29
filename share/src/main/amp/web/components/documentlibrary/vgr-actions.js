/*
 * @overridden projects/slingshot/source/web/components/documentlibrary/actions.js
 */

(function(renderAction) {

   var $html = Alfresco.util.encodeHTML;
   var $isValueSet = Alfresco.util.isValueSet;
   var $siteURL = Alfresco.util.siteURL;

   Alfresco.doclib.Actions.prototype.renderAction = function(p_action, p_record) {
      var urlContext = Alfresco.constants.URL_RESCONTEXT + "components/documentlibrary/actions/",
        iconStyle = 'style="background-image:url(' + urlContext + '{icon}-16.png)" ',
        actionTypeMarkup =
        {
          "link": '<div class="{id}"><a title="{label}" class="simple-link" href="{href}" ' + iconStyle + '{target}><span>{label}</span></a></div>',
          "pagelink": '<div class="{id}"><a title="{label}" class="simple-link" href="{pageUrl}" ' + iconStyle + '><span>{label}</span></a></div>',
          "javascript": '<div class="{id}" id="{jsfunction}"><a title="{label}" class="action-link" href="#"' + iconStyle + '><span>{label}</span></a></div>',
          "header" : '<div class="action-header"><span>{label}</span></div>'
        };

      // Store quick look-up for client-side actions
      p_record.actionParams[p_action.id] = p_action.params;

      var markupParams =
      {
         "id": p_action.id,
         "icon": p_action.icon,
         "label": $html(Alfresco.util.substituteDotNotation(this.msg(p_action.label), p_record))
      };

      // Parameter substitution for each action type
      if (p_action.type === "link")
      {
         if (p_action.params.href)
         {
            markupParams.href = Alfresco.util.substituteDotNotation(p_action.params.href, p_record);
            markupParams.target = p_action.params.target ? "target=\"" + p_action.params.target + "\"" : "";
         }
         else
         {
            Alfresco.logger.warn("Action configuration error: Missing 'href' parameter for actionId: ", p_action.id);
         }
      }
      else if (p_action.type === "pagelink")
      {
         if (p_action.params.page)
         {
            markupParams.pageUrl = Alfresco.util.substituteDotNotation(p_action.params.page, p_record);

            /**
             * If the page starts with a "{" character we're going to assume it's a placeholder variable
             * that will be resolved by the getActionsUrls() function. In which case, we do not want to
             * use the $siteURL() function here as that will result in a double-prefix.
             */
            if (p_action.params.page.charAt(0) !== "{")
            {
               var recordSiteName = $isValueSet(p_record.location.site) ? p_record.location.site.name : null;
               markupParams.pageUrl = $siteURL(markupParams.pageUrl,
               {
                  site: recordSiteName
               });
            }
         }
         else
         {
            Alfresco.logger.warn("Action configuration error: Missing 'page' parameter for actionId: ", p_action.id);
         }
      }
      else if (p_action.type === "javascript")
      {
         if (p_action.params["function"])
         {
            markupParams.jsfunction = p_action.params["function"];
         }
         else
         {
            Alfresco.logger.warn("Action configuration error: Missing 'function' parameter for actionId: ", p_action.id);
         }
      }

      return YAHOO.lang.substitute(actionTypeMarkup[p_action.type], markupParams);
   };

}(Alfresco.doclib.Actions.prototype.renderAction));

(function() {

   /**
    * Same as onActionUploadNewVersion, but performs an document id check. User can still choose to overwrite.
    */
   Alfresco.doclib.Actions.prototype.onActionCheckInNewVersion = function(asset) {
      if (Alfresco.thirdparty && Alfresco.thirdparty.onActionCheckInNewVersion) {
         Alfresco.thirdparty.onActionCheckInNewVersion(asset, this.name, function(nodeRef, filename) {
            this.onNewVersionUploadComplete({
               successful : [ {
                  nodeRef : nodeRef,
                  fileName : asset.displayName
               } ]
            });
         }, this, {});
      }
   };

   Alfresco.doclib.Actions.prototype.onActionCreateFinalVersion = function(asset) {
      var self = this;

      Alfresco.util.PopupManager.getUserInput({
         title : this.msg('actions.document.create-final-version'),
         text : this.msg('label.comments'),
         callback : {
            fn : function(comment) {
               Alfresco.util.PopupManager.displayMessage({
                  text : self.msg('message.create-final-version.waiting')
               });
               
               Alfresco.util.Ajax.jsonPost({
                  url : Alfresco.constants.PROXY_URI_RELATIVE + "se/vgregion/alfresco/nodes/createfinalversion",
                  dataObj : {
                     nodeRef : asset.nodeRef,
                     comment : comment
                  },
                  successCallback : {
                     fn : function() {
                        Alfresco.util.PopupManager.displayMessage({
                           text : self.msg('message.create-final-version.success')
                        });

                        YAHOO.Bubbling.fire("metadataRefresh", {});
                     },
                     scope : self
                  },
                  failureMessage : self.msg("message.create-final-version.failure"),
                  failureCallback : {
                     fn : function() {
                     },
                     scope : self
                  }
               });
            },
            scope : self
         }
      });
   };

   Alfresco.doclib.Actions.prototype.onActionRecreatePdfa = function(record, owner) {
      // Get action params
      var params = this.getAction(record, owner).params, displayName = record.displayName;

      // Deactivate action
      var ownerTitle = owner.title;
      owner.title = owner.title + "_deactivated";

      // Prepare genericAction config
      var config = {
         success : {
            callback : {
               fn : function() {
                  Alfresco.util.PopupManager.displayMessage({
                     text : this.msg("message.recreate-pdfa.success", record.nodeRef)
                  });
               },
               obj : record,
               scope : this
            }
         },
         failure : {
            callback : {
               fn : function(error) {
                  Alfresco.util.PopupManager.displayPrompt({
                     text : this.msg("message.recreate-pdfa.failure", record.nodeRef)
                  });
               },
               obj : record,
               scope : this
            }
         },
         webscript : {
            method : Alfresco.util.Ajax.POST,
            stem : Alfresco.constants.PROXY_URI + "api/",
            name : "actionQueue"
         },
         config : {
            requestContentType : Alfresco.util.Ajax.JSON,
            dataObj : {
               actionedUponNode : record.nodeRef,
               actionDefinitionName : 'vgr.action.recreate-pdfa'
            }
         },
         wait: {
            message: this.msg("message.recreate-pdfa.wait", record.nodeRef)
         }
      };

      // Execute the repo action
      this.modules.actions.genericAction(config);
   };

}());

(function (_actionEditOnlineInternal) {

   Alfresco.doclib.Actions.prototype._launchOnlineEditorDisplayPrompt = function(callback) {
      Alfresco.util.PopupManager.displayPrompt({
         title : this.msg("actions.document.edit-online"),
         text : "<span style='color: red'>" + this.msg("actions.document.edit-online.information") + "</span>",
         modal : true,
         noEscape : true,
         buttons : [ {
            text : this.msg("button.ok"),
            handler : function() {
               this.destroy();

               callback();
            },
            isDefault : true
         } ]
      });
   };


   Alfresco.doclib.Actions.prototype.actionEditOnlineInternal = function(record) {
      var me = this;
      this._launchOnlineEditorDisplayPrompt(function() {
         _actionEditOnlineInternal.call(me, record);
      });
   }
})(Alfresco.doclib.Actions.prototype.actionEditOnlineInternal)
