(function() {
  var VGR = {};

  // Get publish status
  VGR.getPublishStatus = function(nodeRef, imagePlaceholder) {
    // Create ajax call to show correct publish status
    if (imagePlaceholder!==null && imagePlaceholder!==undefined) {
      Alfresco.util.Ajax.jsonGet({
        url : Alfresco.constants.PROXY_URI + "vgr/publishstatus/" + nodeRef.replace("://", "/"),
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
            } else {
              //imagePlaceholder.className = imagePlaceholder.className+" hidden";
            }
          },
          scope : this
        },
        failureCallback : {
          fn : function(res) {

          },
          scope : this
        }
      });
    }
    var test = "";
  };

  // Find placeholders
  VGR.updateDocumentPublishStatusDocList = function() {
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
  
  //Find placeholder for details
  VGR.updateDocumentPublishStatusDetailedView = function(name, args) {
    VGR.getPublishStatus(args[1].nodeRef, args[1].imagePlaceholder);
  }

  // Bind to events
  VGR.subscribeToRenderEvent = function(args) {
    var scope = arguments[1][1].scope;
    scope.widgets.dataTable.subscribe("renderEvent", VGR.updateDocumentPublishStatusDocList, this, true);
  };
  YAHOO.Bubbling.on("postDocumentListOnReady", VGR.subscribeToRenderEvent, this);
  YAHOO.Bubbling.on("vgrUpdatePublishStatus", VGR.updateDocumentPublishStatusDetailedView, this);

})();