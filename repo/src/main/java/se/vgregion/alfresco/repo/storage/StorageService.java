package se.vgregion.alfresco.repo.storage;

import java.util.List;

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

  int createMissingPdfRenditions(CreationCallback creationCallback);

  boolean pdfaRendable(NodeRef nodeRef);

  /**
   * Gets the latest published storage version of a document
   * 
   * @param nodeRef
   *          Original document id
   * @return
   */
  public NodeRef getLatestPublishedStorageVersion(final String nodeRef);

  /**
   * Gets an expecit version of a document in the storage
   * 
   * @param nodeRef
   *          Original document id
   * @param version
   * @return
   */
  public NodeRef getPublishedStorageVersion(final String nodeRef, final String version);

  /**
   * Gets the latest storage version of a document
   * 
   * @param nodeRef
   *          Original document id
   * @return
   */
  public NodeRef getLatestStorageVersion(final String nodeRef);

  /**
   * Gets all storage versions of a document
   * 
   * @param nodeRef
   *          Original document id
   * @return
   */
  public List<NodeRef> getStorageVersions(final String nodeRef);

}
