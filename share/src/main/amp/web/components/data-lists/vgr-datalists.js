/*
 * @overridden projects/slingshot/source/web/components/data-lists/datalists.js
 */

/**
 * TODO write some docs
 */
(function(renderDataLists) {

   Alfresco.component.DataLists.prototype.renderDataLists = function(p_highlightName) {
      renderDataLists.call(this, p_highlightName);

      Alfresco.util.YUILoaderHelper.require([ "dragdrop" ], null, this);

      var lists = YAHOO.util.Selector.query('div.datalists div.filter ul li');

      for ( var x = 0; x < lists.length; x++) {
         var el = lists[x];

         var link = YAHOO.util.Selector.query('a.filter-link', el)[0].href;

         var name = link.substring(link.indexOf('data-lists?list=') + 16);

         for ( var index in this.dataLists) {
            var list = this.dataLists[index];

            if (list.name !== name) {
               continue;
            }

            YAHOO.util.Dom.addClass(el, list.nodeRef);

            var permissions = list.permissions;

            if (permissions["edit"]) {
               // add drop capability, but not to the selected
               if (list.name != this.options.listId) {
                  new YAHOO.util.DDTarget(el, list.itemType); // dnd group is list type
               }
            }
         }
      }
   };

}(Alfresco.component.DataLists.prototype.renderDataLists));
