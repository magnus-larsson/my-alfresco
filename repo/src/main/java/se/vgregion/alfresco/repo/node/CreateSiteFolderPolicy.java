package se.vgregion.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import se.vgregion.alfresco.repo.model.VgrModel;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Whenever a folder is created in a site, add the aspect vgr:metadata to it
 */
public class CreateSiteFolderPolicy extends AbstractPolicy implements OnCreateNodePolicy {

  private static final Logger LOG = Logger.getLogger(CreateSiteFolderPolicy.class);

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    final NodeRef folderNodeRef = childAssocRef.getChildRef();

    // if the node isn't in any documentLibrary, exit
    if (!isDocumentLibrary(folderNodeRef)) {
      return;
    }

    // it must be a pure cm:folder...
    if (!_nodeService.getType(folderNodeRef).isMatch(ContentModel.TYPE_FOLDER)) {
      return;
    }

    // just add the standard aspect
    runSafe(new DefaultRunSafe(folderNodeRef) {

      @Override
      public void execute() {
        _nodeService.addAspect(folderNodeRef, VgrModel.ASPECT_METADATA, new HashMap<QName, Serializable>());
      }

    });

    if (LOG.isDebugEnabled()) {
      LOG.debug(this.getClass().getName());
    }
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();

    _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this, "onCreateNode",
            Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
  }

}
