function main() {
   // Check mandatory parameters
   var nodeRef = args.nodeRef;

   if (nodeRef == null || nodeRef.length == 0) {
      status.code = 400;
      status.message = "Parameter 'nodeRef' is missing.";
      status.redirect = true;
      return;
   }

   var json = remote.call("/vgr/metadata?nodeRef=" + nodeRef);
   var n = eval('(' + json + ')');

   if (json.status.code != 200) {
      status.code = 400;
      status.message = "Can't get metadata for nodeRef " + nodeRef;
      status.redirect = true;
      return;
   }

   var mcns = "{http://www.alfresco.org/model/content/1.0}";
   var name = n.properties[mcns + "name"];

   model.has_pdf_rendition = n.has_pdf_rendition;
   model.pdf_rendition = n.pdf_rendition;
   model.nodeRef = nodeRef;
   model.name = name;
}

main();
