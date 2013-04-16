function main() {
  var published = vgrDok.publish();
  
  logger.warn("Published '" + published + "' VGR Dok documents.");
  
  model.published = published;
}

main();