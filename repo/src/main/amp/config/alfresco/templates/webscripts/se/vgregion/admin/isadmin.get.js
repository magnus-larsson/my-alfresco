function main() {
   var username = args.username;

   var user = people.getPerson(username);

   if (!user) {
      status.setCode(status.STATUS_NOT_FOUND, "Could not find user");
      status.redirect = true;
      return;
   }

   model.isadmin = people.isAdmin(user);
}

main();