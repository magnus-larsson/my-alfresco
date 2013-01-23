package se.vgregion.alfresco.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.redpill.alfresco.module.metadatawriter.model.MetadataWriterModel;
import org.springframework.util.Assert;
import se.vgregion.alfresco.repo.model.VgrModel;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;

public class AddPdfaPolicy extends AbstractPolicy implements OnContentUpdatePolicy {

  private final static Logger LOG = Logger.getLogger(AddPdfaPolicy.class);

  private ContentService _contentService;

  private Behaviour _behaviour;

  public void setContentService(ContentService contentService) {
    _contentService = contentService;
  }

  public void onContentUpdate(final NodeRef nodeRef, final boolean newContent) {
    // have to disable this particular behaviour, otherwise a endless loop is created
    _behaviour.disable();

    runSafe(new DefaultRunSafe(nodeRef) {

      @Override
      public void execute() {
        doContentUpdate(nodeRef, newContent);
      }

    });

    _behaviour.enable();
  }

  private void doContentUpdate(NodeRef nodeRef, boolean newContent) {
    // exit if node is not yet saved
    if (!_nodeService.exists(nodeRef)) {
      return;
    }

    final NodeRef pdfNodeRef = nodeRef;
    final NodeRef parentNodeRef = _nodeService.getPrimaryParent(pdfNodeRef).getParentRef();

    // this policy should only work for PDF/A parents that is published documents
    if (!_nodeService.hasAspect(parentNodeRef, VgrModel.ASPECT_PUBLISHED)) {
      return;
    }

    // first fix the .native properties
    populateNativeProperties(parentNodeRef, pdfNodeRef);

    Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    properties.put(MetadataWriterModel.PROP_METADATA_SERVICE_NAME, "vgr.metadata-writer.service");
    properties.put(MetadataWriterModel.PROP_METADATA_ASYNCHRONOUSLY, true);

    // add the metadata writer aspect and the correct service name for the
    // metadata to be written to the PDF file...
    _nodeService.addAspect(pdfNodeRef, MetadataWriterModel.ASPECT_METADATA_WRITEABLE, properties);

    // get all the properties from the node
    properties = _nodeService.getProperties(parentNodeRef);

    final Map<QName, Serializable> dcProperties = new HashMap<QName, Serializable>();

    // remove all properties that's not dc: ones
    for (final QName property : properties.keySet()) {
      if (property.getLocalName().startsWith("dc.")) {
        final Serializable value = properties.get(property);

        dcProperties.put(property, value);
      }
    }

    _nodeService.addProperties(pdfNodeRef, dcProperties);

    if (LOG.isInfoEnabled()) {
      LOG.info("Successfully update PDF/A for " + parentNodeRef);
    }
  }

  private void populateNativeProperties(final NodeRef nodeRef, final NodeRef pdfNodeRef) {
    runSafe(new DefaultRunSafe(nodeRef) {

      @Override
      public void execute() {
        final Serializable storedFilename = _nodeService.getProperty(nodeRef, VgrModel.PROP_TITLE_FILENAME);

        final ContentReader nativeContentReader = _contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

        final InputStream nativeInputStream = nativeContentReader.getContentInputStream();

        final Serializable nativeChecksum = _serviceUtils.getChecksum(nativeInputStream);
        final Serializable nativeExtension = _serviceUtils.getFileExtension(nodeRef, false);
        final Serializable nativeFilename = FilenameUtils.removeExtension((String) storedFilename) + "." + nativeExtension;
        final Serializable nativeIdentifier = _serviceUtils.getDocumentIdentifier(nodeRef, true);
        final Serializable nativeMimetype = _serviceUtils.getMimetype(nodeRef);

        _nodeService.setProperty(nodeRef, VgrModel.PROP_CHECKSUM_NATIVE, nativeChecksum);
        _nodeService.setProperty(nodeRef, VgrModel.PROP_TITLE_FILENAME_NATIVE, nativeFilename);
        _nodeService.setProperty(nodeRef, VgrModel.PROP_IDENTIFIER_NATIVE, nativeIdentifier);
        _nodeService.setProperty(nodeRef, VgrModel.PROP_FORMAT_EXTENT_MIMETYPE_NATIVE, nativeMimetype);
        _nodeService.setProperty(nodeRef, VgrModel.PROP_FORMAT_EXTENT_EXTENSION_NATIVE, nativeExtension);

        final ContentReader pdfContentReader = _contentService.getReader(pdfNodeRef, ContentModel.PROP_CONTENT);

        final InputStream pdfInputStream = pdfContentReader.getContentInputStream();

        final Serializable pdfChecksum = _serviceUtils.getChecksum(pdfInputStream);
        final Serializable pdfExtension = _serviceUtils.getFileExtension(pdfNodeRef, false);
        final Serializable pdfFilename = FilenameUtils.removeExtension((String) storedFilename) + "." + pdfExtension;
        final Serializable pdfIdentifier = _serviceUtils.getDocumentIdentifier(nodeRef);
        final Serializable pdfMimetype = _serviceUtils.getMimetype(pdfNodeRef);

        _nodeService.setProperty(nodeRef, VgrModel.PROP_CHECKSUM, pdfChecksum);
        _nodeService.setProperty(nodeRef, VgrModel.PROP_TITLE_FILENAME, pdfFilename);
        _nodeService.setProperty(nodeRef, VgrModel.PROP_IDENTIFIER, pdfIdentifier);
        _nodeService.setProperty(nodeRef, VgrModel.PROP_FORMAT_EXTENT_MIMETYPE, pdfMimetype);
        _nodeService.setProperty(nodeRef, VgrModel.PROP_FORMAT_EXTENT_EXTENSION, pdfExtension);
      }

    });
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    Assert.notNull(_contentService);

    _behaviour = new JavaBehaviour(this, "onContentUpdate", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);

    _policyComponent.bindClassBehaviour(OnContentUpdatePolicy.QNAME, ContentModel.TYPE_THUMBNAIL, _behaviour);
  }
}
