YAHOO.util.Event.onDOMReady(function() {

   var anchors = YAHOO.util.Selector.query('ul.favourite-sites-list li a');
   
   for (var x = 0; x < anchors.length; x++) {
      var anchor = anchors[x];
      
      anchor.href = anchor.href.replace("/dashboard", "/default-redirect"); 
   }

});
