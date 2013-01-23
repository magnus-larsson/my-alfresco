package se.vgregion.alfresco.repo.security;


public interface ExternalUsersService {

  String createExternalUser(String firstname, String lastname, String email);

  String inviteExternalUser(String username, String site, String link);

}
