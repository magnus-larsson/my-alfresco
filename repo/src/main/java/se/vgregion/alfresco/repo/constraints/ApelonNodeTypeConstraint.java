package se.vgregion.alfresco.repo.constraints;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.i18n.MessageLookup;

import se.vgregion.alfresco.repo.utils.ApplicationContextHolder;

public class ApelonNodeTypeConstraint extends ListOfValuesConstraint implements Serializable {

  private static final long serialVersionUID = 8968017162953457246L;

  private String _nodeType;

  public String getNodeType() {
    return _nodeType;
  }

  public void setNodeType(final String nodeType) {
    _nodeType = nodeType;
  }

  public ApelonNodeTypeConstraintBean getApelonNodeTypeConstraintBean() {
    return (ApelonNodeTypeConstraintBean) ApplicationContextHolder.getApplicationContext().getBean(
        "vgr.ApelonNodeTypeConstraint");
  }

  @Override
  protected List<String> getRawAllowedValues() {
    return getApelonNodeTypeConstraintBean().getAllowedValues(_nodeType);
  }

  @Override
  public String getType() {
    return "APELON_NODE_TYPE";
  }

  @Override
  public Map<String, Object> getParameters() {
    final Map<String, Object> parameters = super.getParameters();

    parameters.put("nodeType", _nodeType);
    parameters.put("allowedValues", getRawAllowedValues());

    return parameters;
  }

  @Override
  public void initialize() {
    // must override to nullify check later on in super class
  }

  @Override
  protected void evaluateSingleValue(final Object value) {
 // must override to nullify check later on in super class
  }
  
  @Override
  public String getDisplayLabel(String constraintAllowableValue, MessageLookup messageLookup) {
    return constraintAllowableValue;
  }

}