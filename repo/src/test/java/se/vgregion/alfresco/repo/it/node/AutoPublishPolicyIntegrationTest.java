package se.vgregion.alfresco.repo.it.node;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import se.vgregion.alfresco.repo.it.AbstractVgrRepoIntegrationTest;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;

public class AutoPublishPolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {

  private static final Logger LOG = Logger.getLogger(AutoPublishPolicyIntegrationTest.class);

  private static final String DEFAULT_USERNAME = "testuser_" + System.currentTimeMillis();
  private static SiteInfo site;
  private static NodeRef user;

  @Autowired
  private StorageService _storageService;

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
  public void testAutoPublish() throws FileExistsException, FileNotFoundException {
    final NodeRef documentLibrary = _siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);

    final NodeRef autopublishfolder = _fileFolderService.create(documentLibrary, "autopublishfolder", ContentModel.TYPE_FOLDER).getNodeRef();
    assertNotNull(autopublishfolder);

    final List<String> projects = new ArrayList<String>();
    projects.add("Project");

    _nodeService.addAspect(autopublishfolder, VgrModel.ASPECT_AUTO_PUBLISH, null);
    _nodeService.setProperty(autopublishfolder, VgrModel.PROP_AUTO_PUBLISH_ALL_VERSIONS, true);
    Set<NodeRef> documents = _transactionHelper.doInTransaction(new RetryingTransactionCallback<Set<NodeRef>>() {
      @Override
      public Set<NodeRef> execute() throws Throwable {
        Set<NodeRef> documents = new HashSet<NodeRef>();
        NodeRef document = uploadDocument(site, "test.doc", null, null, "test1.doc", autopublishfolder).getNodeRef();
        _nodeService.setProperty(document, VgrModel.PROP_TYPE_RECORD, "type record");
        _nodeService.setProperty(document, VgrModel.PROP_TYPE_RECORD_ID, "123456");
        _nodeService.setProperty(document, VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT, (Serializable) projects);
        documents.add(document);
        List<AssociationRef> targetAssocs = _nodeService.getTargetAssocs(document, VgrModel.ASSOC_PUBLISHED_TO_STORAGE);
        assertEquals(0, targetAssocs.size());

        document = uploadDocument(site, "test.doc", null, null, "test2.doc", autopublishfolder).getNodeRef();
        _nodeService.setProperty(document, VgrModel.PROP_TYPE_RECORD, "type record");
        _nodeService.setProperty(document, VgrModel.PROP_TYPE_RECORD_ID, "123456");
        _nodeService.setProperty(document, VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT, (Serializable) projects);
        documents.add(document);
        targetAssocs = _nodeService.getTargetAssocs(document, VgrModel.ASSOC_PUBLISHED_TO_STORAGE);
        assertEquals(0, targetAssocs.size());

        return documents;
      }
    }, false, true);
    assertEquals(2, documents.size());
    for (NodeRef document : documents) {
      LOG.info("Testing document: " + document);
      List<AssociationRef> targetAssocs = _nodeService.getTargetAssocs(document, VgrModel.ASSOC_PUBLISHED_TO_STORAGE);
      assertEquals(1, targetAssocs.size());
      NodeRef publishedNode = targetAssocs.get(0).getTargetRef();

      assertTrue(_nodeService.hasAspect(publishedNode, VgrModel.ASPECT_PUBLISHED));
    }
  }

}
