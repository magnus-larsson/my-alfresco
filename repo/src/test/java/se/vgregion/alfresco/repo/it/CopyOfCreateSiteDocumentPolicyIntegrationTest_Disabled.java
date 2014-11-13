package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.*;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class CopyOfCreateSiteDocumentPolicyIntegrationTest_Disabled extends AbstractVgrRepoIntegrationTest {

  private static final String DEFAULT_USERNAME = "testuser";
  private SiteInfo site;
  private String siteManagerUser;

  @Override
  public void beforeClassSetup() {
    super.beforeClassSetup();

    _authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

    // Create a site
    site = createSite();
    assertNotNull(site);

    // Create a user
    siteManagerUser = "sitemanager" + System.currentTimeMillis();
    createUser(siteManagerUser);

    // Make user the site manager of the site
    _siteService.setMembership(site.getShortName(), siteManagerUser, SiteModel.SITE_MANAGER);

    // Run the tests as this user
    _authenticationComponent.setCurrentUser(siteManagerUser);
  }

  @Override
  public void afterClassSetup() {
    super.afterClassSetup();
    _authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    _siteService.deleteSite(site.getShortName());
    _personService.deletePerson(siteManagerUser);
    if (_authenticationService.authenticationExists(siteManagerUser)) {
      _authenticationService.deleteAuthentication(siteManagerUser);
    }
    _authenticationComponent.clearCurrentSecurityContext();
  }

  @Test
  public void testInSiteSimple() {
    NodeRef documentLibrary = _siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);

    NodeRef folder = _fileFolderService.create(documentLibrary, "testfolder", ContentModel.TYPE_FOLDER).getNodeRef();
    _nodeService.setProperty(folder, VgrModel.PROP_TYPE_TEMPLATENAME, "very nice template");

    NodeRef document = uploadDocument(site, "test.doc", null, null, null, folder, "vgr:document").getNodeRef();
    assertEquals(VgrModel.TYPE_VGR_DOCUMENT, _nodeService.getType(document));
    String template = (String) _nodeService.getProperty(document, VgrModel.PROP_TYPE_TEMPLATENAME);

    assertEquals("very nice template", template);
  }
/*
  @Test
  public void testInSiteWithMove() {
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

  @Test
  public void testInSiteWithCopy() {
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
*/
}
