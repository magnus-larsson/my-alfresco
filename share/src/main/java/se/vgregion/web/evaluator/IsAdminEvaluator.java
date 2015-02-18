package se.vgregion.web.evaluator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;

public class IsAdminEvaluator extends BaseEvaluator {

  @Override
  public boolean evaluate(JSONObject jsonObject) {
    Boolean isadmin = Boolean.FALSE;

    RequestContext requestContext = ThreadLocalRequestContext.getRequestContext();

    String userId = requestContext.getUserId();

    try {
      Connector connector = requestContext.getServiceRegistry().getConnectorService().getConnector("alfresco", userId, ServletUtil.getSession());

      Response response = connector.call("/vgr/admin/isadmin?username=" + userId);

      if (response.getStatus().getCode() == Status.STATUS_OK) {
        JSONObject json = (JSONObject) JSONValue.parseWithException(response.getResponse());

        isadmin = (Boolean) json.get("isadmin");
      }

      return isadmin;
    } catch (Exception ex) {
      throw new AlfrescoRuntimeException("Failed to fetch admin status for user '" + userId + "'");
    }
  }
}
