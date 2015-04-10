(function(onReady) {

   var Dom = YAHOO.util.Dom,
      Selector = YAHOO.util.Selector,
      Event = YAHOO.util.Event;

   Alfresco.WebPreview.prototype.onReady = function() {
      var self = this;

      onReady.call(this);

      if (Alfresco.WebPreview.prototype.Plugins.Embed!==undefined && this.plugin instanceof Alfresco.WebPreview.prototype.Plugins.Embed) {
         var timeout = this.plugin.attributes.timeout ? this.plugin.attributes.timeout : 10;

         // first add the toolbar to the src for Adobe Acrobat plugin
         var iframe = Dom.get(this.id + '-embed');

         var success = false;

         if (iframe) {
            // fiddle with the onreadystatechange and set to true to now when the iframe has loaded
            var onreadystatechange = iframe.onreadystatechange;

            iframe.onreadystatechange = function() {
               if (onreadystatechange) {
                  onreadystatechange.call(this);
               }

               success = true;
            };

            iframe.src = iframe.src + '#toolbar=1';
         }

         // fix so that the iframe is positioned above the Adobe Reader plugin
         Event.onContentReady(this.id + '-embed', function() {
            // Delay the check slightly, so that the panel has time to display and be detected
            YAHOO.lang.later(100, self.plugin, self.plugin._hideShowIframe);
            // Again, because it may be to fast. We want fast, but depends on browser and hardware.
            // To slow and its is "blinking". So do it twice.
            YAHOO.lang.later(300, self.plugin, self.plugin._hideShowIframe);

            // hide the preview if the iframe contains a div
            var checkExist = setInterval(function() {
               if (!success) {
                  return;
               }

               try {
                  var document = iframe.contentDocument || iframe.contentWindow.document;
               } catch (error) {
                  return;
               }

               if (!document) {
                  return;
               }

               if (!document.body ||  !document.body.hasChildNodes()) {
                  return;
               }

               var first = document.body.firstChild;

               if (first && first.nodeName && first.nodeName.toLowerCase() == 'div') {
                  Alfresco.thirdparty.hidePreview(self);
                  clearInterval(checkExist);
               }
            }, 500);

            // check if success after a certain amount of time, and if not, hide the preview and show download link instead
            setTimeout(function() {
               if (success) {
                  return;
               }

               Alfresco.thirdparty.hidePreview(self);
            }, timeout * 1000);
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

      if (Alfresco.WebPreview.prototype.Plugins.PdfJs!==undefined && this.plugin instanceof Alfresco.WebPreview.prototype.Plugins.PdfJs) {
         var timeout = this.plugin.attributes.timeout ? this.plugin.attributes.timeout : 10;

         var self = this;

         var _onGetDocumentFailure = Alfresco.WebPreview.prototype.Plugins.PdfJs.prototype._onGetDocumentFailure;
         var _onGetDocumentSuccess = Alfresco.WebPreview.prototype.Plugins.PdfJs.prototype._onGetDocumentSuccess;
         var _loadPdf = Alfresco.WebPreview.prototype.Plugins.PdfJs.prototype._loadPdf;
         var success = false;

         Alfresco.WebPreview.prototype.Plugins.PdfJs.prototype._onGetDocumentSuccess = function(pdf) {
            success = true;

            _onGetDocumentSuccess.call(this, pdf);
         };

         Alfresco.WebPreview.prototype.Plugins.PdfJs.prototype._loadPdf = function(params) {
            _loadPdf.call(this, params);

            setTimeout(function() {
               if (!success) {
                  Alfresco.thirdparty.hidePreview(self);
               }
            }, timeout * 1000);
         };

         Alfresco.WebPreview.prototype.Plugins.PdfJs.prototype._onGetDocumentFailure = function(message, exception) {
            if (!exception) {
               exception = {
                  name: "UnknownException",
                  cose: "unknownexception"
               };
            }

            _onGetDocumentFailure.call(this, message, exception);

            if (exception.name != 'PasswordException') {
               Alfresco.thirdparty.hidePreview(self);
            }
         };

         Alfresco.thirdparty.addTitleText(this);
      }
   };

}(Alfresco.WebPreview.prototype.onReady));

/**
 * Function for adding a 'preview as PDF' text to the document details page.
 */
(function() {

   var Dom = YAHOO.util.Dom,
      Selector = YAHOO.util.Selector,
      Event = YAHOO.util.Event;

   Alfresco.thirdparty = Alfresco.thirdparty ||  {};

   Alfresco.thirdparty.addTitleText = function(scope) {
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

   Alfresco.thirdparty.hidePreview = function(scope) {
      var messages = [];

      // Tell user that the content can't be displayed
      var message = scope.msg("label.noPreview", scope.getContentUrl(true));

      for (i = 0, il = messages.length; i < il; i++) {
         message += '<br/>' + messages[i];
      }

      scope.widgets.previewerElement.innerHTML = '<div class="message">' + message + '</div>';

      Dom.removeClass(scope.widgets.previewerElement, "PdfJs");
   };

})();