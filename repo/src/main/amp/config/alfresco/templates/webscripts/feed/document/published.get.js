<import resource="classpath:alfresco/templates/webscripts/feed/document/date.format.js">
<import resource="classpath:alfresco/templates/webscripts/feed/document/underscore.string.js">

function main() {
	// Default if not specified: from = 30 minutes before now, to = now
	var modifiedFromTimestamp = (args.from != null) ? args.from : dateFormat(addToDate(new Date(), 0, -30), "isoDateTime");
	var modifiedToTimestamp = (args.to != null) ? args.to : dateFormat(new Date(), "isoDateTime");
	var nodeRef = (args.nodeRef != null) ? args.nodeRef : null;
	var to = (args.to != null) ? "\"" + args.to + "\"" : "NOW";
	
	var ftsQuery = "";
	
	if (nodeRef != null) {
		if (logger.isLoggingEnabled()) {
			logger.log("Search for published documents for nodeRef " + nodeRef);
		}
		
		ftsQuery = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND ID=\"" + nodeRef + "\"";
	} else {
		if (logger.isLoggingEnabled()) {
			logger.log("Search for published documents modifed between " + modifiedFromTimestamp + " and " + to);
		}

		ftsQuery = "TYPE:\"vgr:document\" AND ASPECT:\"vgr:published\" AND cm:modified:[\"" + modifiedFromTimestamp + "\" TO " + to + "]";
	}
	
	if(logger.isLoggingEnabled()) {
		logger.log("Query for published documents: " + ftsQuery);
	}
	
	var queryDef = {
    query: ftsQuery,
		language: "fts-alfresco",
		sort: [{column:"cm\:modified", ascending: true}],
		onerror: "no-results"
	};
		
	nodes = search.query(queryDef);
	
	if(logger.isLoggingEnabled()) {
		logger.log("Query " + ftsQuery + " resulted in " + nodes.length + " hits");
	}
	
	model.from = modifiedFromTimestamp;
	model.to = modifiedToTimestamp;
	model.data = processResults(nodes);
	model.now = dateFormat(new Date(), "isoUtcDateTime");
}

/**
 * Processes the search results. Filters out unnecessary nodes
 * 
 * @return the final search results object
 */
function processResults(nodes) {    
   var results = []
   	  ,item
   	  ,i;
   
   for (i = 0; i < nodes.length; i++) {     
	   if(logger.isLoggingEnabled()) {
		   logger.log("Processing node " + nodes[i].name);
	   }
       item = getDocumentItem(nodes[i]);
       if (item !== null) {
    	   results.push(item);
       }
   }
   
   return ({
   		items: results
   });
}

/**
 * Returns an item representing the node with needed meta data.
 */
