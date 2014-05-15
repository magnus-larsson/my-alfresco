package se.vgregion.alfresco.repo.publish;

import org.alfresco.service.cmr.repository.NodeRef;

public interface NodeRefCallbackHandler {

  public void processNodeRef(NodeRef nodeRef);

}
