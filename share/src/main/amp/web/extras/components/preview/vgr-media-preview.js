(function(display) {

   var Dom = YAHOO.util.Dom, Selector = YAHOO.util.Selector, Event = YAHOO.util.Event;

   /**
    * Does multiple stuff:
    * 
    * - adds toolbar=1 to the PDF url to instruct Adobe Reader to view the toolbar
    * - adds a maximise button to the page
    */
   Alfresco.WebPreview.prototype.Plugins.Embed.prototype.display = function() {
      var self = this;

      var url = this.attributes.src ? this.wp.getThumbnailUrl(this.attributes.src) : this.wp.getContentUrl(), displaysource, previewHeight;

      // add the toolbar (will be shown if Adobe Acrobat)
      url = url + '#toolbar=1';

      previewHeight = this.wp.setupPreviewSize();

      displaysource = '<div class="iframe-view-controls"><div class="iframe-viewer-button">';
      displaysource += '<a title="View In Browser" class="simple-link" href="' + url;
      displaysource += '" target="_blank" style="background-image:url(' + Alfresco.constants.URL_RESCONTEXT + 'components/documentlibrary/actions/document-view-content-16.png)">';
      displaysource += '<span>' + Alfresco.util.message("actions.document.view") + ' </span></a></div></div>'
      // Set the iframe
      displaysource += '<iframe id="' + this.wp.id + '-embed" name="Embed" src="' + url + '" scrolling="yes" marginwidth="0" marginheight="0" frameborder="0" vspace="0" hspace="0"  style="height:'
               + (previewHeight - 10).toString() + 'px; width:100%"></iframe>';

      Alfresco.util.YUILoaderHelper.require([ "tabview" ], this.onComponentsLoaded, this);
      Alfresco.util.YUILoaderHelper.loadComponents();
      
      YAHOO.util.Event.onContentReady(this.wp.id + '-embed', function() {
         YAHOO.lang.later(100, self, self._hideShowIframe);
      });

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
         window.open(Alfresco.constants.URL_PAGECONTEXT + "pdf-maximise?nodeRef=" + self.wp.options.nodeRef, "_blank");
      });

      button.appendChild(firstChild);
      firstChild.appendChild(anchor);
      nodeAction.insertBefore(button, nodeAction.firstChild);

      return displaysource;
   };

}(Alfresco.WebPreview.prototype.Plugins.Embed.prototype.display));

/**
 * Overloaded function to add the 'preview as PDF' text to document details
 */
(function(onComponentsLoaded) {

   var Dom = YAHOO.util.Dom, Selector = YAHOO.util.Selector, Event = YAHOO.util.Event;

   Alfresco.WebPreview.prototype.Plugins.Embed.prototype.onComponentsLoaded = function() {
      onComponentsLoaded.call(this);
      
      Alfresco.thirdparty.addTitleText(this);
      
      // Attach to links to capture action events (the yui buttons swallows the above)
      var buttons = Selector.query('span.yui-button button');
      
      for (button in buttons) {
         Event.addListener(buttons[button], "click", this.onClick, this, true);
      }
   };

}(Alfresco.WebPreview.prototype.Plugins.Embed.prototype.onComponentsLoaded));

/**
 * Overloaded function to add the 'preview as PDF' text to document details
 */
(function(onComponentsLoaded) {

   var Dom = YAHOO.util.Dom, Selector = YAHOO.util.Selector, Event = YAHOO.util.Event;

   Alfresco.WebPreview.prototype.Plugins.PdfJs.prototype.onComponentsLoaded = function() {
      onComponentsLoaded.call(this);
      
      Alfresco.thirdparty.addTitleText(this);
   };

}(Alfresco.WebPreview.prototype.Plugins.PdfJs.prototype.onComponentsLoaded));

/**
 * Function for adding a 'preview as PDF' text to the document details page.
 */
(function() {
   
   Alfresco.thirdparty = Alfresco.thirdparty || {};

   Alfresco.thirdparty.addTitleText = function (scope) {
      var result = Selector.query('div.node-info span.document-version');
      
      if (result.length > 0) {
         var titleNote = document.createElement('span');
         
         Dom.addClass(titleNote, 'title-note');
         
         titleNote.innerHTML = '(' + scope.wp.msg('preview.title-text') + ')';
         
         result[0].parentNode.appendChild(titleNote);
      }
   };
   
})();