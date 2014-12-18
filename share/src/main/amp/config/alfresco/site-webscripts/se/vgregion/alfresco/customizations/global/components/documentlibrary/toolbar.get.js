function main() {
   
   var actions = [];

   // first remove the actions which should not be there
   for each (action in model.actionSet) {
      if (action.id === 'onActionAssignWorkflow') {
         continue;
      }
      
      if (action.id === 'onActionCloudSync') {
         continue;
      }
      
      if (action.id === 'onActionCloudSyncRequest') {
         continue;
      }
      
      actions.push(action);
   }

   actions.push({
      id: "onActionPublishToStorage",
      type: null,
      permission: "ChangePermissions",
      asset: null,
      href: "",
      label: "actions.document.publish-to-storage",
      hasAspect: "",
      notAspect: ""
   });
   
   actions.push({
      id: "onActionUnpublishFromStorage",
      type: null,
      permission: "ChangePermissions",
      asset: null,
      href: "",
      label: "actions.document.unpublish-from-storage",
      hasAspect: "",
      notAspect: ""
   });
   
   model.actionSet = actions;
}

main();
