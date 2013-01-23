package se.vgregion.alfresco.repo.scripts;

import org.alfresco.repo.processor.BaseProcessorExtension;

import org.alfresco.service.cmr.repository.NodeRef;
import se.vgregion.alfresco.repo.storage.StorageService;

public class ScriptStorageService extends BaseProcessorExtension {

  private StorageService _storageService;

  public void setStorageService(final StorageService storageService) {
    _storageService = storageService;
  }
  
  public void publishToStorage(final String nodeRef) {
    _storageService.publishToStorage(nodeRef);
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
