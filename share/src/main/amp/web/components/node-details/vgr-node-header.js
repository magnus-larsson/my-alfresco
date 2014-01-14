// @overridden projects/slingshot/source/web/components/node-details/node-header.js

(function(onReady) {

   Alfresco.component.NodeHeader.prototype.onReady = function() {
      onReady.call(this);
      
      var status = this.options.status;

      var icon = YAHOO.util.Selector.query('img.node-thumbnail')[0];
      
      var indicators = _constructStatusIcons(this.options.status, this);
      
      if (this.options.isContainer === true) {
         for ( var x = 0; x < indicators.length; x++) {
            var indicator = indicators[x];

            var result = YAHOO.util.Dom.insertAfter(indicator, icon);
         }
      } else {
        var indicator = _constructPublishStatusPlaceholder();
        var result = YAHOO.util.Dom.insertAfter(indicator, icon);
        var placeholder = YAHOO.util.Selector.query('img#vgr-publish-status-placeholder')[0];
        YAHOO.Bubbling.fire("vgrUpdatePublishStatus",
            {
              nodeRef: this.options.nodeRef.replace("://","/"),
              imagePlaceholder: placeholder
            });
      }
      
      // add on click on reload document button
      var anchor = YAHOO.util.Selector.query('span.onReloadDocumentClick span.first-child a')[0];
      
      YAHOO.util.Event.addListener(anchor, 'click', function(e) {
         location.reload();
      });
   };
   
   function _constructPublishStatusPlaceholder() {
     var indicator = new YAHOO.util.Element(document.createElement('img'));
     indicator.addClass('status-icon');
     //indicator.addClass('hidden');
     indicator.set('id', "vgr-publish-status-placeholder");
     //indicator.set('src', Alfresco.constants.URL_RESCONTEXT + "components/documentlibrary/indicators/loading.gif");
     return indicator;
   }

   function _constructStatusIcons(dataStatus, scope) {
      if (!dataStatus || dataStatus.length == 0) {
         return "";
      }

      var statuses = dataStatus.split(",").sort(), s, SPACE = " ", meta, i18n, i18nMeta;

      var indicators = [];

      for ( var i = 0, j = statuses.length; i < j; i++) {
         status = statuses[i];

         meta = "";

         s = status.indexOf(SPACE);

         if (s > -1) {
            meta = status.substring(s + 1);
            status = status.substring(0, s);
         }

         i18n = "tip." + status;

         i18nMeta = i18n + ".meta";

         if (meta && scope.msg(i18nMeta) !== i18nMeta) {
            i18n = i18nMeta;
         }
         
         tip = Alfresco.util.message(i18n, scope.name, meta.split("|")); // Note: deliberate bypass of scope.msg() function

         var indicator = new YAHOO.util.Element(document.createElement('img'));
         indicator.addClass('status-icon');
         indicator.set('src', Alfresco.constants.URL_RESCONTEXT + 'components/documentlibrary/indicators/' + status + '-16.png');
         indicator.set('alt', status);
         indicator.set('title', tip);

         indicators.push(indicator);
      }

      return indicators;
   }

}(Alfresco.component.NodeHeader.prototype.onReady));
