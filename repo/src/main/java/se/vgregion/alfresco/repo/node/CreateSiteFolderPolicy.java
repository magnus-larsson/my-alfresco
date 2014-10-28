package se.vgregion.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * Whenever a folder is created in a site, add the aspect vgr:metadata to it
 */
public class CreateSiteFolderPolicy extends AbstractPolicy implements OnCreateNodePolicy {

  private static final Logger LOG = Logger.getLogger(CreateSiteFolderPolicy.class);

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    final NodeRef folder = childAssocRef.getChildRef();

    if (!_nodeService.exists(folder)) {
      return;
    }

    // if the node already has the aspect, just exit
    if (_nodeService.hasAspect(folder, VgrModel.ASPECT_METADATA)) {
      return;
    }

    // if the node isn't in any documentLibrary, exit
    if (!isDocumentLibrary(folder)) {
      return;
    }

    // it must be a pure cm:folder...
    if (!_nodeService.getType(folder).isMatch(ContentModel.TYPE_FOLDER)) {
      return;
    }

    // just add the standard aspect
    runSafe(new DefaultRunSafe(folder) {

      @Override
      public void execute() {
        _nodeService.addAspect(folder, VgrModel.ASPECT_METADATA, null);
      }

    });

    if (LOG.isDebugEnabled()) {
      LOG.debug(this.getClass().getName());
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.EVERY_EVENT));
  }

}
