package se.vgregion.alfresco.repo.scripts;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

import se.vgregion.alfresco.repo.storage.StorageService;

public class ScriptStorageService extends BaseProcessorExtension {

  private static final Logger LOG = Logger.getLogger(ScriptStorageService.class);

  private StorageService _storageService;

  public void setStorageService(final StorageService storageService) {
    _storageService = storageService;
  }

  public void publishToStorage(String nodeRef, Boolean async) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("About to publish '" + nodeRef + "'");
    }

    NodeRef publishedNodeRef = _storageService.publishToStorage(nodeRef, async);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Published '" + nodeRef + "' to '" + publishedNodeRef + "'");
    }
  }

  public void unpublishFromStorage(final String nodeRef) {
    _storageService.unpublishFromStorage(nodeRef);
  }

  public int createMissingPdfRenditions() {
    return _storageService.createMissingPdfRenditions();
  }

  public boolean pdfaRendable(NodeRef nodeRef) {
    return _storageService.pdfaRendable(nodeRef);
  }

}
