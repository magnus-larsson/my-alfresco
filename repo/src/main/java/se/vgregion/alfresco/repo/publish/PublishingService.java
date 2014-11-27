package se.vgregion.alfresco.repo.publish;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface PublishingService {

  String findPublishedDocumentsQuery(Date availableDate);
  
  String findPublishedDocumentsQuery(Date availableDate, Date modifiedFrom, Date modifiedTo);

  String findPublishedDocumentsQuery(Date availableDate, Date modifiedFrom, Date modifiedTo, boolean excludeAlreadyPushed);

  String findUnpublishedDocumentsQuery(Date availableDate);
  
  String findUnpublishedDocumentsQuery(Date availableDate, Date modifiedFrom, Date modifiedTo);

  String findUnpublishedDocumentsQuery(Date availableDate, Date modifiedFrom, Date modifiedTo, boolean excludeAlreadyPushed);

  List<NodeRef> findPublishedDocuments(Date availableDate);

  void findPublishedDocuments(Date availableDate, Date modifiedFrom, Date modifiedTo, NodeRefCallbackHandler callback, boolean excludeAlreadyPushed, Integer maxItems, Integer skipCount);

  List<NodeRef> findUnpublishedDocuments(Date availableDate);

  void findUnpublishedDocuments(Date availableDate, Date modifiedFrom, Date modifiedTo, NodeRefCallbackHandler callback, boolean excludeAlreadyPushed, Integer maxItems, Integer skipCount);

  boolean isPublished(NodeRef nodeRef);

  boolean isPublished(NodeRef nodeRef, boolean excludeAlreadyPushed);
  
  boolean isPublished(NodeRef nodeRef, boolean excludeAlreadyPushed, boolean onlyOK);

  boolean isPublished(String nodeRef);

  boolean isPublished(String nodeRef, boolean excludeAlreadyPushed);

  boolean isPublished(String nodeRef, boolean excludeAlreadyPushed, boolean onlyOK);

}
