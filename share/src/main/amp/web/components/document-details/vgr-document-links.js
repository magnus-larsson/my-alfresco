/*
 * @overridden projects/slingshot/source/web/components/document-details/document-links.js
 */

/**
 * TODO write some docs
 */
(function(onReady) {

   Alfresco.DocumentLinks.prototype.onReady = function() {
      onReady.call(this);
      
      // Prefix some of the urls with values from the client
      Dom.get(this.id + "-page-url-text").innerHTML = document.location.href;
      
      var downloadUrl = Alfresco.constants.PROXY_URI + "api/node/content/" + this.options.nodeRef.replace(":/", "") + "/" + encodeURIComponent(this.options.fileName);
      
      Dom.get(this.id + "-download-url-text").innerHTML = downloadUrl + "?a=true";
      
      Dom.get(this.id + "-document-url-text").innerHTML = downloadUrl + "?a=false";
   };

}(Alfresco.DocumentLinks.prototype.onReady));
