// Get the tutorial link from the config (taking care to avoid scripting errors!)
var tutorial = config.scoped["HelpPages"];

if (tutorial != null) {
   tutorial = tutorial["help-pages"];
   tutorial = (tutorial != null) ? tutorial.getChildValue("share-help-external") : "";
}

// column that should be visible on the user dashboard page
function getVgrHelpColumn() {
   return ({
      title : "welcome.user.help.title",
      description : "welcome.user.help.description",
      imageUrl : "/res/components/images/help-tutorial-bw-64.png",
      actionMsg : "welcome.user.help.link",
      actionHref : tutorial,
      actionId : null,
      actionTarget : "_blank"
   });
}

function getSiteCustomisationColumnForManager() {
   return ({
      title : "welcome.site.customisation-manager.title",
      description : "welcome.site.customisation-manager.description",
      imageUrl : "/res/components/images/help-task-bw-64.png",
      actionMsg : "welcome.site.customisation-manager.link",
      actionHref : "customise-site",
      actionId : "-customisation-manager-button",
      actionTarget : null
   });
}

function getSiteCustomisationColumn() {
   return ({
      title : "welcome.site.customisation.title",
      description : "welcome.site.customisation.description",
      imageUrl : "/res/components/images/help-task-bw-64.png",
      actionMsg : null,
      actionHref : null,
      actionId : null,
      actionTarget : null
   });
}

function main() {
   var columns = [];

   var manager = false;

   for ( var x = 0; x < model.columns.length; x++) {
      var column = model.columns[x];
      
      if (!column) {
         continue;
      }

      if (column.title.indexOf('invite') >= 0) {
         manager = true;
      }

      if (column.title.indexOf('cloud') >= 0) {
         continue;
      }

      columns.push(column);
   }

   if (args.dashboardType == 'user') {
      columns.push(getVgrHelpColumn());
   } else if (args.dashboardType == 'site') {
      if (manager) {
         columns.push(getSiteCustomisationColumnForManager());
      } else {
         columns.push(getSiteCustomisationColumn());
      }
   }

   model.columns = columns;
}

main();
