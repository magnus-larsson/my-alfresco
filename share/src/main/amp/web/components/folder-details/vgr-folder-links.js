/**
 * TODO write some docs
 */
(function(onReady) {

   Alfresco.FolderLinks.prototype.onReady = function() {
      onReady.call(this);
      
      // Prefix some of the urls with values from the client
      Dom.get(this.id + "-page-url-text").innerHTML = document.location.href;
   };

}(Alfresco.FolderLinks.prototype.onReady));
