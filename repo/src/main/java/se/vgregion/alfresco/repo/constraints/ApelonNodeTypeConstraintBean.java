package se.vgregion.alfresco.repo.constraints;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import se.vgregion.alfresco.repo.model.VgrModel;

/**
 * Bean for getting list of values for a specific node type from the Apelon
 * Service.
 * 
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class ApelonNodeTypeConstraintBean extends AbstractConstraintBean {

  private static final Logger LOG = Logger.getLogger(ApelonNodeTypeConstraintBean.class);

  protected ThreadLocal<List<String>> _allowedValues = new ThreadLocal<List<String>>();

  protected ApelonService _apelonService;

  public void setApelonService(ApelonService apelonService) {
    _apelonService = apelonService;
  }

  /**
   * Get allowed values from the Vocabulary Service for the passed node type.
   * 
   * @param nodeType
   *          The nodeType to get the values for
   * @return A {@link List} of strings
   */
  public List<String> getAllowedValues(String nodeType) {
    if (_allowedValues.get() != null) {
      return _allowedValues.get();
    }

    _allowedValues.set(new ArrayList<String>());

    Assert.hasText(nodeType);

    try {
      List<NodeRef> nodes = _apelonService.getNodes(nodeType);

      for (NodeRef nodeRef : nodes) {
        String value = getValue(nodeRef);

        _allowedValues.get().add(StringUtils.isNotBlank(value) ? value : "");
      }
    } catch (Exception ex) {
      LOG.warn(ex.getMessage(), ex);
    }

    return _allowedValues.get();
  }

  protected String getValue(NodeRef nodeRef) {
    Serializable name = getName(nodeRef);

    return name != null ? name.toString() : "";
  }

  protected Serializable getName(final NodeRef nodeRef) {
    return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Serializable>() {

      @Override
      public Serializable doWork() throws Exception {
        return _nodeService.getProperty(nodeRef, VgrModel.PROP_APELON_NAME);
      }

    }, AuthenticationUtil.getSystemUserName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(_apelonService);

    super.afterPropertiesSet();
  }

}
