function main() {
  model.vlue = 'none';

  var folder = args.folder;

  if (!folder || folder.length == 0) {
    status.code = 404;
    status.message = "No folder supplied for auto publish get settings";
    status.redirect = true;
    return;
  }

  var folderNode = search.findNode(folder);

  if (folderNode.hasAspect('vgr:auto-publish')) {
    if (folderNode.properties['vgr:auto_publish_major_version']) {
      model.vlue = 'major-version';
      return;
    }

    if (folderNode.properties['vgr:auto_publish_all_versions']) {
      model.vlue = 'all-versions';
      return;
    }
  }
}


main();