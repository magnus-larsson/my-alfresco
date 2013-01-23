package se.vgregion.alfresco.repo.ldap;

import java.util.List;


public interface PortalLdapService {

  public List<Person> getPeople(String filter, int maxResults);

}
