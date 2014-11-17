package se.vgregion.alfresco.repo.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
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
  private SiteInfo site;

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

    

    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
      @Override
      public Void execute() throws Throwable {
        _nodeService.setProperty(document, VgrModel.PROP_TITLE, "testar");
        
        _nodeService.setProperty(document, ContentModel.PROP_NAME, "testar.doc");

        return null;
      }
    }, false, true);
    
    assertEquals("testar.doc", _nodeService.getProperty(document, ContentModel.PROP_NAME));
    assertEquals("testar.doc", _nodeService.getProperty(document, VgrModel.PROP_TITLE_FILENAME));

    final String title = "title - " + System.currentTimeMillis();
    final String description = "description - " + System.currentTimeMillis();

    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
      @Override
      public Void execute() throws Throwable {

        _nodeService.setProperty(document, VgrModel.PROP_TITLE, title);

        _nodeService.setProperty(document, VgrModel.PROP_DESCRIPTION, description);

        _nodeService.setProperty(document, ContentModel.PROP_NAME, "testar.doc");

        return null;
      }
    }, false, true);

    assertEquals(title, _nodeService.getProperty(document, ContentModel.PROP_TITLE));
    assertEquals(description, _nodeService.getProperty(document, ContentModel.PROP_DESCRIPTION));
    Date modified1 = (Date) _nodeService.getProperty(document, ContentModel.PROP_MODIFIED);
    Date modified2 = (Date) _nodeService.getProperty(document, VgrModel.PROP_DATE_SAVED);
    assertTrue(modified1.equals(modified2));

    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
      @Override
      public Void execute() throws Throwable {
        _nodeService.setProperty(document, ContentModel.PROP_NAME, "testar");
        return null;
      }
    }, false, true);

    assertEquals("doc", _nodeService.getProperty(document, VgrModel.PROP_FORMAT_EXTENT_EXTENSION));

    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
      @Override
      public Void execute() throws Throwable {
        _nodeService.setProperty(document, ContentModel.PROP_NAME, "testar.pdf");

        // the extension is a calculated property and can't be set
        _nodeService.setProperty(document, VgrModel.PROP_TITLE, "simple_title");
        return null;
      }
    }, false, true);

    assertEquals("pdf", _nodeService.getProperty(document, VgrModel.PROP_FORMAT_EXTENT_EXTENSION));

    assertEquals("application/msword", _nodeService.getProperty(document, VgrModel.PROP_FORMAT_EXTENT_MIMETYPE));

    assertEquals(DEFAULT_USERNAME + " Test (" + DEFAULT_USERNAME + ")", _nodeService.getProperty(document, VgrModel.PROP_CONTRIBUTOR_SAVEDBY));
    assertEquals(DEFAULT_USERNAME, _nodeService.getProperty(document, VgrModel.PROP_CONTRIBUTOR_SAVEDBY_ID));

    String version1 = (String) _nodeService.getProperty(document, ContentModel.PROP_VERSION_LABEL);
    String version2 = (String) _nodeService.getProperty(document, VgrModel.PROP_IDENTIFIER_VERSION);
    assertTrue(version1.equals(version2));

    assertEquals("simple_title.pdf", _nodeService.getProperty(document, ContentModel.PROP_NAME));
    
    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
      @Override
      public Void execute() throws Throwable {
        _nodeService.setProperty(document, VgrModel.PROP_FORMAT_EXTENT_EXTENSION, "doc");
        return null;
      }
    }, false, true);
    
    assertEquals("simple_title.pdf", _nodeService.getProperty(document, ContentModel.PROP_NAME));

  }
}
