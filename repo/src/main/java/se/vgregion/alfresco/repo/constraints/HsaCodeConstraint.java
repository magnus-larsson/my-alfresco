package se.vgregion.alfresco.repo.constraints;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;

import se.vgregion.alfresco.repo.utils.ApplicationContextHolder;

public class HsaCodeConstraint extends ListOfValuesConstraint implements Serializable {

  private static final long serialVersionUID = 8968017162953457246L;

  private static final String NODE_TYPE = "apelon:hsacode";

  public static final String NODE_TYPE_PARAM = "nodeType";

  public ApelonNodeTypeConstraintBean getApelonNodeTypeConstraintBean() {
    return (ApelonNodeTypeConstraintBean) ApplicationContextHolder.getApplicationContext().getBean("vgr.hsaCodeConstraint");
  }

  @Override
  protected List<String> getRawAllowedValues() {
    return getApelonNodeTypeConstraintBean().getAllowedValues(NODE_TYPE);
  }

  @Override
  public String getType() {
    return "APELON_NODE_TYPE";
  }

  @Override
  public Map<String, Object> getParameters() {
    final Map<String, Object> parameters = super.getParameters();

    parameters.put(NODE_TYPE_PARAM, NODE_TYPE);
    parameters.put(ALLOWED_VALUES_PARAM, getRawAllowedValues());

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

}