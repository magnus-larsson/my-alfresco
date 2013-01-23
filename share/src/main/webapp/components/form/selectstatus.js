(function() {
   
   /**
    * SelectStatusDocument constructor.
    * 
    * @param {string}
    *           htmlId The HTML id of the parent element
    * @return {Alfresco.thirdparty.SelectDocumentStatus} The new SelectStatusDocument instance
    * @constructor
    * @private
    */
   Alfresco.thirdparty.SelectDocumentStatus = function(htmlId) {
      Alfresco.thirdparty.SelectDocumentStatus.superclass.constructor.call(this, "Alfresco.thirdparty.SelectDocumentStatus", htmlId, []);

      return this;
   };

   YAHOO.extend(Alfresco.thirdparty.SelectDocumentStatus, Alfresco.component.Base, {

      onReady : function() {
         var self = this;

         YAHOO.util.Event.onDOMReady(function() {
            self.onDOMReady();
         });
      },

      onDOMReady : function() {
         // add an event listener to the select box that set the hidden field on change
         YAHOO.util.Event.addListener(this.id, "change", function() {
            var hiddenIdInput = YAHOO.util.Dom.get(this.id + '.id');

            if (!hiddenIdInput) {
               return;
            }

            var optionId = this.options[this.selectedIndex].id;

            hiddenIdInput.value = optionId;
         });

         var hiddenIdInput = YAHOO.util.Dom.get(this.id + '.id');

         if (!hiddenIdInput) {
            return;
         }

         var values = hiddenIdInput.value.split(this.options.optionSeparator);

         var options = YAHOO.util.Dom.get(this.id).options;

         for ( var x = 0; x < options.length; x++) {
            var option = options[x];

            for ( var y = 0; y < values.length; y++) {
               var value = values[y];

               if (value == option.id) {
                  option.selected = true;
               }
            }
         }
      }

   });
})();
