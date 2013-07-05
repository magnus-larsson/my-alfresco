package se.vgregion.alfresco.repo.push;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface PushService {

  boolean pingPush();

  boolean pushFile(NodeRef nodeRef);

  boolean pushFiles(List<NodeRef> nodeRefs);

  /**
   * Find files pushed for publishing, limit results by status.
   * 
   * @param publishStatus
   * @param unpublishStatus
   * @return
   */
  List<NodeRef> findPushedFiles(String publishStatus, String unpublishStatus, Date startTime, Date endTime);

  List<NodeRef> findErroneousPushedFiles(Integer count, Integer minimumPushAge);

}