function getDocumentItem(node) {
  var item = null;
   
  // check whether this is a file
  if (node.isDocument) {
     try {
   	  var documentUrl = serviceUtils.getDocumentIdentifier(node.nodeRef.toString());
     } catch (error) {
        logger.warn(error);
        
        return null;
     }
     
	  var title = node.properties["vgr:dc.title"] != null ? node.properties["vgr:dc.title"] : node.name;
	  var languageCodes = [];
	  var code;
     var now = new Date().getTime();
	  var availableFrom = node.properties["vgr:dc.date.availablefrom"];
	  var availableTo = node.properties["vgr:dc.date.availableto"];
	  var safeAvailableFrom = availableFrom != null ? availableFrom.getTime() : 0;
	  var safeAvailableTo = availableTo != null ? availableTo.getTime() : now;
	  
	  var published = now <= safeAvailableTo && now >= safeAvailableFrom;
	  
	  var requestId;
    if (published && node.properties["vgr:pushed-for-publish"]!=null) {
      requestId = "publish" + "_" + "workspace://SpacesStore/" + node.id + "_" + node.properties["vgr:pushed-for-publish"].getTime();
    } else if (!published && node.properties["vgr:pushed-for-unpublish"]!=null) {
      requestId = "unpublish" + "_" + "workspace://SpacesStore/" + node.id + "_" + node.properties["vgr:pushed-for-unpublish"].getTime();
    } else {
      //Document has not been pushed yet, skip it
      return null;
    }

	  
	  // change the language to ISO standard
	  for each (var language in node.properties["vgr:dc.language"]) {
	    code = serviceUtils.findLanguageCode(language);
	    languageCodes.push(code);
	  }
	  
    item = {
    	nodeRef                : node.nodeRef.toString()
       ,title                  : title
       ,alt_title              : node.properties["vgr:dc.title.alternative"]
       ,title_filename         : node.properties["vgr:dc.title.filename"]
       ,title_filename_native         : node.properties["vgr:dc.title.filename.native"]
       ,saved                  : getIsoUtcDate(node.properties["vgr:dc.date.saved"] ? node.properties["vgr:dc.date.saved"] : node.properties["vgr:dc.date.created"], "isoUtcDateTime")
       ,modified               : getIsoUtcDate(node.properties["cm:modified"])
       ,created                : getIsoUtcDate(node.properties["vgr:dc.date.created"])
       ,creator                : node.properties["vgr:dc.creator"]
       ,creator_id                : node.properties["vgr:dc.creator.id"]
       ,description            : node.properties["vgr:dc.description"] ? node.properties["vgr:dc.description"] : ""  
       ,language               : languageCodes
       ,format                 : node.properties["vgr:dc.format.extent.mimetype"]
       ,format_native          : node.properties["vgr:dc.format.extent.mimetype.native"]
       ,author_keywords        : node.properties["vgr:dc.subject.authorkeywords"]                          
       ,subject_keywords       : node.properties["vgr:dc.subject.keywords"]
       ,subject_keywords_id       : node.properties["vgr:dc.subject.keywords.id"]
       ,creator_freetext       : node.properties["vgr:dc.creator.freetext"]
       ,creator_document       : node.properties["vgr:dc.creator.document"]
       ,creator_document_id       : node.properties["vgr:dc.creator.document.id"]
       ,creator_function       : node.properties["vgr:dc.creator.function"]
       ,creator_forunit        : node.properties["vgr:dc.creator.forunit"]
       ,creator_forunit_id : node.properties["vgr:dc.creator.forunit.id"]
       ,creator_recordscreator : node.properties["vgr:dc.creator.recordscreator"]
       ,creator_recordscreator_id : node.properties["vgr:dc.creator.recordscreator.id"]
       ,creator_project_assignment : node.properties["vgr:dc.creator.project-assignment"]
       ,publisher              : node.properties["vgr:dc.publisher"]
       ,publisher_id              : node.properties["vgr:dc.publisher.id"]
       ,publisher_forunit      : node.properties["vgr:dc.publisher.forunit"]
       ,publisher_forunit_id      : node.properties["vgr:dc.publisher.forunit.id"]
       ,publisher_project_assignment : node.properties["vgr:dc.publisher.project-assignment"]
       ,date_issued                     : getIsoUtcDate(node.properties["vgr:dc.date.issued"])
       ,contributor_savedby : node.properties["vgr:dc.contributor.savedby"]
       ,contributor_savedby_id : node.properties["vgr:dc.contributor.savedby.id"]
       ,contributor_acceptedby          : node.properties["vgr:dc.contributor.acceptedby"]
       ,contributor_acceptedby_id       : node.properties["vgr:dc.contributor.acceptedby.id"]
       ,contributor_acceptedby_freetext : node.properties["vgr:dc.contributor.acceptedby.freetext"]
       ,date_accepted : getIsoUtcDate(node.properties["vgr:dc.date.accepted"])
       ,contributor_acceptedby_role : node.properties["vgr:dc.contributor.acceptedby.role"]
       ,contributor_acceptedby_unit_freetext : node.properties["vgr:dc.contributor.acceptedby.unit.freetext"]
       ,contributor_controlledby : node.properties["vgr:dc.contributor.controlledby"]
       ,contributor_controlledby_id : node.properties["vgr:dc.contributor.controlledby.id"]
       ,contributor_controlledby_freetext : node.properties["vgr:dc.contributor.controlledby.freetext"]
       ,date_controlled : getIsoUtcDate(node.properties["vgr:dc.date.controlled"])
       ,contributor_controlledby_role : node.properties["vgr:dc.contributor.controlledby.role"]
       ,contributor_controlledby_unit_freetext : node.properties["vgr:dc.contributor.controlledby.unit.freetext"]
       ,contributor_unit : node.properties["vgr:dc.contributor.unit"]
       ,date_validfrom : getIsoUtcDate(node.properties["vgr:dc.date.validfrom"])
       ,date_validto : getIsoUtcDate(node.properties["vgr:dc.date.validto"])
       ,date_availablefrom : getIsoUtcDate(availableFrom)
       ,date_availableto : getIsoUtcDate(availableTo)
       ,date_copyrighted : getIsoUtcDate(node.properties["vgr:dc.date.copyrighted"])
       ,type_document : node.properties["vgr:dc.type.document"]
       ,type_document_structure : node.properties["vgr:dc.type.document.structure"]
       ,type_document_structure_id : node.properties["vgr:dc.type.document.structure.id"]
       ,type_templatename : node.properties["vgr:dc.type.templatename"]
       ,type_record : node.properties["vgr:dc.type.record"]
       ,type_record_id : node.properties["vgr:dc.type.record.id"]
       ,type_process_name : node.properties["vgr:dc.type.process.name"]
       ,type_file_process : node.properties["vgr:dc.type.file.process"]
       ,type_file : node.properties["vgr:dc.type.file"]
       ,type_document_serie : node.properties["vgr:dc.type.document.serie"]
       ,type_document_id : node.properties["vgr:dc.type.document.id"]
       ,format_extent : node.properties["vgr:dc.format.extent"]
       ,format_extension : node.properties["vgr:dc.format.extension"]
       ,format_extension_native : node.properties["vgr:dc.format.extension.native"]
       ,identifier_diarie_id : node.properties["vgr:dc.identifier.diarie.id"]
       ,identifier : node.properties["vgr:dc.identifier"]
       ,identifier_native : node.properties["vgr:dc.identifier.native"]
       ,identifier_checksum : node.properties["vgr:dc.identifier.checksum"]
       ,identifier_checksum_native : node.properties["vgr:dc.identifier.checksum.native"]
       ,identifier_documentid : "workspace://SpacesStore/" + node.id
       ,identifier_version : node.properties["vgr:dc.identifier.version"]
       ,source : node.properties["vgr:dc.source"]
       ,source_documentid : node.properties["vgr:dc.source.documentid"]
       ,origin : node.properties["vgr:dc.source.origin"]
       ,relation_isversionof : node.properties["vgr:dc.relation.isversionof"]
       ,relation_replaces : node.properties["vgr:dc.relation.replaces"]
       ,coverage_hsacode : node.properties["vgr:dc.coverage.hsacode"]
       ,coverage_hsacode_id : node.properties["vgr:dc.coverage.hsacode.id"]
       ,audience : node.properties["vgr:dc.audience"]
       ,audience_id : node.properties["vgr:dc.audience.id"]
       ,status_document : node.properties["vgr:vgr.status.document"]
       ,status_document_id : node.properties["vgr:vgr.status.document.id"]
       ,rights_accessrights : node.properties["vgr:dc.rights.accessrights"]
       ,identifier_location : node.properties["vgr:dc.identifier.location"]
       ,downloadUrl                     : documentUrl
       ,objectType                      : "document"
       ,id : "tag:" + host + ",2011-06-30:" + node.id
       ,published: published ? "true" : "false"
       ,request_id : requestId
    };
   }
   
   return item;
}

function getIsoUtcDate(date) {
  return date != null ? dateFormat(date, "isoUtcDateTime") : null;
}

function addToDate(date, hours, minutes) {
	
  if (logger.isLoggingEnabled()) {
    logger.log("Original date " + date.toUTCString() + " will be added with " + hours + "h and " + minutes + " m.");
  }
	
  var m_secs = date.getTime();

  if (hours) {
    m_secs += hours * 60 * 60 * 1000;
  }
  
  if (minutes) {
    m_secs += minutes * 60 * 1000;
  }

  var newDate = new Date(m_secs);
    
  if (logger.isLoggingEnabled()) {
		logger.log("New date " + newDate.toUTCString());
	}
    
  return newDate;
}

main();