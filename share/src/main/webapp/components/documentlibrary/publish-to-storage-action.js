/**
 * DocumentList "Publish To Storage" action
 *
 * @namespace Alfresco
 * @class Alfresco.DocumentList
 */
(function () {

  /**
   * Publish multiple documents/folders to the Storage.
   * Toolbar action
   * @method onActionPublishToStorage
   * @param file
   *          {object} DataTable row representing file to be actioned
   */
  var onActionPublishToStorage = function DLT_onActionPublishToStorage(files, options) {
    if (!YAHOO.lang.isArray(files)) {
      files = [files];
    }

    //always create a new dialog since the data is loaded with the template
    var pub = new Alfresco.thirdparty.PublishToStorage(this.id + "-publishToStorage");

    if (options) {
      options.files = files;
      pub.setOptions(options);
    } else {
      pub.setOptions({ files: files });
    }
    pub.showDialog();
    return pub;
  };

  /**
   * Revoke publication of folders/document
   * Toolbar action
   * @method onActionUnpublishToStorage
   * @param file
   *          {object} DataTable row representing file to be actioned
   */
  var onActionUnpublishFromStorage = function DLT_onActionUnpublishFromStorage(files, options) {
    if (!YAHOO.lang.isArray(files)) {
      files = [files];
    }

    // always create a new dialog since the data is loaded with the template
    // Unpublishing uses the same object as publishing since they share so much code
    var pub = new Alfresco.thirdparty.PublishToStorage(this.id + "-unpublishFromStorage");

    if (options) {
      options.files = files;
      options.revoke = true; //this tells the object to revoke, not publish
      pub.setOptions(options);
    } else {
      pub.setOptions({ files: files, revoke: true });
    }
    pub.showDialog();
    return pub;
  };

  var onActionDeleteChecked = function (assets, oldDelete, options) {

    var component_name = this.name;

    // show "please wait dialog"
    var feedbackMessage = Alfresco.util.PopupManager.displayMessage({
      text: Alfresco.util.message("message.checking.published", component_name),
      spanClass: "wait",
      displayTime: 0
    });

    if (!options) {
      options = {};
    }

    if (!YAHOO.lang.isArray(assets)) {
      files = [assets];
    } else {
      files = assets;
    }

    // setup options and PublishToStorage object
    options.revoke = true;
    options.files = files;

    //callback after success
    var me = this;
    olddialog = function () {
      feedbackMessage.hide();
      oldDelete.call(me, assets);
    };

    options.successCallback = olddialog;

    var pub = new Alfresco.thirdparty.PublishToStorage(this.id + "-unpublishFromStorage");
    pub.setOptions(options);

    var dialog = function () {
      feedbackMessage.hide();
      Alfresco.util.PopupManager.displayPrompt({
        title: Alfresco.util.message("message.confirm.delete.published.title", component_name),
        text: Alfresco.util.message("message.confirm.delete.published", component_name),
        buttons: [
          {
            text: Alfresco.util.message("button.publish-to-storage.delete", component_name),
            handler: function dlA_onActionDeleteChecked_publish() {
              this.destroy();
              pub.showDialog();
            }
          },
          {
            text: Alfresco.util.message("button.delete"),
            handler: function dlA_onActionDelete_delete() {
              this.destroy();
              me._onActionDeleteConfirm.call(me, assets);
            }
          },
          {
            text: Alfresco.util.message("button.cancel"),
            handler: function dlA_onActionDelete_cancel() {
              this.destroy();
            },
            isDefault: true
          }
        ]
      });
    };

    // first check if the supplied file or folder contains published functions
    pub.checkPublishedStatus(dialog, olddialog, true);
  };

  var onActionAutoPublish = function DLT_onActionAutoPublish(folder) {
    var autoPublish = new Alfresco.thirdparty.AutoPublish(this.id + "_auto_publish").setOptions({
      'folder': folder
    });

    autoPublish.showDialog();

    return autoPublish;
  };

  // documentlibrary
  if (Alfresco.DocumentList) {
    Alfresco.DocumentList.prototype.onActionPublishToStorage = onActionPublishToStorage;
    Alfresco.DocumentList.prototype.onActionUnpublishFromStorage = onActionUnpublishFromStorage;
    Alfresco.DocumentList.prototype.onActionAutoPublish = function (folder) {
      onActionAutoPublish.call(this, folder);
    };

    // monkey patch remove so that we can check for published documents
    var oldDelete = Alfresco.DocumentList.prototype.onActionDelete;
    Alfresco.DocumentList.prototype.onActionDelete = function (assets) {
      onActionDeleteChecked.call(this, assets, oldDelete);
    };
  }

  // toolbar
  if (Alfresco.DocListToolbar) {
    Alfresco.DocListToolbar.prototype.onActionPublishToStorage = onActionPublishToStorage;
    Alfresco.DocListToolbar.prototype.onActionUnpublishFromStorage = onActionUnpublishFromStorage;
    var oldToolbarDelete = Alfresco.DocListToolbar.prototype.onActionDelete;
    Alfresco.DocListToolbar.prototype.onActionDelete = function (assets) {
      onActionDeleteChecked.call(this, assets, oldToolbarDelete);
    };
  }

  // Document details
  if (Alfresco.DocumentActions) {
    Alfresco.DocumentActions.prototype.onActionPublishToStorage = function (files) {
      onActionPublishToStorage(files, { reload: true });
    };

    Alfresco.DocumentActions.prototype.onActionUnpublishFromStorage = function (files) {
      onActionUnpublishFromStorage(files, { reload: true });
    };

    var oldDetailDelete = Alfresco.DocumentActions.prototype.onActionDelete;
    Alfresco.DocumentActions.prototype.onActionDelete = function (assets) {
      onActionDeleteChecked.call(this, assets, oldDetailDelete);
    };
  }

  // folder details
  if (Alfresco.FolderActions) {
    Alfresco.FolderActions.prototype.onActionAutoPublish = function (folder) {
      onActionAutoPublish(folder, {});
    };
  }

})();
