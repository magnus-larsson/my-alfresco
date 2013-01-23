var values = function () {
  var fnFieldValue = function (field) {
    return field.value.length() > 0 && field.value != "null" ? field.value + "" : null;
  };

  var vals = {
    description: '',
    majorVersion: false
  };

  for each(field in formdata.fields) {
    switch (String(field.name).toLowerCase()) {
      case "filedata":
        if (field.isFile) {
          vals.filename = field.filename;
          vals.content = field.content;
          vals.content2 = field.content;
        }
        break;
      case "noderef":
        vals.nodeRef = fnFieldValue(field);
        break;

      case "majorversion":
        vals.majorVersion = fnFieldValue(field);
        break;

      case "description":
        vals.description = fnFieldValue(field);
        break;

      case "destination":
        vals.nodeRef = fnFieldValue(field);
        break;
    }
  }
  return vals;
}

function main() {

  var data = values();

  //default result is unkown error
  model.result = "unknown";

  //set values on model
  model.filename = data.filename;
  model.nodeRef = data.nodeRef;
  model.majorVersion = data.majorVersion;
  model.description = data.description;

  //sanity check
  if (!data.filename || !data.content || !data.nodeRef) {
    return;
  }

  //Store file temporary
  data.tempFilename = serviceUtils.copyToTempFile(data.content2);
  if (!data.tempFilename) {
    return;
  }
  model.tempFilename = data.tempFilename;

  //find node
  var n = search.findNode(data.nodeRef);
  if (!n) {
    return;
  }

  //check if it really has metadata
  if (n.hasAspect('{http://www.redpill.se/model/metadata-writer/1.0}metadatawriteable')) {

    try {
      var properties = metadataExtracter.extractMetadataProperties(data.content, data.filename);
    } catch (e) {
      return;
    }

    //no properties on a filetype that should have is a nomatch
    if (!properties) {
      model.result = "nomatch";
      return;
    }

    //find UUID part and compare to nodeRef
    var storedUUID = "";
    for (var property in properties) {
      if (property === 'nodeRef' || property.indexOf(":nodeRef") >= 0) {
        storedUUID = properties[property] + "";
        break;
      }
    }
    model.storedUUID = storedUUID;

    var source = "";

    var dataNode = search.findNode(data.nodeRef);

    if (dataNode) {
      source = dataNode.properties['cm:source'] ? dataNode.properties['cm:source'].nodeRef.toString() : "";
    }

    // check if the uuid parts match either the working copy or the original
    if (data.nodeRef.indexOf(storedUUID) != -1 || source.indexOf(storedUUID) != -1) {
      model.result = "match";
      return;
    }

    model.result = "nomatch";

  } else {
    //filetype is not supported, treat it as a regular upload
    model.result = "nometadata";
    return
  }
}
main();
