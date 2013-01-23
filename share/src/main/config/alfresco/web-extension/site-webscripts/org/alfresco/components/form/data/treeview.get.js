var resource = "" + url.extension;

model.result = resource;

switch (resource) {

  case "kiv":
    var alfUrl = "/vgr/kiv/unitChildren?node=" + encodeURIComponent(args.node);

    var connector = remote.connect("alfresco");

    var callresult = connector.get(alfUrl);

    model.result = callresult;

    break;

  case "keywords":
    var alfUrl = "/vgr/apelon/keywordChildren?node=" + encodeURIComponent(args.node);

    var connector = remote.connect("alfresco");

    var callresult = connector.get(alfUrl);

    model.result = callresult;

    break;

   case "documentstructure":
      var alfUrl = "/vgr/apelon/documentStructureChildren?node=" + encodeURIComponent(args.node);

      var connector = remote.connect("alfresco");

      var callresult = connector.get(alfUrl);

      model.result = callresult;

      break;

}
