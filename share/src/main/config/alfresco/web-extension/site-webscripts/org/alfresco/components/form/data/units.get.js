var resource = "" + url.extension;

model.result = resource;

switch (resource) {

  case "kiv":
    var alfUrl = "/vgr/kiv/units?nodes=" + encodeURIComponent(args.nodes);

    var connector = remote.connect("alfresco");

    var callresult = connector.get(alfUrl);

    model.result = callresult;

    break;

  case "keywords":
    var alfUrl = "/vgr/apelon/keywords?nodes=" + encodeURIComponent(args.nodes);

    var connector = remote.connect("alfresco");

    var callresult = connector.get(alfUrl);

    model.result = callresult;

    break;

   case "documentstructure":
      var alfUrl = "/vgr/apelon/documentStructure?nodes=" + encodeURIComponent(args.nodes);

      var connector = remote.connect("alfresco");

      var callresult = connector.get(alfUrl);

      model.result = callresult;

      break;

}
