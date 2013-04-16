function main() {
   var shortName = args.shortName;

   model.lastActivityOnSite = siteReport.getLastActivityOnSite(shortName);
}

main();
