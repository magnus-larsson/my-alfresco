// @overridden projects/slingshot/source/web/components/invite/addemail.js

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
 * AddEmailInvite component.
 *
 * @namespace Alfresco
 * @class Alfresco.AddEmailInvite
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;

   /**
    * AddEmailInvite constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.AddEmailInvite} The new AddEmailInvite instance
    * @constructor
    */
   Alfresco.AddEmailInvite = function(htmlId)
   {
      return Alfresco.AddEmailInvite.superclass.constructor.call(this, "Alfresco.AddEmailInvite", htmlId);
   };

   YAHOO.extend(Alfresco.AddEmailInvite, Alfresco.component.Base,
      {
         /**
          * Fired by YUI when parent element is available for scripting.
          * Component initialisation, including instantiation of YUI widgets and event listener binding.
          *
          * @method onReady
          */
         onReady: function AddEmailInvite_onReady()
         {
            // listen on ok button click
            this.widgets.addEmailButton = Alfresco.util.createYUIButton(this, "add-email-button", this.addEmailButtonClick);
         },

         /**
          * Add email button click
          *
          * @method addEmailButtonClick
          * @param e {object} DOM Event
          * @param p_obj {object} Optional object literal from event listener definition
          */
         addEmailButtonClick: function AddEmailInvite_addEmailButtonClick(e, p_obj)
         {
            // fetch the firstname, lastname nad email
            var firstNameElem = YAHOO.util.Dom.get(this.id + "-firstname"),
               firstName = firstNameElem.value,
               lastNameElem = YAHOO.util.Dom.get(this.id + "-lastname"),
               lastName = lastNameElem.value,
               emailElem = YAHOO.util.Dom.get(this.id + "-email"),
               email = emailElem.value,
               self = this;

            // check whether we got enough information to proceed
            if (firstName.length < 1 || lastName.length < 1 || email.length < 1)
            {
               Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("addemail.mandatoryfieldsmissing")
                  });
               return;
            }

            this.checkExistanceBasedOnEmail(emailElem.value, function() {
               self.addEmail(firstNameElem, lastNameElem, emailElem);
            }, function() {
               Alfresco.util.PopupManager.displayPrompt({
                  title: self.msg("addemail.useralreadyexists.title"),
                  text:  self.msg("addemail.useralreadyexists.text"),
                  modal: true,
                  buttons: [{
                     text: self.msg("ok"),
                     handler: function() {
                        this.destroy();
                     }
                  }]
               });

               return;
            });
         },

         /**
          * Checks whether the person exists in the system or not
          */
         checkExistanceBasedOnEmail: function(email, successCallback, failureCallback) {
            Alfresco.util.Ajax.request({
               url: Alfresco.constants.PROXY_URI_RELATIVE + "vgr/peoplebyemail",
               dataObj: { email: email },
               successCallback: {
                  fn: function(response) {
                     var exists = response.json.people.length > 0;

                     if (!exists) {
                        successCallback();
                     } else {
                        failureCallback();
                     }
                  },
                  scope: this
               }
            });
         },

         addEmail: function(firstNameElem, lastNameElem, emailElem) {
            // Fire the personSelected bubble event
            YAHOO.Bubbling.fire("personSelected",
               {
                  firstName: firstNameElem.value,
                  lastName: lastNameElem.value,
                  email: emailElem.value
               });

            // clear the values
            firstNameElem.value = "";
            lastNameElem.value = "";
            emailElem.value = "";
         }

      });
})();
