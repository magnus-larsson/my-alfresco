(function(onReady) {

   var Dom = YAHOO.util.Dom, Selector = YAHOO.util.Selector, Event = YAHOO.util.Event;

   Alfresco.WebPreview.prototype.onReady = function() {
      var self = this;
      
      onReady.call(this);

      if (this.plugin instanceof Alfresco.WebPreview.prototype.Plugins.Embed) {
         // first add the toolbar to the src for Adobe Acrobat plugin
         var iframe = Dom.get(this.id + '-embed');
         
         if (iframe) {
            iframe.src = iframe.src + '#toolbar=1';
         }

         // fix so that the iframe is positioned above the Adobe Reader plugin
         Event.onContentReady(this.id + '-embed', function() {
            // Delay the check slightly, so that the panel has time to display and be detected
            YAHOO.lang.later(100, self.plugin, self.plugin._hideShowIframe);
            // Again, because it may be to fast. We want fast, but depends on browser and hardware.
            // To slow and its is "blinking". So do it twice.
            YAHOO.lang.later(300, self.plugin, self.plugin._hideShowIframe);
         });

         // Attach to links to capture action events (the yui buttons swallows the above)
         var buttons = Selector.query('span.yui-button button');
         
         for (button in buttons) {
            Event.addListener(buttons[button], "click", function() {
               // Delay the check slightly, so that the panel has time to display and be detected
               YAHOO.lang.later(100, self.plugin, self.plugin._hideShowIframe);
               // Again, because it may be to fast. We want fast, but depends on browser and hardware.
               // To slow and its is "blinking". So do it twice.
               YAHOO.lang.later(300, self.plugin, self.plugin._hideShowIframe);
            }, this, true);
         }

         Alfresco.thirdparty.addTitleText(this);
         
         Alfresco.thirdparty.addMaximiseButton(this);
      }
      
      if (this.plugin instanceof Alfresco.WebPreview.prototype.Plugins.PdfJs) {
         Alfresco.thirdparty.addTitleText(this);
      }
   };

}(Alfresco.WebPreview.prototype.onReady));

/**
 * Function for adding a 'preview as PDF' text to the document details page.
 */
(function() {

   var Dom = YAHOO.util.Dom, Selector = YAHOO.util.Selector, Event = YAHOO.util.Event;
   
   Alfresco.thirdparty = Alfresco.thirdparty || {};

   Alfresco.thirdparty.addTitleText = function (scope) {
      var result = Selector.query('div.node-info span.document-version');
      
      if (result.length > 0) {
         var titleNote = document.createElement('span');
         
         Dom.addClass(titleNote, 'title-note');
         
         titleNote.innerHTML = '(' + scope.msg('preview.title-text') + ')';
         
         result[0].parentNode.appendChild(titleNote);
      }
   };
   
   Alfresco.thirdparty.addMaximiseButton = function(scope) {
      var nodeAction = Selector.query('div.node-action')[0];
      nodeAction.style.width = '30%';
      var nodeInfo = Selector.query('div.node-header div.node-info')[0];
      nodeInfo.style.width = '70%';

      var button = document.createElement('span');
      Dom.addClass(button, 'yui-button');
      Dom.addClass(button, 'yui-link-button');
      Dom.addClass(button, 'onMaximiseDocumentClick');

      var firstChild = document.createElement('span');
      Dom.addClass(firstChild, 'first-child');

      var anchor = document.createElement('a');
      anchor.setAttribute('tabindex', '0');
      anchor.innerHTML = 'Fullskärm';

      Event.addListener(anchor, 'click', function(e) {
         window.open(Alfresco.constants.URL_PAGECONTEXT + "pdf-maximise?nodeRef=" + scope.options.nodeRef, "_blank");
      });

      button.appendChild(firstChild);
      firstChild.appendChild(anchor);
      nodeAction.insertBefore(button, nodeAction.firstChild);
   };
   
})();