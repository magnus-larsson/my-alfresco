<#-- @overridden projects/slingshot/config/alfresco/site-webscripts/org/alfresco/components/footer/footer.get.html.ftl -->

<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/modules/about-share.css" group="footer"/>
   <@link href="${url.context}/res/components/footer/footer.css" group="footer"/>
</@>

<@markup id="js">
   <@script src="${url.context}/res/modules/about-share.js" group="footer"/>
</@>

<@markup id="widgets">
   <@createWidgets/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#assign fc=config.scoped["Edition"]["footer"]>
      <div class="footer ${fc.getChildValue("css-class")!"footer-com"}">
         <span class="copyright">
            <a href="#" onclick="Alfresco.module.getAboutShareInstance().show(); return false;"><img src="${url.context}/res/components/images/${fc.getChildValue("logo")!"alfresco-share-logo.png"}" alt="${fc.getChildValue("alt-text")!"Alfresco Community"}" border="0"/></a>
            <#if licenseHolder != "" && licenseHolder != "UNKNOWN">
               <span class="licenseHolder">${msg("label.licensedTo")} ${licenseHolder}</span><br>
            </#if>
            <span>${msg(fc.getChildValue("label")!"label.copyright")}</span>
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
      if (document.location.hostname!=='localhost') {
      	var pkBaseURL = (("https:" == document.location.protocol) ? "https://piwik.vgregion.se/" : "http://piwik.vgregion.se/");
      	document.write(unescape("%3Cscript src='" + pkBaseURL + "piwik.js' type='text/javascript'%3E%3C/script%3E"));
      }
      </script><script type="text/javascript">
      if (document.location.hostname!=='localhost') {
      	try {
      		var piwikTracker = Piwik.getTracker(pkBaseURL + "piwik.php", 7);
      		piwikTracker.trackPageView();
      		piwikTracker.enableLinkTracking();
      	} catch( err ) {}
      }
      </script><noscript><p><img src="http://piwik.vgregion.se/piwik.php?idsite=7" style="border:0" alt="" /></p></noscript>
      <!-- End Piwik Tracking Code -->
   </@>
</@>
