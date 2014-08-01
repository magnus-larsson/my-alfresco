<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

// @overridden projects/remote-api/config/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/copy-to.post.json.js

/**
 * Copy multiple files action
 * @method POST
 */

/**
 * Entrypoint required by action.lib.js
 *
 * @method runAction
 * @param p_params {object} Object literal containing files array
 * @return {object|null} object representation of action results
 */
function runAction(p_params)
{
   var results = [],
      destNode = p_params.destNode,
      files = p_params.files,
      file, fileNode, result, nodeRef,
      fromSite, copiedNode;

   // Must have array of files
   if (!files || files.length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No files.");
      return;
   }
   
   for (file in files)
   {
      nodeRef = files[file];
      result =
      {
         nodeRef: nodeRef,
         from: nodeRef,
         action: "copyFile",
         success: false
      };
      
      try
      {
         fileNode = search.findNode(nodeRef);
         if (fileNode == null)
         {
            result.id = file;
            result.nodeRef = nodeRef;
            result.success = false;
         }
         else
         {
            result.type = fileNode.isContainer ? "folder" : "document"
            
            // Retain the name of the site the node is currently in. Null if it's not in a site.
            fromSite = String(fileNode.siteShortName);
            
            // copy the node (deep copy for containers)
            if (fileNode.isContainer)
            {
               copiedNode = fileNode.copy(destNode, true);
            }
            else
            {
               copiedNode = fileNode.copy(destNode);
            }

            result.id = copiedNode.name;
            result.nodeRef = copiedNode.nodeRef.toString();
            result.success = (result.nodeRef != null);
            
            if (result.success)
            {
               //add a "donottouch" aspect to hinder CreateSiteDocumentPolicy copying properties (we do that ourselves later)
               copiedNode.addAspect('{http://www.vgregion.se/model/1.0}donottouch');
               
               // If this was an inter-site copy, we'll need to clean up the permissions on the node
               if (fromSite != String(copiedNode.siteShortName))
               {
                  siteService.cleanSitePermissions(copiedNode);
               }
                 
               // reset some properties on the copy since they are not to be copied
               // blacklisted properties are ignored
               var blacklist = properties.get('vgr.metadata.copy.blacklist', '').split(',');
               for each (black in blacklist) {
                  if (!black || black.length() == 0) {
                     continue;
                  }
                  
                  if (black.indexOf('{http://www.vgregion.se/model/1.0}') === 0) {
                     copiedNode.properties[black] = null; //since we can't delete
                  } else {
                     copiedNode.properties['{http://www.vgregion.se/model/1.0}'+black] = null; //since we can't delete
                  }
               }
               
               // use the cm:name from the copied node to set the vgr:dc.title to support the property replication
               // this ensures that property replication works :)
               copiedNode.properties['vgr:dc.title'] = serviceUtils.removeExtension(copiedNode.properties['cm:name']);
               
               copiedNode.save();
               
               // remove any published aspect from the copy
               if (copiedNode.hasAspect('vgr:published')) {
                  copiedNode.removeAspect('vgr:published');
               }
               
               // remove any association to published documents
               var assocs = copiedNode.assocs["vgr:published-to-storage"];
                
               if (assocs) {
                  // check assocs
                  for each (assoc in assocs) {
                     // if the associated node is not in the regular workspace, skip it
                     var assocStoreRef = assoc.nodeRef.storeRef.toString() + "";
                     if (assocStoreRef !== "workspace://SpacesStore") {
                        continue;
                     }
               
                     copiedNode.removeAssociation(assoc,"vgr:published-to-storage");
                  }
               }
            }
         }
      }
      catch (e)
      {
         result.id = file;
         result.nodeRef = nodeRef;
         result.success = false;
      }
      
      results.push(result);
   }

   return results;
}

/* Bootstrap action script */
main();
