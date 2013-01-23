package se.vgregion.alfresco.repo.scripts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import se.vgregion.alfresco.repo.ldap.Person;
import se.vgregion.alfresco.repo.ldap.PortalLdapService;

public class ScriptPortalLdapService extends BaseScopableProcessorExtension {

  private PortalLdapService _portalLdapService;

  private PersonService _personService;

  private NodeService _nodeService;

  public void setPortalLdapService(final PortalLdapService portalLdapService) {
    _portalLdapService = portalLdapService;
  }

  public void setUserRegistrySynchronizer(final UserRegistrySynchronizer userRegistrySynchronizer) {
  }

  public List<Person> getPeople(final String filter, final int maxResults) {
    return _portalLdapService.getPeople(filter, maxResults);
  }

  public void setPersonService(final PersonService personService) {
    _personService = personService;
  }

  public void setNodeService(final NodeService nodeService) {
    _nodeService = nodeService;
  }

  public Scriptable getTest(final String filter, final int maxResults) {
    final List<Person> people = getPeople(filter, maxResults);

    final Object[] result = people.toArray();

    return Context.getCurrentContext().newArray(getScope(), result);
  }

  public boolean createPerson(final String username, final String firstname, final String lastname, final String email,
      final String organization) {
    return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>() {

      @Override
      public Boolean doWork() throws Exception {
        final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

        properties.put(ContentModel.PROP_FIRSTNAME, firstname);
        properties.put(ContentModel.PROP_LASTNAME, lastname);
        properties.put(ContentModel.PROP_EMAIL, email);
        properties.put(ContentModel.PROP_ORGANIZATION, organization);

        if (_personService.personExists(username)) {
          final NodeRef user = _personService.getPerson(username, true);

          _nodeService.setProperties(user, properties);
        } else {
          properties.put(ContentModel.PROP_USERNAME, username);

          _personService.createPerson(properties);
        }

        return true;
      }

    }, AuthenticationUtil.getSystemUserName());
  }

}
