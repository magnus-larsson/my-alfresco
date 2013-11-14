package se.vgregion.alfresco.repo.constraints.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.constraints.KivService;
import se.vgregion.alfresco.repo.kivclient.KivWsClient;
import se.vgregion.alfresco.repo.model.KivUnit;

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
