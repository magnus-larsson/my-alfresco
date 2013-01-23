package se.vgregion.alfresco.repo.constraints;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;

import se.vgregion.alfresco.repo.utils.ApplicationContextHolder;

public class ApelonFindNodesConstraint extends ListOfValuesConstraint implements Serializable {

  private static final long serialVersionUID = 8968017162953457246L;

  private String _namespace;

  private String _propertyName;

  private String _propertyValue;

  public ApelonFindNodesConstraint() {
  }

  @Override
  public void initialize() {
  }

  public String getNamespace() {
    return _namespace;
  }

  public void setNamespace(String namespace) {
    _namespace = namespace;
  }

  public String getPropertyName() {
    return _propertyName;
  }

  public void setPropertyName(String propertyName) {
    _propertyName = propertyName;
  }

  public String getPropertyValue() {
    return _propertyValue;
  }

  public void setPropertyValue(String propertyValue) {
    _propertyValue = propertyValue;
  }

  public ApelonFindNodesConstraintBean getApelonFindNodesConstraintBean() {
    return (ApelonFindNodesConstraintBean) ApplicationContextHolder.getApplicationContext().getBean(
        "vgr.ApelonFindNodesConstraint");
  }

  @Override
  public List<String> getAllowedValues() {
    return getApelonFindNodesConstraintBean().getAllowedValues(_namespace, _propertyName, _propertyValue);
  }

  @Override
  public String getType() {
    return "APELON_FIND_NODES";
  }

  @Override
  public Map<String, Object> getParameters() {
    Map<String, Object> parameters = new HashMap<String, Object>();

    parameters.put("namespace", _namespace);
    parameters.put("propertyName", _propertyName);
    parameters.put("propertyValue", _propertyValue);

    return parameters;
  }

  @Override
  protected void evaluateSingleValue(Object value) {
  }

}