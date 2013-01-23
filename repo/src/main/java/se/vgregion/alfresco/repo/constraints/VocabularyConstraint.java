package se.vgregion.alfresco.repo.constraints;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;

import se.vgregion.alfresco.repo.utils.ApplicationContextHolder;

public class VocabularyConstraint extends ListOfValuesConstraint implements Serializable {

  private static final long serialVersionUID = 8968017162953457246L;

  private String _path;

  public VocabularyConstraint() {
  }

  @Override
  public void initialize() {
  }

  public String getPath() {
    return _path;
  }

  public void setPath(String path) {
    _path = path;
  }

  public VocabularyConstraintBean getVocabylaryConstraintBean() {
    return (VocabularyConstraintBean) ApplicationContextHolder.getApplicationContext().getBean(
        "vgr.VocabularyConstraint");
  }

  @Override
  public List<String> getAllowedValues() {
    return getVocabylaryConstraintBean().getAllowedValues(_path);
  }

  @Override
  public String getType() {
    return "APELON";
  }

  @Override
  public Map<String, Object> getParameters() {
    Map<String, Object> parameters = new HashMap<String, Object>(1);

    parameters.put("path", _path);

    return parameters;
  }

  @Override
  protected void evaluateSingleValue(Object value) {
  }

}