(function() {

  var VGR = {};

  // Get publish status
  VGR.getPublishStatus = function(nodeRef, imagePlaceholder) {
    // Create ajax call to show correct publish status
    Alfresco.util.Ajax.jsonGet({
      url : Alfresco.constants.PROXY_URI + "vgr/publishstatus/"
          + nodeRef.replace("://", "/"),
      successCallback : {
        fn : function(res) {
          var result = res.json.result;
          
          if (result == "ERROR") {
            imagePlaceholder.src = Alfresco.constants.URL_RESCONTEXT + "components/documentlibrary/indicators/published-to-storage-error-16.png";
            imagePlaceholder.title = Alfresco.util.message("status.vgr-publish-status-error");   
          } else if (result == "PUBLISH_ERROR") {
            imagePlaceholder.src = Alfresco.constants.URL_RESCONTEXT + "components/documentlibrary/indicators/published-to-storage-error-16.png";
            imagePlaceholder.title = Alfresco.util.message("status.vgr-publish-status-publish-error");   
          } else if (result == "UNPUBLISH_ERROR") {
            imagePlaceholder.src = Alfresco.constants.URL_RESCONTEXT + "components/documentlibrary/indicators/published-to-storage-error-16.png";
            imagePlaceholder.title = Alfresco.util.message("status.vgr-publish-status-unpublish-error");   
          } else if (result == "PUBLISHED") {
            imagePlaceholder.src = Alfresco.constants.URL_RESCONTEXT + "components/documentlibrary/indicators/published-to-storage-16.png";
            imagePlaceholder.title = Alfresco.util.message("status.vgr-publish-status-published");   
          } else if (result == "UNPUBLISHED") {
            imagePlaceholder.src = Alfresco.constants.URL_RESCONTEXT + "components/documentlibrary/indicators/published-to-storage-before-16.png";
            imagePlaceholder.title = Alfresco.util.message("status.vgr-publish-status-unpublished");   
          } else if (result == "SENT_FOR_PUBLISH") {
            imagePlaceholder.src = Alfresco.constants.URL_RESCONTEXT + "components/documentlibrary/indicators/published-to-storage-future-16.png";
            imagePlaceholder.title = Alfresco.util.message("status.vgr-publish-status-sent-for-publish");   
          } else if (result == "SENT_FOR_UNPUBLISH") {
            imagePlaceholder.src = Alfresco.constants.URL_RESCONTEXT + "components/documentlibrary/indicators/unpublished-from-storage-future-16.png";
            imagePlaceholder.title = Alfresco.util.message("status.vgr-publish-status-sent-for-unpublish");   
          } else if (result == "PREVIOUSLY_PUBLISHED") {
            imagePlaceholder.src = Alfresco.constants.URL_RESCONTEXT + "components/documentlibrary/indicators/published-to-storage-before-16.png";
            imagePlaceholder.title = Alfresco.util.message("status.vgr-publish-status-unpublished");   
          } else if (result == "PREVIOUS_VERSION_PUBLISHED") {
            imagePlaceholder.src = Alfresco.constants.URL_RESCONTEXT + "components/documentlibrary/indicators/published-to-storage-older-version-16.png";
            imagePlaceholder.title = Alfresco.util.message("status.vgr-publish-status-previous-version-published");   
          }
          
          
  /**
   * <#if vgr_publish.result?? && vgr_publish.result=="ERROR"><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/published-to-storage-error-16.png" alt="Fel vid publicering / avpublicering" title="Fel vid publicering / avpublicering" /></#if>
      <#if vgr_publish.result?? && vgr_publish.result=="PUBLISH_ERROR"><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/published-to-storage-error-16.png" alt="Fel vid publicering" title="Fel vid publicering" /></#if>
      <#if vgr_publish.result?? && vgr_publish.result=="UNPUBLISH_ERROR"><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/published-to-storage-error-16.png" alt="Fel vid avpublicering" title="Fel vid avpublicering" /></#if>
      <#if vgr_publish.result?? && vgr_publish.result=="PUBLISHED"><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/published-to-storage-16.png" alt="Publicerad" title="Publicerad" /></#if>
      <#if vgr_publish.result?? && vgr_publish.result=="UNPUBLISHED"><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/published-to-storage-before-16.png" alt="Tidigare version har varit publicerad" title="Tidigare version har varit publicerad" /></#if>
      <#if vgr_publish.result?? && vgr_publish.result=="SENT_FOR_PUBLISH"><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/published-to-storage-future-16.png" alt="Denna version kommer att bli publicerad" title="Denna version kommer att bli publicerad" /></#if>
      <#if vgr_publish.result?? && vgr_publish.result=="SENT_FOR_UNPUBLISH"><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/unpublished-from-storage-future-16.png" alt="Denna version kommer att bli avpublicerad" title="Denna version kommer att bli avpublicerad" /></#if>
      <#if vgr_publish.result?? && vgr_publish.result=="PREVIOUSLY_PUBLISHED"><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/published-to-storage-before-16.png" alt="Tidigare version har varit publicerad" title="Tidigare version har varit publicerad" /></#if>
      <#if vgr_publish.result?? && vgr_publish.result=="PREVIOUS_VERSION_PUBLISHED"><img id="${el}-title-published-img" src="${url.context}/res/components/documentlibrary/images/published-to-storage-older-version-16.png" alt="Tidigare version är publicerad" title="Tidigare version är publicerad" /></#if>
   */
        },
        scope : this
      },
      failureCallback : {
        fn : function(res) {

        },
        scope : this
      }
    });
    var test = "";
  };

  // Find placeholders
  VGR.updateDocumentPublishStatus = function() {
    var records = YAHOO.util.Selector.query('tr.yui-dt-rec');
    for (i = 0; i < records.length; i++) {
      var record = records[i];
      var nodeRef = YAHOO.util.Selector.query('input', record)[0].value;
      var divStatus = YAHOO.util.Selector.query('div.status', record)[0];
      var imgPlaceholders = YAHOO.util.Selector.query('img', divStatus);
      for (j = 0; j < imgPlaceholders.length; j++) {
        var imgPlaceholder = imgPlaceholders[0];
        if (imgPlaceholder.alt == "vgr-publish-status-placeholder") {
          VGR.getPublishStatus(nodeRef, imgPlaceholder);
          // We have found our placeholder so break out
          break;
        }
      }
      var test = "";
    }
  };

  // Bind to events
  VGR.subscribeToRenderEvent = function(args) {
    var scope = arguments[1][1].scope;
    scope.widgets.dataTable.subscribe("renderEvent",
        VGR.updateDocumentPublishStatus, this, true);
  };
  YAHOO.Bubbling
      .on("postDocumentListOnReady", VGR.subscribeToRenderEvent, this);
})();