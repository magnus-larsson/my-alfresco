package se.vgregion.alfresco.repo.constraints;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;

import se.vgregion.alfresco.repo.model.VgrModel;

public class HsaCodeConstraintBean extends ApelonNodeTypeConstraintBean {

  @Override
  protected String getValue(final NodeRef nodeRef) {
    String value = super.getValue(nodeRef);

    String verksamhetskod = getVerksamhetskod(nodeRef);

    if (StringUtils.isBlank(value) || StringUtils.isBlank(verksamhetskod)) {
      return "";
    }

    return verksamhetskod + "#sep#" + value;
  }

  private String getVerksamhetskodUnauthorized(final NodeRef nodeRef) {
    final List<ChildAssociationRef> properties = _nodeService.getChildAssocs(nodeRef);

    for (final ChildAssociationRef property : properties) {
      final String key = (String) _nodeService.getProperty(property.getChildRef(), VgrModel.PROP_APELON_KEY);

      if (!key.equalsIgnoreCase("verksamhetskod")) {
        continue;
      }

      return getSingleValue(property.getChildRef(), VgrModel.PROP_APELON_VALUE);
    }

    return null;
  }

  private String getVerksamhetskod(final NodeRef nodeRef) {
    return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>() {

      @Override
      public String doWork() throws Exception {
        return getVerksamhetskodUnauthorized(nodeRef);
      }

    }, AuthenticationUtil.getSystemUserName());
  }

  private String getSingleValue(final NodeRef propertyNode, final QName property) {
    final Serializable value = _nodeService.getProperty(propertyNode, property);

    String result;

    if (value instanceof List) {
      @SuppressWarnings("unchecked")
      final List<String> list = (List<String>) value;

      result = list.size() > 0 ? list.get(0) : "";
    } else {
      result = value != null ? value.toString() : "";
    }

    return result;
  }

}
