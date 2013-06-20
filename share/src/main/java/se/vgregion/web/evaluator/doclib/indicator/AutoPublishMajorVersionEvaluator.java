package se.vgregion.web.evaluator.doclib.indicator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;

public class AutoPublishMajorVersionEvaluator extends BaseEvaluator {

  @Override
  public boolean evaluate(JSONObject jsonObject) {
    if (!getNodeAspects(jsonObject).contains("vgr:auto-publish")) {
      return false;
    }

    Object property = getProperty(jsonObject, "vgr:auto_publish_major_version");

    return property == null ? false : property.toString().equalsIgnoreCase("true");
  }

}
