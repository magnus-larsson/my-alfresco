// @overridden projects/web-framework-commons/source/web/components/form/form.js

(function() {

   Alfresco.util.updateMultiSelectListValue = function(list, hiddenField, signalChange, optionSeparator) {
      var listElement = YUIDom.get(list);

      if (listElement !== null)
      {
         var values = new Array();
         for ( var j = 0, jj = listElement.options.length; j < jj; j++) 
         {
            if (listElement.options[j].selected) 
            {
               values.push(listElement.options[j].value);
            }
         }

         YUIDom.get(hiddenField).value = values.join(optionSeparator);

         if (signalChange) 
         {
            YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
         }
      }
   };

   /**
    * Helper function to add the current state of the given multi-property text to the given hidden field.
    * 
    * @method updateMultiInputTextValue
    * @param list
    *           {string} The id of the multi-select element
    * @param hiddenField
    *           {string} The id of the hidden field to populate the value with
    * @param signalChange
    *           {boolean} If true a bubbling event is sent to inform any interested listeners that the hidden field value changed
    * @static
    */
   Alfresco.util.updateMultiInputTextValue = function(hiddenField, signalChange) {
      var elements = Selector.query('input[id^=' + hiddenField + '_]');

      if (elements !== null) {
         var values = new Array();

         for ( var x = 0; x < elements.length; x++) {
            var element = elements[x];

            if (element.value == null || element.value == "") {
               continue;
            }

            values.push(element.value);
         }

         if (values.length == 0) {
            YUIDom.get(hiddenField).value = "";
         } else if (values.length == 1) {
            YUIDom.get(hiddenField).value = values[0];
         } else {
            YUIDom.get(hiddenField).value = values.join("#alf#");
         }

         if (signalChange) {
            YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
         }
      }
   };

   Alfresco.util.removeMultiInputTextValue = function(hiddenField, inputField, signalChange) {
      // delete the LI and all it's children...
      var obj = document.getElementById(inputField);
      var parentLI = obj.parentNode;
      var parentUL = parentLI.parentNode;
      parentUL.removeChild(parentLI);

      // update the hidden input text
      Alfresco.util.updateMultiInputTextValue(hiddenField, signalChange);

      // if there's only one text field left, hide the remove image and link
      var elements = Selector.query('input[id^=' + hiddenField + '_]');

      if (elements.length == 1) {
         var element = elements[0];

         YUIDom.setStyle(element.id + "-image", "display", "none");
         YUIDom.setStyle(element.id + "-remove-link", "display", "none");
      }

      if (signalChange) {
         YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
      }
   };

   Alfresco.util.addMultiInputTextValue = function(hiddenField, fieldName, signalChange) {
      var elements = Selector.query('input[id^=' + hiddenField + '_]');

      // first create the new element
      if (elements !== null) {
         // get the last element
         var element = elements[elements.length - 1];
         parentLI = element.parentNode;
         var newLI = parentLI.cloneNode(true);

         var newInput = Selector.query('input', newLI);
         var newLink = Selector.query('a', newLI);
         var newImage = Selector.query('img', newLI);

         var inputId = "" + YUIDom.getAttribute(newInput, "id");
         var lastNumber = parseInt(inputId.replace(hiddenField + "_", ""));
         var newNumber = parseInt(inputId.replace(hiddenField + "_", "")) + 1;

         YUIDom.setAttribute(newInput, "id", hiddenField + "_" + newNumber);
         YUIDom.setAttribute(newInput, "name", fieldName + "_" + newNumber);
         YUIDom.setAttribute(newInput, "value", "");

         YUIDom.setAttribute(newLink, "id", hiddenField + "_" + newNumber + "-remove-link");

         YUIDom.setAttribute(newImage, "id", hiddenField + "_" + newNumber + "-image");
         var newImageOnClick = "" + YUIDom.getAttribute(newImage, "onclick");
         newImageOnClick = newImageOnClick.replace(hiddenField + "_" + lastNumber, hiddenField + "_" + newNumber);
         YUIDom.setAttribute(newImage, "onclick", newImageOnClick);

         parentLI.parentNode.appendChild(newLI);

         // give the newly created element focus
         document.getElementById(hiddenField + "_" + newNumber).focus();
      }

      // if there's more than one text field, show the remove image and link
      // if there's only one text field left, hide the remove image and link
      var elements = Selector.query('input[id^=' + hiddenField + '_]');

      if (elements.length > 1) {
         for ( var x = 0; x < elements.length; x++) {
            var element = elements[x];

            YUIDom.setStyle(element.id + "-image", "display", "inline");
            YUIDom.setStyle(element.id + "-remove-link", "display", "inline");
         }
      }

      if (signalChange) {
         YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
      }
   };

}());
