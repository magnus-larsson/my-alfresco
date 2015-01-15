package se.vgregion.alfresco.repo.it.node;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.junit.Test;
import org.redpill.alfresco.module.metadatawriter.model.MetadataWriterModel;

import se.vgregion.alfresco.repo.it.AbstractVgrRepoIntegrationTest;

/**
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class EnableMetadataWriterPolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {

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

  private void testInSite(SiteInfo site) {
    NodeRef document1 = uploadDocument(site, "test.doc", null, null, "test1.doc", null, "vgr:document").getNodeRef();
    NodeRef document2 = uploadDocument(site, "test.doc", null, null, "test2.doc").getNodeRef();
    
    assertHasAspect(document1, MetadataWriterModel.ASPECT_METADATA_WRITEABLE);
    assertHasAspect(document2, MetadataWriterModel.ASPECT_METADATA_WRITEABLE);
  }

}
