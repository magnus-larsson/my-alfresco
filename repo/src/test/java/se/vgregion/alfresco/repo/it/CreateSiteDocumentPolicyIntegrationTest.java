package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.*;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class CreateSiteDocumentPolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {
  private static final Logger LOG = Logger.getLogger(CreateSiteDocumentPolicyIntegrationTest.class);
  private static final String DEFAULT_USERNAME = "testuser";
  private static SiteInfo site;
  private static NodeRef user;

  @Override
  public void beforeClassSetup() {
    LOG.debug("beforeClassSetup");
    super.beforeClassSetup();
    user = createUser(DEFAULT_USERNAME);
    LOG.debug("Created user " + DEFAULT_USERNAME + ": " + user);
    _authenticationComponent.setCurrentUser(DEFAULT_USERNAME);
    site = createSite();
    LOG.debug("Created site " + site.getShortName());
  }

  @Override
  public void afterClassSetup() {
    LOG.debug("afterClassSetup");
    super.afterClassSetup();
    deleteSite(site);
    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());
    deleteUser(DEFAULT_USERNAME);
    _authenticationComponent.clearCurrentSecurityContext();
  }

  @Test
  public void testInSiteSimple() {
    NodeRef documentLibrary = _siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);

    NodeRef folder = _fileFolderService.create(documentLibrary, "testfolder", ContentModel.TYPE_FOLDER).getNodeRef();
    _nodeService.setProperty(folder, VgrModel.PROP_TYPE_TEMPLATENAME, "very nice template");
    assertNotNull(_transactionHelper);
    NodeRef document = uploadDocument(site, "test.doc", null, null, "test1.doc", folder, "vgr:document").getNodeRef();

    String template = (String) _nodeService.getProperty(document, VgrModel.PROP_TYPE_TEMPLATENAME);

    assertEquals("very nice template", template);
  }
/*
  @Test
  public void testInSiteWithMove() {
    NodeRef documentLibrary = _siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);

    NodeRef folder = _fileFolderService.create(documentLibrary, "testfolder2", ContentModel.TYPE_FOLDER).getNodeRef();

    _nodeService.setProperty(folder, VgrModel.PROP_TYPE_TEMPLATENAME, "very nice template");

    assertNotNull(_transactionHelper);
    final NodeRef document = uploadDocument(site, "test.doc", null, null, "test2.doc", null, "vgr:document").getNodeRef();

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
*/
  @Test
  public void testInSiteWithCopy() {
    NodeRef documentLibrary = _siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);

    NodeRef folder = _fileFolderService.create(documentLibrary, "testfolder3", ContentModel.TYPE_FOLDER).getNodeRef();
    _nodeService.setProperty(folder, VgrModel.PROP_TYPE_TEMPLATENAME, "very nice template");

    NodeRef document = uploadDocument(site, "test.doc", null, null, "test3.doc", null, "vgr:document").getNodeRef();

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
