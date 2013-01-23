//dynamically load jQuery dependencies and tycktill dialog on click, this means we do not need the bloat of jQery UI on every page
//all the time
(function(){
   
   
   var Dom = YAHOO.util.Dom;
   
   if (!Alfresco.thirdparty) {
      Alfresco.thirdparty = {};
   }
   
   if (!Alfresco.thirdparty.module)  {
      Alfresco.thirdparty.module = {};
   }

   Alfresco.thirdparty.module.TyckTill = function(htmlId)
   {
      return Alfresco.module.Sites.superclass.constructor.call(this, "Alfresco.thirdparty.module.TyckTill", htmlId, []);
   };

   YAHOO.extend(Alfresco.thirdparty.module.TyckTill, Alfresco.component.Base,
   {
     
      options: {
         //http://bugs.jquery.com/ticket/10891 
         //we use a pathced version of jquery
         jquerycdn: Alfresco.constants.URL_CONTEXT + 'modules/header/jquery-1.7.2-patched-min.js',
         tycktillurl: 'http://tycktill.vgregion.se/tyck-till/tycktill/resources/js/jquery.tycktill.js',
         formurl: 'http://tycktill.vgregion.se/tyck-till/tycktill/KontaktaOss?formName=alfresco',
         uiurl:   'http://tycktill.vgregion.se/tyck-till/tycktill/resources/js/jquery-ui-1.8.6.custom.min.js',
         uicss:   'http://tycktill.vgregion.se/tyck-till/tycktill/resources/style/smoothness/jquery-ui-1.8.16.custom.css'
      },
      
      loaded: {},
      
      /**
       * Fired by YUI when parent element is available for scripting.
       *
       * @method onReady
       */
      onReady: function () {
         var b = new YAHOO.widget.Button(this.id);
         b.on('click',this._load,this,true);
         
         //add a hidden tycktill link to throw event on
         var a  = document.createElement('a');
         a.href = this.options.formurl;
         Dom.addClass(a,'tycktill');
         a.style.display = "none";
         Dom.get(this.id).appendChild(a);
      },
      
      _load_script: function(url,test,callback){
            if (!this.loaded[url]) {
               //add script tag to load the loader script
               var s = document.createElement('script');
               s.src = url;
               s.type = 'text/javascript';
               s.language = 'javascript';
               document.getElementsByTagName('head')[0].appendChild(s);
               this.loaded[url] = true;
            }
            var me = this;
            this._until(test,callback);
      },
      
      _load: function() {
         
         //we have some kind of problem with the jQuery used in tycktill-loader in ie7, therefore we load 
         //all the scripts manually
         this._load_script(this.options.jquerycdn,
                           function(){ return typeof jQuery !== 'undefined'; },
                           this._load_ui);
         
      },
      
      _load_ui: function(){
         //set jQuery in no conflict mode
         jQuery.noConflict();
         
         //jquery ui css
         var link = document.createElement('link');
         link.setAttribute("rel", "stylesheet");
         link.setAttribute("type", "text/css");
         link.setAttribute("href", this.options.uicss);
         document.getElementsByTagName('head')[0].appendChild(link);
         
         //jquery ui
         this._load_script(this.options.uiurl,
                           function(){ return typeof jQuery.fn.dialog !== 'undefined'; },
                           this._load_tycktill);
         
      },
      
      _load_tycktill: function() {
         
         
         this._load_script(this.options.tycktillurl,
                           function(){ return typeof jQuery.fn.tycktill !== 'undefined'; },
                           function(){
                               //we've got jquery here so let's use it
                               jQuery('.tycktill').click();
                           });
                    
      },
      
      _until: function(test,callback) {
            if (test()) {
                callback.call(this);
            } else {
                var me = this;
                setTimeout(function(){
                    me._until(test,callback);
                },300);
            }
      }
      
   });
})();
