package se.vgregion.alfresco.repo.constraints.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javax.naming.directory.SearchControls;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.GreaterThanOrEqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.constraints.KivService;
import se.vgregion.alfresco.repo.kivclient.KivWsClient;
import se.vgregion.alfresco.repo.model.KivUnit;
import se.vgregion.alfresco.repo.utils.ServiceUtils;

public class KivServiceWSImpl implements KivService, InitializingBean {
  private static final Logger LOG = Logger.getLogger(KivServiceWSImpl.class);

  private KivWsClient kivWsClient;

  
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
    try {
      return kivWsClient.searchUnit(searchBase, modifyTimestamp);
    } catch (Exception e) {
      LOG.error("Error while trying to fetch kiv units", e);
    }
    return new ArrayList<KivUnit>();    
  }
  
  

  @Override
  public List<KivUnit> findRecordsCreators() {
    return null;
  }

  public KivWsClient getKivWsClient() {
    return kivWsClient;
  }

  public void setKivWsClient(KivWsClient kivWsClient) {
    this.kivWsClient = kivWsClient;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(kivWsClient);
  }

}
