package se.vgregion.alfresco.repo.node;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * On document creation, adds swedish as default language for vgr:dc.language,
 * and also sets the access right to Intranät as default.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class DefaultSwedishLanguagePolicy extends AbstractPolicy implements OnCreateNodePolicy {

  private static final Logger LOG = Logger.getLogger(DefaultSwedishLanguagePolicy.class);

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    final NodeRef nodeRef = childAssocRef.getChildRef();

    if (shouldSkipPolicy(nodeRef)) {
      return;
    }

    setDefaultLanguage(nodeRef);

    setDefaultAccessRight(nodeRef);

    if (LOG.isDebugEnabled()) {
      LOG.debug(this.getClass().getName());
    }
  }

  private void setDefaultLanguage(final NodeRef nodeRef) {
    final Serializable language = _nodeService.getProperty(nodeRef, VgrModel.PROP_LANGUAGE);

    if (language == null) {
      _nodeService.setProperty(nodeRef, VgrModel.PROP_LANGUAGE, VgrModel.DEFAULT_LANGUAGE);
    }
  }

  private void setDefaultAccessRight(final NodeRef nodeRef) {
    final Serializable accessRight = _nodeService.getProperty(nodeRef, VgrModel.PROP_ACCESS_RIGHT);

    if (accessRight == null) {
      _nodeService.setProperty(nodeRef, VgrModel.PROP_ACCESS_RIGHT, VgrModel.DEFAULT_ACCESS_RIGHT);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode",
        NotificationFrequency.TRANSACTION_COMMIT));
  }

}
