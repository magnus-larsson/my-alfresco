package se.vgregion.alfresco.repo.it.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.junit.Test;

import se.vgregion.alfresco.repo.it.AbstractVgrRepoIntegrationTest;
import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class CreateSiteFolderPolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {

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
    NodeRef documentLibrary = _siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);

    NodeRef folder = _fileFolderService.create(documentLibrary, "testfolder", ContentModel.TYPE_FOLDER).getNodeRef();

    assertHasAspect(folder, VgrModel.ASPECT_METADATA);
  }

}
