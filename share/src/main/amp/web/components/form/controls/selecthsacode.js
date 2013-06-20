/**
 * Helper function to update the current state of the HSA Code multi-select list to the given hidden field.
 *
 * @method updateMultiSelectListValue
 * @param list {string} The id of the multi-select element
 * @param hiddenField {string} The id of the hidden field to populate the value with
 * @param signalChange {boolean} If true a bubbling event is sent to inform any
 *        interested listeners that the hidden field value changed
 * @static
 */
Alfresco.util.updateHsaCodeValue = function(list, hiddenField, signalChange)
{
   var listElement = YAHOO.util.Dom.get(list);
   var hiddenIdElement = YAHOO.util.Dom.get(hiddenField + ".id");
   
   if (listElement !== null)
   {
      var values = new Array();
      var ids = new Array();

      for (var j = 0, jj = listElement.options.length; j < jj; j++)
      {
         if (listElement.options[j].selected)
         {
            ids.push(listElement.options[j].value);
            values.push(listElement.options[j].text);
         }
      }

      YAHOO.util.Dom.get(hiddenField).value = values.join("#alf#") + "";
      
      if (hiddenIdElement) {
         hiddenIdElement.value = ids.join("#alf#");
      }

      if (signalChange)
      {
         YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
      }
   }
};
