package se.vgregion.alfresco.repo.scripts;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import se.vgregion.alfresco.repo.model.VgrModel;

public class SetPublishStatus extends DeclarativeWebScript implements InitializingBean {

  private static final String TYPE_PUBLISH_STATUS = "publish";
  private static final String TYPE_UNPUBLISH_STATUS = "unpublish";
  private NodeService _nodeService;
  private Properties _globalProperties;
  private BehaviourFilter _behaviourFilter;
  private static final Logger LOG = Logger.getLogger(SetPublishStatus.class);

  @Override
  protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
    Map<String, Object> model = new HashMap<String, Object>();
    String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();

    String allowedUsersProperty = _globalProperties.getProperty("vgr.publishstatus.allowedUsers", "admin");
    String[] allowedUsers;

    if (allowedUsersProperty.contains(",")) {
      allowedUsers = allowedUsersProperty.split(",");
    } else {
      allowedUsers = new String[1];
      allowedUsers[0] = allowedUsersProperty;
    }

    boolean userIsAllowed = false;
    for (String allowedUser : allowedUsers) {
      if (fullyAuthenticatedUser.equalsIgnoreCase(allowedUser)) {
        userIsAllowed = true;
        break;
      }
    }

    // Validation of user
    if (!userIsAllowed) {
      LOG.warn("The user " + fullyAuthenticatedUser + " tried to access the set publish status web script." + " However the user is not on the allowed users list: " + allowedUsersProperty);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("You do not have permission to access the requested resource");
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String storeType = templateVars.get("store_type");
    String storeId = templateVars.get("store_id");
    String nodeId = templateVars.get("node_id");
    if (nodeId.indexOf("&") >= 0) {
      nodeId = nodeId.substring(0, nodeId.indexOf("&"));
    }
    // Validation of parameters
    String documentId = storeType + "://" + storeId + "/" + nodeId;

    final String publishingStatus = req.getParameter("status");

    final String type = req.getParameter("type");

    if (StringUtils.isBlank(documentId)) {
      LOG.error("The required parameter documentId is not set.");
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("The required parameter documentId is not set.");
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }

    final NodeRef nodeRef;

    try {
      nodeRef = new NodeRef(documentId);
    } catch (Exception e) {
      LOG.error("Malformed document id: " + documentId, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Malformed document id: " + e.getMessage());
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }

    if (!_nodeService.exists(nodeRef)) {
      LOG.error("Document with id " + documentId + " does not exist.");
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Document with id " + documentId + " does not exist.");
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }

    if (StringUtils.isBlank(publishingStatus)) {
      LOG.error("The required parameter status is not set.");
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("The required parameter status is not set.");
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }

    if (!TYPE_PUBLISH_STATUS.equalsIgnoreCase(type) && !TYPE_UNPUBLISH_STATUS.equalsIgnoreCase(type)) {
      LOG.error("The required parameter type is invalid: " + type + ". Expected " + TYPE_PUBLISH_STATUS + " or " + TYPE_UNPUBLISH_STATUS);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("The required parameter type is invalid: " + type + ". Expected " + TYPE_PUBLISH_STATUS + " or " + TYPE_UNPUBLISH_STATUS);
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }

    if (!_nodeService.hasAspect(nodeRef, VgrModel.ASPECT_PUBLISHED)) {
      LOG.error("Document " + documentId + " does not have the required aspect " + VgrModel.ASPECT_PUBLISHED.toPrefixString());
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Document " + documentId + " does not have the required aspect " + VgrModel.ASPECT_PUBLISHED.toPrefixString());
      status.setRedirect(true);
      model.put("result", "ERROR");
      return model;
    }

    AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        _behaviourFilter.disableBehaviour(nodeRef);

        try {
          if (TYPE_PUBLISH_STATUS.equals(type)) {
            _nodeService.setProperty(nodeRef, VgrModel.PROP_PUBLISH_STATUS, publishingStatus);
          } else {
            _nodeService.setProperty(nodeRef, VgrModel.PROP_UNPUBLISH_STATUS, publishingStatus);
          }
        } finally {
          _behaviourFilter.enableBehaviour(nodeRef);
        }

        return null;
      }
    });

    model.put("result", "OK");

    if (LOG.isDebugEnabled()) {
      LOG.debug("Setting " + type + " status " + publishingStatus + " for node " + nodeRef);
    }

    return model;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ParameterCheck.mandatory("nodeService", _nodeService);
    ParameterCheck.mandatory("globalProperties", _globalProperties);
    ParameterCheck.mandatory("behaviourFilter", _behaviourFilter);
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setGlobalProperties(Properties globalProperties) {
    _globalProperties = globalProperties;
  }

  public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

}
