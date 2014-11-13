package se.vgregion.alfresco.repo.node;

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;

public class AutoPublishPolicy extends AbstractPolicy implements OnCreateNodePolicy, OnUpdateNodePolicy {

  private static final Logger LOG = Logger.getLogger(AutoPublishPolicy.class);

  private FileFolderService _fileFolderService;

  private StorageService _storageService;

  public void setStorageService(StorageService storageService) {
    _storageService = storageService;
  }

  public void setFileFolderService(FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  public void setTransactionService(TransactionService transactionService) {
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
    _policyComponent.bindClassBehaviour(OnUpdateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onUpdateNode", NotificationFrequency.TRANSACTION_COMMIT));
  }

  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    NodeRef folderNode = childAssocRef.getParentRef();

    FileInfo folder = getFileInfo(folderNode);

    if (folder == null) {
      return;
    }

    if (!folder.isFolder()) {
      return;
    }

    if (!_nodeService.exists(childAssocRef.getChildRef())) {
      return;
    }

    doPublish(folderNode, childAssocRef.getChildRef());
  }

  private FileInfo getFileInfo(final NodeRef folderNode) {
    return AuthenticationUtil.runAsSystem(new RunAsWork<FileInfo>() {

      @Override
      public FileInfo doWork() throws Exception {
        return _fileFolderService.getFileInfo(folderNode);
      }
    });
  }

  @Override
  public void onUpdateNode(NodeRef fileNode) {
    if (!_nodeService.exists(fileNode)) {
      return;
    }

    NodeRef folderNode = _nodeService.getPrimaryParent(fileNode).getParentRef();

    FileInfo folder = getFileInfo(folderNode);

    if (folder == null) {
      return;
    }

    if (!folder.isFolder()) {
      return;
    }

    
    doPublish(folderNode, fileNode);
  }
  
  private void doPublish(NodeRef folderNode, NodeRef fileNode) {
    if (!_nodeService.exists(fileNode)) {
      return;
    }

    if (!_nodeService.exists(folderNode)) {
      return;
    }

    if (!_serviceUtils.isDocumentLibrary(fileNode)) {
      return;
    }

    // first check if the folder has the correct aspect
    if (!_nodeService.hasAspect(folderNode, VgrModel.ASPECT_AUTO_PUBLISH)) {
      return;
    }

    NodeRef publishedNode = _storageService.getLatestPublishedStorageVersion(fileNode.toString());
    
    if (_nodeService.getProperty(fileNode, VgrModel.PROP_IDENTIFIER_VERSION) == null) {
      return;
    }

    float currentVersion = getNodeVersion(fileNode);

    if (publishedNode != null) {
      float publishedVersion = getNodeVersion(publishedNode);

      // this version is already published
      if (currentVersion == publishedVersion) {
        return;
      }
    }

    // then check if the document fulfills the criteria stored on the folder
    Boolean autoPublishMajorVersion = (Boolean) _nodeService.getProperty(folderNode, VgrModel.PROP_AUTO_PUBLISH_MAJOR_VERSION);
    Boolean autoPublishAllVersions = (Boolean) _nodeService.getProperty(folderNode, VgrModel.PROP_AUTO_PUBLISH_ALL_VERSIONS);

    autoPublishMajorVersion = autoPublishMajorVersion != null ? autoPublishMajorVersion : false;
    autoPublishAllVersions = autoPublishAllVersions != null ? autoPublishAllVersions : false;

    boolean autoPublish = false;

    if (autoPublishMajorVersion) {
      String version = (String) _nodeService.getProperty(fileNode, ContentModel.PROP_VERSION_LABEL);

      autoPublish = StringUtils.isBlank(version) ? false : version.endsWith(".0");
    }

    if (autoPublishAllVersions) {
      autoPublish = true;
    }

    if (!autoPublish) {
      return;
    }

    _behaviourFilter.disableBehaviour(fileNode);
    
    try {
      _storageService.publishToStorage(fileNode);
    } catch (AlfrescoRuntimeException ex) {
      LOG.debug("Publish to storage failed cause document misses some properties.");
    } finally {
      _behaviourFilter.enableBehaviour(fileNode);
    }
  }

  private Float getNodeVersion(NodeRef fileNode) {
    Serializable version = _nodeService.getProperty(fileNode, VgrModel.PROP_IDENTIFIER_VERSION);
    
    if (version == null) {
      return null;
    }
    
    return Float.parseFloat(version.toString());
  }

}