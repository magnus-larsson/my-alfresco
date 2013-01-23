package se.vgregion.alfresco.repo.constraints;

import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public abstract class AbstractConstraintBean implements InitializingBean {

  protected NodeService _nodeService;

  public void setNodeService(final NodeService nodeService) {
    _nodeService = nodeService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_nodeService);
  }

}
