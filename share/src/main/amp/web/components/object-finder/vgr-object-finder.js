/**
 * TODO write some docs
 */
(function(_adjustCurrentValues) {

   Alfresco.ObjectFinder.prototype._adjustCurrentValues = function() {
      if (!this.options.disabled) {
         var addedItems = this.getAddedItems(), removedItems = this.getRemovedItems(), selectedItems = this.getSelectedItems();

         if (this.options.maintainAddedRemovedItems) {
            Dom.get(this.id + "-added").value = addedItems.join(this.options.optionSeparator);

            Dom.get(this.id + "-removed").value = removedItems.join(this.options.optionSeparator);
         }

         Dom.get(this.currentValueHtmlId).value = selectedItems.join(this.options.optionSeparator);
         if (Alfresco.logger.isDebugEnabled()) {
            Alfresco.logger.debug("Hidden field '" + this.currentValueHtmlId + "' updated to '" + selectedItems.join(this.options.optionSeparator) + "'");
         }

         // inform the forms runtime that the control value has been updated (if field is mandatory)
         if (this.options.mandatory) {
            YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
         }

         YAHOO.Bubbling.fire("formValueChanged", {
            eventGroup : this,
            addedItems : addedItems,
            removedItems : removedItems,
            selectedItems : selectedItems,
            selectedItemsMetaData : Alfresco.util.deepCopy(this.selectedItems)
         });

         this._enableActions();
      }
   };

}(Alfresco.ObjectFinder.prototype._adjustCurrentValues));

/**
 * TODO write some docs
 */
(function(getAddedItems) {

   Alfresco.ObjectFinder.prototype.getAddedItems = function() {
      var addedItems = [], currentItems = Alfresco.util.arrayToObject(this.options.currentValue.split(this.options.optionSeparator));

      for ( var item in this.selectedItems) {
         if (this.selectedItems.hasOwnProperty(item)) {
            if (!(item in currentItems)) {
               addedItems.push(item);
            }
         }
      }

      return addedItems;
   };

}(Alfresco.ObjectFinder.prototype.getAddedItems));

/**
 * TODO write some docs
 */
(function(getRemovedItems) {

   Alfresco.ObjectFinder.prototype.getRemovedItems = function() {
      var removedItems = [], currentItems = Alfresco.util.arrayToObject(this.options.currentValue.split(this.options.optionSeparator));

      for ( var item in currentItems) {
         if (currentItems.hasOwnProperty(item)) {
            if (!(item in this.selectedItems)) {
               removedItems.push(item);
            }
         }
      }
      
      return removedItems;
   };

}(Alfresco.ObjectFinder.prototype.getRemovedItems));
