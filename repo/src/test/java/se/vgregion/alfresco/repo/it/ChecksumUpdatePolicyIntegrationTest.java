package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.assertEquals;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.apache.log4j.Logger;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;

public class ChecksumUpdatePolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {
  
  private static final Logger LOG = Logger.getLogger(ChangeTypePolicyIntegrationTest.class);

  private static final String DEFAULT_USERNAME = "testuser";
  
  @Test
  public void test() {
    LOG.debug("Starting test...");
    
    try {
      createUser(DEFAULT_USERNAME);

      AuthenticationUtil.runAs(new RunAsWork<Void>() {

        @Override
        public Void doWork() throws Exception {
          testAsUser();

          return null;
        }

      }, DEFAULT_USERNAME);
    } finally {
      deleteUser(DEFAULT_USERNAME);
    }
    
    LOG.debug("Ending test...");
  }

  protected void testAsUser() {
    SiteInfo site = createSite();

    try {
      testInSite(site);
    } finally {
      deleteSite(site);
    }
  }

  private void testInSite(SiteInfo site) {
    NodeRef document = uploadDocument(site, "test.doc").getNodeRef();
    
    String checksum = (String) _nodeService.getProperty(document, VgrModel.PROP_CHECKSUM);
    
    assertEquals("73422f4acd791700eb64b0e909224bf0", checksum);
  }

}
