// @overridden projects/slingshot/source/web/components/site-members/site-members.js

(function(onReady) {

   var self = this;

   /**
    * Show All event handler
    * 
    * @method onShowAll
    */
   Alfresco.SiteMembers.prototype.onShowAll = function() {
      this._performSearch('');
   };

   /**
    * Reset password button custom datacell formatter
    * 
    * @method renderCellUninvite
    * @param elCell
    *           {object}
    * @param oRecord
    *           {object}
    * @param oColumn
    *           {object}
    * @param oData
    *           {object|string}
    */
   var renderCellResetPassword = function(elCell, oRecord, oColumn, oData) {
      Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

      var userName = oRecord.getData("userName");

      if (self.isCurrentUserSiteAdmin && self.options.currentUser !== userName && oRecord.getData("zone") === "internal") {
         // create HTML for representing buttons

         elCell.innerHTML = '<span id="' + self.id + '-button-resetPassword-' + userName + '"></span>';

         // create the reset password button
         var button = new YAHOO.widget.Button({
            container : self.id + '-button-resetPassword-' + userName,
            label : self.msg("site-members.resetPassword"),
            onclick : {
               fn : self.showResetPasswordDialog,
               obj : userName,
               scope : self
            }
         });

         // store the buttons
         self.buttons[userName + "-button-resetPassword"] = {
            button : button
         };
      } else {
         // output padding div so layout is not messed up due to missing buttons
         elCell.innerHTML = '<div></div>';
      }
   };

   Alfresco.SiteMembers.prototype.onReady = function() {
      this.widgets.showallButton = Alfresco.util.createYUIButton(this, "showall", this.onShowAll);

      onReady.call(this);

      self = this;

      this.widgets.dataTable.insertColumn({
         key : "resetPassword",
         label : "Reset Password",
         formatter : renderCellResetPassword,
         width : 140
      });
   };

   Alfresco.SiteMembers.prototype.showResetPasswordDialog = function(event, user) {
      // show a dialog to the user to confirm the request
      var parent = this;

      Alfresco.util.PopupManager.displayPrompt({
         title : parent.msg("site-members.reset-password.dialog.title", user),
         text : parent.msg("site-members.reset-password.dialog.text", user),
         buttons : [ {
            text : parent.msg("button.reset-password"),
            handler : function() {
               parent.doResetPassword(event, user);

               this.destroy();
            }
         }, {
            text : parent.msg("button.close"),
            handler : function() {
               this.destroy();
            },
            isDefault : true
         } ]
      });
   };

   /**
    * Reset password event handler
    * 
    * @method doRemove
    * @param event
    *           {object} The event object
    * @param user
    *           {string} The userName to remove
    */
   Alfresco.SiteMembers.prototype.doResetPassword = function(event, user) {
      // show a wait message
      this.widgets.feedbackMessage = Alfresco.util.PopupManager.displayMessage({
         text : this.msg("message.reset-password"),
         spanClass : "wait",
         displayTime : 0,
         effect : null
      });

      // request success handler
      var success = function(response, user) {
         // hide the wait message
         this.widgets.feedbackMessage.destroy();

         // show popup message to confirm
         Alfresco.util.PopupManager.displayMessage({
            text : this.msg("site-members.reset-password-success", user)
         });

      };

      // request failure handler
      var failure = function(response) {
         // remove the message
         this.widgets.feedbackMessage.destroy();
      };

      // make ajax call to site service to remove user
      Alfresco.util.Ajax.request({
         url : Alfresco.constants.PROXY_URI + "vgr/resetpassword/" + this.options.siteId + "/" + encodeURIComponent(user),
         method : "PUT",
         successCallback : {
            fn : success,
            obj : user,
            scope : this
         },
         failureMessage : this.msg("site-members.reset-password-failure", user),
         failureCallback : {
            fn : failure,
            scope : this
         }
      });
   };

}(Alfresco.SiteMembers.prototype.onReady));

(function(_performSearch) {

   Alfresco.SiteMembers.prototype._performSearch = function(searchTerm) {
      _performSearch.call(this, searchTerm);

      this.widgets.showallButton.set("disabled", true);
   };

}(Alfresco.SiteMembers.prototype._performSearch));

(function(_enableSearchUI) {

   Alfresco.SiteMembers.prototype._enableSearchUI = function() {
      _enableSearchUI.call(this);

      this.widgets.showallButton.set("disabled", false);
   };

}(Alfresco.SiteMembers.prototype._enableSearchUI));
