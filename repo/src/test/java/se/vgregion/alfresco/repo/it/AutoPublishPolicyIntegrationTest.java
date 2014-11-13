package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;

public class AutoPublishPolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {

  private static final Logger LOG = Logger.getLogger(AutoPublishPolicyIntegrationTest.class);

  private static final String DEFAULT_USERNAME = "testuser";

  @Autowired
  private StorageService _storageService;

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
    final NodeRef documentLibrary = _siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);

    NodeRef folder = _fileFolderService.create(documentLibrary, "testfolder", ContentModel.TYPE_FOLDER).getNodeRef();

    List<String> projects = new ArrayList<String>();
    projects.add("Project");

    _nodeService.addAspect(folder, VgrModel.ASPECT_AUTO_PUBLISH, null);
    _nodeService.setProperty(folder, VgrModel.PROP_AUTO_PUBLISH_ALL_VERSIONS, true);

    NodeRef document = uploadDocument(site, "test.doc", null, null, null, folder).getNodeRef();
    _nodeService.setProperty(document, VgrModel.PROP_TYPE_RECORD, "type record");
    _nodeService.setProperty(document, VgrModel.PROP_TYPE_RECORD_ID, "123456");
    _nodeService.setProperty(document, VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT, (Serializable) projects);

    NodeRef publishedNode = _storageService.getPublishedNodeRef(document);

    assertTrue(_nodeService.hasAspect(publishedNode, VgrModel.ASPECT_PUBLISHED));
  }

}
