package se.vgregion.alfresco.repo.rendition.executer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnDeleteNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.FailedRenditionInfo;
import se.vgregion.alfresco.repo.storage.StorageService;

/**
 * Behaviour/Policies for the {@link VgrModel#ASPECT_FAILED_RENDITION_SOURCE}
 * aspect. When the last {@link VgrModel#TYPE_FAILED_RENDITION} child is deleted
 * from under a source node, then all failures are considered removed and the
 * {@link VgrModel#ASPECT_FAILED_RENDITION_SOURCE} aspect can be removed.
 * <p/>
 * Also, any {@link VgrModel#TYPE_FAILED_RENDITION failed renditions} should be
 * removed from the model onUpdateProperties as the new content may have become
 * rendable.
 * 
 * @author Niklas Ekman
 */
public class FailedRenditionSourceAspect implements OnDeleteNodePolicy, OnContentUpdatePolicy, BeforeCreateNodePolicy, OnCreateNodePolicy, InitializingBean {

  private static final Logger LOG = Logger.getLogger(FailedRenditionSourceAspect.class);

  private BehaviourFilter _behaviourFilter;

  private NodeService _nodeService;

  private PolicyComponent _policyComponent;

  private LockService _lockService;

  private StorageService _storageService;

  @Override
  public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
    if (!_nodeService.exists(childAssocRef.getParentRef())) {
      // We are in the process of deleting the parent.
      return;
    }

    // When a failedRendition node has been deleted, we should check if there
    // are any other failedRendition peer nodes left. If there are not, then we
    // can remove the failedRenditionSource aspect.

    Map<String, FailedRenditionInfo> failures = _storageService.getFailedRenditions(childAssocRef.getParentRef());

    if (failures.isEmpty()) {
      if (LOG.isDebugEnabled()) {
        StringBuilder msg = new StringBuilder();

        msg.append("No remaining failedRendition children of ").append(childAssocRef.getParentRef()).append(" therefore removing aspect ").append(VgrModel.ASPECT_FAILED_RENDITION_SOURCE);

        LOG.debug(msg.toString());
      }

      _behaviourFilter.disableBehaviour(childAssocRef.getParentRef(), ContentModel.ASPECT_AUDITABLE);

      try {
        _nodeService.removeAspect(childAssocRef.getParentRef(), VgrModel.ASPECT_FAILED_RENDITION_SOURCE);
      } finally {
        _behaviourFilter.enableBehaviour(childAssocRef.getParentRef(), ContentModel.ASPECT_AUDITABLE);
      }
    }
  }

  @Override
  public void onContentUpdate(final NodeRef nodeRef, boolean newContent) {
    AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {

      @Override
      public Object doWork() {
        if (_nodeService.exists(nodeRef) && _lockService.getLockStatus(nodeRef) != LockStatus.LOCKED) {
          deleteFailedRenditionChildren(nodeRef);
        }

        return null;
      }

    });
  }

  /**
   * Delete all cm:failedRendition children as they represent a failure to
   * render the old content. By deleting all cm:failedRendition children, the
   * cm:failedRenditionSource aspect will be automatically removed by a
   * policy/behaviour in the ThumbnailService.
   *
   * This is necessary so that if a new version of a 'broken' document is
   * uploaded, then it will be rendered in the normal way.
   */
  private void deleteFailedRenditionChildren(NodeRef nodeRef) {
    Map<String, FailedRenditionInfo> failedRenditions = _storageService.getFailedRenditions(nodeRef);

    _behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);

    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Deleting " + failedRenditions.size() + " " + VgrModel.TYPE_FAILED_RENDITION + " nodes");
      }

      for (Entry<String, FailedRenditionInfo> entry : failedRenditions.entrySet()) {
        FailedRenditionInfo info = entry.getValue();

        _nodeService.deleteNode(info.getFailedRenditionNode());
      }
    } finally {
      _behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
    }
  }

  @Override
  public void beforeCreateNode(NodeRef parentRef, QName assocTypeQName, QName assocQName, QName nodeTypeQName) {
    // When a rendition has failed, we must delete any existing (successful)
    // renditions of that renditionDefinition.
    if (VgrModel.TYPE_FAILED_RENDITION.equals(nodeTypeQName)) {
      // In fact there should only be zero or one such renditions
      Set<QName> childNodeTypes = new HashSet<QName>();

      childNodeTypes.add(ContentModel.TYPE_CONTENT);

      List<ChildAssociationRef> existingRenditions = _nodeService.getChildAssocs(parentRef, childNodeTypes);

      for (ChildAssociationRef chAssRef : existingRenditions) {
        String name = (String) _nodeService.getProperty(chAssRef.getChildRef(), ContentModel.PROP_NAME);
        
        if (chAssRef.getQName().equals(assocQName) && name.equalsIgnoreCase("pdfa")) {
          if (LOG.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder();

            msg.append("Deleting rendtion node ").append(chAssRef.getChildRef());

            LOG.debug(msg.toString());
          }

          _nodeService.deleteNode(chAssRef.getChildRef());
        }
      }
    }

    // We can't respond to the creation of a cm:thumbnail node at this point as
    // they are created with
    // temporary assoc qnames and so cannot be matched to the relevant thumbnail
    // definition.
    // Instead we must do it "onCreateNode()"
  }

  @Override
  public void onCreateNode(ChildAssociationRef childAssoc) {
    // When a rendition succeeds, we must delete any existing rendition failure
    // nodes.
    String renditionName = (String) _nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME);
    
    // if it's not a PDF/A then quit
    if (!renditionName.equalsIgnoreCase("pdfa")) {
      return;
    }

    // In fact there should only be zero or one such failedRenditions
    Map<String, FailedRenditionInfo> failures = _storageService.getFailedRenditions(childAssoc.getParentRef());

    FailedRenditionInfo existingFailedRendition = failures.get(renditionName);

    if (existingFailedRendition != null) {
      if (LOG.isDebugEnabled()) {
        StringBuilder msg = new StringBuilder();

        msg.append("Deleting failedRendition node ").append(existingFailedRendition.getFailedRenditionNode());

        LOG.debug(msg.toString());
      }

      _nodeService.deleteNode(existingFailedRendition.getFailedRenditionNode());
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    _policyComponent.bindClassBehaviour(OnDeleteNodePolicy.QNAME, VgrModel.TYPE_FAILED_RENDITION, new JavaBehaviour(this, "onDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT));

    _policyComponent.bindClassBehaviour(OnContentUpdatePolicy.QNAME, VgrModel.ASPECT_FAILED_RENDITION_SOURCE, new JavaBehaviour(this, "onContentUpdate",
        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

    _policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.EVERY_EVENT));

    _policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeCreateNodePolicy.QNAME, VgrModel.TYPE_FAILED_RENDITION, new JavaBehaviour(this, "beforeCreateNode",
        Behaviour.NotificationFrequency.EVERY_EVENT));
  }

  public void setPolicyComponent(PolicyComponent policyComponent) {
    _policyComponent = policyComponent;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setStorageService(StorageService storageService) {
    _storageService = storageService;
  }

  public void setLockService(LockService lockService) {
    _lockService = lockService;
  }

  public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

}
