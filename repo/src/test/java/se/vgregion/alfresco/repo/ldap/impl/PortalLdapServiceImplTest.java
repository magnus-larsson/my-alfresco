package se.vgregion.alfresco.repo.ldap.impl;

import java.util.List;

import org.junit.Test;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import se.vgregion.alfresco.repo.ldap.Person;

public class PortalLdapServiceImplTest {

  @Test
  public void testGetPeople() throws Exception {
    final String url = "ldap://ldap2.linpro.no";
    final String userDn = "cn=bind_alfresco,ou=ServiceAccounts,dc=linpro,dc=no";
    final String password = "leiw5Jei5Eip";
    final String base = "dc=linpro,dc=no";

    final PortalLdapServiceImpl service = new PortalLdapServiceImpl();

    final LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl(url);
    contextSource.setUserDn(userDn);
    contextSource.setPassword(password);
    contextSource.setBase(base);
    contextSource.afterPropertiesSet();

    final LdapTemplate ldapTemplate = new LdapTemplate();
    ldapTemplate.setContextSource(contextSource);

    service.setLdapTemplate(ldapTemplate);
    service.afterPropertiesSet();

    List<Person> result = service.getPeople("niklas", 10);

    for (final Person person : result) {
      System.out.println(person.getRepresentation());
    }

    result = service.getPeople("niklas ekman", 10);

    for (final Person person : result) {
      System.out.println(person.getRepresentation());
    }
  }

}
