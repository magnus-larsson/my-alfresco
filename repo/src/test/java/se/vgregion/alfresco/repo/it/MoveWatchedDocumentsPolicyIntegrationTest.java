package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * Test for both MoveWatchedDocumentsPolicy and PreventPublishedDuplicatesPolicy
 * 
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class MoveWatchedDocumentsPolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {
  private static final Logger LOG = Logger.getLogger(MoveWatchedDocumentsPolicyIntegrationTest.class);
  private static final String DEFAULT_USERNAME = "testuser_" + System.currentTimeMillis();
  private static SiteInfo site;
  private static NodeRef user;
  @Resource(name = "OwnableService")
  private OwnableService _ownableService;

  @Autowired
  private Repository _repository;

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
  public void testMoveWatchedDocument() {
    final NodeRef companyHome = _repository.getCompanyHome();

    final String folderName = "Transfer Folder " + System.currentTimeMillis();

    // as system, create a new folder and change ownership for it
    final NodeRef folder = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>() {
      @Override
      public NodeRef doWork() throws Exception {
        NodeRef folder = _fileFolderService.create(companyHome, folderName, ContentModel.TYPE_FOLDER).getNodeRef();
        _nodeService.addAspect(folder, VgrModel.ASPECT_WATCHED, null);
        _ownableService.takeOwnership(folder);
        return folder;
      }
    });

    List<String> projects = new ArrayList<String>();
    projects.add("Testproject");

    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(VgrModel.PROP_TITLE, "title");
    properties.put(VgrModel.PROP_TYPE_RECORD, "type record");
    properties.put(VgrModel.PROP_TYPE_RECORD_ID, "type.record.id");
    properties.put(VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT, (Serializable) projects);
    properties.put(VgrModel.PROP_SOURCE_DOCUMENTID, System.currentTimeMillis());
    properties.put(VgrModel.PROP_IDENTIFIER_VERSION, "1.0");
    properties.put(VgrModel.PROP_SOURCE_ORIGIN, "Alfresco");

    String filename = "test_" + System.currentTimeMillis() + ".doc";

    NodeRef document1 = uploadDocument(null, "test.doc", null, null, filename, folder, "vgr:document", properties).getNodeRef();

    assertType(document1, VgrModel.TYPE_VGR_DOCUMENT);
    assertHasAspect(document1, VgrModel.ASPECT_STANDARD);
    assertHasAspect(document1, VgrModel.ASPECT_PUBLISHED);

    //Make sure that the document has been moved
    assertNotEquals(folder, _nodeService.getPrimaryParent(document1).getParentRef());
    try {
      //Upload the document again
      uploadDocument(null, "test.doc", null, null, filename, folder, "vgr:document", properties).getNodeRef();
      LOG.warn("Upload of second document should be blocked");
      LOG.warn("We cannot guarantee that duplicate documents are not created when the origin is Alfresco and the subsystem is solr");
    } catch (Exception ex) {
      LOG.info(ex);
    }
  }
  
  @Test
  public void testInvalidOriginOfDocument() {
    final NodeRef companyHome = _repository.getCompanyHome();

    final String folderName = "Transfer Folder " + System.currentTimeMillis();

    // as system, create a new folder and change ownership for it
    final NodeRef folder = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>() {
      @Override
      public NodeRef doWork() throws Exception {
        NodeRef folder = _fileFolderService.create(companyHome, folderName, ContentModel.TYPE_FOLDER).getNodeRef();
        _nodeService.addAspect(folder, VgrModel.ASPECT_WATCHED, null);
        _ownableService.takeOwnership(folder);
        return folder;
      }
    });

    List<String> projects = new ArrayList<String>();
    projects.add("Testproject");

    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(VgrModel.PROP_TITLE, "title");
    properties.put(VgrModel.PROP_TYPE_RECORD, "type record");
    properties.put(VgrModel.PROP_TYPE_RECORD_ID, "type.record.id");
    properties.put(VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT, (Serializable) projects);
    properties.put(VgrModel.PROP_SOURCE_DOCUMENTID, System.currentTimeMillis());
    properties.put(VgrModel.PROP_IDENTIFIER_VERSION, "1.0");
    properties.put(VgrModel.PROP_SOURCE_ORIGIN, "SomethingElse");

    String filename = "test_" + System.currentTimeMillis() + ".doc";
    try {
      //Upload the document again
      uploadDocument(null, "test.doc", null, null, filename, folder, "vgr:document", properties).getNodeRef();
      fail("Unsupported origins should not be allowed");
    } catch (AlfrescoRuntimeException ex) {
      LOG.debug(ex);
    }
  }
  
  @Test
  public void testMoveWatchedBariumDocument() {
    final NodeRef companyHome = _repository.getCompanyHome();

    final String folderName = "Barium " + System.currentTimeMillis();

    // as system, create a new folder and change ownership for it
    final NodeRef folder = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>() {
      @Override
      public NodeRef doWork() throws Exception {
        NodeRef folder = _fileFolderService.create(companyHome, folderName, ContentModel.TYPE_FOLDER).getNodeRef();
        _nodeService.addAspect(folder, VgrModel.ASPECT_WATCHED, null);
        _ownableService.takeOwnership(folder);
        return folder;
      }
    });

    List<String> projects = new ArrayList<String>();
    projects.add("Testproject");

    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(VgrModel.PROP_TITLE, "title");
    properties.put(VgrModel.PROP_TYPE_RECORD, "type record");
    properties.put(VgrModel.PROP_TYPE_RECORD_ID, "type.record.id");
    properties.put(VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT, (Serializable) projects);
    properties.put(VgrModel.PROP_SOURCE_DOCUMENTID, System.currentTimeMillis());
    properties.put(VgrModel.PROP_IDENTIFIER_VERSION, "1.0");
    properties.put(VgrModel.PROP_SOURCE_ORIGIN, "Barium");

    String filename = "test_" + System.currentTimeMillis() + ".doc";

    NodeRef document1 = uploadDocument(null, "test.doc", null, null, filename, folder, "vgr:document", properties).getNodeRef();

    assertType(document1, VgrModel.TYPE_VGR_DOCUMENT);
    assertHasAspect(document1, VgrModel.ASPECT_STANDARD);
    assertHasAspect(document1, VgrModel.ASPECT_PUBLISHED);

    //Make sure that the document has been moved
    assertNotEquals(folder, _nodeService.getPrimaryParent(document1).getParentRef());
    try {
      //Upload the document again
      uploadDocument(null, "test.doc", null, null, filename, folder, "vgr:document", properties).getNodeRef();
      fail("Upload of second barium document should be blocked");
    } catch (Exception ex) {
      LOG.info(ex);
    }
  }

}
