var Published = {

   noSourceAndPublished: function (node) {
      var availableTo = node.properties['vgr:dc.date.availableto'];
      var origin = node.properties['vgr:dc.source.origin'] + "";

      var now = new Date();

      // past availableTo date, not published anymore
      if (availableTo !== null && now.getTime() > availableTo.getTime()) {
         return false;
      }
      
      // if it's not a repo document, then return false
      if (!this.isRepoDocument(node)) {
         return false;
      }
      
      // get the source id of the published document
      var sourceId = node.properties['vgr:dc.source.documentid'];
      
      // if no source id found, just exit
      if (sourceId === null || sourceId === '') {
         return false;
      }
      
      // if the origin is not Alfresco, always return false
      if (origin !== 'Alfresco') {
         return false;
      }
      
      // try to find the source node
      var sourceNode = search.findNode(sourceId);
      
      // if the source node is null, then it's deleted... return true
      return sourceNode === null;
   },
   
   unknownSourceAndPublished: function (node) {
      var availableTo = node.properties['vgr:dc.date.availableto'];
      var origin = node.properties['vgr:dc.source.origin'] + "";

      var now = new Date();

      // past availableTo date, not published anymore
      if (availableTo !== null && now.getTime() > availableTo.getTime()) {
         return false;
      }
      
      // if it's not a repo document, then return false
      if (!this.isRepoDocument(node)) {
         return false;
      }
      
      // get the source id of the published document
      var sourceId = node.properties['vgr:dc.source.documentid'];
      
      // if no source id found, just exit
      if (sourceId === null || sourceId === '') {
         return false;
      }
      
      // if the origin is not Alfresco, always return true (UNKNOWN)
      return origin !== 'Alfresco';
   },   

   isRepoDocument: function (node) {
      return node.hasAspect("vgr:published") && ((node.typeShort + "") === "vgr:document");
   },

   isPublished: function (node) {
      var assocs = node.assocs["vgr:published-to-storage"];
      var p = { published: false, hasbeen: false , future: false, publishedold: false };

      if (assocs == null) {
         return p;
      }

      // since there is an assoc we have at least been published in the past
      p.hasbeen = true;

      // check assocs
      for each (assoc in assocs) {
         // if the associated node is not in the regular workspace, skip it
         var assocStoreRef = assoc.nodeRef.storeRef.toString() + "";

         if (assocStoreRef !== "workspace://SpacesStore") {
            continue;
         }

         var nodeVersion = node.properties["vgr:dc.identifier.version"];
         var publishedVersion = assoc.properties["vgr:dc.identifier.version"];

         // if a assoc has the same version as current version we have been published
         if (nodeVersion == publishedVersion) {
            var status = this._checkPublishedAvailability(assoc);
            if ( status === "future") {
                p.future = true;
                p.published = true;
                p.publishedold = false;
            } else if (status === "published") {
                p.published = true;
                p.publishedold = false; // we only have one published document version at any given time
            } else {
                p.published = false;
            }
            break; // we only have one published document version at any given time
         }

         // check if an assoc is still published, or is going to be published in the future
         if (this._checkPublishedAvailability(assoc)) {
            p.published = false;
            p.publishedold = true;
            break; // we only have one published document version at any given time
         }
      }

      return p;
   },

   _checkPublishedAvailability: function(node) {
      var availableFrom = node.properties['vgr:dc.date.availablefrom'];
      var availableTo   = node.properties['vgr:dc.date.availableto'];

      var now = new Date();

      // past availableTo date, not published anymore
      if (availableTo != null && now.getTime() > availableTo.getTime()) {
         return false;
      } else if (availableFrom != null && now.getTime() < availableFrom.getTime()) {   // before availableFrom date means not published right now, but will be in the future
         return "future";
      }

      return "published";
   },
   
   publishAllowed: function(node) {
      return node.hasPermission("Editor");
   }

};
