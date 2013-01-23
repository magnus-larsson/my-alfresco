package se.vgregion.alfresco.repo.storage;

import org.alfresco.service.cmr.repository.NodeRef;

public interface StorageService {

  void publishToStorage(String nodeRef);

  void publishToStorage(NodeRef nodeRef);

  void unpublishFromStorage(String nodeRef);

  void moveToStorage(NodeRef fileNodeRef);

  NodeRef getPublishedNodeRef(final NodeRef nodeRef);

  boolean createPdfRendition(NodeRef nodeRef);

  boolean createPdfRendition(NodeRef nodeRef, boolean async);

  int createMissingPdfRenditions();

  boolean pdfaRendable(NodeRef nodeRef);

}
