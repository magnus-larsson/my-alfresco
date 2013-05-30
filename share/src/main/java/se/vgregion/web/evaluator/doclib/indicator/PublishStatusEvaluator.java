package se.vgregion.web.evaluator.doclib.indicator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;

public class PublishStatusEvaluator extends BaseEvaluator {

  @Override
  public boolean evaluate(JSONObject jsonObject) {
    Object property = getProperty(jsonObject, "vgr:dc.publisher.id");
    
    return property == null ? false : true;
  }

}
