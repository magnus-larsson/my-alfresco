/*
 * @overridden projects/slingshot/source/web/components/profile/usersites.js
 */

/**
 * Adding a endsWith function to String 
 */
(function() {

  if (typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
      return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
  }
   
})();

/**
 * Replace dashboard url with default-redirect
 */
(function(onReady) {
  Alfresco.UserSites.prototype.onReady = function() {
    onReady.call(this);
    var anchors = YAHOO.util.Selector.query("li a", this.id+"-sites");
    for (var i=0;i<anchors.length;i++) {
      var anchor = anchors[i];
      if (anchor != null && anchor.href.endsWith("dashboard")) {
        anchor.href = anchor.href.replace("dashboard", "default-redirect");
      }
    }
  };
}(Alfresco.UserSites.prototype.onReady));
