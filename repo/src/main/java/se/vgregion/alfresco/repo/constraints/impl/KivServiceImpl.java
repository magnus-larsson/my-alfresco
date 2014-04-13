package se.vgregion.alfresco.repo.constraints.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.naming.directory.SearchControls;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.GreaterThanOrEqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.constraints.KivService;
import se.vgregion.alfresco.repo.model.KivUnit;
import se.vgregion.alfresco.repo.utils.impl.ServiceUtilsImpl;

public class KivServiceImpl implements KivService, InitializingBean {

  private ServiceUtilsImpl _serviceUtils;

  private LdapTemplate _ldapTemplate;

  public void setServiceUtils(final ServiceUtilsImpl serviceUtils) {
    _serviceUtils = serviceUtils;
  }

  public void setLdapTemplate(final LdapTemplate ldapTemplate) {
    _ldapTemplate = ldapTemplate;
  }

  @Override
  public List<KivUnit> findOrganisationalUnits() {
    return findOrganisationalUnits(null, null);
  }

  @Override
  public List<KivUnit> findOrganisationalUnits(final String searchBase) {
    return findOrganisationalUnits(searchBase, null);
  }

  @Override
  public List<KivUnit> findOrganisationalUnits(final Date modifyTimestamp) {
    return findOrganisationalUnits(null, modifyTimestamp);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<KivUnit> findOrganisationalUnits(final String searchBase, final Date modifyTimestamp) {
    final String base = StringUtils.isBlank(searchBase) ? "ou=OrgExtended" : searchBase;

    final SearchControls searchControls = new SearchControls();
    searchControls.setReturningAttributes(new String[] { "cn", "ou", "hsaIdentity" });
    searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);

    final AndFilter filter = new AndFilter();

    filter.append(new LikeFilter("objectClass", "*"));

    if (modifyTimestamp != null) {
      final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss'Z'");

      final GreaterThanOrEqualsFilter greaterFilter = new GreaterThanOrEqualsFilter("modifyTimestamp", dateFormat.format(modifyTimestamp));

      filter.and(greaterFilter);
    }

    return _ldapTemplate.search("\"" + base + "\"", filter.encode(), searchControls, getContextMapper());
  }

  private ContextMapper getContextMapper() {
    return new ContextMapper() {

      @Override
      public Object mapFromContext(final Object object) {
        final DirContextAdapter context = (DirContextAdapter) object;

        final String ou = context.getStringAttribute("ou");
        final String cn = context.getStringAttribute("cn");

        final KivUnit unit = new KivUnit();

        final String value = StringUtils.isBlank(ou) ? cn : ou;

        unit.setDistinguishedName(context.getDn().toString());
        unit.setHsaIdentity(context.getStringAttribute("hsaIdentity"));
        unit.setOrganisationalUnit(value);

        return unit;
      }
    };
  }

  @Override
  public List<KivUnit> findRecordsCreators() {
    return null;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_ldapTemplate);
    Assert.notNull(_serviceUtils);
  }

}
