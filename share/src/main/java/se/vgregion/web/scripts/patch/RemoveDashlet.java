package se.vgregion.web.scripts.patch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.ModelObject;
import org.springframework.extensions.surf.ModelObjectService;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.types.Component;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.Response;

/**
 * Removes a dashlet for all users in the system who have an account.
 *
 * @author Niklas Ekman (niklas.ekman@redpill-linpro.com)
 */
public class RemoveDashlet extends DeclarativeWebScript {

  private static final Logger LOG = Logger.getLogger(RemoveDashlet.class);

  protected static final String ENDPOINT_ID = "alfresco";
  protected static final String ALFRESCO_PROXY = "/proxy/alfresco";

  private ModelObjectService _modelObjectService;

  public void setModelObjectService(final ModelObjectService modelObjectService) {
    _modelObjectService = modelObjectService;
  }

  @Override
  protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
    final Map<String, Object> result = new HashMap<String, Object>();

    /*
     * final Map<String, String> templateVariables =
     * req.getServiceMatch().getTemplateVars();
     *
     * final String dashlet = templateVariables.get("dashletname"); final String
     * username = templateVariables.get("username");
     */

    final String dashlet = req.getParameter("dashletname");
    final String username = req.getParameter("username");

    if (StringUtils.isBlank(username)) {
      for (char firstLetter = 'a'; firstLetter <= 'z'; firstLetter++) {
        for (char secondLetter = 'a'; secondLetter <= 'z'; secondLetter++) {
          final String filter = String.valueOf(firstLetter) + String.valueOf(secondLetter);

          LOG.debug("Removing dashlet '" + dashlet + "' for users with filter '" + filter + "'");

          removeDashletForUsers(dashlet, filter);
        }
      }
    } else {
      removeDashletForUser(dashlet, username);
    }

    return result;
  }

  private void removeDashletForUser(final String dashlet, final String username) {
    removeDashboardComponent(username, "/components/dashlets/" + dashlet);
  }

  private void removeDashletForUsers(final String dashlet, final String filter) {
    final List<String> users = getUsers(filter);

    for (final String user : users) {
      removeDashboardComponent(user, "/components/dashlets/" + dashlet);
    }
  }

  private void removeDashboardComponent(final String user, final String componentUrl) {
    final String sourceId = "user/" + user + "/dashboard";

    final Map<String, ModelObject> types = _modelObjectService.findComponents("page", null, sourceId, null);

    for (final String type : types.keySet()) {
      final Component component = (Component) types.get(type);

      if (!component.getURL().equals(componentUrl)) {
        continue;
      }

      _modelObjectService.unbindComponent("page", component.getRegionId(), sourceId);

      LOG.debug("Dashlet '" + componentUrl + "' removed for user '" + user + "'");
    }
  }

  private List<String> getUsers(final String filter) {
    try {
      final RequestContext requestContext = ThreadLocalRequestContext.getRequestContext();

      final ConnectorService connectorService = requestContext.getServiceRegistry().getConnectorService();

      final String currentUserId = requestContext.getUserId();

      final HttpSession currentSession = ServletUtil.getSession(true);

      final Connector connector = connectorService.getConnector(ENDPOINT_ID, currentUserId, currentSession);

      final String alfrescoURL = "/vgr/peoplewithdashboard?filter=" + filter;

      final Response response = connector.call(alfrescoURL);

      final String jsonResponse = response.getResponse();

      if (StringUtils.isBlank(jsonResponse)) {
        return new ArrayList<String>();
      }

      final JSONObject json = new JSONObject(new JSONTokener(jsonResponse));

      final JSONArray list = json.getJSONArray("users");

      final List<String> users = new ArrayList<String>();

      for (int x = 0; x < list.length(); x++) {
        final JSONObject user = list.getJSONObject(x);

        users.add(user.getString("userName"));
      }

      return users;
    } catch (final Exception ex) {
      return new ArrayList<String>();
    }
  }

}
