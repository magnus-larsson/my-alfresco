package se.vgregion.alfresco.repo.storage;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

public interface StorageService {
  public static final String ORIGIN_BARIUM = "Barium";
  public static final String ORIGIN_ALFRESCO = "Alfresco";

  public static final String STORAGE_LAGRET = "Lagret";
  public static final String STORAGE_BARIUM = "Barium";
  public static final String STORAGE_BARIUM_VERSIONS = "versions";
  NodeRef publishToStorage(String nodeRef);

  NodeRef publishToStorage(String nodeRef, boolean async);

  NodeRef publishToStorage(NodeRef nodeRef);

  NodeRef publishToStorage(NodeRef nodeRef, boolean async);
  
  void unpublishFromStorage(String nodeRef);

  void moveToStorage(NodeRef fileNodeRef);

  NodeRef getPublishedNodeRef(final NodeRef nodeRef);

  boolean createPdfaRendition(NodeRef nodeRef);

  boolean createPdfaRendition(NodeRef nodeRef, boolean async);

  boolean createPdfaRendition(NodeRef nodeRef, Long timeout);

  NodeRef getPdfaRendition(NodeRef nodeRef);

  int createMissingPdfRenditions();

  int createMissingPdfRenditions(CreationCallback creationCallback);

  NodeRef getStorageNodeRef();

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

  Map<String, FailedRenditionInfo> getFailedRenditions(NodeRef nodeRef);

  NodeRef getOrCreatePdfaRendition(NodeRef nodeRef);

  /**
   * Checks for the existance of a document in storage
   * @param id
   * @param version
   * @param origin
   * @return
   */
  public boolean documentExistInStorage(String id, String version, String origin);

}
