package se.vgregion.alfresco.repo.constraints;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import se.vgregion.alfresco.repo.utils.ApplicationContextHolder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DocumentStructureConstraint extends ListOfValuesConstraint implements Serializable {

  private static final String NODE_TYPE = "apelon:documentStructure";

  public ApelonNodeTypeConstraintBean getApelonNodeTypeConstraintBean() {
    return (ApelonNodeTypeConstraintBean) ApplicationContextHolder.getApplicationContext().getBean("vgr.documentStructureConstraint");
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

    parameters.put("nodeType", NODE_TYPE);
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

}