(function() {

   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Bubbling = YAHOO.Bubbling;

   // Custom TreeView Node since we had problems with click event not being stopped
   // properly on icon
   Alfresco.thirdparty.TreeSelectNode = function(oData, oParent, expanded) {
      Alfresco.thirdparty.TreeSelectNode.superclass.constructor.call(this, oData, oParent, expanded);

      return this;
   };

   YAHOO.extend(Alfresco.thirdparty.TreeSelectNode, YAHOO.widget.TextNode, {

      // override constructions of html
      getNodeHtml : function() {
         var sb = [], i;

         sb[sb.length] = '<table id="ygtvtableel' + this.index + '" border="0" cellpadding="0" cellspacing="0" class="ygtvtable ygtvdepth' + this.depth;

         if (this.enableHighlight) {
            sb[sb.length] = ' ygtv-highlight' + this.highlightState;
         }

         if (this.className) {
            sb[sb.length] = ' ' + this.className;
         }

         sb[sb.length] = '"><tr class="ygtvrow">';

         for (i = 0; i < this.depth; ++i) {
            sb[sb.length] = '<td class="ygtvcell ' + this.getDepthStyle(i) + '"><div class="ygtvspacer"></div></td>';
         }

         if (this.hasIcon) {
            sb[sb.length] = '<td id="' + this.getToggleElId();
            sb[sb.length] = '" class="ygtvcell ';
            sb[sb.length] = this.getStyle();
            sb[sb.length] = '"><a href="#" onclick="return false;" class="ygtvspacer">&#160;</a></td>';
         }

         sb[sb.length] = '<td id="' + this.contentElId;
         sb[sb.length] = '" class="ygtvcell ';
         sb[sb.length] = this.contentStyle + ' ygtvcontent" ';
         sb[sb.length] = (this.nowrap) ? ' nowrap="nowrap" ' : '';
         sb[sb.length] = ' >';
         sb[sb.length] = this.getContentHtml();
         sb[sb.length] = '</td></tr></table>';

         return sb.join("");
      }

   });

   /**
    * Helper function to add the current state of the given list to the given hidden field.
    * 
    * @method updateListValue
    * @param list
    *           {string} The id of the ul|ol element
    * @param hiddenField
    *           {string} The id of the hidden field to populate the value with
    * @param signalChange
    *           {boolean} If true a bubbling event is sent to inform any interested listeners that the hidden field value changed
    * @static
    */
   Alfresco.util.updateListValue = function(list, hiddenField, signalChange) {
      var listElement = YUIDom.get(list);

      if (listElement !== null) {
         var values = [];
         var ids = [];

         var children = YUIDom.getChildren(listElement);

         var hiddenIdInput = YUIDom.get(hiddenField + ".id");

         for ( var j = 0, jj = children.length; j < jj; j++) {
            var id = children[j].id;
            var value = children[j].innerHTML;

            if (id.indexOf('//') != -1 && id.indexOf('//') + 2 < id.length) {
               id = id.split('//')[1]; // id of li is ${fieldHtmlId}//ID to make it unique to that control
            }

            if (hiddenIdInput) {
               values.push(value);
               ids.push(id);
            } else {
               values.push(id + '|' + value);
            }
         }

         YUIDom.get(hiddenField).value = values.join("#alf#");

         if (hiddenIdInput) {
            YUIDom.get(hiddenIdInput).value = ids.join("#alf#");
         }

         if (signalChange) {
            YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
         }
      }
   };

})();
