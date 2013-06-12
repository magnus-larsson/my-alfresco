/**
 *
 *
 * @namespace Alfresco.module
 * @class Alfresco.thirdparty.PublishToStorage
 */
(function () {
  /**
   * YUI Library aliases
   */
  var Dom = YAHOO.util.Dom, KeyListener = YAHOO.util.KeyListener, Selector = YAHOO.util.Selector;

  /**
   * Alfresco Slingshot aliases
   */
  var $html = Alfresco.util.encodeHTML, $combine = Alfresco.util.combinePaths, $hasEventInterest = Alfresco.util.hasEventInterest;

  Alfresco.thirdparty.PublishToStorage = function (htmlId) {
    Alfresco.thirdparty.PublishToStorage.superclass.constructor.call(this, "Alfresco.thirdparty.PublishToStorage", htmlId, ["button", "container", "connection", "json", "treeview"]);

    // Initialise prototype properties
    this.containers = {};

    // Decoupled event listeners
    if (htmlId != "null") {
      this.eventGroup = htmlId;

    }
    this.id = htmlId;

    return this;
  };

  YAHOO.extend(Alfresco.thirdparty.PublishToStorage, Alfresco.component.Base, {
    /**
     * Object container for initialization options
     */
    options: {


      /**
       * Width for the dialog
       *
       * @property width
       * @type string
       * @default 60em
       */
      width: "60em",

      /**
       * Files to action
       *
       * @property files
       * @type object
       * @default null
       */
      files: null,

      /**
       * Template URL
       *
       * @property templateUrl
       * @type string
       * @default Alfresco.constants.URL_SERVICECONTEXT + "modules/documentlibrary/publish-to-storage"
       */
      templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/documentlibrary/publish-to-storage",

      /**
       * Template URL for revoking publication
       *
       * @property revokeTemplateUrl
       * @type string
       * @default Alfresco.constants.URL_SERVICECONTEXT + "modules/documentlibrary/unpublish-from-storage"
       */
      revokeTemplateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/documentlibrary/unpublish-from-storage",

      /**
       * Published service URL
       *
       * @property publishedUrl
       * @type string
       * @default Alfresco.constants.PROXY_URI + "vgr/publishtostorage",
       */
      publishedUrl: Alfresco.constants.PROXY_URI + "vgr/publishtostorage",

      /**
       * Unpublish service URL
       *
       * @property unpublishedUrl
       * @type string
       * @default Alfresco.constants.PROXY_URI + "vgr/unpublishfromstorage",
       */
      unpublishedUrl: Alfresco.constants.PROXY_URI + "vgr/unpublishfromstorage",

      /**
       * Reload page on succesful posting
       *
       *@default false
       */
      reload: false,

      /**
       * Revoke instead of publishing
       * This controls which template is loaded and which webservice is called on ok
       */
      revoke: false,

      /**
       * Callback in case of succesful (un)publishing
       */
      successCallback: null
    },

    /**
     * Container element for template in DOM.
     *
     * @property containerDiv
     * @type DOMElement
     */
    containerDiv: null,

    /**
     * Main entry point
     * @method showDialog
     */
    showDialog: function DLGF_showDialog() {

      var templateUrl = this.options.templateUrl;
      if (this.options.revoke) {
        templateUrl = this.options.revokeTemplateUrl;
      }

      // Load the UI template from the server
      Alfresco.util.Ajax.request({
        url: templateUrl,
        dataObj: {htmlId: this.id },
        successCallback: {
          fn: this.onTemplateLoaded,
          scope: this
        },
        failureMessage: "Could not load 'publish-to-storage' template:" + this.options.templateUrl,
        execScripts: true
      });
    },

    /**
     * Event callback when dialog template has been loaded
     *
     * @method onTemplateLoaded
     * @param response {object} Server response from load template XHR request
     */
    onTemplateLoaded: function DLGF_onTemplateLoaded(response) {
      // Reference to self - used in inline functions
      var me = this;

      // Inject the template from the XHR request into a new DIV element
      this.containerDiv = document.createElement("div");
      this.containerDiv.setAttribute("style", "display:none");
      this.containerDiv.innerHTML = response.serverResponse.responseText;

      // The panel is created from the HTML returned in the XHR request, not the container
      var dialogDiv = Dom.getFirstChild(this.containerDiv);

      // Create and render the YUI dialog
      this.widgets.dialog = Alfresco.util.createYUIPanel(dialogDiv, {
        width: this.options.width
      });
      
      // OK button
      this.widgets.okButton = Alfresco.util.createYUIButton(this, "ok", this.onOK);

      // Cancel button
      this.widgets.cancelButton = Alfresco.util.createYUIButton(this, "cancel", this.onCancel);

      // Make user enter-key-strokes also trigger a change
      /*var buttons = this.widgets.modeButtons.getButtons(),
       fnEnterListener = function(e)
       {
       if (KeyListener.KEY.ENTER == e.keyCode)
       {
       this.set("checked", true);
       }
       };

       for (var i = 0; i < buttons.length; i++)
       {
       buttons[i].addListener("keydown", fnEnterListener);
       } */

      this.loadPublishedData(function (res) {
        var tree = new YAHOO.widget.TreeView(this.id + "-publish-to-storage-tree");

        var build = function (obj, parent) {

          //transform date to true javascript date, YUI won't format it otherwise
          var from = obj.valid_from ? new Date(obj.valid_from) : null;

          var html = [];
          if (!obj.isFolder && obj.validated) { //if this is a document add availableFrom date

            html.push('<div class="pub_date">');
            if (from) {
              html.push(YAHOO.util.Date.format(from, {format: "%Y-%m-%d %H:%M"}));
            } else {
              html.push(me.msg('now'));
            }

            html.push('</div>');
            html.push(obj.name); //after floated date...
          } else if (!obj.isFolder && !obj.validated) {
            html.push(obj.name); //...or before error message
            var msg = obj.error_msgs && obj.error_msgs.length > 0 ? me.msg(obj.error_msgs[0]) : me.msg("std.error");
            html.push('<div class="error_desc">');
            html.push(msg);
            html.push('</div>');
          } else {
            html.push(obj.name);
          }

          var label = '<div class="ygtvlabel ' + (obj.published ? " published" : (obj.publishedold ? " publishedold" : " notpublished")) + (!obj.validated ? " error" : "") + '">';

          var tmp = new YAHOO.widget.HTMLNode({
            html: label + html.join('') + '</div>',
            nodeRef: obj.nodeRef

          }, parent);

          if (obj.children) {
            for (var i = 0; i < obj.children.length; i++) {
              build(obj.children[i], tmp);
            }
          }
        };
        var root = tree.getRoot();
        for (var i = 0; i < res.json.results.length; i++) {
          build(res.json.results[i], root);
        }
        root.expandAll();
        tree.render();

        //update statistics text
        var help = Dom.get(this.id + "-publish-to-storage-description");
        help.innerHTML = help.innerHTML.replace(/\{folders\}/, res.json.statistics.folders).replace(/\{documents\}/, res.json.statistics.documents);

        //check for validation errors
        if (res.json.statistics.errors > 0) {
          Dom.removeClass(this.id + "-publish-to-storage-error", "hidden");
          Dom.addClass(this.id + "-publish-to-storage-description", "hidden");
          this._showDialog();
          this.widgets.okButton.set("disabled", true, false);
        } else {
          this._showDialog();
        }

      });

    },

    /**
     * Does an AJAX request and loads current status of document(s),
     * i.e. the data to present in the treeview. This will cache data so
     * multiple calls only results in one request
     * @method loadPublishedData
     * @param callback, the callback function for succes
     *
     */
    loadPublishedData: function (callback) {
      if (this._published_data) {
        callback.call(this, this._published_data);
      } else {
        //determine url, unpublishing has no validation check
        var url = this.options.publishedUrl;
        if (this.options.revoke) {
          url = this.options.unpublishedUrl;
        }

        Alfresco.util.Ajax.request({
          url: url,
          method: Alfresco.util.Ajax.POST,
          requestContentType: Alfresco.util.Ajax.JSON,
          responseContentType: Alfresco.util.Ajax.JSON,
          dataObj: this._prepRequestData(this.options.files),
          successCallback: {
            fn: function (data) {
              this._published_data = data;
              callback.call(this, data);
            },
            scope: this
          },
          failureMessage: "Could not load 'publish-to-storage' data from url: " + url,
          execScripts: true
        });
      }
    },

    /**
     * Internal show dialog function
     * @method _showDialog
     */
    _showDialog: function DLGF__showDialog() {
      // Enable buttons
      this.widgets.okButton.set("disabled", false);
      this.widgets.cancelButton.set("disabled", false);
      var self = this;

      // Register the ESC key to close the dialog
      if (!this.widgets.escapeListener) {
        this.widgets.escapeListener = new KeyListener(document, {
          keys: KeyListener.KEY.ESCAPE
        }, {
          fn: function (id, keyEvent) {
            self.onCancel();
          },
          scope: this,
          correctScope: true
        });
      }

      // Show the dialog
      this.widgets.escapeListener.enable();
      this.widgets.dialog.show();
      
      // this fix is here because the regular hideshow fix for the Embed plugin is to slow, this dialog takes too long time to show
      var iframes = Selector.query("iframe");
      for (index in iframes) {
         var iframe = iframes[index];
         
         if (iframe.name === 'Embed') {
            Dom.setStyle(iframe, 'visibility', 'hidden');
         }
      }
    },

    /**
     * YUI WIDGET EVENT HANDLERS
     * Handlers for standard events fired from YUI widgets, e.g. "click"
     */

    /**
     * Dialog OK button event handler
     *
     * @method onOK
     * @param e {object} DomEvent
     * @param p_obj {object} Object passed back from addListener method
     */
    onOK: function DLGF_onOK(e, p_obj) {
      this.widgets.escapeListener.disable();
      this.widgets.dialog.hide();

      var popup = Alfresco.util.PopupManager.displayMessage({
        text: this.msg("pts.publishing"),
        spanClass: "wait",
        displayTime: 0 //infinite
      });

      var url = this.options.publishedUrl;
      if (this.options.revoke) {
        url = this.options.unpublishedUrl;
      }

      //req object
      var req = this._prepRequestData(this.options.files);
      req.action = this.options.revoke ? "revoke" : "publish";

      //publish
      Alfresco.util.Ajax.request({
        url: url,
        method: Alfresco.util.Ajax.POST,
        requestContentType: Alfresco.util.Ajax.JSON,
        responseContentType: Alfresco.util.Ajax.JSON,
        dataObj: req,
        successCallback: {
          fn: function (res) {

            if (this.options.reload) {
              if (this.options.successCallback) {
                this.options.successCallback.call(this, res);
              }
              var href = location.href;
              //change url so that IE won't cache
              location.href = href + (href.indexOf("?") == -1 ? "?" : "&") + (new Date().getTime());
            } else {
              popup.hide();
              YAHOO.Bubbling.fire("metadataRefresh");

              if (this.options.successCallback) {
                this.options.successCallback.call(this, res);
              }

            }
          },
          scope: this
        },
        successMessage: this.msg("pts.published"),
        failureCallback: { fn: function () {
          popup.hide();
        }, scope: this },
        failureMessage: this.msg("pts.exception"),
        execScripts: true
      });

    },

    /**
     * Dialog Cancel button event handler
     *
     * @method onCancel
     * @param e {object} DomEvent
     * @param p_obj {object} Object passed back from addListener method
     */
    onCancel: function DLGF_onCancel(e, p_obj) {
      this.widgets.escapeListener.disable();
      this.widgets.dialog.hide();
    },

    /**
     * Common functionality to check if user like to unpublish before.
     * If any file under assets has been published, published_callback is called
     * otherwise notpublished_callback is called
     * @param published_callback function
     * @param notpublished_callback function
     * @param consider_publishedold boolean, if true published callback will be
     *                                  called even if the document is not
     *                                  published right now but has been before
     *                                  default: false (optional)
     */
    checkPublishedStatus: function (published_callback, notpublished_callback, consider_publishedold) {
      if (!consider_publishedold) {
        consider_publishedold = false;
      }

      this.loadPublishedData(function (res) {
        //if resulting data indicates we have no published files, just show
        //original dialog
        var notpublished = true;
        var check = function (node) {
          notpublished = notpublished && !node.published;
          if (consider_publishedold) {
            notpublished = notpublished && !node.publishedold;
          }

          if (node.children) {
            for (var i = 0; i < node.children.length; i++) {
              check(node.children[i]);
            }
          }
        };
        var results = res.json.results;
        for (var i = 0; i < results.length; i++) {
          check(results[i]);
        }

        if (notpublished) {
          notpublished_callback.call(this);
        } else {
          published_callback.call(this);
        }
      });
    },

    /* Internal method to parse assets for request data, either nodeRefs or a
     * site name
     */
    _prepRequestData: function (files) {
      var data = {};
      var nodes = [];
      var sites = [];
      for (var i = 0; i < files.length; i++) {
        if (files[i].nodeRef) {
          nodes.push(files[i].nodeRef);
        }
        if (files[i].site) {
          sites.push(files[i].site);
        }
      }

      data.nodes = nodes;
      data.sites = sites;
      return data;
    }


  });

  Alfresco.thirdparty.AutoPublish = function (htmlId) {
    Alfresco.thirdparty.AutoPublish.superclass.constructor.call(this, "Alfresco.thirdparty.AutoPublish", htmlId, ["button", "container", "connection", "json"]);

    // Initialise prototype properties
    this.containers = {};

    // Decoupled event listeners
    if (htmlId != "null") {
      this.eventGroup = htmlId;
    }

    this.id = htmlId;

    return this;
  };

  YAHOO.extend(Alfresco.thirdparty.AutoPublish, Alfresco.component.Base, {

    /**
     * Object container for initialization options
     */
    options: {

      /**
       * Width for the dialog
       *
       * @property width
       * @type string
       * @default 60em
       */
      width: "22em",

      /**
       * Folder for auto publish settings
       *
       * @property folder
       * @type string
       * @default null
       */
      folder: null,

      /**
       * Template URL
       *
       * @property templateUrl
       * @type string
       * @default Alfresco.constants.URL_SERVICECONTEXT + "modules/documentlibrary/auto-publish"
       */
      templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/documentlibrary/auto-publish",

      /**
       * Auto Publish save URL
       *
       * @property saveUrl
       * @type string
       * @default Alfresco.constants.PROXY_URI_RELATIVE + "vgr/autopublish"
       */
      saveUrl: Alfresco.constants.PROXY_URI_RELATIVE + "vgr/autopublish",

      /**
       * Auto Publish load URL
       *
       * @property loadUrl
       * @type string
       * @default Alfresco.constants.PROXY_URI_RELATIVE + "vgr/autopublish"
       */
      loadUrl: Alfresco.constants.PROXY_URI_RELATIVE + "vgr/autopublish"

    },

    /**
     * Container element for template in DOM.
     *
     * @property containerDiv
     * @type DOMElement
     */
    containerDiv: null,

    /**
     * Main entry point
     * @method showDialog
     */
    showDialog: function () {
      var templateUrl = this.options.templateUrl;

      // Load the UI template from the server
      Alfresco.util.Ajax.request({
        url: templateUrl,
        dataObj: { htmlId: this.id },
        successCallback: {
          fn: this.onTemplateLoaded,
          scope: this
        },
        failureMessage: "Could not load 'auto-publish' template:" + this.options.templateUrl,
        execScripts: true
      });
    },

    /**
     * Event callback when dialog template has been loaded
     *
     * @method onTemplateLoaded
     * @param response {object} Server response from load template XHR request
     */
    onTemplateLoaded: function (response) {
      // Inject the template from the XHR request into a new DIV element
      this.containerDiv = document.createElement("div");
      this.containerDiv.setAttribute("style", "display:none");
      this.containerDiv.innerHTML = response.serverResponse.responseText;
      var self = this;

      // The panel is created from the HTML returned in the XHR request, not the container
      var dialogDiv = Dom.getFirstChild(this.containerDiv);

      // Create and render the YUI dialog

      this.widgets.dialog = Alfresco.util.createYUIPanel(dialogDiv, {
        width: this.options.width
      });

      // OK button
      this.widgets.okButton = Alfresco.util.createYUIButton(this, "ok", this.onOK);

      // Cancel button
      this.widgets.cancelButton = Alfresco.util.createYUIButton(this, "cancel", this.onCancel);

      var buttonGroup = new YAHOO.widget.ButtonGroup({
        id: this.id + "-buttonGroup",
        name: this.id + "-radiofield",
        container: this.id + "-auto-publish-form"
      });

      this.widgets.buttonGroup = buttonGroup;

      buttonGroup.addButtons([
        { label: this.msg('auto-publish.major-version'), value: "major-version", checked: true, type: 'radio' },
        { label: this.msg('auto-publish.all-versions'), value: "all-versions", type: 'radio' },
        { label: this.msg('auto-publish.none'), value: "none", type: 'radio' }
      ]);

      // load the current settings
      Alfresco.util.Ajax.request({
        url: this.options.loadUrl,
        responseContentType: Alfresco.util.Ajax.JSON,
        dataObj: { 'folder': this.options.folder.nodeRef },
        successCallback: {
          fn: function (data) {
            var value = data.json.value;

            switch (value) {
              case 'major-version':
                self.widgets.buttonGroup.check(0);
                break;
              case 'all-versions':
                self.widgets.buttonGroup.check(1);
                break;
              default:
                self.widgets.buttonGroup.check(2);
                break;
            }

            self._showDialog();
          }, scope: self
        },
        failureMessage: self.msg('auto-publish.failure.load'),
        failureCallback: {
          fn: function () {
            self._showDialog();
          }, scope: self
        },
        execScripts: true
      });
    },

    /**
     * Internal show dialog function
     * @method _showDialog
     */
    _showDialog: function () {
      // Enable buttons
      this.widgets.okButton.set("disabled", false);
      this.widgets.cancelButton.set("disabled", false);

      var self = this;

      // Register the ESC key to close the dialog
      if (!this.widgets.escapeListener) {
        this.widgets.escapeListener = new KeyListener(document, {
          keys: KeyListener.KEY.ESCAPE
        }, {
          fn: function (id, keyEvent) {
            self.onCancel();
          },
          scope: this,
          correctScope: true
        });
      }

      // Show the dialog
      this.widgets.escapeListener.enable();
      this.widgets.dialog.show();
    },

    /**
     * YUI WIDGET EVENT HANDLERS
     * Handlers for standard events fired from YUI widgets, e.g. "click"
     */

    /**
     * Dialog OK button event handler
     *
     * @method onOK
     * @param e {object} DomEvent
     * @param p_obj {object} Object passed back from addListener method
     */
    onOK: function (e, p_obj) {
      var self = this;
      var buttons = this.widgets.buttonGroup.getButtons();
      var value = null;

      for (x = 0; x < buttons.length; x++) {
        var button = buttons[x];

        if (button.get("checked")) {
          value = button.get('value')
          break;
        }
      }

      Alfresco.util.Ajax.request({
        url: this.options.saveUrl,
        method: Alfresco.util.Ajax.POST,
        requestContentType: Alfresco.util.Ajax.JSON,
        responseContentType: Alfresco.util.Ajax.JSON,
        dataObj: {
          'folder': this.options.folder.nodeRef,
          'value': value
        },
        successCallback: {
          fn: function (data) {
            self.widgets.escapeListener.disable();
            self.widgets.dialog.hide();
            YAHOO.Bubbling.fire("metadataRefresh");
          }, scope: self
        },
        failureMessage: self.msg('auto-publish.failure.save'),
        failureCallback: {
          fn: function () {
            self.widgets.escapeListener.disable();
            self.widgets.dialog.hide();
            YAHOO.Bubbling.fire("metadataRefresh");
          }, scope: self
        },
        execScripts: true
      });
    },

    /**
     * Dialog Cancel button event handler
     *
     * @method onCancel
     * @param e {object} DomEvent
     * @param p_obj {object} Object passed back from addListener method
     */
    onCancel: function DLGF_onCancel(e, p_obj) {
      this.widgets.escapeListener.disable();
      this.widgets.dialog.hide();
    }

  });

  /* Dummy instance to load optional YUI components early */
  var dummyInstance = new Alfresco.thirdparty.AutoPublish("null");

})();
