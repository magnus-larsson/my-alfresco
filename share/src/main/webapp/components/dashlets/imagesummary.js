// @overridden projects/slingshot/source/web/components/dashlets/imagesummary.js

/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
 
/**
 * Dashboard Image Summary component.
 * 
 * @namespace Alfresco.dashlet
 * @class Alfresco.dashlet.ImageSummary
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Event    = YAHOO.util.Event;
   var Dom      = YAHOO.util.Dom;
   var Selector = YAHOO.util.Selector; 
   
   var PREF_PATH = "se.vgr.alfresco.share.imagesummary.path";
   
   /**
    * Dashboard ImageSummary constructor.
    * 
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.dashlet.ImageSummary} The new component instance
    * @constructor
    */
   Alfresco.dashlet.ImageSummary = function ImageSummary_constructor(htmlId)
   {
      Alfresco.dashlet.ImageSummary.superclass.constructor.call(this, "Alfresco.dashlet.ImageSummary", htmlId, ["treeview", "json"]);
      
      this.itemsPerRow = 0;
      Event.addListener(window, 'resize', this.resizeThumbnailList, this, true);
      this.id = htmlId;

      //preferences      
      this.services.preferences = new Alfresco.service.Preferences();
      
      return this;
   };

   YAHOO.extend(Alfresco.dashlet.ImageSummary, Alfresco.component.Base,
   {
   
      options:
      {
         /**
         * Template URL
         *
         * @property templateUrl
         * @type string
         * @default Alfresco.constants.URL_SERVICECONTEXT + "modules/dashlet/tree"
         */
         templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/dashlet/tree",
         
         /**
          * Width of dialog
          *
          */
         width: "400px",
         
         componentId: null,
         
         siteId: null
      },
   
   
   
      /**
       * Keep track of thumbnail items per row - so don't resize unless actually required
       * 
       * @property itemsPerRow
       * @type integer
       */
      itemsPerRow: null,
      
      widgets: {
            menu: null,
            pagination: null,
            dialog: null,
            selectButton: null,
            cancelButton: null,
            tree: null,
            images: [],
            imageList: null
      },
      
      services: {},
      
      /**
       * Index in widgets.images of last loaded image
       */
      currentlyLoaded: 0,
      
      
      /**
       * Fired by YUI when parent element is available for scripting
       * @method onReady
       */
      onReady: function Activities_onReady()
      {
         var me = this;
         
         var button = Dom.get(this.id + "-folder");
         
         if (button) { //if we are siteadmin we have buttons
            // Create dropdown filter widgets
            this.widgets.menu = new YAHOO.widget.Button(button,
            {
               type: "split",
               menu: this.id + "-folder-menu",
               lazyloadmenu: false
            });
            
            this.widgets.menu.on("click", this.onFolderButtonClicked, this, true);
            this.widgets.menu.getMenu().subscribe("click", function (p_sType, p_aArgs)
            {
               var menuItem = p_aArgs[1];
               if (menuItem)
               {
                  me.widgets.menu.set("label", menuItem.cfg.getProperty("text"));
                  me.onFolderMenuChanged.call(me, p_aArgs[1]);
               }
            });
         }         
         
         //find all images to (possibly) load
         this.resizeThumbnailList();
         this.widgets.imageList = Dom.get(this.id + "-list");
         this.widgets.images = Selector.query("img",this.widgets.imageList);
         this._loadImages();
         Event.addListener(this.widgets.imageList,'scroll',this._loadImages,this,true);
        
      },
      
       /**
       * Folder button clicked event handler
       * @method onDateFilterClicked
       * @param p_oEvent {object} Dom event
       */
      onFolderButtonClicked: function (p_oEvent)
      {
         
      },
      
      /**
       * Date drop-down changed event handler
       * @method onDateFilterChanged
       * @param p_oMenuItem {object} Selected menu item
       */
      onFolderMenuChanged: function (item)
      {
         //we only present dialog if we select "choose folder..."
         if (item.value == 'CHOOSE_FOLDER') {
              
            // Load the UI template from the server
		      Alfresco.util.Ajax.request(
		      {
		         url: this.options.templateUrl,
		         dataObj: { htmlid: this.id },
		         successCallback:
		         {
		            fn: function(response)
		            {
                     // Inject the template from the XHR request into a new DIV element
                     var containerDiv = document.createElement("div");
                     containerDiv.setAttribute("style", "display:none");
                     containerDiv.innerHTML = response.serverResponse.responseText;

                     // Create and render the YUI dialog from dialog div inside container
                     this.widgets.dialog = Alfresco.util.createYUIPanel(Dom.getFirstChild(containerDiv),{
                        width: this.options.width
                     });
                     
                     // Select button
                     this.widgets.selectButton = Alfresco.util.createYUIButton(this, "select", this.onSelect);
                     this.widgets.selectButton.set("disabled",true);
               

                     // Cancel button
                     this.widgets.cancelButton = Alfresco.util.createYUIButton(this, "cancel", function(){ this.widgets.dialog.hide(); });

                     Alfresco.util.addMessages({ "node.root": this.msg("node.root") },"Alfresco.DocListTree");                     
                     //create tree
                     this.widgets.tree = new Alfresco.DocListTree(this.id).setOptions(
                     {
                           siteId: this.options.siteId,
                           containerId: "documentLibrary",
                           evaluateChildFolders: true,
                           maximumFolderCount: -1
                     });
                     this.widgets.tree.onReady();
                     this.widgets.tree.isFilterOwner = true;

                  
                     
                     //hook up events
                     var me = this;
                     YAHOO.Bubbling.on("changeFilter",function(e,args) {
                        if (me.widgets.tree.selectedNode) {
                           me.widgets.selectButton.set("disabled",false);
                        } else {
                           me.widgets.selectButton.set("disabled",true);
                        }
                     });
                     
                     this.widgets.dialog.show();

		            },
		            scope: this
		         },
		         failureMessage: "Could not load template:" + this.options.templateUrl,
		         execScripts: false
		      });
         }
         
      },
      
      
      onSelect: function () {
         if (this.widgets.tree.selectedNode) {
            var path = this.widgets.tree.selectedNode.data.path;
            var recursive = Dom.get(this.id + '-recursive').checked;
            
            
            this.widgets.menu.set("label", path);
            this.widgets.menu.getMenu().insertItem(path,this.widgets.menu.getMenu().getItems().length-1);
            
            //do a post to set settings...
            Alfresco.util.Ajax.jsonRequest(
            {
               method: Alfresco.util.Ajax.POST,
               url: Alfresco.constants.URL_SERVICECONTEXT + "components/site/site-preferences",
               dataObj: { prefs: {"imagesummary": { path: path,recursive: recursive}}, siteId: this.options.siteId },
               successCallback:
               {
                  fn: function(arg1,arg2)
                  {
                     if (window.console && console.log) {
                        console.log(arg1);
                        console.log(arg2);
                     }
                  },
                  scope: this
               },
               failureMessage: Alfresco.util.message("message.saveFailure", this.name)
            });
            
            this.widgets.dialog.hide();
            this._reloadImages();
         }
      },
      
      /**
       * Fired on window resize event.
       * 
       * @method resizeThumbnailList
       * @param e {object} the event source
       */
      resizeThumbnailList: function resizeThumbnailList(e)
      {
         // calculate number of thumbnails we can display across the dashlet width
         var listDiv = Dom.get(this.id + "-list");
         var count = Math.floor((listDiv.clientWidth - 16) / 112);
         if (count == 0) count = 1;
         
         if (count !== this.itemsPerRow)
         {
            this.itemsPerRow = count;
            var items = Dom.getElementsByClassName("item", null, listDiv);
            for (var i=0, j=items.length; i<j; i++)
            {
               if (i % count == 0)
               {
                  // initial item for the current row
                  Dom.addClass(items[i], "initial");
               }
               else
               {
                  Dom.removeClass(items[i], "initial");
               }
            }
         }
      },
      
      /**
       *  Lazy loads images as they are needed
       * 
       */
      _loadImages: function () {
         var images = this.widgets.images;
         //figure out if any need showing
         if (this.itemsPerRow != null && this.currentlyLoaded < images.length) {
            var imageList = this.widgets.imageList;
            var height = imageList.clientHeight;
            
            //calculate visible, load one extra row so that we always stay ahead of the user
            var visible = this.itemsPerRow * (Math.ceil((imageList.scrollTop+height)/134)+1); 
            if (this.currentlyLoaded < visible) {
               var end = Math.min(visible,images.length);
               for (var i=this.currentlyLoaded; i<end; i++) {
                  images[i].src = images[i].attributes["data-url"].value;
               }
               this.currentlyLoaded = end;
            }
         }
      },      
      
      /**
       * Reload images by an AJAX request
       *
       */
      _reloadImages: function() {
         //this is a bit of a hack, it reloads the entire page, but cuts out the dashlets html
         var listId = this.id+"-list";
         var me = this;
         
         this.widgets.imageList.innerHTML = '<div style="padding: 24px; text-align: center;">' + this.msg("loading") + '</div>';
         
         //since IE uses what it has in cache instead of doing a request we need to add
         //a argument to get rid of it
         var url = location.href + (location.href.indexOf('?')==-1?'?':'&') + 'nocacheplease='+new Date().getTime();
         
         YAHOO.util.Connect.asyncRequest("GET", 
               url, 
               { 
                  success: function(res){
                                    var containerDiv = document.createElement("div");
                                    containerDiv.setAttribute("style", "display:none");
                                    containerDiv.innerHTML = res.responseText;

                                    var imgs = Dom.getElementBy(function(n){
                                                           return n.id == listId;
                                                },"div",containerDiv);

                                    //Replace the images
                                    Dom.get(imgs.id).innerHTML = imgs.innerHTML;
                                    
                                    //reset lazy loads
                                    me.widgets.imageList = Dom.get(listId);
                                    me.widgets.images = Selector.query("img",me.widgets.imageList);
                                    me.currentlyLoaded = 0;
                                    me.resizeThumbnailList();
                                    me._loadImages();
                  }
               }
         );
      }      
   });
})();
