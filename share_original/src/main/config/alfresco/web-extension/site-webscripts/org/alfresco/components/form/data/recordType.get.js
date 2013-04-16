var documentTypeId = "" + url.extension;

var alfrescoURL = "/vgr/apelon/recordTypes?documentTypeId=" + documentTypeId;

var connector = remote.connect("alfresco");

var callresult = connector.get(alfrescoURL);

var values = eval('(' + callresult + ')').values, result = new Array(), value, entry, property, x;

for (x = 0; x < values.length; x++) {
   value = values[x];

   entry = {
      "name" : value.name,
      "internalId" : value.internalId
   };

   for (property in value.properties) {
      if (property === "Sökväg") {
         entry.name = value.properties[property];
      }
   }

   result.push(entry);
}

model.result = jsonUtils.toJSONString({
   values : result
});
