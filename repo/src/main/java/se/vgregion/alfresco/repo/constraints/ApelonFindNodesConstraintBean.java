package se.vgregion.alfresco.repo.constraints;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.ApelonNode;

public class ApelonFindNodesConstraintBean implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(ApelonFindNodesConstraintBean.class);

  private ApelonService _apelonService;

  public void setApelonService(ApelonService apelonService) {
    _apelonService = apelonService;
  }

  /**
   * Get allowed values from the Apelon Service for the passed namespace.
   * 
   * @param namespace
   *          The namespace to get the values for
   * @return A {@link List} of strings
   */
  public List<String> getAllowedValues(String namespace, String propertyName, String propertyValue) {
    Assert.hasText(namespace);

    List<String> result = new ArrayList<String>();

    try {
      List<ApelonNode> nodes = _apelonService.findNodes(namespace, propertyName, propertyValue, true);

      for (ApelonNode node : nodes) {
        result.add(node.getName());
      }
    } catch (Exception ex) {
      LOG.warn(ex.getMessage(), ex);
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_apelonService);
  }

}
