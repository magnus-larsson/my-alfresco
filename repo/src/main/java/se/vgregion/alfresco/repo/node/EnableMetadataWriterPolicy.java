package se.vgregion.alfresco.repo.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckOut;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.redpill.alfresco.module.metadatawriter.factories.MetadataContentFactory;
import org.redpill.alfresco.module.metadatawriter.model.MetadataWriterModel;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * On document creation, adds the metadatawriter aspect on all vgr:document
 * nodes. Also adds the aspect on checkout to support all old documents.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class EnableMetadataWriterPolicy extends AbstractPolicy implements OnCheckOut, OnCreateNodePolicy {

  private static final Logger LOG = Logger.getLogger(EnableMetadataWriterPolicy.class);

  private MetadataContentFactory _metadataContentFactory;

  public void setMetadataContentFactory(final MetadataContentFactory metadataContentFactory) {
    _metadataContentFactory = metadataContentFactory;
  }

  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    final NodeRef nodeRef = childAssocRef.getChildRef();

    runSafe(new DefaultRunSafe(nodeRef) {

      @Override
      public void execute() {
        if (shouldSkipPolicy(nodeRef)) {
          return;
        }

        addMetadataWriterStuff(nodeRef);

        if (LOG.isDebugEnabled()) {
          LOG.debug(this.getClass().getName());
        }
      }

    });
  }

  @Override
  public void onCheckOut(final NodeRef workingCopy) {
    runSafe(new DefaultRunSafe(workingCopy) {

      @Override
      public void execute() {
        if (!_nodeService.exists(workingCopy)) {
          return;
        }

        // don't do this for working copies
        if (!_nodeService.hasAspect(workingCopy, ContentModel.ASPECT_WORKING_COPY)) {
          return;
        }

        // if it's not the spaces store, exit
        if (!StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(workingCopy.getStoreRef())) {
          return;
        }

        // if it's not the document library, exit
        if (!isDocumentLibrary(workingCopy)) {
          return;
        }

        if (!_nodeService.getType(workingCopy).isMatch(VgrModel.TYPE_VGR_DOCUMENT)) {
          return;
        }

        addMetadataWriterStuff(workingCopy);

        if (LOG.isDebugEnabled()) {
          LOG.debug(this.getClass().getName());
        }
      }

    });
  }

  /**
   * Only add the metadata stuff if the mime type is supported.
   *
   * @param nodeRef
   */
  private void addMetadataWriterStuff(final NodeRef nodeRef) {
    ContentData content = (ContentData) _nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);

    // if there's no content, do nothing...
    if (content == null || content.getSize() == 0) {
      return;
    }

    if (!_metadataContentFactory.supportsMetadataWrite(nodeRef)) {
      return;
    }

    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(MetadataWriterModel.PROP_METADATA_SERVICE_NAME, "vgr.metadata-writer.service");
    properties.put(MetadataWriterModel.PROP_METADATA_ASYNCHRONOUSLY, true);

    // add the metadata writer aspect and the correct service name for the
    // metadata to be written to the file...

    _nodeService.addAspect(nodeRef, MetadataWriterModel.ASPECT_METADATA_WRITEABLE, properties);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    Assert.notNull(_metadataContentFactory);

    _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode",
        NotificationFrequency.TRANSACTION_COMMIT));

    _policyComponent.bindClassBehaviour(OnCheckOut.QNAME, VgrModel.TYPE_VGR_DOCUMENT, new JavaBehaviour(this, "onCheckOut",
        NotificationFrequency.TRANSACTION_COMMIT));
  }
}
