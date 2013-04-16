/**
 * Adding a endsWith function to String 
 */
(function() {

   if (typeof String.prototype.endsWith !== 'function') {
      String.prototype.endsWith = function(suffix) {
         return this.indexOf(suffix, this.length - suffix.length) !== -1;
      };
   }
   
})();

/**
 * Adding bubbling on added site to onReady() call
 */
(function(onReady) {
   Alfresco.dashlet.MySites.prototype.onReady = function() {
      YAHOO.Bubbling.on("siteAdded", this.loadSites, this);

      onReady.call(this);
   };
}(Alfresco.dashlet.MySites.prototype.onReady));

/**
 * Replaces 'dashboard' with 'default-redirect' on the link for the site.
 */
(function(renderCellDetail) {
   Alfresco.dashlet.MySites.prototype.renderCellDetail = function(elCell, oRecord, oColumn, oData) {
      renderCellDetail.call(this, elCell, oRecord, oColumn, oData);

      var anchor = YAHOO.util.Selector.query("h3 a", elCell)[0];

      if (anchor.href.endsWith("dashboard")) {
         anchor.href = anchor.href.replace("dashboard", "default-redirect");
      }
   };
}(Alfresco.dashlet.MySites.prototype.renderCellDetail));
