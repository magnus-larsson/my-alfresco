<import resource="classpath:alfresco/templates/webscripts/se/vgregion/alfresco/console/push/date.format.js">

function subtractFromDate(date, days, hours, minutes) {
   var m_secs = date.getTime();

   if (hours) {
      m_secs = m_secs - hours * 60 * 60 * 1000;
   }

   if (minutes) {
      m_secs = m_secs - minutes * 60 * 1000;
   }

   if (days) {
      m_secs = m_secs - days * 24 * 60 * 60 * 1000;
   }

   var newDate = new Date(m_secs);

   return newDate;
}

function countArchivedNodes() {
   var nodes = search.query({
      query : 'TYPE:"sys:base"',
      language : 'fts-alfresco',
      store : 'archive://SpacesStore'
   });
   
   return nodes.length;
}

function main() {
   var total = countArchivedNodes();
   
   logger.warn("There is in total " + total + " archived nodes.");
   
   var maxItems = args.maxItems ? args.maxItems : 5;
   var days = args.days ? args.days : 30;
   
   var query = 'TYPE:"sys:base"';
   
   if (args.user) {
      query += ' AND sys:archivedBy:"' + args.user + '"';
   }

   var nodes = search.query({
      query : query,
      language : 'fts-alfresco',
      store : 'archive://SpacesStore',
      page : {
         maxItems : maxItems
      }
   });
   
   logger.warn("Purging max " + maxItems + " nodes, older than " + days + " days.");
   
   var count = 0;

   for ( var x = 0; x < nodes.length; x++) {
      var node = nodes[x];
      
      var archiveDate = null;
      
      try {
         archiveDate = node.properties['sys:archivedDate'];
      } catch (error) {
         continue;
      }
      
      var keepDate = subtractFromDate(new Date(), days, 0, 0);
      
      if (archiveDate >= keepDate) {
         continue;
      }
      
      node.remove();
      
      logger.warn("Purged node " + node.nodeRef);
      
      count++;
   }
   
   logger.warn("Purged " + count + " out of " + total);
}

main();
