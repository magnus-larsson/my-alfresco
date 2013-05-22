(function(onReady) {

   var $html = Alfresco.util.encodeHTML;
   var $userProfile = Alfresco.util.userProfileLink;

   Alfresco.PeopleFinder.prototype._userText = function(userName, fullName, spanAttr) {
      if (!YAHOO.lang.isString(userName) || userName.length === 0) {
         return "";
      }
      
      var html = Alfresco.util.encodeHTML(YAHOO.lang.isString(fullName) && fullName.length > 0 ? fullName : userName);

      return '<span ' + (spanAttr || "") + '>' + html + '</span>';
   };      
   
   Alfresco.PeopleFinder.prototype.onReady = function() {
      var me = this;
      
      renderCellDescription = function(elCell, oRecord, oColumn, oData){
         // small helper functions to break up long words
         var breakUp = function(word) {
            return word.replace(/\//g,'/<span class="small-space"> </span>');
         };
         
         var userName = oRecord.getData("userName"),
            name = userName,
            firstName = oRecord.getData("firstName"),
            lastName = oRecord.getData("lastName"),
            userStatus = oRecord.getData("userStatus"),
            userStatusTime = oRecord.getData("userStatusTime");
            email = oRecord.getData("email");
         
         if ((firstName !== undefined) || (lastName !== undefined))
         {
            name = firstName ? firstName + " " : "";
            name += lastName ? lastName : "";
         }
         
         if (email == undefined) {
           email = "";
         }
         
         var title = oRecord.getData("jobtitle") || "",
            organization = oRecord.getData("organization") || "";
         
         if (me.options.siteId == "") {
           var desc = '<h3 class="itemname">' + $userProfile(userName, name, 'class="theme-color-1" tabindex="0"') + ' <span class="lighter">(' + $html(userName) + ')</span></h3>';
         } else {
           var desc = '<h3 class="itemname">' + me._userText(userName, name, 'class="theme-color-1" tabindex="0"') + ' <span class="lighter">(' + $html(userName) + ')</span></h3>';
         }
         
         if (title.length !== 0)
         {
            if (me.options.viewMode == Alfresco.PeopleFinder.VIEW_MODE_COMPACT)
            {
               desc += '<div class="detail">' + breakUp($html(title)) + '</div>';
            }
            else
            {
               desc += '<div class="detail"><span>' + me.msg("label.title") + ":</span> " + breakUp($html(title)) + '</div>';
            }
         }
         
         if (email.length !== 0)
         {
            if (me.options.viewMode == Alfresco.PeopleFinder.VIEW_MODE_COMPACT)
            {
               desc += '<div class="detail">&nbsp;(' + $html(email) + ')</div>';
            }
            else
            {
               desc += '<div class="detail"><span>' + me.msg("label.email") + ":</span> " + $html(email) + '</div>';
            }
         }
         
         if (organization.length !== 0)
         {
            if (me.options.viewMode == Alfresco.PeopleFinder.VIEW_MODE_COMPACT)
            {
               desc += '<div class="detail">&nbsp;(' + breakUp($html(organization)) + ')</div>';
            }
            else
            {
               desc += '<div class="detail"><span>' + me.msg("label.company") + ":</span> " + breakUp($html(organization)) + '</div>';
            }
         }
         
         if (userStatus !== null && me.options.viewMode !== Alfresco.PeopleFinder.VIEW_MODE_COMPACT)
         {
            desc += '<div class="user-status">' + $html(userStatus) + ' <span>(' + Alfresco.util.relativeTime(Alfresco.util.fromISO8601(userStatusTime.iso8601)) + ')</span></div>';
         }
         
         elCell.innerHTML = desc;
      };
      
      onReady.call(this);
      
      var page = Alfresco.constants.PAGEID;
      
      if (!page || page.length === 0) {
         var span = YAHOO.util.Selector.query('span.title-explanation')[0];
         
         YAHOO.util.Dom.setStyle(span, "display", "none");
      }
      
      this.widgets.dataTable.getColumn('person').formatter = renderCellDescription;
   };

}(Alfresco.PeopleFinder.prototype.onReady));
