package se.vgregion.alfresco.repo.push;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface PushService {

  public boolean pushFile(NodeRef nodeRef);

  public boolean pushFiles(List<NodeRef> nodeRefs);

}
