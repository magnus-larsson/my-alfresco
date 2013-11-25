// @overridden projects/slingshot/source/web/components/invite/addemail.js

(function(onReady) {

   Alfresco.AddEmailInvite.prototype.addEmailButtonClick = function(e, p_obj) {
      // fetch the firstname, lastname nad email
      var firstNameElem = YAHOO.util.Dom.get(this.id + "-firstname"), firstName = firstNameElem.value, lastNameElem = YAHOO.util.Dom.get(this.id + "-lastname"), lastName = lastNameElem.value, emailElem = YAHOO.util.Dom
               .get(this.id + "-email"), email = emailElem.value, self = this;

      // check whether we got enough information to proceed
      if (firstName.length < 1 || lastName.length < 1 || email.length < 1) {
         Alfresco.util.PopupManager.displayMessage({
            text : this.msg("addemail.mandatoryfieldsmissing")
         });
         return;
      }

      this.checkExistanceBasedOnEmail(emailElem.value, function() {
         self.addEmail(firstNameElem, lastNameElem, emailElem);
      }, function() {
         Alfresco.util.PopupManager.displayPrompt({
            title : self.msg("addemail.useralreadyexists.title"),
            text : self.msg("addemail.useralreadyexists.text"),
            modal : true,
            buttons : [ {
               text : self.msg("ok"),
               handler : function() {
                  this.destroy();
               }
            } ]
         });

         return;
      });
   };

   /**
    * Checks whether the person exists in the system or not
    */
   Alfresco.AddEmailInvite.prototype.checkExistanceBasedOnEmail = function(email, successCallback, failureCallback) {
      Alfresco.util.Ajax.request({
         url : Alfresco.constants.PROXY_URI_RELATIVE + "vgr/peoplebyemail",
         dataObj : {
            email : email
         },
         successCallback : {
            fn : function(response) {
               var exists = response.json.people.length > 0;

               if (!exists) {
                  successCallback();
               } else {
                  failureCallback();
               }
            },
            scope : this
         }
      });
   };

   Alfresco.AddEmailInvite.prototype.addEmail = function(firstNameElem, lastNameElem, emailElem) {
      // Fire the personSelected bubble event
      YAHOO.Bubbling.fire("personSelected", {
         firstName : firstNameElem.value,
         lastName : lastNameElem.value,
         email : emailElem.value
      });

      // clear the values
      firstNameElem.value = "";
      lastNameElem.value = "";
      emailElem.value = "";
   };

}(Alfresco.AddEmailInvite.prototype.addEmailButtonClick));
