package se.vgregion.alfresco.repo.node;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;

public class AutoPublishPolicy extends AbstractPolicy implements OnCreateNodePolicy, OnUpdateNodePolicy {

  private static final Logger LOG = Logger.getLogger(AutoPublishPolicy.class);
  private static final String KEY_NODE_INFO = AutoPublishPolicy.class.getName() + ".nodeInfo";
  protected FileFolderService _fileFolderService;

  protected StorageService _storageService;

  protected TransactionService _transactionService;
  private boolean _initialized = false;

  private TransactionListener _transactionListener;

  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    NodeRef folderNodeRef = childAssocRef.getParentRef();

    FileInfo folder = getFileInfo(folderNodeRef);

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

  private FileInfo getFileInfo(final NodeRef folderNodeRef) {
    return AuthenticationUtil.runAsSystem(new RunAsWork<FileInfo>() {

      @Override
      public FileInfo doWork() throws Exception {
        return _fileFolderService.getFileInfo(folderNodeRef);
      }
    });
  }

  @Override
  public void onUpdateNode(NodeRef fileNodeRef) {
    if (!_nodeService.exists(fileNodeRef)) {
      return;
    }

    NodeRef folderNodeRef = _nodeService.getPrimaryParent(fileNodeRef).getParentRef();

    FileInfo folder = getFileInfo(folderNodeRef);

    if (folder == null) {
      return;
    }

    if (!folder.isFolder()) {
      return;
    }

    if (!_serviceUtils.isDocumentLibrary(fileNodeRef)) {
      return;
    }

    // first check if the folder has the correct aspect
    if (!_nodeService.hasAspect(folderNodeRef, VgrModel.ASPECT_AUTO_PUBLISH)) {
      return;
    }

    doPublish(folderNodeRef, fileNodeRef);
  }

  private void doPublish(NodeRef folderNodeRef, NodeRef fileNodeRef) {
    AlfrescoTransactionSupport.bindListener(_transactionListener);
    Set<Pair<NodeRef, NodeRef>> nodeRefs = (Set<Pair<NodeRef, NodeRef>>) AlfrescoTransactionSupport.getResource(KEY_NODE_INFO);
    if (nodeRefs == null) {
      nodeRefs = new HashSet<Pair<NodeRef, NodeRef>>(5);
      AlfrescoTransactionSupport.bindResource(KEY_NODE_INFO, nodeRefs);
    }
    Pair<NodeRef, NodeRef> pair = new Pair<NodeRef, NodeRef>(folderNodeRef, fileNodeRef);
    nodeRefs.add(pair); 
  }

  private class AutoPublishTransactionListener extends TransactionListenerAdapter {

    @Override
    public void afterCommit() {
      Set<Pair<NodeRef, NodeRef>> nodeRefs = (Set<Pair<NodeRef, NodeRef>>) AlfrescoTransactionSupport.getResource(KEY_NODE_INFO);
      if (nodeRefs != null) {
        for (Pair<NodeRef, NodeRef> nodeRefPair : nodeRefs) {
          final NodeRef folderNodeRef = nodeRefPair.getFirst();
          final NodeRef fileNodeRef = nodeRefPair.getSecond();

          _transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {

            @Override
            public Object execute() throws Throwable {
              doInTransaction(fileNodeRef, folderNodeRef);

              return null;
            }

          }, false, true);
        }
      }
    }

    private void doInTransaction(NodeRef fileNodeRef, NodeRef folderNodeRef) {

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
        _behaviourFilter.disableBehaviour();
        if (LOG.isDebugEnabled()) {
          LOG.debug("Autopublishing of node " + fileNodeRef);
        }
        _storageService.publishToStorage(fileNodeRef);

        _behaviourFilter.enableBehaviour();
      } catch (AlfrescoRuntimeException ex) {
        LOG.debug("Publish to storage failed cause document misses some properties.");
      }
    }

  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    Assert.notNull(_transactionService);
    Assert.notNull(_storageService);
    Assert.notNull(_fileFolderService);
    if (!_initialized) {
      _policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onUpdateNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

      _initialized = true;
    }

    _transactionListener = new AutoPublishTransactionListener();
  }

  @Required
  public void setFileFolderService(FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  @Required
  public void setStorageService(StorageService storageService) {
    _storageService = storageService;
  }

  @Required
  public void setTransactionService(TransactionService transactionService) {
    _transactionService = transactionService;
  }

}