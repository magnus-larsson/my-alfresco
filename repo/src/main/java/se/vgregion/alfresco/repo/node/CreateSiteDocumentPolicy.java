package se.vgregion.alfresco.repo.node;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
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
public class CreateSiteDocumentPolicy extends AbstractPolicy implements OnCreateNodePolicy {

  private static final Logger LOG = Logger.getLogger(CreateSiteDocumentPolicy.class);

  @Resource(name = "DictionaryService")
  protected DictionaryService _dictionaryService;

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    final NodeRef fileNodeRef = childAssocRef.getChildRef();
    final NodeRef folderNodeRef = childAssocRef.getParentRef();

    runSafe(new DefaultRunSafe(fileNodeRef, _serviceUtils.getCurrentUserName()) {

      @Override
      public void execute() {
        doCreateNode(fileNodeRef, folderNodeRef);
      }

    });
  }

  private void doCreateNode(NodeRef fileNodeRef, NodeRef folderNodeRef) {
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

    if (LOG.isDebugEnabled()) {
      LOG.debug(this.getClass().getName());
    }
  }

}
