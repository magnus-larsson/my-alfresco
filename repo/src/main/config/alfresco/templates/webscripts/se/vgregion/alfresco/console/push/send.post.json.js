function main() {
   var fromDate = json.get("fromDate");
   var fromTime = json.get("fromTime");
   var toDate = json.get("toDate");
   var toTime = json.get("toTime");
   
   var from = fromDate + 'T' + fromTime + ':00';
   var to = toDate + 'T' + toTime + ':00';
   
   var query = 'TYPE:"vgr:document" AND ASPECT:"vgr:published" AND @cm\\:modified:["' + from + '" TO "' + to + '"]';
   
   logger.warn(query);
   
   var documents = search.luceneSearch(query);

   pushService.send(documents);
}

main();