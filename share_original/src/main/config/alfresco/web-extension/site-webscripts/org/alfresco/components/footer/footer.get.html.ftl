<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/footer/footer.get.html.ftl -->

<div class="footer footer-ent">
   <span class="copyright">
      <img src="${url.context}/components/images/alfresco-share-logo-enterprise.png" alt="Alfresco Enterprise" height="27" width="207" />
      <span>${msg("label.copyright.enterprise")}</span>
   </span>
</div>

<script type="text/javascript">
   if (window.console && console.log) {
      <#if config.global?keys?seq_contains('probe-host')>
      console.log("${config.global['probe-host'].value}");
      </#if>
   }
</script>

<!-- Piwik -->
<script type="text/javascript">
var pkBaseURL = (("https:" == document.location.protocol) ? "https://piwik.vgregion.se/" : "http://piwik.vgregion.se/");
document.write(unescape("%3Cscript src='" + pkBaseURL + "piwik.js' type='text/javascript'%3E%3C/script%3E"));
</script><script type="text/javascript">
try {
var piwikTracker = Piwik.getTracker(pkBaseURL + "piwik.php", 7);
piwikTracker.trackPageView();
piwikTracker.enableLinkTracking();
} catch( err ) {}
</script><noscript><p><img src="http://piwik.vgregion.se/piwik.php?idsite=7" style="border:0" alt="" /></p></noscript>
<!-- End Piwik Tracking Code -->
