// @overridden projects/slingshot/source/web/modules/documentlibrary/permissions.js

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
 * Document Library "Permissions" module for Document Library.
 * 
 * @namespace Alfresco.module
 * @class Alfresco.module.DoclibPermissions
 */
(function()
{
   /**
   * YUI Library aliases
   */
   var Dom = YAHOO.util.Dom;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;
   
   var isEmpty = function(ob){
         for(var i in ob){ if(ob.hasOwnProperty(i)){return false;}}
         return true;
   };  
   

   Alfresco.module.DoclibPermissions = function(htmlId)
   {
      Alfresco.module.DoclibPermissions.superclass.constructor.call(this, "Alfresco.module.DoclibPermissions", htmlId, ["button", "container", "connection", "json"]);

      // Initialise prototype properties
      this.rolePickers = {};
      this.hiddenRoles = [];

      return this;
   };
   
   YAHOO.extend(Alfresco.module.DoclibPermissions, Alfresco.component.Base,
   {
      /**
       * Object container for initialization options
       */
      options:
      {
         /**
          * Current siteId.
          * 
          * @property siteId
          * @type string
          */
         siteId: "",

         /**
          * Available roles
          *
          * @property: roles
          * @type: array
          * @default: null
          */
         roles: null,

         /**
          * Files to be included in workflow
          *
          * @property: files
          * @type: array
          * @default: null
          */
         files: null,

         /**
          * Width for the dialog
          *
          * @property: width
          * @type: integer
          * @default: 44em
          */
         width: "900px",
         
         /**
          * Site id, used in search
          */
          siteId: null
      },
      
      /**
       * Object container for storing role picker UI elements.
       * 
       * @property rolePickers
       * @type object
       */
      rolePickers: null,
      
      /**
       * Object container for storing user picker UI elements.
       * 
       * @property rolePickers
       * @type object
       */
      userPickers: {},

      /**
       * Object container for storing roles that picker doesn't show
       * 
       * @property hiddenRoles
       * @type array of objects
       */
      hiddenRoles: null,

      /**
       * Container element for template in DOM.
       * 
       * @property containerDiv
       * @type DOMElement
       */
      containerDiv: null,
      
      /**
       * Container element for user permissions in DOM.
       * 
       * @property containerDiv
       * @type DOMElement
       */
      userContainerDiv: null,


      /**
       * Button for addUser
       *
       */
      addUserButton: null,
      
      /**
       * People finder widget
       */
      peopleFinder: null,
      
      /**
       * Node with template for user
       */
      userTemplate: null,

      /**
       * Main entry point
       * @method showDialog
       */
      showDialog: function DLP_showDialog()
      {
         // DocLib Actions module
         if (!this.modules.actions)
         {
            this.modules.actions = new Alfresco.module.DoclibActions();
         }
         
         if (!this.containerDiv)
         {
            // Load the UI template from the server
            Alfresco.util.Ajax.request(
            {
               url: Alfresco.constants.URL_SERVICECONTEXT + "modules/documentlibrary/permissions",
               dataObj:
               {
                  htmlid: this.id,
                  site: this.options.siteId
               },
               successCallback:
               {
                  fn: this.onTemplateLoaded,
                  scope: this
               },
               failureMessage: "Could not load Document Library Permissions template",
               execScripts: true
            });
         }
         else
         {
            // Show the dialog
            this._showDialog();
         }
      },

      /**
       * Event callback when dialog template has been loaded
       *
       * @method onTemplateLoaded
       * @param response {object} Server response from load template XHR request
       */
      onTemplateLoaded: function DLP_onTemplateLoaded(response)
      {
         // Inject the template from the XHR request into a new DIV element
         this.containerDiv = document.createElement("div");
         this.containerDiv.setAttribute("style", "display:none");
         this.containerDiv.innerHTML = response.serverResponse.responseText;
         

         // The panel is created from the HTML returned in the XHR request, not the container
         var dialogDiv = Dom.getFirstChild(this.containerDiv);
         while (dialogDiv && dialogDiv.tagName.toLowerCase() != "div")
         {
            dialogDiv = Dom.getNextSibling(dialogDiv);
         }
         
         // Create and render the YUI dialog
         this.widgets.dialog = Alfresco.util.createYUIPanel(dialogDiv,
         {
            width: this.options.width
         });
         
         // OK and cancel buttons
         this.widgets.okButton = Alfresco.util.createYUIButton(this, "ok", this.onOK);
         this.widgets.cancelButton = Alfresco.util.createYUIButton(this, "cancel", this.onCancel);
         
         // Mark-up the group/role drop-downs
         var roles = YAHOO.util.Selector.query('button.site-group', this.widgets.dialog.element),
            roleElementId, roleValue;
         
         for (var i = 0, j = roles.length; i < j; i++)
         {
            roleElementId = roles[i].id;
            roleValue = roles[i].value;
            this.rolePickers[roleValue] = new YAHOO.widget.Button(roleElementId,
            {
               type: "menu", 
               menu: roleElementId + "-select"
            });
            this.rolePickers[roleValue].getMenu().subscribe("click", this.onRoleSelected, this.rolePickers[roleValue]);
         }
         
         // Reset Permissions button
         this.widgets.resetAll = Alfresco.util.createYUIButton(this, "reset-all", this.onResetAll);
         
         //start loading people finder
         Alfresco.util.Ajax.request(
         {
             url: Alfresco.constants.URL_SERVICECONTEXT + "components/people-finder/people-finder",
             dataObj: { htmlid: this.id + "-peopleFinder" },
             successCallback:
             {
              fn: this.onPeopleFinderLoaded,
              scope: this
             },
             failureMessage: this.msg("message.peopleFinderFail"),
             execScripts: true
        }); 
         
         //users
         //find div for users directly added to this 
         this.userContainerDiv = Dom.get(this.id+'-users');
         var defaultRoles = YAHOO.lang.isArray(this.options.files)?this.options.files[0].permissions.roles:this.options.files.permissions.roles;
         var users = [];
         for (i = 0, j = defaultRoles.length; i < j; i++)
         {
            var permissions = defaultRoles[i].split(";");
            
            if (permissions[1].lastIndexOf('GROUP',0) !== 0) 
            {
                //add user pickers as we find them
                users.push(permissions);
            }
            
          }
         
         //setup user list
         this._addUsers(users);
         
         
         // Show the dialog
         this._showDialog();
      },


      /**
       * YUI WIDGET EVENT HANDLERS
       * Handlers for standard events fired from YUI widgets, e.g. "click"
       */

      /**
       * Role menu item selected event handler
       *
       * @method onRoleSelected
       * @param e {object} DomEvent
       */
      onRoleSelected: function DLP_onRoleSelected(p_sType, p_aArgs, p_oButton)
      {
         var target = p_aArgs[1];
         p_oButton.set("label", target.cfg.getProperty("text"));
         p_oButton.set("name", target.value);
      },
      
      /**
       * Reset All button event handler
       *
       * @method onResetAll
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onResetAll: function DLP_onResetAll(e, p_obj)
      {
         var me = this;
         Alfresco.util.PopupManager.displayPrompt({
                  title: this.msg('title.resetall'),
                  text:  this.msg('message.resetall'),
                  modal: true,
                  noEscape: true,
                  buttons: [{
                               text: this.msg("label.reset-all"),
                               handler: function(){
                                 this.destroy();
                                 me._applyPermissions("reset-all");                             
                               }
                            },{ 
                              text: this.msg("button.cancel"),
                              handler: function() {
                                          this.destroy();
                                       }
                            
                            }]
         });
      },
      
      /**
       * Dialog OK button event handler
       *
       * @method onOK
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onOK: function DLP_onOK(e, p_obj)
      {
         // Generate data webscript parameters from UI elements
         var permissions = this._parseUI();
         this._applyPermissions("set", permissions);
      },
      
      /**
       * Apply permissions by calling data webscript with given operation
       *
       * @method _applyPermission
       * @param operation {string} set|reset-all|allow-members-collaborate|deny-all
       * @param params {object} Permission parameters
       */
      _applyPermissions: function DLP__applyPermissions(operation, permissions)
      {
         var files, multipleFiles = [];

         // Single/multi files into array of nodeRefs
         files = this.options.files;
         for (var i = 0, j = files.length; i < j; i++)
         {
            multipleFiles.push(files[i].nodeRef);
         }
         
         // Success callback function
         var fnSuccess = function DLP__onOK_success(p_data)
         {
            var result;
            var successCount = p_data.json.successCount;
            var failureCount = p_data.json.failureCount;
            
            this._hideDialog();

            // Did the operation succeed?
            if (!p_data.json.overallSuccess)
            {
               Alfresco.util.PopupManager.displayMessage(
               {
                  text: this.msg("message.permissions.failure")
               });
               return;
            }
            
            YAHOO.Bubbling.fire("filesPermissionsUpdated",
            {
               successCount: successCount,
               failureCount: failureCount
            });
            
            for (var i = 0, j = p_data.json.totalResults; i < j; i++)
            {
               result = p_data.json.results[i];
               
               if (result.success)
               {
                  YAHOO.Bubbling.fire(result.type == "folder" ? "folderPermissionsUpdated" : "filePermissionsUpdated",
                  {
                     multiple: true,
                     nodeRef: result.nodeRef
                  });
               }
            }
            
            Alfresco.util.PopupManager.displayMessage(
            {
               text: this.msg("message.permissions.success", successCount)
            });
         };
         
         // Failure callback function
         var fnFailure = function DLP__onOK_failure(p_data)
         {
            this._hideDialog();

            Alfresco.util.PopupManager.displayMessage(
            {
               text: this.msg("message.permissions.failure")
            });
         };

         // Construct the data object for the genericAction call
         this.modules.actions.genericAction(
         {
            success:
            {
               callback:
               {
                  fn: fnSuccess,
                  scope: this
               }
            },
            failure:
            {
               callback:
               {
                  fn: fnFailure,
                  scope: this
               }
            },
            webscript:
            {
               method: Alfresco.util.Ajax.POST,
               name: "permissions/{operation}/site/{site}",
               params:
               {
                  site: this.options.siteId,
                  operation: operation
               }
            },
            config:
            {
               requestContentType: Alfresco.util.Ajax.JSON,
               dataObj:
               {
                  nodeRefs: multipleFiles,
                  permissions: permissions
               }
            }
         });

         this.widgets.okButton.set("disabled", true);
         this.widgets.cancelButton.set("disabled", true);
      },

      /**
       * Dialog Cancel button event handler
       *
       * @method onCancel
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onCancel: function DLP_onCancel(e, p_obj)
      {
         this._hideDialog();
      },


      /**
       * PRIVATE FUNCTIONS
       */

      /**
       * Internal show dialog function
       * @method _showDialog
       */
      _showDialog: function DLP__showDialog()
      {
         var i, j;
         
         // Enable buttons
         this.widgets.okButton.set("disabled", false);
         this.widgets.cancelButton.set("disabled", false);


         // Dialog title
         var titleDiv = Dom.get(this.id + "-title");
         if (YAHOO.lang.isArray(this.options.files))
         {
            titleDiv.innerHTML = this.msg("title.multi", this.options.files.length);
         }
         else
         {
            var fileSpan = '<span class="light">' + $html(this.options.files.displayName) + '</span>';
            titleDiv.innerHTML = this.msg("title.single", fileSpan);
            // Convert to array
            this.options.files = [this.options.files];
         }
         
         // Default values - "None" initially
         for (var rolePicker in this.rolePickers)
         {
            if (this.rolePickers.hasOwnProperty(rolePicker))
            {
               this.rolePickers[rolePicker].set("name", "");
               this.rolePickers[rolePicker].set("label", this.msg("role.None"));
            }
         }
         
         for (var userPicker in this.userPickers)
         {
            if (this.userPickers.hasOwnProperty(userPicker))
            {
               this.userPickers[userPicker].set("name", "");
               this.userPickers[userPicker].set("label", this.msg("role.None"));
            }
         }
        
        
         var defaultRoles = this.options.files[0].permissions.roles;
         var permissions;
         for (var i = 0, j = defaultRoles.length; i < j; i++)
         {
            permissions = defaultRoles[i].split(";");
            if (permissions[1] in this.rolePickers && permissions[2] in this.options.roles)
            {
               this.rolePickers[permissions[1]].set("name", permissions[2]);
               this.rolePickers[permissions[1]].set("label", this.msg("role." + permissions[2]));
            }
            else if (permissions[1].lastIndexOf('GROUP',0) !== 0) 
            {
                this.userPickers[permissions[1]].set("name", permissions[2])
                this.userPickers[permissions[1]].set("value", permissions[1]);
                this.userPickers[permissions[1]].set("label", this.msg("role." + permissions[2]));
            }
            else
            {
               this.hiddenRoles.push(
               {
                  user:permissions[1],
                  role:permissions[2]
               });
            }
          }
         
         //remove any person still with us that don't have any permissions
         var toRemove = [];
         for (var userPicker in this.userPickers)
         {
            if (this.userPickers.hasOwnProperty(userPicker) && (this.userPickers[userPicker].get('name') === 'NONE' || this.userPickers[userPicker].get('name') === ''))
            {
               toRemove.push(Dom.get(this.id+'-person-'+userPicker));
            }
         }
         for (var i=0; i<toRemove.length; i++) {
            if (toRemove[i]) {
               delete this.userPickers[userPicker];
               this.userContainerDiv.removeChild(toRemove[i]);
            }
         }
         //check if it's empty
         if (isEmpty(this.userPickers)) {
             this._emptyMessage(); 
         }
      
         
          
         // Register the ESC key to close the dialog
         var escapeListener = new YAHOO.util.KeyListener(document,
         {
            keys: YAHOO.util.KeyListener.KEY.ESCAPE
         },
         {
            fn: function(id, keyEvent)
            {
               this.onCancel();
            },
            scope: this,
            correctScope: true
         });
         escapeListener.enable();

         // Show the dialog
         this.widgets.dialog.show();
      },

      /**
       * Hide the dialog, removing the caret-fix patch
       *
       * @method _hideDialog
       * @private
       */
      _hideDialog: function DLP__hideDialog()
      {
         // Grab the form element
         var formElement = Dom.get(this.id + "-form");

         // Undo Firefox caret issue
         Alfresco.util.undoCaretFix(formElement);
         this.widgets.dialog.hide();
      },

      /**
       * Parse the UI elements into a parameters object
       *
       * @method _parseUI
       * @return {object} Parameters ready for webscript execution
       * @private
       */
      _parseUI: function DLP__parseUI()
      {
         var params = [],
            role;
         
         var parsePickers = function(pickers) {
             for (var picker in pickers)
             {
                if (pickers.hasOwnProperty(picker))
                {
                   role = pickers[picker].get("name");
                   if ((role != "") && (role != "None"))
                   {
                      params.push(
                      {
                         group: pickers[picker].get("value"),
                         role: role
                      });
                   }
                }
             }
         };
         
         parsePickers(this.rolePickers);
         parsePickers(this.userPickers);

         // Set hiddenRoles to avoid removing them from node
         for (var i = 0, j = this.hiddenRoles.length; i < j; i++)
         {
            params.push(
            {
               group: this.hiddenRoles[i].user,
               role: this.hiddenRoles[i].role
            });
         }
         return params;
      },
      
      /**
       * helper method for adding a user
       *
       */
       _addUser: function(name) {
            //clone template node
            var clone = document.createElement('div');
            clone.id = this.id+'-person-'+name;
            Dom.addClass(clone,'yui-gd');
            clone.innerHTML = this.userTemplate.replace('{LABEL}',name);
            
            //append it
            this.userContainerDiv.appendChild(clone);
            return clone;
       },
      /**
       * Add a user picker for all users in the list
       * will do an ajax call to get information about the users
       */
      _addUsers: function DPL_addUsers(users) {
      
        var me = this;
        
        //we have a template hidden in the users div
        var template = Dom.getFirstChild(this.userContainerDiv);
        this.userTemplate = template.innerHTML;
        this.userContainerDiv.removeChild(template);
        
        //filter on ALLOWED
        var names = [];
        var roles = {};
        for (var i=0;i<users.length; i++) {
            if (users[i][0] === 'ALLOWED') {
                names.push(users[i][1]);
                roles[users[i][1]] = users[i][2];
            }
        }
        
        //do we have any?
        if (names.length === 0) {
            this._emptyMessage();
            return;
        }
        
        //build list        
        var nodes = [];
        for (var i=0;i<names.length; i++) {
            var name = names[i];
            
            //fetch user data
            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "api/people/"+name,
                successCallback: { fn: me._onAddUser, scope: me }
            });
            
            nodes.push( this._addUser(name) );
        }
        
        //hook up buttons
        var buttons = Dom.getElementsByClassName('site-user','button',this.userContainerDiv);//YAHOO.util.Selector.query('button',this.userContainerDiv);
        this.userPickers = {};
         
        for (var i = 0; i < buttons.length; i++)
        {
           this._setupUserPicker(names[i],buttons[i],roles[names[i]]);
        }   
        
        //remove buttons
        var rbuttons = Dom.getElementsByClassName('remove-user','button',this.userContainerDiv);
                
        for (var i = 0; i < rbuttons.length; i++)
        {
           this._setupRemoveButton(names[i],nodes[i],rbuttons[i]);
        }   
      },
      
      _emptyMessage: function () {
          var empty = Dom.get(this.id + '-nousers');
          if (!empty) {
            var text = document.createElement('div');
            text.innerHTML = this.msg('users.nousers');
            text.id = this.id + '-nousers';
            this.userContainerDiv.appendChild(text);
            Dom.addClass(text,'nousers');
          } else {
            Dom.removeClass(empty,'hidden'); //might just be hidden
          }
      },
      
      _setupRemoveButton: function(name,node,button) {
          
        var remove = new YAHOO.widget.Button(button);
        var me = this;
        remove.on('click',function(){
            delete me.userPickers[name];
            me.userContainerDiv.removeChild(node);
            
            //check if it's empty
            if (isEmpty(me.userPickers)) {
                me._emptyMessage(); 
            }
        });      
      },
      
      /**
       * Creates a button for a given user in the list
       */
      _setupUserPicker: function(name,node,role){
           this.userPickers[name] = new YAHOO.widget.Button(node,
           {
              type: "menu", 
              menu: Dom.getNextSibling(node)
           });
           this.userPickers[name].getMenu().subscribe("click", this.onRoleSelected, this.userPickers[name]);
           if (role) {
            this.userPickers[name].set("name", role)
           }
           this.userPickers[name].set("value", name);
           this.userPickers[name].set("label", this.msg("role." + role));
      },
      
      /** 
        *Callback that enriches the user with more info
        */      
      _onAddUser: function(res){
        var p  = res.json;
        var div = Dom.get(this.id+'-person-'+p.userName);
        
        var label = Dom.getElementsByClassName('user-label','span',div)[0];
        label.innerHTML = p.firstName + ' ' + p.lastName + ' <span class="username">(' + p.userName + ')</span>';
         
      },
      
      /**
        * Called when the people finder template has been loaded.
        * Creates a dialog and inserts the people finder for choosing groups and users to add.
        *
        * @method onPeopleFinderLoaded
        * @param response The server response
        */
        onPeopleFinderLoaded: function Permissions_onPeopleFinderLoaded(response)
        {
            // Inject the component from the XHR request into it's placeholder DIV element
            var finderDiv = Dom.get(this.id + "-peopleFinder");
            if (finderDiv)
            {
                finderDiv.innerHTML = response.serverResponse.responseText;
                
                // Find the people Finder by container ID
                this.peopleFinder = Alfresco.util.ComponentManager.get(this.id + "-peopleFinder");
                // Set the correct options for our use
                this.peopleFinder.setOptions({
                    dataWebScript: "vgr/people?siteFilter=true&site="+this.options.siteId,
                    viewMode: Alfresco.PeopleFinder.VIEW_MODE_COMPACT,
                    singleSelectMode: false,
                    minSearchTermLength: 1
                });
                
                
                //HACK WARNING
                //We don't want links on peoples names, if you click them you'll loose what you're working, not relevant here.
                //Neither do we wan't to overload the entire PeopleFinder component
                //So instead we listen to the dataTables "postRender" event
                //but to do that the table needs to be initializes, which it might or might not be!
                var peopleFinder = this.peopleFinder;
                var me = this;
                
                var stopLinks = function() {
                    var links = Dom.getElementsByClassName('theme-color-1','a',Dom.get(this.id+'-peopleFinder'));
                    for (var i=0; i<links.length;i++) {
                        links[i].href = "#";
                        links[i].onclick = function(){ return false; };
                    }
                }
                
                //timing issues, we don't know if PeopleFinder.onReady has happened yet
                if (peopleFinder.widgets.dataTable) {
                    peopleFinder.widgets.dataTable.on('postRenderEvent',stopLinks);
                } else { 
                    //ok onReady hasn't been called yet. Lets monkey patch it                
                    var oldReady = peopleFinder.onReady;
                    this.peopleFinder.onReady = function() {
                        oldReady.apply(this,arguments);
                        
                        //listen to postRenderEvent on datatable and stop links
                        peopleFinder.widgets.dataTable.on('postRenderEvent',stopLinks);
                    };
                }
                
                
                //insert "view all" button
                var searchbar = Dom.getElementsByClassName('search-bar','div',this.peopleFinder.id)[0];;
                
                
                var all = document.createElement('button');
                all.innerHTML = this.msg('label.viewall');
                all.id = this.id + "-viewAllButton";
                
                var div = document.createElement('div');
                Dom.addClass(div,'view-all');
                
                div.appendChild(all);
                searchbar.appendChild(div);
                
                Alfresco.util.createYUIButton(this, "viewAllButton" ,function(e,button){
                    peopleFinder._performSearch('*');
                });
                
               
                // Add User/Group button
                var me = this;
                this.addUserButton = Alfresco.util.createYUIButton(this, "addUserButton",function(e,button){
                    if (button.get('checked') === true) {
                        //me.peopleFinder.clearResults();
                        Dom.removeClass(finderDiv,'hidden');
                        me.addUserButton.set('label',me.msg('label.close'));
                    } else {
                        Dom.addClass(finderDiv,'hidden');
                        me.addUserButton.set('label',me.msg('label.addUser'));
                    }
                },
                {
                    type: "checkbox",
                    checked: false
                });
                
                //listen top person selected event
                YAHOO.Bubbling.on("personSelected", function(e,p) {
                    //close diaolog
                    Dom.addClass(finderDiv,'hidden'); 
                    this.addUserButton.set('label',me.msg('label.addUser'));
                    this.addUserButton.set('checked',false);
                    
                    //add person
                    this._onPersonSelected(e,p);
                }, this);
            } 
      
       },
       
       _onPersonSelected: function(e,payload) {
           var data = payload[1];
           if (data) {
              //check if it already exists
              var root = Dom.get(this.id+'-person-'+data.userName);
              if (root == null) {
                //TODO: a little clumsy way of stayinf DRY, clean it up a bit
                Dom.addClass(this.id + '-nousers','hidden');
                root = this._addUser(data.userName);
                this._onAddUser({json:data}); 
                
                //hoook up buttons
                var button = Dom.getElementsByClassName('site-user','button',root)[0];
                this._setupUserPicker(data.userName,button,"None");
                var rbutton = Dom.getElementsByClassName('remove-user','button',root)[0];
                this._setupRemoveButton(data.userName,root,rbutton);
              }
           }
       }
       
       
       
    
    });

   /* Dummy instance to load optional YUI components early */
   var dummyInstance = new Alfresco.module.DoclibPermissions("null");
})();
