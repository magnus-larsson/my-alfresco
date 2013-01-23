package se.vgregion.alfresco.repo.node;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;

import static org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import static org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;

public class AutoPublishPolicy extends AbstractPolicy implements OnCreateNodePolicy, OnUpdateNodePolicy {

  private static final Logger LOG = Logger.getLogger(AutoPublishPolicy.class);

  private static final String FILE_NODE_REF = AutoPublishPolicy.class.getName() + "_FILE_NODE_REF";
  private static final String FOLDER_NODE_REF = AutoPublishPolicy.class.getName() + "_FOLDER_NODE_REF";

  private FileFolderService _fileFolderService;

  private StorageService _storageService;

  private TransactionService _transactionService;

  private TransactionListener _transactionListener;

  public void setStorageService(StorageService storageService) {
    _storageService = storageService;
  }

  public void setFileFolderService(FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  public void setTransactionService(TransactionService transactionService) {
    _transactionService = transactionService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    _transactionListener = new AutoPublishTransactionListener();

    _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
    _policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onUpdateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
  }

  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    NodeRef folderNodeRef = childAssocRef.getParentRef();

    FileInfo folder = _fileFolderService.getFileInfo(folderNodeRef);

    if (folder == null) {
      return;
    }

    if (!folder.isFolder()) {
      return;
    }

    if (!_nodeService.exists(childAssocRef.getChildRef())) {
      return;
    }

    doPublish(folderNodeRef, childAssocRef.getChildRef());
  }

  @Override
  public void onUpdateNode(NodeRef fileNodeRef) {
    if (!_nodeService.exists(fileNodeRef)) {
      return;
    }

    NodeRef folderNodeRef = _nodeService.getPrimaryParent(fileNodeRef).getParentRef();

    FileInfo folder = _fileFolderService.getFileInfo(folderNodeRef);

    if (folder == null) {
      return;
    }

    if (!folder.isFolder()) {
      return;
    }

    doPublish(folderNodeRef, fileNodeRef);
  }

  private void doPublish(NodeRef folderNodeRef, NodeRef fileNodeRef) {
    AlfrescoTransactionSupport.bindResource(FILE_NODE_REF, fileNodeRef);
    AlfrescoTransactionSupport.bindResource(FOLDER_NODE_REF, folderNodeRef);

    AlfrescoTransactionSupport.bindListener(_transactionListener);
  }

  private class AutoPublishTransactionListener extends TransactionListenerAdapter {

    @Override
    public void afterCommit() {
      final NodeRef fileNodeRef = AlfrescoTransactionSupport.getResource(FILE_NODE_REF);
      final NodeRef folderNodeRef = AlfrescoTransactionSupport.getResource(FOLDER_NODE_REF);

      _transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {

        @Override
        public Object execute() throws Throwable {
          doInTransaction(fileNodeRef, folderNodeRef);

          return null;
        }

      }, false, true);
    }

  }

  private void doInTransaction(NodeRef fileNodeRef, NodeRef folderNodeRef) {
    if (!_serviceUtils.isDocumentLibrary(fileNodeRef)) {
      return;
    }

    // first check if the folder has the correct aspect
    if (!_nodeService.hasAspect(folderNodeRef, VgrModel.ASPECT_AUTO_PUBLISH)) {
      return;
    }

    // then check if the document fulfills the criteria stored on the folder
    Boolean autoPublishMajorVersion = (Boolean) _nodeService.getProperty(folderNodeRef, VgrModel.PROP_AUTO_PUBLISH_MAJOR_VERSION);
    Boolean autoPublishAllVersions = (Boolean) _nodeService.getProperty(folderNodeRef, VgrModel.PROP_AUTO_PUBLISH_ALL_VERSIONS);

    autoPublishMajorVersion = autoPublishMajorVersion != null ? autoPublishMajorVersion : false;
    autoPublishAllVersions = autoPublishAllVersions != null ? autoPublishAllVersions : false;

    boolean autoPublish = false;

    if (autoPublishMajorVersion) {
      String version = (String) _nodeService.getProperty(fileNodeRef, ContentModel.PROP_VERSION_LABEL);

      autoPublish = StringUtils.isBlank(version) ? false : version.endsWith(".0");
    }

    if (autoPublishAllVersions) {
      autoPublish = true;
    }

    if (!autoPublish) {
      return;
    }

    try {
      _behaviourFilter.disableAllBehaviours();

      _storageService.publishToStorage(fileNodeRef);

      _behaviourFilter.enableAllBehaviours();
    } catch (AlfrescoRuntimeException ex) {
      LOG.debug("Publish to storage failed cause document misses some properties.");
    }
  }

}