(function(onTemplateLoaded) {

   Alfresco.module.CreateSite.prototype.onTemplateLoaded = function(response) {
      onTemplateLoaded.call(this, response);

      var shortInput = YAHOO.util.Dom.get(this.id + "-shortName");

      YAHOO.util.Dom.addClass(shortInput, "disabled");

      YAHOO.util.Event.addListener(shortInput, 'focus', function() {
         YAHOO.util.Dom.removeClass(shortInput, 'disabled');
      });
   };

}(Alfresco.module.CreateSite.prototype.onTemplateLoaded));

(function(_showPanel) {

   Alfresco.module.CreateSite.prototype._showPanel = function() {
      _showPanel.call(this);

      var shortInput = YAHOO.util.Dom.get(this.id + "-shortName");

      YAHOO.util.Dom.addClass(shortInput, "disabled");

   };

}(Alfresco.module.CreateSite.prototype._showPanel));
