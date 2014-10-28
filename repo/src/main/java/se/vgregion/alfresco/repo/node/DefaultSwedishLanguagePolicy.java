package se.vgregion.alfresco.repo.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnSetNodeTypePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * On document creation, adds swedish as default language for vgr:dc.language,
 * and also sets the access right to Intranät as default.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class DefaultSwedishLanguagePolicy extends AbstractPolicy implements OnCreateNodePolicy, OnSetNodeTypePolicy {

  private static final Logger LOG = Logger.getLogger(DefaultSwedishLanguagePolicy.class);

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    addDefaults(childAssocRef.getChildRef());
  }

  @Override
  public void onSetNodeType(NodeRef nodeRef, QName oldType, QName newType) {
    if (!newType.isMatch(_nodeService.getType(nodeRef))) {
      return;
    }
    
    addDefaults(nodeRef);
  }

  private void addDefaults(final NodeRef file) {
    runSafe(new DefaultRunSafe(file) {

      @Override
      public void execute() {
        if (shouldSkipPolicy(file)) {
          return;
        }

        setDefaultLanguage(file);

        setDefaultAccessRight(file);
      }

    });

    if (LOG.isDebugEnabled()) {
      LOG.debug(this.getClass().getName());
    }
  }

  private void setDefaultLanguage(final NodeRef file) {
    final Serializable language = _nodeService.getProperty(file, VgrModel.PROP_LANGUAGE);

    if (language == null) {
      List<String> languages = new ArrayList<String>();
      languages.add(VgrModel.DEFAULT_LANGUAGE);

      _nodeService.setProperty(file, VgrModel.PROP_LANGUAGE, (Serializable) languages);
    }
  }

  private void setDefaultAccessRight(final NodeRef file) {
    final Serializable accessRight = _nodeService.getProperty(file, VgrModel.PROP_ACCESS_RIGHT);

    if (accessRight == null) {
      List<String> accessRights = new ArrayList<String>();
      accessRights.add(VgrModel.DEFAULT_ACCESS_RIGHT);

      _nodeService.setProperty(file, VgrModel.PROP_ACCESS_RIGHT, (Serializable) accessRights);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.EVERY_EVENT));
    _policyComponent.bindClassBehaviour(OnSetNodeTypePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onSetNodeType", NotificationFrequency.EVERY_EVENT));
  }

}
