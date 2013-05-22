/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * WebPreview component.
 *
 * @namespace Alfresco
 * @class Alfresco.WebPreview
 */
(function () {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Element = YAHOO.util.Element, KeyListener = YAHOO.util.KeyListener;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;

   /**
    * WebPreview constructor.
    *
    * @param {string} htmlId The HTML id of the parent element
    * @return {Alfresco.WebPreview} The new WebPreview instance
    * @constructor
    * @private
    */
   Alfresco.WebPreview = function (containerId) {
      Alfresco.WebPreview.superclass.constructor.call(this, "Alfresco.WebPreview", containerId, ["button", "container", "uploader"]);

      /* Decoupled event listeners are added in setOptions */
      YAHOO.Bubbling.on("documentDetailsAvailable", this.onDocumentDetailsAvailable, this);
      YAHOO.Bubbling.on("recalculatePreviewLayout", this.onRecalculatePreviewLayout, this);

      return this;
   };

   YAHOO.extend(Alfresco.WebPreview, Alfresco.component.Base, {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options: {
         /**
          * Noderef to the content to display
          *
          * @property nodeRef
          * @type string
          */
         nodeRef: "",

         /**
          * The size of the content
          *
          * @property size
          * @type string
          */
         size: "0",

         /**
          * The file name representing root container
          *
          * @property name
          * @type string
          */
         name: "",

         /**
          * The icon displayed in the header of the component
          *
          * @property icon
          * @type string
          */
         icon: "",

         /**
          * The mimeType of the node to display, needed to decide what preview
          * that should be used.
          *
          * @property mimeType
          * @type string
          */
         mimeType: "",

         /**
          * A list of previews available for this component
          *
          * @property previews
          * @type Array
          */
         previews: [],

         /**
          * Decides if the Previewer shall disable the i18n input fix shall be disabled for all browsers.
          * If it shall be disabled for certain a certain os/browser override the disableI18nInputFix() method.
          *
          * Fix solves the Flash i18n input keyCode bug when "wmode" is set to "transparent"
          * http://bugs.adobe.com/jira/browse/FP-479
          * http://issues.alfresco.com/jira/browse/ALF-1351
          *
          * ...see "Browser Testing" on this page to see supported browser/language combinations for AS2 version
          * http://analogcode.com/p/JSTextReader/
          *
          * ... We are using the AS3 version of the same fix
          * http://blog.madebypi.co.uk/2009/04/21/transparent-flash-text-entry/
          *
          * @property disableI18nInputFix
          * @type boolean
          * @default false
          */
         disableI18nInputFix: false,

         /**
          * Decides if images shall be displayed in the web preview or not.
          * By default they are not, du to: http://issues.alfresco.com/jira/browse/ALF-7420
          *
          * @property displayImageInWebPreview
          * @type boolean
          * @default false
          */
         displayImageInWebPreview: false,

         /**
          * If images are displayed using the img tag and are larger than this property,
          * the user must click a link to watch the image to avoid unecessary loading of large images.
          *
          * @property maxImageSizeToDisplay
          * @type Number
          * @default 500000
          */
         maxImageSizeToDisplay: 500000,

         /**
          * @property hasPdfRendition
          * @type boolean
          * @default false
          */
         hasPdfRendition: false,

         /**
          * @property pdfRendition
          * @type: string
          * @default: null
          */
         pdfRendition: null,

         /**
          * A json representation of the .get.config.xml file.
          * This is evaluated on the client side since we need the plugins to make sure it is supported
          * the user's browser and browser plugins.
          *
          * @property pluginConditions
          * @type Array
          */
         pluginConditions: []
      },

      /**
       * Space for preview "plugins" to register themselves in.
       * To provide a 3rd party plugin:
       *
       * 1. Create a javascript file and make it define a javascript class that defines a "plugin class" in this namespace.
       * 2. Override this component's .get.head.ftl file and make sure your javascript file (and its resources) are included.
       * 3. Override this component's .get.config.xml and define for which mimeTypes or thumbnails it shall be used.
       * 4. To make sure your plugin works in the browser, define a report() method that
       *    returns nothing if the browser is supported and otherwise a string with a message saying the reason the
       *    plugin can't be used in the browser.
       * 5. Define a display() method that will display the browser plugin or simply return a string of markup that shall be inserted.
       *
       * @property Plugins
       * @type Object
       */
      Plugins: {},

      /**
       * If a plugin was found to preview the content it will be stored here, for future reference.
       *
       * @property plugin One of the plugins that have registered themselves in Alfresco.WebPreview.Plugin
       * @type Object
       * @public
       */
      plugin: null,

      /**
       * Fired by YUILoaderHelper when required component script files have
       * been loaded into the browser.
       *
       * @method onComponentsLoaded
       */
      onComponentsLoaded: function WP_onComponentsLoaded() {
         /**
          * SWFObject patch
          * Ensures all flashvars are URI encoded
          */
         YAHOO.deconcept.SWFObject.prototype.getVariablePairs = function () {
            var variablePairs = [], key, variables = this.getVariables();

            for (key in variables) {
               if (variables.hasOwnProperty(key)) {
                  variablePairs[variablePairs.length] = key + "=" + encodeURIComponent(variables[key]);
               }
            }
            return variablePairs;
         };

         Event.onContentReady(this.id, this.onReady, this, true);
      },

      /**
       * Fired by YUI when parent element is available for scripting
       *
       * @method onReady
       */
      onReady: function WP_onReady() {
         // Convert the JSON string conditions back into an object...
         this.options.pluginConditions = eval(this.options.pluginConditions);

         // Setup web preview
         this._setupWebPreview(false);
      },

      /**
       * Called when document details has been available or changed (if the useDocumentDetailsAvailableEvent
       * option was set to true) on the page so the web previewer can remove its old preview and
       * display a new one if available.
       *
       * @method onDocumentDetailsAvailable
       * @param p_layer The type of the event
       * @param p_args Event information
       */
      onDocumentDetailsAvailable: function WP_onDocumentDetailsAvailable(p_layer, p_args) {
         // Get the new info about the node and decide if the previewer must be refreshed
         var documentDetails = p_args[1].documentDetails, refresh = false;

         // Name
         if (this.options.name != documentDetails.fileName) {
            this.options.name = documentDetails.fileName;
            refresh = true;
         }

         // Mime type
         if (this.options.mimeType != documentDetails.mimetype) {
            this.options.mimeType = documentDetails.mimetype;
            refresh = true;
         }

         // Size
         if (this.options.size != documentDetails.size) {
            this.options.size = documentDetails.size;
            refresh = true;
         }

         // Setup previewer
         if (refresh) {
            this._setupWebPreview();
         }
      },

      /**
       * Because the WebPreview content is absolutely positioned, components which alter DOM layout can fire
       * this event to prompt a recalculation of the absolute coordinates.
       *
       * @method onRecalculatePreviewLayout
       * @param p_layer The type of the event
       * @param p_args Event information
       */
      onRecalculatePreviewLayout: function WP_onRecalculatePreviewLayout(p_layer, p_args) {
         // Only if not in maximize view
         if (this.widgets.realSwfDivEl.getStyle("height") !== "100%") {
            this._positionOver(this.widgets.realSwfDivEl, this.widgets.shadowSfwDivEl);
         }
      },

      /**
       * Will setup the
       *
       * @method _setupWebPreview
       * @private
       */
      _setupWebPreview: function WP__setupWebPreview() {
         // Save a reference to the HTMLElement displaying texts so we can alter the texts later
         this.widgets.swfPlayerMessage = Dom.get(this.id + "-swfPlayerMessage-div");
         this.widgets.titleText = Dom.get(this.id + "-title-span");
         this.widgets.titleImg = Dom.get(this.id + "-title-img");

         // Set title and icon
         this.widgets.titleText.innerHTML = "<span><span>" + $html(this.options.name) + "</span><span class='title-note'>(förhandsgranskning som PDF)</span></span>";
         this.widgets.titleImg.src = Alfresco.constants.URL_RESCONTEXT + this.options.icon.substring(1);

         // Parameter nodeRef is mandatory
         if (this.options.nodeRef === undefined) {
            throw new Error("A nodeRef must be provided");
         }

         /**
          * To support full window mode an extra div (realSwfDivEl) is created with absolute positioning
          * which will have the same position and dimensions as shadowSfwDivEl.
          * The realSwfDivEl element is to make sure the flash move is on top of all other divs and
          * the shadowSfwDivEl element is to make sure the previewer takes the screen real estate it needs.
          */
         if (!this.widgets.realSwfDivEl) {
            var realSwfDivEl = new Element(document.createElement("div"));
            realSwfDivEl.set("id", this.id + "-real-swf-div");
            realSwfDivEl.setStyle("position", "absolute");
            realSwfDivEl.addClass("web-preview");
            realSwfDivEl.addClass("real");
            realSwfDivEl.appendTo(document.body);
            this.widgets.realSwfDivEl = realSwfDivEl;
         }
         this.widgets.shadowSfwDivEl = new Element(this.id + "-shadow-swf-div");

         if (this.options.size == "0") {
            // Shrink the web previewers real estate and tell user that node has no content
            this.widgets.shadowSfwDivEl.removeClass("has-content");
            this.widgets.realSwfDivEl.addClass("no-content");
            this.widgets.swfPlayerMessage.innerHTML = this.msg("label.noContent");
         } else if (this.options.mimeType.match(/^image\/jpeg$|^image\/png$|^image\/gif$/) && !this.displayImageInWebPreviewer()) {
            /**
             * Flash cannot display large images that exceeds a certain pixel dimension,
             * to avoid this we display images using the html img element instead.
             * ALF-7420 "Certain images will not preview in share, may be dpi/size related"
             * http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/flash/display/BitmapData.html?filter_coldfusion=9&filter_flex=3&filter_flashplayer=10&filter_air=1.5#top
             */
            this.setupImage();
            this.widgets.shadowSfwDivEl.removeClass("has-content");
            this.widgets.shadowSfwDivEl.removeClass("preview-swf");
            this.widgets.shadowSfwDivEl.addClass("image");
            this.widgets.realSwfDivEl.addClass("no-content");
         } else if (this._matchesConditions().length > 0) {
            var pluginDescriptors = this._matchesConditions();

            for (var x = 0; x < pluginDescriptors.length; x++) {
               var pluginDescriptor = pluginDescriptors[x];

               if (typeof Alfresco.WebPreview.prototype.Plugins[pluginDescriptor.name] != "function") {
                  continue;
               }

               var plugin = new Alfresco.WebPreview.prototype.Plugins[pluginDescriptor.name](this, pluginDescriptor.attributes);

               // Special case to ignore the WebPreviewer plugin on iOS - we don't want to report output either
               // as the output is simply an HTML message unhelpfully informing the user to install Adobe Flash
               if (YAHOO.env.ua.ios && pluginDescriptor.name === "WebPreviewer") {
                  continue;
               }

               // Make sure it may run in this browser...
               var report = plugin.report();

               if (report) {
                  continue;
               }

               this.widgets.shadowSfwDivEl.addClass("has-content");
               this.widgets.realSwfDivEl.removeClass("no-content");

               var markup = plugin.display();

               if (markup) {
                  Dom.get(this.id + '-real-swf-div').innerHTML = markup;
               }

               Dom.setStyle(this.id + '-real-swf-div', 'height', '670px');

               break;
            }
         } else if (Alfresco.util.hasRequiredFlashPlayer(9, 0, 124) || this.options.mimeType === 'application/pdf' || this.options.hasPdfRendition) {
            // Find the url to the preview
            var previewCtx = this._resolvePreview();

            if (previewCtx) {
               // Make sure the web previewers real estate is big enough for displaying something
               this.widgets.shadowSfwDivEl.addClass("has-content");
               this.widgets.realSwfDivEl.removeClass("no-content");

               var swfId = "WebPreviewer_" + this.id;

               this._setupPdfViewing(previewCtx, swfId);

               if (this.options.mimeType === 'application/pdf' || this.options.hasPdfRendition) {
                  // add a refresh button too, this is in the flash viewer but as we
                  // don't have that we have to add it ourselves
                  this._addRefreshButton();

                  this._addMaximiseButton();
               }

               /**
                * FF3 and SF4 hides the browser cursor if the flashmovie uses a custom cursor
                * when the flash movie is placed/hidden under a div (which is what happens if a dialog
                * is placed on top of the web previewer) so we must turn off custom cursor
                * when the html environment tells us to.
                */
               Event.addListener(swfId, "mouseover", function (e) {
                  var swf = Dom.get(swfId);
                  if (swf && YAHOO.lang.isFunction(swf.setMode)) {
                     Dom.get(swfId).setMode("active");
                  }
               });
               Event.addListener(swfId, "mouseout", function (e) {
                  var swf = Dom.get(swfId);
                  if (swf && YAHOO.lang.isFunction(swf.setMode)) {
                     Dom.get(swfId).setMode("inactive");
                  }
               });

               // Page unload / unsaved changes behaviour
               Event.addListener(window, "resize", function (e) {
                  YAHOO.Bubbling.fire("recalculatePreviewLayout");
               });
            } else {
               // Shrink the web previewers real estate and tell user that the node has nothing to display
               this.widgets.shadowSfwDivEl.removeClass("has-content");
               this.widgets.realSwfDivEl.addClass("no-content");
               var url = Alfresco.constants.PROXY_URI + "api/node/content/" + this.options.nodeRef.replace(":/", "") + "/" + encodeURIComponent(this.options.name) + "?a=true";
               this.widgets.swfPlayerMessage.innerHTML = this.msg("label.noPreview", url);

               if (this.options.mimeType === 'application/pdf' || this.options.hasPdfRendition) {
                  // add a refresh button too, this is in the flash viewer but as we
                  // don't have that we have to add it ourselves
                  this._addRefreshButton();

                  this._addMaximiseButton();
               }
            }
         } else {
            // Shrink the web previewers real estate and tell user that no sufficient flash player is installed
            this.widgets.shadowSfwDivEl.removeClass("has-content");
            this.widgets.realSwfDivEl.addClass("no-content");
            this.widgets.swfPlayerMessage.innerHTML = this.msg("label.noFlash");

            if (this.options.mimeType === 'application/pdf' || this.options.hasPdfRendition) {
               // add a refresh button too, this is in the flash viewer but as we
               // don't have that we have to add it ourselves
               this._addRefreshButton();

               this._addMaximiseButton();
            }
         }

         // Place the real flash preview div on top of the shadow div
         this._positionOver(this.widgets.realSwfDivEl, this.widgets.shadowSfwDivEl);
      },

      _matchesConditions: function() {
         var result = [];

         for (var i = 0, il = this.options.pluginConditions.length; i <il ; i++) {
            var condition = this.options.pluginConditions[i];

            if (condition.attributes.mimeType && condition.attributes.mimeType == this.options.mimeType) {
               result = condition.plugins;

               break;
            }
         }

         return result;
      },

      _addRefreshButton: function () {
         var refresh_button = new Element(document.createElement("span"));
         refresh_button.appendTo(this.widgets.titleText.parentNode);
         refresh_button.addClass("refresh-button");
         Dom.setAttribute(refresh_button, "id", "refresh-button-" + this.id);
         Dom.setAttribute(refresh_button, "alt", this.msg("button.refreshPage"));
         Dom.setAttribute(refresh_button, "title", this.msg("button.refreshPage"));

         var refresh_button_image = new Element(document.createElement("img"));
         refresh_button_image.appendTo(refresh_button);
         Dom.setAttribute(refresh_button_image, "src", Alfresco.constants.URL_RESCONTEXT + "components/preview/refresh.png");
         var self = this;
         Event.addListener("refresh-button-" + this.id, "click", function (e) {
            var newurl = self._updateUrl(document.location.href, "refresh", Math.floor((Math.random() * 10000) + 1));
            document.location = newurl;
         });
      },

      _addMaximiseButton: function() {
         var maximise_button = new Element(document.createElement("span"));
         maximise_button.appendTo(this.widgets.titleText.parentNode);
         maximise_button.addClass("maximise-button");

         Dom.setAttribute(maximise_button, "id", "maximise-button-" + this.id);
         Dom.setAttribute(maximise_button, "alt", this.msg("button.maximisePage"));
         Dom.setAttribute(maximise_button, "title", this.msg("button.maximisePage"));

         var maximise_button_image = new Element(document.createElement("img"));
         maximise_button_image.appendTo(maximise_button);

         Dom.setAttribute(maximise_button_image, "src", Alfresco.constants.URL_RESCONTEXT + "components/preview/maximise.png");

         var self = this;

         Event.addListener("maximise-button-" + this.id, "click", function (e) {
            window.open(Alfresco.constants.URL_PAGECONTEXT + "pdf-maximise?nodeRef=" + self.options.nodeRef, "_blank");
         });
      },

      _updateUrl: function (currUrl, param, paramVal) {
         var url = currUrl;
         var newAdditionalURL = "";
         var tempArray = url.split("?");
         var baseURL = tempArray[0];
         var aditionalURL = tempArray[1];
         var temp = "";
         if (aditionalURL) {
            var tempArray = aditionalURL.split("&");
            for (i = 0; i < tempArray.length; i++) {
               if (tempArray[i].split('=')[0] != param) {
                  newAdditionalURL += temp + tempArray[i];
                  temp = "&";
               }
            }
         }
         var rows_txt = temp + "" + param + "=" + paramVal;
         var finalURL = baseURL + "?" + newAdditionalURL + rows_txt;
         return finalURL;
      },

      _setupSwfObject: function (previewCtx, swfId) {
         // Create flash web preview by using swfobject
         var so = new YAHOO.deconcept.SWFObject(Alfresco.constants.URL_CONTEXT + "components/preview/WebPreviewer.swf", swfId, "100%", "100%", "9.0.45");
         so.addVariable("fileName", this.options.name);
         so.addVariable("paging", previewCtx.paging);
         so.addVariable("url", previewCtx.url);
         so.addVariable("jsCallback", "Alfresco.util.ComponentManager.get('" + this.id + "').onWebPreviewerEvent");
         so.addVariable("jsLogger", "Alfresco.util.ComponentManager.get('" + this.id + "').onWebPreviewerLogging");
         so.addVariable("i18n_actualSize", this.msg("preview.actualSize"));
         so.addVariable("i18n_fitPage", this.msg("preview.fitPage"));
         so.addVariable("i18n_fitWidth", this.msg("preview.fitWidth"));
         so.addVariable("i18n_fitHeight", this.msg("preview.fitHeight"));
         so.addVariable("i18n_fullscreen", this.msg("preview.fullscreen"));
         so.addVariable("i18n_fullwindow", this.msg("preview.fullwindow"));
         so.addVariable("i18n_fullwindow_escape", this.msg("preview.fullwindowEscape"));
         so.addVariable("i18n_page", this.msg("preview.page"));
         so.addVariable("i18n_pageOf", this.msg("preview.pageOf"));
         so.addVariable("show_maximise_button", true);
         so.addVariable("show_fullwindow_button", true);
         so.addVariable("disable_i18n_input_fix", this.disableI18nInputFix());
         so.addParam("allowScriptAccess", "sameDomain");
         so.addParam("allowFullScreen", "true");
         so.addParam("wmode", "transparent");

         // Finally create (or recreate) the flash web preview in the new div
         this.widgets.swfPlayerMessage.innerHTML = "";
         so.write(this.widgets.realSwfDivEl.get("id"));
         this.widgets.swfObject = so;
      },

      _setupPdfViewing: function (previewCtx, swfId) {
         // try to embed via Adobe PDF plugin
         try {
            this.widgets.pdfobject = new PDFObject({
               // FIXME: actual document url
               url: previewCtx.url,
               height: "670px",
               pdfOpenParams: {
                  view: "Fit",
                  toolbar: 1
               }
            }).embed(this.id + '-real-swf-div');

            this.widgets.pdfobject.id = swfId;
         } catch (e) {
            this.widgets.pdfobject = false;
         }

         if (this.widgets.pdfobject !== false) {
            // plugin worked!
            this.options.type = "plugin";

            // adjust div height
            Dom.setStyle(this.id + '-real-swf-div', 'height', '670px');
         } else {
            // no reader? Let's try PDF.js
            if (this._canvasSupport()) {
               this._setupPDFjs(previewCtx, swfId);
               this.options.type = "pdfjs";
               Dom.setStyle(this.id + '-real-swf-div', 'height', '670px');
            }
         }
      },

      /**
       * Tests if the pdf.js can be used in the users browser.
       *
       * @method report
       * @return boolean
       * @public
       */
      _canvasSupport: function () {
         // Test if canvas is supported
         if (window.HTMLCanvasElement) {
            // Do some engine test as well, some support canvas but not the
            // rest for full html5
            if (YAHOO.env.ua.webkit > 0 && YAHOO.env.ua.webkit < 534) {
               // http://en.wikipedia.org/wiki/Google_Chrome
               // Guessing for the same for safari
               return false;
            }
            if (YAHOO.env.ua.ie > 0 && YAHOO.env.ua.ie < 9) {
               return false;
            }
            if (YAHOO.env.ua.gecko > 0 && YAHOO.env.ua.gecko < 5) {
               // http://en.wikipedia.org/wiki/Gecko_(layout_engine)
               return  false;
            }

            return true
         }

         return false;
      },

      _setupPDFjs: function (previewCtx, swfId) {
         //iframe to the pdf viewer page
         Dom.get(this.id + '-real-swf-div').innerHTML = this._PDFViewerFrame(previewCtx, swfId);
      },

      /**
       * Constructs iframe for pdfjs support
       */
      _PDFViewerFrame: function (previewCtx, swfId) {
         // html5 is supported, display with pdf.js
         // id and name needs to be equal, easier if you need scripting access
         // to iframe
         var displaysource = '<iframe id="' + swfId + '" name="PdfJs" src="' + this._PDFViewerFrameURL(previewCtx) + '"' + ' scrolling="yes" marginwidth="0" marginheight="0" frameborder="0" vspace="5" hspace="5"  style="height: 670px; width: 100%">' + '</iframe>';

         return displaysource;
      },

      _PDFViewerFrameURL: function (previewCtx) {
         return Alfresco.constants.URL_PAGECONTEXT + 'pdfviewer?file=' + encodeURIComponent(previewCtx.url);
      },

      /**
       *
       * Overriding this method to implement a os/browser version dependent version that decides
       * if the i18n fix described for the disableI18nInputFix option shall be disabled or not.
       *
       * @method disableI18nInputFix
       * @return false by default if the config hasn't changed
       */
      disableI18nInputFix: function WP__resolvePreview(event) {
         // Override this method if you want to turn off the fix for a specific client
         return this.options.disableI18nInputFix;
      },

      /**
       * Checks if it is an image that is about to be displayed and i it shall be displayed using
       * the WebPreviewer or an <img> tag.
       *
       * @method displayImageInWebPreviewer
       * @return
       */
      displayImageInWebPreviewer: function () {
         return this.options.displayImageInWebPreview || !Alfresco.util.hasRequiredFlashPlayer(9, 0, 124);
      },

      /**
       * Display an image as a html element.
       *
       * @method display
       * @public
       */
      setupImage: function Image_setupImage() {
         var contentUrl = Alfresco.constants.PROXY_URI + "api/node/content/" + this.options.nodeRef.replace(":/", "") + "?c=force&noCache=" + new Date().getTime(), downloadUrl = contentUrl + "&a=true", imgpreview = Alfresco.constants.PROXY_URI + "api/node/" + this.options.nodeRef.replace(":/", "") + "/content/thumbnails/imgpreview?noCache=" + new Date().getTime() + "&c=force", previewUrl = Alfresco.util.arrayContains(this.options.previews, ["imgpreview"]) ? imgpreview : contentUrl;

         var html = '';
         if (previewUrl == imgpreview) {
            // Display the "imgpreview" thumbnail
            html = '<img src="' + previewUrl + '" alt="' + this.options.name + '" title="' + this.options.name + '"/>';
         } else {
            if (this.options.size > this.options.maxImageSizeToDisplay) {
               // The node's content was about to be used and its to big to display
               html += this.msg("image.tooLargeFile", this.options.name, Alfresco.util.formatFileSize(this.options.size));
               html += '<br/>';
               html += '<a class="theme-color-1" href="' + downloadUrl + '">';
               html += this.msg("image.downloadLargeFile");
               html += '</a>';
               html += '<br/>';
               html += '<a style="cursor: pointer;" class="theme-color-1" onclick="javascript: this.parentNode.parentNode.innerHTML = \'<img src=' + contentUrl + '>\';">';
               html += this.msg("image.viewLargeFile");
               html += '</a>';
               html = '<div class="message">' + html + '</div>';
            } else {
               // We can display the node's content directly since it was relatively small
               html = '<img src="' + previewUrl + '" alt="' + this.options.name + '" title="' + this.options.name + '"/>';
            }
         }
         Dom.get(this.id + "-shadow-swf-div").innerHTML = html;
      },

      /**
       * Helper method for deciding what preview to use, if any
       *
       * @method _resolvePreview
       * @return the name of the preview to use or null if none is appropriate
       */
      _resolvePreview: function WP__resolvePreview(event) {
         var ps = this.options.previews, webpreview = "webpreview", imgpreview = "imgpreview", pdf = "pdf", nodeRefAsLink = this.options.nodeRef.replace(":/", ""), argsNoCache = "?c=force&noCacheToken=" + new Date().getTime(), preview, url;

         if (this.options.mimeType.match(/^image\/jpeg$|^image\/png$|^image\/gif$/)) {
            /* The content matches an image mimetype that the web-previewer can handle without a preview */
            url = Alfresco.constants.PROXY_URI + "api/node/" + nodeRefAsLink + "/content" + argsNoCache;
            return (
            {
               url: url,
               paging: false
            });
         } else if (this.options.mimeType === 'application/pdf' || this.options.hasPdfRendition) {
            url = this._getPdfUrl();
            return ({
               url: url,
               paging: false
            });
         } else if (this.options.mimeType.match(/application\/x-shockwave-flash/)) {
            url = Alfresco.constants.PROXY_URI + "api/node/content/" + nodeRefAsLink + argsNoCache + "&a=false";
            return (
            {
               url: url,
               paging: false
            });
            return null;
         } else {
            preview = Alfresco.util.arrayContains(ps, pdf) ? "pdf" : (Alfresco.util.arrayContains(ps, imgpreview) ? imgpreview : null);

            if (!preview) {
               preview = Alfresco.util.arrayContains(ps, webpreview) ? webpreview : (Alfresco.util.arrayContains(ps, imgpreview) ? imgpreview : null);
            }

            if (preview !== null) {
               url = Alfresco.constants.PROXY_URI + "api/node/" + nodeRefAsLink + "/content/thumbnails/" + preview + argsNoCache;
               return (
               {
                  url: url,
                  paging: true
               });
            }
            return null;
         }
      },

      _getPdfUrl: function () {
         var url = "";

         if (this.options.mimeType === 'application/pdf') {
            url = Alfresco.util.contentURL(this.options.nodeRef, this.options.name);
         } else {
            var extension = Alfresco.util.getFileExtension(this.options.name);

            var filename = this.options.name.replace(extension, "pdf");

            url = Alfresco.util.contentURL(this.options.nodeRef, filename) + "?streamId=" + this.options.pdfRendition;
         }

         return url;
      },

      /**
       * Helper method for plugins to create url tp the node's content.
       *
       * @method getContentUrl
       * @param {Boolean} (Optional) Default false. Set to true if the url shall be constructed so it forces the
       *        browser to download the document, rather than displaying it inside the browser.
       * @return {String} The "main" element holding the actual previewer.
       * @public
       */
      getContentUrl: function WP_getContentUrl(download)
      {
         var proxy = window.location.protocol + "//" + window.location.host + Alfresco.constants.URL_CONTEXT + "proxy/alfresco/",
            nodeRefAsLink = this.options.nodeRef.replace(":/", ""),
            noCache = "noCache=" + new Date().getTime();
         download = download ? "a=true" : "a=false";
         return proxy + "api/node/" + nodeRefAsLink + "/content/" + this.options.name + "?c=force&" + noCache + "&" + download
      },

      /**
       * Helper method for plugins to create a url to the thumbnail's content.
       *
       * @param thumbnail {String} The thumbnail definition name
       * @param fileSuffix {String} (Optional) I.e. ".png" if shall be inserted in the url to make certain flash
       *        plugins understand the mimetype of the thumbnail.
       * @return {String} The url to the thumbnail content.
       * @public
       */
      getThumbnailUrl: function WP_getThumbnailUrl(thumbnail, fileSuffix)
      {
         var proxy = window.location.protocol + "//" + window.location.host + Alfresco.constants.URL_CONTEXT + "proxy/alfresco/",
            nodeRefAsLink = this.options.nodeRef.replace(":/", ""),
            noCache = "noCache=" + new Date().getTime(),
            force = "c=force";

         // return proxy + "api/node/" + nodeRefAsLink + "/content/thumbnails/" + thumbnail + (fileSuffix ? "/suffix" + fileSuffix : "") + "?" + force + "&" + noCache
         return null;
      },

      /**
       * Makes it possible for plugins to get hold of the "previewer wrapper" HTMLElement.
       *
       * I.e. Useful for elements that use an "absolute" layout for their plugins (most likely flash), so they have
       * an element in the Dom to position their own elements after.
       *
       * @method getPreviewerElement
       * @return {HTMLElement} The "main" element holding the actual previewer.
       * @public
       */
      getPreviewerElement: function()
      {
         return Dom.get(this.id + '-real-swf-div');
      },

      /**
       * Called from the WebPreviewer when a log message has been logged.
       *
       * @method onWebPreviewerLogging
       * @param msg {string} The log message
       * @param level {string} The log level
       */
      onWebPreviewerLogging: function WP_onWebPreviewerLogging(msg, level) {
         if (YAHOO.lang.isFunction(Alfresco.logger[level])) {
            Alfresco.logger[level].call(Alfresco.logger, "WebPreviewer: " + msg);
         }
      },

      /**
       * Called from the WebPreviewer when an event or error is dispatched.
       *
       * @method onWebPreviewerEvent
       * @param event {object} an WebPreview message
       */
      onWebPreviewerEvent: function WP_onWebPreviewerEvent(event) {
         if (event.event) {
            if (event.event.type == "onFullWindowClick") {
               var clientRegion = Dom.getClientRegion();
               this.widgets.realSwfDivEl.setStyle("left", clientRegion.left + "px");
               this.widgets.realSwfDivEl.setStyle("top", clientRegion.top + "px");
               this.widgets.realSwfDivEl.setStyle("width", "100%");
               this.widgets.realSwfDivEl.setStyle("height", "100%");
            } else if (event.event.type == "onFullWindowEscape") {
               this._positionOver(this.widgets.realSwfDivEl, this.widgets.shadowSfwDivEl);
            }
         } else if (event.error) {
            // Inform the user about the failure
            var message = "Error";
            if (event.error.code) {
               message = this.msg("error." + event.error.code);
            }
            Alfresco.util.PopupManager.displayMessage({
               text: message
            });

            // Tell other components that the preview failed
            YAHOO.Bubbling.fire("webPreviewFailure", {
               error: event.error.code,
               nodeRef: this.showConfig.nodeRef,
               failureUrl: this.showConfig.failureUrl
            });
         }
      },

      /**
       * Positions the one element over another
       *
       * @method _positionOver
       * @param event
       */
      _positionOver: function WP__positionOver(positionedYuiEl, sourceYuiEl) {
         var region = Dom.getRegion(sourceYuiEl.get("id"));
         positionedYuiEl.setStyle("left", region.left + "px");
         positionedYuiEl.setStyle("top", region.top + "px");
         positionedYuiEl.setStyle("width", region.width + "px");
         positionedYuiEl.setStyle("height", region.height + "px");
      }
   });
})();