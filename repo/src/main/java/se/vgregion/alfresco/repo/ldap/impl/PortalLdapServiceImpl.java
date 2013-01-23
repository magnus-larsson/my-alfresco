package se.vgregion.alfresco.repo.ldap.impl;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.ldap.Person;
import se.vgregion.alfresco.repo.ldap.PortalLdapService;

public class PortalLdapServiceImpl implements PortalLdapService, InitializingBean {

  private static final String DEFAULT_FIRSTNAME_ATTRIBUTE = "givenName";

  private static final String DEFAULT_LASTNAME_ATTRIBUTE = "sn";

  private static final String DEFAULT_USERNAME_ATTRIBUTE = "uid";

  private static final String DEFAULT_ORGANISATION_ATTRIBUTE = "ou";

  private static final String DEFAULT_EMAIL_ATTRIBUTE = "mail";

  private LdapTemplate _ldapTemplate;

  private String _organisationAttribute;

  private String _firstnameAttribute;

  private String _lastnameAttribute;

  private String _usernameAttribute;

  private String _emailAttribute;

  public void setLdapTemplate(final LdapTemplate ldapTemplate) {
    _ldapTemplate = ldapTemplate;
  }

  public String getOrganisationAttribute() {
    return _organisationAttribute;
  }

  public void setOrganisationAttribute(final String organisationAttribute) {
    _organisationAttribute = organisationAttribute;
  }

  public String getFirstnameAttribute() {
    return _firstnameAttribute;
  }

  public void setFirstnameAttribute(final String firstnameAttribute) {
    _firstnameAttribute = firstnameAttribute;
  }

  public String getLastnameAttribute() {
    return _lastnameAttribute;
  }

  public void setLastnameAttribute(final String lastnameAttribute) {
    _lastnameAttribute = lastnameAttribute;
  }

  public String getUsernameAttribute() {
    return _usernameAttribute;
  }

  public void setUsernameAttribute(final String usernameAttribute) {
    _usernameAttribute = usernameAttribute;
  }

  public String getEmailAttribute() {
    return _emailAttribute;
  }

  public void setEmailAttribute(final String emailAttribute) {
    _emailAttribute = emailAttribute;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Person> getPeople(final String filter, final int maxResults) {
    final AttributesMapper attributesMapper = getAttributesMapper();

    final Filter searchFilter = constructFilter(filter);

    final SearchControls searchControls = new SearchControls();
    searchControls.setReturningAttributes(new String[] { _emailAttribute, _firstnameAttribute, _lastnameAttribute,
        _organisationAttribute, _usernameAttribute });
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

    List<Person> result = _ldapTemplate.search(StringUtils.EMPTY, searchFilter.encode(), searchControls,
        attributesMapper);

    if (result.size() > maxResults) {
      result = result.subList(0, maxResults);
    }

    return result;
  }

  private AttributesMapper getAttributesMapper() {
    final AttributesMapper attributesMapper = new AttributesMapper() {

      @Override
      public Object mapFromAttributes(final Attributes attributes) throws NamingException {
        final String organisation = attributes.get(_organisationAttribute) != null ? attributes
            .get(_organisationAttribute).get().toString() : null;
        final String firstName = attributes.get(_firstnameAttribute) != null ? attributes.get(_firstnameAttribute)
            .get().toString() : null;
        final String lastName = attributes.get(_lastnameAttribute) != null ? attributes.get(_lastnameAttribute).get()
            .toString() : null;
        final String userName = attributes.get(_usernameAttribute) != null ? attributes.get(_usernameAttribute).get()
            .toString() : null;

        final String email = attributes.get(_emailAttribute) != null ? attributes.get(_emailAttribute).get().toString()
            : null;

        return new Person(firstName, lastName, userName, organisation, email);
      }

    };
    return attributesMapper;
  }

  private Filter constructFilter(final String filter) {
    Filter result;

    final String[] parts = StringUtils.split(filter, " ");

    if (parts.length == 2) {
      final AndFilter andFilter = new AndFilter();

      andFilter.and(getAttributeFilter(_firstnameAttribute, parts[0]));
      andFilter.and(getAttributeFilter(_lastnameAttribute, parts[1]));

      result = andFilter;
    } else {
      final OrFilter orFilter = new OrFilter();

      orFilter.or(getAttributeFilter(_firstnameAttribute, filter));
      orFilter.or(getAttributeFilter(_lastnameAttribute, filter));
      orFilter.or(getAttributeFilter(_usernameAttribute, filter));
      orFilter.or(getAttributeFilter(_emailAttribute, filter));

      result = orFilter;
    }

    return result;
  }

  private Filter getAttributeFilter(final String attributeName, final String attributeValue) {
    Filter filter;

    if (attributeValue.contains("*")) {
      filter = new LikeFilter(attributeName, attributeValue);
    } else {
      filter = new EqualsFilter(attributeName, attributeValue);
    }

    return filter;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_ldapTemplate);

    if (StringUtils.isBlank(_firstnameAttribute)) {
      setFirstnameAttribute(DEFAULT_FIRSTNAME_ATTRIBUTE);
    }

    if (StringUtils.isBlank(_lastnameAttribute)) {
      setLastnameAttribute(DEFAULT_LASTNAME_ATTRIBUTE);
    }

    if (StringUtils.isBlank(_usernameAttribute)) {
      setUsernameAttribute(DEFAULT_USERNAME_ATTRIBUTE);
    }

    if (StringUtils.isBlank(_organisationAttribute)) {
      setOrganisationAttribute(DEFAULT_ORGANISATION_ATTRIBUTE);
    }

    if (StringUtils.isBlank(_emailAttribute)) {
      setEmailAttribute(DEFAULT_EMAIL_ATTRIBUTE);
    }
  }

}
