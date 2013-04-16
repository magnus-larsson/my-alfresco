<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/published.lib.js">

// @overridden projects/remote-api/config/alfresco/templates/webscripts/org/alfresco/repository/metadata/metadata.get.js

var json = "{}";

// allow for content to be loaded from id
if (args["nodeRef"] != null)
{
	var nodeRef = args["nodeRef"];
	node = search.findNode(nodeRef);
	
	if (node != null) {
	   // if the node was found get JSON representation
	  
		if (args["shortQNames"] != null)
		{
			json = node.toJSON(true);
		}
		else
		{
			json = node.toJSON();
		}
		
		var pdfRendition = renditionService.getRenditionByName(node, "cm:pdf");
		
		// in order to add a published property to the JSON string, do some conversion magic
		var jsonObject = eval('(' + json + ')');
		var p = Published.isPublished(node);
		jsonObject.published = p.published;
		jsonObject.published_before = p.hasbeen;
		jsonObject.will_be_published = p.future;
		jsonObject.older_version_published = p.publishedold;
		jsonObject.availablefrom = node.properties['vgr:dc.date.availablefrom']?node.properties['vgr:dc.date.availablefrom'].getTime():null;
		jsonObject.has_pdf_rendition = (pdfRendition != null);
		jsonObject.pdf_rendition = (pdfRendition != null) ? pdfRendition.nodeRef : '';
		json = jsonUtils.toJSONString(jsonObject);
	} else {
        status.code = 404;
        status.redirect = true;
	}
}

// store node onto model
model.json = json;


