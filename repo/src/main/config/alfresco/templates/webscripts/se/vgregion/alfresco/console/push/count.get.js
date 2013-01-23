<import resource="classpath:alfresco/templates/webscripts/feed/document/date.format.js">
<import resource="classpath:alfresco/templates/webscripts/feed/document/underscore.string.js">

function main() {
   var fromDate = args.fromDate;
   var fromTime = args.fromTime;
   var toDate = args.toDate;
   var toTime = args.toTime;
   
   var from = fromDate + 'T' + fromTime + ':00';
   var to = toDate + 'T' + toTime + ':00';
   
   var query = 'TYPE:"vgr:document" AND ASPECT:"vgr:published" AND @cm\\:modified:["' + from + '" TO "' + to + '"]';
   
   logger.warn(query);
   
   var documents = search.luceneSearch(query);
   
   model.count = documents.length;
}

main();