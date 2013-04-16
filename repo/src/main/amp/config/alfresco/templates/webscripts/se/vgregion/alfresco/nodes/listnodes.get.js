<import resource="classpath:alfresco/templates/webscripts/feed/document/date.format.js">

function main() {
  var days = args.days ? args.days : 5;

  var from = dateFormat(removeFromDate(new Date(), days), "isoDateTime");

  var query = 'TYPE:"vgr:document" AND cm:modified:["' + from + '" TO NOW]';

  var nodes = search.query({
    query: query,
    language: 'fts-alfresco',
    store: 'workspace://SpacesStore'
  });

  model.nodes = [];

  for (x = 0; x < nodes.length; x++) {
    var node = nodes[x];

    if (!node.getSiteShortName()) {
      continue;
    }

    model.nodes.push(node);
  }
}

main();

function removeFromDate(date, days) {
  var m_secs = date.getTime();

  if (days) {
    m_secs -= days * 60 * 60 * 1000 * 24;
  }

  var newDate = new Date(m_secs);

  return newDate;
}
