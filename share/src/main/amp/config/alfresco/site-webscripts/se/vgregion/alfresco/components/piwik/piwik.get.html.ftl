<script type="text/javascript"> 

   if (window.console && console.log) {
      <#if config.global?keys?seq_contains('probe-host')>
      console.log("Probe host: ${config.global['probe-host'].value}");
      </#if>
   }

   if (document.location.hostname!=='localhost') {
      var _paq = _paq || [];
      
      try {
         var u = (("https:" == document.location.protocol) ? "https://piwik.vgregion.se/" : "http://piwik.vgregion.se/");
         
         _paq.push(['setSiteId', 'ALFRESCO']);
         _paq.push(['setTrackerUrl', u + 'piwik.php']);
         _paq.push(['trackPageView']);
         _paq.push(['enableLinkTracking']);
         
         var pw = document.createElement('script');
         var s = document.getElementsByTagName('script')[0];
         
         pw.type = 'text/javascript';
         pw.defer = true;
         pw.async = true;
         pw.src = u + 'piwik.js';
         
         s.parentNode.insertBefore(pw, s);
      } catch (err) {
      }
   }   
</script>
