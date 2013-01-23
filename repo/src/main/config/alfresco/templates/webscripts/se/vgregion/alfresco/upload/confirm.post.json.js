// Confirms a pre upload, i.e. pushes the tempfile 
// as an upload

function main()
{
   //try
   //{
     
      var updateNodeRef = null;
      var tempFilename = null;
      var filename = null;
      var majorVersion = false;
      var description = "";
      
      if (json.has('updateNodeRef')) {
        updateNodeRef = json.get('updateNodeRef');
      }
      if (json.has('tempFilename')) {
        tempFilename = json.get('tempFilename');
      }
      if (json.has('filename')) {
        filename = json.get('filename');
      }
      if (json.has('majorVersion')) { //java.lang.Bool 
        majorVersion = (''+json.get('majorVersion')) == 'true';
      }
      if (json.has('description')) {
        description = json.get('description');
      }


      // Ensure mandatory file attributes have been located. Need either destination, or site + container or updateNodeRef
      if (filename === null || tempFilename === null || updateNodeRef === null) {
         status.code = 400;
         status.message = "Required parameters are missing";
         status.redirect = true;
         return;
      }
     
      if (updateNodeRef !== null) {
         
         //html uploader mangles the document id somewhere escaping / with \, i.e \/
         updateNodeRef = String(updateNodeRef).replace(/\\/g,'');
         
         /**
          * Update existing file specified in updateNodeRef
          */
         var updateNode = search.findNode(updateNodeRef);
         if (updateNode === null) {
            status.code = 404;
            status.message = "Node specified by updateNodeRef (" + updateNodeRef + ") not found.";
            status.redirect = true;
            return;
         }
         
         if (updateNode.isLocked) {
            // We cannot update a locked document
            status.code = 404;
            status.message = "Cannot update locked document '" + updateNodeRef + "', supply a reference to its working copy instead.";
            status.redirect = true;
            return;
         }

         if (!updateNode.hasAspect("cm:workingcopy")) {
            // Ensure the file is versionable (autoVersion = true, autoVersionProps = false)
            updateNode.ensureVersioningEnabled(true, false);

            // It's not a working copy, do a check out to get the actual working copy
            updateNode = updateNode.checkoutForUpload();
         }

         // Update the working copy content
         serviceUtils.writeContent(updateNode.properties.content, tempFilename);

         // Reset working copy mimetype and encoding
         updateNode.properties.content.guessMimetype(filename);
         updateNode.properties.content.guessEncoding();
         // check it in again, with supplied version history note
         updateNode = updateNode.checkin(description, majorVersion);
         
         model.document = updateNode;
         model.status = "success";
      }
      
   //}
   //catch (e)
   //{
      // capture exception, annotate it accordingly and re-throw
    //  if (e.message && e.message.indexOf("org.alfresco.service.cmr.usage.ContentQuotaException") == 0)
    //  {
    //     e.code = 413;
    //  }
    //  else
    //  {
    //     e.code = 500;
         //e.message = "Unexpected error occured during upload of new content.";      
    //  }
    //  throw e;
   //}
}


main();
