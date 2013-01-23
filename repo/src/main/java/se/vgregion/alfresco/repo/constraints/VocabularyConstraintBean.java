package se.vgregion.alfresco.repo.constraints;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.ApelonNode;

/**
 * Bean for getting list of values for a specific path from the Vocabulary
 * Service.
 * 
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class VocabularyConstraintBean implements InitializingBean {

  private static final Logger LOG = Logger.getLogger(VocabularyConstraintBean.class);

  private ApelonService _apelonService;

  public void setApelonService(ApelonService apelonService) {
    _apelonService = apelonService;
  }

  /**
   * Get allowed values from the Vocabulary Service for the passed path.
   * 
   * @param path
   *          The path to get the values for
   * @return A {@link List} of strings
   */
  public List<String> getAllowedValues(String path) {
    Assert.hasText(path);

    List<String> result = new ArrayList<String>();

    try {
      List<ApelonNode> nodes = _apelonService.getVocabulary(path);

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
