(function(renderAction) {
   
   var $html = Alfresco.util.encodeHTML;
   var $isValueSet = Alfresco.util.isValueSet;
   var $siteURL = Alfresco.util.siteURL;

   Alfresco.doclib.Actions.prototype.renderAction = function(p_action, p_record) {
      var urlContext = Alfresco.constants.URL_RESCONTEXT + "components/documentlibrary/actions/", iconStyle = 'style="background-image:url(' + urlContext + '{icon}-16.png)" ', actionTypeMarkup = {
         "link" : '<div class="{id}"><a title="{label}" class="simple-link" href="{href}" ' + iconStyle + '{target}><span>{label}</span></a></div>',
         "pagelink" : '<div class="{id}"><a title="{label}" class="simple-link" href="{pageUrl}" ' + iconStyle + '><span>{label}</span></a></div>',
         "javascript" : '<div class="{id}" title="{jsfunction}"><a title="{label}" class="action-link" href="#"' + iconStyle + '><span>{label}</span></a></div>',
         "header" : '<div class="action-header"><span>{label}</span></div>'
      };

      // Store quick look-up for client-side actions
      p_record.actionParams[p_action.id] = p_action.params;

      var markupParams = {
         "id" : p_action.id,
         "icon" : p_action.icon,
         "label" : $html(Alfresco.util.substituteDotNotation(this.msg(p_action.label), p_record))
      };

      // Parameter substitution for each action type
      if (p_action.type === "link") {
         if (p_action.params.href) {
            markupParams.href = Alfresco.util.substituteDotNotation(p_action.params.href, p_record);
            markupParams.target = p_action.params.target ? "target=\"" + p_action.params.target + "\"" : "";
         } else {
            Alfresco.logger.warn("Action configuration error: Missing 'href' parameter for actionId: ", p_action.id);
         }
      } else if (p_action.type === "pagelink") {
         if (p_action.params.page) {
            markupParams.pageUrl = Alfresco.util.substituteDotNotation(p_action.params.page, p_record);

            /**
             * If the page starts with a "{" character we're going to assume it's a placeholder variable that will be resolved by the getActionsUrls() function. In which case, we do not want to use
             * the $siteURL() function here as that will result in a double-prefix.
             */
            if (p_action.params.page.charAt(0) !== "{") {
               var recordSiteName = $isValueSet(p_record.location.site) ? p_record.location.site.name : null;
               markupParams.pageUrl = $siteURL(markupParams.pageUrl, {
                  site : recordSiteName
               });
            }
         } else {
            Alfresco.logger.warn("Action configuration error: Missing 'page' parameter for actionId: ", p_action.id);
         }
      } else if (p_action.type === "javascript") {
         if (p_action.params["function"]) {
            markupParams.jsfunction = p_action.params["function"];
         } else {
            Alfresco.logger.warn("Action configuration error: Missing 'function' parameter for actionId: ", p_action.id);
         }
      } else if (p_action.type === "header") {
               
      }
      
      return YAHOO.lang.substitute(actionTypeMarkup[p_action.type], markupParams);
   };

}(Alfresco.doclib.Actions.prototype.renderAction));
