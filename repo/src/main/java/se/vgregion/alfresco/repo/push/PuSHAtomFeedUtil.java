package se.vgregion.alfresco.repo.push;

import java.io.OutputStream;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

public interface PuSHAtomFeedUtil {

  /**
   * Create a publish document atom feed
   * 
   * @param nodeRef
   *          nodeRef of the publish document
   * @return
   */
  String createPublishDocumentFeed(NodeRef nodeRef);

  /**
   * Create an unpublish document atom feed
   * 
   * @param nodeRef
   *          nodeRef of the unpublish document
   * @return
   */
  String createUnPublishDocumentFeed(NodeRef nodeRef);

  void createDocumentFeed(Date from, Date to, OutputStream outputStream, boolean excludeAlreadyPushed);

}
