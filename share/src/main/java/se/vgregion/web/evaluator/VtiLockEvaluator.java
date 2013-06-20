package se.vgregion.web.evaluator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;

public class VtiLockEvaluator extends BaseEvaluator {

  @Override
  public boolean evaluate(JSONObject jsonObject) {
    Object lockType = getProperty(jsonObject, "cm:lockType");

    return lockType != null && "WRITE_LOCK".equalsIgnoreCase(lockType.toString());
  }

}
