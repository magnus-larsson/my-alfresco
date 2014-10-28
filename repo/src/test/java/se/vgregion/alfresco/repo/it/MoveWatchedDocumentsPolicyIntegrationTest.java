package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * Test for both MoveWatchedDocumentsPolicy and PreventPublishedDuplicatesPolicy
 * 
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class MoveWatchedDocumentsPolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {

  private static final String DEFAULT_USERNAME = "testuser_" + System.currentTimeMillis();

  @Resource(name = "OwnableService")
  private OwnableService _ownableService;

  @Autowired
  private Repository _repository;

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
    try {
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

      String filename = "test_" + System.currentTimeMillis() + ".doc";

      NodeRef document1 = uploadDocument(null, "test.doc", null, null, filename, folder, "vgr:document", properties).getNodeRef();

      assertType(document1, VgrModel.TYPE_VGR_DOCUMENT);
      assertHasAspect(document1, VgrModel.ASPECT_STANDARD);
      assertHasAspect(document1, VgrModel.ASPECT_PUBLISHED);

      try {
        uploadDocument(null, "test.doc", null, null, filename, folder, "vgr:document", properties).getNodeRef();
        fail();
      } catch (Exception ex) {
        // do nothing, this exception is supposed to be
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

}
