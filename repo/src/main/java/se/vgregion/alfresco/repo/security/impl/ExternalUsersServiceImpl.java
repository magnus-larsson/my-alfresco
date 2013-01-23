package se.vgregion.alfresco.repo.security.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;

import se.vgregion.alfresco.repo.security.ExternalUsersService;
import se.vgregion.portal.createuser.CreateUser;
import se.vgregion.portal.createuser.CreateUserResponse;
import se.vgregion.portal.inviteuser.InviteUser;
import se.vgregion.portal.inviteuser.InviteUserResponse;

public class ExternalUsersServiceImpl implements ExternalUsersService {

  private WebClient _createClient;

  private WebClient _inviteClient;

  public void setCreateClient(final WebClient createClient) {
    _createClient = createClient;
  }

  public void setInviteClient(final WebClient inviteClient) {
    _inviteClient = inviteClient;
  }

  @Override
  public String createExternalUser(final String firstname, final String lastname, final String email) {
    // final String sponsor = AuthenticationUtil.getFullyAuthenticatedUser();

    final CreateUser user = new CreateUser();

    user.setUserFirstName(firstname);
    user.setUserSurName(lastname);
    user.setUserMail(email);
    user.setSponsor("susro3");

    final CreateUserResponse response = _createClient.post(user, CreateUserResponse.class);

    String username;

    switch (response.getStatusCode()) {
    case NEW_USER:
      username = response.getVgrId();
      break;
    default:
      throw new RuntimeException(response.getMessage());
    }

    return username;
  }

  @Override
  public String inviteExternalUser(final String username, final String site, final String link) {
    final InviteUser user = new InviteUser();

    String system = "Alfresco";

    if (StringUtils.isNotBlank(site)) {
      system = "webbplatsen '" + site + "' i Alfresco";
    }

    user.setUserId(username);
    user.setSystem(system);
    user.setCustomMessage("HEJ");
    user.setCustomURL(link);

    final InviteUserResponse response = _inviteClient.post(user, InviteUserResponse.class);

    String result;

    switch (response.getStatusCode()) {
    case SUCCESS:
      result = response.getUserId();
      break;
    default:
      throw new RuntimeException(response.getMessage());
    }

    return result;
  }

}
