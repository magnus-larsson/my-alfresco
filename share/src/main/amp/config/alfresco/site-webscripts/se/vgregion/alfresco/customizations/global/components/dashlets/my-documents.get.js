function vgr_main() {
   // then add another filter for viewing what others are editing
   model.filters.splice(2, 0, "editingOthers");
}

vgr_main();

function getContentTypes() {
   return contentTypes = [{
      id : "vgr:document",
      value : "vgr_document"
   }];
}

model.contentTypes = getContentTypes();
