package se.vgregion.alfresco.repo.admin.patch.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.surf.util.I18NUtil;

import se.vgregion.alfresco.repo.model.VgrModel;

public class AddSystemUserPatch extends AbstractPatch {

  private static final String MSG_SUCCESS = "vgr.patch.addSystemUserPatch.result";

  private PersonService _personService;

  private AuthorityService _authorityService;

  private MutableAuthenticationService _authenticationService;

  private String _username = VgrModel.SYSTEM_USER_NAME;

  private String _email;

  private String _firstname = "System";

  private String _lastname = "User";

  @Override
  protected String applyInternal() throws Exception {
    // create a authentication with a random password
    // we're not going to use the password anyway
    _authenticationService.createAuthentication(_username, GUID.generate().toCharArray());

    PropertyMap properties = new PropertyMap();
    properties.put(ContentModel.PROP_USERNAME, _username);
    properties.put(ContentModel.PROP_FIRSTNAME, _firstname);
    properties.put(ContentModel.PROP_LASTNAME, _lastname);
    properties.put(ContentModel.PROP_EMAIL, _email);

    _personService.createPerson(properties);

    _authorityService.addAuthority("GROUP_ALFRESCO_ADMINISTRATORS", _username);

    return I18NUtil.getMessage(MSG_SUCCESS);
  }

  public void setPersonService(PersonService personService) {
    _personService = personService;
  }

  public void setAuthorityService(AuthorityService authorityService) {
    _authorityService = authorityService;
  }

  public void setAuthenticationService(MutableAuthenticationService authenticationService) {
    _authenticationService = authenticationService;
  }

  public void setUsername(String username) {
    _username = username;
  }

  public void setFirstname(String firstname) {
    _firstname = firstname;
  }

  public void setLastname(String lastname) {
    _lastname = lastname;
  }

  public void setEmail(String email) {
    _email = email;
  }

}
