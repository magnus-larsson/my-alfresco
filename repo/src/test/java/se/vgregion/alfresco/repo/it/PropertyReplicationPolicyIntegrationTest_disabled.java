package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.junit.Test;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * Test for both MoveWatchedDocumentsPolicy and PreventPublishedDuplicatesPolicy
 * 
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class PropertyReplicationPolicyIntegrationTest_disabled extends AbstractVgrRepoIntegrationTest {

  private static final String DEFAULT_USERNAME = "testuser_" + System.currentTimeMillis();

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
    NodeRef document = uploadDocument(site, "test.doc", null, null, "test.doc").getNodeRef();

    String title = "title - " + System.currentTimeMillis();
    _nodeService.setProperty(document, VgrModel.PROP_TITLE, title);
    assertEquals(title, _nodeService.getProperty(document, ContentModel.PROP_TITLE));
    
    String description = "description - " + System.currentTimeMillis();
    _nodeService.setProperty(document, VgrModel.PROP_DESCRIPTION, description);
    assertEquals(description, _nodeService.getProperty(document, ContentModel.PROP_DESCRIPTION));
    
    Date modified1 = (Date) _nodeService.getProperty(document, ContentModel.PROP_MODIFIED);
    Date modified2 = (Date) _nodeService.getProperty(document, VgrModel.PROP_DATE_SAVED);
    assertTrue(modified1.equals(modified2));
    
    _nodeService.setProperty(document, ContentModel.PROP_NAME, "testar.doc");
    assertEquals("testar.doc", _nodeService.getProperty(document, VgrModel.PROP_TITLE_FILENAME));
    
    _nodeService.setProperty(document, ContentModel.PROP_NAME, "testar");
    assertEquals("doc", _nodeService.getProperty(document, VgrModel.PROP_FORMAT_EXTENT_EXTENSION));
    _nodeService.setProperty(document, ContentModel.PROP_NAME, "testar.pdf");
    assertEquals("pdf", _nodeService.getProperty(document, VgrModel.PROP_FORMAT_EXTENT_EXTENSION));
    
    assertEquals("application/msword", _nodeService.getProperty(document, VgrModel.PROP_FORMAT_EXTENT_MIMETYPE));
    
    assertEquals(DEFAULT_USERNAME + " Test ("+DEFAULT_USERNAME+")", _nodeService.getProperty(document, VgrModel.PROP_CONTRIBUTOR_SAVEDBY));
    assertEquals(DEFAULT_USERNAME, _nodeService.getProperty(document, VgrModel.PROP_CONTRIBUTOR_SAVEDBY_ID));
    
    String version1 = (String) _nodeService.getProperty(document, ContentModel.PROP_VERSION_LABEL);
    String version2 = (String) _nodeService.getProperty(document, VgrModel.PROP_IDENTIFIER_VERSION);
    assertTrue(version1.equals(version2));
    
    // the extension is a calculated property and can't be set
    _nodeService.setProperty(document, VgrModel.PROP_TITLE, "simple_title");
    assertEquals("simple_title.pdf", _nodeService.getProperty(document, ContentModel.PROP_NAME));
    _nodeService.setProperty(document, VgrModel.PROP_FORMAT_EXTENT_EXTENSION, "doc");
    assertEquals("simple_title.pdf", _nodeService.getProperty(document, ContentModel.PROP_NAME));
  }
}
