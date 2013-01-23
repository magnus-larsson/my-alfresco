// @overridden projects/slingshot/source/web/components/dashlets/my-sites.js

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
 * Dashboard MySites component.
 *
 * @namespace Alfresco.dashlet
 * @class Alfresco.dashlet.MySites
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Selector = YAHOO.util.Selector;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;

   /**
    * Use the getDomId function to get some unique names for global event handling
    */
   var favEventClass = Alfresco.util.generateDomId(null, "fav-site"),
      imapEventClass = Alfresco.util.generateDomId(null, "imap-site"),
      deleteEventClass = Alfresco.util.generateDomId(null, "del-site");

   /**
    * Preferences
    */
   var PREFERENCES_SITES_DASHLET_FILTER = "org.alfresco.share.sites.dashlet.filter";       

   /**
    * Dashboard MySites constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.dashlet.MySites} The new component instance
    * @constructor
    */
   Alfresco.dashlet.MySites = function MySites_constructor(htmlId)
   {
      Alfresco.dashlet.MySites.superclass.constructor.call(this, "Alfresco.dashlet.MySites", htmlId, ["button", "container", "datasource", "datatable", "animation"]);

      // Initialise prototype properties
      this.preferencesService = new Alfresco.service.Preferences();
      this.sites = [];
      this.createSite = null;

      // Listen for events from other components
      YAHOO.Bubbling.on("siteDeleted", this.onSiteDeleted, this);
      YAHOO.Bubbling.on("siteAdded", this.loadSites, this);

      return this;
   };

   YAHOO.extend(Alfresco.dashlet.MySites, Alfresco.component.Base,
   {
      /**
       * Preferences service to get and set the dashlet's site filter
       *
       * @property preferencesService
       * @type Alfresco.service.Preferences
       * @private
       */
      preferencesService: [],

      /**
       * Site data
       *
       * @property sites
       * @type array
       * @private
       */
      sites: [],

      /**
       * CreateSite module instance.
       *
       * @property createSite
       * @type Alfresco.module.CreateSite
       */
      createSite: null,

      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
         /**
          * Flag if IMAP server is enabled
          *
          * @property imapEnabled
          * @type boolean
          * @default false
          */
         imapEnabled: false,
         
         /**
          * Result list size maximum
          *
          * @property listSize
          * @type integer
          * @default 100
          */
         listSize: 100
      },

      /**
       * Date drop-down changed event handler
       * 
       * @method onTypeFilterChanged
       * @param p_sType {string} The event
       * @param p_aArgs {array}
       */
      onTypeFilterChanged: function MySites_onTypeFilterChanged(p_sType, p_aArgs)
      {
         var menuItem = p_aArgs[1];
         if (menuItem)
         {
            this.widgets.type.set("label", menuItem.cfg.getProperty("text"));
         }
         this.widgets.type.value = menuItem.value;
         this.onTypeFilterClicked();
      },

      /**
       * Type button clicked event handler
       *
       * @method onTypeFilterClicked
       * @param p_oEvent {object} Dom event
       */
      onTypeFilterClicked: function MySites_onTypeFilterClicked(p_oEvent)
      {
         // Save preferences and load sites afterwards
         this.preferencesService.set(PREFERENCES_SITES_DASHLET_FILTER, this.widgets.type.value, { successCallback:
         {
            fn: this.loadSites,
            scope: this
         }});
      },

      /**
       * Load the list of sites to process
       * 
       * @method loadSites
       */
      loadSites: function()
      {
         // Load sites
         Alfresco.util.Ajax.request(
         {
            url: Alfresco.constants.PROXY_URI + "api/people/" + encodeURIComponent(Alfresco.constants.USERNAME) + "/sites?roles=user&size=" + this.options.listSize,
            successCallback:
            {
               fn: this.onSitesLoaded,
               scope: this
            }
         });
      },

      /**
       * Retrieve user preferences
       *
       * @method getPrefs
       * @param p_response {object} Response from "api/people/{userId}/sites" query
       */
      onSitesLoaded: function MySites_getPrefs(p_response)
      {
         // Save sites form response
         var items = p_response.json;

         // Load preferences (after which the appropriate sites will be displayed) 
         Alfresco.util.Ajax.request(
         {
            url: Alfresco.constants.PROXY_URI + "api/people/"+ encodeURIComponent(Alfresco.constants.USERNAME) + "/preferences?pf=org.alfresco.share.sites",
            successCallback:
            {
               fn: this.onPreferencesLoaded,
               scope: this,
               obj: items
            }
         });
      },

      /**
       * Process response from sites and preferences queries
       *
       * @method onSitesUpdate
       * @param p_response {object} Response from "api/people/{userId}/preferences" query
       * @param p_items {object} Response from "api/people/{userId}/sites" query
       */
      onPreferencesLoaded: function MySites_onSitesUpdate(p_response, p_items)
      {
         var favSites = {},
            imapfavSites = {},
            siteManagers, i, j, k, l,
            ii = 0;

         // Save preferences
         if (p_response.json.org)
         {
            favSites = p_response.json.org.alfresco.share.sites.favourites;
            if (typeof(favSites) === "undefined")
            {
               favSites = {};
            } 
            imapfavSites = p_response.json.org.alfresco.share.sites.imapFavourites;
         }

         // Select the preferred filter in the ui
         var filter = Alfresco.util.findValueByDotNotation(p_response.json, PREFERENCES_SITES_DASHLET_FILTER, "all");
         if (filter)
         {
            this.widgets.type.set("label", this.msg("filter." + filter));
            this.widgets.type.value = filter;
         }

         // Display the toolbar now that we have selected the filter
         Dom.removeClass(Selector.query(".toolbar div", this.id, true), "hidden");

         for (i = 0, j = p_items.length; i < j; i++)
         {
            p_items[i].isSiteManager = p_items[i].siteRole === "SiteManager";
            p_items[i].isFavourite = typeof(favSites[p_items[i].shortName]) == "undefined" ? false : favSites[p_items[i].shortName];
            if (imapfavSites)
            {
               p_items[i].isIMAPFavourite = typeof(imapfavSites[p_items[i].shortName]) == "undefined" ? false : imapfavSites[p_items[i].shortName];
            }
         }

         this.sites = [];
         for (i = 0, j = p_items.length; i < j; i++)
         {
            var site =
            {
               shortName: p_items[i].shortName,
               title: p_items[i].title,
               description: p_items[i].description,
               isFavourite: p_items[i].isFavourite,
               isIMAPFavourite: p_items[i].isIMAPFavourite,
               sitePreset: p_items[i].sitePreset,
               isSiteManager: p_items[i].isSiteManager
            };

            if (this.filterAccept(site))
            {
               this.sites[ii] = site;
               ii++;
            }
         }

         this.sites.sort(function(a, b)
         {
            var name1 = a.title ? a.title.toLowerCase() : a.shortName.toLowerCase(),
                name2 = b.title ? b.title.toLowerCase() : b.shortName.toLowerCase();
            return (name1 > name2) ? 1 : (name1 < name2) ? -1 : 0;
         });

         var successHandler = function MD__oFC_success(sRequest, oResponse, oPayload)
         {
            oResponse.results=this.sites;
            this.widgets.dataTable.set("MSG_EMPTY", "");
            this.widgets.dataTable.onDataReturnInitializeTable.call(this.widgets.dataTable, sRequest, oResponse, oPayload);
         };

         this.widgets.dataSource.sendRequest(this.sites,
         {
            success: successHandler,
            scope: this
         });
      },

      /**
       * Determine whether a given site should be displayed or not depending on the current filter selection
       * @method filterAccept
       * @param site {object} Site object literal
       * @return {boolean}
       */
      filterAccept: function MySites_filterAccept(site)
      {
         switch (this.widgets.type.value)
         {
            case "all":
               return true;

            case "sites":
               return (site.sitePreset !== "document-workspace" && site.sitePreset !== "meeting-workspace");

            case "favSites":
               return (site.isFavourite || (this.options.imapEnabled && site.isIMAPFavourite));

            case "docWorkspaces":
               return (site.sitePreset === "document-workspace");

            case "meetWorkspaces":
               return (site.sitePreset === "meeting-workspace");
         }
         return false;
      },

      /**
       * Fired by YUI when parent element is available for scripting
       * @method onReady
       */
      onReady: function MySites_onReady()
      {
         var me = this;

         // Create Dropdown filter
         this.widgets.type = new YAHOO.widget.Button(this.id + "-type",
         {
            type: "split",
            menu: this.id + "-type-menu"
         });
         this.widgets.type.on("click", this.onTypeFilterClicked, this, true);
         this.widgets.type.getMenu().subscribe("click", this.onTypeFilterChanged, this, true);

         // Listen on clicks for the create site link
         Event.addListener(this.id + "-createSite-button", "click", this.onCreateSiteLinkClick, this, true);

         // DataSource definition
         this.widgets.dataSource = new YAHOO.util.DataSource(this.sites,
         {
            responseType: YAHOO.util.DataSource.TYPE_JSARRAY
         });

         // DataTable column defintions
         var columnDefinitions =
         [
            { key: "siteId", label: "Favourite", sortable: false, formatter: this.bind(this.renderCellFavourite), width: this.options.imapEnabled ? 40 : 20 },
            { key: "title", label: "Description", sortable: false, formatter: this.bind(this.renderCellName) },
            { key: "description", label: "Actions", sortable: false, formatter: this.bind(this.renderCellActions), width: 32 }
         ];

         // DataTable definition
         this.widgets.dataTable = new YAHOO.widget.DataTable(this.id + "-sites", columnDefinitions, this.widgets.dataSource,
         {
            MSG_EMPTY: this.msg("label.noSites")
         });

         // Add animation to row delete
         this.widgets.dataTable._deleteTrEl = function(row)
         {
            var scope = this,
               trEl = this.getTrEl(row);

            var changeColor = new YAHOO.util.ColorAnim(trEl,
            {
               opacity:
               {
                  to: 0
               }
            }, 0.25);
            changeColor.onComplete.subscribe(function()
            {
               YAHOO.widget.DataTable.prototype._deleteTrEl.call(scope, row);
            });
            changeColor.animate();
         };

         /**
          * Hook favourite site events
          */
         var registerEventHandler = function(cssClass, fnHandler)
         {
            var fnEventHandler = function MS_oR_fnEventHandler(layer, args)
            {
               var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
               if (owner !== null)
               {
                  fnHandler.call(me, args[1].target.offsetParent, owner);
               }

               return true;
            };
            YAHOO.Bubbling.addDefaultAction(cssClass, fnEventHandler);
         };

         registerEventHandler(favEventClass, this.onFavouriteSite);
         registerEventHandler(imapEventClass, this.onImapFavouriteSite);
         registerEventHandler(deleteEventClass, this.onDeleteSite);

         // Enable row highlighting
         this.widgets.dataTable.subscribe("rowMouseoverEvent", this.widgets.dataTable.onEventHighlightRow);
         this.widgets.dataTable.subscribe("rowMouseoutEvent", this.widgets.dataTable.onEventUnhighlightRow);

         // Load sites & preferences
         this.loadSites();
      },

      /**
       * Favourites custom datacell formatter
       */
      renderCellFavourite: function MySites_renderCellFavourite(elCell, oRecord, oColumn, oData)
      {
         Dom.setStyle(elCell, "width", oColumn.width + "px");
         Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

         var isFavourite = oRecord.getData("isFavourite"),
               isIMAPFavourite = oRecord.getData("isIMAPFavourite");

         var desc = '<div class="site-favourites">';
         desc += '<a class="favourite-site ' + favEventClass + (isFavourite ? ' enabled' : '') + '" title="' + this.msg("link.favouriteSite") + '">&nbsp;</a>';
         if (this.options.imapEnabled)
         {
            desc += '<a class="imap-favourite-site ' + imapEventClass + (isIMAPFavourite ? ' imap-enabled' : '') + '" title="' + this.msg("link.imap_favouriteSite") + '">&nbsp;</a>';
         }
         desc += '</div>';
         elCell.innerHTML = desc;
      },

      /**
       * Name & description custom datacell formatter
       */
      renderCellName: function MySites_renderCellName(elCell, oRecord, oColumn, oData)
      {
         var siteId = oRecord.getData("shortName"),
               siteTitle = oRecord.getData("title"),
               siteDescription = oRecord.getData("description");

         var desc = '<div class="site-title"><a href="' + Alfresco.constants.URL_PAGECONTEXT + 'site/' + siteId + '/default-redirect" class="theme-color-1">' + $html(siteTitle) + '</a></div>';
         desc += '<div class="site-description">' + $html(siteDescription) + '</div>';

         elCell.innerHTML = desc;
      },

      /**
       * Actions custom datacell formatter
       */
      renderCellActions: function MySites_renderCellActions(elCell, oRecord, oColumn, oData)
      {
         Dom.setStyle(elCell, "width", oColumn.width + "px");
         Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

         var desc = "";
         if (oRecord.getData("isSiteManager"))
         {
            desc += '<a class="delete-site ' + deleteEventClass + '" title="' + this.msg("link.deleteSite") + '">&nbsp;</a>';
         }
         elCell.innerHTML = desc;
      },

      /**
       * Adds an event handler for bringing up the delete site dialog for the specific site
       *
       * @method onDeleteSite
       * @param row {object} DataTable row representing file to be actioned
       */
      onDeleteSite: function MySites_onDeleteSite(row)
      {
         var record = this.widgets.dataTable.getRecord(row);

         // Display the delete dialog for the site
         Alfresco.module.getDeleteSiteInstance().show(
         {
            site: record.getData()
         });
      },

      /**
       * Fired by DeleteSite module when a site has been deleted.
       *
       * @method onSiteDeleted
       * @param layer {object} Event fired (unused)
       * @param args {array} Event parameters (unused)
       */
      onSiteDeleted: function MySites_onSiteDeleted(layer, args)
      {
         var site = args[1].site,
            siteId = site.shortName;

         // Find the record corresponding to this site
         var record = this._findRecordByParameter(siteId, "shortName");
         if (record !== null)
         {
            this.widgets.dataTable.deleteRow(record);
         }
      },

      /**
       * Adds an event handler that adds or removes the site as favourite site
       *
       * @method onFavouriteSite
       * @param row {object} DataTable row representing file to be actioned
       */
      onFavouriteSite: function MySites_onFavouriteSite(row)
      {
         var record = this.widgets.dataTable.getRecord(row),
            site = record.getData(),
            siteId = site.shortName;

         site.isFavourite = !site.isFavourite;

         this.widgets.dataTable.updateRow(record, site);

         // Assume the call will succeed, but register a failure handler to replace the UI state on failure
         var responseConfig =
         {
            failureCallback:
            {
               fn: function MS_oFS_failure(event, obj)
               {
                  // Reset the flag to it's previous state
                  var record = obj.record,
                     site = record.getData();

                  site.isFavourite = !site.isFavourite;
                  this.widgets.dataTable.updateRow(record, site);
                  Alfresco.util.PopupManager.displayPrompt(
                  {
                     text: this.msg("message.siteFavourite.failure", site.title)
                  });
               },
               scope: this,
               obj:
               {
                  record: record
               }
            },
            successCallback:
            {
               fn: function MS_oFS_success(event, obj)
               {
                  var record = obj.record,
                     site = record.getData();

                  YAHOO.Bubbling.fire(site.isFavourite ? "favouriteSiteAdded" : "favouriteSiteRemoved", site);
               },
               scope: this,
               obj:
               {
                  record: record
               }
            }
         };

         this.preferencesService.set(Alfresco.service.Preferences.FAVOURITE_SITES + "." + siteId, site.isFavourite, responseConfig);
      },

      /**
       * Adds an event handler that adds or removes the site as favourite site
       *
       * @method _addImapFavouriteHandling
       * @param row {object} DataTable row representing file to be actioned
       */
      onImapFavouriteSite: function MySites_onImapFavouriteSite(row)
      {
         var record = this.widgets.dataTable.getRecord(row),
            site = record.getData(),
            siteId = site.shortName;

         site.isIMAPFavourite = !site.isIMAPFavourite;

         this.widgets.dataTable.updateRow(record, site);

         // Assume the call will succeed, but register a failure handler to replace the UI state on failure
         var responseConfig =
         {
            failureCallback:
            {
               fn: function MS_oIFS_failure(event, obj)
               {
                  // Reset the flag to it's previous state
                  var record = obj.record,
                     site = record.getData();

                  site.isIMAPFavourite = !site.isIMAPFavourite;
                  this.widgets.dataTable.updateRow(record, site);
                  Alfresco.util.PopupManager.displayPrompt(
                  {
                     text: this.msg("message.siteFavourite.failure", site.title)
                  });
               },
               scope: this,
               obj:
               {
                  record: record
               }
            }
         };

         this.preferencesService.set(Alfresco.service.Preferences.IMAP_FAVOURITE_SITES + "." + siteId, site.isIMAPFavourite, responseConfig);
      },

      /**
       * Fired by YUI Link when the "Create site" label is clicked
       * @method onCreateSiteLinkClick
       * @param event {domEvent} DOM event
       */
      onCreateSiteLinkClick: function MySites_onCreateSiteLinkClick(event)
      {
         Alfresco.module.getCreateSiteInstance().show();
         Event.preventDefault(event);
      },

      /**
       * Searches the current recordSet for a record with the given parameter value
       *
       * @method _findRecordByParameter
       * @param p_value {string} Value to find
       * @param p_parameter {string} Parameter to look for the value in
       */
      _findRecordByParameter: function MySites__findRecordByParameter(p_value, p_parameter)
      {
        var recordSet = this.widgets.dataTable.getRecordSet();
        for (var i = 0, j = recordSet.getLength(); i < j; i++)
        {
           if (recordSet.getRecord(i).getData(p_parameter) == p_value)
           {
              return recordSet.getRecord(i);
           }
        }
        return null;
      }
   });
})();