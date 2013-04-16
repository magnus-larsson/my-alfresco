(function() {
  
  var Dom = YAHOO.util.Dom;

  Alfresco.DocListToolbar.prototype.onActionExport = function DL_onActionExport(assets) {
    // Save the nodeRefs
    var nodeRefs = [];
    for ( var i = 0, ii = assets.length; i < ii; i++) {
      nodeRefs.push(assets[i].nodeRef);
    }

    // Open the export dialog
    if (!this.modules.exportDialog) {

      // Load if for the first time
      this.modules.exportDialog = new Alfresco.module.SimpleDialog(this.id + "-exportDialog").setOptions({
        width : "45em",
        templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/documentlibrary/export",
        actionUrl : Alfresco.constants.PROXY_URI + "vgr/export",
        firstFocus : this.id + "-exportDialog-mets",
        doBeforeFormSubmit : {
          fn : function dLA_onActionExport_SimpleDialog_doBeforeFormSubmit() {
            // Close dialog now since no callback is provided since we are
            // submitting in a hidden iframe.
            this.modules.exportDialog.hide();
          },
          scope : this
        }
      });
    }

    // doBeforeDialogShow needs re-registering each time as nodeRefs array is
    // dynamic
    this.modules.exportDialog.setOptions({
      clearForm : true,
      doBeforeDialogShow : {
        fn : function dLA_onActionExport_SimpleDialog_doBeforeDialogShow(p_config, p_simpleDialog, p_obj) {
          // Set the hidden nodeRefs field to a comma-separated list of nodeRefs
          Dom.get(this.id + "-exportDialog-nodeRefs").value = p_obj.join(",");
          var failure = "window.parent.Alfresco.util.ComponentManager.get('" + this.id + "')";
          Dom.get(this.id + "-exportDialog-failureCallbackFunction").value = failure + ".onExportFailure";
          Dom.get(this.id + "-exportDialog-failureCallbackScope").value = failure;
        },
        obj : nodeRefs,
        scope : this
      }
    });

    this.modules.exportDialog.show();
  };

  Alfresco.DocListToolbar.prototype.onExportFailure = function DL_onExportFailure(error) {
    Alfresco.util.PopupManager.displayPrompt({
      title : this.msg("message.failure"),
      text : error.message
    });
  };

})();