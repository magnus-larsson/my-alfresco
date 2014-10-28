package se.vgregion.alfresco.repo.node;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * Class for adding metadata to a newly created document based on if the
 * containing folder has a certain aspect and metadata attached to it.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 * @version $Id$
 */
public class CreateSiteDocumentPolicy extends AbstractPolicy implements OnCreateNodePolicy, OnMoveNodePolicy {

  private static final Logger LOG = Logger.getLogger(CreateSiteDocumentPolicy.class);

  private DictionaryService _dictionaryService;

  public void setDictionaryService(final DictionaryService dictionaryService) {
    _dictionaryService = dictionaryService;
  }

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    final NodeRef file = childAssocRef.getChildRef();
    final NodeRef folder = childAssocRef.getParentRef();

    runSafe(new DefaultRunSafe(file, _serviceUtils.getCurrentUserName()) {

      @Override
      public void execute() {
        doUpdateNode(file, folder);
      }

    });
  }

  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    final NodeRef file = newChildAssocRef.getChildRef();
    final NodeRef folder = newChildAssocRef.getParentRef();

    runSafe(new DefaultRunSafe(file, _serviceUtils.getCurrentUserName()) {

      @Override
      public void execute() {
        doUpdateNode(file, folder);
      }

    });
  }

  private void doUpdateNode(NodeRef file, NodeRef folder) {
    if (!_nodeService.exists(file)) {
      return;
    }

    if (!_nodeService.exists(folder)) {
      return;
    }

    if (!_nodeService.getType(file).isMatch(VgrModel.TYPE_VGR_DOCUMENT)) {
      return;
    }

    if (!_nodeService.hasAspect(file, VgrModel.ASPECT_STANDARD)) {
      return;
    }

    if (!_nodeService.hasAspect(folder, VgrModel.ASPECT_METADATA)) {
      return;
    }

    if (_nodeService.hasAspect(file, VgrModel.ASPECT_DONOTTOUCH)) {
      // this node should not have it's properties copied, it's probably a copy
      // action
      return;
    }

    final Map<QName, Serializable> folderProperties = _nodeService.getProperties(folder);

    for (final Entry<QName, Serializable> folderProperty : folderProperties.entrySet()) {
      final QName key = folderProperty.getKey();

      final AspectDefinition aspectMetadata = _dictionaryService.getAspect(VgrModel.ASPECT_METADATA);

      if (!aspectMetadata.getProperties().containsKey(folderProperty.getKey())) {
        continue;
      }

      final Serializable value = folderProperty.getValue();

      if (value == null) {
        continue;
      }

      if (StringUtils.isEmpty(value.toString())) {
        continue;
      }

      _nodeService.setProperty(file, key, value);
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(this.getClass().getName());
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, VgrModel.TYPE_VGR_DOCUMENT, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.EVERY_EVENT));
    _policyComponent.bindClassBehaviour(OnMoveNodePolicy.QNAME, VgrModel.TYPE_VGR_DOCUMENT, new JavaBehaviour(this, "onMoveNode", NotificationFrequency.EVERY_EVENT));
  }

}
