var connector = remote.connect("alfresco");

var result = connector.get("/slingshot/doclib/container/" + model.site + "/" + model.container);

var containerType = "";

if (result.status == 200) {
   var data = eval('(' + result + ')');
   
   containerType = data.container.type;
}

model.containerType = containerType;