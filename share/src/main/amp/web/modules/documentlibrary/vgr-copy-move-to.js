(function() {
   var Dom = YAHOO.util.Dom;

   var isEmpty = function(ob) {
      for ( var i in ob) {
         if (ob.hasOwnProperty(i)) {
            return false;
         }
      }
      
      return true;
   };

})();
