// @overridden projects/slingshot/source/web/components/site-finder/site-finder.js

(function(onReady) {

   Alfresco.SiteFinder.prototype.onReady = function() {
      onReady.call(this);

      var renderCellThumbnail = this.widgets.dataTable.getColumn('shortName').formatter;

      this.widgets.dataTable.getColumn('shortName').formatter = function(elCell, oRecord, oColumn, oData) {
         renderCellThumbnail.call(this, elCell, oRecord, oColumn, oData);
         
         var anchor = YAHOO.util.Selector.query('table tbody tr td div a')[0];
         
         if (anchor) {
            anchor.href = anchor.href.replace("/dashboard", "/default-redirect"); 
         }
      };

      var renderCellDescription = this.widgets.dataTable.getColumn('description').formatter;

      this.widgets.dataTable.getColumn('description').formatter = function(elCell, oRecord, oColumn, oData) {
         renderCellDescription.call(this, elCell, oRecord, oColumn, oData);
         
         var anchor = YAHOO.util.Selector.query('h3.sitename a')[0];
         
         if (anchor) {
            anchor.href = anchor.href.replace("/dashboard", "/default-redirect"); 
         }
      };
   };

}(Alfresco.SiteFinder.prototype.onReady));
