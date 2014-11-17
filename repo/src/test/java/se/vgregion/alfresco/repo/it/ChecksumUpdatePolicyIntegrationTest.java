package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.assertEquals;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.apache.log4j.Logger;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;

public class ChecksumUpdatePolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {

  private static final Logger LOG = Logger.getLogger(ChecksumUpdatePolicyIntegrationTest.class);

  private static final String DEFAULT_USERNAME = "testuser";

  private SiteInfo site;

  @Override
  public void beforeClassSetup() {
    super.beforeClassSetup();
    createUser(DEFAULT_USERNAME);
    _authenticationComponent.setCurrentUser(DEFAULT_USERNAME);
    site = createSite();
  }

  @Override
  public void afterClassSetup() {
    deleteSite(site);
    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());
    deleteUser(DEFAULT_USERNAME);
    _authenticationComponent.clearCurrentSecurityContext();
  }

  @Test
  public void test() {
    LOG.debug("Starting test...");

    NodeRef document = uploadDocument(site, "test.doc").getNodeRef();

    String checksum = (String) _nodeService.getProperty(document, VgrModel.PROP_CHECKSUM);
    LOG.debug("Checksum: " + checksum);
    assertEquals("73422f4acd791700eb64b0e909224bf0", checksum);

    LOG.debug("Ending test...");
  }

}
