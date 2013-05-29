package se.vgregion.alfresco.repo.push;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface PushService {

  public boolean pushFile(NodeRef nodeRef);

  public boolean pushFiles(List<NodeRef> nodeRefs);

  /**
   * Find files pushed for publishing, limit results by status.
   * @param publishStatus
   * @param unpublishStatus
   * @return
   */
  public List<NodeRef> findPushedFiles(String publishStatus, String unpublishStatus, Date startTime, Date endTime);
}
