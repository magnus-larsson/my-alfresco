package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class CreateSiteDocumentPolicyIntegrationTest_disabled extends AbstractVgrRepoIntegrationTest {

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
      testInSiteSimple(site);

      testInSiteWithMove(site);

      testInSiteWithCopy(site);
    } finally {
      deleteSite(site);
    }
  }

  private void testInSiteSimple(SiteInfo site) {
    NodeRef documentLibrary = _siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);

    NodeRef folder = _fileFolderService.create(documentLibrary, "testfolder", ContentModel.TYPE_FOLDER).getNodeRef();
    _nodeService.setProperty(folder, VgrModel.PROP_TYPE_TEMPLATENAME, "very nice template");

    NodeRef document = uploadDocument(site, "test.doc", null, null, null, folder, "vgr:document").getNodeRef();

    String template = (String) _nodeService.getProperty(document, VgrModel.PROP_TYPE_TEMPLATENAME);

    assertEquals("very nice template", template);
  }

  private void testInSiteWithMove(SiteInfo site) {
    NodeRef documentLibrary = _siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);

    NodeRef folder = _fileFolderService.create(documentLibrary, "testfolder2", ContentModel.TYPE_FOLDER).getNodeRef();
    _nodeService.setProperty(folder, VgrModel.PROP_TYPE_TEMPLATENAME, "very nice template");

    NodeRef document = uploadDocument(site, "test.doc", null, null, null, null, "vgr:document").getNodeRef();

    String template = (String) _nodeService.getProperty(document, VgrModel.PROP_TYPE_TEMPLATENAME);

    assertNull(template);

    _nodeService.addAspect(document, VgrModel.ASPECT_DONOTTOUCH, null);

    try {
      _fileFolderService.move(document, folder, null);
    } catch (Exception ex) {
      fail(ex.getMessage());
    }

    template = (String) _nodeService.getProperty(document, VgrModel.PROP_TYPE_TEMPLATENAME);

    assertNull(template);

    try {
      _fileFolderService.move(document, documentLibrary, null);

      _nodeService.removeAspect(document, VgrModel.ASPECT_DONOTTOUCH);

      _fileFolderService.move(document, folder, null);
    } catch (Exception ex) {
      fail(ex.getMessage());
    }

    template = (String) _nodeService.getProperty(document, VgrModel.PROP_TYPE_TEMPLATENAME);

    assertEquals("very nice template", template);
  }

  private void testInSiteWithCopy(SiteInfo site) {
    NodeRef documentLibrary = _siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);

    NodeRef folder = _fileFolderService.create(documentLibrary, "testfolder3", ContentModel.TYPE_FOLDER).getNodeRef();
    _nodeService.setProperty(folder, VgrModel.PROP_TYPE_TEMPLATENAME, "very nice template");

    NodeRef document = uploadDocument(site, "test.doc", null, null, null, null, "vgr:document").getNodeRef();

    String template = (String) _nodeService.getProperty(document, VgrModel.PROP_TYPE_TEMPLATENAME);

    assertNull(template);
    
    NodeRef copy = null;

    try {
      copy = _fileFolderService.copy(document, folder, null).getNodeRef();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    template = (String) _nodeService.getProperty(copy, VgrModel.PROP_TYPE_TEMPLATENAME);

    assertEquals("very nice template", template);
  }

}
