package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.*;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class DefaultSwedishLanguagePolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {

  private static final String DEFAULT_USERNAME = "testuser";

  @Test
  public void test() {
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
  }

  protected void testAsUser() {
    SiteInfo site = createSite();

    try {
      testInSite(site);
    } finally {
      deleteSite(site);
    }
  }

  @SuppressWarnings("unchecked")
  private void testInSite(SiteInfo site) {
    NodeRef document1 = uploadDocument(site, "test.doc", null, null, "test1.doc", null, "vgr:document").getNodeRef();
    
    List<String> languages1 = (List<String>) _nodeService.getProperty(document1, VgrModel.PROP_LANGUAGE);
    List<String> accessRights1 = (List<String>) _nodeService.getProperty(document1, VgrModel.PROP_ACCESS_RIGHT);
    
    assertEquals("Svenska", languages1.get(0));
    assertEquals("Intranät", accessRights1.get(0));
    
    NodeRef document2 = uploadDocument(site, "test.doc", null, null, "test2.doc").getNodeRef();

    List<String> languages2 = (List<String>) _nodeService.getProperty(document2, VgrModel.PROP_LANGUAGE);
    List<String> accessRights2 = (List<String>) _nodeService.getProperty(document2, VgrModel.PROP_ACCESS_RIGHT);

    assertEquals("Svenska", languages2.get(0));
    assertEquals("Intranät", accessRights2.get(0));
  }

}
