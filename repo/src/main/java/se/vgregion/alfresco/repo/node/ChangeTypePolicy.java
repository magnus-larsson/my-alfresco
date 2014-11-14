package se.vgregion.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.log4j.Logger;
import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * Policy for changing all documents created in the document library to
 * vgr:document. Must be done in order to be able to use the Sharepoint feature.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class ChangeTypePolicy extends AbstractPolicy implements OnCreateNodePolicy {

  private static final Logger LOG = Logger.getLogger(ChangeTypePolicy.class);
  private static boolean _initialized = false;
  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " - onCreateNode begin");
    }
    final NodeRef nodeRef = childAssocRef.getChildRef();

    runSafe(new DefaultRunSafe(nodeRef) {

      @Override
      public void execute() {
        doCreateNode(nodeRef);
      }

    });
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " - onCreateNode end");
    }
  }

  private void doCreateNode(NodeRef nodeRef) {
    // if the node does not exist, exit
    if (!_nodeService.exists(nodeRef)) {
      return;
    }

    // don't do this for working copies
    if (_nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
      return;
    }

    // if it's not the spaces store, exit
    if (!StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(nodeRef.getStoreRef())) {
      return;
    }

    // if it's anything but locked, don't do anything
    if (_lockService.getLockStatus(nodeRef) != LockStatus.NO_LOCK) {
      return;
    }

    // if the node is already vgr:document, exit
    if (_nodeService.getType(nodeRef).isMatch(VgrModel.TYPE_VGR_DOCUMENT)) {
      return;
    }

    // if it's not a cm:content node, continue (it may be a forum node...)
    if (!_nodeService.getType(nodeRef).isMatch(ContentModel.TYPE_CONTENT)) {
      return;
    }

    // if the node is thumbnail, don't do anything
    if (_nodeService.getType(nodeRef).isMatch(ContentModel.TYPE_THUMBNAIL)) {
      return;
    }

    if (_nodeService.getType(nodeRef).isMatch(VgrModel.RD_PDF)) {
      return;
    }

    // if it's not the document library, exit
    if (!isDocumentLibrary(nodeRef)) {
      return;
    }

    _nodeService.setType(nodeRef, VgrModel.TYPE_VGR_DOCUMENT);

    if (LOG.isDebugEnabled()) {
      LOG.debug(this.getClass().getName());
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    if (!_initialized) {
      LOG.info("Initialized " + this.getClass().getName());
      _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.EVERY_EVENT));
    }
  }
}
