package se.vgregion.alfresco.repo.it.storage;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.apache.chemistry.opencmis.server.support.query.CmisQlExtParser_CmisBaseGrammar.null_predicate_return;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import se.vgregion.alfresco.repo.it.AbstractVgrRepoIntegrationTest;
import se.vgregion.alfresco.repo.it.node.AddPdfaPolicyIntegrationTest;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;

/**
 * This test is disabled when no pdfaPilot server is available
 * 
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class PublishToStorageIntegrationTest extends AbstractVgrRepoIntegrationTest {
  private static final Logger LOG = Logger.getLogger(PublishToStorageIntegrationTest.class);

  private static final String DEFAULT_USERNAME = "testuser";

  private SiteInfo _site;

  @Autowired
  protected StorageService _storageService;

  @Autowired
  @Qualifier(value = "LockService")
  protected LockService _lockService;

  @Resource(name = "transformer.pdfaPilot")
  protected ContentTransformer _transformer;

  @Override
  public void beforeClassSetup() {
    super.beforeClassSetup();

    createUser(DEFAULT_USERNAME);

    _authenticationComponent.setCurrentUser(DEFAULT_USERNAME);

    _site = createSite();
  }

  @Override
  public void afterClassSetup() {
    deleteSite(_site);

    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());

    deleteUser(DEFAULT_USERNAME);

    _authenticationComponent.clearCurrentSecurityContext();

    super.afterClassSetup();
  }

  @Test
  public void testPublish() {
    LOG.info("================================================");
    LOG.info("= testPublish begin                            =");
    LOG.info("================================================");
    setRequiresNew(true);

    // this happens if pdfaPilot is not enabled and isn't around to do the work
    /*
     * if (!_transformer.isTransformable("application/pdf", -1,
     * "application/pdf", null)) { return; }
     */

    final NodeRef sourceDocument = _transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {

      @Override
      public NodeRef execute() throws Throwable {
        return uploadDocument(_site, "test.doc").getNodeRef();
      }
    }, false, true);

    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        _nodeService.setProperty(sourceDocument, VgrModel.PROP_TYPE_RECORD, "type record");
        _nodeService.setProperty(sourceDocument, VgrModel.PROP_TYPE_RECORD_ID, "dc.type.record");
        _nodeService.setProperty(sourceDocument, VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT, "publisher project assignment");
        _nodeService.setProperty(sourceDocument, VgrModel.PROP_TITLE, "title");

        return null;
      }
    }, false, true);

    NodeRef publishedDocument = _transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {

      @Override
      public NodeRef execute() throws Throwable {
        LOG.info("Publishing document first time");
        return _storageService.publishToStorage(sourceDocument, false);
      }
    }, false, true);

    assertNotNull(publishedDocument);
    assertTrue(_nodeService.exists(publishedDocument));
    try {
      _transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {

        @Override
        public NodeRef execute() throws Throwable {
          LOG.info("Publishing document second time");
          return _storageService.publishToStorage(sourceDocument, false);
        }
      }, false, true);
      fail("Second publish of the same document should fail!");
    } catch (Exception e) {

    }
    LOG.info("================================================");
    LOG.info("= testPublish end                              =");
    LOG.info("================================================");
  }

  @Test
  public void testPublishOfLockedNode() {

    setRequiresNew(true);

    // this happens if pdfaPilot is not enabled and isn't around to do the work
    /*
     * if (!_transformer.isTransformable("application/pdf", -1,
     * "application/pdf", null)) { return; }
     */

    final NodeRef sourceDocument = _transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {

      @Override
      public NodeRef execute() throws Throwable {
        return uploadDocument(_site, "test.doc", null, null, "test_lock.doc").getNodeRef();
      }
    }, false, true);

    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        _nodeService.setProperty(sourceDocument, VgrModel.PROP_TYPE_RECORD, "type record");
        _nodeService.setProperty(sourceDocument, VgrModel.PROP_TYPE_RECORD_ID, "dc.type.record");
        _nodeService.setProperty(sourceDocument, VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT, "publisher project assignment");
        _nodeService.setProperty(sourceDocument, VgrModel.PROP_TITLE, "title");
        _lockService.lock(sourceDocument, LockType.WRITE_LOCK);
        return null;
      }
    }, false, true);

    try {
      _transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {

        @Override
        public NodeRef execute() throws Throwable {
          return _storageService.publishToStorage(sourceDocument, false);
        }
      }, false, true);
      fail("Publishing should not be accepted on a locked node");
    } catch (Exception e) {

    }
  }

  @Test
  public void testPublishOfWorkingCopyNode() {

    setRequiresNew(true);

    // this happens if pdfaPilot is not enabled and isn't around to do the work
    /*
     * if (!_transformer.isTransformable("application/pdf", -1,
     * "application/pdf", null)) { return; }
     */

    final NodeRef sourceDocument = _transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {

      @Override
      public NodeRef execute() throws Throwable {
        return uploadDocument(_site, "test.doc", null, null, "test_working_copy.doc").getNodeRef();
      }
    }, false, true);

    _transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        _nodeService.setProperty(sourceDocument, VgrModel.PROP_TYPE_RECORD, "type record");
        _nodeService.setProperty(sourceDocument, VgrModel.PROP_TYPE_RECORD_ID, "dc.type.record");
        _nodeService.setProperty(sourceDocument, VgrModel.PROP_PUBLISHER_PROJECT_ASSIGNMENT, "publisher project assignment");
        _nodeService.setProperty(sourceDocument, VgrModel.PROP_TITLE, "title");
        _nodeService.addAspect(sourceDocument, ContentModel.ASPECT_WORKING_COPY, null);
        return null;
      }
    }, false, true);

    try {
      _transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {

        @Override
        public NodeRef execute() throws Throwable {
          return _storageService.publishToStorage(sourceDocument, false);
        }
      }, false, true);
      fail("Publishing should not be accepted on a working copy node");
    } catch (Exception e) {

    }
  }
}
