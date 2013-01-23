function main() {
   model.hsa_id = "";
   model.hsa_value = "";
   
   var dn = person.properties['vgr:organization_dn'];

   if (dn === null || dn === "") {
      return;
   }

   dn = dn.replace(",ou=Org,o=VGR", ",ou=Västra Götalandsregionen,ou=OrgExtended");
   dn = dn.replace("å", "?");
   dn = dn.replace("ä", "?");
   dn = dn.replace("ö", "?");
   dn = dn.replace("Å", "?");
   dn = dn.replace("Ä", "?");
   dn = dn.replace("Ö", "?");
   
   var dns = dn.split(",ou=");
   
   // take out the slice which is the top most unit
   dns = dns.slice(dns.length - 3, dns.length);
   
   dn = dns.join(",ou=");

   // add ou= to the start of the string
   dn = dn.indexOf("ou=") == 0 ? dn : "ou=" + dn;
   
   var query = '@kiv\\:dn:"' + dn + '"';
   
   var result = search.luceneSearch(query);
   
   if (result.length === 0) {
      return;
   }
   
   var node = result[0];
   
   model.hsa_id = node.properties["kiv:hsaidentity"];
   model.hsa_value = node.properties["kiv:ou"];
}

main();
