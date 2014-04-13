package se.vgregion.alfresco.repo.push;

import org.alfresco.service.cmr.repository.NodeRef;

public interface PuSHAtomFeedUtil {

  /**
   * Create a publish document atom feed
   * 
   * @param nodeRef
   *          nodeRef of the publish document
   * @return
   */
  public String createPublishDocumentFeed(NodeRef nodeRef);
  
  /**
   * Create an unpublish document atom feed
   * 
   * @param nodeRef
   *          nodeRef of the unpublish document
   * @return
   */
  public String createUnPublishDocumentFeed(NodeRef nodeRef);
}
