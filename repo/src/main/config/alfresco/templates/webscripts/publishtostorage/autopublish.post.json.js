function main() {
  var folder = json.get('folder') + '';
  var value = json.get('value') + '';

  if (!folder || folder.length == 0) {
    status.code = 404;
    status.message = "Wrong folder for auto publish saving '" + folder + "'";
    status.redirect = true;
    return;
  }

  var folderNode = search.findNode(folder);

  switch (value) {
    case 'major-version':
      if (!folderNode.hasAspect('vgr:auto-publish')) {
        folderNode.addAspect('vgr:auto-publish');
      }

      folderNode.properties['vgr:auto_publish_major_version'] = true;
      folderNode.properties['vgr:auto_publish_all_versions'] = false;

      saveFolder(folderNode);

      break;
    case 'all-versions':
      if (!folderNode.hasAspect('vgr:auto-publish')) {
        folderNode.addAspect('vgr:auto-publish');
      }

      folderNode.properties['vgr:auto_publish_major_version'] = false;
      folderNode.properties['vgr:auto_publish_all_versions'] = true;

      saveFolder(folderNode);

      break;
    case 'none':
      if (folderNode.hasAspect('vgr:auto-publish')) {
        folderNode.removeAspect('vgr:auto-publish');

        saveFolder(folderNode);
      }

      break;
    default:
      status.code = 404;
      status.message = "Wrong value for auto publish saving '" + value + "'";
      status.redirect = true;
      return;
  }

  model.result = true
}

function saveFolder(folder) {
  serviceUtils.disableAllBehaviours();

  folder.save();
}

main();