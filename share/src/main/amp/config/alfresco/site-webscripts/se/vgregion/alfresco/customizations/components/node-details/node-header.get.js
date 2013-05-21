/**
 * This extension adds the missing status values that was present in the old slingshot doclib repo script.
 * 
 * TODO switch over to the new web script extension in the repo backend in 4.2
 *  
 * @returns
 */

function main() {
   var aspects = model.item.node.aspects;
   
   var status = [];
   
   for each (var aspect in aspects) {
      if (aspect == 'rule:rules') {
         status.push("rules");
      } 
      
      if (aspect == 'vgr:auto-publish') {
         var major = model.item.node.properties['vgr:auto_publish_major_version'] == 'true';
         
         if (major) {
            status.push('auto-publish-major-version');
         }
         
         var all = model.item.node.properties['vgr:auto_publish_all_versions'] == 'true';
         
         if (all) {
            status.push('auto-publish-all-versions');
         }

         model.item.status = status.join(',');
      }
   }
   
   logger.warn(model.item.status);
}

main();