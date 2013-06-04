/**
 * Custom content types
 */
function getContentTypes() {
   return contentTypes = [ {
      id : "vgr:document",
      value : "vgr_document"
   } ];
}

function getRepositoryContentTypes() {
   return contentTypes = [ {
      id : "cm:content",
      value : "cm_content"
   } ];
}

model.contentTypes = page.id == 'repository' ? getRepositoryContentTypes() : getContentTypes();
