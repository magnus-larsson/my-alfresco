(function() {

   var Dom = YAHOO.util.Dom;
   var Event = YAHOO.util.Event;
   var Bubbling = YAHOO.Bubbling;
   var KeyListener = YAHOO.util.KeyListener;

   /**
    * Alfresco.thirdparty.PdfMaximise constructor.
    * 
    * @param {string}
    *           htmlId
    * @param {list}
    *           selected, already selected values
    * @param {function}
    *           data_loader, a function that loads dynamic data, takes a node.id as parent and a callback which takes a list of objects with least id and label attributes
    * @return {Alfresco.thirdparty.PdfMaximise} The new PdfMaximise self
    * @constructor
    */
   Alfresco.thirdparty.PdfMaximise = function(htmlId) {
      Alfresco.thirdparty.PdfMaximise.superclass.constructor.call(this, "Alfresco.thirdparty.PdfMaximise", htmlId, [ "button" ]);

      this.name = "Alfresco.thirdparty.PdfMaximise";
      this.htmlId = htmlId;
      this.id = htmlId;
      // array holding the values for the list
      this.selected = [];

      return this;
   };

   YAHOO.extend(Alfresco.thirdparty.PdfMaximise, Alfresco.component.Base, {

      widgets : {},

      /**
       * Object container for initialization options
       * 
       * @property options
       * @type object
       */
      options : {

         /**
          * @property nodeRef
          * @type string
          * @default ""
          */
         nodeRef : "",

         /**
          * @property name
          * @type string
          * @default ""
          */
         name : ""

      },

      getThumbnailUrl : function(thumbnail, fileSuffix) {
         var nodeRefAsLink = this.options.nodeRef.replace(":/", ""), noCache = "noCache=" + new Date().getTime(), force = "c=force";

         return Alfresco.constants.PROXY_URI + "api/node/" + nodeRefAsLink + "/content/thumbnails/" + thumbnail + (fileSuffix ? "/suffix" + fileSuffix : "") + "?" + noCache + "&" + force
      },

      onReady : function() {
         var url = this.getThumbnailUrl('pdf');

         console.log(url);

         // Register the ESC key to close the dialog
         if (!this.widgets.escapeListener) {
            this.widgets.escapeListener = new KeyListener(document, {
               keys : KeyListener.KEY.ESCAPE
            }, {
               fn : function(id, keyEvent) {
                  window.close();
               },
               scope : this,
               correctScope : true
            });
         }

         // Show the dialog
         this.widgets.escapeListener.enable();

         new PDFObject({
            url : url,
            height : YAHOO.util.Dom.getViewportHeight() + "px",
            width : YAHOO.util.Dom.getViewportWidth() + "px",
            pdfOpenParams : {
               view : "FitV",
               pagemode : "none",
               toolbar : "1",
               statusbar : "0",
               message : "0",
               navpanes : "0"
            }
         }).embed("pdf");
      }

   });

})();
