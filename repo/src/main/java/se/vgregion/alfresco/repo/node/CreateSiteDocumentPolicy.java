package se.vgregion.alfresco.repo.node;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
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

  private static boolean _initialized = false;

  @Resource(name = "DictionaryService")
  protected DictionaryService _dictionaryService;

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " - onCreateNode begin");
    }
    final NodeRef fileNodeRef = childAssocRef.getChildRef();
    final NodeRef folderNodeRef = childAssocRef.getParentRef();

    runSafe(new DefaultRunSafe(fileNodeRef, _serviceUtils.getCurrentUserName()) {

      @Override
      public void execute() {
        doCreateOrMoveNode(fileNodeRef, folderNodeRef);
      }

    });
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " - onCreateNode end");
    }
  }

  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " - onMoveNode begin");
    }
    final NodeRef fileNodeRef = newChildAssocRef.getChildRef();
    final NodeRef folderNodeRef = newChildAssocRef.getParentRef();

    runSafe(new DefaultRunSafe(fileNodeRef, _serviceUtils.getCurrentUserName()) {

      @Override
      public void execute() {
        doCreateOrMoveNode(fileNodeRef, folderNodeRef);
      }

    });
    if (LOG.isTraceEnabled()) {
      LOG.trace(this.getClass().getName() + " - onMoveNode end");
    }
  }

  private void doCreateOrMoveNode(NodeRef fileNodeRef, NodeRef folderNodeRef) {
    if (!_nodeService.exists(fileNodeRef)) {
      return;
    }

    if (!_nodeService.exists(folderNodeRef)) {
      return;
    }

    if (!_nodeService.getType(fileNodeRef).isMatch(VgrModel.TYPE_VGR_DOCUMENT)) {
      return;
    }

    if (!_nodeService.hasAspect(fileNodeRef, VgrModel.ASPECT_STANDARD)) {
      return;
    }

    if (!_nodeService.hasAspect(folderNodeRef, VgrModel.ASPECT_METADATA)) {
      return;
    }

    if (_nodeService.hasAspect(fileNodeRef, VgrModel.ASPECT_DONOTTOUCH)) {
      // this node should not have it's properties copied, it's probably a copy
      // action
      return;
    }

    final Map<QName, Serializable> folderProperties = _nodeService.getProperties(folderNodeRef);

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

      _nodeService.setProperty(fileNodeRef, key, value);
    }

  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();
    if (!_initialized) {
      LOG.info("Initialized " + this.getClass().getName() + ".onMoveNode");
      LOG.info(this.getClass().getName() + ".onCreateNode " + " is handled by delegate class");
      _policyComponent.bindClassBehaviour(OnMoveNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onMoveNode", NotificationFrequency.TRANSACTION_COMMIT));
    }
  }

}
