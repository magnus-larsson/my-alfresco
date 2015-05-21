package se.vgregion.alfresco.repo.it.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.junit.Test;

import se.vgregion.alfresco.repo.it.AbstractVgrRepoIntegrationTest;
import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * 
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 * @author Marcus Svensson (marcus.svensson@redpill-linpro.com)
 */
public class PropertyReplicationPolicyIntegrationTest extends AbstractVgrRepoIntegrationTest {

  private static final String DEFAULT_USERNAME = "testuser_" + System.currentTimeMillis();
  private static SiteInfo site;

  @Override
  public void beforeClassSetup() {
    super.beforeClassSetup();
    createUser(DEFAULT_USERNAME);
    _authenticationComponent.setCurrentUser(DEFAULT_USERNAME);
    site = createSite();
  }

  @Override
  public void afterClassSetup() {
    deleteSite(site);
    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());
    deleteUser(DEFAULT_USERNAME);
    _authenticationComponent.clearCurrentSecurityContext();
  }

  @Test
  public void testPropertyReplication() throws InterruptedException {
    final NodeRef document = uploadDocument(site, "test.doc", null, null, "test.doc").getNodeRef();
    assertEquals("test", _nodeService.getProperty(document, ContentModel.PROP_TITLE));
    assertEquals("test", _nodeService.getProperty(document, VgrModel.PROP_TITLE));
    assertEquals("test.doc", _nodeService.getProperty(document, ContentModel.PROP_NAME));
    assertEquals("test.doc", _nodeService.getProperty(document, VgrModel.PROP_TITLE_FILENAME));
    assertEquals("doc", _nodeService.getProperty(document, VgrModel.PROP_FORMAT_EXTENT_EXTENSION));

    // Verify that when title is updated, name is NOT updated and that the title field can contain special characters
    _nodeService.setProperty(document, VgrModel.PROP_TITLE, "test2 ... /*3:?  . . ..  ");
    assertEquals("test2 ... /*3:?  . . ..  ", _nodeService.getProperty(document, VgrModel.PROP_TITLE));
    assertEquals("test2 ... /*3:?  . . ..  ", _nodeService.getProperty(document, ContentModel.PROP_TITLE));
    assertEquals("test.doc", _nodeService.getProperty(document, ContentModel.PROP_NAME));

    // Do allow allow changing prop_name
    _nodeService.setProperty(document, ContentModel.PROP_NAME, "test3.doc");
    assertEquals("test2 ... /*3:?  . . ..  ", _nodeService.getProperty(document, ContentModel.PROP_TITLE));
    assertEquals("test3.doc", _nodeService.getProperty(document, ContentModel.PROP_NAME));

    // Test that description is replicated
    _nodeService.setProperty(document, VgrModel.PROP_DESCRIPTION, "desc");
    assertEquals("desc", _nodeService.getProperty(document, ContentModel.PROP_DESCRIPTION));

    // Verify that modified date is replicated
    Date modified1 = (Date) _nodeService.getProperty(document, ContentModel.PROP_MODIFIED);
    Date modified2 = (Date) _nodeService.getProperty(document, VgrModel.PROP_DATE_SAVED);
    assertTrue(modified1.equals(modified2));

    // Assert that mimetype was replicated
    assertEquals("application/msword", _nodeService.getProperty(document, VgrModel.PROP_FORMAT_EXTENT_MIMETYPE));

    // Assert that user name and username was replicated
    assertEquals(DEFAULT_USERNAME + " Test (" + DEFAULT_USERNAME + ")", _nodeService.getProperty(document, VgrModel.PROP_CONTRIBUTOR_SAVEDBY));
    assertEquals(DEFAULT_USERNAME, _nodeService.getProperty(document, VgrModel.PROP_CONTRIBUTOR_SAVEDBY_ID));

    // Assert that versions were replicated
    String version1 = (String) _nodeService.getProperty(document, ContentModel.PROP_VERSION_LABEL);
    String version2 = (String) _nodeService.getProperty(document, VgrModel.PROP_IDENTIFIER_VERSION);
    assertTrue(version1.equals(version2));

    // Test that changing the file name with a new extensino does not give a new
    // a new extension
    _nodeService.setProperty(document, VgrModel.PROP_TITLE, "test4");
    _nodeService.setProperty(document, ContentModel.PROP_NAME, "test5.pdf");
    assertEquals("doc", _nodeService.getProperty(document, VgrModel.PROP_FORMAT_EXTENT_EXTENSION));
    assertEquals("application/msword", _nodeService.getProperty(document, VgrModel.PROP_FORMAT_EXTENT_MIMETYPE));
    assertEquals("test5.doc", _nodeService.getProperty(document, ContentModel.PROP_NAME));

    // Test that extension is replicated to name if changed
    _nodeService.setProperty(document, VgrModel.PROP_FORMAT_EXTENT_EXTENSION, "pdf");
    assertEquals("test5.pdf", _nodeService.getProperty(document, ContentModel.PROP_NAME));
  }

}
