package se.vgregion.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.model.VgrModel;
import se.vgregion.alfresco.repo.storage.StorageService;

/**
 * Moves a document to the storage that is placed in a folder with the
 * vgr:watched aspect.
 */
public class MoveWatchedDocumentsPolicy extends AbstractPolicy implements OnCreateNodePolicy {

  private final static Logger LOG = Logger.getLogger(MoveWatchedDocumentsPolicy.class);

  private StorageService _storageService;

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    final NodeRef file = childAssocRef.getChildRef();
    final NodeRef folder = childAssocRef.getParentRef();

    runSafe(new DefaultRunSafe(file) {

      @Override
      public void execute() {
        // if the node does not exist, don't do anything...
        if (!_nodeService.exists(file)) {
          return;
        }

        // if the node is not of type "vgr:document", exit
        if (!_nodeService.getType(file).isMatch(VgrModel.TYPE_VGR_DOCUMENT)) {
          return;
        }

        // if the node does not have the required aspect "vgr:standard", exit
        if (!_nodeService.hasAspect(file, VgrModel.ASPECT_STANDARD)) {
          return;
        }

        // if the nodes folder does not have the required aspect "watched", exit
        if (!_nodeService.hasAspect(folder, VgrModel.ASPECT_WATCHED)) {
          return;
        }

        // if we reach this point, all is well and good so let's move the file
        _storageService.moveToStorage(file);

        if (LOG.isDebugEnabled()) {
          LOG.debug(this.getClass().getName());
        }
      }

    });
  }

  public void setStorageService(final StorageService storageService) {
    _storageService = storageService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
  }

}
